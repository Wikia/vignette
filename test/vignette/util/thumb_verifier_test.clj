(ns vignette.util.thumb-verifier-test
  (:require [midje.sweet :refer :all]
            [vignette.util.thumb-verifier :as verify]))

;estimator tests

(facts :requested-sizes
  (verify/requested-size {:a "irrelevant" :width "100" :height "200"})
    => {:width 100 :height 200}
  (verify/requested-window-size {:window-width "300" :window-height "150" :x 1})
    => {:width 300 :height 150})

(facts :scale-width
  (verify/scale-width {:width 150} {:width 100 :height 200})
    => {:width 150 :height 300}
  (verify/scale-width {:width 50} {:width 100 :height 200})
    => {:width 50 :height 100})

(facts :scale-width-upto-original
  (verify/scale-width-upto-original {:width 150} {:width 100 :height 200})
    => {:width 100 :height 200}
  (verify/scale-width-upto-original {:width 50} {:width 100 :height 200})
    => {:width 50 :height 100})

(facts :scale-height-upto-original
  (verify/scale-height-upto-original {:height 250} {:width 100 :height 200})
    => {:width 100 :height 200}
  (verify/scale-height-upto-original {:height 50} {:width 100 :height 200})
    => {:width 25 :height 50})

(facts :scale-proportionally
  (verify/scale-proportionally {:width 60 :height 30}
                               {:width 100 :height 100})
    => {:width 30 :height 30}
  (verify/scale-proportionally {:width 50 :height 100}
                               {:width 100 :height 100})
    => {:width 50 :height 50})

(facts :scale-window-width
  (verify/scale-window-width {:window-width 100 :window-height 50 :width 150})
    => {:width 150 :height 75}
  (verify/scale-window-width {:window-width 100 :window-height 50 :width 50})
    => {:width 50 :height 25})

(facts :fit-in-original
  (verify/fit-in-original {:width 50 :height 40} {:width 100 :height 100})
    => {:width 50 :height 40}
  (verify/fit-in-original {:width 100 :height 100} {:width 100 :height 100})
    => {:width 100 :height 100}
  (verify/fit-in-original {:width 250 :height 150} {:width 100 :height 100})
    => {:width 100 :height 60}
  (verify/fit-in-original {:width 150 :height 250} {:width 100 :height 100})
    => {:width 60 :height 100})

(facts :scale-and-fit-in-original
  (verify/scale-and-fit-in-original {:width 50 :height 40}
                                    {:width 100 :height 100})
    => {:width 40 :height 40}
  (verify/scale-and-fit-in-original {:width 40 :height 50}
                                    {:width 100 :height 100})
    => {:width 40 :height 40}
  (verify/scale-and-fit-in-original {:width 250 :height 150}
                                    {:width 100 :height 100})
    => {:width 100 :height 100}
  (verify/scale-and-fit-in-original {:width 150 :height 250}
                                    {:width 100 :height 100})
    => {:width 100 :height 100})

(facts :not-close-in-size
  (verify/not-close-in-size {:width 100 :height 100}
                            {:width 100 :height 100})
    => false
  (verify/not-close-in-size {:width 100 :height 100}
                            {:width 101 :height 100})
    => false
  (verify/not-close-in-size {:width 100 :height 100}
                            {:width 99 :height 100})
    => false
  (verify/not-close-in-size {:width 100 :height 100}
                            {:width 100 :height 101})
    => false
  (verify/not-close-in-size {:width 100 :height 100}
                            {:width 100 :height 99})
    => false
  (verify/not-close-in-size {:width 100 :height 100}
                            {:width 101 :height 101})
    => false
  (verify/not-close-in-size {:width 100 :height 100}
                            {:width 99 :height 99})
    => false
  (verify/not-close-in-size {:width 100 :height 100}
                            {:width 102 :height 100})
    => true
  (verify/not-close-in-size {:width 100 :height 100}
                            {:width 100 :height 102})
    => true)

(facts :area-ratio
  (verify/area-ratio {} {:width 100 :height 100}) => 0.0
  (verify/area-ratio {:width 100 :height 100} {:width 100 :height 0}) => 0.0
  (verify/area-ratio {:width 100 :height 100} {:width 100 :height 100}) => 1.0
  (verify/area-ratio {:width 150 :height 100} {:width 100 :height 100}) => 1.5
  (verify/area-ratio {:width 50 :height 100} {:width 100 :height 100}) => 0.5)
