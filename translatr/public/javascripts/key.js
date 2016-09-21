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
	var form = sb.dom.find('#form-message');
	var progress = form.find('.progress');
	var fieldId = sb.dom.find('#field-id');
	var fieldLocale = sb.dom.find('#field-locale');
	var fieldValue = sb.dom.find('#field-value');
	var localeName = sb.dom.find('#locale-name');
	var preview = sb.dom.find('#preview');
	var message = sb.dom.find("#panel-message");
    var messages = sb.dom.find('#panel-messages');
    var template = sb.dom.find('.message.template');
    var cancelButton = sb.dom.find('.btn-cancel');
    var noSelection = sb.dom.find("#no-selection");

    function _handleSaveMessage(message) {
    	progress.css('visibility', 'hidden');
    	sb.dom.find('#' + message.localeId).removeClass('no-message');
    	sb.dom.find('#' + message.localeId + ' .value').html(message.value);
    	sb.publish('localeSelected', message.localeId);
    	Materialize.toast(messages['message.updated'], 5000);
    }

    function _updateForm(localeId) {
    	noSelection.hide();
    	message.show();
    	preview.show();
    	fieldId.val('');
    	fieldLocale.val(keyName).attr('localeId', localeId);
    	fieldValue.val('');
    	Materialize.updateTextFields();
    	preview.html('');
    	localeName.html(sb.dom.find('#' + localeId + ' .name').html());
    	sb.utilities.ajax(
    		jsRoutes.controllers.Api.getMessage(localeId, keyName)
    	).done(_handleMessage);
    }

    function _handleMessage(message) {
    	fieldId.val(message.id);
    	fieldLocale.val(message.localeName).attr('localeId', message.localeId);
    	fieldValue.val(message.value).trigger('autoresize');
    	Materialize.updateTextFields();
    	preview.html(message.value);
    }

	return {
		create: function() {
			message.hide();

			var locales = sb.dom.find('a.locale');
			locales.click(function() {
				var $this = sb.dom.wrap(this);
				locales.parent().removeClass('active');
				$this.parent().addClass('active')
				_updateForm($this.attr('id'));
			})

			fieldValue.on('change keyup paste', function() {
				preview.html(fieldValue.val());
			});

			cancelButton.click(function() {
				sb.dom.find('a.locale').parent().removeClass('active');
				message.hide();
				preview.hide();
				noSelection.show();
				window.location.hash = '#';
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
								  "id": fieldLocale.attr('localeId')
								},
								"key": {
									"id": keyId
								},
								"value": fieldValue.val()
							})
						}
					)
				).done(_handleSaveMessage);
		    });
		},
		destroy: function() {
		}
	};
};

$(document).ready(function() {
	var params = $.deparam.fragment();
	if('locale' in params) {
		var $a = $('a.locale[localeName="'+params.locale+'"]');
		$a.click();
	} else {
		var $locale = $('.locales .collection-item:first-child a.locale');
		$locale.click();
	}
});

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

App.Core.register('KeyCreateModule', App.Modules.KeyCreateModule);
App.Core.register('MessageModule', App.Modules.MessageModule);
App.Core.register('SuggestionModule', App.Modules.SuggestionModule);
App.Core.register('ProjectSearchModule', App.Modules.ProjectSearchModule);
