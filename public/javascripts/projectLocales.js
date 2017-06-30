App.Modules.SuggestionModule = function(sb) {
	function _handleSuggestionSelected(suggestion) {
		window.location.href = suggestion.data.url;
	}

	return {
		create: function() {
			sb.subscribe('suggestionSelected', _handleSuggestionSelected);
		},
		destroy: function() {
		}
	};
};

App.Modules.LocalesHashModule = function(sb) {
	var location = window.location;

	function _handleSearch(value) {
		location.hash = '#search=' + value;
	}

	return {
		create : function() {
			sb.subscribe('searchLocales', _handleSearch);

			var hash = location.hash;
			if(hash !== '' && hash.startsWith('#search=')) {
				var s = hash.replace('#search=', '');
				sb.publish('initSearchLocales', s);
				sb.publish('searchLocales', s);
			}
		},
		destroy : function() {
		}
	};
};

App.Core.register('MaterialModule', App.Modules.MaterialModule);
App.Core.register('ProjectSearchModule', App.Modules.ProjectSearchModule);
App.Core.register('LocalesHashModule', App.Modules.LocalesHashModule);
