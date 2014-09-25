(ns vignette.api.legacy.test
  (:require [vignette.api.legacy.routes :refer :all]
            [clout.core :refer (route-matches)]
            [ring.mock.request :refer :all]))

(defn route-map->legacy
  "Converts a route map to a legacy map with the same keys for comparison
  with the perl output."
  [m])

(defn normalize-new
  [m]
  m)

(defn remove-swift
  [s]
  (clojure.string/replace s #"[/]?swift/v1/" ""))

(defn normalize-old
  [m]
  (-> m
    (update-in [:wikia] remove-swift)
    (update-in [:thumbpath] remove-swift)))

(defn fields-equal?
  [new-map old-map field]
  (= (get new-map field)
     (get old-map field)))

(defn key-fields-equal?
  [new-map old-map]
  (map (partial fields-equal?
                (normalize-new new-map)
                (normalize-old old-map))
       [:thumbnail :thumbpath :wikia :width]))

(defn match
  [url]
  ; when we implement others we need to chain them in here in order
  (when-let [m (route-matches image-thumbnail (request :get url))]
    (-> m
        (add-request-type :image-thumbnail)
        (request-map-add-thumbpath)
        (route->dimensions))))

