(ns vignette.media-types
  (require [vignette.util.query-options :refer :all]))

(declare original)

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

(defn height
  [data]
  (Integer. (:height data)))

(defn width
  [data]
  (Integer. (:width data)))

(declare thumbnail)

; /3/35/100px-100px-resize-arwen.png
(defn thumbnail-path
  [data]
  (let [image-path (clojure.string/join "/" ((juxt top-dir middle-dir) data))
        thumbnail (thumbnail data)]
    (if (nil? (revision data))
      (clojure.string/join "/" [image-path thumbnail])
      (clojure.string/join "/" [archive-dir image-path (revision-filename data) thumbnail]))))

(defn thumbnail
  [data]
  (format "%dpx-%dpx-%s%s-%s" (width data) (height data) (mode data) (query-opts-str data) (original data)))
