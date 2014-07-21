(ns vignette.storage.local
  (:require [vignette.storage.core :refer :all]
            [vignette.media-types :as mt]
            [clojure.java.io :as io]
            [clojure.java.shell :as sh]))


(defn- join-slash
  [& s]
  (clojure.string/join "/" s))

(defrecord LocalImageStorage [store original-prefix thumb-prefix]
  ImageStorageProtocol

  ; ^thumb-map mt/->MediaThmubnailFile :- bool
  (save-thumbnail [this resource thumb-map]
    (let [path (mt/thumbnail-path thumb-map)]
      (put-object (:store this)
                  resource
                  (mt/wikia thumb-map)
                 (join-slash (:thumb-prefix this) path))))

  ; ^thumb-map mt/->MediaThumbnailFile :- resource
  (get-thumbnail [this thumb-map]
    (let [path (mt/thumbnail-path thumb-map)]
      (get-object (:store this)
                  (mt/wikia thumb-map)
                  (join-slash (:thumb-prefix this) path))))

  (save-original [this resource original-map])
  (get-original [this original-map]))


(defn create-local-image-storage
  [store original-prefix thumb-prefix]
  (->LocalImageStorage store original-prefix thumb-prefix))


(declare resolve-local-path)
(declare create-local-path)
(declare get-parent)
(declare transfer!)
(declare file-exists?)

(defrecord LocalObjectStorage [directory]
  ObjectStorageProtocol

 (get-object [this bucket path]
   (let [real-path-object (io/file (resolve-local-path (:directory this) bucket path))]
     (when (file-exists? real-path-object)
       (slurp real-path-object))))

 (put-object [this resource bucket path]
   (let [real-path (resolve-local-path (:directory this) bucket path)]
     (create-local-path (get-parent real-path))
     (transfer! resource real-path)))

  (delete-object [this bucket path]
    (let [real-path (resolve-local-path (:directory this) bucket path)]
     (let [status (io/delete-file real-path :silently true)]
       (if (= status :silently)
         false
         true))))

  (list-buckets [this])
  (list-objects [this bucket]) )

(defn create-local-object-storage
  [directory]
  (->LocalObjectStorage directory))

(defn resolve-local-path
  [directory bucket path]
  (format "%s/%s/%s" directory bucket path))

(defn get-parent
  [path]
  (.getParent (io/file path)))

(defn create-local-path
  [path]
  (.mkdirs (io/file path)))

; i think this should be a multimethod that dispatches
; based on the type
(defn transfer!
  [in out]
  (io/copy (io/file in) (io/file out))
  (file-exists? out))

(defn file-exists?
  [file]
  (.exists (io/file file)))



