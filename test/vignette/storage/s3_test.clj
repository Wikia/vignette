(ns vignette.storage.s3-test
  (:require (vignette.storage [protocols :refer :all]
                              [s3 :refer :all])
            (vignette.util [byte-streams :refer :all])
            [aws.sdk.s3 :as s3]
            [midje.sweet :refer :all]
            [clojure.java.io :as io]))

(facts :s3 :get-object :unit
  (get-object (create-s3-object-storage ..creds..) "bucket" "a/ab/image.jpg") => ..bytes..
  (provided
    (s3/get-object ..creds.. "bucket" "a/ab/image.jpg") => {:content ..stream..
                                                            :metadata {:content-length ..length..}}
    (read-byte-stream ..stream.. ..length..) => ..bytes..)
  
  (get-object (create-s3-object-storage ..creds..) "bucket" "a/ab/image.jpg") => falsey
  (provided
    (s3/get-object ..creds.. "bucket" "a/ab/image.jpg") => nil))
