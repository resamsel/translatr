function search(value) {
    if(value !== '') {
		$('tr.key').hide();
		$('tr.key[name*="' + value.toLowerCase() +'"]').show();
    } else {
    	$('tr.key').show();
    }
}
$(document).ready(function() {
	$('#field-search').on('change keyup paste', function() {
		search($('#field-search').val());
	});

	var hash = window.location.hash;
	if(hash !== '') {
		console.log('Hash: ', hash);
		var s = hash.replace('#search=', '');
		$('#field-search').val(s);
		search(s);
	}
});
