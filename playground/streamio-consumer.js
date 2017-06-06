var stream = require('getstream');
// Instantiate a new client (server side)
client = stream.connect('9jtyb249ejzp', 'jnbe82eryq4qvquj4wn5dc8nh85bry33jpbmu84jn58xc3uk4y697xke4rcz9kyk', '24985');
// Instantiate a new client (client side)
//client = stream.connect('9jtyb249ejzp', null, '24985');

var p1 = client.feed('project_aggregated', 'd0bbf3d8-c6da-460e-b7cf-ab99b79e9986');

// Read 'timeline' for jack - the post by chris will show up:
p1.get({ limit: 10 }).then(function(results) {
	var activityData = results;

	console.log('Activity data:', activityData);
	// Read the next page, using id filtering for optimal performance:
	p1.get({ limit: 10, id_lte: activityData[activityData.length-1].id }).then(function(results) {
		var nextActivityData = results;
		console.log('Activity data:', activityData);
	});
});