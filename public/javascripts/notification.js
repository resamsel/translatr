var NotificationListItemView = Backbone.View.extend({
	tagName: 'li',
	className: 'collection-item avatar',
	template: null,

	initialize: function(collection) {
		this.template = _.template($('#notification-tmpl').html());
	},

	render: function() {
		var html = this.template({
			contentType: this.model.get('contentType'),
			icon: this.model.get('icon'),
			color: this.model.get('color'),
			link: this.model.get('link'),
			title: this.model.get('title'),
			subtitle: this.model.get('subtitle')
		});
		this.$el.html(html);
		return this;
	}
});
var NotificationListView = Backbone.View.extend({
	el: '#dropdown-notifications',

	initialize: function(collection) {
		this.collection = collection;

		this.noNotifications = $('#dropdown-notifications .no-notifications');

		this.listenTo(this.collection, 'sync', this.render);

		this.collection.fetch();
	},

	render: function() {
		var $list = this.$el;
		var collection = this.collection;

		if(collection.size() == 0) {
			this.noNotifications.show();
		} else {
			this.noNotifications.hide();
			collection.each(function(model) {
				$list.find('.collection-footer').before(new NotificationListItemView({ model: model }).render().$el);
			}, this);
		}
	}
});
