(ns vignette.http.jetty
  (:require [environ.core :refer [env]])
  (:import [org.eclipse.jetty.server.handler HandlerCollection RequestLogHandler]
           [org.eclipse.jetty.server NCSARequestLog Handler]))


(def default-access-log "/tmp/vignette-access.log")

(defn ncsa-request-log
  [server]
  (if (env :enable-access-log nil)
    (let [handlers (HandlerCollection.)
          request-log-handler (RequestLogHandler.)
          request-log (doto (NCSARequestLog. (env :access-log-file default-access-log))
                        (.setRetainDays 90)
                        (.setAppend true)
                        (.setExtended false)
                        (.setLogLatency true)
                        (.setLogTimeZone "GMT"))]
      (.setHandlers handlers (into-array Handler (conj (into [] (.getHandlers server))
                                                       request-log-handler)))
      (.setRequestLog request-log-handler request-log)
      (.setHandler server handlers))
    server))

(defn configure-jetty
  [server]
  (-> server
      (ncsa-request-log)))
