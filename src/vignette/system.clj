(ns vignette.system
  (:require [environ.core :refer [env]]
            [org.httpkit.server :refer :all]
            [vignette.http.routes :refer [app-routes]]
            [vignette.protocols :refer :all]
            [vignette.server :as s]))

(def default-threads 4)
(def default-queue-size 20000)

(defrecord VignetteSystem [state]
  SystemAPI
  (store [this]
    (:store (:state this)))
  (start [this port]
    (swap! (:running (:state this))
           (fn [_]
             (run-server
               (#'app-routes this)
               {:port port
                :thread (Integer. (env :vignette-server-threads default-threads))
                :queue-size (Integer. (env :vignette-server-queue-size default-queue-size))}))))
  (stop [this]
    (when-let [stop-fn @(:running (:state this))]
      (stop-fn))))

(defn create-system
  [store]
  (->VignetteSystem {:store store :running (atom nil)}))
