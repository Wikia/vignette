(ns vignette.http.api-routes
  (:require [cheshire.core :refer :all]
            [clout.core :refer [route-compile route-matches]]
            [compojure.core :refer [context routes GET ANY HEAD]]
            [vignette.http.middleware :refer :all]
            [vignette.http.route-helpers :refer :all]
            [vignette.http.proto-routes :as proto]
            [vignette.util.regex :refer :all]))

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

(defn api-routes
  [store]
  (flatten
    [(create-request-handlers store proto/scale-to-width-route handle-thumbnail route->thumbnail-auto-height-map)
     (create-request-handlers store proto/scale-to-width-down-route handle-thumbnail route->thumbnail-auto-height-map)
     (create-request-handlers store proto/scale-to-height-down-route handle-thumbnail route->thumbnail-auto-width-map)
     (create-request-handlers store proto/window-crop-route handle-thumbnail route->thumbnail-auto-height-map)
     (create-request-handlers store proto/window-crop-fixed-route handle-thumbnail route->thumbnail-map)
     (create-request-handlers store proto/thumbnail-route handle-thumbnail route->thumbnail-map)
     (create-request-handlers store proto/original-route handle-original route->original-map)]))

(def wiki-context ["/:wikia:image-type/:top-dir/:middle-dir/:original/revision/:revision"
                   :wikia wikia-regex
                   :image-type image-type-regex
                   :top-dir top-dir-regex
                   :middle-dir middle-dir-regex])

(def uuid-context ["/:uuid"
                   :uuid uuid-regex])

(defmacro def-api-context [ctx store]
  `(context ~(eval ctx) [] (apply routes (api-routes ~store))))
