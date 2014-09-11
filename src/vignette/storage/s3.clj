(ns vignette.storage.s3
  (:require [aws.sdk.s3 :as s3]
            (vignette.storage [protocols :refer :all]
                              [common :refer :all])
            (vignette.util [filesystem :refer :all]
                           [byte-streams :refer :all])
            [pantomime.mime :refer (mime-type-of)]
            [clojure.java.io :as io])
  (:use [environ.core])
  (:import [com.amazonaws.services.s3.model AmazonS3Exception]))

(def storage-creds (let [creds {:access-key  (env :storage-access-key)
                                :secret-key  (env :storage-secret-key)
                                :endpoint    (env :storage-endpoint)
                                :proxy {:host (env :storage-proxy)}}]
                     (if-let [port (env :storage-proxy-port)]
                       (assoc-in creds [:proxy :port] (Integer/parseInt port))
                       creds)))

(defn valid-s3-get?
  [response]
  (and (map? response)
       (contains? response :content)
       (contains? response :metadata)
       (contains? (:metadata response) :content-length)))

(defn safe-get-object
  [creds bucket path]
  (try 
    (s3/get-object creds bucket path)
    (catch AmazonS3Exception e
      (if (= (.getStatusCode e) 404)
        nil
        (throw e)))))

(defrecord S3ObjectStorage [creds]
  ObjectStorageProtocol
  (get-object [this bucket path]
    (when-let [object (safe-get-object (:creds this) bucket path)]
      (when (valid-s3-get? object)
        (let [stream (:content object)
              meta-data (:metadata object)
              length (:content-length meta-data)
              content-type (:content-type meta-data)]
          (create-storage-object stream content-type length)))))
  (put-object [this resource bucket path]
    (let [mime-type (mime-type-of resource)]
      (when-let [response (s3/put-object (:creds this) bucket path resource {:content-type mime-type})]
        response)))
  (delete-object [this bucket path])
  (list-buckets [this])
  (list-objects [this bucket]))

(defn create-s3-object-storage
  [creds]
  (->S3ObjectStorage creds))
