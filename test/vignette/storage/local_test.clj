(ns vignette.storage.local-test
  (:require [vignette.storage.core :refer :all]
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


(fact
  (file-exists? "project.clj") => truthy)

(with-state-changes
  [(before :facts (do
                    (sh "rm" "-rf" local-path)
                    (sh "mkdir" "-p" local-path)))]
  (facts :transfer
     (transfer! "project.clj" (format "%s/tbar" local-path)) => truthy)

  (facts :put-object
    (let [local (create-local-object-storage "/tmp/vignette-local-storage")]
      local => truthy
      (put-object local "project.clj" "bucket" "bar") => truthy))

  (facts :get-object
    (let [local (create-local-object-storage "/tmp/vignette-local-storage")]
      local => truthy
      (get-object local "bucket" "bar") => falsey
      (put-object local "project.clj" "bucket" "bar") => truthy
      (get-object local "bucket" "bar") => string?))

  (facts :delete-object
    (let [local (create-local-object-storage "/tmp/vignette-local-storage")]
      local => truthy
      (delete-object local "bucket" "bar") => falsey
      (put-object local "project.clj" "bucket" "bar") => truthy
      (delete-object local "bucket" "bar") => truthy)))


(with-state-changes
  [(before :facts (do
                    (sh "rm" "-rf" local-path)
                    (sh "mkdir" "-p" local-path)))]

  (facts :save-thumbnail
    (let [local (create-local-object-storage "/tmp/vignette-local-storage")
          image-store (create-local-image-storage local "originals" "thumbs")]
      local => truthy
      image-store => truthy
      (save-thumbnail image-store (io/file "image-samples/ropes.jpg") sample-thumbnail-hash) => truthy))

  (facts :get-thumbnail
    (let [local (create-local-object-storage "/tmp/vignette-local-storage")
          image-store (create-local-image-storage local "originals" "thumbs")]
      local => truthy
      image-store => truthy
      (get-thumbnail image-store sample-thumbnail-hash) => falsey
      (save-thumbnail image-store (io/file "image-samples/ropes.jpg") sample-thumbnail-hash) => truthy
      (get-thumbnail image-store sample-thumbnail-hash) => truthy)))
