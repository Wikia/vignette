(ns vignette.util.thumbnail
  (:require [vignette.storage.protocols :refer :all]
            [vignette.media-types :refer :all]
            [vignette.util :as u]
            [vignette.util.filesystem :refer :all]
            [vignette.protocols :refer :all]
            [clojure.java.shell :refer (sh)]
            [clojure.java.io :as io]
            [cheshire.core :refer :all])
  (:use [environ.core]))

(def thumbnail-bin (env :vignette-thumbnail-bin (if (file-exists? "/usr/local/bin/thumbnail")
                                                  "/usr/local/bin/thumbnail"
                                                  "bin/thumbnail")))

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

(defn original->thumbnail
  [resource thumb-map]
  (let [temp-file (temp-filename (str (wikia thumb-map) "_thumb"))
        base-command [thumbnail-bin
                      "--in" (.getAbsolutePath resource)
                      "--out" temp-file]
        thumb-options (thumbnail-options thumb-map)
        args (reduce conj base-command thumb-options)
        sh-out (run-thumbnailer args)]
    (cond
      (zero? (:exit sh-out)) (io/file temp-file)
      ; FIXME: use slingshot here (https://github.com/scgilardi/slingshot)
      :else (u/log-error-and-throw "thumbnailing failed" sh-out))))

(defn get-or-generate-thumbnail
  [system thumb-map]
  ; FIXME: how should we handle this when we are using S3 if we never store the
  ; thumbnails on S3?
  (if-let [thumb (get-thumbnail (store system) thumb-map)]
    thumb
    (when-let [original (get-original (store system) thumb-map)]
      (when-let [thumb (original->thumbnail original thumb-map)]
        (save-thumbnail (store system) thumb thumb-map)
        thumb)))) ; TODO: cron to delete thumbs older than X)

(defn generate-thumbnail
  "Generate a thumbnail from the original specified in thumb-map.
  This function will download the original locally and thumbnail it. If
  delete-local-original is truthy, the original will be removed after the
  thumbnailing is completed."
  [system thumb-map & [delete-local-original]]
  (when-let [local-original (get-original (store system) thumb-map)]
    (try
      (when-let [thumb (original->thumbnail local-original thumb-map)]
        thumb)
      (catch Exception e (throw e))
      (finally
        (when delete-local-original
          (future (io/delete-file local-original true)))))))
