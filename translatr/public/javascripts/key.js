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
    $('#field-locale').val(message.locale.name).attr('localeId', message.locale.id);
    $('#field-value').val(message.value);
    $('#preview').html(message.value);
}
function handleSaveMessage(message) {
	$('#' + message.locale.id).removeClass('no-message');
	$('#' + message.locale.id + ' .value').html(message.value);
	updateForm(message.locale.id);
}
function updateForm(localeId) {
	$("#no-selection").hide();
	$("#panel-message").show();
    $('#field-id').val('');
	$('#field-locale').val(keyName).attr('localeId', localeId);
    $('#field-value').val('');
    $('#preview').html('');
    $('#locale-name').html($('#' + localeId + ' .name').html());
	$.ajax(
		jsRoutes.controllers.Api.getMessage(localeId, keyName)
	).done(handleMessage);
}
function search(value) {
	var filterUntranslated = $('#field-untranslated').is(':checked');
    if(value !== '') {
		$('a.locale').hide();
		if(filterUntranslated) {
			console.log('Untranslated checked');
			$('a.locale.no-message[name*="' + value.toLowerCase() +'"]').show();
		} else {
			console.log('Untranslated unchecked');
			$('a.locale[name*="' + value.toLowerCase() +'"]').show();
		}
    } else {
		if(filterUntranslated) {
			console.log('Untranslated checked');
			$('a.locale').hide();
	    	$('a.locale.no-message').show();
		} else {
			console.log('Untranslated unchecked');
	    	$('a.locale').show();
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
		).done(function(data) {
			handleSaveMessage(data);
		});
    });
	$('a.locale').click(function() {
		$('a.locale').removeClass('active');
		$(this).addClass('active')
		updateForm($(this).attr('id'));
	})
	$('#field-value').on('change keyup paste', function() {
	    $('#preview').html($('#field-value').val());
	});
	$('.btn-cancel').click(function() {
		$('a.locale').removeClass('active');
		$("#panel-message").hide();
		$("#no-selection").show();
		window.location.hash = '#';
	});

	var hash = window.location.hash;
	if(hash !== '') {
		console.log('Hash: ', hash);
		var localeName = hash.replace('#locale=', '');
		var $a = $('a.locale[name="'+localeName+'"]');
		//$a[0].scrollIntoView();
		$a.click();
	} else {
		var $locale = $('a.locale:first-child');
		window.location.hash = $locale.attr('href').replace('.*#', '#');
		$locale.click();
	}
});
