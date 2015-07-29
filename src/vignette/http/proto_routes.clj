(ns vignette.http.proto-routes
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
