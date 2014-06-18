(ns vignette.storage.local-test
  (:require [vignette.storage.core :refer :all]
            [vignette.storage.local :refer :all]
            [clojure.java.shell :refer (sh)]
            [midje.sweet :refer :all]))

(with-state-changes
  [(before :facts (do
                    (sh "rm" "-rf" "/tmp/vignette-local-storage")))]
  (facts :get
    (let [local (create-local-image-storage "/tmp/vignette-local-storage")]
      local => truthy
      (get-object local "foo" "bar") => falsey)))
