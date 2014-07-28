(ns vignette.util.thumbnail
  (:require [vignette.storage.protocols :as store-prot]
            [vignette.storage.local :as store-loc]
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
                  :mode "mode"})

(defn thumbnail-options
  [thumb-map]
  (reduce (fn [running [opt-key val]]
            (if-let [opt (get options-map opt-key)]
              (conj running (str "--" opt) (str val))
              running))
          []
          thumb-map))

(defn generate-thumbnail
  [resource thumb-map]
  (let [temp-file (temp-filename)
        base-command [thumbnail-bin
                      "--in" (.getAbsolutePath resource)
                      "--out" temp-file]
        thumb-options (thumbnail-options thumb-map)
        command (reduce conj base-command thumb-options)
        sh-out (apply sh command)]
    (cond
      (zero? (:exit sh-out)) (io/file temp-file)
      :else (throw (Exception.
                     (str "generating thumbnail failed (" (:exit sh-out) "): STDERR '" (:err sh-out)"' STDOUT: '" (:out sh-out "'")))))))

(defn get-or-generate-thumbnail
  [system thumb-map]
  (if-let [thumb (store-prot/get-thumbnail (store system) thumb-map)]
    thumb
    (if-let [original (store-prot/get-original (store system) thumb-map)]
      (if-let [thumb (generate-thumbnail original thumb-map)]
        (do (store-prot/save-thumbnail (store system) thumb thumb-map)
            thumb) ; TODO: cron to delete thumbs older than X)
        nil)
      nil)))
