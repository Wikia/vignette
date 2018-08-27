(ns vignette.perfmonitoring.core-test
  (:require [midje.sweet :refer :all]
            [vignette.perfmonitoring.core :refer :all]
            [prometheus.core :as prometheus]))

(facts :publish
   (publish {:request-count 1}) => nil
   (provided
     (prometheus/increase-counter anything "vignette" "request_count" [] 1) => nil)

   (publish {::request-count 1 :generate-thumbnail 2}) => nil
   (provided
     (prometheus/increase-counter anything "vignette" "request_count" [] 1) => nil
     (prometheus/increase-counter anything "vignette" "generate_thumbnail" [] 2) => nil)

   (publish {:test-a 1}) => nil
   (provided
     (prometheus/increase-counter anything "vignette" "test_a" [] 1) => nil))
