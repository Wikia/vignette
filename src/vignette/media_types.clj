(ns vignette.media-types
  (:require [schema.core :as schema]
            [schema.macros :as sm] ))

; scchema structure
(def MediaFile
  {:type String
   :original String
   :middle-dir String
   :top-dir String
   :wikia String})

(def MediaThumbnailFile
  (merge MediaFile
         {:mode String
          :height String
          :width String})) 


(defn get-top-dir
  [data]
  (:top-dir data))

(defn get-middle-dir
  [data]
  (:middle-dir data))

(defn get-original
  [data]
  (:original data))

(defn get-original-path
  [data]
  (clojure.string/join "/" ((juxt get-top-dir get-middle-dir get-original) data)))

(defn get-wikia
  [data]
  (:wikia data))

(defn get-mode
  [data]
  (:mode data))

(defn get-height
  [data]
  (Integer. (:height data)))

(defn get-width
  [data]
  (Integer. (:width data)))

(declare get-thumbnail)

; /3/35/100px-100px-resize-arwen.png
(defn get-thumbnail-path
  [data]
  (clojure.string/join "/" 
         ((juxt get-top-dir get-middle-dir get-thumbnail) data)))

(defn get-thumbnail
  [data]
  (format "%dpx-%dpx-%s-%s" (get-width data) (get-height data) (get-mode data) (get-original data)))

(sm/defn create-thumbnail :- MediaThumbnailFile
  [data :- MediaThumbnailFile]
  data)

(defn create-thumbnail-validated
  [data]
  (schema/with-fn-validation
    (create-thumbnail data)))
