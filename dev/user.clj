(ns user
  (:require (vignette.storage [core :refer :all]
                              [local :as vlocal]
                              [s3 :as vs3])
            (vignette.api.legacy [routes :as alr]
                                 [test :as t])
            (vignette.http [routes :as r])
            (vignette [server :as s]
                      [media-types :as mt])
            [aws.sdk.s3 :as s3]
            [midje.repl :refer :all]
            [clout.core :as c]
            [ring.mock.request :refer :all]
            [cheshire.core :refer :all]
            [schema.core :as schema]
            [schema.macros :as sm]
            [clojure.java.io :as io]
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

(def storage-creds
  {:access-key  (env :storage-access-key)
   :secret-key  (env :storage-secret-key)
   :endpoint    (env :storage-endpoint)} )
