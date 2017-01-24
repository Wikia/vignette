(ns vignette.storage.static-assets
  (:require [vignette.storage.protocols :refer :all]
            [org.httpkit.client :as http]
            [vignette.util.filesystem :as fs]
            [clojure.java.io :as io]))

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

(defn create-async-response-stored-object [static-image-get]
  (let [static-image-response @static-image-get]
    (if (-> static-image-response :status (= 200))
      (->AsyncResponseStoredObject static-image-response))))

(defrecord StaticImageStorage [static-image-url] ImageStorageProtocol
  (save-thumbnail [_ _ _] nil)
  (get-thumbnail [_ _] nil)
  (save-original [_ _ _] nil)

  (get-original [_ original-map]
    (if-let [uuid (:uuid original-map)]
      (let [response @(http/get (static-image-url uuid) {:as :stream})]
        (let [status (:status response)]
          (if (= 200 status)
            (->AsyncResponseStoredObject response)
            (if (= 451 status)
              (if-let [pid (:blocked-placeholder original-map)]
                (create-async-response-stored-object (http/get (static-image-url pid) {:as :stream})))))))))

  (original-exists? [_ image-map] nil
    (if-let [uuid (:uuid image-map)]
      (let [static-image-response (http/head (static-image-url uuid))]
        (-> @static-image-response :status (= 200))))))
             

(defn create-static-image-storage [static-image-url]
  (->StaticImageStorage static-image-url))
