(ns vignette.http.routes-test
  (:require [clojure.java.io :as io]
            [clout.core :refer (route-compile route-matches)]
            [midje.sweet :refer :all]
            [ring.mock.request :refer :all]
            [vignette.http.routes :refer :all]
            [vignette.http.api-routes :refer :all]
            [vignette.protocols :refer :all]
            [vignette.util.image-response :refer :all]
            [vignette.storage.core :refer :all]
            [vignette.storage.local :as ls]
            [vignette.storage.protocols :as sp]
            [vignette.util.image-response :as ir]
            [vignette.test.helper :refer [context-route-matches]]
            [vignette.util.thumbnail :as u]
            [vignette.setup :refer [image-routes]]
            [vignette.http.legacy.routes :as hlr]
            [vignette.media-types :as mt]))

(def in-wiki-context-route-matches (partial context-route-matches ["/:wikia:image-type/:top-dir/:middle-dir/:original/revision/:revision"
                                                                   :wikia wikia-regex
                                                                   :image-type image-type-regex
                                                                   :top-dir top-dir-regex
                                                                   :middle-dir middle-dir-regex]))

(facts :original-route
       (route-matches original-route (request :get "/swift/v1")) => falsey
       (in-wiki-context-route-matches
         original-route
    (request :get "/lotr/3/35/Arwen_Sword.PNG/revision/latest")) => (contains {:wikia "lotr"
                                                                               :top-dir "3"
                                                                               :middle-dir "35"
                                                                               :original "Arwen_Sword.PNG"
                                                                               :revision "latest"})
       (in-wiki-context-route-matches
         original-route
    (request :get "/lotr/3/35/Arwen_Sword.PNG/revision/123456")) => (contains {:wikia "lotr"
                                                                               :top-dir "3"
                                                                               :middle-dir "35"
                                                                               :original "Arwen_Sword.PNG"
                                                                               :revision "123456"})

       (in-wiki-context-route-matches
         original-route
    (request :get "/bucket/a/ab/ropes.jpg/revision/latest")) => (contains {:wikia "bucket"
                                                                           :top-dir "a"
                                                                           :middle-dir "ab"
                                                                           :original "ropes.jpg"}))

(facts :thumbnail-route
  (route-matches thumbnail-route (request :get "something")) => falsey
       (in-wiki-context-route-matches thumbnail-route
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
       (in-wiki-context-route-matches thumbnail-route
                 (request :get "/bucket/a/ab/ropes.jpg/revision/12345/resize/width/10/height/10")) =>
                                 (contains {:wikia "bucket"
                                            :top-dir "a"
                                            :middle-dir "ab"
                                            :original "ropes.jpg"
                                            :thumbnail-mode "resize"
                                            :revision "12345"
                                            :width "10"
                                            :height "10"}))

(facts :create-routes
       ((create-routes (image-routes {})) (request :get "/not-a-valid-route")) => (contains {:status 404}))

(facts :ping_pong
       ((create-routes (image-routes {})) (request :get "/ping")) => (contains {:status 200}))

(facts :all-routes-thumbnail
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
                      :requested-format nil
                      :options {}}]
    ((create-routes (image-routes {:wikia-store ..wiki-store.. :static-store ..static-store..}))
      (request :get "/lotr/3/35/ropes.jpg/revision/latest/thumbnail/width/10/height/10")) => (contains {:status 200})
    (provided
     (u/get-or-generate-thumbnail ..wiki-store.. route-params) => (ls/create-stored-object (io/file "image-samples/ropes.jpg")))

    (:headers ((create-routes (image-routes {:wikia-store ..wiki-store.. :static-store ..static-store..})) (request :get "/lotr/3/35/ropes.jpg/revision/latest/thumbnail/width/10/height/10"))) => (contains {"Content-Type" "image/jpeg"})
    (provided
      (u/get-or-generate-thumbnail ..wiki-store.. route-params) => (ls/create-stored-object (io/file "image-samples/ropes.jpg")))

    ((create-routes (image-routes {:wikia-store ..wiki-store.. :static-store ..static-store..})) (request :get "/lotr/3/35/ropes.jpg/revision/latest/thumbnail/width/10/height/10")) => (contains {:status 404})
    (provided
     (u/get-or-generate-thumbnail ..wiki-store.. route-params) => nil
     (ir/error-image route-params) => ..thumb..
     (ir/create-image-response ..thumb.. route-params) => {})

    ((create-routes (image-routes {:wikia-store ..wiki-store.. :static-store ..static-store..})) (request :get "/lotr/3/35/ropes.jpg/revision/latest/thumbnail/width/10/height/10")) => (contains {:status 500})
    (provided
      (u/get-or-generate-thumbnail ..wiki-store.. route-params) =throws=> (NullPointerException.))))

(facts :all-routes-thumbnail-webp
       (let [route-params {:request-type     :thumbnail
                           :image-type       "images"
                           :original         "ropes.jpg"
                           :revision         "latest"
                           :middle-dir       "35"
                           :top-dir          "3"
                           :wikia            "lotr"
                           :thumbnail-mode   "thumbnail"
                           :height           "10"
                           :width            "10"
                           :requested-format nil
                           :options          {:format mt/webp-format}}]
         ((create-routes (image-routes {:wikia-store ..wiki-store.. :static-store ..static-store..})) (assoc-in (request :get "/lotr/3/35/ropes.jpg/revision/latest/thumbnail/width/10/height/10") [:headers "accept"] "image/webp")) => (contains {:status 200})
         (provided
           (u/get-or-generate-thumbnail ..wiki-store.. route-params) => (ls/create-stored-object (io/file "image-samples/ropes.jpg"))
           )))

(facts :all-routes-original
  (let [route-params {:request-type :original
                      :image-type "images"
                      :original "ropes.jpg"
                      :middle-dir "35"
                      :top-dir "3"
                      :revision "12345"
                      :wikia "lotr"
                      :requested-format nil
                      :options {}}
        file-resource (ls/create-stored-object (io/file "image-samples/ropes.jpg"))
        forced-route-params (assoc route-params :thumbnail-mode "type-convert" :request-type :thumbnail)
        forced-webp-route-params (assoc forced-route-params :options {:format "webp"})
        no-webp-request (request :get "/lotr/3/35/ropes.jpg/revision/12345")
        webp-request (assoc-in no-webp-request [:headers "accept"] "image/webp")]

    ((create-routes (image-routes {:wikia-store ..wiki-store.. :static-store ..static-store..})) no-webp-request) => (contains {:status 200})
    (provided
      (sp/get-thumbnail ..wiki-store.. forced-route-params) => file-resource)

    ((create-routes (image-routes {:wikia-store ..wiki-store.. :static-store ..static-store..})) no-webp-request) => (contains {:status 200})
    (provided
     (sp/get-thumbnail ..wiki-store.. forced-route-params) => nil
     (sp/get-original ..wiki-store.. forced-route-params) => file-resource)

    ((create-routes (image-routes {:wikia-store ..wiki-store.. :static-store ..static-store..})) webp-request) => (contains {:status 200})
    (provided
      (sp/get-thumbnail ..wiki-store.. forced-webp-route-params) => file-resource)

    ((create-routes (image-routes {:wikia-store ..wiki-store.. :static-store ..static-store..})) webp-request) => (contains {:status 200})
    (provided
      (sp/get-thumbnail ..wiki-store.. forced-webp-route-params) => nil
      (u/generate-thumbnail ..wiki-store.. forced-webp-route-params nil) => file-resource)

    ((create-routes (image-routes {:wikia-store ..wiki-store.. :static-store ..static-store..})) no-webp-request) => (contains {:status 404})
    (provided
     (sp/get-thumbnail ..wiki-store.. forced-route-params) => nil
     (sp/get-original ..wiki-store.. forced-route-params) => nil
     (ir/error-image forced-route-params) => ..thumb..
     (ir/create-image-response ..thumb.. forced-route-params) => {})

    ((create-routes (image-routes {:wikia-store ..wiki-store.. :static-store ..static-store..})) no-webp-request) => (contains {:status 500})
    (provided
      (sp/get-thumbnail ..wiki-store.. forced-route-params) =throws=> (NullPointerException.)
    )))

(facts :window-crop-route
       (in-wiki-context-route-matches window-crop-route
                      (request :get "/muppet/images/4/40/JohnvanBruggen.jpg/revision/latest/window-crop/width/200/x-offset/0/y-offset/29/window-width/206/window-height/103")) =>
       {:wikia "muppet"
        :image-type "/images"
        :top-dir "4"
        :middle-dir "40"
        :original "JohnvanBruggen.jpg"
        :revision "latest"
        :thumbnail-mode "window-crop"
        :width "200"
        :x-offset "0"
        :y-offset "29"
        :window-width "206"
        :window-height "103"}

       (in-wiki-context-route-matches window-crop-route
                      (request :get "/muppet/images/4/40/JohnvanBruggen.jpg/revision/latest/window-crop/width/200/x-offset/-1/y-offset/29/window-width/206/window-height/103")) =>
       {:wikia "muppet"
        :image-type "/images"
        :top-dir "4"
        :middle-dir "40"
        :original "JohnvanBruggen.jpg"
        :revision "latest"
        :thumbnail-mode "window-crop"
        :width "200"
        :x-offset "-1"
        :y-offset "29"
        :window-width "206"
        :window-height "103"})

(facts :window-crop-fixed-route
       (in-wiki-context-route-matches window-crop-fixed-route
                      (request :get "/thelastofus/images/5/58/Door_4.jpg/revision/latest/window-crop-fixed/width/400/height/400/x-offset/400/y-offset/200/window-width/200/window-height/400")) =>
       {:wikia "thelastofus"
        :image-type "/images"
        :top-dir "5"
        :middle-dir "58"
        :original "Door_4.jpg"
        :revision "latest"
        :thumbnail-mode "window-crop-fixed"
        :width "400"
        :height "400"
        :x-offset "400"
        :y-offset "200"
        :window-width "200"
        :window-height "400"})

(facts :scale-to-width-route
       (in-wiki-context-route-matches scale-to-width-route
                      (request :get "/muppet/4/40/JohnvanBruggen.jpg/revision/latest/scale-to-width/200")) =>
       {:wikia "muppet"
        :image-type ""
        :top-dir "4"
        :middle-dir "40"
        :original "JohnvanBruggen.jpg"
        :revision "latest"
        :thumbnail-mode "scale-to-width"
        :width "200"})

(facts :scale-to-width-down-route
       (in-wiki-context-route-matches scale-to-width-down-route
                        (request :get "/muppet/4/40/JohnvanBruggen.jpg/revision/latest/scale-to-width-down/200")) =>
        {:wikia "muppet"
         :image-type ""
         :top-dir "4"
         :middle-dir "40"
         :original "JohnvanBruggen.jpg"
         :revision "latest"
         :thumbnail-mode "scale-to-width-down"
         :width "200"})

(facts :scale-to-height-down-route
       (in-wiki-context-route-matches scale-to-height-down-route
                        (request :get "/muppet/4/40/JohnvanBruggen.jpg/revision/latest/scale-to-height-down/200")) =>
        {:wikia "muppet"
         :image-type ""
         :top-dir "4"
         :middle-dir "40"
         :original "JohnvanBruggen.jpg"
         :revision "latest"
         :thumbnail-mode "scale-to-height-down"
         :height "200"})

(facts :avatar-request
       (in-wiki-context-route-matches scale-to-width-route
                      (request :get "/common/avatars/7/7c/1271044.png/revision/latest/scale-to-width/150")) =>
       {:wikia "common"
        :image-type "/avatars"
        :top-dir "7"
        :middle-dir "7c"
        :original "1271044.png"
        :revision "latest"
        :thumbnail-mode "scale-to-width"
        :width "150"})
