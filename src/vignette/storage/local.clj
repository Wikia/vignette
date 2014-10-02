(ns vignette.storage.local
  (:require [clojure.java.io :as io]
            [clojure.java.shell :as sh]
            [pantomime.mime :refer [mime-type-of]]
            [vignette.media-types :as mt]
            [vignette.storage.protocols :refer :all]
            [vignette.util.filesystem :refer :all])
  (:import  (java.io FileInputStream)))

(declare create-local-image-response)

; TODO: when the logger has a closure port, we should create an exception that has context that we can log w/ exceptions
(defrecord LocalObjectStorage [directory]
  ObjectStorageProtocol

  (get-object [this bucket path]
    (let [real-file (io/file (resolve-local-path (:directory this) bucket path))]
      (when (file-exists? real-file)
        (create-local-image-response real-file))))

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
  (list-objects [this bucket]))

(defrecord LocalImageResponse [file]
  ImageResponseProtocol
  (file-stream [this]
    (:file this))
  (content-length [this]
    (file-length (file-stream this)))
  (content-type [this]
    (mime-type-of (file-stream this))))

(defn create-local-object-storage
  [directory]
  (->LocalObjectStorage directory))

(defn create-local-image-response
  [file]
  (->LocalImageResponse file))
