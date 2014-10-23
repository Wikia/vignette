(ns vignette.media-types-test
  (:require [midje.sweet :refer :all]
            [vignette.media-types :refer :all]))

(def original-map {:wikia "batman"
                   :image-type "images"
                   :top-dir "2"
                   :middle-dir "2a"
                   :original "Injustice_Vol2_1.jpg"
                   :revision "latest"
                   :options {}})

(def archive-map {:wikia "bucket"
                  :image-type "images"
                  :top-dir "a"
                  :middle-dir "ab"
                  :original "boat.jpg"
                  :revision "12345"
                  :thumbnail-mode "thumbnail"
                  :width "200"
                  :height "300"
                  :options {}})

(def latest-map (assoc archive-map :revision "latest"))

(def filled-map (assoc latest-map :options {:fill "green"}))

(def lang-map (assoc latest-map :options {:lang "es"}))

(def lang-original-map (assoc original-map :options {:lang "es"}))

(facts :revision
       (revision archive-map) => "12345"
       (revision latest-map) => nil)

(facts :revision-filename
       (revision-filename archive-map) => "12345!boat.jpg"
       (revision-filename latest-map) => "boat.jpg")

(facts :original-path
       (original-path archive-map) => "images/archive/a/ab/12345!boat.jpg"
       (original-path latest-map) => "images/a/ab/boat.jpg")

(facts :thumbnail-path
       (thumbnail-path archive-map) => "images/thumb/archive/a/ab/12345!boat.jpg/200px-300px-thumbnail-boat.jpg"
       (thumbnail-path latest-map) => "images/thumb/a/ab/boat.jpg/200px-300px-thumbnail-boat.jpg")

(facts :thumbnail-path-filled
       (thumbnail-path filled-map) => "images/thumb/a/ab/boat.jpg/200px-300px-thumbnail[fill=green]-boat.jpg")

(facts :lang-path
       (thumbnail-path lang-map) => "es/images/thumb/a/ab/boat.jpg/200px-300px-thumbnail[lang=es]-boat.jpg"
       (original-path lang-original-map) => "es/images/2/2a/Injustice_Vol2_1.jpg")
