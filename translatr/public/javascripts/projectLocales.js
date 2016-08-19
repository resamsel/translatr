function search(value) {
    if(value !== '') {
		$('tr.locale').hide();
		$('tr.locale[name*="' + value.toLowerCase() +'"]').show();
    } else {
    	$('tr.locale').show();
    }
}
$(document).ready(function() {
	$('#field-search').on('change keyup paste', function() {
		search($('#field-search').val());
	});
	$('.button-save').click(function() {
		$('#form-locale').submit();
	});
});
