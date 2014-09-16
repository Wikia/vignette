(ns vignette.util.query-options)

; regex of valid inputs for query args
(def query-opts-map {:fill #"^#[a-g0-9]+$|^\w+$"
                     :format #"^\w+$"
                     :lang #"^\w+$"})

(defn extract-query-opts
  [request]
  (reduce (fn [running [key val]]
            (if (and (contains? query-opts-map (keyword key))
                     (re-matches ((keyword key) query-opts-map) val))
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
            (if-let [opt (name opt-key)]
              (conj running (str "--" opt) (str val))
              running))
          []
          (query-opts data)))

(defn query-opts->image-prefix
  [data prefix]
  (if-let [lang (query-opt data :lang)]
    (str lang "/" prefix)
    prefix))

(defn modify-temp-file
  [data filename]
  (cond
    (query-opt data :format) (str (query-opt data :format) ":" filename)
    (= (query-opt data :fill) "transparent") (str "png:" filename)
    :else filename))