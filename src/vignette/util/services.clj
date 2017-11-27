(ns vignette.util.services
  (:require [clojure.string :refer [join]]
    [environ.core :refer [env]]))


(defn materialize-static-asset-url [oid]
  (let [environment (env :wikia-environment)
        physical-dc (env :wikia-datacenter)
        logical-dc (if (= environment "dev") (clojure.string/join "-" [physical-dc environment]) physical-dc)]
    (if (empty? environment) (throw (Exception. "WIKIA_ENVIRONMENT not set"))
      (if (empty? physical-dc) (throw (Exception. "WIKIA_DATACENTER not set"))
        (format "http://%s.%s.k8s.wikia.net/static-assets/image/%s" environment logical-dc oid)))))
