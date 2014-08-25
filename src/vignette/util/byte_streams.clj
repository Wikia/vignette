(ns vignette.util.byte-streams
  (:import [com.amazonaws.services.s3.model S3ObjectInputStream]))


; See also https://github.com/http-kit/http-kit/blob/3fbeda1b31d90efb0ff4c208a61328ee0cf5b305/src/java/org/httpkit/HttpUtils.java#L88
; for dealing with streams.
(defn read-byte-stream-core
  [resource length]
  (with-open [stream resource]
    (let [output (byte-array length)]
      (loop [offset 0
             nbytes (.read stream output offset length)]
        (if (or (neg? nbytes) (>= (+ offset nbytes) length))
          output
          (recur (+ offset nbytes)
                 (.read stream output (+ offset nbytes) length)))))))

(defmulti read-byte-stream (fn [resource length] (class resource)))

(defmethod read-byte-stream S3ObjectInputStream
  [resource length]
  (read-byte-stream-core resource length))

(defmethod read-byte-stream :default
  [resource length]
  (read-byte-stream-core (clojure.java.io/input-stream resource) length))

(defn write-byte-stream
  [resource array]
  (with-open [stream (clojure.java.io/output-stream resource)]
    (.write stream array)))
