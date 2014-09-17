(ns vignette.util.thumbnail
  (:require (vignette.storage [protocols :refer :all]
                              [common :refer :all])
            [vignette.media-types :refer :all]
            [vignette.util.filesystem :refer :all]
            [vignette.protocols :refer :all]
            [vignette.util.query-options :as q]
            [clojure.java.shell :refer (sh)]
            [clojure.java.io :as io]
            [wikia.common.logger :as log]
            [cheshire.core :refer :all]
            [slingshot.slingshot :refer (try+ throw+)])
  (:use [environ.core]))

(def thumbnail-bin (env :vignette-thumbnail-bin (if (file-exists? "/usr/local/bin/thumbnail")
                                                  "/usr/local/bin/thumbnail"
                                                  "bin/thumbnail")))

(def options-map {:height "height"
                  :width "width"
                  :thumbnail-mode "mode"})

(defn route-map->thumb-args
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
                      "--out" (q/modify-temp-file thumb-map temp-file)]
        route-options (route-map->thumb-args thumb-map)
        query-options (q/query-opts->thumb-args thumb-map)
        thumb-options (reduce conj route-options query-options)
        args (reduce conj base-command thumb-options)
        sh-out (run-thumbnailer args)]
    (cond
      (zero? (:exit sh-out)) (io/file temp-file)
      :else (throw+ {:type ::convert-error :exit (:exit sh-out) :out (:out sh-out) :err (:err sh-out)}))))

(declare generate-thumbnail)
(declare background-save-thumbnail)

(defn get-or-generate-thumbnail
  [system thumb-map]
  (if-let [thumb (get-thumbnail (store system) thumb-map)]
    thumb
    (when-let [thumb (generate-thumbnail system thumb-map)]
      (background-save-thumbnail (store system) (file-stream thumb) thumb-map)
      thumb))) ; TODO: cron to delete thumbs older than X)

(declare original->local)
(declare background-delete-file)

(defn generate-thumbnail
  "Generate a thumbnail from the original specified in thumb-map.
  This function will download the original locally and thumbnail it.
  The original will be removed after the thumbnailing is completed."
  [system thumb-map]
  (if-let [original (get-original (store system) thumb-map)]
    (when-let [local-original (original->local (file-stream original) thumb-map)]
      (try+
        (when-let [thumb (original->thumbnail local-original thumb-map)]
          ; if we support changing to a different type we need to change the content-type lookup here
          (create-storage-object thumb (content-type original) (file-length thumb)))
        (catch Object _ (throw+))
        (finally
          (background-delete-file local-original))))
    (log/warn "unable to get original for thumbnailing" thumb-map)))

(defn original->local
  "Take the original and make it local."
  [original thumb-map]
  (let [temp-file (io/file (temp-filename (str (wikia thumb-map) "_original")))]
    (when (transfer! original temp-file)
      temp-file)))

(defn background-save-thumbnail
  "Save the thumbnail in the background. This should not delay the rendering."
  [store stream thumb-map]
  (future (save-thumbnail store stream thumb-map)))

(defn background-delete-file
  [file]
  (future (io/delete-file file true)))
