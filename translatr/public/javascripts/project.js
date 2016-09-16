var options = {
	chartPadding: {
		left: -10,
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

	new Chartist.Line('#chart-timeline', data, options);
});

App.Modules.ProjectSearchModule = function(sb) {
	var fieldSearch = sb.dom.find('#field-search');

	function _handleResultSelection(suggestion) {
		console.log('Result selected: ', suggestion);
		window.location.href = suggestion.data;
	}

	return {
		create : function() {
			fieldSearch.autocomplete({
				serviceUrl: jsRoutes.controllers.Projects.projectSearch(projectId).url,
				onSelect: _handleResultSelection,
				deferRequestBy: 200,
				paramName: 'search'
			});
		},
		destroy : function() {
		}
	};
};

App.Core.register('ProjectSearchModule', App.Modules.ProjectSearchModule);
