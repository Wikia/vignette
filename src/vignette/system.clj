(ns vignette.system
  (:require [environ.core :refer (env)]))

(defprotocol SystemAPI
  (store [this])
  (start [this port])
  (stop [this]))

(defrecord VignetteSystem [store]
  SystemAPI
  (store [this]
    (:store this))
  (start [this port])
  (stop [this]))

(defn create-system
  [store]
  (->VignetteSystem store))
