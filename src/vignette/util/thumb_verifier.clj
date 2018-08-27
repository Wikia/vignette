(ns vignette.util.thumb-verifier
  (:require [vignette.common.logger :as log]
            [clojure.java.shell :refer [sh]]
            [clojure.string :refer [trim]])
  (:use [environ.core]))

(declare
  thumb-size-estimators
  estimate-thumb-size
  size-of
  not-close-in-size
  area-ratio)

(defn check-thumb-size
  "Checks if the generated thumbnail has the correct size.
  If not, an error is logged."
  [thumb-map thumb original]
  (try
    (if-let [estimator
              (thumb-size-estimators (keyword (:thumbnail-mode thumb-map)))]
      (let [thumb-size (size-of thumb)
            estimated-size (estimate-thumb-size estimator thumb-map original)]
              (if (not-close-in-size estimated-size thumb-size)
                (log/error "Thumbnail size is incorrect!" {
                  :thumb_map thumb-map
                  :estimated estimated-size
                  :actual thumb-size
                  :area_ratio (area-ratio thumb-size estimated-size)
                  })))
      (log/error "Couldn't verify thumbnail size. No estimator found." {
        :thumb_map thumb-map
        }))
    (catch Exception e (log/error (str "Thumbnail verification failed - " e) {
      :thumb_map thumb-map
      }))))

(defn not-close-in-size
  "Checks if the given dimentions are different,
  assuming there may be a rounding error"
  [estimated actual]
  (let [dw (Math/abs (- (:width estimated) (:width actual 0)))
        dh (Math/abs (- (:height estimated) (:height actual 0)))]
    (or (< 1 dw) (< 1 dh))))

(defn area
  [dimentions]
  (* (:width dimentions 0) (:height dimentions 0)))

(defn area-ratio
  [actual estimated]
  (let [actual-area (area actual)
        estimated-area (area estimated)]
    (if (zero? estimated-area) 0.0 (float (/ actual-area estimated-area)))))

(def identify-bin
  (trim (str (env :imagemagick-base "/usr/local") "/bin/identify")))

(defn identify
  [file]
  (:out (sh identify-bin file)))

(defn size-of
  "Calculates the size of the image in the given file using ImageMagick"
  [img-file]
  (-> (.getAbsolutePath img-file)
      identify
      (->> (re-matcher #"(\d+)x(\d+)")
           re-find
           rest
           (map #(Integer/valueOf %))
           (zipmap [:width :height]))))

(defn extract-req-size
  "Extracts dimentions from the request parameters"
  [width height]
  (fn [thumb-map]
    {:width (Integer/valueOf (width thumb-map))
     :height (Integer/valueOf (height thumb-map))}))

(def requested-size (extract-req-size :width :height))

(def requested-window-size (extract-req-size :window-width :window-height))

(defn ratio
  [[numerator denominator] size]
  (/ (numerator size) (denominator size)))

(defn scale
  "Produces a scaling function along one dimention of a box (width or height)"
  [dim other]
  (fn [requested original]
    (let [value (Integer/valueOf (dim requested))
          ratio (ratio [other dim] original)]
          {dim value other (Math/round (float (* value ratio)))})))

(defn scale-upto-original
  "Produces a scaling function bounded by the size of the original"
  [dim other scale-fn]
  (fn [requested original]
    (let [requested-value (Integer/valueOf (dim requested))
          original-value (dim original)
          adjusted-value (min requested-value original-value)]
          (scale-fn {dim adjusted-value} original))))

(def scale-width (scale :width :height))

(def scale-width-upto-original (scale-upto-original :width :height scale-width))

(def scale-height (scale :height :width))

(def scale-height-upto-original
  (scale-upto-original :height :width scale-height))

(defn scale-proportionally
  "Scales the size to the request and maintains the aspect ratio"
  [requested original]
  (let [req-ratio (ratio [:height :width] (requested-size requested))
        org-ratio (ratio [:height :width] original)]
    (if (> req-ratio org-ratio)
      (scale-width requested original)
      (scale-height requested original))))

(defn scale-window-width
  "Scales the size to the requested width
  maintaining the proportions of the requested window"
  [requested]
  (let [window (requested-window-size requested)]
    (scale-width requested window)))

(defn keep-dimensions
  [requested original]
  (requested-size original))

(defn fitter
  "Creates a function that proportionally shrinks the given box,
  so that its 'a' (width or height) is not grater than that of the container"
  [a b]
  (fn [{box-a a box-b b :as box} {container-a a container-b b :as container}]
    (if (> box-a container-a)
      (let [delta-a (- box-a container-a)
            ratio (/ box-b box-a)
            delta-b (Math/round (* delta-a (float ratio)))]
            {a (- box-a delta-a) b (- box-b delta-b)})
      box)))

(def fit-width (fitter :width :height))

(def fit-height (fitter :height :width))

(defn fit-in-original
  "Shrinks the requested size to fit inside the original.
  The proportions of the box are maintained"
  [requested original]
  (-> requested
      requested-size
      (fit-width original)
      (fit-height original)))

(defn scale-and-fit-in-original
  [requested original]
  (-> requested
      (scale-proportionally original)
      (fit-in-original original)))

(def as-requested
  {:fn requested-size :requires-original false})

(def scaled-width
  {:fn scale-width :requires-original true})

(def scaled-width-upto-original
  {:fn scale-width-upto-original :requires-original true})

(def scaled-height-upto-original
  {:fn scale-height-upto-original :requires-original true})

(def scaled-proportionally
  {:fn scale-proportionally :requires-original true})

(def scaled-window-width
  {:fn scale-window-width :requires-original false})

(def requested-proportions-upto-original
  {:fn fit-in-original :requires-original true})

(def original-proportions-upto-original
  {:fn scale-and-fit-in-original :requires-original true})

(def keep-original-dimensions
  {:fn keep-dimensions :requires-original true})

(def thumb-size-estimators {
  :fixed-aspect-ratio as-requested
  :fixed-aspect-ratio-down as-requested
  :scale-to-width scaled-width
  :scale-to-width-down scaled-width-upto-original
  :scale-to-height-down scaled-height-upto-original
  :thumbnail scaled-proportionally
  :thumbnail-down original-proportions-upto-original
  :top-crop as-requested
  :top-crop-down requested-proportions-upto-original
  :window-crop scaled-window-width
  :window-crop-fixed as-requested
  :zoom-crop as-requested
  :zoom-crop-down requested-proportions-upto-original
  :type-convert keep-original-dimensions
  })

(defn estimate-thumb-size
  [{estimate :fn requires-original :requires-original} thumb-map original]
  (if requires-original
    (estimate thumb-map (size-of original))
    (estimate thumb-map)))
