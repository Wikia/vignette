(ns vignette.system
  (:require [environ.core :refer [env]]
            [ring.adapter.jetty9 :as jetty]
            [vignette.http.routes :refer [app-routes]]
            [vignette.http.jetty :refer [configure-jetty]]
            [vignette.protocols :refer :all])
  (:import [java.util.concurrent ArrayBlockingQueue]))

(def default-min-threads 50)
(def default-max-threads 150)
(def default-queue-size 9000)

(defrecord VignetteSystem [state]
  SystemAPI
  (store [this]
    (:store (:state this)))
  (cache [this]
    (:cache (:state this)))
  (start [this port]
    (swap! (:running (:state this))
           (fn [_]
             (jetty/run-jetty
               (app-routes this)
               {:port port
                :configurator configure-jetty
                :join? false
                ; FIXME: update the readme
                :min-therads (Integer. (env :vignette-server-min-threads default-min-threads))
                :max-threads (Integer. (env :vignette-server-max-threads default-max-threads))
                ; see https://wiki.eclipse.org/Jetty/Howto/High_Load#Thread_Pool
                :job-queue (ArrayBlockingQueue.
                             (Integer. (env :vignette-server-queue-size default-queue-size)))}))))
  (stop [this]
    (when-let [server @(:running (:state this))]
      (.stop server))))

(defn create-system
  ([store cache]
   (->VignetteSystem {:store store :cache cache :running (atom nil)}))
  ([store]
   (create-system store nil)))
