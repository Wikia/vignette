(ns vignette.http.route-helpers-test
  (:require [clojure.java.io :as io]
            [clout.core :refer (route-compile route-matches)]
            [midje.sweet :refer :all]
            [ring.mock.request :refer :all]
            [vignette.http.route-helpers :refer :all]
            [vignette.protocols :refer :all]
            [vignette.util.image-response :refer :all]
            [vignette.storage.protocols :as sp]
            [vignette.util.image-response :as ir]
            [vignette.util.thumbnail :as u])
  (:import java.io.FileNotFoundException))

(def webp-support-request
  (assoc-in (request :get "/bucket/a/ab/ropes.jpg/revision/12345/resize/width/10/height/10") [:headers "accept"] "image/webp"))

(def no-webp-support-request
  (request :get "/bucket/a/ab/ropes.jpg/revision/12345/resize/width/10/height/10"))

(def request-with-format
  (assoc-in (request :get "/bucket/a/ab/ropes.jpg/revision/12345/resize/width/10/height/10") [:query-params :format] "jpg"))

(def request-without-format
  (request :get "/bucket/a/ab/ropes.jpg/revision/12345/resize/width/10/height/10"))

(facts :handle-thumbnail
       (handle-thumbnail ..store.. ..params.. ..request..) => ..response..
       (provided
         (u/get-or-generate-thumbnail ..store.. ..params..) => ..thumb..
         (create-image-response ..thumb.. ..params..) => ..response..)

       (handle-thumbnail ..store.. ..params.. ..request..) => ..error..
       (provided
         (u/get-or-generate-thumbnail ..store.. ..params..) => nil
         (error-response 404 ..params..) => ..error..))

(facts :handle-original
       (handle-original ..store.. ..params.. ..request..) => ..response..
       (provided
         (sp/get-original ..store.. ..params..) => ..original..
         (create-image-response ..original.. ..params..) => ..response..)

       (handle-original ..store.. ..params.. ..request..) => ..error..
       (provided
         (sp/get-original ..store.. ..params..) => nil
         (error-response 404 ..params..) => ..error..))

(facts :route-params->image-type
       (route-params->image-type {:image-type ""}) => "images"
       (route-params->image-type {:image-type "/images"}) => "images"
       (route-params->image-type {:image-type "/avatars"}) => "avatars")

(facts :autodetect-request-format
       (autodetect-request-format no-webp-support-request {}) => {}
       (autodetect-request-format no-webp-support-request {:format "png"}) => {:format "png"}
       (autodetect-request-format webp-support-request {}) => {:format "webp"}
       (autodetect-request-format webp-support-request {:format "jpeg"}) => {:format "jpeg"})

(facts :route->webp-request-format
       (route->webp-request-format {:options {}} webp-support-request) => {:options {:format "webp"}, :requested-format nil}
       (route->webp-request-format {:options {:format "jpg"}} webp-support-request) => {:options {:format "jpg"}, :requested-format "jpg"}
       (route->webp-request-format {:options {:format "jpg"}} no-webp-support-request) => {:options {:format "jpg"}, :requested-format "jpg"}
       (route->webp-request-format {:options {}} no-webp-support-request) => {:options {}, :requested-format nil})
