(ns vignette.storage.s3
  (:require [aws.sdk.s3 :as s3]
            [vignette.storage.protocols :refer :all]
            [vignette.storage.local :refer (create-local-object-storage)]
            (vignette.util [filesystem :refer :all]
                           [byte-streams :refer :all])
            [clojure.java.io :as io])
  (:use [environ.core])
  (:import [com.amazonaws.services.s3.model AmazonS3Exception]))

(def local-cache-directory (env :local-cache-directory "/tmp/vignette/_cache"))

(defn valid-s3-get?
  [response]
  (and (map? response)
       (contains? response :content)
       (contains? response :metadata)
       (contains? (:metadata response) :content-length)))

(defn stream->temp-file
  "Writes the byte array to a temporary file and returns it."
  [ba prefix]
  (let [tempfile (io/file (temp-filename prefix))]
    (transfer! ba tempfile)
    tempfile))

(defn safe-get-object
  [creds bucket path]
  (try 
    (s3/get-object creds bucket path)
    (catch AmazonS3Exception e
      (if (= (.getStatusCode e) 404)
        nil
        (throw e)))))

(defrecord S3ObjectStorage [creds local-cache]
  ObjectStorageProtocol
  (get-object [this bucket path]
    (when-let [object (safe-get-object (:creds this) bucket path)]
      (when (valid-s3-get? object)
        (let [stream (:content object)
              meta-data (:metadata object)
              length (:content-length meta-data)
              ba (read-byte-stream stream length)]
          (stream->temp-file ba bucket)))))
  (put-object [this resource bucket path]
    (when-let [response (s3/put-object (:creds this) bucket path resource)]
      response))
  (delete-object [this bucket path])
  (list-buckets [this])
  (list-objects [this bucket]))

(defn create-s3-object-storage
  ([creds cache-directory]
   (->S3ObjectStorage creds (create-local-object-storage cache-directory)))
  ([creds]
   (create-s3-object-storage creds local-cache-directory)))
