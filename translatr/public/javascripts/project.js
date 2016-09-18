App.Modules.MaterialModule = function(sb) {
	var select = sb.dom.find('select');

	return {
		create: function() {
			select.material_select();
		},
		destroy: function() {
		}
	};
};

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

App.Modules.TimelineModule = function(sb) {
	var options = {
		chartPadding: {
			bottom: -10
		},
		showArea: true,
		showLine: false,
		showPoint: false,
		stackBars: true,
		axisX: {
			type: Chartist.FixedScaleAxis,
			divisor: 8,
			labelInterpolationFnc: function(value) {
				return moment(value).format('MMM D, HH:00');
			},
			showGrid: false
		},
		axisY: {
			onlyInteger: true,
			showGrid: false
		}
	};

	return {
		create: function() {
			new Chartist.Line('#chart-timeline', data, options);
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
App.Core.register('TimelineModule', App.Modules.TimelineModule);
