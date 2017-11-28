(ns vignette.storage.static-assets-test
    (:require [midje.sweet :refer :all]
      [org.httpkit.client :as http]
      [vignette.storage.core :refer :all]
      [vignette.storage.local :refer :all]
      [vignette.storage.static-assets :as sa]
      [vignette.storage.protocols :refer :all]
      ))

(facts :static-assets :get-original
       (let [store (create-image-storage ..disk-store..)]

            (get-original
              (sa/create-static-image-storage store --static-asset-get--) {:uuid ..uuid..}) => ..object..
            (provided
              (--static-asset-get-- ..uuid..) => ..static-asset-url..
              (http/get ..static-asset-url.. {:as :stream :user-agent "vignette"}) => (future {:status 200})
              (sa/->AsyncResponseStoredObject {:status 200}) => ..object..)
            (get-original
              (sa/create-static-image-storage store --static-asset-get--) {:uuid ..uuid.. :options {:status ..statuses..}}) => nil
            (provided
              (--static-asset-get-- ..uuid..) => ..static-asset-url..
              (http/get ..static-asset-url.. {:as :stream :user-agent "vignette"}) => (future {:status 404}))))

(facts :static-assets :filename
       (filename
         (sa/->AsyncResponseStoredObject ..response..)) => "filename.png"
       (provided
         ..response.. =contains=> {:headers {:content-disposition "Filename=\"filename.png\""}})
       (filename
         (sa/->AsyncResponseStoredObject ..response..)) => nil
       (provided
         ..response.. =contains=> {:headers {:content-disposition "qilename=\"filename.png\""}})
       (filename
         (sa/->AsyncResponseStoredObject ..response..)) => nil
       (provided
         ..response.. =contains=> {:headers {}})
       (filename
         (sa/->AsyncResponseStoredObject ..response..)) => nil)

(facts :static-assets :get-blocked-placeholder
       (let [store (create-image-storage ..disk-store..)]

            (get-original
              (sa/create-static-image-storage store --static-asset-get--) {:uuid ..uuid.. :blocked-placeholder ..placeholder-id..}) => ..placeholder..
            (provided
              (--static-asset-get-- ..uuid..) => ..static-asset-url..
              (http/get ..static-asset-url.. {:as :stream :user-agent "vignette"}) => (future {:status 451})
              (--static-asset-get-- ..placeholder-id..) => ..placeholder-url..
              (http/get ..placeholder-url.. {:as :stream :user-agent "vignette"}) => (future {:status 200})
              (sa/->AsyncResponseStoredObject {:status 200}) => ..placeholder..)))

(facts :static-assets :save-thumbnail
       (let [store (create-image-storage ..disk-store..)]

            (save-thumbnail (sa/create-static-image-storage store --static-asset-get--) ..resource..
                            {:height         :auto, :requested-format nil, :options {:format "webp"},
                             :image-type     "images", :request-type :thumbnail,
                             :thumbnail-mode "scale-to-width-down", :width 690,
                             :uuid           "d6f0194a-ea6a-410d-9c45-81411b43abcd"}) => nil
            (provided
              (put-object #vignette.storage.core.ImageStorage{:store ..disk-store.., :cache-thumbnails true}
                          ..resource..
                          "abcd" "images/thumb/d6f0194a-ea6a-410d-9c45-81411b43abcd/690px-autopx-scale-to-width-down[format=webp]") => nil)

            (save-thumbnail (sa/create-static-image-storage store --static-asset-get--) ..resource..
                            {:height         :auto, :requested-format nil, :options {:format "webp"},
                             :image-type     "images", :request-type :thumbnail,
                             :thumbnail-mode "scale-to-width-down", :width 690,
                             :uuid           "wrong-uuid"}) => (throws #"Incorrect UUID")

            (save-thumbnail (sa/create-static-image-storage store --static-asset-get--) ..resource..
                            {:requested-format nil, :options {:format "webp"},
                             :image-type       "images", :request-type :thumbnail,
                             :thumbnail-mode   nil, :uuid "d6f0194a-ea6a-410d-9c45-81411b43abcd"}) => nil
            (provided
              (put-object #vignette.storage.core.ImageStorage{:store ..disk-store.., :cache-thumbnails true}
                          ..resource..
                          "abcd" "images/thumb/d6f0194a-ea6a-410d-9c45-81411b43abcd/nullpx-nullpx-null[format=webp]") => nil)))

(facts :static-assets :get-thumbnail
       (let [store (create-image-storage ..disk-store..)]

            (get-thumbnail (sa/create-static-image-storage store --static-asset-get--)
                            {:height         :auto, :requested-format nil, :options {:format "webp"},
                             :image-type     "images", :request-type :thumbnail,
                             :thumbnail-mode "scale-to-width-down", :width 690,
                             :uuid           "d6f0194a-ea6a-410d-9c45-81411b43abcd"}) => ..resource..
            (provided
              (get-object #vignette.storage.core.ImageStorage{:store ..disk-store.., :cache-thumbnails true}
                          "abcd" "images/thumb/d6f0194a-ea6a-410d-9c45-81411b43abcd/690px-autopx-scale-to-width-down[format=webp]") => ..resource..)

            (get-thumbnail (sa/create-static-image-storage store --static-asset-get--)
                            {:height         :auto, :requested-format nil, :options {:format "webp"},
                             :image-type     "images", :request-type :thumbnail,
                             :thumbnail-mode "scale-to-width-down", :width 690,
                             :uuid           "wrong-uuid"}) => (throws #"Incorrect UUID")

            (get-thumbnail (sa/create-static-image-storage store --static-asset-get--)
                            {:requested-format nil, :options {:format "webp"},
                             :image-type       "images", :request-type :thumbnail,
                             :thumbnail-mode   nil, :uuid "d6f0194a-ea6a-410d-9c45-81411b43abcd"}) => ..resource..
            (provided
              (get-object #vignette.storage.core.ImageStorage{:store ..disk-store.., :cache-thumbnails true}
                          "abcd" "images/thumb/d6f0194a-ea6a-410d-9c45-81411b43abcd/nullpx-nullpx-null[format=webp]") => ..resource..)))
