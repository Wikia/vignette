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
         (run-thumbnailer anything) => {:exit 0})

       ; failed run
       (original->thumbnail beach-file beach-map) => (throws Exception)
       (provided
         (run-thumbnailer anything) => {:exit 1 :err 256 :out "testing failure"})

       ;special mime type
       (generate-thumbnail ..store.. beach-map nil) => ..file..
       (provided
         (get-original ..store.. beach-map) => ..file..
         (filename ..file..) => "file.ogv"))

(facts :orignal->local-maintains-file-extension
       (.getName (original->local
                   (ls/create-stored-object (io/file "project.clj")))) => (just #"[-0-9a-f]{36}\.clj"))

(facts :orignal->local-handles-no-file-extension
       (.getName (original->local
                   (ls/create-stored-object (io/file "LICENSE")))) => (just #"[-0-9a-f]{36}"))

(facts :orignal->local-handles-no-file-extension-with-invalid-chars
       (original->local ..stored-object..) => ..tmp-file..
       (provided
         (filename ..stored-object..) => "LICEN.S#().)_E"
         (io/file (temp-filename nil nil)) => ..tmp-file..
         (transfer! ..stored-object.. ..tmp-file..) => ..tmp-file..))

(facts :orignal->local-handles-file-exension-with-valid-chars
       (original->local ..stored-object..) => ..tmp-file..
       (provided
         (filename ..stored-object..) => "LICEN.S#().Ejpg"
         (io/file (temp-filename nil "Ejpg")) => ..tmp-file..
         (transfer! ..stored-object.. ..tmp-file..) => ..tmp-file..))


(facts :generate-thumbnail
       (generate-thumbnail ..store.. beach-map nil) => ..object..
       (provided
         (get-original ..store.. beach-map) => ..original..
         (original->local ..original..) => ..local..
         (filename ..original..) => ..filename..
         (is-passthrough-required ..original-mime-type.. beach-map) => false
         (original->thumbnail ..local.. beach-map) => ..thumb..
         (background-check-and-delete-original beach-map & anything) => nil
         (create-stored-object ..thumb.. & anything) => ..object..
         (mime-type-of ..filename..) => ..original-mime-type..)

       (generate-thumbnail ..store.. beach-map nil) => (throws ExceptionInfo)
       (provided
         (get-original ..store.. beach-map) => nil)

       (generate-thumbnail ..store.. beach-map nil) => falsey
       (provided
         (get-original ..store.. beach-map) => ..original..
         (original->local ..original..) => ..local..
         (filename ..original..) => ..filename..
         (is-passthrough-required ..original-mime-type.. beach-map) => false
         (original->thumbnail ..local.. beach-map) => nil
         (mime-type-of ..filename..) => ..original-mime-type..))

(facts :get-or-generate-thumbnail
       ; get existing
       (get-or-generate-thumbnail ..store.. beach-map) => ..thumb..
       (provided
         (get-thumbnail ..store.. beach-map) => ..thumb..)

       ; generate new - success
       (get-or-generate-thumbnail ..store.. beach-map) => ..thumb..
       (provided
         (get-thumbnail ..store.. beach-map) => false
         (generate-thumbnail ..store.. beach-map nil) => ..thumb..)

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
           (generate-thumbnail ..store.. option-map nil) => ..new-thumb..)))

(facts :route-map->thumb-args
       (route-map->thumb-args beach-map) => (contains ["--height" "100" "--width" "100"
                                                       "--mode" "thumbnail"] :in-any-order))

(facts :passthrough-mime-types
       (is-passthrough-required "image/png" {}) => false
       (is-passthrough-required "image/jpeg" {}) => false
       (is-passthrough-required "audio/ogg" {}) => true
       (is-passthrough-required "video/ogg" {}) => true
       (is-passthrough-required "image/png" {:thumbnail-mode "type-convert" :options {}}) => true
       (is-passthrough-required "image/png" {:thumbnail-mode "type-convert" :options {:format nil}}) => true
       (is-passthrough-required "image/png" {:thumbnail-mode "type-convert" :options {:format "webp"}}) => false
       (is-passthrough-required "image/bmp" {:thumbnail-mode "type-convert" :options {:format "webp"}}) => true)
