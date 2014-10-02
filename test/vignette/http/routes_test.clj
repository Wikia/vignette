(ns vignette.http.routes-test
  (:require [clojure.java.io :as io]
            [clout.core :refer (route-compile route-matches)]
            [midje.sweet :refer :all]
            [ring.mock.request :refer :all]
            [vignette.http.routes :refer :all]
            [vignette.protocols :refer :all]
            [vignette.storage.local :as l]
            [vignette.storage.protocols :as sp]
            [vignette.util.thumbnail :as u])
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
     (u/get-or-generate-thumbnail ..system.. route-params) => (l/create-stored-object (io/file "image-samples/ropes.jpg")))

    ((app-routes ..system..) (request :get "/lotr/3/35/ropes.jpg/revision/latest/resize/width/10/height/10")) => (contains {:status 404})
    (provided
     (u/get-or-generate-thumbnail ..system.. route-params) => nil)

    ((app-routes ..system..) (request :get "/lotr/3/35/ropes.jpg/revision/latest/resize/width/10/height/10")) => (contains {:status 500})
    (provided
      (u/get-or-generate-thumbnail ..system.. route-params) =throws=> (NullPointerException.))))

(facts :app-routes-original

  (let [route-params {:request-type :original
                      :original "ropes.jpg"
                      :middle-dir "35"
                      :top-dir "3"
                      :revision "12345"
                      :wikia "lotr"
                      :options {}} ]
    ((app-routes ..system..) (request :get "/lotr/3/35/ropes.jpg/revision/12345")) => (contains {:status 200})
    (provided
     (store ..system..) => ..store..
     (sp/get-original ..store.. route-params) => (l/create-stored-object (io/file "image-samples/ropes.jpg")))

    ((app-routes ..system..) (request :get "/lotr/3/35/ropes.jpg/revision/12345")) => (contains {:status 404})
    (provided
     (store ..system..) => ..store..
     (sp/get-original ..store.. route-params) => nil)

    ((app-routes ..system..) (request :get "/lotr/3/35/ropes.jpg/revision/12345")) => (contains {:status 500})
    (provided
      (store ..system..) => ..store..
      (sp/get-original ..store.. route-params) =throws=> (NullPointerException.))))
