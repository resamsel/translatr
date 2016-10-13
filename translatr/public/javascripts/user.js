App.Modules.ActivityModule = function(sb) {
	var width = 901,
    height = 136,
    cellSize = 17; // cell size

	var percent = d3.format(".1%"),
	    format = d3.time.format("%Y-%m-%d");

	var color = d3.scale.quantize()
	    .domain([0, 1])
	    .range(d3.range(11).map(function(d) { return "q" + d + "-11"; }));

	return {
		create: function() {
			var svg = d3.select(".svg-container").selectAll("svg")
			    .data(d3.range(2016, 2017))
			  .enter().append("svg")
			    .attr("preserveAspectRatio", "xMinYMin meet")
			    .attr("class", "RdYlGn svg-content")
			    .attr("viewBox", "0 0 " + width + " " + height)
			    .append("g");

			svg.append("text")
			    .attr("transform", "translate(-6," + cellSize * 3.5 + ")rotate(-90)")
			    .style("text-anchor", "middle")
			    .text(function(d) { return d; });

			var rect = svg.selectAll(".day")
			    .data(function(d) { return d3.time.days(new Date(d, 0, 1), new Date(d + 1, 0, 1)); })
			  .enter().append("rect")
			    .attr("class", "day")
			    .attr("width", cellSize)
			    .attr("height", cellSize)
			    .attr("x", function(d) { return d3.time.weekOfYear(d) * cellSize; })
			    .attr("y", function(d) { return d.getDay() * cellSize; })
			    .datum(format);

			rect.append("title")
			    .text(function(d) { return d; });

			svg.selectAll(".month")
			    .data(function(d) { return d3.time.months(new Date(d, 0, 1), new Date(d + 1, 0, 1)); })
			  .enter().append("path")
			    .attr("class", "month");

			d3.csv(jsRoutes.controllers.Users.activityCsv(userId).url, function(error, csv) {
			  if (error) throw error;

			  var data = d3.nest()
			    .key(function(d) { return d.Date; })
			    .rollup(function(d) { return d[0].Value; })
			    .map(csv);

			  rect.filter(function(d) { return d in data; })
			      .attr("class", function(d) { return "day " + color(data[d]); })
			    .select("title")
			      .text(function(d) { return d + ": " + percent(data[d]); });
			});
		},
		destroy: function() {
		}
	};
};

App.Core.register('ActivityModule', App.Modules.ActivityModule);
