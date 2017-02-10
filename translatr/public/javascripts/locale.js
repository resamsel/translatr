App.Modules.KeyCreateModule = function(sb) {
	var form = sb.dom.find('#form-key');
	var keyName = sb.dom.find('#field-key-name');
	var saveButton = sb.dom.find('.modal .btn-save');

	return {
		create : function() {
			saveButton.click(function() {
				form.attr('action', form.attr('action') + '#key=' + keyName.val());
				form.submit();
			});
		},
		destroy : function() {
		}
	};
};

App.Modules.EditorMessageListModule = function(sb, options) {
	var options = options || {};
	var projectId = options.projectId || '';
	var locales = options.locales || {};

	var panelMessages = sb.dom.find('#panel-messages .messages');
	var fieldValue = sb.dom.find('#field-value');
	var panelPreview = sb.dom.find('#panel-preview');
	var itemRight = sb.dom.find('.item-right');
	var preview = sb.dom.find('#preview');
	var dropdownButton = sb.dom.find('.item-right .filter .dropdown-button');
	var dropdownPreview = sb.dom.find('#dropdown-preview');
	var template = panelMessages.find('.template');

	function _handleCopyMessageValue(e) {
		e.preventDefault();
		var value = sb.dom.wrap(this).data('value');
		fieldValue.val(value).trigger('autoresize');
		Materialize.updateTextFields();
		sb.publish('valueChanged', value);
		preview.html(value);
	}
	
	function _handleShowMessageValue(e) {
		e.preventDefault();
		var entry = sb.dom.wrap(this).data('message');
		//sb.publish('valueChanged', entry.value);
		preview.html(entry.value);
		dropdownButton.find('span').html(locales[entry.localeId]);
	}

	function _handleMessageList(keyName, paged) {
		panelMessages.find('.message:not(.template)').remove();
		paged.list.forEach(function(entry) {
			var $msg = template.clone().removeClass('template');
			var $a = $msg.find('a');
			$a.attr('href', window.location.hash)
				.data('value', entry.value)
	    		.click(_handleCopyMessageValue);
	    	$msg.find('.localeName').html(locales[entry.localeId]);
	    	$msg.find('.value').html(entry.value);
	    	panelMessages.append($msg);
		});
	}

	function _handleItemSelected(item) {
		if(item === null) {
			panelMessages.hide();
			return;
		}

		panelMessages.show();

		var keyId = item.keyId;
		var keyName = item.keyName;

		sb.utilities.ajax(sb.utilities.merge(
			jsRoutes.controllers.TranslationsApi.find(projectId),
			{data: {keyName: keyName}}
		)).done(function(data) {
			_handleMessageList(keyName, data);
		});
	}

	return {
		create: function() {
			sb.subscribe('itemSelected', _handleItemSelected);
		},
		destroy: function() {
		}
	};
};

App.Modules.EditorLocaleSelectorModule = function(sb, options) {
	var dropdownLinks = sb.dom.find('#dropdown-locales a');

	function _handleItemSelected(item) {
		if(item === null) {
			dropdownLinks.each(function() {
				$(this).attr('href', jsRoutes.controllers.Locales.locale($(this).attr('id')).url);
			});

			return;
		}

		dropdownLinks.each(function() {
			$(this).attr('href', jsRoutes.controllers.Locales.locale($(this).attr('id')).url + '#key=' + item.keyName);
		});
	}

	return {
		create: function() {
			sb.subscribe('itemSelected', _handleItemSelected);
		},
		destroy: function() {
		}
	}
}

App.Modules.EditorSwitchModule = function(sb, options) {
	var localeName = options.localeName || '';
	var switchButton = sb.dom.find('#switch-editor');

	function _handleItemSelected(item) {
		if(item === null) {
			switchButton.attr('href', '#');
			switchButton.addClass('disabled');

			return;
		}

		switchButton.attr('href', jsRoutes.controllers.Keys.key(item.keyId).url + '#locale=' + localeName);
		switchButton.removeClass('disabled');
	}

	return {
		create: function() {
			sb.subscribe('itemSelected', _handleItemSelected);
		},
		destroy: function() {
		}
	}
}

App.Modules.SuggestionModule = function(sb) {
	var form = sb.dom.find('#form-search');

	function _handleSuggestionSelected(suggestion) {
		if(suggestion.data.type == 'key' && suggestion.data.name != '+++') {
			form.submit();
		} else {
			window.location.href = suggestion.data.url;
		}
	}

	return {
		create: function() {
			sb.subscribe('suggestionSelected', _handleSuggestionSelected);
		},
		destroy: function() {
		}
	};
};

App.Modules.LocaleHashModule = function(sb) {
	var params = $.deparam.fragment();
	var location = window.location;
	var doc = sb.dom.wrap(document);
	var keys = sb.dom.find('.collection.keys');

	function _initFromHash() {
		if('key' in params) {
			var $a = sb.dom.find('a.key[keyName="'+params.key+'"]');
			keys.animate(
				{ scrollTop: _scrollTopOffset(keys, $a) },
				'500'
			);
			$a.click();
		} else {
			var $key = sb.dom.find('.keys .collection-item:first-child a.key');
			params.key = $key.attr('keyName');
			$key.click();
		}
	}
	
	function _scrollTopOffset(container, item) {
		return - container.position().top - container.css('margin-top').replace('px', '') + item.parent().offset() ? item.parent().offset().top : 0
	}

	return {
		create : function() {
			doc.ready(_initFromHash);
		},
		destroy : function() {
		}
	};
};

App.Core.register('SuggestionModule', App.Modules.SuggestionModule);
App.Core.register('ProjectSearchModule', App.Modules.ProjectSearchModule);
App.Core.register('KeyCreateModule', App.Modules.KeyCreateModule);
App.Core.register('LocaleHashModule', App.Modules.LocaleHashModule);
