(ns vignette.http.legacy.integration-test
  (:require [clout.core :as c]
            [midje.sweet :refer :all]
            [ring.mock.request :refer :all]
            [digest :as digest]
            [clj-http.client :as client]
            [vignette.system :refer :all]
            [vignette.protocols :refer :all]
            [vignette.test.helper :refer :all]
            [vignette.storage.local :refer [create-local-storage-system]]
            [vignette.storage.core :refer [create-image-storage]]
            [vignette.util.integration :refer [create-integration-env integration-path]]
            [vignette.http.legacy.routes :as hlr]))


(create-integration-env)

(def default-port 8888)
(def los (create-local-storage-system integration-path))
(def lis (create-image-storage los))
(def system-local (create-system {:wikia-store lis}))

; parses legacy request log, spitting out URLs we don't understand
(defn parse-request-log
  [request-log]
  (with-open [reader (clojure.java.io/reader request-log)]
    (loop [lines (line-seq reader)
           parse-failures []
           total-lines 0]
      (if-let [line (first lines)]
        (if (or (and (re-find #"/thumb/" line) (c/route-matches hlr/thumbnail-route (request :get line)))
                (c/route-matches hlr/original-route (request :get line)))
          (recur (rest lines) parse-failures (inc total-lines))
          (recur (rest lines) (conj parse-failures line) (inc total-lines)))
        (/ (count parse-failures) total-lines)))))

(defn- less-than
  [max]
  (fn [actual]
    (< actual max)))

(facts :legacy-requests :integration
       (parse-request-log "legacy/request.log") => (less-than 0.01))

(with-state-changes [(before :facts (start system-local default-port))
                     (after :facts (stop system-local))]

  (facts :legacy-requests :thumbnail-integration
    (let [response (client/get (format "http://localhost:%d/bucket/images/thumb/a/ab/beach.jpg/200px-beach.jpg" default-port) {:as :byte-array})]
      (:status response) => 200
      (get (:headers response) "Surrogate-Key") => "6f13d7df6b332e4945d90bd6785226b535f8b248"
      (Integer/parseInt (get (:headers response) "Content-Length")) => (roughly 16341 20)
      (get (:headers response) "Connection") => "close"
      (get (:headers response) "Content-Type") => "image/jpeg"
      (vec (:body response)) => (has-prefix jpeg-header)
      (get (:headers response) "Cache-Control") => "public, max-age=31536000")))
      ;(digest/sha1 (:body response)) => "ffd6e8e3b5fc7eb3100857f273d6d1e6e19df51c")))
