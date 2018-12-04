(ns vignette.util.image-response
  (:require [clojure.java.io :as io]
            [clojure.string :as string]
            [compojure.route :refer [not-found]]
            [ring.util.response :refer [response status header]]
            [ring.util.io :refer [piped-input-stream]]
            [pantomime.mime :refer [extension-for-name]]
            [digest :as digest]
            [vignette.media-types :refer :all]
            [vignette.util.query-options :refer :all]
            [vignette.storage.local :refer [create-stored-object]]
            [vignette.storage.protocols :refer :all]
            [vignette.util.thumbnail :refer :all]
            [ring.util.codec :refer [url-encode]]
            [io.clj.logging :as log])
  (:use [environ.core]
        [clojure.walk]))

(declare create-image-response
         add-content-disposition-header
         add-surrogate-header
         add-vary-header
         surrogate-key)

(defn when-header-val
  ([resp key val]
   (if val
     (header resp key val) resp)))

(def error-image-file (io/file (env :vignette-broken-image "public/brokenImage.jpg")))

(defmulti error-image (fn [map]
                        (:request-type map)))

(defmethod error-image :thumbnail [map]
  (when-let [thumb (original->thumbnail error-image-file map)]
    (create-stored-object thumb)))

(defmethod error-image :default [_]
  (create-stored-object error-image-file))

(defn error-response
  ([code map]
   (let [resp (if-let [image (error-image map)]
                (status (create-image-response image map) code)
                (not-found "Unable to fetch or generate image"))]
     (with-meta (add-surrogate-header resp map) map)))
  ([code]
   (error-response code nil)))

(defn create-image-response
  ([image image-map]
    (let [image-mime-type (content-type image)]
      (with-meta (-> (response
            (piped-input-stream
              (fn [out]
                (with-open [in (io/input-stream (->response-object image))]
                  (io/copy in out)))))
          (when-header-val "Content-Type" image-mime-type)
          (when-header-val "Content-Length" (content-length image))
          (when-header-val "ETag" (str "\"" (etag image) "\""))
          (header "X-Thumbnailer" "Vignette")
          (add-content-disposition-header image-map image image-mime-type)
          (add-surrogate-header image-map)
          (add-vary-header image-map image-mime-type)) image-map)))
  ([image]
   (create-image-response image nil)))

(defn create-head-response
  [image-map]
  (with-meta (-> (response "")
      (header "X-Thumbnailer" "Vignette")
      (add-content-disposition-header image-map)
      (add-surrogate-header image-map)) image-map))

(defn create-response
  ([image-params] (create-response 200 "" image-params))
  ([code data image-params] (with-meta (-> (response data)
                 (status code)
                 (header "X-Thumbnailer" "Vignette")) image-params)))

(defn- base-filename [image-map object]
  (or (if-let [orig (if image-map (original image-map))]
        (string/replace orig "\"" "\\\""))
      (when object (filename object))))

(defn add-content-disposition-header
  ([response-map image-map image-object image-mime-type]
   (if-let [filename (base-filename image-map image-object)]
     (let [target-filename
           (if-let [requested-path
                    (when image-mime-type (extension-for-name image-mime-type))]
             (if (re-find #"\.\w+$" filename)
               (string/replace filename #"\.\w+$" requested-path)
               (str filename requested-path))
             filename)]
       (header response-map "Content-Disposition" (format "inline; filename=\"%s\"; filename*=UTF-8''%s" target-filename (url-encode target-filename))))
     response-map))
  ([response-map image-map]
   (add-content-disposition-header response-map image-map nil nil)))


(defn add-surrogate-header
  [response-map image-map]
  (if (or
        (and (wikia image-map)
             (original image-map)
             (image-type image-map))
        (:uuid image-map))
    (let [sk (surrogate-key image-map)]
      (-> response-map
          (header "Surrogate-Key" sk)))
    response-map))

(defn surrogate-key
  [image-map]
  (if-let [sk (:uuid image-map)] sk
                                 (try
                                   (do
                                     (log/info (str "sk path - " (fully-qualified-original-path image-map)) {:image_map (stringify-keys image-map)})
                                     (digest/sha1 (fully-qualified-original-path image-map)))
                                   (catch Exception e
                                     (str "vignette-" (:original image-map))))))

(defn add-vary-header
  "Add Vary: Accept header for supported thumbail types if format was not specified in query params"
  [response-map image-map image-mime-type]
  (if (and
        image-map
        (contains? image-map :requested-format)             ;; legacy routes - don't emit Vary if :requested-format is not set
        (nil? (:requested-format image-map))
        (webp-or-compatible-mime-type? image-mime-type))
    (do
      (log/info (str " requested format = " (:requested-format image-map)) {})
      (-> response-map
          (header "Vary" "Accept")))
    response-map))
