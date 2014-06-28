(ns vignette.storage.local-test
  (:require [vignette.storage.core :refer :all]
            [vignette.storage.local :refer :all]
            [clojure.java.shell :refer (sh)]
            [clojure.java.io :as io]
            [midje.sweet :refer :all]))

(def local-path "/tmp/vignette-local-storage")

(with-state-changes
  [(before :facts (do
                    ;(sh "rm" "-rf" local-path)
                    (sh "mkdir" "-p" local-path)))]
  (facts :transfer
     (transfer
       (io/file "project.clj")
       (io/file (format "%s/tbar" local-path))) => nil)

  (facts :put
    (let [local (create-local-image-storage "/tmp/vignette-local-storage")]
      local => truthy
      (put-object local (io/file "project.clj") "bucket", "bar") => nil))

  (facts :get
    (let [local (create-local-image-storage "/tmp/vignette-local-storage")]
      local => truthy
      (get-object local "foo" "bar") => falsey)))
