(ns vignette.system
  (:require [environ.core :refer [env]]
            [qbits.jet.server :as jet]
            [vignette.http.routes :refer [all-routes]]
            [vignette.http.jetty :refer [configure-jetty]]
            [vignette.protocols :refer :all]
            [vignette.util.consul :refer []]
            [ring.middleware.reload :refer [wrap-reload]])
  (:import [java.util.concurrent ArrayBlockingQueue]))

(def default-max-threads 150)
(def default-queue-size 9000)

(defrecord VignetteSystem [state]
  SystemAPI
  (store [this]
    (:store (:state this)))
  (start [this port]
    (swap! (:running (:state this))
           (fn [_]
             (jet/run-jetty
               {
                :ring-handler (wrap-reload (all-routes this))
                :port port
                :configurator configure-jetty
                :join? false
                ; FIXME: update the readme
                :max-threads (Integer. (env :vignette-server-max-threads default-max-threads))
                ; see https://wiki.eclipse.org/Jetty/Howto/High_Load#Thread_Pool
                :job-queue (ArrayBlockingQueue.
                             (Integer. (env :vignette-server-queue-size default-queue-size)))}))))
  (stop [this]
    (when-let [server @(:running (:state this))]
      (.stop server))))

(defn create-system
  [store & [consul]]
  (->VignetteSystem {:store store :running (atom nil)}) )
