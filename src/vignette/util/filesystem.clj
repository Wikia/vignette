(ns vignette.util.filesystem
  (:require [vignette.util.byte-streams :refer :all]
            [clojure.java.io :as io])
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
(defmulti transfer! (fn [in out] (class in)))

(defn transfer-file-like-object!
  [in out]
  (io/copy (io/file in) (io/file out))
  (file-exists? out))

(defmethod transfer! java.lang.String
  [in out]
  (transfer-file-like-object! in out))

(defmethod transfer! java.io.File
  [in out]
  (transfer-file-like-object! in out))

(defmethod transfer! (Class/forName "[B")
  [in out]
  (write-byte-stream (io/file out) in)
  (file-exists? out))
