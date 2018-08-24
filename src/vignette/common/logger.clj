(ns vignette.common.logger
  (:require [io.clj.logging :as log]))

(defmacro debug
  ([message context]
   `(log/with-logging-context ~context (log/debug ~message)))
  ([message]
   `(log/debug ~message)))

(defmacro info
  ([message context]
   `(log/with-logging-context ~context (log/info ~message)))
  ([message]
   `(log/info message)))

(defmacro warn
  ([message context]
   `(log/with-logging-context ~context (log/warn ~message)))
  ([message]
   `(log/warn ~message)))

(defmacro error
  ([message context]
   `(log/with-logging-context ~context (log/error ~message)))
  ([message]
   `(log/error ~message)))
