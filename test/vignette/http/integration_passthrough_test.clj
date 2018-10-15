(ns vignette.http.integration-passthrough-test
  (:require [midje.sweet :refer :all]
            [digest :as digest]
            [clj-http.client :as client]
            [vignette.system :refer :all]
            [vignette.protocols :refer :all]
            [vignette.test.helper :refer :all]
            [vignette.storage.local :refer [create-local-storage-system]]
            [vignette.storage.core :refer [create-image-storage]]
            [vignette.util.integration :refer [create-integration-env integration-path]]))
; If you get 500 errors it's probably because your environment is not setup. See the README to make sure
; the thumbnail script location is set.

(create-integration-env)

(def default-port 8888)
(def los  (create-local-storage-system integration-path))
(def lis  (create-image-storage los))
(def system-local (create-system {:wikia-store lis}))

(with-state-changes [(before :facts (start system-local default-port))
                     (after :facts (stop system-local))]

  (facts :requests :thumbnail-integration :original
     (let [response (client/get (format "http://localhost:%d/bucket/a/ab/acdc_back_in_black.ogg/revision/latest" default-port) {:as :byte-array})]
       (:status response) => 200
       (get (:headers response) "Surrogate-Key") => "6b033635239152a36eb7e802bce463f92076b01b"
       (get (:headers response) "Content-Disposition") => "inline; filename=\"acdc_back_in_black.ogg\"; filename*=UTF-8''acdc_back_in_black.ogg"
       (Integer/parseInt (get (:headers response) "Content-Length")) => 191675
       (get (:headers response) "Connection") => "close"
       (get (:headers response) "Cache-Control") => "public, max-age=31536000"
       (get (:headers response) "Content-Type") => "audio/vorbis")

     (let [response (client/get (format "http://localhost:%d/bucket/a/ab/sample_video.ogv/revision/latest" default-port) {:as :byte-array})]
       (:status response) => 200
       (get (:headers response) "Surrogate-Key") => "4735be9f0c2c8bd07885384f58d0095d69224522"
       (get (:headers response) "Content-Disposition") => "inline; filename=\"sample_video.ogv\"; filename*=UTF-8''sample_video.ogv"
       (Integer/parseInt (get (:headers response) "Content-Length")) => 3868617
       (get (:headers response) "Connection") => "close"
       (get (:headers response) "Cache-Control") => "public, max-age=31536000"
       (get (:headers response) "Content-Type") => "video/theora")

     (let [response (client/get (format "http://localhost:%d/bucket/a/ab/acdc_back_in_black.ogg/revision/latest/scale-to-width/200" default-port) {:as :byte-array})]
       (:status response) => 200
       (get (:headers response) "Surrogate-Key") => "6b033635239152a36eb7e802bce463f92076b01b"
       (get (:headers response) "Content-Disposition") => "inline; filename=\"acdc_back_in_black.ogg\"; filename*=UTF-8''acdc_back_in_black.ogg"
       (Integer/parseInt (get (:headers response) "Content-Length")) => 191675
       (get (:headers response) "Connection") => "close"
       (get (:headers response) "Cache-Control") => "public, max-age=31536000"
       (get (:headers response) "Content-Type") => "audio/vorbis")

     (let [response (client/get (format "http://localhost:%d/bucket/a/ab/sample_video.ogv/revision/latest/scale-to-width/200" default-port) {:as :byte-array})]
       (:status response) => 200
       (get (:headers response) "Surrogate-Key") => "4735be9f0c2c8bd07885384f58d0095d69224522"
       (get (:headers response) "Content-Disposition") => "inline; filename=\"sample_video.ogv\"; filename*=UTF-8''sample_video.ogv"
       (Integer/parseInt (get (:headers response) "Content-Length")) => 3868617
       (get (:headers response) "Connection") => "close"
       (get (:headers response) "Cache-Control") => "public, max-age=31536000"
       (get (:headers response) "Content-Type") => "video/theora")))
