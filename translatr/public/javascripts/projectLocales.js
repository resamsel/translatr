App.Modules.LocalesCreateModule = function(sb) {
	var select = sb.dom.find('select');
	var form = sb.dom.find('#form-locale');
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

App.Modules.LocalesSearchModule = function(sb) {
	var fieldSearch = sb.dom.find('#field-search');

	function _handleInitSearch(value) {
		fieldSearch.val(value);
	}

	function _handleSearch(value) {
		if (value !== '') {
			sb.dom.find('tr.locale').hide();
			sb.dom.find('tr.locale[name*="' + value.toLowerCase() + '"]').show();
		} else {
			sb.dom.find('tr.locale').show();
		}
	}

	return {
		create : function() {
			sb.subscribe('initSearchLocales', _handleInitSearch);
			sb.subscribe('searchLocales', _handleSearch);

			fieldSearch.on('change keyup paste', function() {
				sb.publish('searchLocales', fieldSearch.val());
			});
		},
		destroy : function() {
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
App.Core.register('LocalesCreateModule', App.Modules.LocalesCreateModule);
App.Core.register('LocalesSearchModule', App.Modules.LocalesSearchModule);
App.Core.register('LocalesHashModule', App.Modules.LocalesHashModule);
