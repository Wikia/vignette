(ns vignette.media-types-test
  (:require [midje.sweet :refer :all]
            [clout.core :refer [route-compile route-matches]]
            [ring.mock.request :refer :all]
            [vignette.test.helper :refer [context-route-matches]]
            [vignette.http.routes]
            [vignette.http.proto-routes :as proto]
            [vignette.http.route-helpers :as rh]
            [vignette.http.legacy.routes :as hlr]
            [vignette.http.legacy.route-helpers :as hlrh]
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

(def in-wiki-context-route-matches (partial context-route-matches vignette.http.routes/wiki-context))

(def latest-map (assoc archive-map :revision "latest"))

(def filled-map (assoc latest-map :options {:fill "green"}))

(def lang-map (assoc latest-map :options {:path-prefix "es"}))

(def prefix-path-map (assoc latest-map :options {:path-prefix "pokemanshop/zh/de"}))

(def lang-original-map (assoc original-map :options {:path-prefix "es"}))

(def zone-original-map (assoc original-map :options {:zone "temp"}))


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
       (thumbnail-path latest-map) => "images/thumb/a/ab/boat.jpg/200px-300px-thumbnail-boat.jpg"
       (fully-qualified-original-path latest-map) => "bucket/images/a/ab/boat.jpg"
       (fully-qualified-original-path archive-map) => "bucket/images/archive/a/ab/12345!boat.jpg")

(facts :thumbnail-path-filled
       (thumbnail-path filled-map) => "images/thumb/a/ab/boat.jpg/200px-300px-thumbnail[fill=green]-boat.jpg")

; Neither of these should modify the resulting filename since they don't have sideffects
(facts :lang-path
       (thumbnail-path lang-map) => "es/images/thumb/a/ab/boat.jpg/200px-300px-thumbnail-boat.jpg"
       (original-path lang-original-map) => "es/images/2/2a/Injustice_Vol2_1.jpg"
       (fully-qualified-original-path lang-map) => "bucket/es/images/a/ab/boat.jpg"
       (fully-qualified-original-path lang-original-map) => "batman/es/images/2/2a/Injustice_Vol2_1.jpg")

(facts :prefix-path
       (thumbnail-path prefix-path-map) => "pokemanshop/zh/de/images/thumb/a/ab/boat.jpg/200px-300px-thumbnail-boat.jpg")

(facts :fill-path
       (thumbnail-path (assoc-in lang-map [:options :fill] "purple")) => "es/images/thumb/a/ab/boat.jpg/200px-300px-thumbnail[fill=purple]-boat.jpg"
       (original-path lang-original-map) => "es/images/2/2a/Injustice_Vol2_1.jpg")

(facts :zone-path
       (original-path zone-original-map) => "images/temp/2/2a/Injustice_Vol2_1.jpg"
       (thumbnail-path (assoc latest-map :options {:zone "temp"})) => "images/temp/thumb/a/ab/boat.jpg/200px-300px-thumbnail[zone=temp]-boat.jpg"
       (thumbnail-path (assoc archive-map :options {:zone "temp"})) => "images/temp/thumb/archive/a/ab/12345!boat.jpg/200px-300px-thumbnail[zone=temp]-boat.jpg")

(facts :timeline-path
  (original-path timeline-map) => (str "es/images/timeline/" timeline-file)
  (fully-qualified-original-path timeline-map) => (str "television/es/images/timeline/" timeline-file))

(facts :scale-to-width-thumbnail-path
  (let [new-thumbnail-map
        (rh/route->thumbnail-auto-height-map
          (in-wiki-context-route-matches
            proto/scale-to-width-route
            (request :get "/happywheels/images/b/bb/SuperMario64_20.png/revision/latest/scale-to-width/185"))
          {})
        legacy-thumbnail-map
        (hlrh/route->thumb-map
          (route-matches hlr/thumbnail-route
                         (request :get "/happywheels/images/thumb/b/bb/SuperMario64_20.png/185px-SuperMario64_20.png")))]
    (thumbnail-path new-thumbnail-map) => (thumbnail-path legacy-thumbnail-map)))


(facts :window-crop-thumbnail-path
  (let [new-thumbnail-map
        (rh/route->thumbnail-auto-height-map
          (in-wiki-context-route-matches
            proto/window-crop-route
            (request :get "/muppet/images/4/40/JohnvanBruggen.jpg/revision/latest/window-crop/width/200/x-offset/0/y-offset/29/window-width/206/window-height/74"))
          {})
        legacy-thumbnail-map
        (hlrh/route->thumb-map
          (route-matches hlr/thumbnail-route
                         (request :get "/happywheels/images/thumb/4/40/JohnvanBruggen.jpg/200px-0,206,29,103-JohnvanBruggen.jpg")))]
    (thumbnail-path new-thumbnail-map) => (thumbnail-path legacy-thumbnail-map)))


(facts :window-crop-fixed-thumbnail-path
  (let [new-thumbnail-map
        (rh/route->thumbnail-map
          (in-wiki-context-route-matches
             proto/window-crop-fixed-route
             (request :get "/muppet/images/4/40/JohnvanBruggen.jpg/revision/latest/window-crop-fixed/width/200/height/200/x-offset/0/y-offset/29/window-width/206/window-height/74"))
          {})
        legacy-thumbnail-map
        (hlrh/route->thumb-map
          (route-matches hlr/thumbnail-route
                         (request :get "/happywheels/images/thumb/4/40/JohnvanBruggen.jpg/200x200-0,206,29,103-JohnvanBruggen.jpg")))]
    (thumbnail-path new-thumbnail-map) => (thumbnail-path legacy-thumbnail-map)))
