(ns vignette.util.thumbnail-test
  (:require [clojure.java.io :as io]
            [midje.sweet :refer :all]
            [pantomime.mime :refer [mime-type-of]]
            [vignette.protocols :refer :all]
            [vignette.storage.local :refer [create-stored-object]]
            [vignette.storage.protocols :refer :all]
            [vignette.util.filesystem :refer :all]
            [vignette.util.thumbnail :refer :all]
            [vignette.storage.local :as ls])
  (:import [clojure.lang ExceptionInfo]))

(def beach-map {:request-type   :thumbnail
                :original       "beach.jpg"
                :middle-dir     "3"
                :top-dir        "35"
                :wikia          "lotr"
                :thumbnail-mode "thumbnail"
                :height         "100"
                :width          "100"})

(def beach-file (io/file "image-samples/beach.jpg"))

(facts :original-thumbnail
       ; successful run
       (original->thumbnail beach-file beach-map) => truthy
       (provided
         (mime-type-of beach-file) => "image/jpg"
         (run-thumbnailer anything) => {:exit 0})

       ; failed run
       (original->thumbnail beach-file beach-map) => (throws Exception)
       (provided
         (run-thumbnailer anything) => {:exit 1 :err 256 :out "testing failure"})

       ;invalid mime type
       (original->thumbnail ..file.. beach-map) => (throws Exception)
       (provided
         (mime-type-of ..file..) => "video/ogg"))

(facts :orignal->local-maintains-file-extension
       (.getName (original->local
                   (ls/create-stored-object (io/file "project.clj")))) => (just #"[-0-9a-f]{36}\.clj"))

(facts :orignal->local-handles-no-file-extension
       (.getName (original->local
                   (ls/create-stored-object (io/file "LICENSE")))) => (just #"[-0-9a-f]{36}"))

(facts :generate-thumbnail
       (generate-thumbnail ..store.. beach-map) => ..object..
       (provided
         (get-original ..store.. beach-map) => ..original..
         (original->local ..original..) => ..local..
         (original->thumbnail ..local.. beach-map) => ..thumb..
         (background-delete-file ..local..) => true
         (create-stored-object ..thumb.. & anything) => ..object..)

       (generate-thumbnail ..store.. beach-map) => (throws ExceptionInfo)
       (provided
         (get-original ..store.. beach-map) => nil)

       (generate-thumbnail ..store.. beach-map) => falsey
       (provided
         (get-original ..store.. beach-map) => ..original..
         (original->local ..original..) => ..local..
         (background-delete-file ..local..) => true
         (original->thumbnail ..local.. beach-map) => nil))

(facts :get-or-generate-thumbnail
       ; get existing
       (get-or-generate-thumbnail ..store.. beach-map) => ..thumb..
       (provided
         (get-thumbnail ..store.. beach-map) => ..thumb..)

       ; generate new - success
       (get-or-generate-thumbnail ..store.. beach-map) => ..thumb..
       (provided
         (get-thumbnail ..store.. beach-map) => false
         (generate-thumbnail ..store.. beach-map) => ..thumb..)

       ; generate new - fail
       (let [image-dne (assoc beach-map :original "doesnotexist.jpg")]
         (get-or-generate-thumbnail ..store.. image-dne) => (throws ExceptionInfo)
         (provided
           (get-thumbnail ..store.. image-dne) => false
           (get-original ..store.. image-dne) => false))

       ; fall through when :replace is set
       (let [option-map (assoc-in beach-map [:options :replace] "true")]
         (get-or-generate-thumbnail ..store.. option-map) => ..new-thumb..
         (provided
           (generate-thumbnail ..store.. option-map) => ..new-thumb..)))

(facts :route-map->thumb-args
       (route-map->thumb-args beach-map) => (contains ["--height" "100" "--width" "100"
                                                       "--mode" "thumbnail"] :in-any-order))

(facts :assert-original-mime-type
       (assert-original-mime-type "" {}) => nil
       (assert-original-mime-type "file.jpg" {}) => nil
       (assert-original-mime-type "file.ogv" {}) => (throws ExceptionInfo))
