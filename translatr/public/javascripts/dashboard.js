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

App.Modules.DashboardSearchModule = function(sb) {
	var fieldSearch = sb.dom.find('#field-search');
	var win = sb.dom.wrap(window);

	function _handleSelect(suggestion) {
		sb.publish('suggestionSelected', suggestion);
	}

	function _handleKeyPress(event) {
		if (event.which === 70 && (event.ctrlKey || event.metaKey)) {
			event.preventDefault();
			fieldSearch.focus().select();
	    }
	}

	return {
		create: function() {
			win.keydown(_handleKeyPress);

			fieldSearch.autocomplete({
				serviceUrl: jsRoutes.controllers.Dashboards.search().url,
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

App.Modules.ProjectCreateModule = function(sb) {
	var form = sb.dom.find('#form-project');
	var buttonSave = sb.dom.find('#modal-create-project .btn-save');

	return {
		create: function() {
			buttonSave.click(function() {
				form.submit();
			});
		},
		destroy: function() {
		}
	};
};

App.Core.register('SuggestionModule', App.Modules.SuggestionModule);
App.Core.register('DashboardSearchModule', App.Modules.DashboardSearchModule);
App.Core.register('ProjectCreateModule', App.Modules.ProjectCreateModule);
