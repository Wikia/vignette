(ns vignette.http.routes-test
  (:require [clojure.java.io :as io]
            [clout.core :refer (route-compile route-matches)]
            [midje.sweet :refer :all]
            [ring.mock.request :refer :all]
            [vignette.http.routes :refer :all]
            [vignette.protocols :refer :all]
            [vignette.util.image-response :refer :all]
            [vignette.storage.core :refer :all]
            [vignette.storage.local :as ls]
            [vignette.storage.protocols :as sp]
            [vignette.util.image-response :as ir]
            [vignette.util.thumbnail :as u])
  (:import java.io.FileNotFoundException))

(facts :image-request-handler
  (image-request-handler {} :foo {}) => (throws IllegalArgumentException)

  (image-request-handler ..system.. :thumbnail ..request..) => ..response..
  (provided
    (get-image-params ..request.. :thumbnail) => ..params..
    (handle-thumbnail ..system.. ..params..) => ..response..)

  (image-request-handler ..system.. :original ..request..) => ..response..
  (provided
    (get-image-params ..request.. :original) => ..params..
    (handle-original ..system.. ..params..) => ..response..))

(facts :image-request-handler :scale-to-width
  (let [route-params (route-matches scale-to-width-route (request :get "muppet/images/d/d4/Mo-Yet.jpg/revision/latest/scale-to-width/212"))
       request {:request-method :get :route-params route-params}
       merged-route-params (merge route-params {:thumbnail-mode "scale-to-width" :height :auto})]
    (image-request-handler ..system.. :thumbnail
                           request
                           :thumbnail-mode "scale-to-width"
                           :height :auto)  => (contains {:status 200})
   (provided
    (get-image-params request :thumbnail) => merged-route-params
    (handle-thumbnail ..system.. merged-route-params) => {:body nil :status 200})))

(facts :handle-thumbnail
  (handle-thumbnail ..system.. ..params..) => ..response..
  (provided
    (u/get-or-generate-thumbnail ..system.. ..params..) => ..thumb..
    (create-image-response ..thumb.. ..params..) => ..response..)

  (handle-thumbnail ..system.. ..params..) => ..error..
  (provided
    (u/get-or-generate-thumbnail ..system.. ..params..) => nil
    (error-response 404 ..params..) => ..error..))

(facts :handle-original
  (handle-original ..system.. ..params..) => ..response..
  (provided
    (store ..system..) => ..store..
    (sp/get-original ..store.. ..params..) => ..original..
    (create-image-response ..original.. ..params..) => ..response..)

  (handle-original ..system.. ..params..) => ..error..
  (provided
    (store ..system..) => ..store..
    (sp/get-original ..store.. ..params..) => nil
    (error-response 404 ..params..) => ..error..))

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
                      :image-type "images"
                      :original "ropes.jpg"
                      :revision "latest"
                      :middle-dir "35"
                      :top-dir "3"
                      :wikia "lotr"
                      :thumbnail-mode "thumbnail"
                      :height "10"
                      :width "10"
                      :options {}}]
    ((app-routes ..system..) (request :get "/lotr/3/35/ropes.jpg/revision/latest/thumbnail/width/10/height/10")) => (contains {:status 200})
    (provided
     (u/get-or-generate-thumbnail ..system.. route-params) => (ls/create-stored-object (io/file "image-samples/ropes.jpg")))

    ((app-routes ..system..) (request :get "/lotr/3/35/ropes.jpg/revision/latest/thumbnail/width/10/height/10")) => (contains {:status 404})
    (provided
     (u/get-or-generate-thumbnail ..system.. route-params) => nil
     (ir/error-image route-params) => ..thumb..
     (ir/create-image-response ..thumb.. route-params) => {})

    ((app-routes ..system..) (request :get "/lotr/3/35/ropes.jpg/revision/latest/thumbnail/width/10/height/10")) => (contains {:status 500})
    (provided
      (u/get-or-generate-thumbnail ..system.. route-params) =throws=> (NullPointerException.))))

(facts :app-routes-original

  (let [route-params {:request-type :original
                      :image-type "images"
                      :original "ropes.jpg"
                      :middle-dir "35"
                      :top-dir "3"
                      :revision "12345"
                      :wikia "lotr"
                      :options {}} ]
    ((app-routes ..system..) (request :get "/lotr/3/35/ropes.jpg/revision/12345")) => (contains {:status 200})
    (provided
     (store ..system..) => ..store..
     (sp/get-original ..store.. route-params) => (ls/create-stored-object (io/file "image-samples/ropes.jpg")))

    ((app-routes ..system..) (request :get "/lotr/3/35/ropes.jpg/revision/12345")) => (contains {:status 404})
    (provided
     (store ..system..) => ..store..
     (sp/get-original ..store.. route-params) => nil)

    ((app-routes ..system..) (request :get "/lotr/3/35/ropes.jpg/revision/12345")) => (contains {:status 500})
    (provided
      (store ..system..) => ..store..
      (sp/get-original ..store.. route-params) =throws=> (NullPointerException.))))

(facts :window-crop-route
       (route-matches window-crop-route
                      (request :get "/muppet/images/4/40/JohnvanBruggen.jpg/revision/latest/window-crop/width/200/x-offset/0/y-offset/29/window-width/206/window-height/103")) =>
       {:wikia "muppet"
        :image-type "/images"
        :top-dir "4"
        :middle-dir "40"
        :original "JohnvanBruggen.jpg"
        :revision "latest"
        :width "200"
        :x-offset "0"
        :y-offset "29"
        :window-width "206"
        :window-height "103"}
       (route-matches window-crop-route
                      (request :get "/muppet/images/4/40/JohnvanBruggen.jpg/revision/latest/window-crop/width/200/x-offset/-1/y-offset/29/window-width/206/window-height/103")) =>
       {:wikia "muppet"
        :image-type "/images"
        :top-dir "4"
        :middle-dir "40"
        :original "JohnvanBruggen.jpg"
        :revision "latest"
        :width "200"
        :x-offset "-1"
        :y-offset "29"
        :window-width "206"
        :window-height "103"})

(facts :window-crop-fixed-route
       (route-matches window-crop-fixed-route
                      (request :get "/thelastofus/images/5/58/Door_4.jpg/revision/latest/window-crop-fixed/width/400/height/400/x-offset/400/y-offset/200/window-width/200/window-height/400")) =>
       {:wikia "thelastofus"
        :image-type "/images"
        :top-dir "5"
        :middle-dir "58"
        :original "Door_4.jpg"
        :revision "latest"
        :width "400"
        :height "400"
        :x-offset "400"
        :y-offset "200"
        :window-width "200"
        :window-height "400"})

(facts :scale-to-width-route
       (route-matches scale-to-width-route
                      (request :get "/muppet/4/40/JohnvanBruggen.jpg/revision/latest/scale-to-width/200")) =>
       {:wikia "muppet"
        :image-type ""
        :top-dir "4"
        :middle-dir "40"
        :original "JohnvanBruggen.jpg"
        :revision "latest"
        :width "200"})

(facts :avatar-request
       (route-matches scale-to-width-route
                      (request :get "/common/avatars/7/7c/1271044.png/revision/latest/scale-to-width/150")) =>
       {:wikia "common"
        :image-type "/avatars"
        :top-dir "7"
        :middle-dir "7c"
        :original "1271044.png"
        :revision "latest"
        :width "150"})

(facts :route-params->image-type
       (route-params->image-type {:image-type ""}) => "images"
       (route-params->image-type {:image-type "/images"}) => "images"
       (route-params->image-type {:image-type "/avatars"}) => "avatars")
