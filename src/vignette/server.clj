(ns vignette.server
  (:require [vignette.http.routes :as r]
            [org.httpkit.server :refer :all]))



; http://http-kit.org/server.html#stop-server
(defn run
  [system]
  (run-server
    (#'r/app-routes system)
    {:port 8080}))
