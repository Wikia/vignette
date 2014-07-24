(ns vignette.server
  (:require [vignette.http.routes :as r]
            [org.httpkit.server :refer :all]))

(def default-port 8080)

; http://http-kit.org/server.html#stop-server
(defn run
  ([system port]
   (run-server
     (#'r/app-routes system)
     {:port port}))
  ([system]
   (run system default-port)))
