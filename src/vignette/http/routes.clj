(ns vignette.http.routes
  (:require (vignette.storage [protocols :refer :all]
                              [core :refer :all]
                              [common :refer :all])
            [vignette.util.thumbnail :as u]
            [vignette.media-types :as mt]
            [vignette.protocols :refer :all]
            [vignette.util.query-options :refer :all]
            (compojure [route :refer (files not-found)]
                       [core :refer  (routes GET ANY)])
            [clout.core :refer (route-compile route-matches)]
            [ring.util.response :refer (response status charset header)]
            (ring.middleware [params :refer (wrap-params)])
            [cheshire.core :refer :all]
            [slingshot.slingshot :refer (try+ throw+)]
            [wikia.common.logger :as log]
            [clojure.java.io :as io])
  (:import [java.io FileInputStream FileInputStream]
           [java.nio ByteBuffer]
           [java.net InetAddress]))

(def revision-regex #"\d+|latest")
(def wikia-regex #"\w+")
(def top-dir-regex #"\w")
(def middle-dir-regex #"\w\w")
(def original-regex #"[^/]*")
(def adjustment-mode-regex #"\w+")
(def thumbnail-mode-regex #"[\w-]+")
(def size-regex #"\d+")
(def hostname (.getHostName (InetAddress/getLocalHost)))



(def original-route
  (route-compile "/:wikia/:top-dir/:middle-dir/:original/revision/:revision"
                 {:wikia wikia-regex
                  :top-dir top-dir-regex
                  :middle-dir middle-dir-regex
                  :revision revision-regex}))

(def adjust-original-route
  (route-compile "/:wikia/:top-dir/:middle-dir/:original/revision/:revision/:mode"
                 {:wikia wikia-regex
                  :top-dir top-dir-regex
                  :middle-dir middle-dir-regex
                  :revision revision-regex
                  :mode adjustment-mode-regex}))

(def thumbnail-route
  (route-compile "/:wikia/:top-dir/:middle-dir/:original/revision/:revision/:thumbnail-mode/width/:width/height/:height"
                 {:wikia wikia-regex
                  :top-dir top-dir-regex
                  :middle-dir middle-dir-regex
                  :original original-regex
                  :revision revision-regex
                  :thumbnail-mode thumbnail-mode-regex
                  :width size-regex
                  :height size-regex}))

(defn exception-catcher
  [handler]
  (fn [request]
    (try+
      (handler request)
      (catch [:type :vignette.util.thumbnail/convert-error] {:keys [exit out err]}
        (log/warn "thumbnailing failed" {:code exit :out out :err err})
        (status (response "thumbnailing error") 500))
      (catch Exception e
        (log/warn (str e))
        (status (response "Internal Error. Check the logs.") 500)))))

(defn add-headers
  [handler]
  (fn [request]
    (let [response (handler request)]
      (reduce (fn [response [h v]]
                (header response h v))
              response {"X-Served-By" hostname
                        "X-Cache" "ORIGIN"
                        "X-Cache-Hits" "ORIGIN"}))))

(defmulti image-file->response-object
  "Convert an image file object to something that http-kit can understand. The types supported
  can be found in the httpkit::HttpUtils/bodyBuffer."
  (comp class file-stream))

(defmethod image-file->response-object java.io.File
  [object]
  (FileInputStream. (file-stream object)))

(defmethod image-file->response-object (Class/forName "[B")
  [object]
  (ByteBuffer/wrap (file-stream object)))

(defmethod image-file->response-object :default
  [object]
  (file-stream object))

(defn create-image-response
  [image]
  (-> (response (image-file->response-object image))
      (header "Content-Type" (content-type image))))

(defn image-params
  [request request-type]
  (let [route-params (assoc (:route-params request) :request-type request-type)
        options (extract-query-opts request)]
    (assoc route-params :options options)))

; /lotr/3/35/Arwen.png/resize/10/10?debug=true
(defn app-routes
  [system]
  (-> (routes
        (GET thumbnail-route
             request
             (let [image-params (image-params request :thumbnail)]
               (if-let [thumb (u/get-or-generate-thumbnail system image-params)]
                 (create-image-response thumb)
                 (not-found "Unable to create thumbnail"))))
        (GET adjust-original-route
             {route-params :route-params}
             (let [route-params (assoc route-params :request-type :adjust-original)]
               ; FIXME: this needs to be u/reorient-image
               (if-let [image (u/get-or-generate-thumbnail system route-params)]
                 (create-image-response image)
                 (not-found "Unable to create thumbnail"))))
        (GET original-route
             {route-params :route-params}
             (let [route-params (assoc route-params :request-type :original)]
               (if-let [file (get-original (store system) route-params )]
                 (create-image-response file)
                 (not-found "Unable to find image."))))
        (files "/static/")
        (not-found "Unrecognized request path!\n"))
      (wrap-params)
      (exception-catcher)
      (add-headers)))
