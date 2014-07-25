(ns vignette.media-types
  (:require [schema.core :as schema]
            [schema.macros :as sm] ))

; scchema structure
(def MediaFile
  {:type clojure.lang.Keyword
   :original String
   :middle-dir String
   :top-dir String
   :wikia String})

(def MediaThumbnailFile
  (merge MediaFile
         {:mode String
          :height String
          :width String}))

(def MediaReorientFile
  (merge MediaFile
         {:mode String}))

(defmulti get-media-map (fn [media] (:mode media)))
(defmethod get-media-map
           "reorient"
           [media]
  :pre [(schema/validate MediaReorientFile media)]
  media)
(defmethod get-media-map
           "resize"
           [media]
  :pre [(schema/validate MediaThumbnailFile media)]
  media)
(defmethod get-media-map :default [_]
  (throw (IllegalArgumentException. "Invalid media")))


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
  (:mode data))

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
