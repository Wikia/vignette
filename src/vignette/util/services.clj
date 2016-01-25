(ns vignette.util.services
  (:require [vignette.util.consul :as consul]
            [clojure.string :refer [join]]
            [environ.core :refer [env]]))

(defn materialize-static-asset-url [oid]
  (str
    (consul/->uri
      (consul/find-service
        consul/create-consul "static-assets" consul/service-query-tag)) "/image/" oid))

(defn materialize-image-review-url [oid & statuses]
  (let [query-params (str "status=" (if (empty? statuses) (join "&status=" ["ACCEPTED" "FLAGGED" "UNREVIEWED"]) (join "&" statuses)))]
    (str
      (consul/->uri
        (consul/find-service
          consul/create-consul "image-review" consul/service-query-tag)) "/image-review/image/" oid "?" query-params)))
