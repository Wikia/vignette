(ns vignette.storage.core-test
  (:require [vignette.storage.core :refer :all]
            [vignette.storage.protocols :refer :all]
            [vignette.storage.local :refer :all]
            [vignette.media-types :as mt]
            [clojure.java.shell :refer (sh)]
            [clojure.java.io :as io]
            [midje.sweet :refer :all]))

(def local-path "/tmp/vignette-local-storage")

(def sample-media-hash {:wikia "lotr"
                        :top-dir "3"
                        :middle-dir "35"
                        :request-type :original
                        :original "arwen.png"})

(def sample-thumbnail-hash {:wikia "lotr"
                            :top-dir "3"
                            :middle-dir "35"
                            :request-type :thumbnail
                            :original "arwen.png"
                            :mode "resize"
                            :height "10"
                            :width "10"})


(facts :local-image-storage
  (let [store (create-image-storage ..disk-store.. "originals" "thumbs")
        file-name "a/bc/d.png"]

    (save-thumbnail store ..file.. ..map..) => truthy
    (provided
      (put* ..disk-store.. ..file.. ..map.. "thumbs" mt/thumbnail-path) => true)

    (get-thumbnail store ..map..) => "bytes"
    (provided
      (get* ..disk-store.. ..map.. "thumbs" mt/thumbnail-path) => "bytes")

    (save-original store ..file.. ..map..) => true
    (provided
      (put* ..disk-store.. ..file.. ..map.. "originals" mt/original-path) => true)

    (get-original store ..map..) => "bytes"
    (provided
      (get* ..disk-store.. ..map.. "originals" mt/original-path) => "bytes")))

(with-state-changes
  [(before :facts (do
                    (sh "rm" "-rf" local-path)
                    (sh "mkdir" "-p" local-path)))]

  (facts :save-thumbnail :integration
    (let [local (create-local-storage-system local-path)
          image-store (create-image-storage local "originals" "thumbs")
          resource (create-stored-object (io/file "image-samples/ropes.jpg"))]
      local => truthy
      image-store => truthy
      (save-thumbnail image-store resource sample-thumbnail-hash) => truthy))

  (facts :get-thumbnail :integration
    (let [local (create-local-storage-system local-path)
          image-store (create-image-storage local "originals" "thumbs")
          resource (create-stored-object (io/file "image-samples/ropes.jpg"))]
      (get-thumbnail image-store sample-thumbnail-hash) => falsey
      (save-thumbnail image-store resource sample-thumbnail-hash) => truthy
      (get-thumbnail image-store sample-thumbnail-hash) => truthy))


  (facts :save-original :integration
    (let [local (create-local-storage-system local-path)
          image-store (create-image-storage local "originals" "thumbs")
          resource (create-stored-object (io/file "image-samples/ropes.jpg"))]
      (save-original image-store resource sample-media-hash) => truthy))

  (facts :get-original :integration
    (let [local (create-local-storage-system local-path)
          image-store (create-image-storage local "originals" "thumbs")
          resource (create-stored-object (io/file "image-samples/ropes.jpg"))]
      (save-original image-store resource sample-media-hash) => truthy
      (get-original image-store sample-media-hash) => truthy)))
