(ns vignette.perfmonitoring.core
  (:import (java.net DatagramSocket DatagramPacket InetAddress))
  (:require [cheshire.core :as json]
            [vignette.common.logger :as log]
            [environ.core :refer [env]]
            [prometheus.core :as prometheus]))

(def app (.toLowerCase (env :perfmonitoring-app "vignette")))

(defonce metrics-registry (atom nil))

(defn unify [counter]
  (clojure.string/replace (name counter) "-" "_"))

(defn register [store]
  (->
   store
   (prometheus/register-counter app (unify :convert-error) "Conversion error" [])
   (prometheus/register-counter app (unify :exception-count) "Total exception occured" [])
   (prometheus/register-counter app (unify :request-count) "Total requests number" [])
   (prometheus/register-counter app (unify :bad-request-path-count) "Total bad path requests" [])
   (prometheus/register-counter app (unify :thumbnail-cache-hit) "Cache hit counter" [])
   (prometheus/register-counter app (unify :generate-thumbnail) "Thumbnail generation counter" [])
   (prometheus/register-counter app (unify :connection-pool-timeout) "Timeout counter" [])
   (prometheus/register-histogram app (unify :request-time) "Requests time histogram in ms" [] [1.0, 2.0, 5.0, 10.0, 25.0, 50.0, 75.0, 100.0, 200.0, 400.0, 600.0, 800.0, 1000.0, 1500.0, 2000.0, 3000.0, 5000.0])
   (prometheus/register-histogram app (unify :imagemagick) "Imagemagick time histogram in ms" [] [1.0, 2.0, 5.0, 10.0, 25.0, 50.0, 75.0, 100.0, 200.0, 400.0, 600.0, 800.0, 1000.0, 1500.0, 2000.0, 3000.0, 5000.0])
   (prometheus/register-histogram app (unify :s3-get) "s3 get image time histogram in ms" [] [1.0, 2.0, 5.0, 10.0, 25.0, 50.0, 75.0, 100.0, 200.0, 400.0, 600.0, 800.0, 1000.0, 1500.0, 2000.0, 3000.0, 5000.0])
   (prometheus/register-histogram app (unify :s3-put) "s3 put image time histogram in ms" [] [1.0, 2.0, 5.0, 10.0, 25.0, 50.0, 75.0, 100.0, 200.0, 400.0, 600.0, 800.0, 1000.0, 1500.0, 2000.0, 3000.0, 5000.0])
   (prometheus/register-histogram app (unify :s3-delete) "s3 delete image time histogram in ms" [] [1.0, 2.0, 5.0, 10.0, 25.0, 50.0, 75.0, 100.0, 200.0, 400.0, 600.0, 800.0, 1000.0, 1500.0, 2000.0, 3000.0, 5000.0])
   (prometheus/register-histogram app (unify :s3-bucket-exists) "s3 check bucket image time histogram in ms" [] [1.0, 2.0, 5.0, 10.0, 25.0, 50.0, 75.0, 100.0, 200.0, 400.0, 600.0, 800.0, 1000.0, 1500.0, 2000.0, 3000.0, 5000.0])
   (prometheus/register-histogram app (unify :s3-bucket-create) "s3 create bucket image time histogram in ms" [] [1.0, 2.0, 5.0, 10.0, 25.0, 50.0, 75.0, 100.0, 200.0, 400.0, 600.0, 800.0, 1000.0, 1500.0, 2000.0, 3000.0, 5000.0])
   (prometheus/register-histogram app (unify :s3-list-objects) "s3 list objects time histogram in ms" [] [1.0, 2.0, 5.0, 10.0, 25.0, 50.0, 75.0, 100.0, 200.0, 400.0, 600.0, 800.0, 1000.0, 1500.0, 2000.0, 3000.0, 5000.0])
  ))

(defn init []
  (->> (prometheus/init-defaults)
       (register)
       (reset! metrics-registry)))

(defn publish
  [points]
  (doseq [[counter inc] points]
    (try
      (prometheus/increase-counter @metrics-registry app (unify counter) [] inc)
      (catch Exception e
        (log/warn "prometheus publish failed" {:exception (str e) :counter counter})))))

(defn current-time [] (System/currentTimeMillis))

(defmacro timing [metric & body]
  `(let [start# (current-time)]
    (try
      ~@body
      (finally
          (try
            (prometheus/track-observation @metrics-registry app (unify ~metric) (- (current-time) start#) [])
            (catch Exception e#
              (log/warn "prometheus timer failed" {:exception (str e#) :counter ~metric})))))))
