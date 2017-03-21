(defproject vignette "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :repositories [["snapshots" "https://artifactory.wikia-inc.com/artifactory/libs-snapshot-local/"]
                 ["releases" "https://artifactory.wikia-inc.com/artifactory/libs-release-local/"]]
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/tools.cli "0.3.1"]
                 [cheshire "5.3.1"]
                 [clj-aws-s3 "0.3.10"]
                 [compojure "1.4.0"]
                 [consul-clojure "0.1.0"]
                 [com.novemberain/pantomime "2.9.0"]
                 [digest "1.4.4"]
                 [environ "0.5.0"]
                 [cc.qbits/jet "0.6.6"]
                 [digest "1.4.4"]
                 [slingshot "0.10.3"]
                 [ring/ring-devel "1.4.0"]
                 [useful "0.8.8"]
                 [wikia/commons "0.1.5"]
                 [http-kit "2.1.18"]]
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
