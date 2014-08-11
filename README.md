# vignette

A Clojure library for thumbnail generation and storage.

# Proposed Plan of Action

Here is a high level overview of where I think we want to take this in no particular order:

* Create an interface for storage backend CRUD vignette.storage.core 
  * WIP Create concrete implementations for local disk and CEPH/S3.
* Create an interface for thumbnailing that accepts the below parameters. It might be a good idea to
  continue to use the shell script though that can’t be easily tested.
  * source
  * destination
  * height
  * width
  * x,y
  * crop mode
  * output type (e.g. webp)
* Create an interface for destructuring URL parameters and create concrete implementations for
  * WIP Wikia thumbnailer request patters. See the [Thumbnailer.pm](https://github.com/Wikia/backend/blob/master/lib/Wikia/Thumbnailer.pm#L171)
    * We should create a large (1e6) sample of request URLs and make sure we parse them in the same way
  * Huddler thumbnailer request patters-- this one will fundamentally change because we’ll need to provide
    the file in thu URL.
  * WIP 2.0 request API- We should put some thought into how to make this simple yet extensible. Do we use query parameters?

# TODO

 * Map the bin/thumbnail flags to more sensible mode strings (e.g. ZC -> zoomcrop)
 * Integration tests that exercise the routes.
 * S3 storage interface.
 * Functional testing page with grid of images and thumbnail options.
    * Include animated gifs http://hanky.huddler.com/t/5013/testing-animated-gifs
 * Add ‘fill’ option support for the ‘far’ mode. Should this be a query parameter?
 * Packaging and deployment pipeline
    * How do we unsure that what is being shipped to production has passed both unit and integration tests?
       * How do we track test coverage? Do we care about coverage? 
    * What do we ship as a package to production? A jar? A docker instance?
       * We need to resolve the imagemagick dependency
 * Monitoring in production
    * Latency, rates, image sizes
    * How do we alert?
    * What do we alert on?

## Testing

All testing is done using [Midje](https://github.com/marick/Midje). Running `lein midje` will run all of the tests.

## Usage

FIXME

## Environment Variables

Below is a list of environment variables that will affect the vignette runtime.

 * `LOGGER_APPLICATION`           this is primarily for wikia-commons. Set this to “vignette”.
 * `STORAGE_ACCESS_KEY`           S3 access key
 * `STORAGE_SECRET_KEY`           S3 secret key
 * `STORAGE_ENDPOINT`             S3 HTTP endpoint
 * `VIGNETTE_TEMP_FILE_LOCATION`  temporary file location. This is used for thumbnail generation. [/tmp/vignette]
 * `VIGNETTE_THUMBNAIL_BIN`       path to the thumbnail script [/usr/local/bin/thumbnail, bin/thumbnail]
 * `VIGNETTE_INTEGRATION_ROOT`    path to use for integration testing files [/tmp/integration]

## License

Copyright © 2014 Wikia

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
