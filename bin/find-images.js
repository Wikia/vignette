/**
 * Uses phantomjs (http://phantomjs.org/) to find images whose src attribute matches a provided regular expression. The
 * output will be line-separated URLs
 *
 * usage: phantomjs find-images.js url_regex [url1 [url2 [...]]]
 * example: phantomjs find-images.js "^http(s)?:\/\/(img\d|vignette\d)\.wikia\.nocookie\.net\/" http://some-url.com/page
 */

var web = require('webpage');
var args = require('system').args;

if (args.length < 3) {
	console.error('Usage: phantomjs', args[0], 'url_regex [url1 [url2 [...]]]');
	phantom.exit(1);
}

var url_regex = new RegExp(args[1]);
var page_urls = args.slice(2);
var completed_pages = 0;

page_urls.forEach(function(page_url) {
	var page = web.create();

	page.onError = function(msg, trace) {
		console.error(page_url, '-', msg);
		phantom.exit(1);
	};

	page.open(page_url, function(s) {
		var all_images = page.evaluate(function() {
			var images = [];
			var nodes = document.querySelectorAll('img');

			for (var i = 0; i < nodes.length; ++i) {
				images.push(nodes[i].src);
			}

			return images;
		});

		all_images.forEach(function(img_src) {
			if (img_src.match(url_regex) != null) {
				console.log(img_src);
			}
		});

		if (++completed_pages == page_urls.length) {
			phantom.exit();
		}
	});
});
