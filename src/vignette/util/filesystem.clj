(ns vignette.util.filesystem
  (:require [clojure.java.io :as io]))

(declare resolve-local-path)
(declare create-local-path)
(declare get-parent)
(declare transfer!)
(declare file-exists?)

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
  [directory bucket path]
  (format "%s/%s/%s" directory bucket path))

; i think this should be a multimethod that dispatches
; based on the type
(defn transfer!
  [in out]
  (io/copy (io/file in) (io/file out))
  (file-exists? out))