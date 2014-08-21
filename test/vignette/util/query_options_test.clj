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
       (request-options {:query-params {"fill" "blue"
                                        "unused" "foo"
                                        "unused2" "bar"}}) => {:fill "blue"})

(facts :q-opts
       (q-opts thumb-option-map) => {:fill "purple"}
       (q-opts thumb-map) => nil)

(facts :q-opt
       (q-opt thumb-map :foo) => nil
       (q-opt thumb-option-map :fill) => "purple"
       (q-opt thumb-option-map :foo) => nil)

(facts :q-opts-str
       (q-opts-str thumb-map) => ""
       (q-opts-str thumb-option-map) "[fill=purple]")