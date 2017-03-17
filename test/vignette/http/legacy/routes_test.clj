(ns vignette.http.legacy.routes-test
  (:require [clout.core :refer [route-compile route-matches]]
            [midje.sweet :refer :all]
            [ring.mock.request :refer :all]
            [vignette.http.legacy.routes :refer :all]
            [vignette.http.legacy.route-helpers :refer :all]))

(facts :thumbnail-route
  (let [request (request :get "/happywheels/images/thumb/b/bb/SuperMario64_20.png/185px-SuperMario64_20.WEBP")
        matched (route->thumb-map (route-matches thumbnail-route request) request)]
    (:request-type matched) => :thumbnail
    (archive? matched) => false
    (:original matched) => "SuperMario64_20.png"
    (:middle-dir matched) => "bb"
    (:top-dir matched) => "b"
    (:image-type matched) => "images"
    (:width matched) => "185"
    (:wikia matched) => "happywheels"
    (:revision matched) => "latest"
    (:thumbname matched) => "SuperMario64_20.WEBP"
    (:format (:options matched)) => "webp")

  (let [request (request :get "/leagueoflegends/images/thumb/3/31/Cassiopeia_RVideo.ogv/mid-Cassiopeia_RVideo.ogv.jpg")
        matched (route-matches thumbnail-route request)
        matched (route->thumb-map matched request)]
    (:request-type matched) => :thumbnail
    (archive? matched) => false
    (:original matched) => "Cassiopeia_RVideo.ogv"
    (:middle-dir matched) => "31"
    (:top-dir matched) => "3"
    (:image-type matched) => "images"
    (:width matched) => default-width
    (:wikia matched) => "leagueoflegends"
    (:revision matched) => "latest"
    (:thumbname matched) => "Cassiopeia_RVideo.ogv.jpg"
    (:format (:options matched)) => "jpg")

  (let [request (request :get "/charmed/images/thumb/archive/b/b6/20101213101955!6x01-Phoebe.jpg/479px-6x01-Phoebe.jpg")
        matched (route->thumb-map (route-matches thumbnail-route request) request)]
    (:request-type matched) => :thumbnail
    (:wikia matched) => "charmed"
    (:image-type matched) => "images"
    (archive? matched) => true
    (:top-dir matched) => "b"
    (:middle-dir matched) => "b6"
    (:original matched) => "6x01-Phoebe.jpg"
    (:width matched) => "479"
    (:height matched) => :auto
    (:revision matched) => "20101213101955"
    (:thumbname matched) => "6x01-Phoebe.jpg"
    (get (:options matched) :format nil) => nil?)

  (let [request (request :get "/charmed/images/thumb/archive/b/b6/20101213101955!6x01-Phoebe.jpg/100x200x300-6x01-Phoebe.jpg")
        matched (route->thumb-map
                  (route-matches thumbnail-route request) request)]
    (:request-type matched) => :thumbnail
    (:wikia matched) => "charmed"
    (:image-type matched) => "images"
    (archive? matched) => true
    (:top-dir matched) => "b"
    (:middle-dir matched) => "b6"
    (:original matched) => "6x01-Phoebe.jpg"
    (:width matched) => "100"
    (:height matched) => "200"
    (:thumbnail-mode matched) => "zoom-crop"
    (:revision matched) => "20101213101955"
    (:thumbname matched) => "6x01-Phoebe.jpg")

  (let [request (request :get "/aigles-et-lys/fr/images/thumb/b/b7/Flag_of_Europe.svg/120px-Flag_of_Europe.svg.png")
        map (route->thumb-map
              (route-matches thumbnail-route request) request)]
    (:request-type map) => :thumbnail
    (:wikia map) => "aigles-et-lys"
    (:top-dir map) => "b"
    (:middle-dir map) => "b7"
    (:original map) => "Flag_of_Europe.svg"
    (:revision map) => "latest"
    (:thumbname map) => "Flag_of_Europe.svg.png"
    (:path-prefix (:options map)) => "fr")

  (let [request (request :get "/happywheels/images/thumb/b/bb/SuperMario64_20.png/185px-0,120,0,240-SuperMario64_20.webp")
        matched (route-matches thumbnail-route request)
        matched (route->thumb-map matched request)]
    (:request-type matched) => :thumbnail
    (archive? matched) => false
    (:original matched) => "SuperMario64_20.png"
    (:middle-dir matched) => "bb"
    (:top-dir matched) => "b"
    (:image-type matched) => "images"
    (:width matched) => "185"
    (:height matched) => :auto
    (:wikia matched) => "happywheels"
    (:revision matched) => "latest"
    (:offset matched) => "0,120,0,240-"
    (:thumbname matched) => "SuperMario64_20.webp"
    (:x-offset matched) => "0"
    (:y-offset matched) => "0"
    (:window-width matched) => "120"
    (:window-height matched) => "240"
    (:thumbnail-mode matched) => "window-crop"
    (:format (:options matched)) => "webp")

  (let [request (request :get "/thelastofus/images/thumb/5/58/Door_4.jpg/400x400-400,600,200,600-Door_4.jpg")
        matched (route-matches thumbnail-route request)
        matched (route->thumb-map matched request)]
    (:thumbnail-mode matched) => "window-crop-fixed"
    (:wikia matched) => "thelastofus"
    (:top-dir matched) => "5"
    (:middle-dir matched) => "58"
    (:original matched) => "Door_4.jpg"
    (:width matched) => "400"
    (:height matched) => "400"
    (:x-offset matched) => "400"
    (:y-offset matched) => "200"
    (:window-width matched) => "200"
    (:window-height matched) => "400")

  (let [request (request :get "/callofduty/images/thumb/1/1f/Undone/v,000000,200px--2,480,-15,255-Undone")
        matched (route-matches thumbnail-route request)
        matched (route->thumb-map matched request)]
    (:thumbnail-mode matched) => "window-crop"
    (:wikia matched) => "callofduty"
    (:top-dir matched) => "1"
    (:middle-dir matched) => "1f"
    (:original matched) => "Undone"
    (:width matched) => "200"
    (:x-offset matched) => "-2"
    (:y-offset matched) => "-15"
    (:window-width matched) => "482"
    (:window-height matched) => "270")

  (let [request (request :get "/muppet/images/thumb/4/40/JohnvanBruggen.jpg/200px-JohnvanBruggen.jpg")
        matched (route-matches thumbnail-route request)
        matched (route->thumb-map matched request)]
    (:thumbnail-mode matched) => "scale-to-width"
    (:wikia matched) => "muppet"
    (:top-dir matched) => "4"
    (:middle-dir matched) => "40"
    (:original matched) => "JohnvanBruggen.jpg"
    (:width matched) => "200")

   (let [request (assoc-in (request :get "/muppet/images/thumb/4/40/JohnvanBruggen.jpg/200px-JohnvanBruggen.jpg") [:headers "accept"] "image/webp")
         matched (route-matches thumbnail-route request)
         matched (route->thumb-map matched request)]
     (:thumbnail-mode matched) => "scale-to-width"
     (:wikia matched) => "muppet"
     (:top-dir matched) => "4"
     (:middle-dir matched) => "40"
     (:original matched) => "JohnvanBruggen.jpg"
     (:options matched) => {:format "webp"}
     (:width matched) => "200")

  (let [request (request :get "/muppet/images/thumb/temp/4/40/JohnvanBruggen.jpg/200px-JohnvanBruggen.jpg")
        matched (route-matches thumbnail-route request)
        matched (route->thumb-map matched request)]
    (:thumbnail-mode matched) => "scale-to-width"
    (zone matched) => "temp"
    (:wikia matched) => "muppet"
    (:top-dir matched) => "4"
    (:middle-dir matched) => "40"
    (:original matched) => "JohnvanBruggen.jpg"
    (:width matched) => "200"))

(facts :original-route
       (let [request (request :get "/happywheels/images/b/bb/SuperMario64_20.png")
             map (route->original-map
                   (route-matches original-route request) request)]
         (:request-type map) => :original
         (:wikia map) => "happywheels"
         (:top-dir map) => "b"
         (:middle-dir map) => "bb"
         (:original map) => "SuperMario64_20.png"
         (:revision map) => "latest")

       (let [request (request :get "/aigles-et-lys/fr/images/b/b7/Flag_of_Europe.svg")
             map (route->original-map
                   (route-matches original-route request) request)]
         (:request-type map) => :original
         (:wikia map) => "aigles-et-lys"
         (:top-dir map) => "b"
         (:middle-dir map) => "b7"
         (:original map) => "Flag_of_Europe.svg"
         (:revision map) => "latest"
         (:path-prefix (:options map)) => "fr")
       (let [request (request :get "/aigles-et-lys/fr/images/temp/b/b7/Flag_of_Europe.svg")
             map (route->original-map
                   (route-matches original-route request) request)]
         (:request-type map) => :original
         (:wikia map) => "aigles-et-lys"
         (zone map) => "temp"
         (:top-dir map) => "b"
         (:middle-dir map) => "b7"
         (:original map) => "Flag_of_Europe.svg"
         (:revision map) => "latest"
         (:path-prefix (:options map)) => "fr"))

(facts :timeline-route
  (let [request (request :get "/television/es/images/timeline/bbe457792492f1b89f21a45aa6ca6088.png")
        map (route->timeline-map
              (route-matches timeline-route request) request)]
    (:request-type map) => :original
    (:top-dir map) => "timeline"
    (:middle-dir map) => nil
    (:wikia map) => "television"
    (:path-prefix (:options map)) => "es"
    (:original map) => "bbe457792492f1b89f21a45aa6ca6088.png"
    (:image-type map) => "images"))

(facts :math-route
       (let [request (request :get "/nelsontest/images/math/3/9/f/39f9e908b194691eef95b328f9abc76c.png")
             map (route->original-map
                   (route-matches math-route request) request)]
         (:request-type map) => :original
         (:top-dir map) => "3"
         (:middle-dir map) => "9/f"
         (:original map) => "39f9e908b194691eef95b328f9abc76c.png"
         (zone map) => "math"
         (:image-type map) => "images"))

(facts :map-original-route
  (let [request (request :get "/intmap_tile_set_4823/20150205173220!phpZDfa00.jpg")
        map (route->interactive-maps-map
              (route-matches interactive-maps-route request) request)]
    (:request-type map) => :original
    (:image-type map) => "arbitrary"
    (:original map) => "20150205173220!phpZDfa00.jpg"
    (:path-prefix map) => empty?
    (:wikia map) => "intmap_tile_set_4823"))

(facts :map-original-route-zoom
  (let [request (request :get "/intmap_tile_set_4692/3/1/2.png")
        map (route->interactive-maps-map
              (route-matches interactive-maps-route request) request)]
    (:request-type map) => :original
    (:image-type map) => "arbitrary"
    (:original map) => "2.png"
    (:path-prefix map) => "/3/1"
    (:path-prefix (:options map)) => "3/1"))

(facts :map-marker-route
  (let [request (request :get "/intmap_markers_109/60px-20140716115821!phpZDweHO.png")
        map (route->interactive-maps-map
              (route-matches interactive-maps-marker-route request) request)]
    (:request-type map) => :original
    (:image-type map) => "arbitrary"
    (:original map) => "60px-20140716115821!phpZDweHO.png"
    (:path-prefix map) => nil
    (:wikia map) => "intmap_markers_109"))

(facts :map-thumbnail-route
  (let [request (request :get "/intmap_tile_set_4823/thumb/20150205173220%21phpZDfa00.jpg/1110x300x5-20150205173220%21phpZDfa00.jpg")
        map (route->interactive-maps-thumbnail-map
              (route-matches interactive-maps-thumbnail-route request) request)]
    (:width map) => "1110"
    (:height map) => "300"
    (:original map) => "20150205173220%21phpZDfa00.jpg"
    (:image-type map) => "arbitrary"))

(facts :image-format
  (image->format "foo.webp") => "webp"
  (image->format "foo.WEBP") => "webp"
  (image->format "foo.PNG") => "png"
  (image->format "foo.jpg.PNG") => "png"
  (image->format "foo") => nil?)
