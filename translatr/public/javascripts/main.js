var App = App || {};
App.Modules = App.Modules || {};

$(document).ready(function() {
	// the "href" attribute of .modal-trigger must specify the modal ID that
	// wants to be triggered
	$('.modal-trigger').leanModal();
});