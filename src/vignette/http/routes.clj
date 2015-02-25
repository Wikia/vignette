(ns vignette.http.routes
  (:require [cheshire.core :refer :all]
            [clojure.java.io :as io]
            [clout.core :refer [route-compile route-matches]]
            [compojure.core :refer [routes GET ANY]]
            [compojure.route :refer [files]]
            [environ.core :refer [env]]
            [ring.middleware.params :refer [wrap-params]]
            [ring.util.response :refer [response status charset header]]
            [slingshot.slingshot :refer [try+ throw+]]
            [vignette.api.legacy.routes :as alr]
            [vignette.http.middleware :refer :all]
            [vignette.protocols :refer :all]
            [vignette.storage.core :refer :all]
            [vignette.storage.protocols :refer :all]
            [vignette.util.external-hotlinking :refer :all]
            [vignette.util.image-response :refer :all]
            [vignette.util.query-options :refer :all]
            [vignette.util.regex :refer :all]
            [vignette.util.thumbnail :as u]))

(def original-route
  (route-compile "/:wikia:image-type/:top-dir/:middle-dir/:original/revision/:revision"
                 {:wikia wikia-regex
                  :image-type image-type-regex
                  :top-dir top-dir-regex
                  :middle-dir middle-dir-regex
                  :original original-regex
                  :revision revision-regex}))

(def thumbnail-route
  (route-compile "/:wikia:image-type/:top-dir/:middle-dir/:original/revision/:revision/:thumbnail-mode/width/:width/height/:height"
                 {:wikia wikia-regex
                  :image-type image-type-regex
                  :top-dir top-dir-regex
                  :middle-dir middle-dir-regex
                  :original original-regex
                  :revision revision-regex
                  :thumbnail-mode thumbnail-mode-regex
                  :width size-regex
                  :height size-regex}))

(def window-crop-route
  (route-compile "/:wikia:image-type/:top-dir/:middle-dir/:original/revision/:revision/:thumbnail-mode/width/:width/x-offset/:x-offset/y-offset/:y-offset/window-width/:window-width/window-height/:window-height"
                 {:wikia wikia-regex
                  :image-type image-type-regex
                  :top-dir top-dir-regex
                  :middle-dir middle-dir-regex
                  :original original-regex
                  :revision revision-regex
                  :thumbnail-mode "window-crop"
                  :width size-regex
                  :x-offset size-regex-allow-negative
                  :window-width size-regex
                  :y-offset size-regex-allow-negative
                  :window-height size-regex}))

(def window-crop-fixed-route
  (route-compile "/:wikia:image-type/:top-dir/:middle-dir/:original/revision/:revision/window-crop-fixed/width/:width/height/:height/x-offset/:x-offset/y-offset/:y-offset/window-width/:window-width/window-height/:window-height"
                 {:wikia wikia-regex
                  :image-type image-type-regex
                  :top-dir top-dir-regex
                  :middle-dir middle-dir-regex
                  :original original-regex
                  :revision revision-regex
                  :width size-regex
                  :height size-regex
                  :x-offset size-regex-allow-negative
                  :window-width size-regex
                  :y-offset size-regex-allow-negative
                  :window-height size-regex}))

(def scale-to-width-route
  (route-compile "/:wikia:image-type/:top-dir/:middle-dir/:original/revision/:revision/:thumbnail-mode/:width"
                 {:wikia wikia-regex
                  :image-type image-type-regex
                  :top-dir top-dir-regex
                  :middle-dir middle-dir-regex
                  :original original-regex
                  :revision revision-regex
                  :thumbnail-mode "scale-to-width"
                  :width size-regex}))

(declare image-request-handler
         handle-thumbnail
         handle-original
         get-image-params
         route->offset
         route->thumbnail-map
         route->thumbnail-auto-height-map
         route->options
         route-params->image-type
         route->image-type)

(defn original-request->file
  [request system image-params]
  (if (force-thumb? request)
    (u/get-or-generate-thumbnail system (image-params->forced-thumb-params image-params))
    (get-original (store system) image-params)))

(defn app-routes
  [system]
  (-> (routes
        (GET scale-to-width-route
             request
             (handle-thumbnail system
                               (route->thumbnail-auto-height-map
                                 (:route-params request)
                                 request)))
        (GET window-crop-route
             request
             (handle-thumbnail system
                               (route->thumbnail-auto-height-map
                                 (:route-params request)
                                 request)))
        (GET window-crop-fixed-route
             request
             (image-request-handler system :thumbnail request
                                    :thumbnail-mode "window-crop-fixed"
                                    :height :auto))
        (GET thumbnail-route
             request
             (image-request-handler system :thumbnail request))
        (GET original-route
             request
             (image-request-handler system :original request))

        ; legacy routes
        (GET alr/thumbnail-route
             request
             (let [image-params (alr/route->thumb-map (:route-params request))]
               (if-let [thumb (u/get-or-generate-thumbnail system image-params)]
                 (create-image-response thumb image-params)
                 (error-response 404 image-params))))
        (GET alr/original-route
             request
             (let [image-params (alr/route->original-map (:route-params request))]
               (if-let [file (original-request->file request system image-params)]
                 (create-image-response file image-params)
                 (error-response 404 image-params))))
        (GET alr/timeline-route
             request
             (let [image-params (alr/route->timeline-map (:route-params request))]
               (if-let [file (original-request->file request system image-params)]
                 (create-image-response file image-params)
                 (error-response 404 image-params))))
        (GET alr/math-route
             request
             (let [image-params (alr/route->original-map (:route-params request))]
               (if-let [file (original-request->file request system image-params)]
                 (create-image-response file image-params)
                 (error-response 404 image-params))))
        (GET alr/interactive-maps-route
             request
             (let [image-params (alr/route->interactive-maps-map (:route-params request))]
               (if-let [file (original-request->file request system image-params)]
                 (create-image-response file image-params)
                 (error-response 404 image-params))))
        (GET alr/interactive-maps-marker-route
             request
             (let [image-params (alr/route->interactive-maps-map (:route-params request))]
               (if-let [file (original-request->file request system image-params)]
                 (create-image-response file image-params)
                 (error-response 404 image-params))))
        (GET alr/interactive-maps-thumbnail-route
             request
             (let [image-params (alr/route->interactive-maps-thumbnail-map (:route-params request))]
               (if-let [thumb (u/get-or-generate-thumbnail system image-params)]
                 (create-image-response thumb image-params)
                 (error-response 404 image-params))))
        (GET "/ping" [] "pong")
        (files "/static/")
        (bad-request-path))
      (wrap-params)
      (exception-catcher)
      (request-timer)
      (add-headers)))

; TODO: remove
(defn image-request-handler
  [system request-type request &{:keys [thumbnail-mode height] :or {thumbnail-mode nil height nil} :as params}]
  (let [image-params (get-image-params request request-type)
        image-params (if params (merge image-params params) image-params)]
    (condp = request-type
      :thumbnail (handle-thumbnail system image-params)
      :original (handle-original system image-params))))

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

; TODO: remove
(defn get-image-params
  [request request-type]
  (let [route-params (assoc (:route-params request) :request-type request-type)
        options (extract-query-opts request)]
    (assoc route-params :options options
                        :image-type (route-params->image-type route-params))))

(defn route-params->image-type
  [route-params]
  (if (clojure.string/blank? (:image-type route-params))
    "images"
    (clojure.string/replace (:image-type route-params)
                            #"^\/(.*)"
                            "$1")))

(defn route->image-type
  [request-map]
  (assoc request-map :image-type (route-params->image-type request-map)))

(defn route->thumbnail-map
  [request-map request &[options]]
  (-> request-map
      (assoc :request-type :thumbnail)
      (route->image-type)
      (route->options request)
      (route->adjust-window-offsets)
      ; todo validate
      (cond->
        options (merge options))))

(defn route->thumbnail-auto-height-map
  [request-map request]
  (route->thumbnail-map request-map request {:height :auto}))

(defn route->options
  "Extracts the query options and moves them to 'request-map'"
  [request-map request]
  (assoc request-map :options (extract-query-opts request)))

(defn route->window-params-seq
  [request-map]
  {:pre [(map? request-map)]}
  (let [tuple ((juxt :x-offset :window-width :y-offset :window-height) request-map)]
    (when-not (some nil? tuple)
      (map #(Integer. %) tuple))))

(defn route->adjust-window-offsets
  [request-map]
  (if-let [[x-offset x-end y-offset y-end] (route->window-params-seq request-map)]
    (let [window-width (- x-end x-offset)
          window-height (- y-end y-offset)]
      (-> request-map
          (assoc :window-width (str window-width))
          (assoc :window-height (str window-height))))
    request-map))
