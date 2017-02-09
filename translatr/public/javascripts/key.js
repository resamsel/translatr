/*
 * Input:
 * 
 * * projectId: Project.id
 * * keyId: Key.id
 * * keyName: Key.name
 * * locales: List of Locale
 */

App.Modules.KeyCreateModule = function(sb) {
	var form = sb.dom.find('#form-key');
	var saveButton = sb.dom.find('.modal .btn-save');

	return {
		create : function() {
			saveButton.click(function() {
				form.submit();
			});
		},
		destroy : function() {
		}
	};
};

App.Modules.MessageModule = function(sb) {
	var win = sb.dom.wrap(window);
	var form = sb.dom.find('#form-message');
	var progress = form.find('.progress');
	var fieldId = sb.dom.find('#field-id');
	var fieldLocale = sb.dom.find('#field-locale');
	var fieldValue = sb.dom.find('#field-value');
	var localeName = sb.dom.find('#locale-name');
	var panelPreview = sb.dom.find('#panel-preview');
	var panelMessages = sb.dom.find('#panel-messages');
	var panelActions = sb.dom.find('.item-main .filter');
	var preview = sb.dom.find('#preview');
	var message = sb.dom.find("#form-message");
    var template = sb.dom.find('.message.template');
    var submitButton = sb.dom.find('#message-submit');
    var cancelButton = sb.dom.find('#message-cancel');
    var noSelection = sb.dom.find("#no-selection");
    var rightFilter = sb.dom.find(".item-right .filter");

	function _handleKeyPress(event) {
		if (event.which == 13 && (event.ctrlKey || event.metaKey)) {
			event.preventDefault();
			form.submit();
	    }
	}

    function _handleSaveMessage(message) {
    	progress.css('visibility', 'hidden');
    	sb.dom.find('#' + message.localeId).removeClass('no-message');
    	sb.dom.find('#' + message.localeId + ' .value').text(message.value);
    	sb.publish('localeSelected', message.localeId);
    	Materialize.toast(messages['message.updated'], 5000);
    }

    function _handleMessage(messageList) {
    	if(messageList.length === 0)
    		return;

    	var message = messageList[0];
    	fieldId.val(message.id);
    	fieldLocale.val(message.localeName).attr('localeId', message.localeId);
    	fieldValue.val(message.value).trigger('autoresize').focus();
    	Materialize.updateTextFields();
    	preview.html(message.value);
    }

    function _updateForm(localeId) {
    	noSelection.hide();
    	message.show();
    	panelPreview.show();
		panelActions.show();
		rightFilter.show();
    	fieldId.val('');
    	fieldLocale.val(keyName).attr('localeId', localeId);
    	fieldValue.val('');
    	Materialize.updateTextFields();
    	preview.html('');
    	localeName.text(sb.dom.find('#' + localeId + ' .name').text());
    	sb.utilities.ajax(sb.utilities.merge(
			jsRoutes.controllers.TranslationsApi.find(projectId),
			{data: {"localeId": localeId, "keyName": keyName}}
		)).done(_handleMessage);
    }

	return {
		create: function() {
			sb.subscribe('localeSelected', _updateForm);

			message.hide();
			panelPreview.hide();
			panelMessages.hide();
			panelActions.hide();
			rightFilter.hide();

			submitButton.on('click', function() {
				form.submit();
			});
			win.keydown(_handleKeyPress);

			var locales = sb.dom.find('a.locale');
			locales.click(function() {
				var $this = sb.dom.wrap(this);
				locales.removeClass('active');
				$this.addClass('active');
				sb.publish('localeSelected', $this.attr('id'));
			})

			fieldValue.on('change keyup paste', function() {
				preview.html(fieldValue.val());
			});

			cancelButton.click(function() {
				sb.dom.find('.locales a.locale.active').removeClass('active');
				message.hide();
				panelPreview.hide();
				panelMessages.hide();
				panelActions.hide();
				rightFilter.hide();
				noSelection.show();
				window.location.hash = '#';
			});

			form.submit(function(e){
		        e.preventDefault();
		    	progress.css('visibility', 'visible');

		        var op;
		    	var data = {
					"localeId": fieldLocale.attr('localeId'),
					"keyId": keyId,
					"value": fieldValue.val()
				};

		        if(fieldId.val() !== '') {
		        	op = jsRoutes.controllers.TranslationsApi.update();
		        	data["id"] = fieldId.val();
		        } else {
		        	op = jsRoutes.controllers.TranslationsApi.create();
		        }

				sb.utilities.ajax(sb.utilities.merge(
					op,
					{
						contentType: 'application/json',
						dataType: 'json',
						data: JSON.stringify(data)
					}
				)).done(_handleSaveMessage);
		    });
		},
		destroy: function() {
		}
	};
};

App.Modules.EditorSwitchModule = function(sb, options) {
	var keyName = options.keyName || '';
	var switchButton = sb.dom.find('#switch-editor');

	function _handleItemSelected(item) {
		if(item === null) {
			switchButton.attr('href', '#');
			switchButton.addClass('disabled');

			return;
		}

		switchButton.attr('href', jsRoutes.controllers.Locales.locale(item.localeId).url + '#key=' + keyName);
		switchButton.removeClass('disabled');
	}

	return {
		create: function() {
			sb.subscribe('itemSelected', _handleItemSelected);
		},
		destroy: function() {
		}
	}
}

App.Modules.KeyHashModule = function(sb) {
	var params = $.deparam.fragment();
	var doc = sb.dom.wrap(document);

	function _initFromHash() {
		if('locale' in params) {
			var $a = sb.dom.find('a.locale[localeName="'+params.locale+'"]');
			$a.click();
		} else {
			var $locale = sb.dom.find('.locales .collection-item.locale:first-child');
			$locale.click();
		}
	}
	
	return {
		create: function() {
			doc.ready(_initFromHash);
		},
		destroy: function() {
		}
	};
}

App.Modules.SuggestionModule = function(sb) {
	var filterUntranslated = sb.dom.find('#field-untranslated');

	function _handleSearch(value) {
		if(value !== '') {
			sb.dom.find('a.locale').parent().hide();
			if(filterUntranslated.is(':checked')) {
				sb.dom.find('a.locale.no-message .name:contains("' + value + '"), a.locale.no-message .value:contains("' + value + '")')
				.parent().parent().show();
			} else {
				sb.dom.find('a.locale .name:contains("' + value + '"), a.locale .value:contains("' + value + '")')
				.parent().parent().show();
			}
		} else {
			if(filterUntranslated.is(':checked')) {
				sb.dom.find('a.locale').parent().hide();
				sb.dom.find('a.locale.no-message').parent().show();
			} else {
				sb.dom.find('a.locale').parent().show();
			}
		}
	}

	function _handleSuggestionSelected(suggestion) {
		if(suggestion.data.type == 'locale' && suggestion.data.name != '+++') {
			_handleSearch(suggestion.data.name);
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

App.Modules.EditorSwitchModule = function(sb, options) {
	var keyName = options.keyName || '';
	var switchButton = sb.dom.find('#switch-editor');

	function _handleItemSelected(item) {
		if(item === null) {
			switchButton.attr('href', '#');
			switchButton.addClass('disabled');

			return;
		}

		switchButton.attr('href', jsRoutes.controllers.Locales.locale(item.localeId).url + '#key=' + keyName);
		switchButton.removeClass('disabled');
	}

	return {
		create: function() {
			sb.subscribe('itemSelected', _handleItemSelected);
		},
		destroy: function() {
		}
	}
}

App.Core.register('KeyCreateModule', App.Modules.KeyCreateModule);
//App.Core.register('MessageModule', App.Modules.MessageModule);
App.Core.register('SuggestionModule', App.Modules.SuggestionModule);
App.Core.register('KeyHashModule', App.Modules.KeyHashModule);
