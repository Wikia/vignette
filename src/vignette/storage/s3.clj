(ns vignette.storage.s3
  (:require [aws.sdk.s3 :as s3]
            [vignette.storage.protocols :refer :all]
            (vignette.util [filesystem :refer (file-exists?)]
                           [byte-streams :refer :all])))

(defn valid-s3-get?
  [response]
  (and (map? response)
       (contains? response :content)
       (contains? response :metadata)
       (contains? (:metadata response) :content-length)))

(defrecord S3ObjectStorage [creds]
  ObjectStorageProtocol
  (get-object [this bucket path]
    (when-let [object (s3/get-object (:creds this) bucket path)]
      (when (valid-s3-get? object)
        (let [stream (:content object)
              meta-data (:metadata object)
              length (:content-length meta-data)]
          (read-byte-stream stream length)))))

  (put-object [this resource bucket path])
  (delete-object [this bucket path])
  (list-buckets [this])
  (list-objects [this bucket]))

(defn create-s3-object-storage
  [creds]
  (->S3ObjectStorage creds))
