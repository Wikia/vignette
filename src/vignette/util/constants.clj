(ns vignette.util.constants
  (:require [environ.core :refer [env]]))

(def statsd-sample-rate (if (env :statsd-sample-rate)
                          (Double/parseDouble (env :statsd-sample-rate))
                          0.1))
