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

App.Modules.KeysSearchModule = function(sb) {
	var fieldSearch = sb.dom.find('#field-search');
	var tbody = sb.dom.find('#keys tbody');
	// timer identifier
	var typingTimer;
	// time in ms
	var doneTypingInterval = 500;

	function _handleInitSearch(value) {
		fieldSearch.val(value);
	}

	function _handleDoneTyping() {
		sb.publish('searchKeys', fieldSearch.val());
	}

	function _handleSearch(value) {
		$.ajax({
			url: jsRoutes.controllers.Application.projectKeysSearch(projectId).url,
			data: {'search': value}
		}).done(function(data) {
			tbody.html(data);
		});
	}

	return {
		create : function() {
			sb.subscribe('initSearchKeys', _handleInitSearch);
			sb.subscribe('searchKeys', _handleSearch);

			fieldSearch.on('change keyup paste', function() {
			    clearTimeout(typingTimer);
		        typingTimer = setTimeout(_handleDoneTyping, doneTypingInterval);
			});
		},
		destroy : function() {
		}
	};
};

App.Core.register('MaterialModule', App.Modules.MaterialModule);
App.Core.register('KeysCreateModule', App.Modules.KeysCreateModule);
App.Core.register('KeysSearchModule', App.Modules.KeysSearchModule);