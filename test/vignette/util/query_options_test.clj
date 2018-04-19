(ns vignette.util.query-options-test
  (:require [midje.sweet :refer :all]
            [vignette.util.query-options :refer :all]))

(def thumb-map {:wikia "bucket"
                :top-dir "a"
                :middle-dir "ab"
                :original "boat.jpg"
                :revision "12345"
                :thumbnail-mode "thumbnail"
                :width "200"
                :height "300"
                :options {}})

(def thumb-option-map (assoc thumb-map :options {:fill "purple" :lang "zh" :replace true}))

(def status-option-map (assoc thumb-map :options {:status "REJECTED"}))

(facts :create-query-opt
  (create-query-opt #"\w+") => (contains {:regex #"\w+" :side-effects true}))

(facts :request-options
       (extract-query-opts {:query-params {"format" ["webp" "jpg"]}}) => {:format "webp"}
       (extract-query-opts {:query-params {"fill" "blue"
                                           "unused" "foo"
                                           "unused2" "bar"}}) => {:fill "blue"}
       (extract-query-opts {:query-params {"fill" ";rm -rf *;"}}) => {}
       (extract-query-opts {:query-params {"format" "ls -l"}}) => {}
       (extract-query-opts {:query-params {"status" "REJECTED"}}) => {:status "REJECTED"})

(facts :query-opts
       (query-opts thumb-option-map) => {:fill "purple" :lang "zh" :replace true}
       (query-opts thumb-map) => nil
       (query-opts status-option-map) => {:status "REJECTED"})

(facts :query-opt
       (query-opt thumb-map :foo) => nil
       (query-opt thumb-option-map :fill) => "purple"
       (query-opt thumb-option-map :foo) => nil
       (query-opt status-option-map :status) => "REJECTED"
       (query-opt thumb-option-map :status) => nil)

(facts :query-opts-str
       (query-opts-str thumb-map) => ""
       (query-opts-str thumb-option-map) => "[fill=purple]")

(facts :query-opts->thumb-args
       (query-opts->thumb-args thumb-map) => []
       (query-opts->thumb-args thumb-option-map) => ["--fill" "purple"])
