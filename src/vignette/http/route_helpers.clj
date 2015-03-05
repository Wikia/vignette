(ns vignette.http.route-helpers
  (:require [vignette.util.image-response :refer :all]
            [vignette.util.query-options :refer :all]
            [vignette.protocols :refer :all]
            [vignette.storage.core :refer :all]
            [vignette.storage.protocols :refer :all]
            [vignette.util.thumbnail :as u]))


(defn handle-thumbnail
  [system image-params]
  (if-let [thumb (u/get-or-generate-thumbnail system image-params)]
    (create-image-response thumb image-params)
    (error-response 404 image-params)))

(defn handle-original
  [system image-params]
  (if-let [file (get-original (store system) image-params)]
    (create-image-response file image-params)
    (error-response 404 image-params)))

(defn route-params->image-type
  [route-params]
  (if (clojure.string/blank? (:image-type route-params))
    "images"
    (clojure.string/replace (:image-type route-params)
                            #"^\/(.*)"
                            "$1")))

(defn route->options
  "Extracts the query options and moves them to 'request-map'"
  [request-map request]
  (assoc request-map :options (extract-query-opts request)))

(defn route->image-type
  [request-map]
  (assoc request-map :image-type (route-params->image-type request-map)))

(defn route->original-map
  [request-map request]
  (-> request-map
      (assoc :request-type :original)
      (route->image-type)
      (route->options request)))

(defn route->thumbnail-map
  [request-map request &[options]]
  (-> request-map
      (assoc :request-type :thumbnail)
      (route->image-type)
      (route->options request)
      (cond->
        options (merge options))))

(defn route->thumbnail-auto-height-map
  [request-map request]
  (route->thumbnail-map request-map request {:height :auto}))
