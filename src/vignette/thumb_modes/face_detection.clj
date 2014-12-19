(ns vignette.thumb-modes.face-detection
  (:require [vignette.util.query-options :as q]
            [vignette.media-types :as mt])
  (:import (org.opencv.core MatOfRect)
           (org.opencv.imgcodecs Imgcodecs)
           (org.opencv.objdetect CascadeClassifier)))

(declare generate-image
         tempify-thumb-map)

(defn face-detect [file thumb-map thumbnail-func]
  (let [filepath (.getAbsolutePath file)
        detector (CascadeClassifier. "resources/lbpcascade_frontalface.xml")
        image (Imgcodecs/imread filepath)
        detections (MatOfRect.)]
    (.detectMultiScale detector image detections)
    (let [detections (.toArray detections)]
      (if (empty? detections)
        filepath
        (generate-image file
                        (get detections
                             (Integer. (q/query-opt thumb-map :face)))
                        thumb-map
                        thumbnail-func)))))

(defn generate-image [file rect thumb-map thumbnail-func]
  (let [temp-thumb-map (tempify-thumb-map thumb-map rect)
        window-image (thumbnail-func file temp-thumb-map)]
    (.getAbsolutePath window-image)))

; alter :wikia, set :thumb-mode to window-crop-fixed
(defn tempify-thumb-map [orig-map rect]
  (let [map (merge orig-map {:thumbnail-mode "window-crop-fixed"
                             :width (.width rect)
                             :height (.height rect)
                             :x-offset (.x rect)
                             :y-offset (.y rect)
                             :window-width (.width rect)
                             :window-height (.height rect)})]
    (merge map {:options (dissoc (:options map) :face)})))
