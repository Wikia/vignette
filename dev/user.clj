(ns user
  (:require (vignette.storage [core :refer (create-image-storage)]
                              [local :as vlocal]
                              [protocols :refer :all]
                              [s3 :as vs3])
            (vignette.api.legacy [routes :as alr]
                                 [test :as t])
            (vignette.http [routes :as r])
            (vignette.util [integration :as itg]
                           [thumbnail :as u]
                           [byte-streams :as bs]
                           [filesystem :as fs])
            (vignette [server :as s]
                      [protocols :refer :all]
                      [media-types :as mt]
                      [system :refer :all])
            [wikia.common.logger :as log]
            [aws.sdk.s3 :as s3]
            [midje.repl :refer :all]
            [clout.core :as c]
            [ring.mock.request :refer :all]
            [cheshire.core :refer :all]
            [clojure.tools.trace :refer :all]
            [clojure.java.io :as io]
            [clojure.tools.namespace.repl :as nrepl]
            [clojure.java.shell :refer (sh)])
  (:use [environ.core]))

(def sample-original-hash {:wikia "bucket"
                           :top-dir "a"
                           :middle-dir "ab"
                           :request-type :original
                           :original "ropes.jpg"})

(def sample-thumbnail-hash {:wikia "bucket"
                            :top-dir "a"
                            :middle-dir "ab"
                            :request-type :thumbnail
                            :original "ropes.jpg"
                            :mode "resize"
                            :height "10"
                            :width "10"})


(def storage-creds
  {:access-key  (env :storage-access-key)
   :secret-key  (env :storage-secret-key)
   :endpoint    (env :storage-endpoint)} )

(def los  (vlocal/create-local-object-storage itg/integration-path))
(def lis  (create-image-storage los))

(def S (create-system lis))

(def s3os  (vs3/create-s3-object-storage storage-creds))
(def s3s   (create-image-storage s3os "images" "images/thumb"))

(def Ss (create-system s3s))

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
