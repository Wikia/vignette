(ns vignette.http.middleware
  (:require [environ.core :refer [env]]
            [compojure.response :refer [render]]
            [clojure.string :as string]
            [ring.util.response :refer [response status charset header get-header]]
            [slingshot.slingshot :refer [try+ throw+]]
            [vignette.util.image-response :refer :all]
            [vignette.common.logger :as log]
            [vignette.perfmonitoring.core :as perf])
  (:import [java.net InetAddress]))

(def hostname (.getHostName (InetAddress/getLocalHost)))
(def cache-control-header "Cache-Control")

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
          (when (>= response-code 500)
            (perf/publish {:convert-error-total 1})
            (println message ":" (:uri request))
            (log/warn message
                      (merge {:path (:uri request)
                              :query (:query-string request)
                              :response-code response-code}
                             context)))
          (error-response response-code thumb-map)))
      (catch Exception e
        (println (.getMessage e) ":" (:uri request))
        (println (.printStackTrace e))
        (perf/publish {:exception-count-total 1})
        (log/warn (str e) {:path (:uri request)
                           :query (:query-string request)})
        (error-response 500)))))

(declare add-cache-control-header
         hours-to-seconds)

(defn log-path
  [handler]
  (fn [request]
    (log/info (str "path-requested" (:uri request)))
    (handler request)
  )
)

(defn add-meta
  [handler]
  (fn [request]
    (let [response (handler request)
          meta (meta response)]
      (with-meta response {:path (get meta :path (get meta :thumbnail-mode (name (get meta :request-type "unknown"))))}))))

(defn add-headers
  [handler]
  (fn [request]
    (let [response (handler request)]
      (-> response
          (add-cache-control-header)
          (header "Varnish-Logs" "vignette")
          (header "X-Served-By" hostname)
          (header "X-Cache" "ORIGIN")
          (header "X-Cache-Hits" "ORIGIN")
          (header "Connection" "close")
          (header "Access-Control-Allow-Origin" "*")))))

(defn add-cache-control-header
  [response]
  (let [status (get response :status 0)]
    (cond
      (and (>= status 200) (< status 300))
      (header response cache-control-header (format "public, max-age=%d",
                                                    (hours-to-seconds (* 365 24))))

      (and (>= status 400) (< status 500))
      (header response cache-control-header (format "public, max-age=%d"
                                                    (hours-to-seconds 1)))

      :else (header response cache-control-header (format "public, max-age=%d"
                                                    (/ (hours-to-seconds 1) 2))))))
(defn uri-multiple-slash-replacement [uri]
  (string/replace uri #"(\/{2,})" "/"))

(defn multiple-slash->single-slash [handler]
  (fn [request]
    (let [uri (uri-multiple-slash-replacement (:uri request))]
      (handler (assoc request :uri uri)))))

(defn hours-to-seconds
  [hours]
  (* 60 60 hours))

(defn request-timer
  [handler]
  (fn [request]
    (perf/publish {:request-count-total 1})
    (perf/timing :request-time-seconds (handler request))))

(defn bad-request-path
  []
  (fn [request]
    (perf/publish {:bad-request-path-count-total 1})
    (log/warn "bad-request-path" (cond-> {:path (:uri request)}
                                         (get-header request "referer") (assoc :referer (get-header request "referer"))))
    (with-meta (-> (render "Unrecognized request path!\nSee https://github.com/Wikia/vignette for documentation.\n" request)
                   (status 404))
     {:path "unrecognized-path"})))
