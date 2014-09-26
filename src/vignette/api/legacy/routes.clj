(ns vignette.api.legacy.routes
  (:require [clout.core :refer (route-compile route-matches)]))

(declare route->revision
         route->dimensions
         route->thumb-mode
         route->options)

(def thumbnail-route
  (route-compile "/:wikia:lang/:image-type/thumb:archive/:top-dir/:middle-dir/:original/:thumbname"
                 {:wikia #"[\w-]+"
                  :lang #".*"
                  :image-type #"images|avatars"
                  :archive #"(?!\/archive).*|\/archive"
                  :top-dir #"\w"
                  :middle-dir #"\w\w"
                  :original #"[^/]*"
                  :thumbname #".*"}))

(def original-route
  (route-compile "/:wikia:lang/images:archive/:top-dir/:middle-dir/:original"
                 {:wikia #"[\w-]+"
                  :lang #".*"
                  :archive #"(?!\/archive).*|\/archive"
                  :top-dir #"\w"
                  :middle-dir #"\w\w"
                  :original #".*"}))

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
