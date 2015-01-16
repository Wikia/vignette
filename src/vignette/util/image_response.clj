(ns vignette.util.image-response
  (:require [clojure.java.io :refer [file]]
            [compojure.route :refer [not-found]]
            [ring.util.response :refer [response status header]]
            [vignette.storage.local :refer [create-stored-object]]
            [vignette.storage.protocols :refer :all]
            [vignette.util.thumbnail :refer :all]))

(declare create-image-response)

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
   (if-let [image (error-image map)]
     (status (create-image-response image) code)
     (not-found "Unable to fetch or generate image")))
  ([code]
   (error-response code nil)))

(defn create-image-response
  ([image image-map]
    (-> (response (->response-object image))
        (header "Content-Type" (content-type image))
        (header "Content-Length" (content-length image))
        (cond->
          (:original image-map) (header "Content-Disposition"
                                        (format "inline; filename=\"%s\"; filename*=utf-8' '%s"
                                                (:original image-map)
                                                (:original image-map))))))
  ([image]
    (create-image-response image nil)))
