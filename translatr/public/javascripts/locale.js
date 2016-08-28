/*
 * Input:
 * 
 * * projectId: Project.id
 * * localeId: Locale.id
 * * locales: List of Locale
 */

function handleMessage(message) {
    $('#field-id').val(message.id);
    $('#field-key').val(message.keyName).attr('keyId', message.keyId);
    $('#field-value').val(message.value).trigger('autoresize');
    Materialize.updateTextFields();
    $('#preview').html(message.value);
}
function handleMessageList(keyName, messages) {
    var $messages = $('#panel-messages');
    $messages.find('.message:not(.template)').remove();
    var $template = $('.message.template');
    messages.forEach(function(entry) {
    	console.log('Message: ', entry);
    	var $msg = $template.clone().removeClass('template');
    	$msg.find('a').attr('href', window.location.hash).click(handleCopyMessageValue);
    	$msg.find('.localeName').text(locales[entry.localeId]);
    	$msg.find('.value').html(entry.value);
    	$messages.append($msg);
    });
	$("#panel-messages").show();
}
function handleCopyMessageValue() {
	var value = $(this).find('.value').html();
	$('#field-value').val(value).trigger('autoresize');
	Materialize.updateTextFields();
	$('#preview').html(value);
}
function handleSaveMessage(message) {
	console.log('Message: ', message);
	$('#form-message .progress').css('visibility', 'hidden');
	$('#' + message.keyId).removeClass('no-message');
	$('#' + message.keyId + ' .value').html(message.value);
	updateForm(message.keyId, $('#field-key').val());
	Materialize.toast(messages['message.updated'], 5000);
}
function updateForm(keyId, keyName) {
	$("#no-selection").hide();
	$("#panel-message").show();
	$("#panel-messages").hide();
	$("#panel-preview").show();
    $('#field-id').val('');
	$('#field-key').val(keyName).attr('keyId', keyId);
    $('#field-value').val('');
    Materialize.updateTextFields();
    $('#preview').html('');
	$.ajax(
		jsRoutes.controllers.Api.getMessage(localeId, keyName)
	).done(handleMessage);
	$.ajax($.extend(
		jsRoutes.controllers.Api.findMessages(projectId),
		{data: {keyName: keyName}}
	)).done(function(data) {
	    handleMessageList(keyName, data);
	});
}
function search(value) {
	var filterUntranslated = $('#field-untranslated').is(':checked');
    if(value !== '') {
		$('a.key').parent().hide();
		if(filterUntranslated) {
			console.log('Untranslated checked');
			$('a.key.no-message .name:contains("' + value + '"), a.key.no-message .value:contains("' + value + '")')
				.parent().parent().show();
		} else {
			console.log('Untranslated unchecked');
			$('a.key .name:contains("' + value + '"), a.key .value:contains("' + value + '")')
				.parent().parent().show();
		}
    } else {
		if(filterUntranslated) {
			console.log('Untranslated checked');
			$('a.key').parent().hide();
	    	$('a.key.no-message').parent().show();
		} else {
			console.log('Untranslated unchecked');
	    	$('a.key').parent().show();
		}
    }
}
$(document).ready(function() {
	$("#no-selection").hide();
	$("#panel-message").hide();
	$("#panel-messages").hide();
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
						  "id": localeId
						},
						"key": {
							"id": $('#field-key').attr('keyId')
						},
						"value": $('#field-value').val()
					})
				}
			)
		).done(handleSaveMessage);
    });
	$('a.key').click(function() {
		$('a.key').parent().removeClass('active');
		$(this).parent().addClass('active')
		updateForm($(this).attr('id'), $(this).attr('name'));
	})
	$('a.key .btn-remove').click(function(e) {
		e.stopPropagation();
		window.location.href=jsRoutes.controllers.Application.keyRemove($(this).parent().attr('id'), localeId).url;
	});
	$('#field-value').on('change keyup paste', function() {
	    $('#preview').html($('#field-value').val());
	});
	$('.btn-cancel').click(function() {
		$('a.key').removeClass('active');
		$("#panel-message").hide();
		$("#panel-messages").hide();
		$("#panel-preview").hide();
		$("#no-selection").show();
		window.location.hash = '#';
	});
	$('.modal .btn-save').click(function() {
		$('#form-key').attr('action', $('#form-key').attr('action') + '#key=' + $('#field-key-name').val());
		$('#form-key').submit();
	});

	var hash = window.location.hash;
	if(hash !== '') {
		console.log('Hash: ', hash);
		var keyName = hash.replace('#key=', '');
		var $a = $('a.key[name="'+keyName+'"]');
		$a.parent()[0].scrollIntoView();
		$a.click();
	} else {
		var $key = $('.keys .collection-item:first-child a.key');
		window.location.hash = $key.attr('href').replace('.*#', '#');
		$key.click();
	}
});
