(ns vignette.caching.edge.fastly-test
  (:require [midje.sweet :refer :all]
            [clj-http.client :as client]
            [vignette.protocols :refer :all]
            [vignette.caching.edge.fastly :refer :all]))

(facts :purge
  (purge (create-fastly-api {:id ..id.. :auth-key ..auth.. :api-url default-fastly-api-url}) ..uri.. ..key..) => false
  (provided
    (purge-request-url default-fastly-api-url ..id.. ..key..) => ..url..
    (api-params ..auth..) => ..params..
    (client/post ..url.. ..params..) => {:status 500})

  (purge (create-fastly-api {:id ..id.. :auth-key ..auth.. :api-url default-fastly-api-url}) ..uri.. ..key..) => true
  (provided
    (purge-request-url default-fastly-api-url ..id.. ..key..) => ..url..
    (api-params ..auth..) => ..params..
    (client/post ..url.. ..params..) => {:status 200}))

