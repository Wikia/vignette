FROM clojure:lein-2.8.1-alpine

RUN apk --no-cache add libwebp libwebp-tools imagemagick bash

ADD target/vignette-standalone.jar /vignette.jar
ADD bin/thumbnail /thumbnail
ADD public/brokenImage.jpg /public/brokenImage.jpg

EXPOSE 8080

ENTRYPOINT ["java"]
CMD ["-Xmx1024m", "-server", "-Dcom.sun.management.jmxremote", "-Dcom.sun.management.jmxremote.authenticate=false", "-Dcom.sun.management.jmxremote.ssl=false", "-jar", "/vignette.jar", "-m", "s3", "-p", "8080", "-C"]
