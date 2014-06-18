(ns vignette.storage.core)

(defprotocol ImageStorageProtocol
  (get-object [this bucket path])
  (put-object [this resource bucket path])
  (delete-object [this bucket path])
  (list-buckets [this])
  (list-objects [this bucket]))
