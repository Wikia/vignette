(ns vignette.storage.core-test
  (:require [vignette.storage.core :refer :all]
            [vignette.storage.protocols :refer :all]
            [vignette.storage.local :refer :all]
            [clojure.java.shell :refer (sh)]
            [clojure.java.io :as io]
            [midje.sweet :refer :all]))

(def local-path "/tmp/vignette-local-storage")

(def sample-media-hash {:wikia "lotr"
                        :top-dir "3"
                        :middle-dir "35"
                        :type "original"
                        :original "arwen.png"})

(def sample-thumbnail-hash {:wikia "lotr"
                            :top-dir "3"
                            :middle-dir "35"
                            :type "thumbnail"
                            :original "arwen.png"
                            :mode "resize"
                            :height "10"
                            :width "10"})

(with-state-changes
  [(before :facts (do
                    (sh "rm" "-rf" local-path)
                    (sh "mkdir" "-p" local-path)))]

  (facts :save-thumbnail :integration
    (let [local (create-local-object-storage "/tmp/vignette-local-storage")
          image-store (create-local-image-storage local "originals" "thumbs")]
      local => truthy
      image-store => truthy
      (save-thumbnail image-store (io/file "image-samples/ropes.jpg") sample-thumbnail-hash) => truthy))

  (facts :get-thumbnail :integration
    (let [local (create-local-object-storage "/tmp/vignette-local-storage")
          image-store (create-local-image-storage local "originals" "thumbs")]
      local => truthy
      image-store => truthy
      (get-thumbnail image-store sample-thumbnail-hash) => falsey
      (save-thumbnail image-store (io/file "image-samples/ropes.jpg") sample-thumbnail-hash) => truthy
      (get-thumbnail image-store sample-thumbnail-hash) => truthy)))
