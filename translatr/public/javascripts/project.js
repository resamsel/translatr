App.Modules.LocaleCreateModule = function(sb) {
	var form = sb.dom.find('#form-locale');
	var fieldLocaleName = sb.dom.find('#field-locale-name');
	var buttonSave = sb.dom.find('#modal-create-locale .btn-save');

	return {
		create: function() {
			buttonSave.click(function() {
				form.attr('action', form.attr('action') + '#locale=' + fieldLocaleName.val());
				form.submit();
			});
		},
		destroy: function() {
		}
	};
};

App.Modules.KeyCreateModule = function(sb) {
	var form = sb.dom.find('#form-key');
	var fieldKeyName = sb.dom.find('#field-key-name');
	var buttonSave = sb.dom.find('#modal-create-key .btn-save');

	return {
		create: function() {
			buttonSave.click(function() {
				form.attr('action', form.attr('action') + '#key=' + fieldKeyName.val());
				form.submit();
			});
		},
		destroy: function() {
		}
	};
};

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
App.Core.register('LocaleCreateModule', App.Modules.LocaleCreateModule);
App.Core.register('KeyCreateModule', App.Modules.KeyCreateModule);
App.Core.register('SuggestionModule', App.Modules.SuggestionModule);
App.Core.register('ProjectSearchModule', App.Modules.ProjectSearchModule);
App.Core.register('ActivityModule', App.Modules.ActivityModule, {dataUrl: jsRoutes.controllers.Projects.activityCsv(projectId).url});