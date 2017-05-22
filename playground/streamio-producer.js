var stream = require('getstream');
// Instantiate a new client (server side)
client = stream.connect('9jtyb249ejzp', 'jnbe82eryq4qvquj4wn5dc8nh85bry33jpbmu84jn58xc3uk4y697xke4rcz9kyk', '24985');
// Instantiate a new client (client side)
//client = stream.connect('9jtyb249ejzp', null, '24985');

var p1 = client.feed('project_aggregated', 'd0bbf3d8-c6da-460e-b7cf-ab99b79e9986');

// Add an activity to the feed
var activity = {
	"actor": "user:79f55e4a-54c8-4642-903b-2ef01e8c24ff",
	"verb": "update",
	"object": "activity:b34e23be-6d8b-4b4b-a974-b529e3ebdbda",
	"foreign_id": "activity:b34e23be-6d8b-4b4b-a974-b529e3ebdbda"
};

// Asynchronous methods return Promise since v3.0.0
p1.addActivity(activity)
	.then(function(data) {
		console.log('Success:', data);
	})
	.catch(function(reason) {
		/* on failure, reason.error contains an explanation */
		console.log('Failure:', reason);
	});