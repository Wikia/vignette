(ns vignette.setup
  (:require [cheshire.core :refer :all]
            [vignette.http.legacy.routes :as hlr]
            [vignette.http.api-routes :refer [uuid-routes wiki-routes metrics-routes]]
            [vignette.util.integration :as i]
            [vignette.storage.s3 :refer [create-s3-storage-system storage-creds]]
            [vignette.storage.core :refer [create-image-storage]]
            [vignette.storage.local :refer [create-local-storage-system]]
            [vignette.util.services :refer [materialize-static-asset-url]]
            [vignette.storage.static-assets :refer [create-static-image-storage]]
            [prometheus.core :as prometheus]))


(defn- create-object-storage [opts]
  (if (= (:mode opts) "local")
    (do
      (i/create-integration-env)
      (create-local-storage-system i/integration-path))
    (create-s3-storage-system storage-creds)))

(defn create-stores [opts]
  {
    :wikia-store  (create-image-storage (create-object-storage opts) (:cache-thumbnails opts))
    :static-store (create-static-image-storage (create-object-storage opts) (materialize-static-asset-url))
   })

(defn image-routes [stores]
  (concat
    (list
      (metrics-routes)
      (uuid-routes (:static-store stores))
      (wiki-routes (:wikia-store stores))
     )
    (hlr/legacy-routes (:wikia-store stores))))
