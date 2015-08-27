(ns vignette.util.static-assets
  (:require [vignette.util.consul :as consul]
            [environ.core :refer [env]]))

(defn materialize-static-asset-url [oid]
  (str
    (consul/->uri
      (consul/find-service
        consul/create-consul "static-assets" consul/service-query-tag)) "/image/" oid))
