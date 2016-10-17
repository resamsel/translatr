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
		if (event.which == 70 && (event.ctrlKey || event.metaKey)) {
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
	var width = options.width || 901,
	    height = options.height || 136,
	    cellSize = options.cellSize || 17;

	var numberOfColors = options.numberOfColors || 4;

	var percent = d3.format(".1%"),
	    format = d3.time.format("%Y-%m-%d"),
	    titleFormat = d3.time.format(options.titleFormat || "%A, %B %d, %Y");

	var color = d3.scale.quantize()
	    .domain([0, 1])
	    .range(d3.range(numberOfColors).map(function(d) { return "q" + d + "-" + numberOfColors; }));

	var weekOfYear = d3.time.mondayOfYear;

	return {
		create: function() {
			if(typeof options.dataUrl === 'undefined') {
				return;
			}

			var startDate = new Date(),
				today = new Date();

			today.setDate(today.getDate() + 1);
			today.setHours(0, 0, 0, 0);
			startDate.setHours(0, 0, 0, 0);
			startDate.setFullYear(today.getFullYear() - 1);

			var svg = d3.select(options.container || ".svg-container").selectAll("svg")
				.data(d3.range(startDate.getFullYear(), today.getFullYear()))
				.enter().append("svg")
				.attr("preserveAspectRatio", "xMinYMin meet")
				.attr("class", "RdYlGn svg-content")
				.attr("viewBox", "0 0 " + width + " " + height)
				.append("g");

			// Y-Axis
			svg.append("text")
			    .attr("transform", "translate(-6," + cellSize * 3.5 + ")rotate(-90)")
			    .style("text-anchor", "middle")
			    .text(function(d) { return d; });

			var woyOffset = weekOfYear(startDate);
			var startYear = startDate.getFullYear();
			var numberOfWeeks = weekOfYear(new Date(startYear, 11, 31));

			function x(d) {
				var diffYears = d.getFullYear() - startYear;
				var woy = weekOfYear(d);

				return (woy - woyOffset + numberOfWeeks * diffYears) * cellSize;
			}

			function y(d) {
				return (d.getDay() - 1 + 7)%7 * cellSize;
			}

			// Cells
			var rect = svg.selectAll(".day")
				.data(function(d) { return d3.time.days(startDate, today); })
				.enter().append("rect")
				.attr("class", "day")
				.attr("width", cellSize)
				.attr("height", cellSize)
				//.attr("x", function(d) { return ((d3.time.weekOfYear(d) + (52 - d3.time.weekOfYear(today)))%52 + 52) * cellSize; })
				.attr("x", x)
				.attr("y", y)
				.datum(format);

			rect.append("title").text(function(d) { return titleFormat(format.parse(d)); });

			d3.csv(options.dataUrl, function(error, csv) {
				if (error) throw error;

				var data = d3.nest()
					.key(function(d) { return d.Date; })
					.rollup(function(d) { return d[0]; })
					.map(csv);

				rect.filter(function(d) { return d in data; })
					.attr("class", function(d) { return "day " + color(data[d].RelativeValue); })
					.select("title")
					.text(function(d) { return titleFormat(format.parse(d)) + ": " + data[d].Value; });
			});
		},
		destroy: function() {
		}
	};
};

App.Core.register('ModalModule', App.Modules.ModalModule);
App.Core.register('NProgressModule', App.Modules.NProgressModule);
