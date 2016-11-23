(ns vignette.util.services
  (:require [vignette.util.consul :as consul]
            [clojure.string :refer [join]]
            [environ.core :refer [env]]))

(defn materialize-static-asset-url [oid]
  (str
    (consul/->uri
      (consul/find-service
        consul/create-consul "static-assets" consul/service-query-tag)) "/image/" oid))

(defn materialize-image-review-url [oid statuses]
  (let [statuses-vector [statuses]
        statuses-params (str "status=" (if (empty? (remove nil? statuses-vector))
                                         (join "&status=" ["ACCEPTED" "QUESTIONABLE" "UNREVIEWED"])
                                         (join "&" statuses-vector)))]
    (str
     (consul/->uri
      (consul/find-service
        consul/create-consul "image-review" consul/service-query-tag)) "/image-review/image/" oid "/status?" statuses-params)))
