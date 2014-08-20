(ns vignette.storage.common)

(defn create-storage-object
  [data content-type length]
  {:data data
   :content-type content-type
   :length length})

(defn data
  [storage-object]
  (get storage-object :data))

(defn content-type
  [storage-object]
  (get storage-object :content-type))

(defn length
  [storage-object]
  (get storage-object :length))
