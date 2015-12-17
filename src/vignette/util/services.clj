(ns vignette.util.services
  (:require [vignette.util.consul :as consul]
            [environ.core :refer [env]]))

(defn materialize-static-asset-url [oid]
  (str
    (consul/->uri
      (consul/find-service
        consul/create-consul "static-assets" consul/service-query-tag)) "/image/" oid))

(defn materialize-image-review-url [oid & statuses]
  ;(let [status-query (empty? statuses))
  (str
    (consul/->uri
      (consul/find-service
        consul/create-consul "image-review" consul/service-query-tag)) "/image-review/image/" oid "?"))
