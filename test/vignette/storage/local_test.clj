(ns vignette.storage.local-test
  (:require [vignette.storage.protocols :refer :all]
            [vignette.storage.local :refer :all]
            [clojure.java.shell :refer (sh)]
            [clojure.java.io :as io]
            [midje.sweet :refer :all]))

(def local-path "/tmp/vignette-local-storage")

(fact
  (file-exists? "project.clj") => truthy)

(with-state-changes
  [(before :facts (do
                    (sh "rm" "-rf" local-path)
                    (sh "mkdir" "-p" local-path)))]
  (facts :transfer
     (transfer! "project.clj" (format "%s/tbar" local-path)) => truthy)

  (facts :put-object
    (let [local (create-local-object-storage "/tmp/vignette-local-storage")]
      local => truthy
      (put-object local "project.clj" "bucket" "bar") => truthy))

  (facts :get-object
    (let [local (create-local-object-storage "/tmp/vignette-local-storage")]
      local => truthy
      (get-object local "bucket" "bar") => falsey
      (put-object local "project.clj" "bucket" "bar") => truthy
      (get-object local "bucket" "bar") => string?))

  (facts :delete-object
    (let [local (create-local-object-storage "/tmp/vignette-local-storage")]
      local => truthy
      (delete-object local "bucket" "bar") => falsey
      (put-object local "project.clj" "bucket" "bar") => truthy
      (delete-object local "bucket" "bar") => truthy)))
