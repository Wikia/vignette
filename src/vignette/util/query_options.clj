(ns vignette.util.query-options
  (:require [vignette.util.regex :refer [uuid-regex]]))

(defn create-query-opt
  ([re side-effects command-line-option]
   {:regex re :side-effects side-effects :command-line-option command-line-option})
  ([re side-effects]
   (create-query-opt re side-effects false) )
  ([re]
   (create-query-opt re true false)))

; regex of valid inputs for query arg
(def query-opts-map {:fill (create-query-opt #"^#[a-g0-9]+$|^\w+$" true true)
                     :format (create-query-opt #"^\w+$")
                     :frame (create-query-opt #"[0-9]+" true true)
                     :status (create-query-opt #"^\w+$")
                     :fastlyBypass (create-query-opt uuid-regex)
                     :path-prefix (create-query-opt #"[\w\.\/-]+" false)
                     :replace (create-query-opt #"^true$" false)
                     :zone (create-query-opt #"\w+")})

(defn query-opt-regex
  [query-opt]
  (get query-opt :regex))

(defn query-opt-side-effects
  [query-opt]
  (get query-opt :side-effects))

(defn query-opts-with-side-effects
  []
  (into {} (filter (fn [[k v]]
                     (= (:side-effects v) true))
                   query-opts-map)))

(defn query-opts-command-line-options
  []
  (into {} (filter (fn [[k v]]
                     (= (:command-line-option v) true))
                   query-opts-map)))

(defn extract-query-opts
  [request]
  (reduce (fn [running [key val]]
            (if (and (contains? query-opts-map (keyword key))
                     (re-matches (query-opt-regex (get query-opts-map (keyword key))) val))
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
  (let [only (keys (query-opts-with-side-effects))
        options (select-keys (query-opts data) only)]
    (if (not-empty options)
      (str "["
           (clojure.string/join "," (sort (map (fn [[k v]]
                                                 (str (name k) "=" (clojure.string/replace v "/" "-")))
                                               options)))
           "]")
      "")))

(defn query-opts->thumb-args
  [data]
  (reduce (fn [running [opt-key val]]
            (if-let [opt (name opt-key)]
              (conj running (str "--" opt) (str val))
              running))
          []
          (select-keys (query-opts data)
                       (keys (query-opts-command-line-options)))))

(defn modify-temp-file
  [data filename]
  (cond
    (query-opt data :format) (str (query-opt data :format) ":" filename)
    (= (query-opt data :fill) "transparent") (str "png:" filename)
    :else filename))
