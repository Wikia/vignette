(ns user
  (:require (vignette.storage [core :refer (create-local-image-storage)]
                              [local :as vlocal]
                              [protocols :refer :all]
                              [s3 :as vs3])
            (vignette.api.legacy [routes :as alr]
                                 [test :as t])
            (vignette.http [routes :as r])
            (vignette.util [integration :as itg])
            (vignette [server :as s]
                      [protocols :refer :all]
                      [media-types :as mt]
                      [system :refer :all])
            [vignette.util.thumbnail :as u]
            [aws.sdk.s3 :as s3]
            [midje.repl :refer :all]
            [clout.core :as c]
            [ring.mock.request :refer :all]
            [cheshire.core :refer :all]
            [schema.core :as schema]
            [schema.macros :as sm]
            [clojure.tools.trace :refer :all]
            [clojure.java.io :as io]
            [clojure.tools.namespace.repl :as nrepl]
            [clojure.java.shell :refer (sh)])
  (:use [environ.core]))

(def sample-original-hash {:wikia "bucket"
                        :top-dir "a"
                        :middle-dir "ab"
                        :type "original"
                        :original "ropes.jpg"})

(def sample-thumbnail-hash {:wikia "bucket"
                            :top-dir "a"
                            :middle-dir "ab"
                            :type "thumbnail"
                            :original "ropes.jpg"
                            :mode "resize"
                            :height "10"
                            :width "10"})

(def los  (vlocal/create-local-object-storage itg/integration-path))
(def lis  (create-local-image-storage los))

(def S (create-system lis))

(comment
  (start S 8080)
  (stop S))

(defn reload-repl
  []
  (nrepl/set-refresh-dirs "src")
  (nrepl/refresh)
  (clojure.core/use '[clojure.core])
  (use '[clojure.repl])
  ;(load-file "dev/user.clj")
  )

(defn re-init-dev
  ([port]
   (do
     (stop S)
     (nrepl/refresh)
     (clojure.core/use '[clojure.core])
     (use '[clojure.repl])
     (load-file "dev/user.clj")
     (start S port)))
  ([]
   (re-init-dev 8080)))

(def storage-creds
  {:access-key  (env :storage-access-key)
   :secret-key  (env :storage-secret-key)
   :endpoint    (env :storage-endpoint)} )

(comment
  "Experimentation with prismatic/schema."
  (def MediaFile
    {:type String
     :original String
     :middle-dir String
     :top-dir String
     :wikia String})

 (def MediaThumbnailFile
   (merge MediaFile
          {:mode String
           :height Long
           :width Long})))
