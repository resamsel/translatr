var EditorSwitchView = Backbone.View.extend({
	el: '#switch-editor',

	initialize: function(keyName) {
		this.keyName = keyName;

		this.listenTo(Backbone, 'item:selected', this.onItemSelected);
	},

	onItemSelected: function(item) {
		if(item === undefined) {
			this.$el.attr('href', '#');
			this.$el.addClass('disabled');

			return;
		}

		this.$el.attr(
			'href',
			jsRoutes.controllers.Locales.localeBy(
				item.get('projectOwnerUsername'),
				item.get('projectPath'),
				item.get('name')
			).url + '#key/' + this.keyName
		);
		this.$el.removeClass('disabled');
	}
});

var KeyEditor = Editor.extend({
	initialize: function() {
		Editor.prototype.initialize.apply(this, arguments);

		this.itemType = 'locale';
		this.itemList = new ItemListView(
			this.project,
			this.project.locales,
			_.extend(this.search, { 'messages.keyName': this.keyName }),
			'locales',
			this.itemType,
			this.keyName
		);
		this.editorSwitch = new EditorSwitchView(this.key.name);

		this.listenTo(Backbone, 'messages:loaded', this.onMessagesLoaded);
	},

	selectedItem: function(itemName) {
		if(itemName !== null) {
			return this.project.locales.find(function(item) {
				return item.get('name') == itemName;
			});
		}
		return undefined;
	},

	onItemSelected: function(model) {
		Editor.prototype.onItemSelected.apply(this, arguments);

		this.itemList.$el.find('.active').removeClass('active');
		if(model !== undefined) {
			this.localeId = model.id;
			this.localeName = model.get('name');
			this.itemList.$el.find('#' + model.id).addClass('active');
		} else {
			this.localeId = null;
			this.localeName = null;
		}
	}
});

/*
 * Input:
 * 
 * * projectId: Project.id
 * * keyId: Key.id
 * * keyName: Key.name
 * * locales: List of Locale
 */

App.Modules.KeyHashModule = function(sb) {
	var params = $.deparam.fragment();
	var doc = sb.dom.wrap(document);

	function _initFromHash() {
		if('locale' in params) {
			var $a = sb.dom.find('a.locale[localeName="'+params.locale+'"]');
			$a.click();
		} else {
			var $locale = sb.dom.find('.locales .collection-item.locale:first-child');
			$locale[0].click();
		}
	}

	return {
		create: function() {
			doc.ready(_initFromHash);
		},
		destroy: function() {
		}
	};
}
