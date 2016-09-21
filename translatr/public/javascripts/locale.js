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

App.Modules.KeyListModule = function(sb) {
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

App.Modules.MessageListModule = function(sb) {
    var messages = sb.dom.find('#panel-messages');
    var template = sb.dom.find('.message.template');
	var fieldValue = sb.dom.find('#field-value');
	var panelPreview = sb.dom.find('#panel-preview');
	var preview = sb.dom.find('#preview');

    function _handleKeyChanged(keyId, keyName) {
		messages.hide();
	    sb.utilities.ajax(sb.utilities.merge(
			jsRoutes.controllers.Api.findMessages(projectId),
			{data: {keyName: keyName}}
		)).done(function(data) {
		    _handleMessageList(keyName, data);
		});
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

	function _handleCopyMessageValue(e) {
		e.preventDefault();
		var value = sb.dom.wrap(this).find('.value').html();
		fieldValue.val(value).trigger('autoresize');
		Materialize.updateTextFields();
		sb.publish('valueChanged', value);
		preview.html(value);
	}

	return {
		create: function() {
			sb.subscribe('keySelected', _handleKeyChanged);

			messages.hide();
		},
		destroy: function() {
		}
	};
};

App.Modules.MessageModule = function(sb) {
	var form = sb.dom.find('#form-message');
	var message = sb.dom.find('#panel-message');
	var panelPreview = sb.dom.find('#panel-preview');
	var preview = sb.dom.find('#preview');
	var progress = form.find('.progress');
	var fieldId = sb.dom.find('#field-id');
	var fieldKey = sb.dom.find('#field-key');
	var fieldValue = sb.dom.find('#field-value');
    var cancelButton = sb.dom.find('.btn-cancel');
    var noSelection = sb.dom.find("#no-selection");

	function _handleSaveMessage(message) {
		progress.css('visibility', 'hidden');
		sb.dom.find('#' + message.keyId).removeClass('no-message');
		sb.dom.find('#' + message.keyId + ' .value').html(message.value);
		sb.publish('keySelected', [message.keyId, fieldKey.val()]);
		Materialize.toast(messages['message.updated'], 5000);
	}

	function _updateForm(keyId, keyName) {
		noSelection.hide();
		message.show();
		panelPreview.show();
	    fieldId.val('');
		fieldKey.val(keyName).attr('keyId', keyId);
	    fieldValue.val('');
	    Materialize.updateTextFields();
	    preview.html('');
	    sb.utilities.ajax(
			jsRoutes.controllers.Api.getMessage(localeId, keyName)
		).done(_handleMessage);
	}

	function _handleMessage(message) {
	    fieldId.val(message.id);
	    fieldKey.val(message.keyName).attr('keyId', message.keyId);
	    fieldValue.val(message.value).trigger('autoresize');
	    Materialize.updateTextFields();
	    preview.html(message.value);
	}

	return {
		create: function() {
			sb.subscribe('keySelected', _updateForm);

			noSelection.hide();
			message.hide();

			fieldValue.on('change keyup paste', function() {
				preview.html(fieldValue.val());
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
				message.hide();
				messages.hide();
				panelPreview.hide();
				noSelection.show();
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

App.Modules.LocaleHashModule = function(sb) {
	var params = $.deparam.fragment();
	var location = window.location;
	var doc = sb.dom.wrap(document);
	var keys = sb.dom.find('.collection.keys');

	function _initFromHash() {
		if('key' in params) {
			var $a = sb.dom.find('a.key[keyName="'+params.key+'"]');
			keys.animate(
				{ scrollTop: _scrollTopOffset(keys, $a) },
				'500'
			);
			$a.click();
		} else {
			var $key = sb.dom.find('.keys .collection-item:first-child a.key');
			params.key = $key.attr('keyName');
			$key.click();
		}
	}
	
	function _scrollTopOffset(container, item) {
		return - container.position().top - container.css('margin-top').replace('px', '') + item.parent().offset() ? item.parent().offset().top : 0
	}


//	function _handleSearch(value) {
//		location.hash = '#search=' + value;
//	}

	return {
		create : function() {
			doc.ready(_initFromHash);

//			sb.subscribe('searchLocales', _handleSearch);
//
//			var hash = location.hash;
//			if(hash !== '' && hash.startsWith('#search=')) {
//				var s = hash.replace('#search=', '');
//				sb.publish('initSearchLocales', s);
//				sb.publish('searchLocales', s);
//			}
		},
		destroy : function() {
		}
	};
};

App.Core.register('SuggestionModule', App.Modules.SuggestionModule);
App.Core.register('ProjectSearchModule', App.Modules.ProjectSearchModule);
App.Core.register('KeyCreateModule', App.Modules.KeyCreateModule);
App.Core.register('KeyListModule', App.Modules.KeyListModule);
App.Core.register('MessageModule', App.Modules.MessageModule);
App.Core.register('MessageListModule', App.Modules.MessageListModule);
App.Core.register('LocaleHashModule', App.Modules.LocaleHashModule);
