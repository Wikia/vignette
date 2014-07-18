(ns vignette.http.routes-test
  (:require [vignette.http.routes :refer :all]
            [midje.sweet :refer :all]
            [clout.core :refer (route-compile route-matches)]
            [ring.mock.request :refer :all]))

(facts :original-route
  (route-matches original-route (request :get "/swift/v1")) => falsey
  (route-matches
    original-route
    (request :get "/lotr/3/35/Arwen_Sword.PNG")) => (contains {:wikia "lotr"
                                                               :top-dir "3"
                                                               :middle-dir "35"
                                                               :original "Arwen_Sword.PNG"}))

(facts :thumbnail-route
  (route-matches thumbnail-route (request :get "something")) => falsey
  (route-matches thumbnail-route
                 (request :get "/lotr/3/35/Arwen_Sword.PNG/resize/250/250")) => (contains {:wikia "lotr"
                                                                                           :top-dir "3"
                                                                                           :middle-dir "35"
                                                                                           :original "Arwen_Sword.PNG"
                                                                                           :mode "resize"
                                                                                           :width "250"
                                                                                           :height "250"
                                                                                           }))
