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

(def lang-map (assoc latest-map :options {:path-prefix "es"}))

(def prefix-path-map (assoc latest-map :options {:path-prefix "pokemanshop/zh/de"}))

(def lang-original-map (assoc original-map :options {:path-prefix "es"}))


(def timeline-file "bbe457792492f1b89f21a45aa6ca6088.jpg")
(def timeline-map {:wikia "television"
                   :image-type "images"
                   :top-dir "timeline"
                   :original timeline-file
                   :revision "latest"
                   :options {:path-prefix "es"}})

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

; Neither of these should modify the resulting filename since they don't have sideffects
(facts :lang-path
       (thumbnail-path lang-map) => "es/images/thumb/a/ab/boat.jpg/200px-300px-thumbnail-boat.jpg"
       (original-path lang-original-map) => "es/images/2/2a/Injustice_Vol2_1.jpg")

(facts :prefix-path
       (thumbnail-path prefix-path-map) => "pokemanshop/zh/de/images/thumb/a/ab/boat.jpg/200px-300px-thumbnail-boat.jpg")

(facts :fill-path
       (thumbnail-path (assoc-in lang-map [:options :fill] "purple")) => "es/images/thumb/a/ab/boat.jpg/200px-300px-thumbnail[fill=purple]-boat.jpg"
       (original-path lang-original-map) => "es/images/2/2a/Injustice_Vol2_1.jpg")

(facts :timeline-path
  (original-path timeline-map) => (str "es/images/timeline/" timeline-file))
