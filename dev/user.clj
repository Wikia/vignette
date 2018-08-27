(ns user
  (:require [aws.sdk.s3 :as s3]
            [cheshire.core :refer :all]
            [clojure.java.io :as io]
            [clojure.java.shell :refer [sh]]
            [clojure.tools.namespace.repl :as nrepl]
            [clojure.tools.trace :refer :all]
            [clout.core :as c]
            [midje.repl :refer :all]
            [pantomime.mime :refer [mime-type-of]]
            [ring.mock.request :refer :all]
            [vignette.http.legacy.routes :as hlr]
            [vignette.http.routes :as r]
            [vignette.media-types :as mt]
            [vignette.protocols :refer :all]
            [vignette.storage.core :refer [create-image-storage]]
            [vignette.storage.local :refer [create-local-storage-system]]
            [vignette.storage.s3 :refer [create-s3-storage-system storage-creds]]
            [vignette.system :refer :all]
            [vignette.util.filesystem :as fs]
            [vignette.util.integration :as itg]
            [vignette.util.thumbnail :as u]
            [vignette.util.query-options :as q]
            [vignette.setup :refer [create-stores]]
            [vignette.common.logger :as log]
            [vignette.perfmonitoring.core :as perf]
            [vignette.storage.static-assets :as sa])
  (:use [environ.core]))

(def sample-original-hash {:wikia        "bucket"
                           :top-dir      "a"
                           :middle-dir   "ab"
                           :request-type :original
                           :original     "ropes.jpg"})

(def sample-thumbnail-hash {:wikia        "bucket"
                            :top-dir      "a"
                            :middle-dir   "ab"
                            :request-type :thumbnail
                            :original     "ropes.jpg"
                            :mode         "resize"
                            :height       "10"
                            :width        "10"})

(def los (create-local-storage-system itg/integration-path))
(def lis (create-image-storage los))

(def system-local (create-stores lis))

(def s3os (create-s3-storage-system storage-creds))
(def s3s (create-image-storage s3os))

(def system-s3 (create-system (create-stores s3s)))

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
  ([s port]
   (do
     (stop s)
     (start s port)))
  ([]
   (re-init-dev system-local 8080)))


(defn mime-stats [path]
  (defn benchmark [file]
    (let [start (System/nanoTime)]
      (mime-type-of file)
      (- (System/nanoTime) start)))
  (when-let [dir (clojure.java.io/file path)]
    (loop [files (file-seq dir)
           time-total 0
           mime-count 0]
      (if-let [file (first files)]
        (if (not (.isDirectory file))
          (recur (rest files) (+ (benchmark file) time-total) (inc mime-count))
          (recur (rest files) time-total mime-count))
        (println (clojure.string/join "\n" [(str "file count: " mime-count)
                                            (str "total time (ns): " time-total)
                                            (str "average (ns): " (float (/ time-total mime-count)))
                                            (str "average (ms): " (* 0.000001 (/ time-total mime-count)))]))))))
