(ns vignette.storage.static-assets
  (:require [vignette.storage.protocols :refer :all]
            [slingshot.slingshot :refer [throw+]]
            [org.httpkit.client :as http]
            [vignette.util.filesystem :as fs]
            [clojure.java.io :as io]
            [vignette.media-types :as mt]))

(defn get-bucket-name [uuid]
      (if-let [[_ bucket] (re-matches #"[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{8}([0-9a-f]{4})" uuid)]
              bucket (throw+ {:type :convert-error :uuid uuid} "Incorrect UUID")))

(defn get*
      [store object-map get-path]
      (get-object store
                  (get-bucket-name (:uuid object-map))
                  (get-path object-map)))

(defn put*
      [store resource object-map get-path]
      (put-object store
                  resource
                  (get-bucket-name (:uuid object-map))
                  (get-path object-map)))

(defn exists?
      [store object-map get-path]
      (object-exists? store
                      (get-bucket-name (:uuid object-map))
                      (get-path object-map)))

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
    (let [response @(http/get url {:as :stream :user-agent "vignette"})]
      (let [status (:status response)]
        (if (= 200 status)
          (->AsyncResponseStoredObject response)
          (if (= 451 status)
            (first-available (rest image-urls))))))))

(defrecord StaticImageStorage [store, static-image-url] ImageStorageProtocol
  (save-thumbnail [this resource thumb-map]
      (put* (:store this)
        resource
        thumb-map
        mt/static-assets-thumbnail-path))

  (get-thumbnail [this thumb-map]
       (get* (:store this)
         thumb-map
         mt/static-assets-thumbnail-path))

  (save-original [_ _ _] nil)

  (get-original [_ original-map]
    (let [uuid (:uuid original-map)
          pid (:blocked-placeholder original-map)]
      (first-available (map static-image-url (remove nil? [uuid pid])))))

  (original-exists? [_ image-map] nil
    (if-let [uuid (:uuid image-map)]
      (let [static-image-response (http/head (static-image-url uuid) {:user-agent "vignette"})]
        (-> @static-image-response :status (= 200))))))


(defn create-static-image-storage [store static-image-url]
  (->StaticImageStorage store static-image-url))
