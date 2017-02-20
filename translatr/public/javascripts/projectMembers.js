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
