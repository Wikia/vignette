(ns vignette.api.legacy.routes-test
  (:require [vignette.api.legacy.routes :as routes]
            [clout.core :refer (route-compile route-matches)]
            [ring.mock.request :refer :all]
            [midje.sweet :refer :all]))

(fact :image-thumbnailer

  (let [matched (route-matches routes/image-thumbnail-swift (request :get "/swift/v1/herofactory/images/thumb/7/7f/Brain_Attack.PNG/130px-102%2C382%2C0%2C247-Brain_Attack.PNG"))]
    (:thumbnail matched) => "130px-102,382,0,247-Brain_Attack.PNG"
    (:original matched) => "Brain_Attack.PNG"
    (:middle-dir matched) => "7f"
    (:top-dir matched) => "7"
    (:image-type matched) => "images"
    (:wikia matched) => "herofactory"
    (:swift-version matched) => "v1"))
