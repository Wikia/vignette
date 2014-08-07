(ns vignette.util.thumbnail
  (:require [vignette.storage.protocols :refer :all]
            [vignette.util.filesystem :refer :all]
            [vignette.protocols :refer :all]
            [clojure.java.shell :refer (sh)]
            [clojure.java.io :as io]
            [cheshire.core :refer :all])
  (:use [environ.core])
  (:import java.util.UUID))

(def thumbnail-bin (env :vignette-thumbnail-bin "bin/thumbnail"))

; fixme: add more randomness
(defn temp-filename
  []
  (let [filename (resolve-local-path
                   temp-file-location
                   (UUID/randomUUID))]
    (create-local-path (get-parent filename))
    filename))

(def options-map {:height "height"
                  :width "width"
                  :thumbnail-mode "mode"})

(defn thumbnail-options
  [thumb-map]
  (reduce (fn [running [opt-key val]]
            (if-let [opt (get options-map opt-key)]
              (conj running (str "--" opt) (str val))
              running))
          []
          thumb-map))

(defn run-thumbnailer
  [args]
  (apply sh args))

(defn generate-thumbnail
  [resource thumb-map]
  (let [temp-file (temp-filename)
        base-command [thumbnail-bin
                      "--in" (.getAbsolutePath resource)
                      "--out" temp-file]
        thumb-options (thumbnail-options thumb-map)
        args (reduce conj base-command thumb-options)
        sh-out (run-thumbnailer args)]
    (cond
      (zero? (:exit sh-out)) (io/file temp-file)
      :else (throw (Exception.
                     (str (format "generating thumbnail failed (%s): %s\nSTDERR '%s' STDOUT: '%s' params: %s"
                                  (:exit sh-out) args (:err sh-out) (:out sh-out) thumb-map)))))))

(defn get-or-generate-thumbnail
  [system thumb-map]
  (if-let [thumb (get-thumbnail (store system) thumb-map)]
    thumb
    (when-let [original (get-original (store system) thumb-map)]
      (when-let [thumb (generate-thumbnail original thumb-map)]
        (save-thumbnail (store system) thumb thumb-map)
        thumb)))) ; TODO: cron to delete thumbs older than X)
