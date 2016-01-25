(ns vignette.util.services-test
  (:require [midje.sweet :refer :all]
            [vignette.util.services :as svc]
            [vignette.util.consul :as consul]))

(facts :static-assets :url
       (svc/materialize-static-asset-url "uuid_sample") => "http://hostname:9999/image/uuid_sample"
       (provided
         (consul/find-service consul/create-consul "static-assets" "prod") => {:address "hostname", :port 9999}))

(facts :image-review :url
       (svc/materialize-image-review-url "uuid_sample") => "http://hostname:9999/image-review/image/uuid_sample?status=ACCEPTED&status=FLAGGED&status=UNREVIEWED"
       (provided
         (consul/find-service consul/create-consul "image-review" "prod") => {:address "hostname", :port 9999}))
