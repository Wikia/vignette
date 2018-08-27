(ns vignette.perfmonitoring.core
  (:require [vignette.common.logger :as log]
            [environ.core :refer [env]]
            [prometheus.core :as prometheus]))

(def app (.toLowerCase (env :perfmonitoring-app "vignette")))

(defonce metrics-registry (atom nil))

; prometheus accepts only underscores
(defn normalize [counter]
  (clojure.string/replace (name counter) "-" "_"))

(defn register [store]
  (->
   store
   (prometheus/register-counter app (normalize :convert-error-total) "Conversion error" [])
   (prometheus/register-counter app (normalize :exception-count-total) "Total exception occured" [])
   (prometheus/register-counter app (normalize :request-count-total) "Total requests number" [])
   (prometheus/register-counter app (normalize :bad-request-path-count-total) "Total bad path requests" [])
   (prometheus/register-counter app (normalize :thumbnail-cache-hit-total) "Cache hit counter" [])
   (prometheus/register-counter app (normalize :generate-thumbnail-total) "Thumbnail generation counter" [])
   (prometheus/register-counter app (normalize :connection-pool-timeout-total) "Timeout counter" [])
   (prometheus/register-histogram app (normalize :request-time-seconds) "Requests time histogram" [] [0.001, 0.005, 0.01, 0.02, 0.05, 0.1, 0.2, 0.3, 0.5, 0.75, 1.0, 2.0, 3.0, 5.0])
   (prometheus/register-histogram app (normalize :imagemagick-seconds) "Imagemagick time histogram" [] [0.001, 0.005, 0.01, 0.02, 0.05, 0.1, 0.2, 0.3, 0.5, 0.75, 1.0, 2.0, 3.0, 5.0])
   (prometheus/register-histogram app (normalize :s3-get-seconds) "s3 get image time histogram" [] [0.001, 0.005, 0.01, 0.02, 0.05, 0.1, 0.2, 0.3, 0.5, 0.75, 1.0, 2.0, 3.0, 5.0])
   (prometheus/register-histogram app (normalize :s3-put-seconds) "s3 put image time histogram" [] [0.001, 0.005, 0.01, 0.02, 0.05, 0.1, 0.2, 0.3, 0.5, 0.75, 1.0, 2.0, 3.0, 5.0])
   (prometheus/register-histogram app (normalize :s3-delete-seconds) "s3 delete image time histogram" [] [0.001, 0.005, 0.01, 0.02, 0.05, 0.1, 0.2, 0.3, 0.5, 0.75, 1.0, 2.0, 3.0, 5.0])
   (prometheus/register-histogram app (normalize :s3-bucket-exists-seconds) "s3 check bucket image time histogram" [] [0.001, 0.005, 0.01, 0.02, 0.05, 0.1, 0.2, 0.3, 0.5, 0.75, 1.0, 2.0, 3.0, 5.0])
   (prometheus/register-histogram app (normalize :s3-bucket-create-seconds) "s3 create bucket image time histogram" [] [0.001, 0.005, 0.01, 0.02, 0.05, 0.1, 0.2, 0.3, 0.5, 0.75, 1.0, 2.0, 3.0, 5.0])
   (prometheus/register-histogram app (normalize :s3-list-objects-seconds) "s3 list objects time histogram" [] [0.001, 0.005, 0.01, 0.02, 0.05, 0.1, 0.2, 0.3, 0.5, 0.75, 1.0, 2.0, 3.0, 5.0])
  ))

(defn init []
  (->> (prometheus/init-defaults)
       (register)
       (reset! metrics-registry)))

(defn publish
  [points]
  (doseq [[counter inc] points]
    (try
      (prometheus/increase-counter @metrics-registry app (normalize counter) [] inc)
      (catch Exception e
        (log/warn "prometheus publish failed" {:exception (str e) :counter counter})))))

(defn current-time [] (System/currentTimeMillis))

(defmacro timing [metric & body]
  `(let [start# (current-time)]
    (try
      ~@body
      (finally
          (try
            (prometheus/track-observation @metrics-registry app (normalize ~metric) (/ (- (current-time) start#) 1000.0) [])
            (catch Exception e#
              (log/warn "prometheus timer failed" {:exception (str e#) :counter ~metric})))))))
