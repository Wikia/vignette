(ns vignette.system
  (:require [environ.core :refer (env)]
            (vignette [server :as s]
                      [protocols :refer :all])))


(defrecord VignetteSystem [state]
  SystemAPI
  (store [this]
    (:store (:state this)))
  (start [this port]
    (swap! (:running (:state this))
           (fn [_]
             (s/run this port))))
  (stop [this]
    (@(:running (:state this)))))

(defn create-system
  [store]
  (->VignetteSystem {:store store :running (atom nil)}))
