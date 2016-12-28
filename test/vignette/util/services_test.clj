(ns vignette.util.services-test
  (:require [midje.sweet :refer :all]
            [vignette.util.services :as svc]
            [vignette.util.consul :as consul]))

(facts :static-assets :url
       (svc/materialize-static-asset-url "uuid_sample") => "http://hostname:9999/image/uuid_sample"
       (provided
         (consul/find-service consul/create-consul "static-assets" "prod") => {:address "hostname", :port 9999}))