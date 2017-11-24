[![Build Status](https://travis-ci.org/Wikia/vignette.svg?branch=master)](https://travis-ci.org/Wikia/vignette)

# Vignette

A Clojure library for thumbnail generation and storage.

# Table of Contents

- [Development](#development)
    - [Environment Variables](#environment-variables)
    - [Testing](#testing)
    - [Entry point](#entry-point)
    - [Thumbnail Modes](#thumbnail-modes)
        - [Thumbnailing Modes](#thumbnailing-modes)
            - [fixed-aspect-ratio](#fixed-aspect-ratio)
            - [fixed-aspect-ratio-down](#fixed-aspect-ratio-down)
            - [scale-to-width](#scale-to-width)
            - [thumbnail](#thumbnail)
            - [thumbnail-down](#thumbnail-down)
            - [top-crop](#top-crop)
            - [top-crop-down](#top-crop-down)
            - [window-crop](#window-crop)
            - [window-crop-fixed](#window-crop-fixed)
            - [zoom-crop](#zoom-crop)
            - [zoom-crop-down](#zoom-crop-down)
        - [Note Regarding Legacy Compatibility](#note-regarding-legacy-compatibility)
    - [Other HTTP Methods Supported](#other-http-methods-supported)
        - [HEAD](#head)
- [License](#license)
- [Contributors](#contributors)

# Development
To run in development:

1. Install [leiningen](http://leiningen.org/)
2. Install [ImageMagick](http://imagemagick.org/)
	* on OSX, with [homebrew](http://brew.sh/): brew install imagemagick --with-webp
3. Set environment variables (see below)
4. From the command line, run ```lein repl```
5. In the REPL, run ```(start system-s3 8080)``` to run using your defined s3 backend,
or ```(start system-local 8080)``` to run using your defined local backend

Vignette should now be running on localhost:8080

## Environment Variables

Below is a list of environment variables that will affect the vignette runtime.

 * `RELOAD_ON_REQUEST`            development option that enables reloading code on each request [false]
 * `LOGGER_APPLICATION`           this is primarily for wikia-commons. Set this to “vignette”.
 * `LOGGER_TYPE`                  where to log. [file, syslog]
 * `LOGGER_SYSLOG_HOST`           Syslog host:port to log to when LOGGER_TYPE=syslog. [127.0.0.1]
 * `LOGGER_FILE_OUTPUT`           Which file to log to when LOGGER_TYPE=file. [logs/wikia-logger.log]
 * `STORAGE_ACCESS_KEY`           S3 access key
 * `STORAGE_SECRET_KEY`           S3 secret key
 * `STORAGE_ENDPOINT`             S3 HTTP endpoint
 * `STORAGE_PROXY`                S3 Proxy
 * `STORAGE_MAX_CONNS`            S3 max simultaneous connections; defaults to 150
 * `STORAGE_MAX_RETRIES`          S3 max error retry count; defaults to 0
 * `STORAGE_PROXY_PORT`           S3 Proxy port
 * `STORAGE_CONNECTION_TIMEOUT`   S3 connection timeout [500]
 * `STORAGE_GET_SOCKET_TIMEOUT`   S3 GET socket timeout [5000]
 * `STORAGE_PUT_SOCKET_TIMEOUT`   S3 PUT socket timeout [10000]
 * `VIGNETTE_TEMP_FILE_LOCATION`  temporary file location. This is used for thumbnail generation. [/tmp/vignette]
 * `VIGNETTE_THUMBNAIL_BIN`       path to the thumbnail script [/usr/local/bin/thumbnail, bin/thumbnail]
 * `VIGNETTE_INTEGRATION_ROOT`    path to use for integration testing files [/tmp/integration]
 * `VIGNETTE_SERVER_MAX_THREADS`  minimum number of threads to allocate for jetty [150]
 * `VIGNETTE_SERVER_QUEUE_SIZE`   queue size to allocate for jetty [9000]
 * `ENABLE_ACCESS_LOG`            enable the NCSA access log [false]
 * `ACCESS_LOG_FILE`              NCSA acces log file [/tmp/Vignette-access.log]
 * `IMAGEMAGICK_BASE`             path to the root of the ImageMagick installation [/usr/local]
 * `GIF2WEBP`                     path to the gif2webp binary from libwebp package [/usr/bin/gif2webp]
 * `WIKIA_ENVIRONMENT`            Environment value (dev, prod, staging) [dev]
 * `WIKIA_DATACENTER`             Server datacenter location (poz, sjc, res) [poz]
 * `GETOPT`                       when running on osx, install gnu-getopt using brew. see bin/thumbnail
 * `CONVERT_CONSTRAINTS`          universal options to pass to ImageMagick. see bin/thumbnail
 * `UNSUPPORTED_REDIRECT_HOST`    on an unsupported legacy thumbnail request, host to redirect

## Command Line Options

To see the command line options available execute `lein run -- -h`. The notable
command line options are the following:

 * -C,--cache-thumbnails: enable thumbnail caching. When this option is provided
   thumbnails will be written to and read from the backing storage provided. The
   default is false.
 * -m,mode <MODE>:        the storage mode. Options are s3 or local. See above
   for the environment variables toggling these settings. The default is s3.
 * -p,port <PORT>:        the port the HTTP server will listen on. The default
   is 8080.

## Testing

All testing is done using [Midje](https://github.com/marick/Midje). Running `lein midje` will run all of the tests.

## Entry point

The main entry point for Vignette happens in src/vignette/http/routes.clj

## Thumbnail Modes

For testing an experimentation, vignette includes a facility for setting up integration or browser testing. To set this up,
do the following:

```sh
$ lein repl

; creates the integration environment which just copies some images to /tmp
user=> (i/create-integration-env)

; starts the server using the local integration environment
user=> (start system-local 8080)
```

The links provided below were generated using the above.

### Thumbnailing Modes

The following examples were rendered from [beach.jpg](/image-samples/beach.jpg)
and [carousel.jpg](/image-samples/carousel.jpg).

#### fixed-aspect-ratio

Returns an image that is exactly width x height pixels with the source image
centered either vertically or horizontally, depending on the longer dimension.

| beach.jpg                                                         | carousel.jpg |
| :--------:                                                        | :-----------: |
| ![beach fixed-aspect-ratio](/assets/fixed-aspect-ratio/beach.jpg) | ![carousel fixed-aspect-ratio](/assets/fixed-aspect-ratio/carousel.jpg) |

Example: [http://localhost:8080/bucket/a/ab/beach.jpg/revision/latest/fixed-aspect-ratio/width/200/height/200?fill=blue](http://localhost:8080/bucket/a/ab/beach.jpg/revision/latest/fixed-aspect-ratio/width/200/height/200?fill=blue)


Note that the `fill=blue` URL parameter isn’t required. It’s there to help
illustrate the cropping behavior.

#### fixed-aspect-ratio-down

This behaves the same as above except that it will not upscale the image. This
is convenient when you want to preserve the aspect ratio but you don’t want the
side effects that can result from upscaling the image.

Example: [http://localhost:8080/bucket/a/ab/beach.jpg/revision/latest/fixed-aspect-ratio-down/width/200/height/200?fill=blue](http://localhost:8080/bucket/a/ab/beach.jpg/revision/latest/fixed-aspect-ratio-down/width/200/height/200?fill=blue)

#### scale-to-width

Scales an image to the specified width. The height is adjusted to maintain the
aspect ratio.

| beach.jpg                                                         | carousel.jpg |
| :--------:                                                        | :-----------: |
| ![beach scale-to-width](/assets/scale-to-width/beach.jpg) | ![carousel scale-to-width](/assets/scale-to-width/carousel.jpg) |

Example: [http://localhost:8080/bucket/a/ab/beach.jpg/revision/latest/scale-to-width/200](http://localhost:8080/bucket/a/ab/beach.jpg/revision/latest/scale-to-width/200)

### scale-to-width-down

Same as above but without upscaling.

Example: [http://localhost:8080/bucket/a/ab/beach.jpg/revision/latest/scale-to-width-down/1000](http://localhost:8080/bucket/a/ab/beach.jpg/revision/latest/scale-to-width-down/1000)

### scale-to-height-down

Scales an image to the specified height. The width is adjusted to maintain the
aspect ratio.

Example: [http://localhost:8080/bucket/a/ab/beach.jpg/revision/latest/scale-to-height-down/1000](http://localhost:8080/bucket/a/ab/beach.jpg/revision/latest/scale-to-height-down/1000)

#### thumbnail

Returns a thumbnail that is at most width pixels wide and height pixels high. The
aspect ratio will be preserved. Image upscaling is permitted.

| beach.jpg                                                         | carousel.jpg |
| :--------:                                                        | :-----------: |
| ![beach thumbnail](/assets/thumbnail/beach.jpg) | ![carousel thumbnail](/assets/thumbnail/carousel.jpg) |

Example: [http://localhost:8080/bucket/a/ab/beach.jpg/revision/latest/thumbnail/width/200/height/200](http://localhost:8080/bucket/a/ab/beach.jpg/revision/latest/thumbnail/width/200/height/200)

#### thumbnail-down

This behaves the same as the above except that it will not upscale the image.

Example: [http://localhost:8080/bucket/a/ab/beach.jpg/revision/latest/thumbnail-down/width/200/height/200](http://localhost:8080/bucket/a/ab/beach.jpg/revision/latest/thumbnail-down/width/200/height/200)

#### top-crop

This mode is similar to `zoom-crop` below except the top of the image is cropped
instead of the center. The output will be the dimensions specified. This mode
(and the other `*-crop*` modes) can be useful when you have a precise placement
you want to fill regardless of the image size or shape.

| beach.jpg                                                         | carousel.jpg |
| :--------:                                                        | :-----------: |
| ![beach top-crop](/assets/top-crop/beach.jpg) | ![carousel top-crop](/assets/top-crop/carousel.jpg) |

Example: [http://localhost:8080/bucket/a/ab/beach.jpg/revision/latest/top-crop/width/200/height/200](http://localhost:8080/bucket/a/ab/beach.jpg/revision/latest/top-crop/width/200/height/200)

#### top-crop-down

Same as the above except that it will not upscale the image.

Example: [http://localhost:8080/bucket/a/ab/beach.jpg/revision/latest/top-crop-down/width/200/height/200](http://localhost:8080/bucket/a/ab/beach.jpg/revision/latest/top-crop-down/width/200/height/200)

#### window-crop

Creates a window within the image and then thumbnails that to the specified
dimensions.

| beach.jpg                                                         | carousel.jpg |
| :--------:                                                        | :-----------: |
| ![beach window-crop](/assets/window-crop/beach.jpg) | ![carousel window-crop](/assets/window-crop/carousel.jpg) |

Examples:
 * [http://localhost:8080/bucket/a/ab/beach.jpg/revision/latest/window-crop/width/200/x-offset/60/y-offset/550/window-width/200/window-height/260](http://localhost:8080/bucket/a/ab/beach.jpg/revision/latest/window-crop/width/200/x-offset/60/y-offset/550/window-width/200/window-height/260)
 * [http://localhost:8080/bucket/a/ab/carousel.jpg/revision/latest/window-crop/width/200/x-offset/690/y-offset/250/window-width/1600/window-height/1900](http://localhost:8080/bucket/a/ab/carousel.jpg/revision/latest/window-crop/width/200/x-offset/690/y-offset/250/window-width/1600/window-height/1900)

#### window-crop-fixed

Same as window-crop, except a height is specified so the canvas will be exactly
the specified width and height with the image centered similarly to fixed-aspect-ratio.

| beach.jpg                                                         | carousel.jpg |
| :--------:                                                        | :-----------: |
| ![beach window-crop-fixed](/assets/window-crop-fixed/beach.jpg) | ![carousel window-crop-fixed](/assets/window-crop-fixed/carousel.jpg) |

Examples:
 * [http://localhost:8080/bucket/a/ab/beach.jpg/revision/latest/window-crop-fixed/width/200/height/200/x-offset/60/y-offset/550/window-width/200/window-height/260?fill=blue](http://localhost:8080/bucket/a/ab/beach.jpg/revision/latest/window-crop-fixed/width/200/height/200/x-offset/60/y-offset/550/window-width/200/window-height/260?fill=blue)
 * [http://localhost:8080/bucket/a/ab/carousel.jpg/revision/latest/window-crop-fixed/width/200/height/200/x-offset/690/y-offset/250/window-width/1600/window-height/1900?fill=black](http://localhost:8080/bucket/a/ab/carousel.jpg/revision/latest/window-crop-fixed/width/200/height/200/x-offset/690/y-offset/250/window-width/1600/window-height/1900?fill=black)

Note that the `fill=black` URL parameter isn’t required. It’s there to help
illustrate the cropping behavior.

#### zoom-crop

Zooms into the center of an image and then crops. The image rendered is to the
dimensions specified.

| beach.jpg                                                         | carousel.jpg |
| :--------:                                                        | :-----------: |
| ![beach zoom-crop](/assets/zoom-crop/beach.jpg) | ![carousel zoom-crop](/assets/zoom-crop/carousel.jpg) |

Example: [http://localhost:8080/bucket/a/ab/beach.jpg/revision/latest/zoom-crop/width/200/height/200](http://localhost:8080/bucket/a/ab/beach.jpg/revision/latest/zoom-crop/width/200/height/200)

#### zoom-crop-down

Same as the above except that the image will not be upscaled-- the original
height and width need to be larger than those specified.

### Note Regarding Legacy Compatibility

Vignette also supports legacy Wikia thumbnail request URIs. This request format
is no longer supported and is currently provided for transitional support. The
legacy support is handled under the `*.legacy.*` namespaces.

It should also be noted that there may be subtle differences between the
thumbnails generated via the legacy and vignette URIs. For example, Legacy URLs
and Vignette URLs calculate window width/height differently because the
parameters to the image mean different things. In the legacy format `a,b,c,d`, `a`
is `x-offset`, `b` is `x-endpoint` (which is not specified at all in Vignette URLs),
and `c,d` are the same but for `y`. In Vignette URLs, we specify the `x-offset`,
which is the same as a above, but we specify window-width instead of `x-endpoint`
(where `window-width` is `x-endpoint - x-offset`).

### Media Passthrough for not certain mime-types

When encountered media types:
 - audio/ogg
 - video/ogg
data will be passed through as - is without applying any thumbnailing operations.

## Other HTTP Methods Supported

### HEAD

Limited support for `HEAD` is provided. Given that any request for an
object will need to go to storage `HEAD` requests currently only check for
object existence. As a result, `HEAD` requests will not include the
`Content-Type` in the response.

# License

Copyright © 2014 Wikia

Distributed under the Eclipse Public License either version 1.0 or (at your option) any later version.

# Contributors

 * [Damon Snyder](https://github.com/drsnyder)
 * [Maciej Brencz](https://github.com/macbre)
 * [Michał Roszka](https://github.com/michalroszka)
 * [Nelson Monterroso](https://github.com/nmonterroso)
 * [Pawel Chojnacki](https://github.com/pchojnacki)
 * [Paweł Wójcik](https://github.com/pwojcik86)
