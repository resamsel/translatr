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
			NProgress.configure({ showSpinner: false });
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
		if (event.which === 70 && (event.ctrlKey || event.metaKey)) {
			event.preventDefault();
			fieldSearch.focus().select();
	    }
	}

	return {
		create: function() {
			win.keydown(_handleKeyPress);

			fieldSearch.autocomplete({
				serviceUrl: jsRoutes.controllers.Projects.search(projectId).url,
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

App.Modules.ActivityModule = function(sb, options) {
	var cellSize = options.cellSize || 17,
	    xAxisHeight = options.xAxisHeight || 20,
	    xAxisPadding = options.xAxisPadding || 8,
	    yAxisWidth = options.yAxisWidth || 36,
	    yAxisPadding = options.yAxisPadding || 8,
	    legendOffsetWeek = options.legendOffsetWeek || 45;

	var messages = options.messages || {
		'contributions.legend.tooltip': 'Relative activity: {0}-{1}% (log(n)/log(max))',
		'contributions.legend.less': 'Less',
		'contributions.legend.more': 'More'
	};

	var width = (options.width || 53*cellSize) + yAxisWidth + (yAxisPadding*2),
		height = (options.height || 9*cellSize) + xAxisHeight;

	var numberOfColors = options.numberOfColors || 4;

	var percent = d3.format(".1%"),
	    format = d3.time.format("%Y-%m-%d"),
	    titleFormat = d3.time.format(options.titleFormat || "%A, %B %d, %Y"),
	    xAxisFormat = d3.time.format(options.xAxisFormat || "%b"),
	    yAxisFormat = d3.time.format(options.yAxisFormat || "%a");

	var cellUrl = options.cellUrl || null;
	var clickable = "";
	if (cellUrl) {
		clickable = "clickable";
	}
	var active = d3.time.format.iso.parse(options.active);

	var color = d3.scale.quantize()
	    .domain([0, 1])
	    .range(d3.range(numberOfColors).map(function(d) { return "q" + d + "-" + numberOfColors; }));

	var weekOfYear = d3.time.mondayOfYear;
	var dayOfWeek = function(d) {
		return (d.getDay() + 6)%7;
	}

	function activeCell(d, active) {
		if(d === format(active)) {
			return "active";
		}
		return "";
	}
	
	function y(d) {
		return xAxisHeight + dayOfWeek(d) * cellSize;
	}

	function _handleCellClick(d) {
		if(cellUrl) {
			var min = format.parse(d);
			var max = format.parse(d);
			max.setDate(max.getDate() + 1);
			var whenCreatedMin = d3.time.format.iso(min);
			var whenCreatedMax = d3.time.format.iso(max);
			window.location.href = cellUrl + '?whenCreatedMin=' + whenCreatedMin + '&whenCreatedMax=' + whenCreatedMax;
		}
	}

	return {
		create: function() {
			if(typeof options.dataUrl === 'undefined') {
				return;
			}

			var startDate = new Date(),
				today = new Date();

			today.setHours(0, 0, 0, 0);
			startDate.setHours(0, 0, 0, 0);
			startDate.setDate(
				today.getDate() // use today as base
				- (52*7) // subtract 52 weeks
				- dayOfWeek(today) // subtract the day of the week
			);
			today.setDate(today.getDate() + 1);

			var svg = d3.select(options.container || ".svg-container").selectAll("svg")
				.data(d3.range(startDate.getFullYear(), today.getFullYear()))
				.enter().append("svg")
				.attr("preserveAspectRatio", "xMinYMin meet")
				.attr("class", "RdYlGn svg-content " + clickable)
				.attr("viewBox", "0 0 " + width + " " + height)
				.append("g");

			var woyOffset = weekOfYear(startDate);
			var startYear = startDate.getFullYear();
			var numberOfWeeks = weekOfYear(new Date(startYear, 11, 31));

			function x(d) {
				var diffYears = d.getFullYear() - startYear;
				var woy = weekOfYear(d);

				return yAxisWidth + yAxisPadding + (woy - woyOffset + numberOfWeeks * diffYears) * cellSize + 1;
			}

			// X-Axis
			var xAxis = svg.selectAll(".month")
				.data(function(d) { return d3.time.months(startDate, today); })
				.enter().append("text")
				.attr("class", "month")
				.attr("x", x)
				.attr("y", function(d) { return xAxisHeight - xAxisPadding; })
				.attr("height", xAxisHeight)
				.text(function(d) { return xAxisFormat(d); });

			// Y-Axis
			svg.selectAll(".weekday")
				.data(function(d) { return [new Date(2014, 0, 7), new Date(2014, 0, 9), new Date(2014, 0, 11)]; })
				.enter().append("text")
				.attr("class", "weekday")
				.attr("x", 0)
				.attr("y", function(d) { return xAxisHeight - xAxisPadding + y(d); })
				.attr("width", yAxisWidth)
				.attr("height", cellSize)
			    .text(function(d) { return yAxisFormat(d); });

			// Cells
			var rect = svg.selectAll(".day")
				.data(function(d) { return d3.time.days(startDate, today); })
				.enter().append("rect")
				.attr("class", "day")
				.attr("width", cellSize - 2)
				.attr("height", cellSize - 2)
				.attr("x", x)
				.attr("y", y)
				.datum(format);

			rect.append("title").text(function(d) { return titleFormat(format.parse(d)); });

			function legendTooltip(d) {
				var from = (d-1)*25, until = d*25;
				if(d === 0) {
					from = '';
					until = 0;
				}
				return messages['contributions.legend.tooltip'].replace('{0}', from).replace('{1}', until);
			}

			// Legend
			svg.selectAll('.legend.day')
				.data([0, 1, 2, 3, 4])
				.enter().append("rect")
				.attr("class", function(d) { return "legend day q" + (d-1) + "-4"; })
				.attr("width", cellSize - 2)
				.attr("height", cellSize - 2)
				.attr("x", function(d) { return yAxisWidth + yAxisPadding + cellSize * (legendOffsetWeek + d); })
				.attr("y", xAxisPadding + cellSize * 8)
				.append("title").text(legendTooltip);
			svg.append("text")
				.attr("class", "legend")
				.attr("height", cellSize)
				.attr("x", yAxisWidth + cellSize * legendOffsetWeek)
				.attr("y", cellSize * 9 + xAxisPadding/2)
				.style("text-anchor", "end")
				.text(messages['contributions.legend.less']);
			svg.append("text")
				.attr("class", "legend")
				.attr("height", cellSize)
				.attr("x", yAxisWidth + cellSize * (legendOffsetWeek + 6) - yAxisPadding/2)
				.attr("y", cellSize * 9 + xAxisPadding/2)
				.text(messages['contributions.legend.more']);

			d3.csv(options.dataUrl, function(error, csv) {
				if (error) throw error;

				var data = d3.nest()
					.key(function(d) { return d.Date; })
					.rollup(function(d) { return d[0]; })
					.map(csv);

				rect.filter(function(d) { return d in data; })
					.attr("class", function(d) { return "day has-data " + color(data[d].RelativeValue); })
					.on("click", _handleCellClick)
					.select("title")
					.text(function(d) { return titleFormat(format.parse(d)) + ": " + data[d].Value; });

				if(active) {
					svg.append("rect")
						.attr("class", "day")
						.attr("width", cellSize)
						.attr("height", cellSize)
						.attr("x", x(active))
						.attr("y", y(active));
				}
			});
		},
		destroy: function() {
		}
	};
};

App.Modules.NotificationModule = function(sb) {
	var button = sb.dom.find('#notification-button');

	function _handleNotification() {
		if(button.hasClass('available')) {
			sb.utilities.ajax(
				jsRoutes.controllers.Profiles.resetNotifications()
			).done(_handleNotificationsChanged);
		}
	}

	function _handleNotificationsChanged(data) {
		button.removeClass('available');
	}

	return {
		create: function() {
			button.click(_handleNotification);
		}
	}
}

App.Core.register('ModalModule', App.Modules.ModalModule);
App.Core.register('NProgressModule', App.Modules.NProgressModule);
App.Core.register('NotificationModule', App.Modules.NotificationModule);
