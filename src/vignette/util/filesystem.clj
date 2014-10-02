(ns vignette.util.filesystem
  (:require [clojure.java.io :as io])
  (:use [environ.core])
  (:import java.util.UUID
           com.amazonaws.services.s3.model.S3ObjectInputStream))

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
