App.Modules.DashboardSearchModule = function(sb) {
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

	// TODO: Replace with AJAX search
	function _handleSearch(value) {
	    if(value !== '') {
			$('.card.project').hide();
			$('.card.project[name*="' + value.toLowerCase() +'"]').show();
	    } else {
	    	$('.card.project').show();
	    }
	}

	return {
		create: function() {
			win.keydown(_handleKeyPress);

			// TODO: Replace with AJAX search
			fieldSearch.on('change keyup paste', function() {
				_handleSearch(fieldSearch.val());
			});

//			fieldSearch.autocomplete({
//				serviceUrl: jsRoutes.controllers.Application.dashboardSearch(projectId).url,
//				onSelect: _handleSelect,
//				triggerSelectOnValidInput: false,
//				deferRequestBy: 200,
//				paramName: 'search',
//				groupBy: 'type'
//			});
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

App.Core.register('DashboardSearchModule', App.Modules.DashboardSearchModule);
App.Core.register('ProjectCreateModule', App.Modules.ProjectCreateModule);
