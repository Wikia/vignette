(ns vignette.storage.local
  (:require [vignette.storage.core :refer :all]
            [clojure.java.io :as io]
            [clojure.java.shell :as sh]))

(declare resolve-local-path)
(declare create-local-path)
(declare transfer)

(defrecord LocalImageStorage [directory]
  ImageStorageProtocol

 (get-object [this bucket path]
   (let [real-path-object (io/file (resolve-local-path (:directory this) bucket path))]
     (when (.exists real-path-object)
       real-path-object)))

  (put-object [this resource bucket path]
    {:pre [(instance? java.io.File resource)]}
    (let [real-path (resolve-local-path (:directory this) bucket path)]
      (create-local-path real-path)
      (transfer resource (io/file real-path))))

  (delete-object [this bucket path])
  (list-buckets [this])
  (list-objects [this bucket]) )

(defn create-local-image-storage
  [directory]
  (->LocalImageStorage directory))

(defn resolve-local-path
  [directory bucket path]
  (format "%s/%s/%s" directory bucket path))

(declare dirname)

; fixme: make the type of these arguments consistent
(defn create-local-path
  ; type hint
  [path]
  (.mkdirs (io/file (dirname path))))

(defn dirname
  [path]
  (if-let [dir (.getParent (io/file path))]
    dir
    "."))

(defn transfer
  [in out]
  {:pre [(instance? java.io.File in)
         (instance? java.io.File out)
         (.exists (io/file (.getParent out)))]}
  (spit out (slurp in)))
