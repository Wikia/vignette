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
            [vignette.util.regex :refer :all]))

(def original-route
  (route-compile "/"))

(def thumbnail-route
  (route-compile "/:thumbnail-mode/width/:width/height/:height"
                 {:thumbnail-mode thumbnail-mode-regex
                  :width size-regex
                  :height size-regex}))

(def window-crop-route
  (route-compile "/:thumbnail-mode/width/:width/x-offset/:x-offset/y-offset/:y-offset/window-width/:window-width/window-height/:window-height"
                 {:thumbnail-mode "window-crop"
                  :width size-regex
                  :x-offset size-regex-allow-negative
                  :window-width size-regex
                  :y-offset size-regex-allow-negative
                  :window-height size-regex}))

(def window-crop-fixed-route
  (route-compile "/:thumbnail-mode/width/:width/height/:height/x-offset/:x-offset/y-offset/:y-offset/window-width/:window-width/window-height/:window-height"
                 {:thumbnail-mode "window-crop-fixed"
                  :width size-regex
                  :height size-regex
                  :x-offset size-regex-allow-negative
                  :window-width size-regex
                  :y-offset size-regex-allow-negative
                  :window-height size-regex}))

(def scale-to-width-route
  (route-compile "/:thumbnail-mode/:width"
                 {:thumbnail-mode "scale-to-width"
                  :width size-regex}))

(def scale-to-width-down-route
  (route-compile "/:thumbnail-mode/:width"
                 {:thumbnail-mode "scale-to-width-down"
                  :width size-regex}))

(def scale-to-height-down-route
  (route-compile "/:thumbnail-mode/:height"
                 {:thumbnail-mode "scale-to-height-down"
                  :height size-regex}))


;;;;


(defn create-request-handlers
  "Creates request handlers for a given route for GET & HEAD using the given route and
  map generation function."
  [system route get-handler route->map-fn]
  ; NOTE: HEAD needs to be first, otherwise compojure will match the GET and set the body to nil
  [(HEAD route
         request
         (handle-head system
                  (route->map-fn (:route-params request)
                                 request)))
   (GET route
        request
        (get-handler system
                     (route->map-fn (:route-params request)
                                    request)
                     request))])

(def wiki-context ["/:wikia:image-type/:top-dir/:middle-dir/:original/revision/:revision"
             :wikia wikia-regex
             :image-type image-type-regex
             :top-dir top-dir-regex
             :middle-dir middle-dir-regex])


(defmacro eval-context [ctx args routes]
  `(context ~(eval ctx) ~args ~routes))

(defn app-routes
  [system]
  (flatten
    [(create-request-handlers system proto/scale-to-width-route handle-thumbnail route->thumbnail-auto-height-map)
     (create-request-handlers system proto/scale-to-width-down-route handle-thumbnail route->thumbnail-auto-height-map)
     (create-request-handlers system proto/scale-to-height-down-route handle-thumbnail route->thumbnail-auto-width-map)
     (create-request-handlers system proto/window-crop-route handle-thumbnail route->thumbnail-auto-height-map)
     (create-request-handlers system proto/window-crop-fixed-route handle-thumbnail route->thumbnail-map)
     (create-request-handlers system proto/thumbnail-route handle-thumbnail route->thumbnail-map)
     (create-request-handlers system proto/original-route handle-original route->original-map)]))

(defn all-routes
  [system]
  (-> (apply routes (concat
                      (hlr/legacy-routes system)
                      (list
                        (eval-context wiki-context [] (apply routes (app-routes system)))
                        (GET "/ping" [] "pong")
                        (files "/static/")
                        (bad-request-path))))
      (wrap-params)
      (exception-catcher)
      (multiple-slash->single-slash)
      (request-timer)
      (add-headers)))
