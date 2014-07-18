(ns vignette.http.routes
  (:require (compojure [route :refer (files not-found)]
                       [core :refer  (routes GET ANY)])
            [clout.core :refer (route-compile route-matches)]
            (ring.middleware [params :refer (wrap-params)])
            [cheshire.core :refer :all]))

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
             (-> route-params
                 (assoc :type :thumbnail)
                 (generate-string {:pretty true})))
        (GET original-route
             {route-params :route-params}
             (-> route-params 
                 (assoc :type :original)
                 (generate-string {:pretty true})))
        (not-found "Unrecognized request path!\n"))
      (wrap-params)))
