(ns vignette.util.integration
  (:require (vignette.storage [core :refer (create-local-image-storage)]
                              [local :refer (create-local-object-storage)]
                              [protocols :refer :all]
                              [s3 :as vs3])
            [aws.sdk.s3 :as s3]
            [clojure.java.io :as io]
            [clojure.java.shell :refer (sh)])
  (:use [environ.core]))

(def integration-path (env :vignette-integration-root "/tmp/integration"))

(def default-map {:wikia "bucket"
                  :top-dir "a"
                  :middle-dir "ab"
                  :type "original"})

(defn get-sample-image-maps
  []
  (let [sample-files (rest (file-seq (io/file "image-samples")))]
    (map #(merge default-map {:file-on-disk %
                              :original (clojure.string/replace % #"image-samples/" "")})
         sample-files)))

(defn create-integration-env
  ([path]
   (let [local-store (create-local-object-storage path)
        image-store (create-local-image-storage local-store)]
    (every? true? (map #(save-original image-store (:file-on-disk %) %)
                       (get-sample-image-maps)))))
  ([]
   (create-integration-env integration-path)))

(defn setup-integration
  ([image-store]
   (map ()))
  ([]
   (setup-integration (create-integration-env))))
