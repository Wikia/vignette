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

 * The storage object get methods will probably return binary or String. We need to make sure this can be rendered
	as binary via the request.

## Usage

FIXME

## License

Copyright © 2014 FIXME

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
