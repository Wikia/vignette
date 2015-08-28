(ns vignette.protocols)

(defprotocol SystemAPI
  (stores [this])
  (start [this port])
  (stop [this]))
