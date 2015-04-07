(ns vignette.util.external-hotlinking
  (:require [vignette.protocols :refer :all]
            [vignette.storage.protocols :refer :all]
            [vignette.util.thumbnail :as u]))

(def force-header-name "x-vignette-force-thumb")
(def force-header-val "1")
(def force-thumb-params {:request-type :thumbnail
                         :thumbnail-mode "scale-to-width"
                         :width "200"
                         :height :auto})

(defn force-thumb? [request]
  (if-let [vary-string (get-in request [:headers force-header-name])]
    (.contains vary-string force-header-val)))

(defn image-params->forced-thumb-params [image-params]
  (merge image-params force-thumb-params))

(defn original-request->file
  [request system image-params]
  (if (force-thumb? request)
    (u/get-or-generate-thumbnail system (image-params->forced-thumb-params image-params))
    (get-original (store system) image-params)))
