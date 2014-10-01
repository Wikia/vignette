(ns vignette.storage.common
  (:import (java.io BufferedInputStream
                    File)
           (com.amazonaws.services.s3.model S3ObjectInputStream))
  (:require [pantomime.mime :refer [mime-type-of]]
            [vignette.util.filesystem :refer [file-length]]))

(defn- storage-object
  [file-stream content-type length]
  {:file-stream file-stream
   :content-type content-type
   :length length})

(defmulti create-storage-object
          (fn [obj & more]
            (class obj)))

(defmethod create-storage-object File
           [file]
  (storage-object file (mime-type-of file) (file-length file)))

(defmethod create-storage-object S3ObjectInputStream
           [stream meta-data]
  (storage-object stream (:content-type meta-data) (:content-length meta-data)))

(defn file-stream
  [storage-object]
  (get storage-object :file-stream))

(defn content-type
  [storage-object]
  (get storage-object :content-type))

(defn length
  [storage-object]
  (get storage-object :length))
