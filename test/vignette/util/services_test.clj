(ns vignette.util.services-test
  (:require [midje.sweet :refer :all]
            [vignette.util.services :as svc]
            [vignette.util.consul :as consul]))

(facts :static-assets :url
       (svc/materialize-static-asset-url "uuid_sample") => "http://hostname:9999/image/uuid_sample"
       (provided
         (consul/find-service consul/create-consul "static-assets" "prod") => {:address "hostname", :port 9999})
       (consul/create-consul ..dupa..) => ..aaa..)

;(defn materialize-static-asset-url [oid]
;  (str
;    (consul/->uri
;      (consul/find-service
;        consul/create-consul "static-assets" consul/service-query-tag)) "/image/" oid))
;
;(defn materialize-image-review-url [oid & statuses]
;  (let [query-params (str "status=" (if (empty? statuses) (join "&status=" ["FLAGGED" "UNREVIEWED"]) (join "&" statuses)))]
;    (str
;      (consul/->uri
;        (consul/find-service
;          consul/create-consul "image-review" consul/service-query-tag)) "/image-review/image/" oid "?" query-params)))
