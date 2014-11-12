(ns vignette.util.filesystem-test
  (:require [midje.sweet :refer :all]
            [vignette.util.filesystem :refer :all]))

(facts :file-extension
       (file-extension "some-file.png") => "png"
       (file-extension "some-file.png.jpg") => "jpg")

(facts :str->rainbow-path
       (str->rainbow-path "abc123") => "a/ab"
       (str->rainbow-path "a.b/c-1'2=3" 6) => "a/ab/abc/abc1/abc12/abc123"
       (str->rainbow-path "asdf" 10) => "a/as/asd/asdf")

(facts :temp-filename
       (temp-filename "original") => "4/47/original_47dfa-047bd-ea31e"
       (provided
         (gen-uuid) => "47dfa-047bd-ea31e"
         (temp-file-dir) => "")

       (temp-filename "thumb" "jpg") => "a/a6/thumb_a-6-ffgd.jpg"
       (provided
         (gen-uuid) => "a-6-ffgd"
         (temp-file-dir) => ""))
