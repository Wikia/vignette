(ns vignette.storage.local
  (:require [vignette.storage.protocols :refer :all]
            [vignette.media-types :as mt]
            [clojure.java.io :as io]
            [clojure.java.shell :as sh]))



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



