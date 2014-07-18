(ns vignette.api.legacy.routes
  (:require [clout.core :refer (route-compile route-matches)]))

;{
; "width" : "130px",
; "archive" : "",
; "input" : "/swift/v1/herofactory/images/thumb/7/7f/Brain_Attack.PNG/130px-102%2C382%2C0%2C247-Brain_Attack.PNG",
; "dbname" : "swift/v1/herofactory",
; "hook_name" : "image thumbnailer",
; "thumbpath" : "swift/v1/herofactory/images/thumb/7/7f/Brain_Attack.PNG",
; "thumbname" : "130px-102%2C382%2C0%2C247-Brain_Attack.PNG",
; "filename" : "Brain_Attack",
; "thumbext" : "102%2C382%2C0%2C247-Brain_Attack",
; "type" : "images",
; "fileext" : "PNG"
; }
(def image-thumbnail
  (route-compile "/swift/:swift-version/:wikia/:image-type/:uri-request-type/:top-dir/:middle-dir/:original/:thumbnail"
                 {:wikia #"\w+"
                  :swift-version #"\w+"
                  :image-type #"images|avatars"
                  :uri-request-type #"\w+"
                  :top-dir #"[0-9a-f]{1}?"
                  :middle-dir #"[0-9a-f]{2}"}))

(defn add-request-type
  [m request-type]
  {:pre [(map? m)]
   :post [(map? %)]}
  (merge m {:request-type request-type}))

(defmulti request-map-add-width :request-type)

(defmethod request-map-add-width :image-thumbnail
  [m]
  "Add the :width field to a request map based on the legacy parsing methods."
  (let [path (get m :thumbnail "")
        matches (re-find #"(?ix)(\d+px|\d+x\d+|\d+x\d+x\d+|)\-(.*)\.(jpg|jpeg|jpe|png|gif|webp)" path)]
     (if-let [[_ width _ _] matches]
       (assoc m :width width)
       m)))

(defmethod request-map-add-width nil
  [m]
  (assoc m :width ""))

(defn request-map->thumbpath
  [m]
  (format "%s/%s/%s/%s/%s/%s"
          (:wikia m)
          (:image-type m)
          (:uri-request-type m)
          (:top-dir m)
          (:middle-dir m)
          (:original m)))

(defn request-map-add-thumbpath
  [m]
  (assoc m :thumbpath (request-map->thumbpath m)))

;;;;;

(defmulti request-map->dimensions :request-type)

(defmethod request-map->dimensions :image-thumbnail
  [m])


(defn remove-extension
  "Remove the extension from a filename."
  [filename]
  (clojure.string/replace filename #"\.\w+$" ""))

(declare seq-of-strings->seq-of-integers)

(def width-px-regex #"^(\d+)px$")
(def width-x-height-regex #"^(\d+)x(\d+)$")
(def width-x-height-x-scale-regex #"^(\d+)x(\d+)x(\d+)$")
;(def color-width-regex #"^v,([0-9a-f]{6},)?(\d+)px$")

; these probably need to be dispatched based on the type
; e.g. image thumbnailer, SVG thumbnailer, etc

(defn thumbnail-path->dimensions
  "Given a thumb path, extract the dimensions.
  For example, from \"130px-102,382,0,247-Brain_Attack.PNG\", it will
  extract the \"130px\" and decode it to a width.

  The return value is a vector with the type of dimension matched followed by
  a sequence with the extracted dimensions.

  Note that the current implementation favors clarity over efficiency. We end up with, in the worst
  case, 5 calls to `re-find` and in the best case 2.

  Source: backend:lib/Wikia/Thumbnailer/File.pm
  See has thumb_size declaration.

  TODO: Defaults and config checks that are currently used in File.pm?
  "
  [thumb-path]
  (let [matches (re-find #"(?ix)(\d+px|\d+x\d+|\d+x\d+x\d+|)\-(.*)\.(jpg|jpeg|jpe|png|gif|webp)" thumb-path)]
    (when-let [[_ width _ _] matches]
      (cond
        (empty? width) nil
        (re-find width-px-regex width) [:width (seq-of-strings->seq-of-integers (rest (re-find width-px-regex width)))]
        (re-find width-x-height-regex width) [:width-height (seq-of-strings->seq-of-integers (rest (re-find width-x-height-regex width)))]
        (re-find width-x-height-x-scale-regex width) [:width-height-scale (seq-of-strings->seq-of-integers (rest (re-find width-x-height-x-scale-regex width)))]
        ; doesn't belong here-- goes with images without extensions
        ;(re-find color-width-regex width) [:width-color (let [[_ color w] (re-find color-width-regex width)]
                                                       ;(list color (Integer. w)))]
        :else [:default (seq-of-strings->seq-of-integers (list width))]))))


(defn thumbnail-path->coordinates
  "Given a thumbnail path, extract the centering coordinates.
  For example, from \"130px-102,382,0,247-Brain_Attack.PNG\", it will
  extract the \"102,382,0,247\" and decode it to a set of coordinates.
  
  Source: backend:lib/Wikia/Thumbnailer/File.pm
  See the 'has' thumb_name of thumb_path.
  "
  [thumb-path]
  (let [matches (re-find #"(\d+),(\d+),(\d+),(\d+)" thumb-path)]
    (when-let [[_ & coordinates] matches]
      (zipmap [:x1 :x2 :y1 :y2] (map #(Integer. %) coordinates)))))

(defn seq-of-strings->seq-of-integers
  [s]
  (map #(Integer. %) s))