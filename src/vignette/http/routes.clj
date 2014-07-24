(ns vignette.http.routes
  (:require (vignette.storage [protocols :refer :all]
                              [core :refer :all])
            [vignette.util.thumbnail :as u]
            [vignette.media-types :as mt]
            [vignette.protocols :refer :all]
            (compojure [route :refer (files not-found)]
                       [core :refer  (routes GET ANY)])
            [clout.core :refer (route-compile route-matches)]
            [ring.util.response :refer (response status charset header)]
            (ring.middleware [params :refer (wrap-params)])
            [cheshire.core :refer :all])
  (:import java.io.FileInputStream))

(def wikia-regex #"\w+")
(def top-dir-regex #"\d")
(def middle-dir-regex #"\d\d")
(def original-regex #"[^/]*")
(def mode-regex #"\w+")
(def size-regex #"\d+")



(def original-route
  (route-compile "/:wikia/:top-dir/:middle-dir/:original"
                 {:wikia wikia-regex
                  :top-dir top-dir-regex
                  :middle-dir middle-dir-regex}))

(def thumbnail-route
  (route-compile "/:wikia/:top-dir/:middle-dir/:original/:mode/:width/:height"
                 {:wikia wikia-regex
                  :top-dir top-dir-regex
                  :middle-dir middle-dir-regex
                  :original original-regex
                  :mode mode-regex
                  :width size-regex
                  :height size-regex}))

; /lotr/3/35/Arwen.png/resize/10/10?debug=true
(defn app-routes
  [system]
  (-> (routes
        (GET thumbnail-route
             {route-params :route-params query-params :query-params}
             (let [route-params (mt/get-media-map (assoc route-params :type "thumbnail"))]
               (if-let [thumb (u/get-thumbnail system route-params)]
                 (response (FileInputStream. thumb))
                 (not-found "Unable to create thumbnail"))))
        (GET original-route
             {route-params :route-params}
             (let [route-params (mt/get-media-map (assoc route-params :type :original))]
               (if-let [file (get-original (store system) route-params )]
                 (response (FileInputStream. file))
                 (not-found " Unable to find image."))))
        (not-found "Unrecognized request path!\n"))
      (wrap-params)))
