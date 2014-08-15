(ns vignette.storage.s3
  (:require [aws.sdk.s3 :as s3]
            [vignette.storage.protocols :refer :all]
            [vignette.storage.local :refer (create-local-object-storage)]
            (vignette.util [filesystem :refer (file-exists?)]
                           [byte-streams :refer :all]))
  (:use [environ.core])
  (:import [com.amazonaws.services.s3.model AmazonS3Exception]))

(def local-cache-directory (env :local-cache-directory "/tmp/vignette/_cache"))

(defn valid-s3-get?
  [response]
  (and (map? response)
       (contains? response :content)
       (contains? response :metadata)
       (contains? (:metadata response) :content-length)))

(defn write-to-local-cache
  [local-cache ba bucket path]
  (put-object local-cache ba bucket path))

(defn read-from-local-cache
  [local-cache bucket path]
  (get-object local-cache bucket path))

(defn write-locally
  [local-cache ba bucket path]
  (and (write-to-local-cache local-cache ba bucket path)
       (read-from-local-cache local-cache bucket path)))

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
    (if-let [object (read-from-local-cache (:local-cache this) bucket path)]
      object
      (when-let [object (safe-get-object (:creds this) bucket path)]
        (when (valid-s3-get? object)
          (let [stream (:content object)
                meta-data (:metadata object)
                length (:content-length meta-data)
                ba (read-byte-stream stream length)]
            ; this has implications for versions and for purging
            (write-locally (:local-cache this) ba bucket path))))))

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
