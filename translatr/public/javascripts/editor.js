App.Modules.EditorModule = function(sb, options) {
	var options = options || {};
	var messages = options.messages || {};

	var win = sb.dom.wrap(window);
	var form = sb.dom.find('#form-message');
	var message = sb.dom.find('#form-message');
	var panelPreview = sb.dom.find('#panel-preview');
	var panelEditor = sb.dom.find('#panel-editor');
	var panelActions = sb.dom.find('.item-main .filter');
	var preview = sb.dom.find('#preview');
	var progress = form.find('.progress');
	var fieldId = sb.dom.find('#field-id');
	var fieldLocale = sb.dom.find('#field-locale');
	var fieldKey = sb.dom.find('#field-key');
    var submitButton = sb.dom.find('#message-submit');
    var cancelButton = sb.dom.find('#message-cancel');
    var noSelection = sb.dom.find("#no-selection");
    var rightFilter = sb.dom.find(".item-right .filter");
	var codeEditor;

	function _handleKeyPress(event) {
		if (event.which == 13 && (event.ctrlKey || event.metaKey)) {
			event.preventDefault();
			form.submit();
	    }
	}

	function _handleSaveMessage(message) {
		progress.css('visibility', 'hidden');
		sb.dom.find('#' + message.keyId).removeClass('no-message');
		sb.dom.find('#' + message.keyId + ' .value').text(message.value);
		sb.publish('itemSelected', [message]);
		Materialize.toast(messages['message.updated'], 5000);
	}

	function _handleMessage(paged) {
    	if(paged.list.length === 0)
    		return;

    	var msg = paged.list[0];
	    fieldId.val(msg.id);
	    fieldKey.val(msg.keyName).attr('keyId', msg.keyId);
	    fieldLocale.val(msg.localeName).attr('localeId', msg.localeId);
	    codeEditor.setValue(msg.value);
	    preview.html(msg.value);
	}

	function _handleItemSelected(item) {
		if(item === null) {
			sb.dom.find('.item-left .collection>a').removeClass('active');

			noSelection.show();
			message.hide();
			panelPreview.hide();
			panelEditor.hide();
			panelActions.hide();
			rightFilter.hide();

			return;
		}

		var keyId = item.keyId;
		var keyName = item.keyName;
		var localeId = item.localeId;
		var localeName = item.localeName;

		noSelection.hide();
		message.show();
		panelPreview.show();
		panelEditor.show();
		panelActions.show();
		rightFilter.show();

	    fieldId.val('');
		fieldLocale.val(localeName).attr('localeId', localeId);
		fieldKey.val(keyName).attr('keyId', keyId);
	    codeEditor.setValue('');
	    preview.html('');

	    sb.utilities.ajax(sb.utilities.merge(
			jsRoutes.controllers.TranslationsApi.find(options.projectId),
			{data: {"localeId": localeId, "keyName": keyName}}
		)).done(_handleMessage);
	}

	function _handleItemsChanged() {
		var $items = sb.dom.find('.item-left .collection>a');
		$items.click(function() {
			var $this = sb.dom.wrap(this);
			$items.removeClass('active');
			$this.addClass('active');
			sb.publish('itemSelected', [options.itemToEvent(sb, $this)]);
		})
	}

	function _handleMessageChanged() {
		preview.html(codeEditor.getValue());
	}

	function _handleSubmit(e){
        e.preventDefault();
        progress.css('visibility', 'visible');

        var data = {
        		"localeId": fieldLocale.attr('localeId'),
        		"keyId": fieldKey.attr('keyId'),
        		"value": codeEditor.getValue()
        };

        var op;
        if(fieldId.val() !== '') {
        	op = jsRoutes.controllers.TranslationsApi.update();
        	data["id"] = fieldId.val();
        } else {
        	op = jsRoutes.controllers.TranslationsApi.create();
        }

    	sb.utilities.ajax(sb.utilities.merge(
			op,
			{
				contentType: 'application/json',
				dataType: 'json',
				data: JSON.stringify(data)
			}
		)).done(_handleSaveMessage);
    }

	function _handleCancelation() {
		sb.publish('itemSelected', [null]);
		window.location.hash = '#';
	}

	return {
		create: function() {
			if(typeof options.projectId === 'undefined') {
				return;
			}

			codeEditor = CodeMirror(panelEditor[0], {
				mode: 'xml',
				lineNumbers: true,
				lineWrapping: true,
				styleActiveLine: true,
				htmlMode: true
			});

			sb.subscribe('itemsChanged', _handleItemsChanged);
			sb.subscribe('itemSelected', _handleItemSelected);

			message.hide();
			panelPreview.hide();
			panelEditor.hide();
			panelActions.hide();
			rightFilter.hide();

			form.submit(_handleSubmit);
			submitButton.click(_handleSubmit);
			win.keydown(_handleKeyPress);
			codeEditor.on('change', _handleMessageChanged);
			cancelButton.click(_handleCancelation);

			_handleItemsChanged();
		},
		destroy: function() {
		}
	};
};