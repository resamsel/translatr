App.Modules.AccessTokenCreateModule = function(sb) {
	var select = sb.dom.find('select');
	var form = sb.dom.find('#form-access-token');
	var saveButton = sb.dom.find('.btn-save');

	return {
		create : function() {
			saveButton.click(function() {
				form.submit();
			});
		},
		destroy : function() {
		}
	};
};

App.Core.register('AccessTokenCreateModule', App.Modules.AccessTokenCreateModule);
