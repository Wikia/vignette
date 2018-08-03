#!/bin/bash

docker run -e "STORAGE_ACCESS_KEY=6J1ZCE0KZZYG2U9CHJGA" \
-e "STORAGE_SECRET_KEY=gpCJjIptrr5S7XOGYG2bH4yXnru+YN/O7hBNyPyf" \
-e "STORAGE_ENDPOINT=http://dfs" \
-e "STORAGE_PROXY=dev-dfs-p6" \
-e "STORAGE_PROXY_PORT=80" \
-e "LOGGER_TYPE=stdout" \
-e "LOGGER_FILE_OUTPUT=/tmp/vignette.log" \
-e "LOGGER_APPLICATION=vignette" \
-e "IMAGEMAGICK_BASE=/usr" \
-e "VIGNETTE_THUMBNAIL_BIN=/thumbnail" \
-e "GETOPT=/bin/getopt" \
-e "TIMEOUT=timeout" \
-e "TIMEOUT_TIME=-t 30" \
-e "GIF2WEBP=/usr/bin/gif2webp" \
-e "WIKIA_ENVIRONMENT=dev" \
-e "WIKIA_DATACENTER=poz" -it -p 8080:8080 artifactory.wikia-inc.com/vignette:0.4
