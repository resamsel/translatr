var App = App || {};
App.Modules = App.Modules || {};

App.Modules.ModalModule = function(sb) {
	return {
		create: function() {
			// the "href" attribute of .modal-trigger must specify the modal ID that
			// wants to be triggered
			sb.dom.find('.modal-trigger').leanModal();
		}
	};
};

App.Modules.MaterialModule = function(sb) {
	return {
		create: function() {
			sb.dom.find('select').material_select();
		},
		destroy: function() {
		}
	};
};

App.Modules.NProgressModule = function(sb) {
	var doc = sb.dom.wrap(document);
	var win = sb.dom.wrap(window);

	return {
		create: function() {
			doc.ajaxStart(function() {
				NProgress.start();
			});
			doc.ajaxStop(function() {
				NProgress.done();
			});
			win.on('beforeunload', function() {
				NProgress.start();
			});
		}
	}
}

App.Modules.ProjectSearchModule = function(sb) {
	var fieldSearch = sb.dom.find('#field-search');
	var win = sb.dom.wrap(window);

	function _handleSelect(suggestion) {
		sb.publish('suggestionSelected', suggestion);
	}

	function _handleKeyPress(event) {
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

App.Core.register('ModalModule', App.Modules.ModalModule);
App.Core.register('NProgressModule', App.Modules.NProgressModule);
