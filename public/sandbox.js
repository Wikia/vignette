$(document).ready(function() {
	var add_history = function(url) {
		$('#history').prepend(""+
			"<div class='history_image'>"+
				"<img src='"+url+"' /><br />"+
				"<span class='history_remove'>[x]</span>"+url+
			"</div>")
	};

	$('#history').on('click', '.history_remove', null, function() {
		$(this).closest('.history_image').remove();
	});

	$('#sandbox-form').submit(function() {
		var image = $('#original-image').val();
		if (image == 0) {
			return false;
		}

		var image_url = "/bucket/a/ab/"+image+"/revision/latest";
		var request_type = $('#request-type').val();
		if (request_type != 0) {
			image_url += '/'+request_type;
		}

		$('#form-dynamics').find(':input').each(function() {
			image_url += '/'+$(this).val();
		});

		var query = $('#query').val();
		if (query != "") {
			image_url += '?'+query;
		}

		add_history(image_url);
		return false;
	});

	$('#request-type').change(function() {
		var dynamics = $('#form-dynamics').html('');
		var request_type = $(this).val();
		var add_options = '';

		$('.form-dynamic').each(function() {
			if ($(this).data('dynamicFor').indexOf(request_type) != -1) {
				add_options += $(this).html();
			}
		});

		if (!add_options.length) {
			return;
		}

		dynamics.html(add_options);
	});
});