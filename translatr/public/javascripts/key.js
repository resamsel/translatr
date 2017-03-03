/*
 * Input:
 * 
 * * projectId: Project.id
 * * keyId: Key.id
 * * keyName: Key.name
 * * locales: List of Locale
 */

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

		switchButton.attr('href', jsRoutes.controllers.Locales.locale(item.localeId).url + '?search=' + keyName + '#key=' + keyName);
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

App.Core.register('SuggestionModule', App.Modules.SuggestionModule);
App.Core.register('KeyHashModule', App.Modules.KeyHashModule);
