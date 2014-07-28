(ns vignette.util.thumbnail_test
  (:require [vignette.util.thumbnail :refer :all]
            [vignette.protocols :refer :all]
            [vignette.storage.protocols :refer :all]
            [midje.sweet :refer :all]
            [vignette.media-types :as mt]
            [clojure.java.io :as io]))

(def beach-map {:type :thumbnail
          :original "beach.jpg"
          :middle-dir "3"
          :top-dir "35"
          :wikia "lotr"
          :mode "resize"
          :height "100"
          :width "100"})

(def beach-file (io/file "images-samples/beach.jpg"))

(facts :get-or-generate-thumbnail
       ; get existing
       (get-or-generate-thumbnail ..system..
                                  (mt/get-media-map beach-map)) => ..file..
       (provided
         (get-thumbnail ..store.. beach-map) => ..file..
         (store ..system..) => ..store..)

       ; generate new - success
       (get-or-generate-thumbnail ..system..
                                  (mt/get-media-map beach-map)) => ..thumb..
       (provided
         (store ..system..) => ..store..
         (get-thumbnail ..store.. beach-map) => false
         (get-original ..store.. beach-map) => beach-file
         (generate-thumbnail beach-file beach-map) => ..thumb..
         (save-thumbnail ..store.. ..thumb.. beach-map) => true)

       ; generate new - fail
       (let [image-dne (mt/get-media-map (merge
                                           beach-map
                                           {:original "doesnotexist.jpg"}))]
         (get-or-generate-thumbnail ..system.. image-dne) => nil
         (provided
           (store ..system..) => ..store..
           (get-thumbnail ..store.. image-dne) => false
           (get-original ..store.. image-dne) => false)))

(facts :generate-thumbnail
       ; successful run
       (generate-thumbnail beach-file beach-map) => truthy
       (provided
         (run-thumbnailer anything) => {:exit 0})

       ; failed run
       (generate-thumbnail beach-file beach-map) => (throws Exception)
       (provided
         (run-thumbnailer anything) => {:exit 1 :err 256 :out "testing failure"}))

(facts :thumbnail-options
       (thumbnail-options beach-map) => (contains ["--height" "100" "--width" "100"
                                                   "--mode" "resize"] :in-any-order))