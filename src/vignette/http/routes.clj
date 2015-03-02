(ns vignette.http.routes
  (:require [cheshire.core :refer :all]
            [clojure.java.io :as io]
            [clout.core :refer [route-compile route-matches]]
            [compojure.core :refer [routes GET]]
            [vignette.http.compojure :refer [GET+PURGE]]
            [compojure.route :refer [files not-found]]
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
                  :thumbnail-mode #"window-crop"
                  :width size-regex
                  :x-offset size-regex-allow-negative
                  :window-width size-regex
                  :y-offset size-regex-allow-negative
                  :window-height size-regex}))

(def window-crop-fixed-route
  (route-compile "/:wikia:image-type/:top-dir/:middle-dir/:original/revision/:revision/:thumbnail-mode/width/:width/height/:height/x-offset/:x-offset/y-offset/:y-offset/window-width/:window-width/window-height/:window-height"
                 {:wikia wikia-regex
                  :image-type image-type-regex
                  :top-dir top-dir-regex
                  :middle-dir middle-dir-regex
                  :original original-regex
                  :revision revision-regex
                  :thumbnail-mode #"window-crop-fixed"
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
                  :thumbnail-mode #"scale-to-width"
                  :width size-regex}))

(declare image-request-handler)

(defn original-request->file
  [request system image-params]
  (if (force-thumb? request)
    (u/get-or-generate-thumbnail system (image-params->forced-thumb-params image-params))
    (get-original (store system) image-params)))


(defn app-routes
  [system]
  (-> (routes
        (GET+PURGE scale-to-width-route
             request
             (image-request-handler system :thumbnail request))
        (GET+PURGE window-crop-route
             request
             (image-request-handler system :thumbnail request))
        (GET+PURGE window-crop-fixed-route
             request
             (image-request-handler system :thumbnail request))
        (GET+PURGE thumbnail-route
             request
             (image-request-handler system :thumbnail request))
        (GET+PURGE original-route
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

(declare request-method-handler
         handle-thumbnail
         handle-original
         handle-purge
         get-image-params
         background-purge
         route-params->image-type)

(defmulti image-request-handler (fn [system request-type request]
                                   (get request :request-method nil)))

(defmethod image-request-handler :get [system request-type request]
  (let [image-params (get-image-params request request-type)]
    (condp = request-type
      :thumbnail (handle-thumbnail system image-params)
      :original (handle-original system image-params))))

(defmethod image-request-handler :purge [system request-type request]
  (handle-purge system (get-image-params request request-type) (:uri request)))

(defmethod image-request-handler :default [system request-type request]
  (throw+ {:type :request-method-error
           :request-method (get request :request-method)
           :request request}))

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

(defn handle-purge
  [system image-params uri]
  (if-let [edge-cache (cache system)]
    (do
      (background-purge edge-cache image-params uri)
      (-> (response "")
          (status 202)))
    (not-found "No purger available\n")))

(defn background-purge
  [cache image-params uri]
  (future (purge cache uri (surrogate-key image-params))))

(defn get-image-params
  [request request-type]
  (let [route-params (assoc (:route-params request) :request-type request-type)
        options (extract-query-opts request)
        image-params (assoc route-params :options options
                            :image-type (route-params->image-type route-params))]
    image-params))

(defn route-params->image-type
  [route-params]
  (if (clojure.string/blank? (:image-type route-params))
    "images"
    (clojure.string/replace (:image-type route-params)
                            #"^\/(.*)"
                            "$1")))
