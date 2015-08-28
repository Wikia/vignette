(ns vignette.http.route-helpers-test
  (:require [clojure.java.io :as io]
            [clout.core :refer (route-compile route-matches)]
            [midje.sweet :refer :all]
            [vignette.http.route-helpers :refer :all]
            [vignette.protocols :refer :all]
            [vignette.util.image-response :refer :all]
            [vignette.storage.protocols :as sp]
            [vignette.util.image-response :as ir]
            [vignette.util.thumbnail :as u])
  (:import java.io.FileNotFoundException))

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
