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

(defn response-is-valid? [response]
  (and
    (-> response :status (= 200))))


(defrecord AsyncResponseStoredObject [response] StoredObjectProtocol
  (file-stream [this] (-> this :response :body))
  (content-length [this] (-> this :response :content-length))
  (content-type [this] (-> this :response :headers :content-type))
  (etag [this] (-> this :response :headers :etag))
  (transfer! [this to]
    (with-open [in-stream (io/input-stream (file-stream this))
                out-stream (io/output-stream to)]
      (io/copy in-stream out-stream))
    (fs/file-exists? to)
    )
  (->response-object [this] (file-stream this))
  )

(defrecord StaticAssetsStorageSystem [creds] StorageSystemProtocol
  (get-object [this oid]
    (let [response @(http/get (build-url oid) {:as :stream})]
      (if (-> response :status (= 200))
        (do
          (->AsyncResponseStoredObject response))))))
