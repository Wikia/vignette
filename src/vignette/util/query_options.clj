(ns vignette.util.query-options)

(def q-opts-map {:fill "fill"})

(defn request-options
  [request]
  (reduce (fn [running [key val]]
            (if (contains? q-opts-map (keyword key))
              (assoc running (keyword key) val)
              running))
          {} (:query-params request)))

(defn q-opts
  [data]
  (if (empty? (:options data))
    nil
    (:options data)))

(defn q-opt
  [data opt]
  (get (q-opts data) opt))

(defn q-opts-str
  [data]
  (if-let [options (q-opts data)]
    (str "["
         (clojure.string/join "," (map (fn [[k v]]
                                         (str (name k) "=" v))
                                       options))
         "]")
    ""))

(defn query->thumb-options
  [data]
  (reduce (fn [running [opt-key val]]
            (if-let [opt (get q-opts-map opt-key)]
              (conj running (str "--" opt) (str val))
              running))
          []
          (q-opts data)))

(defn modify-temp-file
  [data filename]
  (cond
    (= (q-opt data :fill) "transparent") (str "png:" filename)))