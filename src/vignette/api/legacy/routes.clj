(ns vignette.api.legacy.routes
  (:require [clout.core :refer (route-compile route-matches)]))

(declare route->revision
         route->dimensions
         route->thumb-mode
         route->options
         add-request-type)

;29 {
;30    "width" : "185px",
;31    "archive" : "",
;32    "wikia" : "happywheels",
;33    "input" : "/happywheels/images/thumb/b/bb/SuperMario64_20.png/185px-SuperMario64_20.webp",
;34    "hook_name" : "image thumbnailer",
;35    "thumbpath" : "happywheels/images/thumb/b/bb/SuperMario64_20.png",
;36    "thumbname" : "185px-SuperMario64_20.webp",
;37    "filename" : "SuperMario64_20",
;38    "thumbext" : "SuperMario64_20",
;39    "type" : "images",
;40    "fileext" : "png"
;41 }
; qr{ \/((?!\w\/)?(.+)\/(images|avatars)\/thumb((?!\/archive).*|\/archive)?\/\w\/\w{2}\/(.+)\.(jpg|jpeg|png|gif{1,}))\/((\d+px|\d+x\d+|\d+x\d+x\d+|)\-(.*)\.(jpg|jpeg|jpe|png|gif|webp))(\?.*)?$ }xi,
(def thumbnail-route
  (route-compile "/:wikia/:image-type/thumb:archive/:top-dir/:middle-dir/:original/:thumbname"
                 {:wikia #"[\w-]+"
                  :image-type #"images|avatars"
                  :archive #"(?!\/archive).*|\/archive"
                  :top-dir #"\w"
                  :middle-dir #"\w\w"
                  :original #"[^/]*"
                  :thumbname #".*"}))

(def original-route
  (route-compile "/:wikia/images:archive/:top-dir/:middle-dir/:original"
                 {:wikia #"[\w-]+"
                  :archive #"(?!\/archive).*|\/archive"
                  :top-dir #"\w"
                  :middle-dir #"\w\w"
                  :original #".*"}))

(defn route->thumb-map
  [route-params]
  (let [map (-> route-params
                (add-request-type :thumbnail)
                (assoc :thumbnail-mode "thumbnail")
                (route->dimensions)
                (route->revision)
                (route->options))]
    map))

(defn route->original-map
  [route-params]
  (let [map (-> route-params
                (add-request-type :original)
                (route->revision))]
    map))

(defn add-request-type
  [m request-type]
  {:pre [(map? m)]
   :post [(map? %)]}
  (merge m {:request-type request-type}))

(defn route->revision
  [map]
  (let [revision (if (re-matches #"^\d+!.*" (:original map))
                   (re-find #"^\d+" (:original map))
                   "latest")]
    (merge map {:revision revision})))

(defn route->options
  [map]
  (let [[_ format] (re-find #"\.([a-z]+)$" (:thumbname map))]
    (assoc map :options {:format format})))

(defmulti route->dimensions :request-type)

(defmethod route->dimensions :thumbnail
  [route]
  "Add the :width field to a request map based on the legacy parsing methods."
  (if-let [thumb-name (:thumbname route)]
    (if-let [[_ dimension] (re-find #"^(\d+)px-" thumb-name)]
      (merge route {:width dimension
                    :height dimension})
      (if-let [[_ width height] (re-find #"^(\d+)x(\d+)-" thumb-name)]
        (merge route {:width width
                    :height height
                    :thumbnail-mode "fixed-aspect-ratio"})
        (if-let [[_ width height _] (re-find #"^(\d+)x(\d+)x(\d+)-" thumb-name)]
          (merge route {:width width
                        :height height
                        :thumbnail-mode "zoom-crop"})
          route)))
    route))

(defmethod route->dimensions nil
  [map]
  map)

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
