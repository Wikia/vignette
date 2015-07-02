(ns vignette.storage.core
  (:require [vignette.media-types :as mt]
            [vignette.storage.protocols :refer :all]
            [vignette.util.query-options :as q]))

(defn- join-slash
  [& s]
  (clojure.string/join "/" s))

(defn get*
  [store object-map get-path]
  (get-object store
              (mt/wikia object-map)
              (get-path object-map)))

(defn put*
  [store resource object-map get-path]
  (put-object store
              resource
              (mt/wikia object-map)
              (get-path object-map)))

(defn exists?
  [store object-map get-path]
  (object-exists? store
                  (mt/wikia object-map)
                  (get-path object-map)))

(defrecord ImageStorage [store cache-thumbnails]
  ImageStorageProtocol

  (save-thumbnail [this resource thumb-map]
    (when (:cache-thumbnails this)
      (put* (:store this)
            resource
            thumb-map
            mt/thumbnail-path)))

  (get-thumbnail [this thumb-map]
    (when (:cache-thumbnails this)
      (get* (:store this)
            thumb-map
            mt/thumbnail-path)))

  (save-original [this resource original-map]
    (put* (:store this)
          resource
          original-map
          mt/original-path))

  (get-original [this original-map]
    (get* (:store this)
          original-map
          mt/original-path))

  (original-exists? [this image-map]
    (exists? (:store this)
             image-map
             mt/original-path)))

(defn create-image-storage
  ([store cache-thumbnails]
   (->ImageStorage store cache-thumbnails))
  ([store]
   (create-image-storage store true)))
