#!/usr/bin/env perl
use JSON;

sub create_matcher {
	my ($hook_name, $regex) = @_;
	return sub {
		my ($path) = shift(@_);
		if ($path =~ $regex) {
			$json = JSON->new;
			return $json->pretty->encode({
				hook_name => $hook_name,
				input     => $path,
				thumbpath => $1,
				dbname    => $2,
				type      => $3,
				archive   => $4,
				filename  => $5,
				fileext   => $6,
				thumbname => $7,
				width     => $8,
				thumbext  => $9
			});
		}

		return 0;
	};
}

# Thumbnailer.pm: 250
my @ordered_request_matchers = (
	create_matcher(
		"image thumbnailer",
		qr{ \/((?!\w\/)?(.+)\/(images|avatars)\/thumb((?!\/archive).*|\/archive)?\/\w\/\w{2}\/(.+)\.(jpg|jpeg|png|gif{1,}))\/((\d+px|\d+x\d+|\d+x\d+x\d+|)\-(.*)\.(jpg|jpeg|jpe|png|gif|webp))(\?.*)?$ }xi
	),
	create_matcher(
		"image without extension",
		qr{ \/((?!\w\/)?(.+)\/images((?!\/archive).*|\/archive)?\/\w\/\w{2}\/((.+)(\.[^.])?))$ }xi
	)
);

while ($path = <STDIN>) {
	chomp($path);
	$path =~ s@^http://.*?/@/@g;
	foreach my $path_test (@ordered_request_matchers) {
		$result = &$path_test($path);
		if ($result)  {
			print $result;
			last;
		}
	}
}

