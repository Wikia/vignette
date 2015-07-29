(ns vignette.test.helper
  (:require
    [ring.mock.request :refer [request]]
    [clout.core :refer [route-compile route-matches]]))


(defn context-route-matches [ctx compiledroute match-request]
  (let [outer (route-matches
                (route-compile (str (first ctx) ":__rest") (merge (apply hash-map (rest ctx)) {:__rest #"|/.*"}))
                match-request)]
    (merge (route-matches compiledroute
                          (request :get (:__rest outer))) outer)
    ))

