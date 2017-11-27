(ns vignette.util.services-test
  (:require [midje.sweet :refer :all]
            [environ.core :refer [env]]
            [vignette.util.services :as svc]))

(facts :static-assets :url
  (svc/materialize-static-asset-url "uuid_sample") => "http://dev.test-dev.k8s.wikia.net/static-assets/image/uuid_sample"
  (provided
    (env :wikia-environment) => "dev"
    (env :wikia-datacenter) => "test")


  (svc/materialize-static-asset-url "uuid_sample") => "http://prod.test.k8s.wikia.net/static-assets/image/uuid_sample"
  (provided
    (env :wikia-environment) => "prod"
    (env :wikia-datacenter) => "test")

  (svc/materialize-static-asset-url "uuid_sample") => (throws Exception "WIKIA_ENVIRONMENT not set")
  (provided
    (env :wikia-environment) => nil
    (env :wikia-datacenter) => "test")

  (svc/materialize-static-asset-url "uuid_sample") => (throws Exception "WIKIA_DATACENTER not set")
  (provided
    (env :wikia-environment) => "test"
    (env :wikia-datacenter) => nil))
