(ns vignette.util.services-test
  (:require [midje.sweet :refer :all]
            [vignette.util.services :as svc]
            [vignette.util.consul :as consul]))

(facts :static-assets :url
       (svc/materialize-static-asset-url "uuid_sample") => "http://hostname:9999/image/uuid_sample"
       (provided
         (consul/find-service consul/create-consul "static-assets" "prod") => {:address "hostname", :port 9999}))

(facts :image-review :url
       (svc/materialize-image-review-url "uuid_sample" nil nil) => "http://hostname:9999/image-review/image/uuid_sample?status=ACCEPTED&status=QUESTIONABLE&status=UNREVIEWED"
       (provided
         (consul/find-service consul/create-consul "image-review" "prod") => {:address "hostname", :port 9999}))

(facts :image-review :url
       (svc/materialize-image-review-url "uuid_sample" "REJECTED" nil) => "http://hostname:9999/image-review/image/uuid_sample?status=REJECTED"
       (provided
         (consul/find-service consul/create-consul "image-review" "prod") => {:address "hostname", :port 9999}))

(facts :image-review :url
       (svc/materialize-image-review-url "uuid_sample" "REJECTED" "f81a4d41-fa77-4445-8168-18d84e0437ab") =>
       "http://hostname:9999/image-review/image/uuid_sample?status=REJECTED&fastlyBypass=f81a4d41-fa77-4445-8168-18d84e0437ab"
       (provided
         (consul/find-service consul/create-consul "image-review" "prod") => {:address "hostname", :port 9999}))

(facts :image-review :url
       (svc/materialize-image-review-url "uuid_sample" nil "f81a4d41-fa77-4445-8168-18d84e0437ab") =>
       "http://hostname:9999/image-review/image/uuid_sample?status=ACCEPTED&status=QUESTIONABLE&status=UNREVIEWED&fastlyBypass=f81a4d41-fa77-4445-8168-18d84e0437ab"
       (provided
         (consul/find-service consul/create-consul "image-review" "prod") => {:address "hostname", :port 9999}))
