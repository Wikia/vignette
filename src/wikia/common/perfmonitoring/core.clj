(ns wikia.common.perfmonitoring.core
  (:import (java.net DatagramSocket DatagramPacket InetAddress))
  (:require [cheshire.core :as json]
            [environ.core :refer [env]]))

(declare format-content
         format-series-name
         send-data
         write-listener)

(def ^{:private true} host (env :perfmonitoring-host))
(def ^{:private true} port (Integer. (env :perfmonitoring-port 5551)))
(def ^{:private true} app (.toLowerCase (env :perfmonitoring-app "wikia")))
(def ^{:private true} series-name (env :perfmonitoring-series-name :metrics))
(def ^{:private true} config (atom nil))

(defn get-series-name
  []
  series-name)

(defn init []
  (when (env :perfmonitoring-host)
    (swap! config #(or % {:host (InetAddress/getByName host)
                          :port port
                          :socket (DatagramSocket.)}))))

(defn format-series-name [series-name]
  (let [series-name (if (keyword? series-name)
                      (name series-name)
                      series-name)]
    (keyword (str app "_" (clojure.string/replace (.toLowerCase series-name) "-" "_")))))

(defn format-content [point]
  (let [series-name (:series-name point)
        point (dissoc point :series-name)
        columns (keys point)
        vals (vals point)]
    [{:name series-name
      :columns columns
      :points (list vals)}]))

(defn send-data [content]
  content
  (when-let [packet (try
                      (DatagramPacket.
                        ^"[B" (.getBytes content)
                        ^Integer (count content)
                        ^InetAddress (:host @config)
                        ^Integer (:port @config))
                      (catch Exception e
                        nil))]
    (.send (:socket @config) packet)))

(defn publish
  ([series-name point]
    (when @config
      (let [point (merge {:series-name (format-series-name series-name)} point)]
        (future (send-data (json/generate-string (format-content point)))))))
  ([point]
    (publish (get-series-name) point)))

(defn current-time
  []
  (System/currentTimeMillis))

(defmacro timing [metric & body]
  `(let [start# (current-time)]
     (try
       ~@body
       (finally
         (publish (get-series-name) {~metric (- (current-time) start#)})))))
