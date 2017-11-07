(ns vignette.storage.static-assets
  (:require [vignette.storage.protocols :refer :all]
            [org.httpkit.client :as http]
            [vignette.util.filesystem :as fs]
            [clojure.java.io :as io]
            [vignette.media-types :as mt]))

(defn- parse-content-disp
  [header]
  (when-let [m (if header (re-find #"(?i)filename=\"(.*)\"" header))]
    (or (second m) "")))

(defrecord AsyncResponseStoredObject [response] StoredObjectProtocol
  (file-stream [this] (-> this :response :body))
  (content-length [this] (-> this :response :headers :content-length))
  (content-type [this] (-> this :response :headers :content-type))
  (etag [this] (-> this :response :headers :etag))
  (filename [this] (-> this :response :headers :content-disposition parse-content-disp))
  (transfer! [this to]
    (with-open [in-stream (io/input-stream (file-stream this))
                out-stream (io/output-stream to)]
      (io/copy in-stream out-stream))
    (fs/file-exists? to))
  (->response-object [this] (file-stream this)))

(defn first-available [image-urls]
  (if-let [url (first image-urls)]
    (let [response @(http/get url {:as :stream})]
      (let [status (:status response)]
        (if (= 200 status)
          (->AsyncResponseStoredObject response)
          (if (= 451 status)
            (first-available (rest image-urls))))))))

(defrecord StaticImageStorage [static-image-url] ImageStorageProtocol
  (save-thumbnail [this resource thumb-map]
      (put* (:store this)
        resource
        thumb-map
        mt/thumbnail-path)))

  (get-thumbnail [this thumb-map]
       (get* (:store this)
         thumb-map
         mt/thumbnail-path))

  (save-original [this resource original-map]
     (put* (:store this)
       resource
       original-map
       mt/original-path))

  (get-original [_ original-map]
    (let [uuid (:uuid original-map)
          pid (:blocked-placeholder original-map)]
      (first-available (map static-image-url (remove nil? [uuid pid])))))

  (original-exists? [_ image-map] nil
    (if-let [uuid (:uuid image-map)]
      (let [static-image-response (http/head (static-image-url uuid))]
        (-> @static-image-response :status (= 200))))))


(defn create-static-image-storage [static-image-url]
  (->StaticImageStorage static-image-url))
