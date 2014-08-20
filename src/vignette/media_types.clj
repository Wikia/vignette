(ns vignette.media-types)

(declare original)

(def archive-dir "archive")

(defn revision
  [data]
  (if (= (:revision data) "latest")
    nil
    (:revision data)))

(defn revision-filename
  [data]
  (str (revision data) "!" (original data)))

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
        original (original data)
        revision (revision data)]
    (if (nil? revision)
      (clojure.string/join "/" [image-path original])
      (clojure.string/join "/" [archive-dir image-path (revision-filename data)]))))

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
        thumbnail (thumbnail data)
        revision (revision data)]
    (if (nil? revision)
      (clojure.string/join "/" [image-path thumbnail])
      (clojure.string/join "/" [archive-dir image-path (revision-filename data) thumbnail]))))

(defn thumbnail
  [data]
  (format "%dpx-%dpx-%s-%s" (width data) (height data) (mode data) (original data)))
