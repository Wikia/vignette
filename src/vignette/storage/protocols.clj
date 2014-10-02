(ns vignette.storage.protocols)

(defprotocol ObjectStorageProtocol
  (get-object [this bucket path])
  (put-object [this resource bucket path])
  (delete-object [this bucket path])
  (list-buckets [this])
  (list-objects [this bucket]))

(defprotocol ImageStorageProtocol
  (save-thumbnail [this resource thumb-map])
  (get-thumbnail [this thumb-map])

  (save-original  [this resource original-map])
  (get-original  [this original-map]))

(defprotocol StoredObjectProtocol
  (file-stream [this])
  (content-length [this])
  (content-type [this]))
