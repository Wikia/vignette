(ns vignette.storage.local
  (:require (vignette.storage [protocols :refer :all]
                              [common :refer :all])
            [vignette.media-types :as mt]
            [vignette.util.filesystem :refer :all]
            [clojure.java.io :as io]
            [clojure.java.shell :as sh])
  (:import  (java.io FileInputStream)))

; TODO: when the logger has a closure port, we should create an exception that has context that we can log w/ exceptions
(defrecord LocalObjectStorage [directory]
  ObjectStorageProtocol

  (get-object [this bucket path]
    (let [real-file (io/file (resolve-local-path (:directory this) bucket path))]
      (when (file-exists? real-file)
        (create-storage-object real-file))))

  (put-object [this resource bucket path]
    (let [real-path (resolve-local-path (:directory this) bucket path)]
      (create-local-path (get-parent real-path))
      (if (transfer! resource real-path)
        true
        (throw (Exception. "put-object failed")))))

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
