(ns vignette.util.external-hotlinking
  (:require [vignette.media-types :refer :all]
            [vignette.protocols :refer :all]
            [vignette.storage.protocols :refer :all]
            [vignette.util.thumbnail :as u]))

(def force-header-name "x-vignette-force-thumb")
(def force-header-val "1")
(def force-thumb-params {:request-type :thumbnail
                         :thumbnail-mode "scale-to-width"
                         :width "200"
                         :height :auto})
(def force-webp-params {:request-type :thumbnail
                        :thumbnail-mode "type-convert"})

(defn force-thumb? [request]
  (if-let [vary-string (get-in request [:headers force-header-name])]
    (.contains vary-string force-header-val)))

(defn image-params->forced-thumb-params [image-params]
  (merge image-params force-thumb-params))

(defn image-params->forced-webp-params [image-params]
  (merge image-params force-webp-params))

(defn original-request->file
  [request store image-params]
  (if (force-thumb? request)
    (u/get-or-generate-thumbnail store (image-params->forced-thumb-params image-params))
    (u/get-or-generate-thumbnail store (image-params->forced-webp-params image-params))))
