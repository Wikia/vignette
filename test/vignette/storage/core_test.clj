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


(facts :local-image-storage :unit
  (let [store (create-local-image-storage ..disk-store.. "originals" "thumbs")
        file-name "a/bc/d.png"]

    (save-thumbnail store ..file.. ..map..) => truthy
    (provided
      (mt/wikia ..map..) => "lotr"
      (mt/thumbnail-path ..map..) => file-name
      (put* ..disk-store.. ..file.. "lotr" "thumbs" file-name) => true)

    (get-thumbnail store ..map..) => "bytes"
    (provided
      (mt/wikia ..map..) => "lotr"
      (mt/thumbnail-path ..map..) => file-name
      (get* ..disk-store.. "lotr" "thumbs" file-name) => "bytes")

    (save-original store ..file.. ..map..) => true
    (provided
      (mt/wikia ..map..) => "lotr"
      (mt/original-path ..map..) => file-name
      (put* ..disk-store.. ..file.. "lotr" "originals" file-name) => true)

    (get-original store ..map..) => "bytes"
    (provided
      (mt/wikia ..map..) => "lotr"
      (mt/original-path ..map..) => file-name
      (get* ..disk-store.. "lotr" "originals" file-name) => "bytes")))

(with-state-changes
  [(before :facts (do
                    (sh "rm" "-rf" local-path)
                    (sh "mkdir" "-p" local-path)))]

  (facts :save-thumbnail :integration
    (let [local (create-local-object-storage local-path)
          image-store (create-local-image-storage local "originals" "thumbs")]
      local => truthy
      image-store => truthy
      (save-thumbnail image-store (io/file "image-samples/ropes.jpg") sample-thumbnail-hash) => truthy))

  (facts :get-thumbnail :integration
    (let [local (create-local-object-storage local-path)
          image-store (create-local-image-storage local "originals" "thumbs")]
      (get-thumbnail image-store sample-thumbnail-hash) => falsey
      (save-thumbnail image-store (io/file "image-samples/ropes.jpg") sample-thumbnail-hash) => truthy
      (get-thumbnail image-store sample-thumbnail-hash) => truthy))


  (facts :save-original :integration
    (let [local (create-local-object-storage local-path)
          image-store (create-local-image-storage local "originals" "thumbs")]
      (save-original image-store (io/file "image-samples/ropes.jpg") sample-media-hash) => truthy))

  (facts :get-original :integration
    (let [local (create-local-object-storage local-path)
          image-store (create-local-image-storage local "originals" "thumbs")]
      (save-original image-store (io/file "image-samples/ropes.jpg") sample-media-hash) => truthy
      (get-original image-store sample-media-hash) => truthy)))
