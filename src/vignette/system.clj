(ns vignette.system
  (:require [environ.core :refer [env]]
            [qbits.jet.server :as jet]
            [vignette.http.routes :refer [all-routes]]
            [vignette.http.jetty :refer [configure-jetty]]
            [vignette.protocols :refer :all]
            [ring.middleware.reload :refer [wrap-reload]])
  (:import [java.util.concurrent ArrayBlockingQueue]))

(def default-max-threads 150)
(def default-queue-size 9000)

(defrecord VignetteSystem [state]
  SystemAPI
  (stores [this]
    (-> this :state :stores))
  (start [this port]
    (swap! (:running (:state this))
           (fn [_]
             (jet/run-jetty
               {
                :ring-handler (if (boolean (Boolean/valueOf (env :reload-on-request)))
                                (do
                                  (println "Code will be reloaded on each request")
                                  (wrap-reload (all-routes (:wikia-store (stores this)) (:static-store (stores this)))))
                                (all-routes (:wikia-store (stores this)) (:static-store (stores this))))
                :port         port
                :configurator configure-jetty
                :join?        false
                ; FIXME: update the readme
                :max-threads  (Integer. (env :vignette-server-max-threads default-max-threads))
                ; see https://wiki.eclipse.org/Jetty/Howto/High_Load#Thread_Pool
                :job-queue    (ArrayBlockingQueue.
                                (Integer. (env :vignette-server-queue-size default-queue-size)))}))))
  (stop [this]
    (when-let [server @(:running (:state this))]
      (.stop server))))

(defn create-system
  [store static-image-store]
  (->VignetteSystem {:stores {:wikia-store store :static-store static-image-store} :running (atom nil)}))

(defn x [] (let [x (->VignetteSystem {})] all-routes))
