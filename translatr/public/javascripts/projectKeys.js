App.Modules.KeysCreateModule = function(sb) {
	var select = sb.dom.find('select');
	var form = sb.dom.find('#form-key');
	var saveButton = sb.dom.find('.btn-save');

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

App.Modules.SuggestionModule = function(sb) {
	function _handleSuggestionSelected(suggestion) {
		if(suggestion.data.name != '+++' && suggestion.data.name != '???' && suggestion.data.type === 'key') {
			window.location.href = jsRoutes.controllers.Keys.key(suggestion.data.id).url;
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

App.Core.register('MaterialModule', App.Modules.MaterialModule);
App.Core.register('KeysCreateModule', App.Modules.KeysCreateModule);
App.Core.register('SuggestionModule', App.Modules.SuggestionModule);
App.Core.register('ProjectSearchModule', App.Modules.ProjectSearchModule);
