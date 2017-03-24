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

var ItemListItemView = Backbone.View.extend({
	tagName: 'a',
	className: 'collection-item avatar waves-effect waves-light',

	initialize: function(arguments) {
		this.type = arguments.type;

		this.template = _.template($('#item-tmpl').html());

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
		this.$el.addClass(this.type);
		this.$el
			.attr('id', this.model.id)
			.attr('href', '#' + this.type + '/' + this.model.get('name'))
			.attr('title', this.model.get('name'));
		return this;
	}
});

var ItemListView = Backbone.View.extend({
	el: '#items-list',
	itemsEmpty: '#items-empty',

	initialize: function(project, collection, search, styleClass, itemType, queryParams) {
		this.project = project;
		this.collection = collection;
		this.search = search;
		this.itemType = itemType;
		this.queryParams = queryParams;

		this.moreTemplate = _.template($('#more-tmpl').html());
		this.noItems = $('#items-empty');

		this.listenTo(this.collection, 'sync', this.render);
		this.listenTo(Backbone, 'items:loaded', this.onItemsLoaded);

		this.$el.addClass(styleClass);

		var collection = this.collection;
		collection.fetch({data: this.search}).then(function() {
			Backbone.trigger('items:loaded', collection);
		});
	},

	events: {
		"scroll": "onScroll"
	},

	onScroll: function() {
		//console.log('onScroll', this.$el.scrollTop(), this.$el[0].scrollHeight - this.$el.parent().height(), this.$el[0].scrollHeight, this.$el.parent().height());
		if(this.$el.scrollTop() == this.$el[0].scrollHeight - this.$el.height()) {
			this.loadMore();
		}
	},

	render: function() {
		var $list = this.$el.empty();
		var collection = this.collection;

		if(collection.size() == 0) {
			this.noItems.show();
		} else {
			this.noItems.hide();

			collection.each(function(model) {
				var item = new ItemListItemView({
					model: model,
					type: this.itemType
				});
				$list.append(item.render().$el);
			}, this);
		}

		if(this.collection.hasMore) {
			var template = this.moreTemplate({});
			$list.append(template);
			$(template).hide();
		}
	},

	onItemsLoaded: function(items) {
		var that = this;
		var messages = this.project.messages;
		var data = _.extend({
			order: this.itemType + '.' + this.search.order
		}, this.data);
		messages.fetch({data: data}).then(function() {
			messages.each(function(msg) {
				var id = msg.get(that.itemType + 'Id');
				if(id in items._byId) {
					items._byId[id].setMessage(msg);
				}
			});
			Backbone.trigger('messages:loaded');
		});
	},

	loadMore: function() {
		if(!this.collection.hasMore) {
			console.log('No more items on server');
			return;
		}
		var collection = this.collection;
		this.$('.preloader-container').show();
		this.$el.animate({
			scrollTop: this.$el[0].scrollHeight - this.$el.height()
		});
		collection.fetch({
			update: true,
			remove: false,
			data: _.extend(
				this.search,
				{
					offset: collection.length,
					limit: this.search.limit * 2
				}
			)
		}).then(function() {
			setTimeout(function() {
				Backbone.trigger('items:loaded', collection);
			}, 2000);
		});
	}
});

var CodeEditor = Backbone.View.extend({
	el: '#editor-content',

	initialize: function() {
		this.panelEditor = this.$('#panel-editor');
		this.panelActions = this.$('.filter');
		this.noSelection = this.$("#no-selection");
		this.rightFilter = $(".item-right .filter");
		this.codeEditor = CodeMirror(this.panelEditor[0], {
			mode: 'xml',
			lineNumbers: true,
			lineWrapping: true,
			styleActiveLine: true,
			htmlMode: true
		});

		this.listenTo(Backbone, 'item:selected', this.onItemSelected);
		this.listenTo(Backbone, 'message:change', this.onMessageChange);

		var that = this;
		this.codeEditor.on('change', function() {
			Backbone.trigger('message:changed', that.codeEditor.getValue());
		});

		this.noSelection.show();
		this.panelEditor.hide();
		this.panelActions.hide();
		this.rightFilter.hide();
	},

	events: {
		'click #message-submit': 'onSave',
		'click #message-discard': 'onDiscard'
	},

	onSave: function() {
		Backbone.trigger('message:save');
	},

	onDiscard: function() {
		Backbone.trigger('message:discard');
		this.codeEditor.clearHistory();
		this.codeEditor.setValue('');
	},

	onItemSelected: function(item) {
		this.codeEditor.clearHistory();

		if(item === undefined) {
			this.noSelection.show();
			this.panelEditor.hide();
			this.panelActions.hide();
			this.rightFilter.hide();
		} else {
			this.noSelection.hide();
			this.panelEditor.show();
			this.panelActions.show();
			this.rightFilter.show();
		}
	},

	onMessageChange: function(value) {
		console.log('onMessageChange "' + value + '"', value);
		this.codeEditor.setValue(value);
	}
});

var Preview = Backbone.View.extend({
	el: '#panel-preview',

	initialize: function() {
		this.message = null;

		this.listenTo(Backbone, 'item:selected', this.onItemSelected);

		this.$el.hide();
	},

	onItemSelected: function(item) {
		if(this.message !== null) {
			this.stopListening(this.message);
		}
		if(item !== undefined) {
			this.message = item.message;
			this.listenTo(this.message, 'change', this.render);
		} else {
			this.message = null;
		}
		this.render();
	},

	render: function() {
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
	message: null,
	app: null,
	project: null,
	locale: {id: null, name: null},
	key: {id: null, name: null},
	search: null,

	initialize: function() {
		this.app = this.get('app') || this.app;
		this.project = this.get('project') || this.project;
		this.locale = this.get('locale') || this.locale;
		this.key = this.get('key') || this.key;
		this.search = this.get('search') || this.search;

		this.$el = $('#editor');

		this.localeId = this.locale.id;
		this.localeName = this.locale.name;
		this.keyId = this.key.id;
		this.keyName = this.key.name;

		this.search.order = this.search.order || 'name';

		this.listenTo(Backbone, 'item:selected', this.onItemSelected);
		this.listenTo(Backbone, 'messages:loaded', this.onMessagesLoaded);
		this.listenTo(Backbone, 'message:changed', this.onMessageChanged);
		this.listenTo(Backbone, 'message:save', this.onSave);
		this.listenTo(Backbone, 'message:discard', this.onDiscard);

		$(window).keydown(this.onKeyPress);

		var that = this;
		this.app.router.on('route:locale', function(arg) { return that.onRouteChanged(arg); });
		this.app.router.on('route:key', function(arg) { return that.onRouteChanged(arg); });

		this.codeEditor = new CodeEditor;
		this.preview = new Preview;
	},

	selectedItem: function(itemName) {
		return undefined;
	},

	onRouteChanged: function(itemName) {
		console.log('onRouteChanged: ', itemName);
		if(itemName === '') {
			itemName = null;
		}
		this.selectedItemName = itemName;
		Backbone.trigger('item:selected', this.selectedItem(itemName));
	},

	onItemSelected: function(item) {
		console.log('onItemSelected', item);
		if(item !== undefined) {
			this.item = item;
			this.message = item.getMessage();
			Backbone.trigger('message:change', this.message.get('value'));
		} else {
			this.item = null;
			this.message = null;
		}
	},

	onMessagesLoaded: function() {
		console.log('Editor.onMessagesLoaded');
		Backbone.trigger('item:selected', this.selectedItem(this.selectedItemName));
	},

	onMessageChanged: function(value) {
		if(this.message !== null) {
			this.message.set('value', value);
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
		this.app.router.navigate(this.itemType + "/", {trigger: true, replace: true});
	},

	onKeyPress: function(event) {
		if (event.which == 13 && (event.ctrlKey || event.metaKey)) {
			event.preventDefault();
			this.onSave();
		}
	}
});
