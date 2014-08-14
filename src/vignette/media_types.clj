(ns vignette.media-types)

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
  (clojure.string/join "/" ((juxt top-dir middle-dir original) data)))

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
  (clojure.string/join "/" 
         ((juxt top-dir middle-dir thumbnail) data)))

(defn thumbnail
  [data]
  (format "%dpx-%dpx-%s-%s" (width data) (height data) (mode data) (original data)))
