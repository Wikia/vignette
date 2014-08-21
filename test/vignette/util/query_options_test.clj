(ns vignette.util.query-options-test
  (:require [vignette.util.query-options :refer :all]
            [midje.sweet :refer :all]))

(def thumb-map {:wikia "bucket"
                :top-dir "a"
                :middle-dir "ab"
                :original "boat.jpg"
                :revision "12345"
                :thumbnail-mode "thumbnail"
                :width "200"
                :height "300"
                :options {}})

(def thumb-option-map (assoc thumb-map :options {:fill "purple"}))

(facts :request-options
       (extract-query-opts {:query-params {"fill" "blue"
                                        "unused" "foo"
                                        "unused2" "bar"}}) => {:fill "blue"})

(facts :q-opts
       (query-opts thumb-option-map) => {:fill "purple"}
       (query-opts thumb-map) => nil)

(facts :q-opt
       (query-opt thumb-map :foo) => nil
       (query-opt thumb-option-map :fill) => "purple"
       (query-opt thumb-option-map :foo) => nil)

(facts :q-opts-str
       (query-opts-str thumb-map) => ""
       (query-opts-str thumb-option-map) "[fill=purple]")

(facts :query-opts->thumb-args
       (query-opts->thumb-args thumb-map) => []
       (query-opts->thumb-args thumb-option-map) => ["--fill" "purple"])