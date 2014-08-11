(ns vignette.storage.core
  (:require [vignette.storage.protocols :refer :all]
            [vignette.media-types :as mt]))

(defn- join-slash
  [& s]
  (clojure.string/join "/" s))

(defn get*
  [store bucket prefix path]
  (get-object store
              bucket
              (join-slash prefix path)))

(defn put*
  [store resource bucket prefix path]
  (put-object store
              resource
              bucket
              (join-slash prefix path)))

(defrecord LocalImageStorage [store original-prefix thumb-prefix]
  ImageStorageProtocol

  (save-thumbnail [this resource thumb-map]
    (put* (:store this)
          resource
          (mt/wikia thumb-map)
          (:thumb-prefix this)
          (mt/thumbnail-path thumb-map)))

  (get-thumbnail [this thumb-map]
    (get* (:store this)
          (mt/wikia thumb-map)
          (:thumb-prefix this)
          (mt/thumbnail-path thumb-map)))

  (save-original [this resource original-map]
    (put* (:store this)
          resource
          (mt/wikia original-map)
          (:original-prefix this)
          (mt/original-path original-map)))

  (get-original [this original-map]
    (get* (:store this)
          (mt/wikia original-map)
          (:original-prefix this)
          (mt/original-path original-map))))

(defn create-image-storage
  ([store original-prefix thumb-prefix]
   (->LocalImageStorage store original-prefix thumb-prefix))
  ([store]
   (create-image-storage store "originals" "thumbs")))
