(ns vignette.storage.static-assets-test
  (:require [midje.sweet :refer :all]
            [org.httpkit.client :as http]
            [vignette.storage.protocols :refer :all]
            [vignette.storage.static-assets :as sa]
            ))

(facts :static-assets :get-original
       (get-original
         (sa/create-static-image-storage --url-get--) {:uuid ..uuid..}) => ..object..
       (provided
         (--url-get-- ..uuid..) => ..url..
         (http/get ..url.. {:as :stream}) => (future {:status 200})
         (sa/->AsyncResponseStoredObject {:status 200}) => ..object..)
       (get-original
         (sa/create-static-image-storage --url-get--) {:uuid ..uuid..}) => nil
       (provided
         (--url-get-- ..uuid..) => ..url..
         (http/get ..url.. {:as :stream}) => (future {:status 404})))

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
