(ns vignette.system
  (:require [environ.core :refer (env)]
            [org.httpkit.server :refer :all]
            [vignette.http.routes :refer (app-routes)]
            (vignette [server :as s]
                      [protocols :refer :all])))


(defrecord VignetteSystem [state]
  SystemAPI
  (store [this]
    (:store (:state this)))
  (start [this port]
    (swap! (:running (:state this))
           (fn [_]
             (run-server
               (app-routes this)
               {:port port}))))
  (stop [this]
    (@(:running (:state this)))))

(defn create-system
  [store]
  (->VignetteSystem {:store store :running (atom nil)}))
