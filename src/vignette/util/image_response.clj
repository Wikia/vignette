(ns vignette.util.image-response
  (:require [clojure.java.io :refer [file]]
            [clojure.string :as string]
            [compojure.route :refer [not-found]]
            [ring.util.response :refer [response status header]]
            [digest :as digest]
            [vignette.media-types :refer :all]
            [vignette.util.query-options :refer :all]
            [vignette.storage.local :refer [create-stored-object]]
            [vignette.storage.protocols :refer :all]
            [vignette.util.thumbnail :refer :all]))

(declare create-image-response
         add-content-disposition-header
         add-surrogate-header
         surrogate-key)

(defmacro when-header-val
  ([resp key val]
   `(if ~val
     (header ~resp ~key ~val) ~resp)))

(def error-image-file (file "public/brokenImage.jpg"))

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
     (add-surrogate-header resp map)))
  ([code]
   (error-response code nil)))

(defn create-image-response
  ([image image-map]
   (as-> (response (->response-object image)) resp
         (when-header-val resp "Content-Type" (content-type image))
         (when-header-val resp "Content-Length" (content-length image))
         (when-header-val resp "ETag" (etag image))
         (header resp "X-Thumbnailer" "Vignette")
         (add-content-disposition-header resp image-map image)
         (add-surrogate-header resp image-map)))
  ([image]
    (create-image-response image nil)))

(defn create-head-response
  [image-map]
  (-> (response "")
       (header "X-Thumbnailer" "Vignette")
       (add-content-disposition-header image-map)
       (add-surrogate-header image-map)))

(defn- base-filename [image-map object]
  (or (if-let [orig (if image-map (original image-map))]
        (string/replace orig "\"" "\\\""))
      (when object (filename object))))

(defn add-content-disposition-header
  ([response-map image-map object]
   (if-let [filename (base-filename image-map object)]
     (let [target-filename
           (if-let [requested-path
                    (query-opt image-map :format)] (str filename "." requested-path)
                                                   filename)]
       (header response-map "Content-Disposition" (format "inline; filename=\"%s\"" target-filename)))
     response-map))
  ([response-map image-map]
   (add-content-disposition-header response-map image-map nil)))


(defn add-surrogate-header
  [response-map image-map]
  (if (and (wikia image-map)
           (original image-map)
           (image-type image-map))
    (let [sk (surrogate-key image-map)]
      (-> response-map
          (header "Surrogate-Key" sk)
          (header "X-Surrogate-Key" sk)))
    response-map))

(defn surrogate-key
  [image-map]
  (try
    (digest/sha1 (fully-qualified-original-path image-map))
    (catch Exception e
      (str "vignette-"(:original image-map)))))
