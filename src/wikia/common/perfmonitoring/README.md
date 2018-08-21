# PerfMonitoring
Use to send data to InfluxDB

## Usage
The following environment vars are supported:

* `PERFMONITORING_HOST` influxdb host to connect to
* `PERFMONITORING_PORT` port to send data (default: 5551)
* `PERFMONITORING_APP` app name, used as a prefix to series names (default: "wikia")
* `PERFMONITORING_SERIES_NAME` the default series name (without `PERFMONITORING_APP` prefix) to use for data (default: "metrics")

Example uses:
```clojure
(:require [wikia.common.perfmonitoring.core :refer [publish timing series-timing]])
(publish {:my-column 2 :my-other-column "value"}) ; publishes to wikia_metrics
(publish :my-series {:my-column 2 :my-other-column "value"}) ; publishes to wikia_my_series
(timing :request-time (prepare-request request) (run-request request)) ; publishes timing data to wikia_metrics
(series-timing :my-series :request-time (prepare-request request) (do-request)) ; publishes timing data to wikia_my_series
```

`series-timing` functions the same as `timing`, except that the series name can be provided.
