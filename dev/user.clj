(ns user
  (:require (vignette.storage [core :refer (create-local-image-storage)]
                              [local :as vlocal]
                              [protocols :refer :all]
                              [s3 :as vs3])
            (vignette.api.legacy [routes :as alr]
                                 [test :as t])
            (vignette.http [routes :as r])
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

(def sample-media-hash {:wikia "lotr"
                        :top-dir "3"
                        :middle-dir "35"
                        :type "original"
                        :original "arwen.png"})

(def sample-thumbnail-hash {:wikia "lotr"
                            :top-dir "3"
                            :middle-dir "35"
                            :type "thumbnail"
                            :original "arwen.png"
                            :mode "resize"
                            :height "10"
                            :width "10"})

(def los  (vlocal/create-local-object-storage "/tmp/vignette-repl"))
(def lis  (create-local-image-storage los "originals" "thumbs"))

(def S (create-system lis))
(comment
  (start S 8080)
  (stop S))

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
