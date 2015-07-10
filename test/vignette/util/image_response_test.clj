(ns vignette.util.image-response-test
 (:require [clojure.java.io :as io]
           [clout.core :refer (route-compile route-matches)]
           [midje.sweet :refer :all]
           [ring.mock.request :refer :all]
           [vignette.http.route-helpers :refer :all]
           [vignette.http.routes :refer :all]
           [vignette.util.image-response :refer :all]
           [vignette.storage.core :refer :all]
           [vignette.storage.local :refer [create-stored-object]]
           [vignette.util.image-response :as ir]))

(facts :create-image-response
  (let [image-map 
        (route->original-map (route-matches
                               original-route
                               (request :get "/lotr/3/35/ropes.jpg/revision/latest"))
                             {})
        response (create-image-response (create-stored-object "image-samples/ropes.jpg")  image-map)
        response-headers (:headers response)]
    (get response-headers "Surrogate-Key") => "7d1d24f2c2af364882953e8c97bf90092c2f7a08"
    (get response-headers "Content-Disposition") => "inline; filename=\"ropes.jpg\""
    (get response-headers "Content-Length") => "23"
    (get response-headers "ETag") => "c1cfdb01ca32d56c29cf349af37a6779"))


(facts :add-content-disposition-header
       (add-content-disposition-header {} {:original "some-file.png"}) => {:headers {"Content-Disposition" "inline; filename=\"some-file.png\""}}
       (add-content-disposition-header {} {:original "some-\"file\".png"}) => {:headers {"Content-Disposition" "inline; filename=\"some-\\\"file\\\".png\""}}
       (add-content-disposition-header {} {:original "some-\"file,_with_comma!\".png"}) => {:headers {"Content-Disposition" "inline; filename=\"some-\\\"file,_with_comma!\\\".png\""}})
