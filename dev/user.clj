(ns user
  (:require (vignette.storage [core :refer :all]
                              [local :as vlocal]
                              [s3 :as vs3])
    [aws.sdk.s3 :as s3]
            [midje.repl :refer :all]
            [clojure.java.io :as io]
            [clojure.java.shell :refer (sh)])
  (:use [environ.core]))


(def storage-creds
  {:access-key  (env :storage-access-key)
   :secret-key  (env :storage-secret-key)
   :endpoint    (env :storage-endpoint)} )
