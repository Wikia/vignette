(ns vignette.caching.edge.fastly
  (:require [clj-http.client :as client]
            [vignette.protocols :refer :all]
            [wikia.common.logger :as log]
            [wikia.common.perfmonitoring.core :as perf])
  (:use [environ.core]))

(def default-fastly-api-url "https://api.fastly.com")

(def fastly-creds {:id       (env :fastly-api-id)
                   :auth-key (env :fastly-api-auth-key)
                   :api-url  (env :fastly-api-url default-fastly-api-url)})

(declare purge-request-url
         api-params)

(defrecord FastlyAPI [id auth-key fastly-api-url]
  CachePurgeAPI
  (purge [this uri surrogate-key]
    (perf/publish {:purge-count 1})
    (let [response (client/post (purge-request-url (:fastly-api-url this)
                                                   (:id this)
                                                   surrogate-key)
                                (api-params (:auth-key this)))]
      (if (not= (:status response) 200)
        (do (perf/publish {:failed-purge-count 1})
            (log/warn (:body response) {:path uri :key surrogate-key})
            false)
        (do
          (log/info (format "purged: %s using key %s" uri surrogate-key) {:path uri :key surrogate-key})
          true)))))

(defn create-fastly-api
  [creds]
  (->FastlyAPI (:id creds) (:auth-key creds) (:api-url creds)))

(defn purge-request-url
  [fastly-api id surrogate-key]
  (format "%s/service/%s/purge/%s"
          fastly-api
          id
          surrogate-key))

(defn api-params
  [auth-key]
  {:headers {"Fastly-Key" auth-key
             "Accept" "application/json"}})
