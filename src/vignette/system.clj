(ns vignette.system
  (:require [environ.core :refer [env]]
            [ring.adapter.jetty9 :as jetty]
            [vignette.http.routes :refer [app-routes]]
            [vignette.protocols :refer :all])
  (:import [org.eclipse.jetty.server.handler HandlerCollection ContextHandlerCollection DefaultHandler RequestLogHandler]
           [org.eclipse.jetty.server NCSARequestLog Handler]))

(def default-min-threads 50)
(def default-max-threads 150)
(def default-queue-size 20000)
(def default-access-log "/tmp/vignette-access.log")

(declare ncsa-request-log)

(defrecord VignetteSystem [state]
  SystemAPI
  (store [this]
    (:store (:state this)))
  (start [this port]
    (swap! (:running (:state this))
           (fn [_]
             (jetty/run-jetty
               (app-routes this)
               {:port port
                :configurator ncsa-request-log
                :join? false
                ; FIXME: update the readme
                :min-therads (Integer. (env :vignette-server-min-threads default-min-threads))
                :max-threads (Integer. (env :vignette-server-max-threads default-max-threads))
                :queue-size (Integer. (env :vignette-server-queue-size default-queue-size))}))))
  (stop [this]
    (when-let [server @(:running (:state this))]
      (.stop server))))

(defn create-system
  [store]
  (->VignetteSystem {:store store :running (atom nil)}))

(defn ncsa-request-log
  [server]
  (let [handlers (HandlerCollection.)
        context-handler-collection (ContextHandlerCollection.)
        request-log-handler (RequestLogHandler.)
        ; FIXME: take the log file path from the environment
        request-log (doto (NCSARequestLog. (env :vignette-access-log-file "/tmp/access.log"))
                      (.setRetainDays 90)
                      (.setAppend true)
                      (.setExtended false)
                      (.setLogLatency true)
                      (.setLogTimeZone "GMT"))]
    (.setHandlers handlers (into-array Handler (conj (into [] (.getHandlers server))
                                                     request-log-handler)))
    (.setRequestLog request-log-handler request-log)
  (.setHandler server handlers)))
