(ns vignette.api.legacy.routes
  (:require [clout.core :refer (route-compile route-matches)]))

;{
; "width" : "130px",
; "archive" : "",
; "input" : "/swift/v1/herofactory/images/thumb/7/7f/Brain_Attack.PNG/130px-102%2C382%2C0%2C247-Brain_Attack.PNG",
; "dbname" : "swift/v1/herofactory",
; "hook_name" : "image thumbnailer",
; "thumbpath" : "swift/v1/herofactory/images/thumb/7/7f/Brain_Attack.PNG",
; "thumbname" : "130px-102%2C382%2C0%2C247-Brain_Attack.PNG",
; "filename" : "Brain_Attack",
; "thumbext" : "102%2C382%2C0%2C247-Brain_Attack",
; "type" : "images",
; "fileext" : "PNG"
; }
(def image-thumbnail-swift
  (route-compile "/swift/:swift-version/:wikia/:image-type/thumb/:top-dir/:middle-dir/:original/:thumbnail"
                 {:wikia #"\w+"
                  :swift-version #"\w+"
                  :image-type #"images|avatars"
                  :top-dir #"[0-9a-f]{1}?"
                  :middle-dir #"[0-9a-f]{2}"}))
