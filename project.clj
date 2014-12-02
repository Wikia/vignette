(defproject vignette "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[cheshire "5.3.1"]
                 [clj-aws-s3 "0.3.10"]
                 [clout "1.2.0"]
                 [compojure "1.1.8"]
                 [com.novemberain/pantomime "2.3.0"]
                 [environ "0.5.0"]
                 [http-kit "2.1.18"]
                 [org.clojure/clojure "1.5.1"]
                 [org.clojure/tools.cli "0.3.1"]
                 [ring "1.3.0"]
                 [slingshot "0.10.3"]
                 [useful "0.8.8"]
                 [wikia/commons "0.1.1-SNAPSHOT"]]
  :profiles  {:dev  {:source-paths  ["dev"]
                     :plugins [[lein-midje "3.1.1"]]
                     :dependencies  [[midje "1.6.3"]
                                     [javax.servlet/servlet-api "2.5"]
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
