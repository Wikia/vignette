(ns vignette.http.api-routes
  (:require [cheshire.core :refer :all]
            [clout.core :refer [route-compile route-matches]]
            [compojure.core :refer [context routes GET ANY HEAD DELETE]]
            [vignette.http.middleware :refer :all]
            [vignette.http.route-helpers :refer :all]
            [vignette.util.regex :refer :all]))

(def original-route
  (route-compile "/"))

(def thumbnail-route
  (route-compile "/:thumbnail-mode/width/:width/height/:height"
    {:thumbnail-mode thumbnail-mode-regex
     :width          size-regex
     :height         size-regex}))

(def window-crop-route
  (route-compile "/:thumbnail-mode/width/:width/x-offset/:x-offset/y-offset/:y-offset/window-width/:window-width/window-height/:window-height"
    {:thumbnail-mode "window-crop"
     :width          size-regex
     :x-offset       size-regex-allow-negative
     :window-width   size-regex
     :y-offset       size-regex-allow-negative
     :window-height  size-regex}))

(def window-crop-fixed-route
  (route-compile "/:thumbnail-mode/width/:width/height/:height/x-offset/:x-offset/y-offset/:y-offset/window-width/:window-width/window-height/:window-height"
    {:thumbnail-mode "window-crop-fixed"
     :width          size-regex
     :height         size-regex
     :x-offset       size-regex-allow-negative
     :window-width   size-regex
     :y-offset       size-regex-allow-negative
     :window-height  size-regex}))

(def scale-to-width-route
  (route-compile "/:thumbnail-mode/:width"
    {:thumbnail-mode "scale-to-width"
     :width          size-regex}))

(def scale-to-width-down-route
  (route-compile "/:thumbnail-mode/:width"
    {:thumbnail-mode "scale-to-width-down"
     :width          size-regex}))

(def scale-to-height-down-route
  (route-compile "/:thumbnail-mode/:height"
    {:thumbnail-mode "scale-to-height-down"
     :height         size-regex}))

(defn wiki-routes [store]
  (context ["/:wikia:image-type/:top-dir/:middle-dir/:original/revision/:revision"
            :wikia wikia-regex
            :image-type image-type-regex
            :top-dir top-dir-regex
            :middle-dir middle-dir-regex] []
    (apply routes
      [(HEAD scale-to-width-route request (handle-head store (route->thumbnail-auto-height-map (:route-params request) request)))
       (GET scale-to-width-route request (handle-thumbnail store (route->thumbnail-auto-height-map (:route-params request) request) request))
       (HEAD scale-to-width-down-route request (handle-head store (route->thumbnail-auto-height-map (:route-params request) request)))
       (GET scale-to-width-down-route request (handle-thumbnail store (route->thumbnail-auto-height-map (:route-params request) request) request))
       (HEAD scale-to-height-down-route request (handle-head store (route->thumbnail-auto-width-map (:route-params request) request)))
       (GET scale-to-height-down-route request (handle-thumbnail store (route->thumbnail-auto-width-map (:route-params request) request) request))
       (HEAD window-crop-route request (handle-head store (route->thumbnail-auto-height-map (:route-params request) request)))
       (GET window-crop-route request (handle-thumbnail store (route->thumbnail-auto-height-map (:route-params request) request) request))
       (HEAD window-crop-fixed-route request (handle-head store (route->thumbnail-map (:route-params request) request)))
       (GET window-crop-fixed-route request (handle-thumbnail store (route->thumbnail-map (:route-params request) request) request))
       (HEAD thumbnail-route request (handle-head store (route->thumbnail-map (:route-params request) request)))
       (GET thumbnail-route request (handle-thumbnail store (route->thumbnail-map (:route-params request) request) request))
       (HEAD original-route request (handle-head store (route->original-map (:route-params request) request)))
       (GET original-route request (handle-original store (route->original-map (:route-params request) request) request))]
      )))

(defn uuid-routes [store]
  (context ["/:uuid" :uuid uuid-regex] []
    (apply routes
      [(HEAD scale-to-width-route request (handle-head store (route->thumbnail-auto-height-map (:route-params request) request)))
       (GET scale-to-width-route request (handle-thumbnail store (route->thumbnail-auto-height-map (:route-params request) request) request))
       (HEAD scale-to-width-down-route request (handle-head store (route->thumbnail-auto-height-map (:route-params request) request)))
       (GET scale-to-width-down-route request (handle-thumbnail store (route->thumbnail-auto-height-map (:route-params request) request) request))
       (HEAD scale-to-height-down-route request (handle-head store (route->thumbnail-auto-width-map (:route-params request) request)))
       (GET scale-to-height-down-route request (handle-thumbnail store (route->thumbnail-auto-width-map (:route-params request) request) request))
       (HEAD window-crop-route request (handle-head store (route->thumbnail-auto-height-map (:route-params request) request)))
       (GET window-crop-route request (handle-thumbnail store (route->thumbnail-auto-height-map (:route-params request) request) request))
       (HEAD window-crop-fixed-route request (handle-head store (route->thumbnail-map (:route-params request) request)))
       (GET window-crop-fixed-route request (handle-thumbnail store (route->thumbnail-map (:route-params request) request) request))
       (HEAD thumbnail-route request (handle-head store (route->thumbnail-map (:route-params request) request)))
       (GET thumbnail-route request (handle-thumbnail store (route->thumbnail-map (:route-params request) request) request))
       (DELETE original-route request (handle-delete store (route->original-map (:route-params request) request)))
       (HEAD original-route request (handle-head store (route->original-map (:route-params request) request)))
       (GET original-route request (handle-original store (route->original-map (:route-params request) request) request))]
      )))

