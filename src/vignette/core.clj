(ns vignette.core
  (:require [clojure.tools.cli :refer (cli)]
            [vignette.util.integration :as i]
            (vignette.storage [core :refer (create-image-storage)]
                              [local :refer (create-local-object-storage)]
                              [protocols :refer :all])
            (vignette [server :as s]
                      [protocols :refer :all]
                      [system :refer :all])))

(def cli-specs [["-h" "--help" "Show help" :flag true :default false]
                ["-I" "--integration" "Run in integration testing mode" :flag true :default false]
                ["-p" "--port" "Run the server on the specified port" :default 8080 :parse-fn #(Integer/parseInt %) :validate  [#(< 0 % 0x10000) "Must be a number between 0 and 65536"]]])

(defn -main [& args]
  (let [[opts rest-args banner] (apply cli args cli-specs)]
    (when (:help opts)
      (println banner)
      (System/exit 0))

    (if (:integration opts)
      (let [local-store (create-local-object-storage i/integration-path)
            image-store (create-image-storage local-store)
            system (create-system image-store)]
        (i/create-integration-env)
        (println (format "Starting integration server on %d..." (:port opts) ))
        (start system (:port opts)))
      (println "not implemented"))))
