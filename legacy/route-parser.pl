#!/usr/bin/env perl
# This is a simple prototype for mapping the Dancer request paths to the extracted
# JSON so that some form of regression testing can be done between the old and the new
# thumbnailer.
#
# Usage: ./legacy/route-parser.pl < request.log > parsed.json
# Create request.log with the following on an existing thumbnailer:
# varnishlog  -mRxURL:images.wikia.com -iRxURL | grep RxURL | cut -c22- | grep -v auth > request.log
#
use JSON;


sub create_matcher {
	my ($hook_name, $regex, @fields) = @_;

	unshift @fields, 'input';
	unshift @fields, 'hook_name';

	return sub {
		# Try and match the given $regex. If there is a hit, extract the matched values into
		# the fields in @fields that were supplied at call time.
		my ($path, $json) = @_;
		if ($path =~ $regex) {
			my %extracted_map;

			my @values = ($hook_name, $path, $1, $2, $3, $4, $5, $6, $7, $8, $9);

			@extracted_map{@fields} = @values;

			return $json->encode(\%extracted_map);
		}

		return 0;
	};
}

$json = JSON->new;

my @ordered_request_matchers = (
	create_matcher(
		# Thumbnailer.pm: 171
		"image thumbnailer",
		qr{ \/((?!\w\/)?(.+)\/(images|avatars)\/thumb((?!\/archive).*|\/archive)?\/\w\/\w{2}\/(.+)\.(jpg|jpeg|png|gif{1,}))\/((\d+px|\d+x\d+|\d+x\d+x\d+|)\-(.*)\.(jpg|jpeg|jpe|png|gif|webp))(\?.*)?$ }xi,
		('thumbpath', 'dbname', 'type', 'archive', 'filename', 'fileext', 'thumbname', 'width', 'thumbext')
	),
	create_matcher(
		# Thumbnailer.pm: 196
		"SVG thumbnailer",
		qr{ \/((?!\w\/)?(.+)\/images\/thumb((?!\/archive).*|\/archive)?\/\w\/\w{2}\/(.+)\.svg)\/((\d+px|\d+x\d+|\d+x\d+x\d+)\-(.+)\.(.*))$ }xi,
		('thumbpath', 'dbname', 'archive', 'filename', 'fileext', 'width', 'thumbname'),
		# hard codes 'fileext' => svg, 'thumbext' => 'png'
	),
	create_matcher(
		# Thumbnailer.pm: 218
		"OGG thumbnailer",
		qr{ \/((?!\w\/)?(.+)\/images\/thumb((?!\/archive).*|\/archive)?\/\w\/\w{2}\/(.+)\.ogg)\/((\d+px|seek=\d+|mid)\-(.+)\.(jpg))$ }xi,
		('thumbpath', 'dbname', 'type', 'filename', 'thumbname', 'width', 'junk', 'thumbext')
		# hard codes 'fileext' => ogg
		# custom width manipulation
	),
	create_matcher(
		# Thumbnailer.pm: 250
		"image without extension",
		qr{ \/((?!\w\/)?(.+)\/images\/thumb((?!\/archive).*|\/archive)?\/\w\/\w{2}\/(.+))\/((((v\,([0-9a-f]{6}\,)?)?\d+)px|\d+x\d+|\d+x\d+x\d+)\-(.+))$ }xi,
		('thumbpath', 'dbname', 'archive', 'filename', 'fileext', 'width', 'thumbname', 'thumbext')
		# hard codes thumb ext to ''
	),
	create_matcher(
		# Thumbnailer.pm: 276
		"original",
		qr{ \/((?!\w\/)?(.+)\/images((?!\/archive).*|\/archive)?\/\w\/\w{2}\/((.+)(\.[^.])?))$ }xi,
		('thumbpath', 'dbname', 'archive', 'filename', 'junk', 'fileext', 'width', 'thumbname', 'thumbext')
		# stops after fileext
		# hard codes width => '', thumbname => '', thumbext => '', 'original' => 1
	),
	create_matcher(
		# Thumbnailer.pm: 306
		"interactive maps",
		qr{ \/((intmap_(.+))\/thumb\/([^/]+)\.(jpg|jpeg|png|gif))\/((\d+px|\d+x\d+|\d+x\d+x\d+|)\-(.*)\.(jpg|jpeg|jpe|png|gif|webp)) }xi,
		('thumbpath', 'dbname', 'junk', 'filename', 'fileext', 'thumbname', 'width', 'junk', 'thumbext')
		# hard codes type => 'images', original => 0, archive => 0
	)
);

while ($path = <STDIN>) {
	chomp($path);
	$path =~ s@^http://.*?/@/@g;
	$result = null;
	foreach my $path_test (@ordered_request_matchers) {
		$result = $path_test->($path, $json);
		if ($result)  {
			print $result;
			print "\n";
			last;
		}
	}
	if (!$result) {
		print "404 $path\n";
	}
}

