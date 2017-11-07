(ns vignette.storage.static-assets-test
  (:require [midje.sweet :refer :all]
            [org.httpkit.client :as http]
            [vignette.storage.protocols :refer :all]
            [vignette.storage.static-assets :as sa]
            ))

(facts :static-assets :get-original
       (let [store (create-image-storage ..disk-store..)]

       (get-original
         (sa/create-static-image-storage store --static-asset-get--) {:uuid ..uuid..}) => ..object..
       (provided
         (--static-asset-get-- ..uuid..) => ..static-asset-url..
         (http/get ..static-asset-url.. {:as :stream}) => (future {:status 200})
         (sa/->AsyncResponseStoredObject {:status 200}) => ..object..)
       (get-original
         (sa/create-static-image-storage store --static-asset-get--) {:uuid ..uuid.. :options {:status ..statuses..}}) => nil
       (provided
         (--static-asset-get-- ..uuid..) => ..static-asset-url..
         (http/get ..static-asset-url.. {:as :stream}) => (future {:status 404})))

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
         (http/get ..static-asset-url.. {:as :stream}) => (future {:status 451})
         (--static-asset-get-- ..placeholder-id..) => ..placeholder-url..
         (http/get ..placeholder-url.. {:as :stream}) => (future {:status 200})
         (sa/->AsyncResponseStoredObject {:status 200}) => ..placeholder..))
