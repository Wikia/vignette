(ns vignette.storage.local-test
  (:require [clojure.java.io :as io]
            [clojure.java.shell :refer [sh]]
            [midje.sweet :refer :all]
            [vignette.storage.local :as ls]
            [vignette.storage.protocols :refer :all]
            [vignette.util.filesystem :refer :all]))

(def local-path "/tmp/vignette-local-storage")

(fact
  (file-exists? "project.clj") => truthy)

(with-state-changes
  [(before :facts (do
                    (sh "rm" "-rf" local-path)
                    (sh "mkdir" "-p" local-path)))]
  (facts :transfer
     (transfer! (ls/create-stored-object (io/file "project.clj"))
                (format "%s/tbar" local-path)) => truthy)

  (facts :put-object
    (let [local (ls/create-local-storage-system "/tmp/vignette-local-storage")]
      local => truthy
      (object-exists? local "bucket" "bar") => false
      (put-object local
                  (ls/create-stored-object (io/file "project.clj"))
                  "bucket"
                  "bar") => truthy
      (object-exists? local "bucket" "bar") => true))

  (facts :get-object
    (let [local (ls/create-local-storage-system "/tmp/vignette-local-storage")]
      local => truthy
      (get-object local "bucket" "bar") => falsey
      (object-exists? local "bucket" "bar") => false
      (put-object local
                  (ls/create-stored-object (io/file "project.clj"))
                  "bucket"
                  "bar") => truthy
      (get-object local "bucket" "bar") => truthy
      (object-exists? local "bucket" "bar") => true))

  (facts :delete-object
    (let [local (ls/create-local-storage-system "/tmp/vignette-local-storage")]
      local => truthy
      (delete-object local "bucket" "bar") => falsey
      (object-exists? local "bucket" "bar") => false
      (put-object local
                  (ls/create-stored-object (io/file "project.clj"))
                  "bucket"
                  "bar") => truthy
      (object-exists? local "bucket" "bar") => true
      (delete-object local "bucket" "bar") => truthy)))
