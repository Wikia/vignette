(ns vignette.http.uuid.routes
  (:require [clout.core :refer [route-compile route-matches]]
            [compojure.core :refer [routes GET]]
            [vignette.http.legacy.route-helpers :refer :all]
            [vignette.protocols :refer :all]
            [vignette.storage.protocols :refer :all]
            [vignette.util.external-hotlinking :refer [original-request->file]]
            [vignette.util.regex :refer :all]
            [vignette.util.image-response :refer :all]
            [vignette.util.thumbnail :as u]))

(def original-route
  (route-compile "/:uuid"
                 {:uuid uuid-regex}))

(def thumbnail-routes
  (route-compile "/:uuid/:thumbnail-mode/width/:width/height/:height"
                 {:uuid uuid-regex
                  :thumbnail-mode thumbnail-mode-regex
                  :width size-regex
                  :height size-regex}))

(def window-crop-route
  (route-compile "/:uuid/:thumbnail-mode/width/:width/x-offset/:x-offset/y-offset/:y-offset/window-width/:window-width/window-height/:window-height"
                 {:uuid uuid-regex
                  :thumbnail-mode "window-crop"
                  :width size-regex
                  :x-offset size-regex-allow-negative
                  :window-width size-regex
                  :y-offset size-regex-allow-negative
                  :window-height size-regex}))

(def window-crop-fixed-route
  (route-compile "/:uuid/:thumbnail-mode/width/:width/height/:height/x-offset/:x-offset/y-offset/:y-offset/window-width/:window-width/window-height/:window-height"
                 {:uuid uuid-regex
                  :thumbnail-mode "window-crop-fixed"
                  :width size-regex
                  :height size-regex
                  :x-offset size-regex-allow-negative
                  :window-width size-regex
                  :y-offset size-regex-allow-negative
                  :window-height size-regex}))

(def scale-to-width-route
  (route-compile "/:uuid/:thumbnail-mode/:width"
                 {:uuid uuid-regex
                  :thumbnail-mode "scale-to-width"
                  :width size-regex}))

(def scale-to-width-down-route
  (route-compile "/:uuid/:thumbnail-mode/:width"
                 {:uuid uuid-regex
                  :thumbnail-mode "scale-to-width-down"
                  :width size-regex}))

(def scale-to-height-down-route
  (route-compile "/:uuid/:thumbnail-mode/:height"
                 {:uuid uuid-regex
                  :thumbnail-mode "scale-to-height-down"
                  :height size-regex}))
