(defproject vignette "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [http-kit "2.1.16"]
                 [ring "1.3.0"]
                 [compojure "1.1.8"]
                 [environ "0.5.0"]
                 [org.clojure/tools.logging "0.3.0"]
                 [clj-aws-s3 "0.3.9"]]
  :profiles  {:dev  {:source-paths  ["dev"]
                     :dependencies  [[midje "1.6.3"]
                                     [javax.servlet/servlet-api "2.5"]]}})
