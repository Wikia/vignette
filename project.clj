(defproject vignette "0.1.0-SNAPSHOT"
  :description "Thumbnailer in Clojure wrapping Image Magic using DFS."
  :url "https://github.com/Wikia/vignette"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :repositories [["snapshots" "https://artifactory.wikia-inc.com/artifactory/libs-snapshot-local/"]
                 ["releases" "https://artifactory.wikia-inc.com/artifactory/libs-release-local/"]]
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/tools.cli "0.3.1"]
                 [cheshire "5.6.3"]
                 [clj-aws-s3 "0.3.10"]
                 [compojure "1.4.0"]
                 [com.novemberain/pantomime "2.9.0"]
                 [digest "1.4.4"]
                 [environ "0.5.0"]
                 [cc.qbits/jet "0.6.6"]
                 [digest "1.4.4"]
                 [slingshot "0.10.3"]
                 [ring/ring-devel "1.4.0"]
                 [useful "0.8.8"]
                 [http-kit "2.1.18"]
                 [clj-logging-config "1.9.12"]
                 ; metrics
                 [com.soundcloud/prometheus-clj "2.4.1"]
                 ; logger
                 [com.fasterxml.jackson.core/jackson-core "2.9.6"]
                 [com.fasterxml.jackson.core/jackson-databind "2.9.6"]
                 [com.fasterxml.jackson.core/jackson-annotations "2.9.6"]
                 [ch.qos.logback/logback-classic "1.1.3"]
                 [net.logstash.logback/logstash-logback-encoder "5.2"]
                 [io.clj/logging "0.8.1"]
                 [org.slf4j/log4j-over-slf4j "1.7.15"]]
  :profiles  {:dev  {:source-paths  ["dev"]
                     :plugins [[lein-midje "3.1.1"]]
                     :dependencies  [[clj-http "1.0.1"]
                                     [javax.servlet/servlet-api "2.5"]
                                     [midje "1.8.3"]
                                     [org.clojure/tools.namespace "0.2.5"]
                                     [org.clojure/tools.trace "0.7.8"]
                                     [ring-mock "0.1.5"]]}}
  :main vignette.core
  :aot [vignette.core
        vignette.protocols
        vignette.storage.protocols]
  :repl-options {:init-ns user}
  :uberjar-name "vignette-standalone.jar"
  :jvm-opts ["-server"])
