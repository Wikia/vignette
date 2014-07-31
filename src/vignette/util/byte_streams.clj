(ns vignette.util.byte-streams)

(defn read-byte-stream
  [resource length]
  (with-open [stream (clojure.java.io/input-stream resource)] 
    (let [output (byte-array length)]
      (loop [offset 0
             nbytes (.read stream output offset length)]
        (if (or (neg? nbytes) (>= (+ offset nbytes) length))
          output
          (recur (+ offset nbytes)
                 (.read stream output (+ offset nbytes) length)))))))

(defn write-byte-stream
  [resource array]
  (with-open [stream (clojure.java.io/output-stream resource)]
    (.write stream array)))
