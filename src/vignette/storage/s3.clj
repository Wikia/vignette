(ns vignette.storage.s3
  (:require [aws.sdk.s3 :as s3]
            [clojure.java.io :as io]
            [pantomime.mime :refer [mime-type-of]]
            [vignette.storage.protocols :refer :all]
            [vignette.util.filesystem :refer :all]
            [wikia.common.perfmonitoring.core :as perf])
  (:use [environ.core])
  (:import [com.amazonaws.services.s3.model AmazonS3Exception]))


;;;; Note about timeouts: The timeouts you can specify to the s3 client don't
;;;; behave as expected. Setting the timeouts will cause the request to timeout
;;;; but it doesn't match up with the values specified. I suspect it has
;;;; something to do with the abstraction used in S3Client.
;;;;
;;;; In the example below we have timeouts set to 20ms yet it takes almost 2
;;;; seconds for the exception to bubble up. --drsnyder

(comment
  (def storage-creds {:max-retries 0
                      :socket-timeout 20 ; one of these should timeout most of the time
                      :conn-timeout 20
                      :access-key "..."
                      :secret-key "..."
                      :endpoint "http://endpoint"
                      :proxy {:host nil}})

  (time (try (s3/get-object storage-creds "muppet" "images/d/d4/Mo-Yet.jpg")
             (catch Exception e (println (.getMessage e)) (clojure.stacktrace/e)))))
;Unable to execute HTTP request: Read timed out
;java.net.SocketTimeoutException: Read timed out
 ;at java.net.SocketInputStream.socketRead0 (SocketInputStream.java:-2)
    ;java.net.SocketInputStream.read (SocketInputStream.java:129)
    ;org.apache.http.impl.io.AbstractSessionInputBuffer.fillBuffer (AbstractSessionInputBuffer.java:166)
    ;org.apache.http.impl.io.SocketInputBuffer.fillBuffer (SocketInputBuffer.java:90)
    ;org.apache.http.impl.io.AbstractSessionInputBuffer.readLine (AbstractSessionInputBuffer.java:281)
    ;org.apache.http.impl.conn.LoggingSessionInputBuffer.readLine (LoggingSessionInputBuffer.java:115)
    ;org.apache.http.impl.conn.DefaultHttpResponseParser.parseHead (DefaultHttpResponseParser.java:92)
    ;org.apache.http.impl.conn.DefaultHttpResponseParser.parseHead (DefaultHttpResponseParser.java:62)
;"Elapsed time: 1912.641 msecs"


(def default-storage-connection-timeout 100)
(def default-storage-get-socket-timeout 500)
(def default-storage-put-socket-timeout 10000)
(def default-storage-max-retries 0)

(declare create-stored-object)

(def storage-creds (let [creds {:access-key  (env :storage-access-key)
                                :secret-key  (env :storage-secret-key)
                                :endpoint    (env :storage-endpoint)
                                :max-retries (env :storage-max-retries default-storage-max-retries)
                                :proxy {:host (env :storage-proxy)}}]
                     (if-let [port (env :storage-proxy-port)]
                       (assoc-in creds [:proxy :port] (Integer/parseInt port))
                       creds)))

(defn add-timeouts
  [request-type creds]
  (if (= request-type :get)
    (merge creds
           {:conn-timeout (Integer. (env :storage-connection-timeout default-storage-connection-timeout))
            :socket-timeout (Integer. (env :storage-get-socket-timeout default-storage-get-socket-timeout))})
    (merge creds
           {:conn-timeout (Integer. (env :storage-connection-timeout default-storage-connection-timeout))
            :socket-timeout (Integer. (env :storage-put-socket-timeout default-storage-put-socket-timeout))})))

(defn valid-s3-get?
  [response]
  (and (map? response)
       (contains? response :content)
       (contains? response :metadata)
       (contains? (:metadata response) :content-length)))

(defn safe-get-object
  [creds bucket path]
  (try
    (perf/timing :s3-get (s3/get-object creds bucket path))
    (catch AmazonS3Exception e
      (if (= (.getStatusCode e) 404)
        nil
        (throw e)))))

(defrecord S3StorageSystem [creds]
  StorageSystemProtocol
  (get-object [this bucket path]
    (when-let [object (safe-get-object (add-timeouts :get (:creds this)) bucket path)]
      (when (valid-s3-get? object)
        (let [stream (:content object)
              meta-data (:metadata object)]
          (create-stored-object stream meta-data)))))
  (put-object [this resource bucket path]
    (let [file (file-stream resource)
          mime-type (content-type resource)]
      (when-let [response (perf/timing :s3-put (s3/put-object (add-timeouts :put (:creds this))
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
  (->response-object [this]
    (file-stream this))
  (transfer! [this to]
    (with-open [in-stream (io/input-stream (file-stream this))
                out-stream (io/output-stream to)]
      (io/copy in-stream out-stream))
    (file-exists? to)))

(defn create-s3-storage-system
  [creds]
  (->S3StorageSystem creds))

(defn create-stored-object
  [stream meta-data]
  (->S3StoredObject stream meta-data))
