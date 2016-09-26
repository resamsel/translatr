App.Modules.FloatingButtonModule = function(sb) {
	var button = sb.dom.find('.floating-action-btn');

	return {
		create : function() {
			button.pushpin({
				top : button.offset().top,
				offset: 23
			});
		},
		destroy : function() {
		}
	};
};

App.Core.register('FloatingButtonModule', App.Modules.FloatingButtonModule);
