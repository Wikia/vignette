(ns vignette.storage.core
  (:require [vignette.storage.protocols :refer :all]
            [vignette.media-types :as mt]))

(defn- join-slash
  [& s]
  (clojure.string/join "/" s))

(defrecord LocalImageStorage [store original-prefix thumb-prefix]
  ImageStorageProtocol

  ; ^thumb-map mt/->MediaThmubnailFile :- bool
  (save-thumbnail [this resource thumb-map]
    (let [path (mt/thumbnail-path thumb-map)]
      (put-object (:store this)
                  resource
                  (mt/wikia thumb-map)
                 (join-slash (:thumb-prefix this) path))))

  ; ^thumb-map mt/->MediaThumbnailFile :- resource
  (get-thumbnail [this thumb-map]
    (let [path (mt/thumbnail-path thumb-map)]
      (get-object (:store this)
                  (mt/wikia thumb-map)
                  (join-slash (:thumb-prefix this) path))))

  (save-original [this resource original-map])
  (get-original [this original-map]))


(defn create-local-image-storage
  [store original-prefix thumb-prefix]
  (->LocalImageStorage store original-prefix thumb-prefix))
