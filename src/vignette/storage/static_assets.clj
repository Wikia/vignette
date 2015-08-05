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
;(defn cast-to-integer)
;
(defn response-is-valid? [response]

  ;
  (and
    (-> response :status (= 200))))


(defrecord AsyncResponseStoredObject [response] StoredObjectProtocol
  (file-stream [this] (-> this :response :body))
  (content-length [this] (or (-> this :response :content-length) -1))
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
      (println response)
      (if (-> response :status (= 200))
        (do
          (println response "dupdup")
          (->AsyncResponseStoredObject response))))))
;  {:opts {:url https://services.wikia.com/static-assets/images/a5b9dc90-4488-4799-8e53-4c5e70988ee6,
;:method :get, :as :stream}, :body #<BytesInputStream BytesInputStream[len=9109]>,
;:headers {:age 135762,
;          :date Wed, 05 Aug 2015 11:20:45 GMT,
;          :server nginx, :connection keep-alive, :cache-control no-transform, max-age=31536000,
;          :via 1.1 varnish,
;          :content-disposition image/jpeg;
;          ; filename="a5b9dc90-4488-4799-8e53-4c5e70988ee6", :content-type application/octet-stream,
;          ; :x-served-by cache-fra1227-FRA, :vary Accept-Encoding,Origin, :x-cache HIT, :accept-ranges bytes,
;          ; :content-length 8843, :x-cache-hits 1, :content-encoding gzip}, :status 200}

;(defprotocol StoredObjectProtocol
;  (file-stream [this])
;  (content-length [this])
;  (content-type [this])
;  (etag [this])
;  (transfer! [this to])
;  (->response-object [this]))
