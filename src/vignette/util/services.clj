(ns vignette.util.services
  (:require [clojure.string :refer [join]]
            [environ.core :refer [env]]))


(defn materialize-static-asset-url [oid]
  (let [environment (env :wikia-environment)
        physical-dc (env :wikia-datacenter)
        logical-dc (if (= environment "dev") (join "-" [physical-dc environment]) physical-dc)]
    (format "http://%s.%s.k8s.wikia.net/static-assets/image/%s" environment logical-dc oid)))
