(ns vignette.util.thumbnail
  (:require [cheshire.core :refer :all]
            [clojure.java.io :as io]
            [clojure.string :refer [split]]
            [clojure.java.shell :refer [sh]]
            [pantomime.mime :refer [mime-type-of]]
            [slingshot.slingshot :refer [try+ throw+]]
            [vignette.media-types :refer :all]
            [vignette.protocols :refer :all]
            [vignette.storage.local :as ls]
            [vignette.storage.protocols :refer :all]
            [vignette.util.filesystem :refer :all]
            [vignette.util.query-options :as q]
            [wikia.common.perfmonitoring.core :as perf])
  (:use [environ.core]))

(declare original->local
         file->thumbnail
         background-delete-file
         generate-thumbnail
         background-save-thumbnail)

(def thumbnail-bin (env :vignette-thumbnail-bin (if (file-exists? "/usr/local/bin/thumbnail")
                                                  "/usr/local/bin/thumbnail"
                                                  "bin/thumbnail")))

(def options-map {:height         "height"
                  :width          "width"
                  :thumbnail-mode "mode"
                  :x-offset       "x-offset"
                  :y-offset       "y-offset"
                  :window-width   "window-width"
                  :window-height  "window-height"})

(def passthrough-mime-types #{"audio/ogg" "video/ogg"})
(defn is-passthrough-required
  [file]
  (contains? passthrough-mime-types (mime-type-of file)))

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
  (perf/timing :imagemagick (apply sh args)))

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
      (or (zero? (:exit sh-out))
          (and (= 1 (:exit sh-out))
               (file-exists? temp-file))) (io/file temp-file)
      :else (throw+ {:type         :convert-error
                     :error-code   (:exit sh-out)
                     :error-string (:err sh-out)}
                    "thumbnailing error"))))

(defn get-or-generate-thumbnail
  [store thumb-map]
  (if-let [thumb (and (not (q/query-opt thumb-map :replace))
                      (get-thumbnail store thumb-map))]
    (do
      (perf/publish {:thumbnail-cache-hit 1})
      thumb)
    (when-let [thumb (generate-thumbnail store thumb-map)]
      thumb)))

(defn generate-thumbnail
  "Generate a thumbnail from the original specified in thumb-map.
  This function will download the original locally and thumbnail it.
  The original will be removed after the thumbnailing is completed."
  [store thumb-map]
  (if-let [original (get-original store thumb-map)]
    (if (is-passthrough-required original)
      original
      (when-let [local-original (original->local original)]
        (try+
          (when-let [thumb (original->thumbnail local-original thumb-map)]
            (perf/publish {:generate-thumbail 1})
            (ls/create-stored-object thumb (fn [stored-object]
                                             (background-save-thumbnail store
                                                                        stored-object
                                                                        thumb-map))))
          (catch Object _ (throw+))
          (finally
            (background-delete-file local-original)))))
    (throw+ {:type          :convert-error
             :thumb-map     thumb-map
             :response-code 404}
            "unable to get original for thumbnailing")))

(defn- extract-extension [filename]
  (last (rest (split filename #"\."))))

(defn original->local
  "Take the original and make it local."
  [original]
  (let [temp-file (io/file (temp-filename nil (extract-extension (filename original))))]
    (when (transfer! original temp-file)
      temp-file)))

(defn background-save-thumbnail
  "Save the thumbnail in the background. This should not delay the rendering."
  [store stored-object map]
  (future (save-thumbnail store stored-object map)
          (io/delete-file (file-stream stored-object))))

(defn background-delete-file
  [file]
  (future (io/delete-file file true)))
