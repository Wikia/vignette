(ns vignette.util.filesystem
  (:require [clojure.java.io :as io])
  (:use [environ.core])
  (:import java.util.UUID
           com.amazonaws.services.s3.model.S3ObjectInputStream))

(declare resolve-local-path
         create-local-path
         get-parent)

(def temp-file-location (env :vignette-temp-file-location "/tmp/vignette"))
(def rainbow-path-depth (Integer/parseInt (env :vignette-rainbow-depth "2")))

(defn temp-file-dir []
  temp-file-location)

(defn str->rainbow-path
  ([s desired-depth]
   (let [sanitized (clojure.string/replace s #"[^\w]" "")
         depth (min desired-depth (count sanitized))]
     (when (> depth 0)
       (loop [path []]
         (if (= depth (count path))
           (clojure.string/join "/" path)
           (recur (conj path (subs sanitized 0 (inc (count path))))))))))
  ([s]
   (str->rainbow-path s rainbow-path-depth)))

(defn gen-uuid []
  (str (UUID/randomUUID)))

(defn temp-filename
  ([prefix suffix]
   (let [extension (if (empty? suffix)
                     ""
                     (str "." suffix))
         uuid (gen-uuid)
         filename (str prefix "_" uuid extension)
         directory-rainbow (str->rainbow-path uuid)
         temp-dir (temp-file-dir)
         filepath (apply resolve-local-path (filter not-empty [temp-dir directory-rainbow filename]))]
     (create-local-path (get-parent filepath))
     filepath))
  ([prefix]
   (temp-filename prefix nil)))


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

(defn file-extension
  [filename]
  (let [[_ extension] (re-find #"\.(\w+)$" filename)]
    extension))
