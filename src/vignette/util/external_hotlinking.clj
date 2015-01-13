(ns vignette.util.external-hotlinking)

(def force-header-name "x-vary-string")
(def force-header-val "forced-vignette-thumb")
(def force-thumb-params {:request-type :thumbnail
                         :thumbnail-mode "scale-to-width"
                         :width "200"
                         :height :auto})

(defn force-thumb? [request]
  (if-let [vary-string (get-in request [:headers force-header-name])]
    (.contains vary-string force-header-val)))

(defn image-params->forced-thumb-params [image-params]
  (merge image-params force-thumb-params))
