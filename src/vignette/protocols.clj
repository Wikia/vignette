(ns vignette.protocols)

(defprotocol SystemAPI
  (store [this])
  (start [this port])
  (stop [this]))
