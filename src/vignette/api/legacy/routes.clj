(ns vignette.api.legacy.routes
  (:require [clout.core :refer [route-compile route-matches]]
            [useful.experimental :refer [cond-let]]
            [vignette.util.regex :refer :all]))

(declare route->revision
         route->dimensions
         route->offset
         route->thumb-mode
         route->options)

(def archive-regex #"\/archive|")
(def lang-regex #"\/[a-z-]+|")
(def dimension-regex #"\d+px-|\d+x\d+-|\d+x\d+x\d+-|")
(def offset-regex #"(?i)\d+,\d+,\d+,\d+-|\d+%2c\d+%2c\d+%2c\d+-|")
(def thumbname-regex #".*?")

(def thumbnail-route
  (route-compile "/:wikia:lang/:image-type/thumb:archive/:top-dir/:middle-dir/:original/:dimension:offset:thumbname"
                 {:wikia wikia-regex
                  :lang lang-regex
                  :image-type #"images|avatars"
                  :archive archive-regex
                  :top-dir top-dir-regex
                  :middle-dir middle-dir-regex
                  :original original-regex
                  :dimension dimension-regex
                  :offset offset-regex
                  :thumbname thumbname-regex}))

(def original-route
  (route-compile "/:wikia:lang/images:archive/:top-dir/:middle-dir/:original"
                 {:wikia wikia-regex
                  :lang lang-regex
                  :archive archive-regex
                  :top-dir top-dir-regex
                  :middle-dir middle-dir-regex
                  :original original-regex}))

(defn route->thumb-map
  [route-params]
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
  (let [[_ format] (re-find #"\.([a-z]+)$" (get map :thumbname ""))
        [_ lang] (re-find #"^/([a-z]+)$" (get map :lang ""))]
    (assoc map :options {:format format
                         :lang lang})))

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

; we currently don't support offsets
(defn route->offset
  [map]
  (if (clojure.string/blank? (:offset map))
    map
    (assoc map :unsupported true)))
