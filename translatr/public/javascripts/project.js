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

App.Core.register('SuggestionModule', App.Modules.SuggestionModule);
App.Core.register('ProjectSearchModule', App.Modules.ProjectSearchModule);
App.Core.register('TimelineModule', App.Modules.TimelineModule);

$(document).ready(function() {
	$('select').material_select();
	$('#modal-create-locale .btn-save').click(function() {
		$('#form-locale').attr('action', $('#form-locale').attr('action') + '#locale=' + $('#field-locale-name').val());
		$('#form-locale').submit();
	});
	$('#modal-create-key .btn-save').click(function() {
		$('#form-key').attr('action', $('#form-key').attr('action') + '#key=' + $('#field-key-name').val());
		$('#form-key').submit();
	});
});
