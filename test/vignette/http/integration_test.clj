(ns vignette.http.integration-test
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

  (facts :requests :thumbnail-integration :scale-to-width
    (let [response (client/get (format "http://localhost:%d/bucket/a/ab/boat.jpg/revision/latest/scale-to-width/200" default-port) {:as :byte-array})]
      (:status response) => 200
      (get (:headers response) "Surrogate-Key") => "6991f130a508cf3d03f8f097c32b0ff11beb5b77"
      (get (:headers response) "Content-Disposition") => "inline; filename=\"boat.jpg\"; filename*=UTF-8''boat.jpg"
      (Integer/parseInt (get (:headers response) "Content-Length")) => (roughly 8950 50)
      (get (:headers response) "Connection") => "close"
      (get (:headers response) "Content-Type") => "image/jpeg"
      (vec (:body response)) => (has-prefix jpeg-header)
      (get (:headers response) "Cache-Control") => "public, max-age=31536000")
      ;(digest/sha1 (:body response)) => "8cda222a0fe951145839412322810ce3c946d880")

    (let [response (client/head (format "http://localhost:%d/bucket/a/ab/boat.jpg/revision/latest/scale-to-width/200" default-port) {:as :byte-array})]
      (:status response) => 200
      (get (:headers response) "Surrogate-Key") => "6991f130a508cf3d03f8f097c32b0ff11beb5b77"
      (get (:headers response) "Content-Disposition") => "inline; filename=\"boat.jpg\"; filename*=UTF-8''boat.jpg"
      (get (:headers response) "Content-Length") => nil?
      (get (:headers response) "Connection") => "close"
      (get (:headers response) "Cache-Control") => "public, max-age=31536000"
      (:body response) => nil?))

  (facts :requests :thumbnail-integration :window-crop
    (let [response (client/get (format "http://localhost:%d/bucket/a/ab/carousel.jpg/revision/latest/window-crop/width/200/x-offset/690/y-offset/250/window-width/1600/window-height/1900" default-port) {:as :byte-array})]
      (:status response) => 200
      (get (:headers response) "Surrogate-Key") => "e0b7c4db3fb0453950367c1703710925b649babb"
      (get (:headers response) "Content-Disposition") => "inline; filename=\"carousel.jpg\"; filename*=UTF-8''carousel.jpg"
      (Integer/parseInt (get (:headers response) "Content-Length")) => (roughly 23175 50)
      (get (:headers response) "Connection") => "close"
      (get (:headers response) "Content-Type") => "image/jpeg"
      (vec (:body response)) => (has-prefix jpeg-header)
      (get (:headers response) "Cache-Control") => "public, max-age=31536000")
      ;(digest/sha1 (:body response)) => "2c44b2eace007623148ee8a33d0f66f4ea1b0175")

    (let [response (client/head (format "http://localhost:%d/bucket/a/ab/carousel.jpg/revision/latest/window-crop/width/200/x-offset/690/y-offset/250/window-width/1600/window-height/1900" default-port) {:as :byte-array})]
      (:status response) => 200
      (get (:headers response) "Surrogate-Key") => "e0b7c4db3fb0453950367c1703710925b649babb"
      (get (:headers response) "Content-Disposition") => "inline; filename=\"carousel.jpg\"; filename*=UTF-8''carousel.jpg"
      (get (:headers response) "Content-Length") => nil?
      (get (:headers response) "Connection") => "close"
      (get (:headers response) "Cache-Control") => "public, max-age=31536000"
      (:body response) => nil?))

  (facts :requests :thumbnail-integration :window-crop-fixed
    (let [response (client/get (format "http://localhost:%d/bucket/a/ab/beach.jpg/revision/latest/window-crop-fixed/width/200/height/200/x-offset/60/y-offset/550/window-width/200/window-height/260?fill=blue" default-port) {:as :byte-array})]
      (:status response) => 200
      (get (:headers response) "Surrogate-Key") => "6f13d7df6b332e4945d90bd6785226b535f8b248"
      (get (:headers response) "Content-Disposition") => "inline; filename=\"beach.jpg\"; filename*=UTF-8''beach.jpg"
      (Integer/parseInt (get (:headers response) "Content-Length")) => (roughly 9600 50)
      (get (:headers response) "Connection") => "close"
      (get (:headers response) "Content-Type") => "image/jpeg"
      (vec (:body response)) => (has-prefix jpeg-header)
      (get (:headers response) "Cache-Control") => "public, max-age=31536000")
      ;(digest/sha1 (:body response)) => "3f00690095caced27fdf2957f6b58929228d1326")

    (let [response (client/head (format "http://localhost:%d/bucket/a/ab/beach.jpg/revision/latest/window-crop-fixed/width/200/height/200/x-offset/60/y-offset/550/window-width/200/window-height/260?fill=blue" default-port) {:as :byte-array})]
      (:status response) => 200
      (get (:headers response) "Surrogate-Key") => "6f13d7df6b332e4945d90bd6785226b535f8b248"
      (get (:headers response) "Content-Disposition") => "inline; filename=\"beach.jpg\"; filename*=UTF-8''beach.jpg"
      (get (:headers response) "Content-Length") => nil?
      (get (:headers response) "Connection") => "close"
      (get (:headers response) "Cache-Control") => "public, max-age=31536000"
      (:body response) => nil?))

  (facts :requests :thumbnail-integration :fixed-aspect-ratio :thumbnail
    (let [response (client/get (format "http://localhost:%d/bucket/a/ab/beach.jpg/revision/latest/fixed-aspect-ratio/width/200/height/200?fill=blue" default-port) {:as :byte-array})]
      (:status response) => 200
      (get (:headers response) "Surrogate-Key") => "6f13d7df6b332e4945d90bd6785226b535f8b248"
      (Integer/parseInt (get (:headers response) "Content-Length")) => (roughly 9450 50)
      (get (:headers response) "Connection") => "close"
      (get (:headers response) "Content-Type") => "image/jpeg"
      (get (:headers response) "Cache-Control") => "public, max-age=31536000")
      ;(digest/sha1 (:body response)) => "bc34c07703035ae131aeb5615b45ea3eae7b82ba")

    (let [response (client/head (format "http://localhost:%d/bucket/a/ab/beach.jpg/revision/latest/fixed-aspect-ratio/width/200/height/200?fill=blue" default-port) {:as :byte-array})]
      (:status response) => 200
      (get (:headers response) "Surrogate-Key") => "6f13d7df6b332e4945d90bd6785226b535f8b248"
      (get (:headers response) "Content-Length") => nil?
      (get (:headers response) "Connection") => "close"
      (get (:headers response) "Cache-Control") => "public, max-age=31536000"
      (:body response) => nil?))

  (facts :requests :thumbnail-integration :original
    (let [response (client/get (format "http://localhost:%d/bucket/a/ab/beach.jpg/revision/latest" default-port) {:as :byte-array})]
      (:status response) => 200
      (get (:headers response) "Surrogate-Key") => "6f13d7df6b332e4945d90bd6785226b535f8b248"
      (get (:headers response) "Content-Disposition") => "inline; filename=\"beach.jpg\"; filename*=UTF-8''beach.jpg"
      (Integer/parseInt (get (:headers response) "Content-Length")) => (roughly 189612 50)
      (get (:headers response) "Connection") => "close"
      (get (:headers response) "Content-Type") => "image/jpeg"
      (vec (:body response)) => (has-prefix jpeg-header)
      (get (:headers response) "Cache-Control") => "public, max-age=31536000")
      ;(digest/sha1 (:body response)) => "a8969142a23801ee3b2d28896f41e9339e1294e6")

    (let [response (client/head (format "http://localhost:%d/bucket/a/ab/beach.jpg/revision/latest" default-port) {:as :byte-array})]
      (:status response) => 200
      (get (:headers response) "Surrogate-Key") => "6f13d7df6b332e4945d90bd6785226b535f8b248"
      (get (:headers response) "Content-Disposition") => "inline; filename=\"beach.jpg\"; filename*=UTF-8''beach.jpg"
      (get (:headers response) "Content-Length") => nil?
      (get (:headers response) "Connection") => "close"
      (get (:headers response) "Cache-Control") => "public, max-age=31536000"
      (:body response) => nil?)

    (let [response (client/get (format "http://localhost:%d/bucket/a/ab/beach.jpg/revision/latest" default-port) {:as :byte-array, :accept "image/webp"})]
      (:status response) => 200
      (get (:headers response) "Surrogate-Key") => "6f13d7df6b332e4945d90bd6785226b535f8b248"
      (get (:headers response) "Content-Disposition") => "inline; filename=\"beach.jpg.webp\"; filename*=UTF-8''beach.jpg.webp"
      ;(Integer/parseInt (get (:headers response) "Content-Length")) => (roughly 162360 50)
      (get (:headers response) "Connection") => "close"
      (get (:headers response) "Content-Type") => "image/webp"
      (vec (:body response)) => (has-prefix riff-header)
      (vec (:body response)) => (contains webp-header)
      (get (:headers response) "Cache-Control") => "public, max-age=31536000")

    (let [response (client/get (format "http://localhost:%d/bucket/a/ab/baboon.png/revision/latest" default-port) {:as :byte-array, :accept "image/webp"})]
      (:status response) => 200
      (get (:headers response) "Surrogate-Key") => "e77cd6979116303a6d50610962fa9790469574c2"
      (get (:headers response) "Content-Disposition") => "inline; filename=\"baboon.png.webp\"; filename*=UTF-8''baboon.png.webp"
      ;(Integer/parseInt (get (:headers response) "Content-Length")) => (roughly 170350 50)
      (get (:headers response) "Connection") => "close"
      (get (:headers response) "Content-Type") => "image/webp"
      (vec (:body response)) => (has-prefix riff-header)
      (vec (:body response)) => (contains webp-header)
      (get (:headers response) "Cache-Control") => "public, max-age=31536000")

    (let [response (client/get (format "http://localhost:%d/bucket/a/ab/baboon.png/revision/latest" default-port) {:as :byte-array})]
      (:status response) => 200
      (get (:headers response) "Surrogate-Key") => "e77cd6979116303a6d50610962fa9790469574c2"
      (get (:headers response) "Content-Disposition") => "inline; filename=\"baboon.png\"; filename*=UTF-8''baboon.png"
      (Integer/parseInt (get (:headers response) "Content-Length")) => (roughly 126370 50)
      (get (:headers response) "Connection") => "close"
      (get (:headers response) "Content-Type") => "image/png"
      (vec (:body response)) => (contains png-header)
      (get (:headers response) "Cache-Control") => "public, max-age=31536000")

    (let [response (client/get (format "http://localhost:%d/bucket/a/ab/baboon.png/revision/latest/scale-to-width/200" default-port) {:as :byte-array, :accept "image/webp"})]
      (:status response) => 200
      (get (:headers response) "Surrogate-Key") => "e77cd6979116303a6d50610962fa9790469574c2"
      (get (:headers response) "Content-Disposition") => "inline; filename=\"baboon.png.webp\"; filename*=UTF-8''baboon.png.webp"
      (Integer/parseInt (get (:headers response) "Content-Length")) => (roughly 10030 50)
      (get (:headers response) "Connection") => "close"
      (get (:headers response) "Cache-Control") => "public, max-age=31536000"
      (get (:headers response) "Content-Type") => "image/webp"
      (vec (:body response)) => (has-prefix riff-header)
      (vec (:body response)) => (contains webp-header))

    (let [response (client/get (format "http://localhost:%d/bucket/a/ab/beach.jpg/revision/latest/scale-to-width/200" default-port) {:as :byte-array, :accept "image/webp"})]
      (:status response) => 200
      (get (:headers response) "Surrogate-Key") => "6f13d7df6b332e4945d90bd6785226b535f8b248"
      (get (:headers response) "Content-Disposition") => "inline; filename=\"beach.jpg.webp\"; filename*=UTF-8''beach.jpg.webp"
      (Integer/parseInt (get (:headers response) "Content-Length")) => (roughly 11598 50)
      (get (:headers response) "Connection") => "close"
      (get (:headers response) "Cache-Control") => "public, max-age=31536000"
      (get (:headers response) "Content-Type") => "image/webp"
      (vec (:body response)) => (has-prefix riff-header)
      (vec (:body response)) => (contains webp-header))))
      ;(digest/sha1 (:body response)) => "a8969142a23801ee3b2d28896f41e9339e1294e6")))
