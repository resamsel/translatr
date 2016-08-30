var options = {
	chartPadding: {
		left: -10,
		bottom: -10
	},
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
	$('#modal-create-locale .btn-save').click(function() {
		$('#form-locale').attr('action', $('#form-locale').attr('action') + '#locale=' + $('#field-locale-name').val());
		$('#form-locale').submit();
	});
	$('#modal-create-key .btn-save').click(function() {
		$('#form-key').attr('action', $('#form-key').attr('action') + '#key=' + $('#field-key-name').val());
		$('#form-key').submit();
	});

	new Chartist.Bar('#chart-timeline', data, options);
});
