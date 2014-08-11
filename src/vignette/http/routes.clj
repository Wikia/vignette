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
            [cheshire.core :refer :all]
            [wikia.common.logger :as log])
  (:import java.io.FileInputStream))

(def wikia-regex #"\w+")
(def top-dir-regex #"\w")
(def middle-dir-regex #"\w\w")
(def original-regex #"[^/]*")
(def adjustment-mode-regex #"\w+")
(def thumbnail-mode-regex #"\w+")
(def size-regex #"\d+")



(def original-route
  (route-compile "/:wikia/:top-dir/:middle-dir/:original"
                 {:wikia wikia-regex
                  :top-dir top-dir-regex
                  :middle-dir middle-dir-regex}))

(def adjust-original-route
  (route-compile "/:wikia/:top-dir/:middle-dir/:original/:mode"
                 {:wikia wikia-regex
                  :top-dir top-dir-regex
                  :middle-dir middle-dir-regex
                  :mode adjustment-mode-regex}))

(def thumbnail-route
  (route-compile "/:wikia/:top-dir/:middle-dir/:original/:thumbnail-mode/:width/:height"
                 {:wikia wikia-regex
                  :top-dir top-dir-regex
                  :middle-dir middle-dir-regex
                  :original original-regex
                  :thumbnail-mode thumbnail-mode-regex
                  :width size-regex
                  :height size-regex}))

(defn exception-catcher
  [handler]
  (fn [request]
    (try
      (handler request)
      (catch Exception e
        (log/warn (str e))
        (status (response (str e)) 503)))))

(defmulti image-file->response-object class)

(defmethod image-file->response-object java.io.File
  [file]
  (FileInputStream. file))


; /lotr/3/35/Arwen.png/resize/10/10?debug=true
(defn app-routes
  [system]
  (-> (routes
        (GET thumbnail-route
             {route-params :route-params query-params :query-params}
             (let [route-params (mt/get-media-map (assoc route-params :request-type :thumbnail))]
               (if-let [thumb (u/get-or-generate-thumbnail system route-params)]
                 (response (image-file->response-object thumb))
                 (not-found "Unable to create thumbnail"))))
        (GET adjust-original-route
             {route-params :route-params query-params :query-params}
             (let [route-params (mt/get-media-map (assoc route-params :request-type :adjust-original))]
               ; FIXME: this needs to be u/reorient-image
               (if-let [thumb (u/get-or-generate-thumbnail system route-params)]
                 (response (image-file->response-object thumb))
                 (not-found "Unable to create thumbnail"))))
        (GET original-route
             {route-params :route-params}
             (let [route-params (mt/get-media-map (assoc route-params :request-type :original))]
               (if-let [file (get-original (store system) route-params )]
                 (response (image-file->response-object file))
                 (not-found "Unable to find image."))))
        (not-found "Unrecognized request path!\n"))
      (wrap-params)
      (exception-catcher)))
