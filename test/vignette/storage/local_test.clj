(ns vignette.storage.local-test
  (:require [vignette.storage.protocols :refer :all]
            [vignette.storage.local :as local-storage]
            [clojure.java.shell :refer (sh)]
            [vignette.util.filesystem :refer :all]
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
     (transfer! (local-storage/create-stored-object (io/file "project.clj"))
                (format "%s/tbar" local-path)) => truthy)

  (facts :put-object
    (let [local (local-storage/create-local-object-storage "/tmp/vignette-local-storage")]
      local => truthy
      (put-object local
                  (local-storage/create-stored-object (io/file "project.clj"))
                  "bucket"
                  "bar") => truthy))

  (facts :get-object
    (let [local (local-storage/create-local-object-storage "/tmp/vignette-local-storage")]
      local => truthy
      (get-object local "bucket" "bar") => falsey
      (put-object local
                  (local-storage/create-stored-object (io/file "project.clj"))
                  "bucket"
                  "bar") => truthy
      (get-object local "bucket" "bar") => truthy))

  (facts :delete-object
    (let [local (local-storage/create-local-object-storage "/tmp/vignette-local-storage")]
      local => truthy
      (delete-object local "bucket" "bar") => falsey
      (put-object local
                  (local-storage/create-stored-object (io/file "project.clj"))
                  "bucket"
                  "bar") => truthy
      (delete-object local "bucket" "bar") => truthy)))
