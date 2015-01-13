(ns vignette.util.integration-test
  (:require [clojure.java.io :as io]
            [midje.sweet :refer :all]
            [vignette.http.routes :refer :all]
            [vignette.protocols :refer :all]
            [vignette.storage.core :refer [create-image-storage]]
            [vignette.storage.local :refer [create-local-storage-system]]
            [vignette.storage.protocols :refer :all]
            [vignette.util.filesystem :refer :all]
            [vignette.util.integration :as i]))

(facts :get-sample-image-maps
  (first (i/get-sample-image-maps)) => (contains {:top-dir "a", :wikia "bucket", :image-type "images", :middle-dir "ab", :original "beach.jpg", :request-type :original}))

(facts :create-integration-env
  (let [local-store (create-local-storage-system i/integration-path)
        image-store (create-image-storage local-store)]
    (i/create-integration-env i/integration-path) => truthy
    (get-original image-store (first (i/get-sample-image-maps))) => truthy))
