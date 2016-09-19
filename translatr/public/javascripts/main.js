var App = App || {};
App.Modules = App.Modules || {};

$(document).ready(function() {
	// the "href" attribute of .modal-trigger must specify the modal ID that
	// wants to be triggered
	$('.modal-trigger').leanModal();
});

App.Modules.ProjectSearchModule = function(sb) {
	var fieldSearch = sb.dom.find('#field-search');
	var win = sb.dom.wrap(window);

	function _handleSelect(suggestion) {
		sb.publish('suggestionSelected', suggestion);
	}

	function _handleKeyPress(event) {
		console.log(event);
		if (event.which == 70 && (event.ctrlKey || event.metaKey)) {
			event.preventDefault();
			fieldSearch.focus().select();
	    }
	}

	return {
		create: function() {
			win.keydown(_handleKeyPress);

			fieldSearch.autocomplete({
				serviceUrl: jsRoutes.controllers.Projects.projectSearch(projectId).url,
				onSelect: _handleSelect,
				triggerSelectOnValidInput: false,
				deferRequestBy: 200,
				paramName: 'search',
				groupBy: 'type'
			});
		},
		destroy: function() {
		}
	};
};