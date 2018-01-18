(ns vignette.storage.protocols)

(defprotocol StorageSystemProtocol
  (get-object [this bucket path])
  (put-object [this resource bucket path])
  (delete-object [this bucket path])
  (object-exists? [this bucket path])
  (list-buckets [this])
  (list-objects [this bucket path]))

(defprotocol ImageStorageProtocol
  (save-thumbnail [this resource thumb-map])
  (get-thumbnail [this thumb-map])
  (delete-thumbnails [this original-map])

  (save-original  [this resource original-map])
  (get-original  [this original-map])
  (original-exists? [this image-map]))

(defprotocol StoredObjectProtocol
  (file-stream [this])
  (content-length [this])
  (content-type [this])
  (filename [this])
  (etag [this])
  (transfer! [this to])
  (->response-object [this]))
