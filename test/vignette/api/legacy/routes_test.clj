(ns vignette.api.legacy.routes-test
  (:require [clout.core :refer [route-compile route-matches]]
            [midje.sweet :refer :all]
            [ring.mock.request :refer :all]
            [vignette.api.legacy.routes :as alr]))

(facts :thumbnail-route
  (let [matched (route-matches alr/thumbnail-route
                               (request :get "/happywheels/images/thumb/b/bb/SuperMario64_20.png/185px-SuperMario64_20.webp"))
        matched (alr/route->thumb-map matched)]
    (:request-type matched) => :thumbnail
    (:archive matched) => ""
    (:original matched) => "SuperMario64_20.png"
    (:middle-dir matched) => "bb"
    (:top-dir matched) => "b"
    (:image-type matched) => "images"
    (:width matched) => "185"
    (:wikia matched) => "happywheels"
    (:revision matched) => "latest"
    (:thumbname matched) => "SuperMario64_20.webp"
    (:format (:options matched)) => "webp")

  (let [matched (alr/route->thumb-map
                  (route-matches alr/thumbnail-route
                                 (request :get "/charmed/images/thumb/archive/b/b6/20101213101955!6x01-Phoebe.jpg/479px-6x01-Phoebe.jpg")))]
    (:request-type matched) => :thumbnail
    (:wikia matched) => "charmed"
    (:image-type matched) => "images"
    (:archive matched) => "/archive"
    (:top-dir matched) => "b"
    (:middle-dir matched) => "b6"
    (:original matched) => "6x01-Phoebe.jpg"
    (:width matched) => "479"
    (:height matched) => :auto
    (:revision matched) => "20101213101955"
    (:thumbname matched) => "6x01-Phoebe.jpg"
    (:format (:options matched)) => "jpg")

  (let [matched (alr/route->thumb-map
                  (route-matches alr/thumbnail-route
                                 (request :get "/charmed/images/thumb/archive/b/b6/20101213101955!6x01-Phoebe.jpg/100x200x300-6x01-Phoebe.jpg")))]
    (:request-type matched) => :thumbnail
    (:wikia matched) => "charmed"
    (:image-type matched) => "images"
    (:archive matched) => "/archive"
    (:top-dir matched) => "b"
    (:middle-dir matched) => "b6"
    (:original matched) => "6x01-Phoebe.jpg"
    (:width matched) => "100"
    (:height matched) => "200"
    (:thumbnail-mode matched) => "zoom-crop"
    (:revision matched) => "20101213101955"
    (:thumbname matched) => "6x01-Phoebe.jpg"
    (:format (:options matched)) => "jpg")

  (let [map (alr/route->thumb-map
              (route-matches alr/thumbnail-route
                             (request :get "/aigles-et-lys/fr/images/thumb/b/b7/Flag_of_Europe.svg/120px-Flag_of_Europe.svg.png")))]
    (:request-type map) => :thumbnail
    (:wikia map) => "aigles-et-lys"
    (:top-dir map) => "b"
    (:middle-dir map) => "b7"
    (:original map) => "Flag_of_Europe.svg"
    (:revision map) => "latest"
    (:thumbname map) => "Flag_of_Europe.svg.png"
    (:path-prefix (:options map)) => "fr")

  (let [matched (route-matches alr/thumbnail-route
                               (request :get "/happywheels/images/thumb/b/bb/SuperMario64_20.png/185px-0,120,0,240-SuperMario64_20.webp"))
        matched (alr/route->thumb-map matched)]
    (:request-type matched) => :thumbnail
    (:archive matched) => ""
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

  (let [matched (route-matches alr/thumbnail-route
                               (request :get "/thelastofus/images/thumb/5/58/Door_4.jpg/400x400-400,600,200,600-Door_4.jpg"))
        matched (alr/route->thumb-map matched)]
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

  (let [matched (route-matches alr/thumbnail-route
                               (request :get "/callofduty/images/thumb/1/1f/Undone/v,000000,200px-0,480,15,255-Undone"))
        matched (alr/route->thumb-map matched)]
    (:thumbnail-mode matched) => "window-crop"
    (:wikia matched) => "callofduty"
    (:top-dir matched) => "1"
    (:middle-dir matched) => "1f"
    (:original matched) => "Undone"
    (:width matched) => "200"
    (:x-offset matched) => "0"
    (:y-offset matched) => "15"
    (:window-width matched) => "480"
    (:window-height matched) => "240")

  (let [matched (route-matches alr/thumbnail-route
                               (request :get "/muppet/images/thumb/4/40/JohnvanBruggen.jpg/200px-JohnvanBruggen.jpg"))
        matched (alr/route->thumb-map matched)]
    (:thumbnail-mode matched) => "scale-to-width"
    (:wikia matched) => "muppet"
    (:top-dir matched) => "4"
    (:middle-dir matched) => "40"
    (:original matched) => "JohnvanBruggen.jpg"
    (:width matched) => "200"))

(facts :original-route
       (let [map (alr/route->original-map
                   (route-matches alr/original-route
                                  (request :get "/happywheels/images/b/bb/SuperMario64_20.png")))]
         (:request-type map) => :original
         (:wikia map) => "happywheels"
         (:top-dir map) => "b"
         (:middle-dir map) => "bb"
         (:original map) => "SuperMario64_20.png"
         (:revision map) => "latest")

       (let [map (alr/route->original-map
                   (route-matches alr/original-route
                                  (request :get "/aigles-et-lys/fr/images/b/b7/Flag_of_Europe.svg")))]
         (:request-type map) => :original
         (:wikia map) => "aigles-et-lys"
         (:top-dir map) => "b"
         (:middle-dir map) => "b7"
         (:original map) => "Flag_of_Europe.svg"
         (:revision map) => "latest"
         (:path-prefix (:options map)) => "fr"))
