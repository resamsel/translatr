/*
  Class, version 2.7
  Copyright (c) 2006, 2007, 2008, Alex Arnell <alex@twologic.com>
  Licensed under the new BSD License. See end of file for full license terms.
*/

var Class = (function() {
  var __extending = {};

  return {
    extend: function(parent, def) {
      if (arguments.length === 1) { def = parent; parent = null; }
      var func = function() {
        if (arguments[0] ===  __extending) { return; }
        this.initialize.apply(this, arguments);
      };
      if (typeof(parent) === 'function') {
        func.prototype = new parent( __extending);
      }
      var mixins = [];
      if (def && def.include) {
        if (def.include.reverse) {
          // methods defined in later mixins should override prior
          mixins = mixins.concat(def.include.reverse());
        } else {
          mixins.push(def.include);
        }
        delete def.include; // clean syntax sugar
      }
      if (def) Class.inherit(func.prototype, def);
      for (var i = 0; (mixin = mixins[i]); i++) {
        Class.mixin(func.prototype, mixin);
      }
      return func;
    },
    mixin: function (dest, src, clobber) {
      clobber = clobber || false;
      if (typeof(src) != 'undefined' && src !== null) {
        for (var prop in src) {
          if (clobber || (!dest[prop] && typeof(src[prop]) === 'function')) {
            dest[prop] = src[prop];
          }
        }
      }
      return dest;
    },
    inherit: function(dest, src, fname) {
      if (arguments.length === 3) {
        var ancestor = dest[fname], descendent = src[fname], method = descendent;
        descendent = function() {
          var ref = this.parent; this.parent = ancestor;
          var result = method.apply(this, arguments);
          if(ref) {
        	  this.parent = ref;
          } else {
        	  delete this.parent;
          }
          return result;
        };
        // mask the underlying method
        descendent.valueOf = function() { return method; };
        descendent.toString = function() { return method.toString(); };
        dest[fname] = descendent;
      } else {
        for (var prop in src) {
          if (dest[prop] && typeof(src[prop]) === 'function') {
            Class.inherit(dest, src, prop);
          } else {
            dest[prop] = src[prop];
          }
        }
      }
      return dest;
    },
    singleton: function() {
      var args = arguments;
      if (args.length === 2 && args[0].getInstance) {
        var klass = args[0].getInstance(__extending);
        // we're extending a singleton swap it out for it's class
        if (klass) { args[0] = klass; }
      }

      return (function(args){
        // store instance and class in private variables
        var instance = false;
        var klass = Class.extend.apply(args.callee, args);
        return {
          getInstance: function () {
            if (arguments[0] === __extending) return klass;
            if (instance) return instance;
            return (instance = new klass());
          }
        };
      })(args);
    }
  };
})();

// finally remap Class.create for backward compatability with prototype
Class.create = function() {
  return Class.extend.apply(this, arguments);
};

/*
  Redistribution and use in source and binary forms, with or without modification, are
  permitted provided that the following conditions are met:

  * Redistributions of source code must retain the above copyright notice, this list
    of conditions and the following disclaimer.
  * Redistributions in binary form must reproduce the above copyright notice, this
    list of conditions and the following disclaimer in the documentation and/or other
    materials provided with the distribution.
  * Neither the name of typicalnoise.com nor the names of its contributors may be
    used to endorse or promote products derived from this software without specific prior
    written permission.

  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY
  EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
  MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL
  THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
  SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT
  OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
  HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR
  TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

var App = {};

/* 
* CORE-SANDBOX-MODULE Pattern implementation
* see readme.md for references.
*/

/*
* @description Sandbox object, the API of this object is available to modules.
*              see readme.md for reference.
*/

App.Sandbox = Class.extend({
	initialize: function(core) {
		this.publish = core.publish;
		this.subscribe = core.subscribe;
		this.dom = core.dom;
		this.utilities = core.utilities;
	}
});


/*
* @description Core object, the API of this object is available to sandboxes.
*              see readme.md for reference.
* @static
* @param {object} base: base library (jquery is used here).
*/

App.Core = function(_$) {
	var moduleData = {},
		cache = {},
		_dom = {
			find: function(selector) {
				return _$(selector);
			},
			wrap: function(element) {
				return _$(element);
			}
		},
		_utilities = {
			merge: _$.extend,
			grep: _$.grep,
			inArray: _$.inArray,
			each: _$.each,
			ajax: _$.ajax
		};
	var dispatcher = _.clone(Backbone.Events)

	return {
		dom: _dom,
		utilities: _utilities,
		register: function(moduleId, creator, options) {
			moduleData[moduleId] = {
				creator: creator,
				instance: null,
				options: options || {}
			};
		},
		/**
		 * Starts a single module
		 * @param {string} moduleId The module identifier
		 */
		start: function(moduleId) {
			console.log("Starting " + moduleId);
			moduleData[moduleId].instance = new moduleData[moduleId].creator(new App.Sandbox(this), moduleData[moduleId].options);
		    moduleData[moduleId].instance.create();
        },
		stop: function(moduleId) {
			var data = moduleData[moduleId];
			if (data.instance) {
				data.instance.destroy();
				data.instance = null;
			}
		},
		startAll: function() {
			for (var moduleId in moduleData) {
				if (moduleData.hasOwnProperty(moduleId)) {
					this.start(moduleId);
				}
			}
		},
		stopAll: function() {
			for (var moduleId in moduleData) {
				if (moduleData.hasOwnProperty(moduleId)) {
					this.stop(moduleId);
				}
			}
		},
		publish: function(event, args) {
			console.log('publish: ' + event);
			dispatcher.trigger(event, args);
			console.log('/publish: ' + event);
		},
		subscribe: function(message, callback) {
			dispatcher.on(message, callback);
		},
		unsubscribe: function(handle) {
			var t = handle[0];
			base.each(cache[t], function(idx) {
				if (this === handle[1]) {
					cache[t].splice(idx, 1);
				}
			});
		}
	};
} (jQuery);

function toQueryString(parameters) {
	var queryString = _.reduce(
		parameters,
		function(components, value, key) {
			if(value != null) {
				components.push(key + '=' + encodeURIComponent(value));
			}
			return components;
		},
		[]
	).join('&');
	if(queryString.length > 0) {
		queryString = '?' + queryString;
	}
	return queryString;
}

var AppRouter = Backbone.Router.extend({
	initialize: function(app) {
		this.app = app;
	},

	routes: {
		'key/*keyName': 'key',
		'locale/*localeName': 'locale'
	}
});
var Translatr = Backbone.Model.extend({
	initialize: function() {
		this.router = new AppRouter(this);

		this.listenTo(Backbone, 'all', this.onAny);
	},

	onAny: function() {
		console.log('Translatr.onAny', arguments);
	}
});
var app = new Translatr();

var Model = Backbone.Model.extend({
	initialize: function() {
		this.undoManager = new Backbone.UndoManager;
		this.undoManager.register(this);
		this.undoManager.startTracking();
	},
	restart: function() {
		this.undoManager.undoAll();
		this.changed = null;
		this.trigger('change', this);
	},
	request: function(method, model, options) {
		return {
			url: '/api/',
			type: 'GET'
		}
	},
	sync: function(method, model, options) {
		console.log('message.sync', arguments);
		var r = this.request(method, model, options);

		var that = this;
		return $.ajax({
			url: r.url,
			type: r.type,
			contentType: 'application/json',
			dataType: 'json',
			data: JSON.stringify(this.toJSON()),
			success: function(data) {
				options.success(arguments);
				if(method == 'create') {
					model.set('id', data.id);
				}
				that.undoManager.clear();
				that.restart();
				Materialize.toast(messages['message.updated'], 5000);
			},
			error: options.error
		});
	}
});
var WithMessage = Model.extend({
	message: null,

	initialize: function() {
		this.setMessage({value: ''});
	},

	getMessage: function() {
		return this.message;
	},

	setMessage: function(message) {
		console.log('setMessage', message);
		if(this.message !== null) {
			this.message.undoManager.stopTracking();
		}
		if(message) {
			this.message = new Message(message);
		}
		this.trigger('change:message', this.message);
	}
});
var Locale = WithMessage.extend({});
var Key = WithMessage.extend({});
var Message = Model.extend({
	request: function(method, model, options) {
		switch(method) {
		case 'create':
			return jsRoutes.controllers.TranslationsApi.create();
		case 'update':
			return jsRoutes.controllers.TranslationsApi.update();
		}
		return null;
	},
});
var PagedCollection = Backbone.Collection.extend({
	parse: function(data) {
		this.hasMore = data.hasNext;
		return data.list;
	}
});
var LocaleList = PagedCollection.extend({
	model: Locale,

	initialize: function(projectId) {
		this.url = jsRoutes.controllers.LocalesApi.find(projectId).url;
	}
});
var KeyList = PagedCollection.extend({
	model: Key,

	initialize: function(projectId) {
		this.url = jsRoutes.controllers.KeysApi.find(projectId).url;
	}
});
var MessageList = PagedCollection.extend({
	model: Message,

	initialize: function(projectId) {
		this.url = jsRoutes.controllers.TranslationsApi.find(projectId).url;
	}
});
var Project = Backbone.Model.extend({
	initialize: function(id) {
		this.id = id;
		this.keys = new KeyList(id);
		this.locales = new LocaleList(id);
		this.messages = new MessageList(id);
	}
});

$(document).ready(function() {
	Backbone.history.start();
});