(ns vignette.http.compojure
  (:require [compojure.core :refer [compile-route context GET]]))

(defmacro PURGE "Generate a PURGE route."
    [path args & body]
      (compile-route :purge path args body))

(defmacro GET+ "Generate a route that matches GET or PURGE"
  [path args & body]
  `(context "" []
        (GET ~path ~args ~@body)
        (PURGE ~path ~args ~@body)))
