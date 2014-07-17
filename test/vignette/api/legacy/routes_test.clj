(ns vignette.api.legacy.routes-test
  (:require [vignette.api.legacy.routes :as routes]
            [clout.core :refer (route-compile route-matches)]
            [ring.mock.request :refer :all]
            [midje.sweet :refer :all]))

(facts :image-thumbnailer
  (let [matched (route-matches routes/image-thumbnail-swift (request :get "/swift/v1/herofactory/images/thumb/7/7f/Brain_Attack.PNG/130px-102%2C382%2C0%2C247-Brain_Attack.PNG"))]
    (:thumbnail matched) => "130px-102,382,0,247-Brain_Attack.PNG"
    (:original matched) => "Brain_Attack.PNG"
    (:middle-dir matched) => "7f"
    (:top-dir matched) => "7"
    (:image-type matched) => "images"
    (:wikia matched) => "herofactory"
    (:swift-version matched) => "v1"))

(facts :coordinates
  (routes/thumbnail-path->coordinates "") => falsey
  (routes/thumbnail-path->coordinates "0,0,0") => falsey
  (routes/thumbnail-path->coordinates "0,0,0,0") => (contains {:x1 0 :x2 0 :y1 0 :y2 0})
  (routes/thumbnail-path->coordinates "1,2,3,4") => (contains {:x1 1 :x2 2 :y1 3 :y2 4}))

(facts :dimensions
  (routes/thumbnail-path->dimensions "") => falsey
  (routes/thumbnail-path->dimensions "foobar.png") => falsey
  (routes/thumbnail-path->dimensions "123px-foobar.png") => truthy
  (routes/thumbnail-path->dimensions "123px-foobar.png") => [:width '(123)]
  (routes/thumbnail-path->dimensions "123x123-foobar.png") => [:width-height '(123 123)]
  (routes/thumbnail-path->dimensions "123x124x10-foobar.png") => [:width-height-scale '(123 124 10)]
  )
