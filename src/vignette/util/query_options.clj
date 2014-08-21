(ns vignette.util.query-options)

(def query-opts-map {:fill "fill"})

(defn extract-query-opts
  [request]
  (reduce (fn [running [key val]]
            (if (contains? query-opts-map (keyword key))
              (assoc running (keyword key) val)
              running))
          {} (:query-params request)))

(defn query-opts
  [data]
  (if (empty? (:options data))
    nil
    (:options data)))

(defn query-opt
  [data opt]
  (get (query-opts data) opt))

(defn query-opts-str
  [data]
  (if-let [options (query-opts data)]
    (str "["
         (clojure.string/join "," (sort (map (fn [[k v]]
                                               (str (name k) "=" v))
                                             options)))
         "]")
    ""))

(defn query-opts->thumb-args
  [data]
  (reduce (fn [running [opt-key val]]
            (if-let [opt (get query-opts-map opt-key)]
              (conj running (str "--" opt) (str val))
              running))
          []
          (query-opts data)))

(defn modify-temp-file
  [data filename]
  (cond
    (= (query-opt data :fill) "transparent") (str "png:" filename)
    :else filename))