(ns vignette.http.routes
  (:require [cheshire.core :refer :all]
            [clout.core :refer [route-compile route-matches]]
            [compojure.core :refer [context routes GET ANY HEAD]]
            [compojure.route :refer [files]]

            [environ.core :refer [env]]
            [ring.middleware.params :refer [wrap-params]]
            [ring.util.response :refer [response status charset header]]
            [slingshot.slingshot :refer [try+ throw+]]
            [vignette.http.legacy.routes :as hlr]
            [vignette.http.middleware :refer :all]
            [vignette.http.route-helpers :refer :all]
            [vignette.http.proto-routes :as proto]
            [vignette.util.regex :refer :all]
            [vignette.protocols :as vp]))


(defn create-request-handlers
  "Creates request handlers for a given route for GET & HEAD using the given route and
  map generation function."
  [store route get-handler route->map-fn]
  ; NOTE: HEAD needs to be first, otherwise compojure will match the GET and set the body to nil
  [(HEAD route
         request
     (handle-head store
                  (route->map-fn (:route-params request)
                                 request)))
   (GET route
        request
     (get-handler store
                     (route->map-fn (:route-params request)
                                    request)
                     request))])

(def wiki-context ["/:wikia:image-type/:top-dir/:middle-dir/:original/revision/:revision"
             :wikia wikia-regex
             :image-type image-type-regex
             :top-dir top-dir-regex
             :middle-dir middle-dir-regex])

(def uuid-context ["/:uuid"
                   :uuid uuid-regex])

(defmacro eval-context [ctx args routes]
  `(context ~(eval ctx) ~args ~routes))

(defn app-routes
  [store]
  (flatten
    [(create-request-handlers store proto/scale-to-width-route handle-thumbnail route->thumbnail-auto-height-map)
     (create-request-handlers store proto/scale-to-width-down-route handle-thumbnail route->thumbnail-auto-height-map)
     (create-request-handlers store proto/scale-to-height-down-route handle-thumbnail route->thumbnail-auto-width-map)
     (create-request-handlers store proto/window-crop-route handle-thumbnail route->thumbnail-auto-height-map)
     (create-request-handlers store proto/window-crop-fixed-route handle-thumbnail route->thumbnail-map)
     (create-request-handlers store proto/thumbnail-route handle-thumbnail route->thumbnail-map)
     (create-request-handlers store proto/original-route handle-original route->original-map)]))

(defn all-routes
  [wiki-store static-store]
  (-> (apply routes (concat
                      (hlr/legacy-routes wiki-store)
                      (list
                        (eval-context wiki-context [] (apply routes (app-routes wiki-store)))
                        (eval-context uuid-context [] (apply routes (app-routes static-store)))
                        (GET "/ping" [] "pong")
                        (files "/static/")
                        (bad-request-path))))
      (wrap-params)
      (exception-catcher)
      (multiple-slash->single-slash)
      (request-timer)
      (add-headers)))
