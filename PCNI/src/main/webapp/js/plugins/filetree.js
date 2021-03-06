(function(factory) {
  if (typeof define === 'function' && define.amd) {
    define(['jquery'], factory);
  } else {
    factory(jQuery);
  }
})(function($) {

  /*
      Utility functions
   */
  var FileTree, Plugin, defaults, map, old;
  $.fn.check = function() {
    return this.each(function() {
      return $(this).prop('indeterminate', false).prop('checked', true);
    });
  };
  $.fn.uncheck = function() {
    return this.each(function() {
      return $(this).prop('indeterminate', false).prop('checked', false);
    });
  };
  $.fn.semicheck = function() {
    return this.each(function() {
      return $(this).prop('indeterminate', true).prop('checked', false);
    });
  };
  $.fn.togglecheck = function() {
    return this.each(function() {
      var e;
      e = $(this);
      return e.prop('checked', !e.prop('checked'));
    });
  };
  defaults = {
    data: [],
    animationSpeed: 400,
    folderTrigger: "click",
    multiselect: false,
    hierarchy: true,
    hideFiles: false,
    fileContainer: null,
    fileNodeName: 'name',
    folderNodeName: 'name',
    fileNodeTitle: 'name',
    folderNodeTitle: 'name',
    nodeFormatter: function(node) {
      return node;
    },
    ajax: false,
    url: "./",
    requestSettings: {},
    responseHandler: function(data) {
      return data;
    }
  };
  map = Array.prototype.map;

  /*
      FILETREE CLASS DEFINITION
   */
  FileTree = (function() {
    function FileTree(element, options) {
      this.element = element;
      this.settings = $.extend({}, defaults, options);
      this._defaults = defaults;
      this.init();
    }

    FileTree.prototype.init = function() {
      var $root, data, self;
      $root = this._getRootElement($(this.element));
      data = this.settings.data;
      self = this;
      if (this.settings.ajax === true) {
        $.ajax(this.settings.url, this.settings.requestSettings).then(function(data) {
          data = self.settings.responseHandler(data);
          return self._createTree.call(self, $root, data);
        });
      } else if ($.isArray(data) && data.length > 0) {
        this._createTree.call(this, $root, data);
      } else {
        this._parseTree.call(this, $root);
      }
      this._addListeners();
      this._data = $.makeArray($root.find('li').map(function(k, v) {
        return $(v).text().toLowerCase();
      }));
      data = null;
      return $root;
    };

    FileTree.prototype.open = function(elem) {
      return this._openFolder(elem);
    };

    FileTree.prototype.close = function(elem) {
      return this._closeFolder(elem);
    };

    FileTree.prototype.toggle = function(elem) {
      var $parent;
      $parent = $(elem).closest('li');
      if ($parent.hasClass('is-collapsed')) {
        return this._openFolder(elem);
      } else if ($parent.hasClass('is-expanded')) {
        return this._closeFolder(elem);
      }
    };

    FileTree.prototype.select = function(elem) {
      $(this.element).find('li.is-selected').removeClass('is-selected');
      $(elem).closest('li').addClass('is-selected');
      if (this.settings.multiselect === true) {
        return $(elem).siblings('input[type=checkbox]').togglecheck().change();
      }
    };

    FileTree.prototype.getSelected = function() {
      if (this.settings.multiselect === true) {
        return $(this.element).find('input[type=checkbox]:checked').map(function() {
          return $(this).siblings('a');
        }).get();
      }
    };

    FileTree.prototype.expandAll = function() {
      var self;
      self = this;
      return $(this.element).find('li.folder').each(function() {
        return self._openFolder($(this).find('> a'));
      });
    };

    FileTree.prototype.search = function(str) {
      var self;
      str = str.toLowerCase();
      self = this;
      $(this.element).find('li').each(function(index, item) {
        var e, exists;
        e = $(item);
        exists = self._data[index].indexOf(str) < 0;
        if (exists) {
          if (e.hasClass('folder') && e.find('> ul > li').length > 0) {
            e.children('li').removeClass('is-hidden');
          } else {
            e.addClass('is-hidden');
          }
        } else {
          e.removeClass('is-hidden');
        }
      });
      return this;
    };

    FileTree.prototype.destroy = function() {
      return $(this.element).off().empty();
    };

    FileTree.prototype._getRootElement = function(elem, method) {
      if ($(elem).prop('tagName').toLowerCase() === 'ul') {
        return $(elem).addClass('filetree');
      } else if ($(elem).find('ul').length > 0) {
        return $(elem).find('ul').eq(0).addClass('filetree');
      } else {
        return $(document.createElement('ul')).addClass('filetree').appendTo($(elem));
      }
    };

    FileTree.prototype._createTree = function(elem, data) {
      var $elem, a, arrow, checkbox, file, item, key, li, ul, value, _files, _folders, _i, _j, _len, _len1, _subfolders;
      $elem = $(elem);
      _files = [];
      _folders = [];
      for (_i = 0, _len = data.length; _i < _len; _i++) {
        file = data[_i];
        if (file.type === 'folder') {
          _folders.push(file);
        }
        if (file.type === 'file') {
          _files.push(file);
        }
      }
      _files.sort(this._nameSort);
      _folders.sort(this._nameSort);
      data = _folders.concat(_files);
      if ($elem.prop('tagName').toLowerCase() === 'ul') {
        ul = $elem;
      } else {
        ul = $(document.createElement('ul'));
      }
      for (_j = 0, _len1 = data.length; _j < _len1; _j++) {
        item = data[_j];
        li = $(document.createElement('li')).addClass(item.type);
        if (item.type === 'file' && this.settings.hideFiles === true) {
          li.addClass('is-hidden');
        }
        a = $(document.createElement('a')).attr('href', '#');
        if (item.type === 'file') {
          a.attr('title', item[this.settings.fileNodeTitle]).html(item[this.settings.fileNodeName]);
        } else if (item.type === 'folder') {
          a.attr('title', item[this.settings.folderNodeTitle]).html(item[this.settings.folderNodeName]);
        }
        for (key in item) {
          value = item[key];
          if (item.hasOwnProperty(key) && key !== 'children') {
            a.data(key, value);
          }
        }
        li.append(a);
        if (item.type === 'folder' && typeof item.children !== 'undefined' && item.children.length > 0) {
          li.addClass('is-collapsed').addClass('has-children');
          arrow = $(document.createElement('button')).addClass('arrow');
          li.prepend(arrow);
          if (this.settings.hideFiles === true) {
            _subfolders = $.grep(item.children, function(e) {
              return e.type === 'folder';
            });
            if (_subfolders.length > 0) {
              li.removeClass('is-collapsed').removeClass('has-children');
              li.find('button').removeClass('arrow').addClass('no-arrow');
            }
          }
          this._createTree.call(this, li, item.children);
        }
        if (this.settings.multiselect === true) {
          checkbox = $(document.createElement('input')).attr('type', 'checkbox');
          if (!!item.readOnly) {
            checkbox.prop('disabled', true);
            li.addClass('is-read-only');
          }
          li.prepend(checkbox);
        }
        li = this.settings.nodeFormatter.call(null, li);
        ul.append(li);
      }
      return $elem.append(ul);
    };

    FileTree.prototype._openFolder = function(elem) {
      var $a, $parent, $ul, ev_end, ev_start, that;
      $parent = $(elem).closest('li');
      if (!$parent.hasClass('folder')) {
        return false;
      }
      $a = $parent.find('a').eq(0);
      $ul = $parent.find('ul').eq(0);
      that = this;
      ev_start = $.Event('open.folder.filetree');
      ev_end = $.Event('opened.folder.filetree');
      $a.trigger(ev_start);
      return $ul.slideDown(that.settings.animationSpeed, function() {
        $parent.removeClass('is-collapsed').addClass('is-expanded');
        $ul.removeAttr('style');
        return $a.trigger(ev_end);
      });
    };

    FileTree.prototype._closeFolder = function(elem) {
      var $a, $parent, $ul, ev_end, ev_start, that;
      $parent = $(elem).closest('li');
      if (!$parent.hasClass('folder')) {
        return false;
      }
      $a = $parent.find('a').eq(0);
      $ul = $parent.find('ul').eq(0);
      that = this;
      ev_start = $.Event('close.folder.filetree');
      ev_end = $.Event('closed.folder.filetree');
      $a.trigger(ev_start);
      return $ul.slideUp(that.settings.animationSpeed, function() {
        $parent.removeClass('is-expanded').addClass('is-collapsed');
        $ul.removeAttr('style');
        return $a.trigger(ev_end);
      });
    };

    FileTree.prototype._triggerClickEvent = function(eventName) {
      var $a, $root, data, ev, path;
      $a = $(this);
      $root = $(this.element);
      ev = $.Event(eventName, {
        bubbles: false
      });
      data = $a.data();

      /*
          Get path of the file
       */
      if (typeof data.path === 'undefined') {
        path = $a.parentsUntil($root, 'li').clone().children('ul,button').remove().end();
        data.path = map.call(path, function(a) {
          return a.innerText;
        }).reverse().join('/');
      }
      return $a.trigger(ev, data);
    };

    FileTree.prototype._addListeners = function() {
      var $root, that;
      $root = $(this.element);
      that = this;
      $root.on('click', 'li.folder.is-collapsed.has-children > button.arrow', function(event) {
        that._openFolder(this);
        return event.stopImmediatePropagation();
      });
      $root.on('click', 'li.folder.is-expanded.has-children > button.arrow', function(event) {
        that._closeFolder(this);
        return event.stopImmediatePropagation();
      });
      $root.on('click', 'li.folder > a', function(event) {
        that._triggerClickEvent.call(this, 'click.folder.filetree');
        return event.stopImmediatePropagation();
      });
      $root.on('click', 'li.file > a', function(event) {
        that._triggerClickEvent.call(this, 'click.file.filetree');
        return event.stopImmediatePropagation();
      });
      $root.on('click', 'li.file, li.folder', function(event) {
        return event.stopImmediatePropagation();
      });
      $root.on('click', function(event) {
        return event.stopImmediatePropagation();
      });
      $root.on('dblclick', 'li.folder > a', function(event) {
        that._triggerClickEvent.call(this, 'dblclick.folder.filetree');
        return event.stopImmediatePropagation();
      });
      $root.on('dblclick', 'li.file > a', function(event) {
        that._triggerClickEvent.call(this, 'dblclick.file.filetree');
        return event.stopImmediatePropagation();
      });
      if (this.settings.multiselect && this.settings.hierarchy) {
        $root.on('change', 'input[type=checkbox]:not([disabled])', function(event) {
          var $currentNode, ischecked;
          $currentNode = $(event.target).closest('li');
          if ($currentNode.hasClass('folder') && $currentNode.hasClass('has-children')) {
            ischecked = $currentNode.find('> input[type=checkbox]:not([disabled])').prop('checked');
            $currentNode.find('> ul').find('input[type=checkbox]:not([disabled])').prop('checked', ischecked).prop('indeterminate', false);
          }
          $currentNode.parentsUntil($root, 'li.folder').each(function() {
            var $parentNode, checkedNodes, childNodes, immediateChild;
            $parentNode = $(this);
            childNodes = $parentNode.find('> ul').find('input[type=checkbox]:not([disabled])');
            immediateChild = $parentNode.find('> input[type=checkbox]:not([disabled])');
            checkedNodes = $parentNode.find('> ul').find('input[type=checkbox]:not([disabled]):checked');
            if (checkedNodes.length > 0) {
              immediateChild.semicheck();
              if (checkedNodes.length === childNodes.length) {
                return immediateChild.check();
              }
            } else {
              return immediateChild.uncheck();
            }
          });
          return event.stopImmediatePropagation();
        });
      }
    };

    FileTree.prototype._parseTree = function(elem) {
      var $elem, $temp, arrow, checkbox, children, file, files, item, sublist, _i, _j, _len, _len1;
      $elem = $(elem);
      $temp = $(document.createElement('span')).insertAfter($elem);
      $elem.detach();
      files = $elem.find("> li");
      for (_i = 0, _len = files.length; _i < _len; _i++) {
        file = files[_i];
        sublist = $(file).find("> ul");
        children = $(sublist).find("> li");
        if (children.length > 0 || $(file).hasClass('folder')) {
          arrow = $(document.createElement('button')).addClass('arrow');
          $(file).addClass('folder has-children is-collapsed').prepend(arrow);
          for (_j = 0, _len1 = sublist.length; _j < _len1; _j++) {
            item = sublist[_j];
            this._parseTree(item);
          }
        } else {
          $(file).addClass('file');
        }
        if (this.settings.multiselect === true) {
          checkbox = $(document.createElement('input')).attr('type', 'checkbox');
          $(file).prepend(checkbox);
        }
      }
      $elem.find('li > a[data-type=folder]').closest('li').addClass('folder').removeClass('file');
      $elem.insertBefore($temp);
      return $temp.remove();
    };

    FileTree.prototype._nameSort = function(a, b) {
      if (a.name.toLowerCase() < b.name.toLowerCase()) {
        return -1;
      } else if (a.name.toLowerCase() > b.name.toLowerCase()) {
        return 1;
      } else {
        return 0;
      }
    };

    return FileTree;

  })();

  /*
      PLUGIN DEFINITION
   */
  Plugin = function(options, obj) {
    var retVal;
    retVal = this;
    this.each(function() {
      var $this, data;
      $this = $(this);
      data = $this.data('$.filetree');
      if (!data) {
        $this.data("$.filetree", (data = new FileTree(this, options)));
      }
      if (typeof options === 'string' && options.substr(0, 1) !== '_') {
        retVal = data[options].call(data, obj);
      }
    });
    return retVal;
  };
  old = $.fn.filetree;
  $.fn.filetree = Plugin;
  $.fn.filetree.Constructor = FileTree;

  /*
      NO CONFLICT
   */
  $.fn.filetree.noConflict = function() {
    $.fn.filetree = old;
    return this;
  };
});
