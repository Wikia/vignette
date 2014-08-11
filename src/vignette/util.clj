(ns vignette.util
  (:require [wikia.common.logger :as log]))

(defn log-error-and-throw
  [error-msg context]
  (do
    (log/error error-msg context)
    (throw (Exception. error-msg))))

