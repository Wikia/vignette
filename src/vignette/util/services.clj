(ns vignette.util.services
  (:require [clojure.string :refer [join]]
            [environ.core :refer [env]]))


(defn get-domain []
  (let [environment (env :wikia-environment)
        datacenter (env :wikia-datacenter)]
    (join "." [environment
               (if (= environment "dev") (join "-" [datacenter environment]) datacenter)
               "k8s.wikia.net"])))

(defn materialize-static-asset-url [oid]
  (str (get-domain) "/static-assets/image/" oid))
