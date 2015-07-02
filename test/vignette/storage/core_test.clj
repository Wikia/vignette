(ns vignette.storage.core-test
  (:require [clojure.java.io :as io]
            [clojure.java.shell :refer [sh]]
            [midje.sweet :refer :all]
            [vignette.media-types :as mt]
            [vignette.storage.core :refer :all]
            [vignette.storage.local :refer :all]
            [vignette.storage.protocols :refer :all]))

(def local-path "/tmp/vignette-local-storage")

(def sample-media-hash {:wikia "lotr"
                        :image-type "images"
                        :top-dir "3"
                        :middle-dir "35"
                        :request-type :original
                        :original "arwen.png"})

(def sample-thumbnail-hash {:wikia "lotr"
                            :image-type "images"
                            :top-dir "3"
                            :middle-dir "35"
                            :request-type :thumbnail
                            :original "arwen.png"
                            :mode "resize"
                            :height "10"
                            :width "10"})


(facts :local-image-storage
  (let [store (create-image-storage ..disk-store..)]

    (save-thumbnail store ..file.. ..map..) => truthy
    (provided
      (put* ..disk-store.. ..file.. ..map.. mt/thumbnail-path) => true)

    (get-thumbnail store ..map..) => "bytes"
    (provided
      (get* ..disk-store.. ..map.. mt/thumbnail-path) => "bytes")

    (save-original store ..file.. ..map..) => true
    (provided
      (put* ..disk-store.. ..file.. ..map.. mt/original-path) => true)

    (get-original store ..map..) => "bytes"
    (provided
      (get* ..disk-store.. ..map.. mt/original-path) => "bytes")))

(with-state-changes
  [(before :facts (do
                    (sh "rm" "-rf" local-path)
                    (sh "mkdir" "-p" local-path)))]

  (facts :save-thumbnail :integration
    (let [local (create-local-storage-system local-path)
          image-store (create-image-storage local)
          resource (create-stored-object (io/file "image-samples/ropes.jpg"))]
      local => truthy
      image-store => truthy
      (save-thumbnail image-store resource sample-thumbnail-hash) => truthy))

  (facts :save-thumbnail :not :cache-thumbnails
    (let [local (create-local-storage-system local-path)
          image-store (create-image-storage local false)
          resource (create-stored-object (io/file "image-samples/ropes.jpg"))]
      local => truthy
      image-store => truthy
      (save-thumbnail image-store resource sample-thumbnail-hash) => falsey))

  (facts :get-thumbnail :integration
    (let [local (create-local-storage-system local-path)
          image-store (create-image-storage local)
          resource (create-stored-object (io/file "image-samples/ropes.jpg"))]
      (get-thumbnail image-store sample-thumbnail-hash) => falsey
      (save-thumbnail image-store resource sample-thumbnail-hash) => truthy
      (get-thumbnail image-store sample-thumbnail-hash) => truthy))

  (facts :get-thumbnail :not :cache-thumbnails
    (let [local (create-local-storage-system local-path)
          image-store (create-image-storage local false)
          resource (create-stored-object (io/file "image-samples/ropes.jpg"))]
      (get-thumbnail image-store sample-thumbnail-hash) => falsey
      (save-thumbnail image-store resource sample-thumbnail-hash) => falsey
      (get-thumbnail image-store sample-thumbnail-hash) => falsey))


  (facts :save-original :integration
    (let [local (create-local-storage-system local-path)
          image-store (create-image-storage local)
          resource (create-stored-object (io/file "image-samples/ropes.jpg"))]
      (save-original image-store resource sample-media-hash) => truthy
      (original-exists? image-store sample-media-hash) => true
      (original-exists? image-store {:image-type "images"}) => false))

  (facts :get-original :integration
    (let [local (create-local-storage-system local-path)
          image-store (create-image-storage local)
          resource (create-stored-object (io/file "image-samples/ropes.jpg"))]
      (save-original image-store resource sample-media-hash) => truthy
      (original-exists? image-store sample-media-hash) => true
      (get-original image-store sample-media-hash) => truthy)))
