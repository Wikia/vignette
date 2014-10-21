(ns vignette.media-types
  (:require [slingshot.slingshot :refer [throw+]]
            [vignette.util.query-options :refer :all]))

(declare original
         thumbnail
         thumb-map->path)

(def archive-dir "archive")

(defmulti image-type->thumb-prefix (fn [object-map] (:image-type object-map)))

(defmethod image-type->thumb-prefix "avatars" [object-map]
  (:image-type object-map))

(defmethod image-type->thumb-prefix "images" [object-map]
  (let [prefix (:image-type object-map)]
    (if-let [lang (query-opt object-map :lang)]
      (str lang "/" prefix)
      prefix)))

(defmethod image-type->thumb-prefix :default [object-map]
  (throw+ {:type ::error :message (str "unsupported image-type "
                                       (:image-type object-map))}))

(defn thumb-map->prefix [object-map]
  (let [prefix (image-type->thumb-prefix object-map)]
    (str prefix "/thumb" )))

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
  (let [image-type (:image-type data)
        image-path (clojure.string/join "/" ((juxt top-dir middle-dir) data))
        filename (revision-filename data)]
    (if (nil? (revision data))
      (clojure.string/join "/" [image-type image-path filename])
      (clojure.string/join "/" [image-type archive-dir image-path filename]))))

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
  (let [thumb-path (clojure.string/join "/" ((juxt top-dir middle-dir) data))]
    (thumb-map->path data thumb-path)))

(defmulti thumb-map->path (fn [data image-path]
                          (revision data)))

(defmethod thumb-map->path nil [data image-path]
  (let [prefix (thumb-map->prefix data)
        name (format "%s/%spx-%spx-%s%s-%s" (original data) (width data) (height data) (mode data) (query-opts-str data) (original data))]
    (clojure.string/join "/" [prefix image-path name])))

(defmethod thumb-map->path :default [data image-path]
  (let [prefix (thumb-map->prefix data)
        name (format "%spx-%spx-%s%s-%s" (width data) (height data) (mode data) (query-opts-str data) (original data))]
    (clojure.string/join "/" [prefix archive-dir image-path (revision-filename data) name])))
