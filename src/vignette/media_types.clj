(ns vignette.media-types
  (:require [schema.core :as schema]
            [schema.macros :as sm] ))

; scchema structure
(def MediaFile
  {:request-type clojure.lang.Keyword
   :original String
   :middle-dir String
   :top-dir String
   :wikia String})

(def MediaThumbnailFile
  (merge MediaFile
         {:mode String
          :height String
          :width String}))

(def MediaModeFile
  (merge MediaFile
         {:mode String}))

(defmulti get-media-map :request-type)

(defmethod get-media-map
           :mode
           [media]
  (schema/validate MediaModeFile media))

(defmethod get-media-map
           :thumbnail
           [media]
  (schema/validate MediaThumbnailFile media))

(defmethod get-media-map
           :original
           [media]
  (schema/validate MediaFile media))

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
