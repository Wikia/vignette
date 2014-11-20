(ns vignette.storage.s3
  (:require [aws.sdk.s3 :as s3]
            [clj-statsd :as statsd]
            [clojure.java.io :as io]
            [pantomime.mime :refer [mime-type-of]]
            [vignette.storage.protocols :refer :all]
            [vignette.util.constants :refer :all]
            [vignette.util.filesystem :refer :all])
  (:use [environ.core])
  (:import [com.amazonaws.services.s3.model AmazonS3Exception]))

(declare create-stored-object)

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
    (statsd/with-sampled-timing "vignette.s3.get"
                                statsd-sample-rate
                                (s3/get-object creds bucket path))
    (catch AmazonS3Exception e
      (if (= (.getStatusCode e) 404)
        nil
        (throw e)))))

(defrecord S3StorageSystem [creds]
  StorageSystemProtocol
  (get-object [this bucket path]
    (when-let [object (safe-get-object (:creds this) bucket path)]
      (when (valid-s3-get? object)
        (let [stream (:content object)
              meta-data (:metadata object)]
          (create-stored-object stream meta-data)))))
  (put-object [this resource bucket path]
    (let [file (file-stream resource)
          mime-type (content-type resource)]
      (when-let [response (statsd/with-sampled-timing "vignette.s3.put"
                                                      statsd-sample-rate
                                                      (s3/put-object (:creds this)
                                                                     bucket
                                                                     path
                                                                     file
                                                                     {:content-type mime-type}))]
        response)))
  (delete-object [this bucket path])
  (list-buckets [this])
  (list-objects [this bucket]))

(defrecord S3StoredObject [stream meta-data]
  StoredObjectProtocol
  (file-stream [this]
    (:stream this))
  (content-length [this]
    (:content-length (:meta-data this)))
  (content-type [this]
    (:content-type (:meta-data this)))
  (transfer! [this to]
    (with-open [in-stream (io/input-stream (file-stream this))
                out-stream (io/output-stream to)]
      (io/copy in-stream out-stream))
    (file-exists? to))
  (->response-object [this]
    (file-stream this)))

(defn create-s3-storage-system
  [creds]
  (->S3StorageSystem creds))

(defn create-stored-object
  [stream meta-data]
  (->S3StoredObject stream meta-data))
