(ns vignette.core
  (:require [clojure.tools.cli :as cli]
            [vignette.protocols :refer :all]
            [vignette.storage.core :refer [create-image-storage]]
            [vignette.storage.local :refer [create-local-storage-system]]
            [vignette.storage.protocols :refer :all]
            [vignette.storage.s3 :refer [create-s3-storage-system storage-creds]]
            [vignette.system :refer :all]
            [vignette.setup :refer [create-stores]]
            [vignette.util.integration :as i]
            [prometheus.core :as prometheus]
            [vignette.storage.static-assets :as sa])
  (:use [environ.core])
  (:gen-class))

(def cli-specs [["-h" "--help" "Show help"]
                ["-C" "--cache-thumbnails" "Enable thumbnail caching"
                 :default false]
                ["-m" "--mode running mode" "Object storage to use (s3 or local)"
                 :default "local"
                 :validate [#(contains? #{"local" "s3"} %) "Supported modes are \"local\" and \"s3\""]]
                ["-p" "--port listening port" "Run the server on the specified port"
                 :default 8080
                 :parse-fn #(Integer/parseInt %)
                 :validate  [#(< 0 % 0x10000) "Must be a number between 0 and 65536"]]])

(defn -main [& args]
  (let [parsed-opts (cli/parse-opts args cli-specs)
        opts (:options parsed-opts)]
    (when (:help opts)
      (println (:summary parsed-opts))
      (System/exit 0))

    (when (:errors parsed-opts)
      (println (:errors parsed-opts))
      (System/exit 1))

    (let [system (create-system (create-stores opts))]
      (println (format "Mode: %s. Starting server on %d..." (:mode opts) (:port opts)))
      (start system (:port opts)))))
