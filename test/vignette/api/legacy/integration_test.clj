(ns vignette.api.legacy.integration-test
  (:require [clout.core :as c]
            [midje.sweet :refer :all]
            [ring.mock.request :refer :all]
            [vignette.api.legacy.routes :as alr]))

; parses legacy request log, spitting out URLs we don't understand
(defn parse-request-log
  [request-log]
  (with-open [reader (clojure.java.io/reader request-log)]
    (loop [lines (line-seq reader)
           parse-failures []
           total-lines 0]
      (if-let [line (first lines)]
        (if (or (and (re-find #"/thumb/" line) (c/route-matches alr/thumbnail-route (request :get line)))
                (c/route-matches alr/original-route (request :get line)))
          (recur (rest lines) parse-failures (inc total-lines))
          (recur (rest lines) (conj parse-failures line) (inc total-lines)))
        (/ (count parse-failures) total-lines)))))

(defn- less-than
  [max]
  (fn [actual]
    (< actual max)))

(facts :legacy-requests
       (parse-request-log "legacy/request.log") => (less-than 0.01))
