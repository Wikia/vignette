(ns vignette.http.legacy.routes
  (:require [clout.core :refer [route-compile route-matches]]
            [compojure.core :refer [routes GET]]
            [vignette.http.legacy.route-helpers :refer :all]
            [vignette.protocols :refer :all]
            [vignette.storage.protocols :refer :all]
            [vignette.util.external-hotlinking :refer [original-request->file]]
            [vignette.util.regex :refer :all]
            [vignette.util.image-response :refer :all]
            [vignette.util.thumbnail :as u]))

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
  [store]
  [(GET thumbnail-route
         request
         (let [image-params (route->thumb-map (:route-params request) request)]
           (if-let [thumb (u/get-or-generate-thumbnail store image-params)]
             (create-image-response thumb image-params)
             (error-response 404 image-params))))
   (GET original-route
        request
        (let [image-params (route->original-map (:route-params request) request)]
          (if-let [file (original-request->file request store image-params)]
            (create-image-response file image-params)
            (error-response 404 image-params))))
   (GET timeline-route
        request
        (let [image-params (route->timeline-map (:route-params request) request)]
          (if-let [file (original-request->file request store image-params)]
            (create-image-response file image-params)
            (error-response 404 image-params))))
   (GET math-route
        request
        (let [image-params (route->original-map (:route-params request) request)]
          (if-let [file (original-request->file request store image-params)]
            (create-image-response file image-params)
            (error-response 404 image-params))))
   (GET interactive-maps-route
        request
        (let [image-params (route->interactive-maps-map (:route-params request) request)]
          (if-let [file (original-request->file request store image-params)]
            (create-image-response file image-params)
            (error-response 404 image-params))))
   (GET interactive-maps-marker-route
        request
        (let [image-params (route->interactive-maps-map (:route-params request) request)]
          (if-let [file (original-request->file request store image-params)]
            (create-image-response file image-params)
            (error-response 404 image-params))))
   (GET interactive-maps-thumbnail-route
        request
        (let [image-params (route->interactive-maps-thumbnail-map (:route-params request) request)]
          (if-let [thumb (u/get-or-generate-thumbnail store image-params)]
            (create-image-response thumb image-params)
            (error-response 404 image-params))))])
