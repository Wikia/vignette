(defproject vignette "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :repositories ^:replace [["snapshots" "http://dev-nelson:8080/repository/snapshots"]
                           ["releases" "http://dev-nelson:8080/repository/internal"]]
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [clout "1.2.0"]
                 [http-kit "2.1.16"]
                 [cheshire "5.3.1"]
                 [ring "1.3.0"]
                 [compojure "1.1.8"]
                 [prismatic/schema "0.2.4"]
                 [environ "0.5.0"]
                 [org.clojure/tools.logging "0.3.0"]
                 [com.novemberain/pantomime "2.3.0"]
                 [clj-aws-s3 "0.3.9"]
                 [clojure-commons "0.1.0-SNAPSHOT"]]
  :profiles  {:dev  {:source-paths  ["dev"]
                     :plugins [[lein-midje "3.1.1"]]
                     :dependencies  [[midje "1.6.3"]
                                     [org.clojure/tools.trace "0.7.8"]
                                     [org.clojure/tools.namespace "0.2.5"]
                                     [ring-mock "0.1.5"]
                                     [javax.servlet/servlet-api "2.5"]]}})
