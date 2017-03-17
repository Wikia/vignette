(ns vignette.http.legacy.route-helpers
  (:require [useful.experimental :refer [cond-let]]
            [vignette.protocols :refer :all]
            [vignette.storage.protocols :refer :all]
            [vignette.media-types :refer [archive-dir]]
            [vignette.media-types :as mt])
  (:import [java.net URLDecoder]))

(def default-width 200)
(def webp-accept-header-name "accept")

(declare route->revision
         route->dimensions
         route->offset
         route->thumb-mode
         route->options
         route->thumb-map
         route->original-map
         route->interactive-maps-map
         route->interactive-maps-thumbnail-map
         route->timeline-map)

(defn archive? [map]
  (= (:zone map) (str "/" archive-dir)))

(defn browser-supports-webp? [request]
  (if-let [vary-string (get-in request [:headers webp-accept-header-name])]
    (.contains vary-string mt/webp-mime-type)))

(defn autodetect-request-format
  [request options]
  (if (browser-supports-webp? request)
    (merge options {:format mt/webp-format})
    options))

(defn route->webp-request-format
  "In case format was not specified in query options, try to set it to webp based on the accept header."
  [request-map request]
  (assoc request-map :requested-format (:format (:options request-map))
                     :options (autodetect-request-format request (:options request-map))))

(defn zone [map]
  (if (and (not (empty? (:zone map)))
           (not (archive? map)))
    (if (.startsWith (:zone map) "/")
      (subs (:zone map) 1)
      (:zone map))
    nil))

(defn route->thumb-map
  [route-params request]
  ; order is important! mostly due to the different options changing :thumbnail-mode
  (let [map (-> route-params
                (assoc :request-type :thumbnail)
                (assoc :thumbnail-mode "thumbnail")
                (route->dimensions)
                (route->offset)
                (route->revision)
                (route->options)
                (route->webp-request-format request))]
    map))

(defn route->original-map
  [route-params request]
  (let [map (-> route-params
                (assoc :request-type :original)
                (route->revision)
                (route->options)
                (route->webp-request-format request))]
    map))

(defn route->timeline-map
  [route-params request]
  (-> route-params
      (assoc :request-type :original)
      (assoc :top-dir "timeline")
      (route->options)
      (route->webp-request-format request)))

(defn route->interactive-maps-map
  [route-params request]
  (-> route-params
      (assoc :request-type :original)
      (assoc :image-type "arbitrary")
      (route->options)
      (route->webp-request-format request)))

(defn route->interactive-maps-thumbnail-map
  [route-params request]
  (-> route-params
      (assoc :request-type :thumbnail)
      (assoc :image-type "arbitrary")
      (route->dimensions)
      (route->offset)
      (route->options)
      (route->webp-request-format request)))


(defn route->revision
  [map]
  (let [revision (if (and (archive? map)
                          (re-matches #"^\d+!.*" (:original map)))
                   (re-find #"^\d+" (:original map))
                   "latest")
        map (assoc map :revision revision)]
    (if (= revision "latest")
      map
      (assoc map :original (clojure.string/replace (:original map) #"^\d+!" "")))))

(defn image->format
  [file]
  (let [[_ ext] (re-find #"(?i)\.([a-z]+)$" file)]
    (when ext
      (.toLowerCase ext))))

(defn route->options
  [map]
  (let [thumb-format (image->format (get map :thumbname ""))
        original-format (image->format (get map :original ""))
        to-format (when (not= thumb-format original-format)
                 thumb-format)
        [_ path-prefix] (re-find #"^/([/a-z0-9-]+)$" (get map :path-prefix ""))
        zone (zone map)
        options (cond-> {}
                     to-format (assoc :format to-format)
                     path-prefix (assoc :path-prefix path-prefix)
                     zone (assoc :zone zone))]
    (assoc map :options options)))

(defn route->dimensions
  [map]
  "Add the :width field to a request map based on the legacy parsing methods."
  (if-let [thumb-dimension (:dimension map)]
    (cond-let
      ; this matches some legacy video requests that attempt to take the width from a frame in the "middle"
      [_ (re-matches #"^mid-.*" thumb-dimension)] (merge map {:width default-width
                                                              :height :auto
                                                              :thumbnail-mode "scale-to-width"})
      [[_ dimension] (re-find #"^(\d+)px-" thumb-dimension)] (merge map {:width dimension
                                                                         :height :auto
                                                                         :thumbnail-mode "scale-to-width"})
      [[_ width height] (re-find #"^(\d+)x(\d+)-" thumb-dimension)] (merge map {:width width
                                                                                :height height
                                                                                :thumbnail-mode "fixed-aspect-ratio"})
      [[_ width height _] (re-find #"^(\d+)x(\d+)x(\d+)-" thumb-dimension)] (merge map {:width width
                                                                                        :height height
                                                                                        :thumbnail-mode "zoom-crop"})
      :else map)
    map))

(defn route->offset
  [map]
  (if-let [[_ x-offset x-end y-offset y-end] (re-find #"^(-{0,1}\d+),(\d+),(-{0,1}\d+),(\d+)-$"
                                                   (URLDecoder/decode (:offset map)))]
    (let [window-width (- (Integer. x-end) (Integer. x-offset))
          window-height (- (Integer. y-end) (Integer. y-offset))]
      (assoc map :thumbnail-mode (if (= (:height map) :auto)
                                   "window-crop"
                                   "window-crop-fixed")
                 :x-offset x-offset
                 :y-offset y-offset
                 :window-width (str window-width)
                 :window-height (str window-height)))
    map))
