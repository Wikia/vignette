(ns vignette.util.external-hotlinking-test
  (:require [clout.core :refer [route-matches]]
            [midje.sweet :refer :all]
            [ring.mock.request :refer :all]
            [vignette.http.api-routes :refer :all]
            [vignette.http.route-helpers :refer :all]
            [vignette.util.external-hotlinking :refer :all]))

(def original-url "/wikiaglobal/images/5/58/Wikia-Visualization-Main,croatiaempire.png/revision/latest")
(def original-legacy-url "/wikiaglobal/images/5/58/Wikia-Visualization-Main,croatiaempire.png")

(defn generate-request
  ([url]
    (generate-request url false))
  ([url include-header?]
    (let [request (request :get url)]
      (if include-header?
        (assoc-in request [:headers force-header-name] force-header-val)
        request))))

(facts :force-thumb?
       (let [original-force (generate-request original-url true)
             original-noforce (generate-request original-url)
             legacy-force (generate-request original-legacy-url true)
             legacy-noforce (generate-request original-legacy-url)]
         (force-thumb? original-force) => true
         (force-thumb? original-noforce) => falsey
         (force-thumb? legacy-force) => true
         (force-thumb? legacy-noforce) => falsey))

(facts :image-params->forced-thumb-params
       (let [request (generate-request original-url)
             route-match (route-matches original-route request)
             request (assoc-in request [:route-params] route-match)
             image-params (route->original-map route-match request)]
         (image-params->forced-thumb-params image-params) => (contains force-thumb-params)))
