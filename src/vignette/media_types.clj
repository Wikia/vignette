(ns vignette.media-types
  (require [vignette.util.query-options :refer :all]))

(declare original
         thumbnail
         map->filename)

(def archive-dir "archive")

(defn revision
  [data]
  (if (= (:revision data) "latest")
    nil
    (:revision data)))

(defn revision-filename
  [data]
  (if-let [revision (revision data)]
    (str revision "!" (original data))
    (original data)))

(defn top-dir
  [data]
  (:top-dir data))

(defn middle-dir
  [data]
  (:middle-dir data))

(defn original
  [data]
  (:original data))

(defn original-path
  [data]
  (let [image-path (clojure.string/join "/" ((juxt top-dir middle-dir) data))
        filename (revision-filename data)]
    (if (nil? (revision data))
      (clojure.string/join "/" [image-path filename])
      (clojure.string/join "/" [archive-dir image-path filename]))))

(defn wikia
  [data]
  (:wikia data))

(defn mode
  [data]
  (:thumbnail-mode data))

(defn window-format
  [x-or-y offset width-or-height]
  (if (and offset width-or-height)
    (str x-or-y "[offset=" offset ",length=" width-or-height "]")
    x-or-y))

(defn height
  [data]
  (let [height (:height data)
        y-offset (:y-offset data)
        window-height (:window-height data)]
    (window-format height y-offset window-height)))

(defn width
  [data]
  (let [width (:width data)
        x-offset (:x-offset data)
        window-width (:window-width data)]
    (window-format width x-offset window-width)))

(defn thumbnail-path
  [data]
  (let [image-path (clojure.string/join "/" ((juxt top-dir middle-dir) data))]
    (map->filename data image-path)))

(defmulti map->filename (fn [data image-path]
                          (revision data)))

(defmethod map->filename nil [data image-path]
  (let [name (format "%s/%spx-%spx-%s%s-%s" (original data) (width data) (height data) (mode data) (query-opts-str data) (original data))]
    (clojure.string/join "/" [image-path name])))

(defmethod map->filename :default [data image-path]
  (let [name (format "%spx-%spx-%s%s-%s" (width data) (height data) (mode data) (query-opts-str data) (original data))]
    (clojure.string/join "/" [archive-dir image-path (revision-filename data) name])))
