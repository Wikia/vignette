(ns vignette.http.legacy.routes
  (:require [clout.core :refer [route-compile route-matches]]
            [compojure.core :refer [routes GET]]
            [useful.experimental :refer [cond-let]]
            [vignette.media-types :refer [archive-dir]]
            [vignette.protocols :refer :all]
            [vignette.storage.protocols :refer :all]
            [vignette.util.regex :refer :all]
            [vignette.util.external-hotlinking :refer :all]
            [vignette.util.image-response :refer :all]
            [vignette.util.thumbnail :as u])
  (:import [java.net URLDecoder]))

(def default-width 200)

(declare route->revision
         route->dimensions
         route->offset
         route->thumb-mode
         route->options
         route->thumb-map
         route->original-map
         route->interactive-maps-map
         route->interactive-maps-thumbnail-map
         route->timeline-map
         original-request->file)

(def path-prefix-regex #"\/[/a-z-]+|")
(def dimension-regex #"\d+px-|\d+x\d+-|\d+x\d+x\d+-|mid-|")
(def offset-regex #"(?i)-{0,1}\d+,\d+,-{0,1}\d+,\d+-|-{0,1}\d+%2c\d+%2c-{0,1}\d+%2c\d+-|")
(def thumbname-regex #".*?")
(def video-params-regex #"(?i)v,\d{6},|v%2c\d{6}%2c|")
(def zone-regex #"\/[a-z]+|") ; includes "archive"
(def interactive-maps-regex #"intmap_tile_set_\d+")
(def interactive-maps-marker-regex #"intmap_markers_\d+")

(def thumbnail-route
  (route-compile "/:wikia:path-prefix/:image-type/thumb:zone/:top-dir/:middle-dir/:original/:videoparams:dimension:offset:thumbname"
                 {:wikia wikia-regex
                  :path-prefix path-prefix-regex
                  :image-type #"images|avatars"
                  :zone zone-regex
                  :top-dir top-dir-regex
                  :middle-dir middle-dir-regex
                  :original original-regex
                  :videoparams video-params-regex
                  :dimension dimension-regex
                  :offset offset-regex
                  :thumbname thumbname-regex}))

(def original-route
  (route-compile "/:wikia:path-prefix/:image-type:zone/:top-dir/:middle-dir/:original"
                 {:wikia wikia-regex
                  :path-prefix path-prefix-regex
                  :image-type #"images|avatars"
                  :zone zone-regex
                  :top-dir top-dir-regex
                  :middle-dir middle-dir-regex
                  :original original-regex}))

(def timeline-route
  (route-compile "/:wikia:path-prefix/:image-type/timeline/:original"
                 {:wikia wikia-regex
                  :path-prefix path-prefix-regex
                  :image-type #"images"
                  :original original-regex}))

(def math-route
  (route-compile "/:wikia:path-prefix/:image-type/:zone/:top-dir/:middle-dir/:original"
                 {:wikia wikia-regex
                  :path-prefix path-prefix-regex
                  :image-type "images"
                  :zone "math"
                  :top-dir top-dir-regex
                  :middle-dir #"\w\/\w"
                  :original original-regex}))

(def interactive-maps-route
  (route-compile "/:wikia:path-prefix/:original"
                 {:wikia interactive-maps-regex
                  :path-prefix #"/\d+/\d+|"
                  :original original-regex}))

(def interactive-maps-thumbnail-route
  (route-compile "/:wikia/thumb/:original/:dimension:offset:thumbname"
                 {:wikia interactive-maps-regex
                  :original original-regex
                  :dimension dimension-regex
                  :offset offset-regex
                  :thumbname thumbname-regex}))

(def interactive-maps-marker-route
  (route-compile "/:wikia/:original"
                 {:wikia interactive-maps-marker-regex
                  :original original-regex}))

(defn legacy-routes
  [system]
  [(GET thumbnail-route
         request
         (let [image-params (route->thumb-map (:route-params request))]
           (if-let [thumb (u/get-or-generate-thumbnail system image-params)]
             (create-image-response thumb image-params)
             (error-response 404 image-params))))
   (GET original-route
        request
        (let [image-params (route->original-map (:route-params request))]
          (if-let [file (original-request->file request system image-params)]
            (create-image-response file image-params)
            (error-response 404 image-params))))
   (GET timeline-route
        request
        (let [image-params (route->timeline-map (:route-params request))]
          (if-let [file (original-request->file request system image-params)]
            (create-image-response file image-params)
            (error-response 404 image-params))))
   (GET math-route
        request
        (let [image-params (route->original-map (:route-params request))]
          (if-let [file (original-request->file request system image-params)]
            (create-image-response file image-params)
            (error-response 404 image-params))))
   (GET interactive-maps-route
        request
        (let [image-params (route->interactive-maps-map (:route-params request))]
          (if-let [file (original-request->file request system image-params)]
            (create-image-response file image-params)
            (error-response 404 image-params))))
   (GET interactive-maps-marker-route
        request
        (let [image-params (route->interactive-maps-map (:route-params request))]
          (if-let [file (original-request->file request system image-params)]
            (create-image-response file image-params)
            (error-response 404 image-params))))
   (GET interactive-maps-thumbnail-route
        request
        (let [image-params (route->interactive-maps-thumbnail-map (:route-params request))]
          (if-let [thumb (u/get-or-generate-thumbnail system image-params)]
            (create-image-response thumb image-params)
            (error-response 404 image-params))))])



(defn archive? [map]
  (= (:zone map) (str "/" archive-dir)))

(defn zone [map]
  (if (and (not (empty? (:zone map)))
           (not (archive? map)))
    (if (.startsWith (:zone map) "/")
      (subs (:zone map) 1)
      (:zone map))
    nil))

(defn route->thumb-map
  [route-params]
  ; order is important! mostly due to the different options changing :thumbnail-mode
  (let [map (-> route-params
                (assoc :request-type :thumbnail)
                (assoc :thumbnail-mode "thumbnail")
                (route->dimensions)
                (route->offset)
                (route->revision)
                (route->options))]
    map))

(defn route->original-map
  [route-params]
  (let [map (-> route-params
                (assoc :request-type :original)
                (route->revision)
                (route->options))]
    map))

(defn route->timeline-map
  [route-params]
  (-> route-params
      (assoc :request-type :original)
      (assoc :top-dir "timeline")
      (route->options)))

(defn route->interactive-maps-map
  [route-params]
  (-> route-params
      (assoc :request-type :original)
      (assoc :image-type "arbitrary")
      (route->options)))

(defn route->interactive-maps-thumbnail-map
  [route-params]
  (-> route-params
      (assoc :request-type :thumbnail)
      (assoc :image-type "arbitrary")
      (route->dimensions)
      (route->offset)
      (route->options)))


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

(defn original-request->file
  [request system image-params]
  (if (force-thumb? request)
    (u/get-or-generate-thumbnail system (image-params->forced-thumb-params image-params))
    (get-original (store system) image-params)))
