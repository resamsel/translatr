function search(value) {
    if(value !== '') {
		$('tr.locale').hide();
		$('tr.locale[name*="' + value.toLowerCase() +'"]').show();
    } else {
    	$('tr.locale').show();
    }
}
$(document).ready(function() {
	$('select').material_select();
	$('#field-search').on('change keyup paste', function() {
		search($('#field-search').val());
	});
	$('.btn-save').click(function() {
		$('#form-locale').submit();
	});
});
