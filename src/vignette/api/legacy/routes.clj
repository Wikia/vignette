(ns vignette.api.legacy.routes
  (:require [vignette.util.regex :refer :all]
            [clout.core :refer (route-compile route-matches)]
            [useful.experimental :refer (cond-let)]))

(declare route->revision
         route->dimensions
         route->thumb-mode
         route->options)

(def archive-regex #"(?!\/archive).*|\/archive")
(def lang-regex #"\/\w\w|")

(def thumbnail-route
  (route-compile "/:wikia:lang/:image-type/thumb:archive/:top-dir/:middle-dir/:original/:thumbname"
                 {:wikia wikia-regex
                  :lang lang-regex
                  :image-type #"images|avatars"
                  :archive archive-regex
                  :top-dir top-dir-regex
                  :middle-dir middle-dir-regex
                  :original original-regex
                  :thumbname original-regex}))

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
  [route]
  "Add the :width field to a request map based on the legacy parsing methods."
  (if-let [thumb-name (:thumbname route)]
    (cond-let
      [[_ dimension] (re-find #"^(\d+)px-" thumb-name)] (merge route {:width dimension
                                                                      :height dimension})
      [[_ width height] (re-find #"^(\d+)x(\d+)-" thumb-name)] (merge route {:width width
                                                                             :height height
                                                                             :thumbnail-mode "fixed-aspect-ratio"})
      [[_ width height _] (re-find #"^(\d+)x(\d+)x(\d+)-" thumb-name)] (merge route {:width width
                                                                                     :height height
                                                                                     :thumbnail-mode "zoom-crop"})
      :else route)
    route))
