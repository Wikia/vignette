(ns vignette.http.middleware
  (:require [clj-statsd :as statsd]
            [environ.core :refer [env]]
            [ring.util.response :refer [response status charset header]]
            [slingshot.slingshot :refer [try+ throw+]]
            [vignette.util.constants :refer :all]
            [vignette.util.image-response :refer :all]
            [wikia.common.logger :as log])
  (:import [java.net InetAddress]))

(def hostname (.getHostName (InetAddress/getLocalHost)))

(defn exception-catcher
  [handler]
  (fn [request]
    (try+
      (handler request)
      (catch [:type :convert-error] e
        (let [message (:message &throw-context)
              thumb-map (:thumb-map e) ; if present, we'll try to thumbnail the error response
              response-code (or (:response-code e) 500)
              context (assoc (dissoc e :type :thumb-map :response-code) :host hostname)]
          (log/warn message
                    (merge {:path (:uri request)
                            :query (:query-string request)}
                           context))
          (error-response response-code thumb-map)))
      (catch Exception e
        (log/warn (str e) {:path (:uri request)
                           :query (:query-string request)})
        (error-response 500)))))

(defn add-headers
  [handler]
  (fn [request]
    (let [response (handler request)]
      (-> response
          (header "Varnish-Logs" "vignette")
          (header "X-Served-By" hostname)
          (header "X-Cache" "ORIGIN")
          (header "X-Cache-Hits" "ORIGIN")
          (header "Connection" "close")))))

(defn request-timer [handler]
  (fn [request]
    (statsd/increment "vignette.request")
    (statsd/with-sampled-timing "vignette.request"
                                statsd-sample-rate
                                (handler request))))
