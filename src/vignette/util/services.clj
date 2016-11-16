(ns vignette.util.services
  (:require [vignette.util.consul :as consul]
            [clojure.string :refer [join]]
            [environ.core :refer [env]]))

(defn materialize-static-asset-url [oid]
  (str
    (consul/->uri
      (consul/find-service
        consul/create-consul "static-assets" consul/service-query-tag)) "/image/" oid))

(defn materialize-image-review-url [oid statuses fastly-bypass]
  (let [statuses-vector [statuses]
        statuses-params (str "status=" (if (empty? (remove nil? statuses-vector))
                                         (join "&status=" ["ACCEPTED" "QUESTIONABLE" "UNREVIEWED"])
                                         (join "&" statuses-vector)))
        fastly-bypass-params (when-not (nil? fastly-bypass) (str "fastlyBypass=" fastly-bypass))
        joined-params (join "&" (remove nil? [statuses-params fastly-bypass-params]))]
    (str
     (consul/->uri
      (consul/find-service
        consul/create-consul "image-review" consul/service-query-tag)) "/image-review/image/" oid "?" joined-params)))
