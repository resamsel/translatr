var KeyListItemView = Backbone.View.extend({
	tagName: 'a',
	className: 'collection-item avatar waves-effect waves-light key',
	template: _.template($('#key-tmpl').html()),

	initialize: function(arguments) {
		this.editor = arguments.editor;

		this.listenTo(this.model, 'change', this.render);
		this.listenTo(this.model, 'change:message', this.onMessageChanged);
		this.onMessageChanged();
	},

	onMessageChanged: function() {
		this.stopListening(this.message, 'change');
		this.message = this.model.getMessage();
		this.listenTo(this.message, 'change', this.render);
		this.render();
	},

	render: function() {
		var html = this.template(
			_.extend(this.model.toJSON(), {message: this.message})
		);
		this.$el.html(html);
		this.$el
			.attr('id', this.model.id)
			.attr('href', '#key/' + this.model.get('name'))
			.attr('title', this.model.get('name'));
		return this;
	}
});
var KeyListView = Backbone.View.extend({
	el: '#keys-list',
	moreTemplate: _.template($('#more-tmpl').html()),

	initialize: function(editor, search) {
		this.editor = editor;
		this.collection = editor.project.keys;
		this.search = search;

		this.listenTo(this.collection, 'sync', this.render);
		this.listenTo(this.editor, 'keys:loaded', this.onKeysLoaded);

		var collection = this.collection;
		collection.fetch({data: this.search}).then(function() {
			editor.trigger('keys:loaded', collection);
		});
	},

	render: function() {
		var $list = this.$el.empty();
		var collection = this.collection;

		collection.each(function(model) {
			var item = new KeyListItemView({model: model, editor: this.editor});
			$list.append(item.render().$el);
		}, this);

		if(this.collection.hasMore) {
			$list.append(this.moreTemplate({}));
		}
	},

	onKeysLoaded: function(keys) {
		var that = this;
		var messages = this.editor.project.messages;
		messages.fetch({
			data: {
				localeId: this.editor.localeId,
				order: 'key.' + this.search.order
			}
		}).then(function() {
			messages.each(function(msg) {
				var keyId = msg.get('keyId');
				if(keyId in keys._byId) {
					var key = keys._byId[keyId];
					key.setMessage(msg);
				}
			});
			that.trigger('messages:loaded');
		});
	},

	loadMore: function() {
		var editor = this.editor;
		var collection = this.collection;
		collection.fetch({
			update: true,
			remove: false,
			data: _.extend(
				this.search,
				{
					offset: this.collection.length,
					limit: this.search.limit * 2
				}
			)
		}).then(function() {
			editor.trigger('keys:loaded', collection._byId);
		});
	}
});
var MessageListItemView = Backbone.View.extend({
	tagName: 'div',
	className: 'message',
	template: _.template($('#message-tmpl').html()),

	initialize: function(arguments) {
		this.editor = arguments.editor;
	},

	render: function() {
		var html = this.template({
			localeName: this.model.get('localeName'),
			value: stripScripts(this.model.get('value'))
		});
		this.$el.html(html);
		return this;
	},

	events: {
		'click a.btn': 'onCopy'
	},

	onCopy: function() {
		this.editor.trigger('message:change', this.model.get('value'));
	}
});
var MessageListView = Backbone.View.extend({
	el: '#panel-messages',

	initialize: function(editor, search) {
		this.editor = editor;
		this.search = search;

		this.listenTo(this.editor, 'message:selected', this.onMessageSelected);

		this.listView = this.$('.messages');
	},

	render: function() {
		console.log('messagelistview.render');
		var $list = this.listView.empty();
		var collection = this.collection;

		collection.each(function(model) {
			var item = new MessageListItemView({model: model, editor: this.editor});
			$list.append(item.render().$el);
		}, this);

//		if(this.collection.hasMore) {
//			$list.append(this.moreTemplate({}));
//		}
	},

	onMessageSelected: function(item) {
		if(item === undefined) {
			this.listView.hide();

			if(this.collection !== null) {
				this.stopListening(this.collection, 'sync');
				this.collection = null;
			}

			return;
		}

		this.listView.show();

		this.collection = new MessageList(this.editor.project.id);
		this.listenTo(this.collection, 'sync', this.render);

		var editor = this.editor;
		var collection = this.collection;
		collection.fetch({data: {keyName: item.get('name')}});
	}

});
var LocaleSelectorListItemView = Backbone.View.extend({
	tagName: 'li',
	template: _.template($('#locale-tmpl').html()),

	initialize: function(arguments) {
		this.editor = arguments.editor;

		this.listenTo(this.editor, 'message:selected', this.onMessageSelected);
	},

	render: function() {
		var keyName = '';
		if(this.message !== undefined) {
			keyName = this.message.get('name');
		}

		var html = this.template({
			id: this.model.id,
			url: jsRoutes.controllers.Locales.locale(this.model.id).url,
			localeName: this.model.get('name'),
			keyName: keyName
		});
		this.$el.html(html);
		return this;
	},

	onMessageSelected: function(item) {
		this.message = item;
		this.render();
	}
});
var LocaleSelectorListView = Backbone.View.extend({
	el: '#dropdown-locales',

	initialize: function(editor) {
		this.editor = editor;
		this.collection = editor.project.locales;

		this.listenTo(this.collection, 'sync', this.render);

		var collection = this.collection;
		this.collection.fetch({data:{order:'name'}});
	},

	render: function() {
		var $list = this.$el.empty();
		var collection = this.collection;
		collection.each(function(model) {
			var item = new LocaleSelectorListItemView({model: model, editor: this.editor});
			$list.append(item.render().$el);
		}, this);
	}
});
var EditorSwitchView = Backbone.View.extend({
	el: '#switch-editor',

	initialize: function(editor) {
		this.editor = editor;

		this.listenTo(this.editor, 'message:selected', this.onMessageSelected);
	},

	onMessageSelected: function(item) {
		if(item === undefined) {
			this.$el.attr('href', '#');
			this.$el.addClass('disabled');

			return;
		}

		this.$el.attr('href', jsRoutes.controllers.Keys.key(item.id).url + '#locale=' + this.editor.localeName);
		this.$el.removeClass('disabled');
	}
});

var LocaleEditor = Editor.extend({
	initialize: function(project, localeId, keyId, search) {
		Editor.prototype.initialize.apply(this, arguments);

		this.keyList = new KeyListView(this, this.search);
		this.messageList = new MessageListView(this, this.search);
		this.localeSelector = new LocaleSelectorListView(this);
		this.editorSwitch = new EditorSwitchView(this);

		this.listenTo(this, 'messages:loaded', this.onMessagesLoaded);

		var that = this;
		router.on('route:key', function(keyName) {
			if(keyName === '') {
				keyName = null;
			}
			that.selectedItemName = keyName;
			that.trigger('message:selected', that.selectedItem(keyName));
		});
	},

	selectedItem: function(selectedItemName) {
		if(selectedItemName !== null) {
			return this.keyList.collection.find(function(item) {
				return item.get('name') == selectedItemName;
			});
		}
		return undefined;
	},

	onMessagesLoaded: function() {
		this.trigger('message:selected', this.selectedItem(this.selectedItemName));
	},

	onMessageSelected: function(model) {
		Editor.prototype.onMessageSelected.apply(this, arguments);

		this.keyList.$el.find('.active').removeClass('active');
		if(model !== undefined) {
			this.keyId = model.id;
			this.keyName = model.get('name');
			this.keyList.$el.find('#' + model.id).addClass('active');
		} else {
			this.keyId = null;
			this.keyName = null;
		}
	}
});

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
App.Core.register('LocaleHashModule', App.Modules.LocaleHashModule);
