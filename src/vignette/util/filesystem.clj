(ns vignette.util.filesystem
  (:require [vignette.util.byte-streams :refer :all]
            [clojure.java.io :as io])
  (:use [environ.core])
  (:import java.util.UUID))

(declare resolve-local-path)
(declare create-local-path)
(declare get-parent)

(def temp-file-location (env :vignette-temp-file-location "/tmp/vignette"))

(defn temp-filename
  ([prefix]
   (let [uuid (if (not (empty? prefix))
                (str prefix "_" (UUID/randomUUID))
                (UUID/randomUUID))
         filename (resolve-local-path temp-file-location uuid)]
     (create-local-path (get-parent filename))
     filename))
  ([]
   (temp-filename "")))


(defn create-local-path
  [path]
  (.mkdirs (io/file path)))

(defn file-exists?
  [file]
  (.exists (io/file file)))

(defn get-parent
  [path]
  (.getParent (io/file path)))

(defn file-length
  [file]
  (.length file))

(defn absolute-path
  [file]
  (.getAbsolutePath file))

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
