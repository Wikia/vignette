(ns vignette.storage.s3-test
  (:require (vignette.storage [protocols :refer :all]
                              [s3 :refer :all]
                              [common :as sc])
            (vignette.util [byte-streams :refer :all])
            [aws.sdk.s3 :as s3]
            [midje.sweet :refer :all]
            [clojure.java.io :as io]))

(facts :s3 :get-object
  (get-object (create-s3-object-storage ..creds..) "bucket" "a/ab/image.jpg") => ..object..
  (provided
    (safe-get-object ..creds.. "bucket" "a/ab/image.jpg") => {:content ..stream..
                                                              :metadata {:content-length ..length.. :content-type ..content-type..}}
    (read-byte-stream ..stream.. ..length..) => ..bytes..
    (sc/create-storage-object ..bytes.. ..content-type.. ..length..) => ..object..)

  (get-object (create-s3-object-storage ..creds..) "bucket" "a/ab/image.jpg") => falsey
  (provided
    (s3/get-object ..creds.. "bucket" "a/ab/image.jpg") => {}))

(facts :s3 :put-object
  (put-object (create-s3-object-storage ..creds..) ..resource.. "bucket" "a/ab/image.jpg") => ..response..
  (provided
    (s3/put-object ..creds.. "bucket" "a/ab/image.jpg" ..resource..) => ..response..)

  ; this may not be realistic. we'll probably get an error before we get nil
  (put-object (create-s3-object-storage ..creds..) ..resource.. "bucket" "a/ab/image.jpg") => nil
  (provided
    (s3/put-object ..creds.. "bucket" "a/ab/image.jpg" ..resource..) => nil))
