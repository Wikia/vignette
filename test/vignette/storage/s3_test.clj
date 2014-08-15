(ns vignette.storage.s3-test
  (:require (vignette.storage [protocols :refer :all]
                              [local :refer (create-local-object-storage)]
                              [s3 :refer :all])
            (vignette.util [byte-streams :refer :all])
            [aws.sdk.s3 :as s3]
            [midje.sweet :refer :all]
            [clojure.java.io :as io]))

(facts :s3 :get-object
  (get-object (create-s3-object-storage ..creds.. ..cache-dir..) "bucket" "a/ab/image.jpg") => ..file..
  (provided
    (create-local-object-storage ..cache-dir..) => ..local-cache..
    (read-from-local-cache ..local-cache.. "bucket" "a/ab/image.jpg") => ..file..
    )

  (get-object (create-s3-object-storage ..creds.. ..cache-dir..) "bucket" "a/ab/image.jpg") => ..file..
  (provided
    (create-local-object-storage ..cache-dir..) => ..local-cache..
    (read-from-local-cache ..local-cache.. "bucket" "a/ab/image.jpg") => nil
    (s3/get-object ..creds.. "bucket" "a/ab/image.jpg") => {:content ..stream..
                                                            :metadata {:content-length ..length..}}
    (read-byte-stream ..stream.. ..length..) => ..bytes..
    (write-locally ..local-cache.. ..bytes.. "bucket" "a/ab/image.jpg") => ..file..)
  
  (get-object (create-s3-object-storage ..creds.. ..cache-dir..) "bucket" "a/ab/image.jpg") => falsey
  (provided
    (create-local-object-storage ..cache-dir..) => ..local-cache..
    (read-from-local-cache ..local-cache.. "bucket" "a/ab/image.jpg") => nil
    (s3/get-object ..creds.. "bucket" "a/ab/image.jpg") => nil))

(facts :s3 :put-object
  (put-object (create-s3-object-storage ..creds.. ..cache-dir..) ..resource.. "bucket" "a/ab/image.jpg") => ..response..
  (provided
    (s3/put-object ..creds.. "bucket" "a/ab/image.jpg" ..resource..) => ..response..)

  ; this may not be realistic. we'll probably get an error before we get nil
  (put-object (create-s3-object-storage ..creds.. ..cache-dir..) ..resource.. "bucket" "a/ab/image.jpg") => nil
  (provided
    (s3/put-object ..creds.. "bucket" "a/ab/image.jpg" ..resource..) => nil))
