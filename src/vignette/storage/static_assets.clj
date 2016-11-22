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

(defn create-async-response-stored-object [static-image-get image-review-get]
  (let [static-image-response @static-image-get]
    (if (and (-> static-image-response :status (= 200)) (some #(= % (:status @image-review-get)) [200 404 400 500]))
      (->AsyncResponseStoredObject static-image-response))))

(defrecord StaticImageStorage [static-image-url image-review-url] ImageStorageProtocol
  (save-thumbnail [_ _ _] nil)
  (get-thumbnail [_ _] nil)
  (save-original [_ _ _] nil)

  (get-original [_ original-map]
    (if-let [uuid (:uuid original-map)]
      (create-async-response-stored-object
        (http/get (static-image-url uuid) {:as :stream})
        (http/get (image-review-url uuid (get-in original-map [:options :status]))))))

  (original-exists? [_ image-map] nil
    (if-let [uuid (:uuid image-map)]
      (let [static-image-response (http/head (static-image-url uuid)) image-review-response (http/head (image-review-url uuid))]
        (and (-> @static-image-response :status (= 200))
             (-> @image-review-response :status (some-fn #(or (= % 200) (= % 404)))))))))

(defn create-static-image-storage [static-image-url image-review-url]
  (->StaticImageStorage static-image-url image-review-url))
