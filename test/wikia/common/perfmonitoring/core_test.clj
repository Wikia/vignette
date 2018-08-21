(ns wikia.common.perfmonitoring.core-test
  (:require [midje.sweet :refer :all]
            [wikia.common.perfmonitoring.core :refer :all]))

(facts :series-timing
  (timing :test (let [x true] x)) => true
  (provided
    (get-series-name) => :series
    (current-time) => 0
    (publish :series {:test 0}) => nil)
  
  (timing :test (throw (Exception. "something failed"))) => (throws Exception)
  (provided
    (get-series-name) => :series
    (current-time) => 0
    (publish :series {:test 0}) => nil))
