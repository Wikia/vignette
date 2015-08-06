(ns vignette.storage.static-assets
  (:require [vignette.storage.protocols :refer :all]
            [org.httpkit.client :as http]
            [vignette.util.consul :as consul]
            [vignette.util.filesystem :as fs]
            [clojure.java.io :as io]))

(defn build-url [oid]
  (str
    (consul/->uri
      (consul/find-service
        consul/create-consul "static-assets" "production")) "/image/" oid))

(defn- parse-content-disp
  [header]
  (when-let [m (if header (re-find #"(?i)filename=\"(.*)\"" header))]
    (second m)))

(defrecord AsyncResponseStoredObject [response] StoredObjectProtocol
  (file-stream [this] (-> this :response :body))
  (content-length [this] (-> this :response :content-length))
  (content-type [this] (-> this :response :headers :content-type))
  (etag [this] (-> this :response :headers :etag))
  (filename [this] (-> this :response :headers :content-disposition parse-content-disp))
  (transfer! [this to]
    (with-open [in-stream (io/input-stream (file-stream this))
                out-stream (io/output-stream to)]
      (io/copy in-stream out-stream))
    (fs/file-exists? to))
  (->response-object [this] (file-stream this)))

(defn create-async-response-stored-object [oid]
  (let [response @(http/get (build-url oid) {:as :stream})]
    (if (-> response :status (= 200))
      (->AsyncResponseStoredObject response))))

(defrecord StaticAssetsStorageSystem [] StorageSystemProtocol
  (get-object [_ oid] (create-async-response-stored-object oid)))
