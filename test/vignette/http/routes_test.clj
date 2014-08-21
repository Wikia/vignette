(ns vignette.http.routes-test
  (:require [vignette.http.routes :refer :all]
            [vignette.storage.protocols :refer :all]
            [vignette.protocols :refer :all]
            [vignette.util.thumbnail :as u]
            [midje.sweet :refer :all]
            [clout.core :refer (route-compile route-matches)]
            [ring.mock.request :refer :all]
            [clojure.java.io :as io])
  (:import java.io.FileNotFoundException))

(facts :original-route
  (route-matches original-route (request :get "/swift/v1")) => falsey
  (route-matches
    original-route
    (request :get "/lotr/3/35/Arwen_Sword.PNG/revision/latest")) => (contains {:wikia "lotr"
                                                                               :top-dir "3"
                                                                               :middle-dir "35"
                                                                               :original "Arwen_Sword.PNG"
                                                                               :revision "latest"})
  (route-matches
    original-route
    (request :get "/lotr/3/35/Arwen_Sword.PNG/revision/123456")) => (contains {:wikia "lotr"
                                                                               :top-dir "3"
                                                                               :middle-dir "35"
                                                                               :original "Arwen_Sword.PNG"
                                                                               :revision "123456"})

  (route-matches
    original-route
    (request :get "/bucket/a/ab/ropes.jpg/revision/latest")) => (contains {:wikia "bucket"
                                                                           :top-dir "a"
                                                                           :middle-dir "ab"
                                                                           :original "ropes.jpg"}))

(facts :thumbnail-route
  (route-matches thumbnail-route (request :get "something")) => falsey
  (route-matches thumbnail-route
                 (request :get
                          "/lotr/3/35/Arwen_Sword.PNG/revision/latest/resize/width/250/height/250")) =>
                            (contains {:wikia "lotr"
                                       :top-dir "3"
                                       :middle-dir "35"
                                       :original "Arwen_Sword.PNG"
                                       :thumbnail-mode "resize"
                                       :revision "latest"
                                       :width "250"
                                       :height "250"})
  (route-matches thumbnail-route
                 (request :get "/bucket/a/ab/ropes.jpg/revision/12345/resize/width/10/height/10")) =>
                                 (contains {:wikia "bucket"
                                            :top-dir "a"
                                            :middle-dir "ab"
                                            :original "ropes.jpg"
                                            :thumbnail-mode "resize"
                                            :revision "12345"
                                            :width "10"
                                            :height "10"}))

(facts :adjust-original-route
  (route-matches adjust-original-route (request :get "foobar")) => falsey
  (route-matches adjust-original-route (request :get "/bucket/a/ab/ropes.jpg/revision/latest/reorient")) =>
                                                       (contains {:wikia "bucket"
                                                                  :top-dir "a"
                                                                  :middle-dir "ab"
                                                                  :original "ropes.jpg"
                                                                  :revision "latest"
                                                                  :mode "reorient"}))

(facts :app-routes
  ((app-routes nil) (request :get "/not-a-valid-route")) => (contains {:status 404}))

(facts :app-routes-thumbnail
  (let [route-params {:request-type :thumbnail
                      :original "ropes.jpg"
                      :revision "latest"
                      :middle-dir "35"
                      :top-dir "3"
                      :wikia "lotr"
                      :thumbnail-mode "resize"
                      :height "10"
                      :width "10"
                      :options {}}]
    ((app-routes ..system..) (request :get "/lotr/3/35/ropes.jpg/revision/latest/resize/width/10/height/10")) => (contains {:status 200})
    (provided
     (u/get-or-generate-thumbnail ..system.. route-params) => (io/file "image-samples/ropes.jpg"))

    ((app-routes ..system..) (request :get "/lotr/3/35/ropes.jpg/revision/latest/resize/width/10/height/10")) => (contains {:status 404})
    (provided
     (u/get-or-generate-thumbnail ..system.. route-params) => nil)

    ((app-routes ..system..) (request :get "/lotr/3/35/ropes.jpg/revision/latest/resize/width/10/height/10")) => (contains {:status 503})
    (provided
      (u/get-or-generate-thumbnail ..system.. route-params) => (throws java.io.FileNotFoundException))))

(facts :app-routes-original

  (let [route-params {:request-type :original
                      :original "ropes.jpg"
                      :middle-dir "35"
                      :top-dir "3"
                      :revision "12345"
                      :wikia "lotr"} ]
    ((app-routes ..system..) (request :get "/lotr/3/35/ropes.jpg/revision/12345")) => (contains {:status 200})
    (provided
     (store ..system..) => ..store..
     (get-original ..store.. route-params) => (io/file "image-samples/ropes.jpg"))

    ((app-routes ..system..) (request :get "/lotr/3/35/ropes.jpg/revision/12345")) => (contains {:status 404})
    (provided
     (store ..system..) => ..store..
     (get-original ..store.. route-params) => nil)

    ((app-routes ..system..) (request :get "/lotr/3/35/ropes.jpg/revision/12345")) => (contains {:status 503})
    (provided
      (store ..system..) => ..store..
      (get-original ..store.. route-params) => (throws java.io.FileNotFoundException))))
