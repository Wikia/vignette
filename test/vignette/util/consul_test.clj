(ns vignette.util.consul_test
  (:use midje.sweet)
  (:require
    [vignette.util.consul :as c]
    ))

(def sample-response
  [{:Checks  [{
               :CheckID     "service:static-assets_31834",
               :Name        "Service 'static-assets' check",
               :Node        "mesos-s124",
               :Notes       "",
               :Output      "",
               :ServiceID   "static-assets_31834",
               :ServiceName "static-assets",
               :Status      "passing"}
              {
               :CheckID     "serfHealth",
               :Name        "Serf Health Status",
               :Node        "mesos-s4",
               :Notes       "",
               :Output      "Agent alive and reachable",
               :ServiceID   "",
               :ServiceName "",
               :Status      "passing"}],
    :Node    {
              :Address "10.8.62.63",
              :Node    "mesos-s4"},
    :Service {
              :Address "",
              :ID      "static-assets_31834",
              :Port    31834,
              :Service "static-assets",
              :Tags    ["production" "swagger"]}}
   {:Checks  [{
               :CheckID     "service:static-assets_31368",
               :Name        "Service 'static-assets' check",
               :Node        "mesos-s331",
               :Notes       "",
               :Output      "",
               :ServiceID   "static-assets_31368",
               :ServiceName "static-assets",
               :Status      "passing"}
              {:CheckID     "serfHealth",
               :Name        "Serf Health Status",
               :Node        "mesos-s3",
               :Notes       "",
               :Output      "Agent alive and reachable",
               :ServiceID   "",
               :ServiceName "",
               :Status      "passing"}],
    :Node    {
              :Address "10.8.38.30",
              :Node    "mesos-s3"},
    :Service {
              :Address "",
              :ID      "static-assets_31368",
              :Port    31368,
              :Service "static-assets",
              :Tags    ["production" "swagger"]}}])


(defrecord MockConsul [mock-response] c/Consul
  (query-service [this service tag] mock-response))

;instead of rand always pick first entry
(with-redefs [c/pick-service-entry-fn first]
  (fact [{:address "10.8.62.63", :port 31834} {:address "10.8.38.30", :port 31368}]
        => (contains [(#'c/response->address sample-response)])))

(fact (#'c/response->address nil)
      => nil?)

(with-redefs [c/pick-service-entry-fn first]
  (fact (c/find-service (MockConsul. sample-response) "static-assets" "tag")
        => {:address "10.8.62.63", :port 31834}))

(fact (c/->uri {:address "10.8.62.63", :port 31834}) => "http://10.8.62.63:31834")

(fact (c/find-service (MockConsul. []) "static-assets" " tag") => nil)
