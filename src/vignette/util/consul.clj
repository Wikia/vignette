(ns vignette.util.consul
  (:require [consul.core :as consul]
            [environ.core :refer [env]]))

(def default-consul-hostname "localhost")
(def default-consul-http-port 8500)
(def default-service-query-tag "dev")

(def service-query-tag (env :consul-query-tag default-service-query-tag))

(def env-consul-conn {:scheme      :http
                      :server-name (env :consul-hostname default-consul-hostname)
                      :server-port (env :consul-http-port default-consul-http-port)})

(def pick-service-entry-fn rand-nth)

(defn- response->address [response]
  (when (not-empty response)
    (let [entry (pick-service-entry-fn response)]
      (let [address (-> entry :Node :Address)
            port (-> entry :Service :Port)]
        (if (not (or (nil? address) (nil? port)))
          {:address address, :port port})))))

(defprotocol Consul
  (query-service [this service tag]))

(defrecord ConnConsul [consul-conn] Consul
  (query-service [_ service tag]
    (consul/service-health
      consul-conn service :passing? true :tag tag)))

(def create-consul (ConnConsul. env-consul-conn))

(defn find-service
  ([consul service]
   (find-service consul service service-query-tag))
  ([consul service tag]
   (response->address (query-service consul service tag))))

(defn ->uri [service] (str "http://" (service :address) ":" (service :port)))
