/*
 * Input:
 * 
 * * projectId: Project.id
 * * keyId: Key.id
 * * keyName: Key.name
 * * locales: List of Locale
 */

function handleMessage(message) {
    $('#field-id').val(message.id);
    $('#field-locale').val(message.localeName).attr('localeId', message.localeId);
    $('#field-value').val(message.value).trigger('autoresize');
    Materialize.updateTextFields();
    $('#preview').html(message.value);
}
function handleSaveMessage(message) {
	$('#form-message .progress').css('visibility', 'hidden');
	$('#' + message.localeId).removeClass('no-message');
	$('#' + message.localeId + ' .value').html(message.value);
	updateForm(message.localeId);
	Materialize.toast(messages['message.updated'], 5000);
}
function updateForm(localeId) {
	$("#no-selection").hide();
	$("#panel-message").show();
	$("#panel-preview").show();
    $('#field-id').val('');
	$('#field-locale').val(keyName).attr('localeId', localeId);
    $('#field-value').val('');
    Materialize.updateTextFields();
    $('#preview').html('');
    $('#locale-name').html($('#' + localeId + ' .name').html());
	$.ajax(
		jsRoutes.controllers.Api.getMessage(localeId, keyName)
	).done(handleMessage);
}
function search(value) {
	var filterUntranslated = $('#field-untranslated').is(':checked');
    if(value !== '') {
		$('a.locale').parent().hide();
		if(filterUntranslated) {
			console.log('Untranslated checked');
			$('a.locale.no-message .name:contains("' + value + '"), a.locale.no-message .value:contains("' + value + '")')
				.parent().parent().show();
		} else {
			console.log('Untranslated unchecked');
			$('a.locale .name:contains("' + value + '"), a.locale .value:contains("' + value + '")')
				.parent().parent().show();
		}
    } else {
		if(filterUntranslated) {
			console.log('Untranslated checked');
			$('a.locale').parent().hide();
	    	$('a.locale.no-message').parent().show();
		} else {
			console.log('Untranslated unchecked');
	    	$('a.locale').parent().show();
		}
    }
}
$(document).ready(function() {
	$("#panel-message").hide();
	$('#field-search').on('change keyup paste', function() {
		search($('#field-search').val());
	});
	$('#field-untranslated').change(function() {
		search($('#field-search').val());
	});
	$('#form-search').submit(function(e) {
		console.log('Hide...');
        e.preventDefault();
        search($('#field-search').val());
	});
	$("#form-message").submit(function(e){
        e.preventDefault();
    	$('#form-message .progress').css('visibility', 'visible');
		$.ajax(
			$.extend(
				jsRoutes.controllers.Api.putMessage(),
				{
					contentType: 'application/json',
					dataType: 'json',
					data: JSON.stringify({
						"id": $('#field-id').val() !== '' ? $('#field-id').val() : null,
						"locale": {
						  "id": $('#field-locale').attr('localeId')
						},
						"key": {
							"id": keyId
						},
						"value": $('#field-value').val()
					})
				}
			)
		).done(handleSaveMessage);
    });
	$('a.locale').click(function() {
		$('a.locale').parent().removeClass('active');
		$(this).parent().addClass('active')
		updateForm($(this).attr('id'));
	})
	$('#field-value').on('change keyup paste', function() {
	    $('#preview').html($('#field-value').val());
	});
	$('.btn-cancel').click(function() {
		$('a.locale').parent().removeClass('active');
		$("#panel-message").hide();
		$("#panel-preview").hide();
		$("#no-selection").show();
		window.location.hash = '#';
	});
	$('.modal .btn-save').click(function() {
		$('#form-key').submit();
	});

	var hash = window.location.hash;
	if(hash !== '') {
		console.log('Hash: ', hash);
		var localeName = hash.replace('#locale=', '');
		var $a = $('a.locale[localeName="'+localeName+'"]');
		$a.click();
	} else {
		var $locale = $('.locales .collection-item:first-child a.locale');
		$locale.click();
	}
});
