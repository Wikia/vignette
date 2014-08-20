(ns vignette.storage.common)

(defn create-storage-object
  [file-stream content-type length]
  {:file-stream file-stream
   :content-type content-type
   :length length})

(defn file-stream
  [storage-object]
  (get storage-object :file-stream))

(defn content-type
  [storage-object]
  (get storage-object :content-type))

(defn length
  [storage-object]
  (get storage-object :length))
