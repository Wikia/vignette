(ns vignette.storage.local
  (:require [vignette.storage.protocols :refer :all]
            [vignette.media-types :as mt]
            [vignette.util.filesystem :refer :all]
            [clojure.java.io :as io]
            [clojure.java.shell :as sh])
  (:import  (java.io FileInputStream)))

(defrecord LocalObjectStorage [directory]
  ObjectStorageProtocol

 (get-object [this bucket path]
   (let [real-file (io/file (resolve-local-path (:directory this) bucket path))]
     (when (file-exists? real-file)
       real-file)))

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
