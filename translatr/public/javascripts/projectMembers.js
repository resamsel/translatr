App.Modules.MembersCreateModule = function(sb) {
	var select = sb.dom.find('select');
	var form = sb.dom.find('#form-member');
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
		if(suggestion.data.name != '+++' && suggestion.data.name != '???' && suggestion.data.type == 'key') {
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
App.Core.register('MembersCreateModule', App.Modules.MembersCreateModule);
App.Core.register('SuggestionModule', App.Modules.SuggestionModule);
App.Core.register('ProjectSearchModule', App.Modules.ProjectSearchModule);
