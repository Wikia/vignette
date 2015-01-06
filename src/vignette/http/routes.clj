(ns vignette.http.routes
  (:require [cheshire.core :refer :all]
            [clojure.java.io :as io]
            [clout.core :refer [route-compile route-matches]]
            [compojure.core :refer [routes GET ANY]]
            [compojure.route :refer [files not-found]]
            [environ.core :refer [env]]
            [ring.middleware.params :refer [wrap-params]]
            [ring.util.response :refer [response status charset header]]
            [slingshot.slingshot :refer [try+ throw+]]
            [vignette.api.legacy.routes :as alr]
            [vignette.http.middleware :refer :all]
            [vignette.protocols :refer :all]
            [vignette.storage.core :refer :all]
            [vignette.storage.protocols :refer :all]
            [vignette.util.image-response :refer :all]
            [vignette.util.query-options :refer :all]
            [vignette.util.regex :refer :all]
            [vignette.util.thumbnail :as u]))

(def revisionless-original-route
  (route-compile "/:wikia:image-type/:top-dir/:middle-dir/:original"
                 {:wikia wikia-regex
                  :image-type image-type-regex
                  :top-dir top-dir-regex
                  :middle-dir middle-dir-regex
                  :original original-regex}))

(def original-route
  (route-compile "/:wikia:image-type/:top-dir/:middle-dir/:original/revision/:revision"
                 {:wikia wikia-regex
                  :image-type image-type-regex
                  :top-dir top-dir-regex
                  :middle-dir middle-dir-regex
                  :original original-regex
                  :revision revision-regex}))

(def thumbnail-route
  (route-compile "/:wikia:image-type/:top-dir/:middle-dir/:original/revision/:revision/:thumbnail-mode/width/:width/height/:height"
                 {:wikia wikia-regex
                  :image-type image-type-regex
                  :top-dir top-dir-regex
                  :middle-dir middle-dir-regex
                  :original original-regex
                  :revision revision-regex
                  :thumbnail-mode thumbnail-mode-regex
                  :width size-regex
                  :height size-regex}))

(def window-crop-route
  (route-compile "/:wikia:image-type/:top-dir/:middle-dir/:original/revision/:revision/window-crop/width/:width/x-offset/:x-offset/y-offset/:y-offset/window-width/:window-width/window-height/:window-height"
                 {:wikia wikia-regex
                  :image-type image-type-regex
                  :top-dir top-dir-regex
                  :middle-dir middle-dir-regex
                  :original original-regex
                  :revision revision-regex
                  :width size-regex
                  :x-offset size-regex
                  :window-width size-regex
                  :y-offset size-regex
                  :window-height size-regex}))

(def window-crop-fixed-route
  (route-compile "/:wikia:image-type/:top-dir/:middle-dir/:original/revision/:revision/window-crop-fixed/width/:width/height/:height/x-offset/:x-offset/y-offset/:y-offset/window-width/:window-width/window-height/:window-height"
                 {:wikia wikia-regex
                  :image-type image-type-regex
                  :top-dir top-dir-regex
                  :middle-dir middle-dir-regex
                  :original original-regex
                  :revision revision-regex
                  :width size-regex
                  :height size-regex
                  :x-offset size-regex
                  :window-width size-regex
                  :y-offset size-regex
                  :window-height size-regex}))

(def scale-to-width-route
  (route-compile "/:wikia:image-type/:top-dir/:middle-dir/:original/revision/:revision/scale-to-width/:width"
                 {:wikia wikia-regex
                  :image-type image-type-regex
                  :top-dir top-dir-regex
                  :middle-dir middle-dir-regex
                  :original original-regex
                  :revision revision-regex
                  :width size-regex}))

(declare image-request-handler)

; /lotr/3/35/Arwen.png/resize/10/10?debug=true
(defn app-routes
  [system]
  (-> (routes
        (GET scale-to-width-route
             request
             (image-request-handler system :thumbnail request
                                    :thumbnail-mode "scale-to-width"
                                    :height :auto))
        (GET window-crop-route
             request
             (image-request-handler system :thumbnail request
                                    :thumbnail-mode "window-crop"
                                    :height :auto))
        (GET window-crop-fixed-route
             request
             (image-request-handler system :thumbnail request
                                    :thumbnail-mode "window-crop-fixed"
                                    :height :auto))
        (GET thumbnail-route
             request
             (image-request-handler system :thumbnail request))
        (GET revisionless-original-route
             request
             (image-request-handler system :original request))
        (GET original-route
             request
             (image-request-handler system :original request))

        ; legacy routes
        (GET alr/thumbnail-route
             request
             (let [image-params (alr/route->thumb-map (:route-params request))]
               (if (:unsupported image-params)
                 (-> (response "unsupported thumbnail route")
                     (status 307)
                     (header "Location" (str (env :unsupported-redirect-host "http://images.wikia.com")
                                             (:uri request))))
                 (if-let [thumb (u/get-or-generate-thumbnail system image-params)]
                   (create-image-response thumb)
                   (error-response 404 image-params)))))
        (GET alr/original-route
             {route-params :route-params}
             (let [image-params (alr/route->original-map route-params)]
               (if-let [file (get-original (store system) image-params)]
                 (create-image-response file)
                 (error-response 404 image-params))))
        (GET "/ping" [] "pong")
        (files "/static/")
        (not-found "Unrecognized request path!\n"))
      (request-timer)
      (wrap-params)
      (exception-catcher)
      (add-headers)))

(declare handle-thumbnail
         handle-original
         image-params
         route-params->image-type)

(defn image-request-handler
  [system request-type request &{:keys [thumbnail-mode height] :or {thumbnail-mode nil height nil} :as params}]
  (let [image-params (image-params request request-type)
        image-params (if params (merge image-params params) image-params)]
    (condp = request-type
      :thumbnail (handle-thumbnail system image-params)
      :original (handle-original system image-params))))

(defn handle-thumbnail
  [system image-params]
  (if-let [thumb (u/get-or-generate-thumbnail system image-params)]
    (create-image-response thumb)
    (error-response 404 image-params)))

(defn handle-original
  [system image-params]
  (if-let [file (get-original (store system) image-params)]
    (create-image-response file)
    (error-response 404 image-params)))

(defn image-params
  [request request-type]
  (let [route-params (assoc (:route-params request) :request-type request-type)
        options (extract-query-opts request)]
    (assoc route-params :options options
                        :image-type (route-params->image-type route-params))))

(defn route-params->image-type
  [route-params]
  (if (clojure.string/blank? (:image-type route-params))
    "images"
    (clojure.string/replace (:image-type route-params)
                            #"^\/(.*)"
                            "$1")))
