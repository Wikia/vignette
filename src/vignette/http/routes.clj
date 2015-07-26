(ns vignette.http.routes
  (:require [cheshire.core :refer :all]
            [clout.core :refer [route-compile route-matches]]
            [vignette.http.dup :refer [context routes GET ANY HEAD]]
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
  (route-compile "/:wikia:image-type/:top-dir/:middle-dir/:original/revision/:revision/:thumbnail-mode/width/:width/height/:height/x-offset/:x-offset/y-offset/:y-offset/window-width/:window-width/window-height/:window-height"
                 {:wikia wikia-regex
                  :image-type image-type-regex
                  :top-dir top-dir-regex
                  :middle-dir middle-dir-regex
                  :original original-regex
                  :revision revision-regex
                  :thumbnail-mode "window-crop-fixed"
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

(def scale-to-width-down-route
  (route-compile "/:wikia:image-type/:top-dir/:middle-dir/:original/revision/:revision/:thumbnail-mode/:width"
                 {:wikia wikia-regex
                  :image-type image-type-regex
                  :top-dir top-dir-regex
                  :middle-dir middle-dir-regex
                  :original original-regex
                  :revision revision-regex
                  :thumbnail-mode "scale-to-width-down"
                  :width size-regex}))

(def scale-to-height-down-route
  (route-compile "/:wikia:image-type/:top-dir/:middle-dir/:original/revision/:revision/:thumbnail-mode/:height"
                 {:wikia wikia-regex
                  :image-type image-type-regex
                  :top-dir top-dir-regex
                  :middle-dir middle-dir-regex
                  :original original-regex
                  :revision revision-regex
                  :thumbnail-mode "scale-to-height-down"
                  :height size-regex}))

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


;(defmacro expand-contexts [path & rest] (context ~(eval path) '~rest))

(defmacro wiki-context [routes]
  `(context ["/:wikia:image-type/:top-dir/:middle-dir/:original/revision/:revision"
   :wikia wikia-regex
   :image-type image-type-regex
   :top-dir top-dir-regex
   :middle-dir middle-dir-regex] [] ~routes))

(def mosdef ["/:wikia:image-type/:top-dir/:middle-dir/:original/revision/:revision"
             :wikia wikia-regex
             :image-type image-type-regex
             :top-dir top-dir-regex
             :middle-dir middle-dir-regex])

(defmacro eval-context [ctx routes]
  `(context ~(resolve ctx) [] ~routes))

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
                        ;(context (wiki-context) [] (apply routes (app-routes system)))
                        ;(wiki-context (apply routes (app-routes system)))
                        (eval-context mosdef (apply routes (app-routes system)))
                        (GET "/ping" [] "pong")
                        (files "/static/")
                        (bad-request-path))))
      (wrap-params)
      (exception-catcher)
      (multiple-slash->single-slash)
      (request-timer)
      (add-headers)))
