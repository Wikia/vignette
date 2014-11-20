(ns vignette.util.statsd
  (:require [environ.core :refer [env]]))

(def sample-rate (if (env :statsd-sample-rate)
                          (Double/parseDouble (env :statsd-sample-rate))
                          0.1))
