FROM java:8-jre-alpine

RUN apk --no-cache add libwebp imagemagick bash
ADD target/vignette-standalone.jar /srv/vignette.jar
ADD bin/thumbnail /srv/thumbnail

EXPOSE 8080 5001

ENTRYPOINT ["java"]
CMD ["-Xmx1024m", "-server", "-Dcom.sun.management.jmxremote", "-Dcom.sun.management.jmxremote.port=5001", "-Dcom.sun.management.jmxremote.authenticate=false", "-Dcom.sun.management.jmxremote.ssl=false", "-jar", "/srv/vignette.jar", "-m", "s3"]
