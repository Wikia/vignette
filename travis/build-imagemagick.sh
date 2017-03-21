#!/usr/bin/env bash

export PATH=$HOME/opt/bin:$PATH
export LDFLAGS=-L$HOME/opt/lib
export LD_LIBRARY_PATH=/lib:/usr/lib:/usr/local/lib:$HOME/opt/lib
export CPPFLAGS=-I$HOME/opt/include
export CFLAGS=-I$HOME/opt/include
export CORES=$(nproc)

if [[ `convert --version | grep $IMAGEMAGICK_VERSION` ]]; then
    echo "Found correct version of ImageMagick"
    if [[ `gif2webp -h | grep gif2webp` ]]; then
      echo "gif2webp found"
      exit 0
    fi
fi


echo "Using $CORES cores for compiling..."

cd /tmp
curl -O https://storage.googleapis.com/downloads.webmproject.org/releases/webp/libwebp-$LIBWEBP_VERSION.tar.gz
tar xvzf libwebp-$LIBWEBP_VERSION.tar.gz
cd libwebp-$LIBWEBP_VERSION
./configure --prefix=$HOME/opt --enable-libwebpmux
make -j$CORES
make install -j$CORES
cd /tmp
curl -L -O https://github.com/ImageMagick/ImageMagick/archive/$IMAGEMAGICK_VERSION.tar.gz
tar xvzf $IMAGEMAGICK_VERSION.tar.gz
cd ImageMagick-$IMAGEMAGICK_VERSION
./configure --prefix=$HOME/opt --with-webp
make -j$CORES
make install -j$CORES
