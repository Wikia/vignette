(ns vignette.protocols)

(defprotocol SystemAPI
  (store [this])
  (cache [this])
  (start [this port])
  (stop [this]))

(defprotocol CachePurgeAPI
  (purge [this uri surrogate-key]))
