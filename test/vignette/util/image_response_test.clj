(ns vignette.util.image-response-test
  (:require [clojure.java.io :as io]
            [clout.core :refer (route-compile route-matches)]
            [midje.sweet :refer :all]
            [ring.mock.request :refer :all]
            [vignette.test.helper :refer [context-route-matches]]
            [vignette.http.route-helpers :refer :all]
            [vignette.http.api-routes :refer :all]
            [vignette.util.image-response :refer :all]
            [vignette.storage.core :refer :all]
            [vignette.storage.local :refer [create-stored-object]]
            [vignette.util.image-response :as ir]
            [ring.util.codec :refer [url-encode]]
            [vignette.util.regex :refer :all]))

(def in-wiki-context-route-matches (partial context-route-matches ["/:wikia:image-type/:top-dir/:middle-dir/:original/revision/:revision"
                                                                   :wikia wikia-regex
                                                                   :image-type image-type-regex
                                                                   :top-dir top-dir-regex
                                                                   :middle-dir middle-dir-regex]))

(facts :create-image-response
       (let [image-map
             (route->original-map (in-wiki-context-route-matches
                                    original-route
                                    (request :get "/lotr/3/35/ropes.jpg/revision/latest"))
                                  {})
             response (create-image-response (create-stored-object "image-samples/ropes.jpg") image-map)
             response-headers (:headers response)]
         (get response-headers "Surrogate-Key") => "7d1d24f2c2af364882953e8c97bf90092c2f7a08"
         (get response-headers "Content-Disposition") => "inline; filename=\"ropes.jpg\"; filename*=UTF-8''ropes.jpg"
         (get response-headers "Content-Length") => "23"
         (get response-headers "ETag") => "\"c1cfdb01ca32d56c29cf349af37a6779\""))

(facts :add-content-disposition-header
       (add-content-disposition-header {} {:original "some-file.png"}) => {:headers {"Content-Disposition" "inline; filename=\"some-file.png\"; filename*=UTF-8''some-file.png"}}
       (add-content-disposition-header {} {:original "some-\"file\".png"}) => {:headers {"Content-Disposition" "inline; filename=\"some-\\\"file\\\".png\"; filename*=UTF-8''some-%5C%22file%5C%22.png"}}
       (add-content-disposition-header {} {:original "some-\"file,_with_comma!\".png"}) => {:headers {"Content-Disposition" "inline; filename=\"some-\\\"file,_with_comma!\\\".png\"; filename*=UTF-8''some-%5C%22file%2C_with_comma%21%5C%22.png"}})

(facts :when-header-val
       (when-header-val {} "Content-Type" nil) => {}
       (when-header-val {} "Content-Type" "type") => {:headers {"Content-Type" "type"}})

(facts :add-surrogate-key-header
       (add-surrogate-header {} {:uuid "123"}) => {:headers {"Surrogate-Key" "123"}}
       (add-surrogate-header {} {:wikia "wikia" :original "orig" :image-type "images"}) =>
       {:headers {"Surrogate-Key"   "c8dfba77e9beb5c26ca20d4411674065d4a0ded5"}})

(facts :add-vary-header
       (add-vary-header {} {:requested-format nil :request-type :thumbnail } "image/png") => {:headers {"Vary" "Accept"}}
       (add-vary-header {} {:requested-format nil :request-type :thumbnail } "image/webp") => {:headers {"Vary" "Accept"}}
       (add-vary-header {} {:request-type :thumbnail} "image/jpeg") => {} ;; legacy routes - no Vary: Accept
       (add-vary-header {} {:requested-format nil :request-type :original} "image/png") => {:headers {"Vary" "Accept"}}
       (add-vary-header {} {:request-type :original} "image/jpeg") => {} ;; legacy routes - no Vary: Accept
       (add-vary-header {} {:requested-format nil :request-type :thumbnail} "image/bmp") => {}
       (add-vary-header {} {:requested-format "png" :request-type :thumbnail} "image/png") => {}
       (add-vary-header {} {:requested-format "original" :request-type :thumbnail} "image/png") => {}
       (add-vary-header {} {:requested-format "original" :request-type :original} "image/png") => {},
       (add-vary-header {} {:requested-format nil :request-type :original} "image/gif") => {:headers {"Vary" "Accept"}})
