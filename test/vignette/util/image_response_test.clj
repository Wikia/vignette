(ns vignette.util.image-response-test
 (:require [clojure.java.io :as io]
           [clout.core :refer (route-compile route-matches)]
           [midje.sweet :refer :all]
           [ring.mock.request :refer :all]
           [vignette.http.routes :refer :all]
           [vignette.util.image-response :refer :all]
           [vignette.storage.core :refer :all]
           [vignette.storage.local :refer [create-stored-object]]
           [vignette.util.image-response :as ir]))

(facts :create-image-response
  (let [image-map 
        (get-image-params {:route-params (route-matches
                                           original-route
                                           (request :get "/lotr/3/35/ropes.jpg/revision/latest"))}
                          :original)
        response (create-image-response (create-stored-object "image-samples/ropes.jpg")  image-map)
        response-headers (:headers response)]
    (get response-headers "Surrogate-Key") => "lotr/images/3/35/ropes.jpg"
    (get response-headers "Content-Disposition") => "inline; filename=\"ropes.jpg\""))
