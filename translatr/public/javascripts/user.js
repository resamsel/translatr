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
			new Chartist.Bar('#chart-timeline', data, options);
		},
		destroy: function() {
		}
	};
};

App.Core.register('TimelineModule', App.Modules.TimelineModule);
