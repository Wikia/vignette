(ns vignette.util.filesystem-test
  (:require [midje.sweet :refer :all]
            [vignette.util.filesystem :refer :all]))

(facts :file-extension
       (file-extension "some-file.png") => "png"
       (file-extension "Mambo No. 5 (A Little Bit of...)") => nil
       (file-extension "some-file.png.jpg") => "jpg"
       (file-extension nil) => nil)
