(ns vignette.server
  (:require [org.httpkit.server :refer :all]))

(def default-port 8080)

; http://http-kit.org/server.html#stop-server
(defn run
  ([app system port]
   (run-server
     (app system)
     {:port port}))
  ([app system]
   (run app system default-port)))
