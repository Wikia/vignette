(ns vignette.util.filesystem
  (:require [clojure.java.io :as io])
  (:use [environ.core])
  (:import java.util.UUID))

(declare resolve-local-path)
(declare create-local-path)
(declare get-parent)
(declare transfer!)
(declare file-exists?)

(def temp-file-location (env :vignette-temp-file-location "/tmp/vignette"))

(defn temp-filename
  []
  (let [filename (resolve-local-path
                   temp-file-location
                   (UUID/randomUUID))]
    (create-local-path (get-parent filename))
    filename))


(defn create-local-path
  [path]
  (.mkdirs (io/file path)))

(defn file-exists?
  [file]
  (.exists (io/file file)))

(defn get-parent
  [path]
  (.getParent (io/file path)))

(defn resolve-local-path
  [& more]
  (reduce str (interpose "/" more)))

; i think this should be a multimethod that dispatches
; based on the type
(defn transfer!
  [in out]
  (io/copy (io/file in) (io/file out))
  (file-exists? out))
