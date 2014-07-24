(ns vignette.util.thumbnail
  (:require [vignette.storage.protocols :as store-prot]
            [vignette.storage.local :as store-loc]
            [vignette.protocols :refer :all]
            [clojure.java.shell :refer (sh)]
            [clojure.java.io :as io]
            [cheshire.core :refer :all]))

(defn temp-filename
  [thumb-map]
  (let [filename (store-loc/resolve-local-path
                   "/tmp/vignette"
                   "_temp"
                   (generate-string (merge
                                      thumb-map
                                      {:ts (System/currentTimeMillis)})))]
    (store-loc/create-local-path (store-loc/get-parent filename))
    filename))

(def options-map {:height "height"
                  :width "width"
                  :mode "mode"
                  })

(defn thumbnail-options
  [thumb-map]
  (loop [acc []
         params thumb-map]
    (let [[opt-key val] (first params)
          opt (get options-map opt-key)]
      (cond
        (empty? params) acc
        (nil? opt) (recur acc (rest params))
        :else (recur (conj acc (str "--" opt) (str val)) (rest params))))))

(defn generate-thumbnail
  [resource thumb-map]
  (let [temp-file (temp-filename thumb-map)
        base-command ["bin/thumbnail"
                      "--in" (.getAbsolutePath resource)
                      "--out" temp-file]
        thumb-options (thumbnail-options thumb-map)
        command (reduce conj base-command thumb-options)
        sh-out (apply sh command)]
    (cond
      (zero? (:exit sh-out)) (io/file temp-file)
      :else nil))) ; todo: add some logging here

(defn get-thumbnail
  [system thumb-map]
  (if-let [thumb (store-prot/get-thumbnail (store system) thumb-map)]
    thumb
    (if-let [thumb (generate-thumbnail (store-prot/get-original (store system) thumb-map)
                                       thumb-map)]
      (and (store-prot/save-thumbnail (store system) thumb thumb-map)
           (io/delete-file thumb)
           (store-prot/get-thumbnail (store system) thumb-map))
      nil)))
