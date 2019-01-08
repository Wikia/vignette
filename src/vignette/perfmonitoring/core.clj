(ns vignette.perfmonitoring.core
  (:require [clojure.string :as string]
            [vignette.common.logger :as log]
            [environ.core :refer [env]]
            [prometheus.core :as prometheus]))

(def extended-histogram-buckets (atom [0.001, 0.005, 0.01, 0.02, 0.05, 0.1, 0.2, 0.3, 0.5, 0.75, 1.0, 2.0, 3.0, 5.0, 10.0, 20.0, 30.0]))

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
   (prometheus/register-histogram app (normalize :request-time-seconds) "Requests time histogram" [] @extended-histogram-buckets)
   (prometheus/register-histogram app (normalize :imagemagick-seconds) "Imagemagick time histogram" [] @extended-histogram-buckets)
   (prometheus/register-histogram app (normalize :s3-get-seconds) "s3 get image time histogram" [] @extended-histogram-buckets)
   (prometheus/register-histogram app (normalize :s3-put-seconds) "s3 put image time histogram" [] @extended-histogram-buckets)
   (prometheus/register-histogram app (normalize :s3-delete-seconds) "s3 delete image time histogram" [] @extended-histogram-buckets)
   (prometheus/register-histogram app (normalize :s3-bucket-exists-seconds) "s3 check bucket image time histogram" [] @extended-histogram-buckets)
   (prometheus/register-histogram app (normalize :s3-bucket-create-seconds) "s3 create bucket image time histogram" [] @extended-histogram-buckets)
   (prometheus/register-histogram app (normalize :s3-list-objects-seconds) "s3 list objects time histogram" [] @extended-histogram-buckets)
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

(defn record-request-metric [metrics-store app-name request-method response-status request-time response-path]
  (let [status-class (str (int (/ response-status 100)) "XX")
        method-label (string/upper-case (name request-method))
        labels [method-label (str response-status) status-class response-path]]
    (prometheus/track-observation metrics-store app-name "http_request_latency_seconds" request-time labels)
    (prometheus/increase-counter metrics-store app-name "http_requests_total" labels)))

(defn instrument-handler
  "Ring middleware to record request metrics"
  [handler app-name registry]
  (let [metrics-store {:registry registry}
        metrics-store (prometheus/register-counter metrics-store
                                        app-name
                                        "http_requests_total"
                                        "A counter of the total number of HTTP requests processed."
                                        ["method" "status" "statusClass" "path"])
        metrics-store (prometheus/register-histogram metrics-store
                                          app-name
                                          "http_request_latency_seconds"
                                          "A histogram of the response latency for HTTP requests in seconds."
                                          ["method" "status" "statusClass" "path"]
                                          @extended-histogram-buckets)]
    (fn [request]
      (let [request-method (:request-method request)
            start-time (System/currentTimeMillis)
            response (handler request)
            finish-time (System/currentTimeMillis)
            response-status (get response :status 404)
            response-path (get (meta response) :path "unspecified")
            request-time (/ (double (- finish-time start-time)) 1000.0)]
        (record-request-metric metrics-store app-name request-method response-status request-time response-path)
        response))))
