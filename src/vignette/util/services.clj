(ns vignette.util.services
  (:require [vignette.util.consul :as consul]
            [clojure.string :refer [join]]
            [environ.core :refer [env]]))

(defn materialize-static-asset-url [oid]
  (str
    (consul/->uri
      (consul/find-service
        consul/create-consul "static-assets" consul/service-query-tag)) "/image/" oid))