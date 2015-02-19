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

 * `LOGGER_APPLICATION`           this is primarily for wikia-commons. Set this to “vignette”.
 * `LOGGER_TYPE`                  where to log. [file, syslog]
 * `LOGGER_SYSLOG_HOST`           Syslog host:port to log to when LOGGER_TYPE=syslog. [127.0.0.1]
 * `LOGGER_FILE_OUTPUT`           Which file to log to when LOGGER_TYPE=file. [logs/wikia-logger.log]
 * `STORAGE_ACCESS_KEY`           S3 access key
 * `STORAGE_SECRET_KEY`           S3 secret key
 * `STORAGE_ENDPOINT`             S3 HTTP endpoint
 * `STORAGE_PROXY`                S3 Proxy
 * `STORAGE_MAX_RETRIES`          S3 max error retry count; defaults to 0
 * `STORAGE_PROXY_PORT`           S3 Proxy port
 * `VIGNETTE_TEMP_FILE_LOCATION`  temporary file location. This is used for thumbnail generation. [/tmp/vignette]
 * `VIGNETTE_THUMBNAIL_BIN`       path to the thumbnail script [/usr/local/bin/thumbnail, bin/thumbnail]
 * `VIGNETTE_INTEGRATION_ROOT`    path to use for integration testing files [/tmp/integration]
 * `VIGNETTE_SERVER_MIN_THREADS`  minimum number of threads to allocate for jetty [50]
 * `VIGNETTE_SERVER_MAX_THREADS`  minimum number of threads to allocate for jetty [150]
 * `VIGNETTE_SERVER_QUEUE_SIZE`   queue size to allocate for jetty [9000]
 * `ENABLE_ACCESS_LOG`            enable the NCSA access log [false]
 * `ACCESS_LOG_FILE`              NCSA acces log file [/tmp/Vignette-access.log]
 * `IMAGEMAGICK_BASE`             path to the root of the ImageMagick installation [/usr/local]
 * `GETOPT`                       when running on osx, install gnu-getopt using brew. see bin/thumbnail
 * `CONVERT_CONSTRAINTS`          universal options to pass to ImageMagick. see bin/thumbnail
 * `UNSUPPORTED_REDIRECT_HOST`    on an unsupported legacy thumbnail request, host to redirect

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

# License

Copyright © 2014 Wikia

Distributed under the Eclipse Public License either version 1.0 or (at your option) any later version.

# Contributors

 * [Damon Snyder](https://github.com/drsnyder)
 * [Nelson Monterroso](https://github.com/nmonterroso)
