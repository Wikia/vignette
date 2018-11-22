FROM ubuntu:14.04.1

RUN echo "deb [arch=amd64] http://repo.prod.wikia.net/ubuntu trusty main restricted universe multiverse\n" > /etc/apt/sources.list && \
    echo "deb [arch=amd64] http://repo.prod.wikia.net/ubuntu trusty-updates main restricted universe multiverse\n" >> /etc/apt/sources.list && \
    echo "deb [arch=amd64] http://repo.prod.wikia.net/ubuntu trusty-security main restricted universe multiverse\n" >> /etc/apt/sources.list && \
    echo "deb [arch=amd64] http://repo.prod.wikia.net/ubuntu trusty-backports main restricted universe multiverse\n" >> /etc/apt/sources.list && \
    echo "deb [arch=amd64] \"http://repo.prod.wikia.net/repository/dists/trusty/amd64\" /" > /etc/apt/sources.list.d/wikia.list && \
    echo "deb [arch=amd64] \"http://repo.prod.wikia.net/repository\" trusty-wikia main" > /etc/apt/sources.list.d/trusty-wikia.list && \
    apt-get update && \
    echo debconf shared/accepted-oracle-license-v1-1 select true | debconf-set-selections && \
    echo debconf shared/accepted-oracle-license-v1-1 seen true | debconf-set-selections && \
    apt-get install -y --force-yes libgraphviz-dev libfftw3-3 libopenexr6 libdjvulibre21 libautotrace3 imagemagick-7.0.5 libwebp libwmf-dev librsvg2-dev oracle-java8-installer bc && \
    rm -rf /var/lib/apt/lists/*

RUN adduser --home /nonexistent --gecos '' --disabled-password service_user

ADD public/brokenImage.jpg /public/brokenImage.jpg
ADD bin/thumbnail /thumbnail
ADD target/vignette-standalone.jar /vignette.jar

EXPOSE 8080

USER service_user

CMD ["bash", "-c", "source /var/lib/secrets/export.env; exec java -Xmx1024m -server -Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false -jar /vignette.jar -m s3 -p 8080 -C" ]
