(ns vignette.util.thumbnail-test
  (:require [clojure.java.io :as io]
            [midje.sweet :refer :all]
            [vignette.protocols :refer :all]
            [vignette.storage.local :refer [create-stored-object]]
            [vignette.storage.protocols :refer :all]
            [vignette.util.filesystem :refer :all]
            [vignette.util.thumbnail :refer :all]))

(def beach-map {:request-type :thumbnail
                :original "beach.jpg"
                :middle-dir "3"
                :top-dir "35"
                :wikia "lotr"
                :thumbnail-mode "thumbnail"
                :height "100"
                :width "100"})

(def beach-file (io/file "images-samples/beach.jpg"))

(facts :original-thumbnail
       ; successful run
       (original->thumbnail beach-file beach-map) => truthy
       (provided
         (run-thumbnailer anything) => {:exit 0})

       ; failed run
       (original->thumbnail beach-file beach-map) => (throws Exception)
       (provided
         (run-thumbnailer anything) => {:exit 1 :err 256 :out "testing failure"}))

(facts :generate-thumbnail
  (generate-thumbnail ..system.. beach-map) => ..object..
  (provided
    (store ..system..) => ..store..
    (get-original ..store.. beach-map) => ..original..
    (original->local ..original.. beach-map) => ..local..
    (original->thumbnail ..local.. beach-map) => ..thumb..
    (background-delete-file ..local..) => true
    (create-stored-object ..thumb..) => ..object..)
  
  (generate-thumbnail ..system.. beach-map) => falsey
  (provided
    (store ..system..) => ..store..
    (get-original ..store.. beach-map) => nil)

  (generate-thumbnail ..system.. beach-map) => falsey
  (provided
    (store ..system..) => ..store..
    (get-original ..store.. beach-map) => ..original..
    (original->local ..original.. beach-map) => ..local..
    (background-delete-file ..local..) => true
    (original->thumbnail ..local.. beach-map) => nil)) 

(facts :get-or-generate-thumbnail
       ; get existing
       (get-or-generate-thumbnail ..system.. beach-map) => ..thumb..
       (provided
         (store ..system..) => ..store..
         (get-thumbnail ..store.. beach-map) => ..thumb..)

       ; generate new - success
       (get-or-generate-thumbnail ..system.. beach-map) => ..thumb..
       (provided
         (store ..system..) => ..store..
         (get-thumbnail ..store.. beach-map) => false
         (generate-thumbnail ..system.. beach-map) => ..thumb..
         (background-save-thumbnail ..store.. ..thumb.. beach-map) => true)

       ; generate new - fail
       (let [image-dne (assoc beach-map :original "doesnotexist.jpg")]
         (get-or-generate-thumbnail ..system.. image-dne) => nil
         (provided
           (store ..system..) => ..store..
           (get-thumbnail ..store.. image-dne) => false
           (get-original ..store.. image-dne) => false))

       ; fall through when :replace is set
       (let [option-map (assoc-in beach-map [:options :replace] "true")]
         (get-or-generate-thumbnail ..system.. option-map) => ..new-thumb..
         (provided
           (store ..system..) => ..store..
           (generate-thumbnail ..system.. option-map) => ..new-thumb..
           (background-save-thumbnail ..store.. ..new-thumb.. option-map) => true)))

(facts :route-map->thumb-args
       (route-map->thumb-args beach-map) => (contains ["--height" "100" "--width" "100"
                                                      "--mode" "thumbnail"] :in-any-order))
