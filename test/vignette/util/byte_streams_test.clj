(ns vignette.util.byte_streams_test
  (:require (vignette.util [byte-streams :refer :all]
                           [filesystem :refer (file-exists?)])
            [midje.sweet :refer :all]
            [clojure.java.io :as io]
            [clojure.java.shell :refer (sh)]))

(def input-file "image-samples/ropes.jpg")
(def temp-file "/tmp/vignette-test-file")

(facts :integration :read-byte-stream
  (let [resource (io/file input-file)
        len (.length resource)]
    (count (read-byte-stream input-file len)) => len))

(with-state-changes
  [(before :facts (do
                    (sh "rm" temp-file)))]
  (facts :integration :write-byte-stream
    (let [resource (io/file input-file)
          out-file (io/file temp-file)
          len (.length resource)]
      (write-byte-stream out-file (read-byte-stream input-file len)) => falsey
      (file-exists? out-file) => truthy
      (:exit (sh "diff" "-q" input-file temp-file)) => 0)))
