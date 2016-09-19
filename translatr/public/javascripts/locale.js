/*
 * Input:
 * 
 * * projectId: Project.id
 * * localeId: Locale.id
 * * locales: List of Locale
 */

App.Modules.KeyCreateModule = function(sb) {
	var form = sb.dom.find('#form-key');
	var keyName = sb.dom.find('#field-key-name');
	var saveButton = sb.dom.find('.modal .btn-save');

	return {
		create : function() {
			saveButton.click(function() {
				form.attr('action', form.attr('action') + '#key=' + keyName.val());
				form.submit();
			});
		},
		destroy : function() {
		}
	};
};

App.Modules.KeysListModule = function(sb) {
	function _handleKeysChanged() {
		var $keys = sb.dom.find('a.key');
		$keys.click(function() {
			var $this = sb.dom.wrap(this);
			$keys.parent().removeClass('active');
			$this.parent().addClass('active');
			sb.publish('keySelected', [$this.attr('id'), $this.attr('keyName')]);
		})
		sb.dom.find('a.key .btn-remove').click(function(e) {
			e.stopPropagation();
			window.location.href = jsRoutes.controllers.Application.keyRemove($(this).parent().attr('id'), localeId).url;
		});
	}
	return {
		create: function() {
			sb.subscribe('keysChanged', _handleKeysChanged);

			_handleKeysChanged();
		},
		destroy: function() {
		}
	};
};

App.Modules.MessageModule = function(sb) {
	var form = sb.dom.find('#form-message');
	var progress = form.find('.progress');
	var fieldId = sb.dom.find('#field-id');
	var fieldKey = sb.dom.find('#field-key');
	var fieldValue = sb.dom.find('#field-value');
    var messages = sb.dom.find('#panel-messages');
    var template = sb.dom.find('.message.template');
    var cancelButton = sb.dom.find('.btn-cancel');

	function _handleSaveMessage(message) {
		progress.css('visibility', 'hidden');
		sb.dom.find('#' + message.keyId).removeClass('no-message');
		sb.dom.find('#' + message.keyId + ' .value').html(message.value);
		console.log('fieldKey.val():', fieldKey.val());
		sb.publish('keySelected', [message.keyId, fieldKey.val()]);
		Materialize.toast(messages['message.updated'], 5000);
	}

	function _updateForm(keyId, keyName) {
		console.log('_updateForm(keyId:', keyId, ', keyName:', keyName);
		sb.dom.find("#no-selection").hide();
		sb.dom.find("#panel-message").show();
		messages.hide();
		sb.dom.find("#panel-preview").show();
	    fieldId.val('');
	    console.log('fieldKey.val(', keyName, ')');
		fieldKey.val(keyName).attr('keyId', keyId);
	    fieldValue.val('');
	    Materialize.updateTextFields();
	    sb.dom.find('#preview').html('');
	    sb.utilities.ajax(
			jsRoutes.controllers.Api.getMessage(localeId, keyName)
		).done(_handleMessage);
	    sb.utilities.ajax(sb.utilities.merge(
			jsRoutes.controllers.Api.findMessages(projectId),
			{data: {keyName: keyName}}
		)).done(function(data) {
		    _handleMessageList(keyName, data);
		});
	}

	function _handleMessage(message) {
	    fieldId.val(message.id);
	    fieldKey.val(message.keyName).attr('keyId', message.keyId);
	    fieldValue.val(message.value).trigger('autoresize');
	    Materialize.updateTextFields();
	    sb.dom.find('#preview').html(message.value);
	}

	function _handleMessageList(keyName, messageList) {
	    messages.find('.message:not(.template)').remove();
	    messageList.forEach(function(entry) {
	    	var $msg = template.clone().removeClass('template');
	    	var $a = $msg.find('a');
	    	$a.attr('title', $a.attr('title') + ' (' + locales[entry.localeId] + ')')
	    		.attr('href', window.location.hash)
	    		.click(_handleCopyMessageValue);
	    	$msg.find('.localeName').text(locales[entry.localeId]);
	    	$msg.find('.value').html(entry.value);
	    	messages.append($msg);
	    });
		messages.show();
	}

	function _handleCopyMessageValue() {
		var value = sb.dom.wrap(this).find('.value').html();
		fieldValue.val(value).trigger('autoresize');
		Materialize.updateTextFields();
		sb.dom.find('#preview').html(value);
	}

	return {
		create: function() {
			sb.subscribe('keySelected', _updateForm);

			sb.dom.find("#no-selection").hide();
			sb.dom.find("#panel-message").hide();
			messages.hide();

			fieldValue.on('change keyup paste', function() {
				sb.dom.find('#preview').html(fieldValue.val());
			});

			form.submit(function(e){
		        e.preventDefault();
		        progress.css('visibility', 'visible');
		    	sb.utilities.ajax(
		    		sb.utilities.merge(
						jsRoutes.controllers.Api.putMessage(),
						{
							contentType: 'application/json',
							dataType: 'json',
							data: JSON.stringify({
								"id": fieldId.val() !== '' ? fieldId.val() : null,
								"locale": {
								  "id": localeId
								},
								"key": {
									"id": fieldKey.attr('keyId')
								},
								"value": fieldValue.val()
							})
						}
					)
				).done(_handleSaveMessage);
		    });

			cancelButton.click(function() {
				sb.dom.find('a.key').removeClass('active');
				sb.dom.find("#panel-message").hide();
				messages.hide();
				sb.dom.find("#panel-preview").hide();
				sb.dom.find("#no-selection").show();
				window.location.hash = '#';
			});
		},
		destroy: function() {
		}
	};
};

App.Modules.SuggestionModule = function(sb) {
	var form = sb.dom.find('#form-search');

	function _handleSearch(value) {
		$.ajax({
			url: jsRoutes.controllers.Application.localeKeysSearch(localeId).url,
			data: {
				'search': value,
				'missing': fieldMissing.is(':checked') ? 'on' : 'off'
			}
		}).done(function(data) {
			keysContainer.html(data);
//			keysContainer.parent()[0].scrollIntoView();
			sb.publish('keysChanged');
		});
	}

	function _handleSuggestionSelected(suggestion) {
		if(suggestion.data.type == 'key') {
			form.submit();
		} else {
			window.location.href = suggestion.data.url;
		}
	}

	return {
		create: function() {
			sb.subscribe('suggestionSelected', _handleSuggestionSelected);
		},
		destroy: function() {
		}
	};
};

App.Core.register('SuggestionModule', App.Modules.SuggestionModule);
App.Core.register('ProjectSearchModule', App.Modules.ProjectSearchModule);
App.Core.register('KeyCreateModule', App.Modules.KeyCreateModule);
App.Core.register('KeysListModule', App.Modules.KeysListModule);
App.Core.register('MessageModule', App.Modules.MessageModule);

function scrollTopOffset(container, item) {
	return - container.position().top - container.css('margin-top').replace('px', '') + item.parent().offset() ? item.parent().offset().top : 0
}

$(document).ready(function() {
	var params = $.deparam.fragment();
	if('key' in params) {
		var $a = $('a.key[keyName="'+params.key+'"]');
		$('.collection.keys').animate(
			{
				scrollTop: scrollTopOffset($('.collection.keys'), $a)
			},
			'500'
		);
		$a.click();
	} else {
		var $key = $('.keys .collection-item:first-child a.key');
		params.key = $key.attr('keyName');
		$key.click();
	}
});
