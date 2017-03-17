function stripScripts(s) {
	var div = document.createElement('div');
	div.innerHTML = s;
	var scripts = div.getElementsByTagName('script');
	var i = scripts.length;
	while (i--) {
	  scripts[i].parentNode.removeChild(scripts[i]);
	}
	return div.innerHTML;
}

var CodeEditor = Backbone.View.extend({
	el: '#editor-content',
	message: null,

	initialize: function(editor) {
		this.editor = editor;

		this.form = this.$('#form-message');
		this.messageForm = this.$('#form-message');
		this.panelEditor = this.$('#panel-editor');
		this.panelActions = this.$('.filter');
		this.progress = this.form.find('.progress');
		this.fieldId = this.$('#field-id');
		this.fieldLocale = this.$('#field-locale');
		this.fieldKey = this.$('#field-key');
		this.submitButton = this.$('#message-submit');
		this.noSelection = this.$("#no-selection");
		this.rightFilter = $(".item-right .filter");
		this.codeEditor = CodeMirror(this.panelEditor[0], {
			mode: 'xml',
			lineNumbers: true,
			lineWrapping: true,
			styleActiveLine: true,
			htmlMode: true
		});

		this.listenTo(this.editor, 'message:selected', this.onMessageSelected);
		this.listenTo(this.editor, 'message:change', this.onMessageChange);
		this.listenTo(this.editor, 'message:changed', this.onMessageChanged);

		var that = this;
		this.codeEditor.on('change', function() {
			that.editor.trigger('message:changed', that.message);
		});

		this.noSelection.show();
		this.messageForm.hide();
		this.panelEditor.hide();
		this.panelActions.hide();
		this.rightFilter.hide();
	},

	events: {
		'click #message-submit': 'onSave',
		'click #message-discard': 'onDiscard'
	},

	onSave: function() {
		this.editor.onSave();
	},

	onDiscard: function() {
		this.editor.onDiscard();
	},

	onMessageSelected: function(item) {
		if(item === undefined) {
			this.message = null;

			this.noSelection.show();
			this.messageForm.hide();
			this.panelEditor.hide();
			this.panelActions.hide();
			this.rightFilter.hide();

			return;
		}

		this.message = item.getMessage();

		this.noSelection.hide();
		this.messageForm.show();
		this.panelEditor.show();
		this.panelActions.show();
		this.rightFilter.show();

		this.editor.trigger('message:change', this.message.get('value'));
	},

	onMessageChange: function(value) {
		this.codeEditor.setValue(value);
	},

	onMessageChanged: function() {
		this.message.set('value', this.codeEditor.getValue());
	}
});

var Preview = Backbone.View.extend({
	el: '#panel-preview',

	initialize: function(editor) {
		this.editor = editor;
		this.message = null;

		this.listenTo(editor, 'message:selected', this.onMessageSelected);

		this.$el.hide();
	},

	onMessageSelected: function(item) {
		if(this.message !== null) {
			this.stopListening(this.message);
		}
		if(item !== undefined) {
			this.message = item.message;
			this.listenTo(this.message, 'change', this.onMessageChanged);
		} else {
			this.message = null;
		}
		this.onMessageChanged();
	},

	onMessageChanged: function() {
		if(this.message !== null) {
			this.$('#preview').html(stripScripts(this.message.get('value')));
			this.$el.show();
		} else {
			this.$('#preview').html('');
			this.$el.hide();
		}
	}
});

var Editor = Backbone.Model.extend({
	'message': null,

	initialize: function(project, locale, key, search) {
		this.project = project;
		this.locale = locale || {id: null, name: null};
		this.key = key || {id: null, name: null};
		this.search = search;
		this.$el = $('#editor');

		this.localeId = this.locale.id;
		this.localeName = this.locale.name;
		this.keyId = this.key.id;
		this.keyName = this.key.name;

		search.order = search.order || 'name';

		this.listenTo(this, 'message:selected', this.onMessageSelected);
		this.listenTo(this, 'keys:loaded', this.onKeysLoaded);
		$(window).keydown(this.onKeyPress);

		this.undoManager = new Backbone.UndoManager();
		this.codeEditor = new CodeEditor(this);
		this.preview = new Preview(this);
	},

	onMessageSelected: function(item) {
		if(item !== undefined) {
			this.message = item.message;
		} else {
			this.message = null;
		}
	},

	onSave: function() {
		if(this.message.isNew()) {
			this.message.set({
				localeId: this.localeId,
				keyId: this.keyId
			});
		}
		this.message.save();
	},

	onDiscard: function() {
		if(this.message !== null) {
			this.message.restart();
		}
		router.navigate("key/", {trigger: true, replace: true});
	},

	onKeyPress: function(event) {
		if (event.which == 13 && (event.ctrlKey || event.metaKey)) {
			event.preventDefault();
			this.onSave();
		}
	}
});
