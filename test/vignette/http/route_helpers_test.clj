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

(facts :route-params->image-type
       (route-params->image-type {:image-type ""}) => "images"
       (route-params->image-type {:image-type "/images"}) => "images"
       (route-params->image-type {:image-type "/avatars"}) => "avatars")
