function search(value) {
    if(value !== '') {
		$('.card.project').hide();
		$('.card.project[name*="' + value.toLowerCase() +'"]').show();
    } else {
    	$('.card.project').show();
    }
}
$(document).ready(function() {
	$('#field-search').on('change keyup paste', function() {
		search($('#field-search').val());
	});
	$('.button-save').click(function() {
		$('#form-project').submit();
	});
});
