(ns vignette.http.integration-gif-test
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
                   (let [response (client/get (format "http://localhost:%d/bucket/a/ab/chuck-large-animated.gif/revision/latest" default-port) {:as :byte-array, :accept "image/webp"})]
                     (:status response) => 200
                     (get (:headers response) "Surrogate-Key") => "936b19e79decb9240a0b65ff437ae5ea033c0f69"
                     (get (:headers response) "Content-Disposition") => "inline; filename=\"chuck-large-animated.webp\"; filename*=UTF-8''chuck-large-animated.webp"
                     (Integer/parseInt (get (:headers response) "Content-Length")) => (roughly 1405790 50)
                     (get (:headers response) "Connection") => "close"
                     (get (:headers response) "Cache-Control") => "public, max-age=31536000"
                     (get (:headers response) "Content-Type") => "image/webp"
                     (vec (:body response)) => (has-prefix riff-header)
                     (vec (:body response)) => (contains webp-header))

         (let [response (client/get (format "http://localhost:%d/bucket/a/ab/chuck-large-animated.gif/revision/latest/scale-to-width/200" default-port) {:as :byte-array, :accept "image/webp"})]
           (:status response) => 200
           (get (:headers response) "Surrogate-Key") => "936b19e79decb9240a0b65ff437ae5ea033c0f69"
           (get (:headers response) "Content-Disposition") => "inline; filename=\"chuck-large-animated.webp\"; filename*=UTF-8''chuck-large-animated.webp"
           (Integer/parseInt (get (:headers response) "Content-Length")) => (roughly 183990 50)
           (get (:headers response) "Connection") => "close"
           (get (:headers response) "Cache-Control") => "public, max-age=31536000"
           (get (:headers response) "Content-Type") => "image/webp"
           (vec (:body response)) => (has-prefix riff-header)
           (vec (:body response)) => (contains webp-header))

         (let [response (client/get (format "http://localhost:%d/bucket/a/ab/chuck-large-animated.gif/revision/latest/scale-to-width/200" default-port) {:as :byte-array})]
           (:status response) => 200
           (get (:headers response) "Surrogate-Key") => "936b19e79decb9240a0b65ff437ae5ea033c0f69"
           (get (:headers response) "Content-Disposition") => "inline; filename=\"chuck-large-animated.gif\"; filename*=UTF-8''chuck-large-animated.gif"
           (Integer/parseInt (get (:headers response) "Content-Length")) => (roughly 519260 50)
           (get (:headers response) "Connection") => "close"
           (get (:headers response) "Cache-Control") => "public, max-age=31536000"
           (get (:headers response) "Content-Type") => "image/gif"
           (vec (:body response)) => (contains gif-header))))
