(ns vignette.core
  (:require [clojure.tools.cli :as cli]
            [vignette.util.integration :as i]
            (vignette.storage [core :refer (create-image-storage)]
                              [local :refer (create-local-object-storage)]
                              [s3 :refer (create-s3-object-storage storage-creds)]
                              [protocols :refer :all])
            (vignette [server :as s]
                      [protocols :refer :all]
                      [system :refer :all]))
  (:gen-class))

(def cli-specs [["-h" "--help" "Show help"]
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

    (let [object-storage (if (= (:mode opts) "local")
                           (do
                             (i/create-integration-env)
                             (create-local-object-storage i/integration-path))
                           (create-s3-object-storage storage-creds))
          image-store (create-image-storage object-storage)
          system (create-system image-store)]
      (println (format "Mode: %s. Starting server on %d..." (:mode opts) (:port opts)))
      (start system (:port opts)))))
