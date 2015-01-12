(ns vignette.api.legacy.routes
  (:require [clout.core :refer [route-compile route-matches]]
            [useful.experimental :refer [cond-let]]
            [vignette.util.regex :refer :all])
  (:import [java.net URLDecoder]))

(declare route->revision
         route->dimensions
         route->offset
         route->thumb-mode
         route->options)

(def archive-regex #"\/archive|")
(def path-prefix-regex #"\/[/a-z-]+|")
(def dimension-regex #"\d+px-|\d+x\d+-|\d+x\d+x\d+-|")
(def offset-regex #"(?i)-{0,1}\d+,\d+,-{0,1}\d+,\d+-|-{0,1}\d+%2c\d+%2c-{0,1}\d+%2c\d+-|")
(def thumbname-regex #".*?")
(def video-params-regex #"(?i)v,\d{6},|v%2c\d{6}%2c|")

(def thumbnail-route
  (route-compile "/:wikia:path-prefix/:image-type/thumb:archive/:top-dir/:middle-dir/:original/:videoparams:dimension:offset:thumbname"
                 {:wikia wikia-regex
                  :path-prefix path-prefix-regex
                  :image-type #"images|avatars"
                  :archive archive-regex
                  :top-dir top-dir-regex
                  :middle-dir middle-dir-regex
                  :original original-regex
                  :videoparams video-params-regex
                  :dimension dimension-regex
                  :offset offset-regex
                  :thumbname thumbname-regex}))

(def original-route
  (route-compile "/:wikia:path-prefix/:image-type:archive/:top-dir/:middle-dir/:original"
                 {:wikia wikia-regex
                  :path-prefix path-prefix-regex
                  :image-type #"images|avatars"
                  :archive archive-regex
                  :top-dir top-dir-regex
                  :middle-dir middle-dir-regex
                  :original original-regex}))

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

(defn route->revision
  [map]
  (let [revision (if (and (not= (:archive map) "")
                          (re-matches #"^\d+!.*" (:original map)))
                   (re-find #"^\d+" (:original map))
                   "latest")
        map (assoc map :revision revision)]
    (if (= revision "latest")
      map
      (assoc map :original (clojure.string/replace (:original map) #"^\d+!" "")))))

(defn route->options
  [map]
  (let [[_ format] (re-find #"(?i)\.([a-z]+)$" (get map :thumbname ""))
        [_ path-prefix] (re-find #"^/([/a-z]+)$" (get map :path-prefix ""))
        options {}
        options (if format (assoc options :format (.toLowerCase format)) options)
        options (if path-prefix (assoc options :path-prefix path-prefix) options)]
    (assoc map :options options)))

(defn route->dimensions
  [map]
  "Add the :width field to a request map based on the legacy parsing methods."
  (if-let [thumb-dimension (:dimension map)]
    (cond-let
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
