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

App.Core.register('MaterialModule', App.Modules.MaterialModule);
App.Core.register('SuggestionModule', App.Modules.SuggestionModule);
App.Core.register('ProjectSearchModule', App.Modules.ProjectSearchModule);
App.Core.register('ActivityModule', App.Modules.ActivityModule, {
	dataUrl: jsRoutes.controllers.Projects.activityCsv(projectId).url,
	cellUrl: jsRoutes.controllers.Projects.activity(projectId).url,
	messages: contributionMessages
});