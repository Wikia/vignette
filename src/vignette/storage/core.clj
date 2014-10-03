(ns vignette.storage.core
  (:require [vignette.media-types :as mt]
            [vignette.storage.protocols :refer :all]
            [vignette.util.query-options :as q]))

(defn- join-slash
  [& s]
  (clojure.string/join "/" s))

(defn get*
  [store object-map prefix get-path]
  (get-object store
              (mt/wikia object-map)
              (join-slash (q/query-opts->image-prefix object-map prefix)
                          (get-path object-map))))

(defn put*
  [store resource object-map prefix get-path]
  (put-object store
              resource
              (mt/wikia object-map)
              (join-slash (q/query-opts->image-prefix object-map prefix)
                          (get-path object-map))))

(defrecord ImageStorage [store original-prefix thumb-prefix]
  ImageStorageProtocol

  (save-thumbnail [this resource thumb-map]
    (put* (:store this)
          resource
          thumb-map
          (:thumb-prefix this)
          mt/thumbnail-path))

  (get-thumbnail [this thumb-map]
    (get* (:store this)
          thumb-map
          (:thumb-prefix this)
          mt/thumbnail-path))

  (save-original [this resource original-map]
    (put* (:store this)
          resource
          original-map
          (:original-prefix this)
          mt/original-path))

  (get-original [this original-map]
    (get* (:store this)
          original-map
          (:original-prefix this)
          mt/original-path)))

(defn create-image-storage
  ([store original-prefix thumb-prefix]
   (->ImageStorage store original-prefix thumb-prefix))
  ([store]
   (create-image-storage store "images" "images/thumb")))
