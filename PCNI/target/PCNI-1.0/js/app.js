var __hasProp = {}.hasOwnProperty,
  __extends = function(child, parent) { for (var key in parent) { if (__hasProp.call(parent, key)) child[key] = parent[key]; } function ctor() { this.constructor = child; } ctor.prototype = parent.prototype; child.prototype = new ctor(); child.__super__ = parent.prototype; return child; };

$(document).ready(function() {
  var $fileTree, $fsActionModal, $main, $pageIcon, $pageTitle, $ubmenu, AppRouter, abort_all_requests, appRouter, dataFilter, days, elapsedTime, filter, getScheduleSummary, getTree, i, remove_request, renderPages, requestTimer, reset_loading_panel, treeTable, _i;
  if (window.location.search.length < 1 && window.history.pushState) {
    window.history.pushState(null, 'PCNI', './');
  }
  $.ajaxSetup({
    type: "POST"
  });


  
  /*
  
      Append Modals
   */
  $('body').append(Dashboard.Templates.modals());

  /*
  
          Holders for keeping track of elpased time of request
   */
  requestTimer = null;
  elapsedTime = 0;

  /*
  
          Holds the XHR objects while an AJAX request is in progress
   */
  $.ajaxQueue = [];

  /*
  
          Method to abort all in progress AJAX requests
   */
  abort_all_requests = function() {
    var request, _i, _len, _ref;
    _ref = $.ajaxQueue;
    for (_i = 0, _len = _ref.length; _i < _len; _i++) {
      request = _ref[_i];
      if (typeof request.abort === 'function') {
        request.abort();
      }
    }
    if ($.ajaxQueue.length > 0) {
      $.ajaxQueue = [];
    }
  };

  /*
  
       Removes specific XHR object from queue
   */
  remove_request = function(identifier) {
    $.ajaxQueue = _.filter($.ajaxQueue, function(obj) {
      return obj.identifier !== identifier;
    });
  };

  /*
  
          Resets the timers and hides the loading panel
   */
  reset_loading_panel = function() {
    $("#loading-panel").modal('hide');
    clearInterval(requestTimer);
    elapsedTime = 0;
    $('#elpased_time').html('0:00');
  };

  /*
  
          AJAX setup
   */
  $(document).ajaxStart(function() {

    /*
    
            Register a handler to be called when the first Ajax request begins
    
            1. Show the loading panel
    
            2. Start the timer for 'Elapsed Time'
     */
    $(document).trigger($.Event('start.requests.hdi'));
    $("#loading-panel").modal({
      backdrop: 'static'
    }).modal('show');
    requestTimer = setInterval(function() {
      var minutes, seconds;
      ++elapsedTime;
      seconds = (elapsedTime % 60) > 9 ? (elapsedTime % 60).toString() : '0' + (elapsedTime % 60).toString();
      minutes = Math.floor(elapsedTime / 60).toString();
      $('#elpased_time').html(minutes + ':' + seconds);
    }, 1000);
  }).ajaxSend(function(event, xhr, options) {

    /*
    
            Attach a function to be executed before an Ajax request is sent.
    
            1. Update number of active requests
    
            2. Push XHR object in queue
     */
    $('#request-count').html($.active);
    $.ajaxQueue.push(xhr);
  }).ajaxSuccess(function(event, xhr, options) {

    /*
    
            Register a handler to be called when Ajax requests complete successfully
    
            1. Remove XHR object from queue
     */
    remove_request(xhr.identifier);
  }).ajaxComplete(function(event, xhr, options) {

    /*
    
            Register a handler to be called when Ajax requests complete
    
            1. Update number of active request
     */
    $('#request-count').html($.active - 1);
  }).ajaxStop(function() {

    /*
    
            Register a handler to be called when all Ajax requests have completed
    
            1. Close the loading panel
    
            2. Reset the queue if required
     */
    reset_loading_panel();
    if ($.ajaxQueue.length > 0) {
      $.ajaxQueue = [];
    }
    return $(document).trigger($.Event('stop.requests.hdi'));
  }).ajaxError(function(event, xhr, options, error) {

    /*
    
            Register a handler to be called when an error occurs in Ajax request
    
            If any error occurs (expect 'abort')
    
            1. Cancel remaining requests
    
            2. Close the loading panel
    
            3. Show the error in error panel
     */
    if (error === 'abort') {
      return false;
    }
    abort_all_requests();
    reset_loading_panel();
    error = "There was a problem with " + xhr.identifier + ". <b>'" + error + "'</b>.";
    $('#error-generated').html(error);
    $('#error-panel').modal('show');
  });

  /*
  
          Cancel requests on demand
   */
  $('#cancel_all_requests').on('click', function() {
    abort_all_requests();
    reset_loading_panel();
  });
  $.download = function(url, data, method) {
    var form, key, value;
    if (method == null) {
      method = 'post';
    }
    if (data && url) {
      form = $("<form method=\"" + method + "\" action=\"" + url + "\"></form>");
      for (key in data) {
        if (!__hasProp.call(data, key)) continue;
        value = data[key];
        form.append("<input name='" + key + "' value=\"" + value + "\">");
      }
      form.appendTo('body').submit().remove();
    }
  };
  $.getPrint = function(format) {
    var base, data;
    base = "<base href=\"" + window.location.protocol + "//" + window.location.host + window.DashboardGlobals.baseUrl + "\">";
    data = $('html').clone().find('script').remove().end().find('nav').remove().end().find('#dashboardCanvas').removeClass('dashboardCanvas').end().find('head').prepend(base).end().html();
    data = encodeURIComponent(Base64.encode('<html>' + data + '</html>'));
    return $.download(window.DashboardGlobals.reportDownload, {
      xml: data,
      format: format,
      dir: window.DashboardGlobals.folderpath,
      filename: window.DashboardGlobals.file,
      reportType: window.DashboardGlobals.extension,
      resultDirectory: window.DashboardGlobals.savePath,
      reportNameParam: window.DashboardGlobals.fileTitle
    });
  };
  $.fn.serializeObject = function() {
    var arr, obj, results, _i, _len;
    results = {};
    arr = this.serializeArray();
    for (_i = 0, _len = arr.length; _i < _len; _i++) {
      obj = arr[_i];
      if (results.hasOwnProperty(obj.name)) {
        if (!results[obj.name].push) {
          results[obj.name] = [results[obj.name]];
        }
        results[obj.name].push(obj.value || '');
      } else {
        results[obj.name] = obj.value || '';
      }
    }
    return results;
  };
  $.fn.populateForm = function(data) {
    var $ctrl, form, key, re, t, tag, value, x, _i, _len;
    form = $(this);
    re = /^(?:color|date|datetime|email|month|number|password|range|search|tel|text|time|url|week)$/i;
    for (key in data) {
      if (!__hasProp.call(data, key)) continue;
      value = data[key];
      $ctrl = form.find("[name=" + key + "]");
      if ($ctrl.length < 1) {
        continue;
      }
      t = $ctrl.attr('type');
      tag = $ctrl.prop('tagName').toLowerCase();
      if (re.test(t) || tag === 'textarea' || tag === 'select') {
        $ctrl.val(value).change();
      } else if (t === 'checkbox' || t === 'radio') {
        $ctrl.prop('checked', false);
        if ($.isArray(value)) {
          for (_i = 0, _len = value.length; _i < _len; _i++) {
            x = value[_i];
            $ctrl.filter("[value=" + x + "]").prop('checked', true).change();
          }
        } else {
          $ctrl.filter("[value=" + value + "]").prop('checked', true).change();
        }
      }
    }
    return form;
  };
  $.fn.resetForm = function(includeHidden) {
    return this.each(function() {
      return $('input,select,textarea', this).clearFields(includeHidden);
    });
  };
  $.fn.tagName = function() {
    return this.tagName.toLowerCase();
  };
  $.fn.clearFields = function(includeHidden) {
    var re;
    re = /^(?:color|date|datetime|email|month|number|password|range|search|tel|text|time|url|week)$/i;
    return this.each(function() {
      var t, tag;
      t = this.type;
      tag = this.tagName.toLowerCase();
      if (re.test(t) || tag === 'textarea') {
        return this.value = '';
      } else if (t === 'checkbox' || t === 'radio') {
        return this.checked = false;
      } else if (tag === 'select') {
        return this.selectedIndex = -1;
      } else if (t === "file") {
        if (/MSIE/.test(navigator.userAgent)) {
          return $(this).replaceWith($(this).clone(true));
        } else {
          return $(this).val('');
        }
      } else if (includeHidden) {

        /* 
            includeHidden can be the value true, or it can be a selector string
            indicating a special test; for example:
            $('#myForm').clearForm('.special:hidden')
            the above would clean hidden inputs that have the class of 'special'
         */
        if ((includeHidden === true && /hidden/.test(t)) || (typeof includeHidden === 'string' && $(this).is(includeHidden))) {
          return this.value = '';
        }
      }
    });
  };
  $.fn.bindWithDelay = function(type, data, fn, timeout, throttle) {
    if ($.isFunction(data)) {
      throttle = timeout;
      timeout = fn;
      fn = data;
      data = void 0;
    }
    fn.guid = fn.guid || ($.guid && $.guid++);
    return this.each(function() {
      var cb, wait;
      wait = null;
      cb = function() {
        var ctx, e, throttler;
        e = $.extend(true, {}, arguments[0]);
        ctx = this;
        throttler = function() {
          wait = null;
          fn.apply(ctx, [e]);
        };
        if (!throttle) {
          clearTimeout(wait);
          wait = null;
        }
        if (!wait) {
          return wait = setTimeout(throttler, timeout);
        }
      };
      cb.guid = fn.guid;
      return $(this).bind(type, data, cb);
    });
  };
  $main = $('#main');
  $pageIcon = $('#page-icon');
  $pageTitle = $('#page-title');
  $ubmenu = $('#pcni-sub-menu');
  $fileTree = null;
  filter = function(data, name) {
    var d, found, _i, _len;
    found = false;
    for (_i = 0, _len = data.length; _i < _len; _i++) {
      d = data[_i];
      if (d.name === name) {
        found = d.children;
        window.DashboardGlobals.filterBasePath = d.path;
      } else if ($.isArray(d.children)) {
        found = filter(d.children, name);
      }
      if (found !== false) {
        break;
      }
    }
    return found;
  };
  dataFilter = {
    "new-report": "New Report",
    "my-library": "My Library",
    "favourites": "Favourites",
    "saved-reports": "Saved Reports"
  };
  getTree = function(id) {
    $fileTree = $("#" + id + "-list").off().empty();
    if ($fileTree.data('$.filetree')) {
      $fileTree.data('$.filetree', null);
    }
    return $.get(window.DashboardGlobals.solutionLoader).done(function(data) {
      var checkboxes;
      data = filter(JSON.parse(data), dataFilter[id]);
      console.log(data);
      $fileTree.filetree({
        data: data,
        fileNodeName: 'title',
        fileNodeTitle: 'description',
        multiselect: true,
        hierarchy: false,
        nodeFormatter: function(node) {
          var btn, fav, options, scd, select;
          fav = node.find('> a').data('favourite');
          options = node.find('> a').data('options');
          select = options && options.selectable && options.selectable.toLowerCase() === 'true' ? true : false;
          scd = typeof node.find('> a').data('schedulingreference') !== 'undefined';
          if (typeof fav !== 'undefined') {
            btn = $(document.createElement('button')).addClass('fa fav-btn');
            if (fav === 'false') {
              node.append(btn.addClass('fa-star-o'));
            } else {
              node.append(btn.addClass('fa-star'));
            }
          }
          if (scd) {
            node.append($(document.createElement('button')).addClass('fa fa-clock-o scd-btn'));
          }
          if (!select) {
            node.find("> input[type=checkbox]").remove();
          }
          return node;
        }
      }).on('click.file.filetree', function(ev, el) {
        var elem, xhr;
        elem = $(ev.target);
        window.DashboardGlobals.folderpath = elem.data('path');
        window.DashboardGlobals.file = elem.data('name');
        window.DashboardGlobals.extension = elem.data('extension');
        window.DashboardGlobals.fileTitle = elem.data('title');
        window.DashboardGlobals.fileIsFavourite = false;
        if (id === 'favourites' || (elem.siblings('button.fav-btn').length > 0 && elem.siblings('button.fav-btn').eq(0).hasClass('fa-star'))) {
          window.DashboardGlobals.fileIsFavourite = true;
        }
        if (elem.data('type') === 'file') {
          window.DashboardGlobals.folderpath = window.DashboardGlobals.folderpath.replace(elem.data('name'), '').replace(/[\/\\]*$/g, '');
        }
        $('#search-tree').closest('form').addClass('hidden');
        xhr = $.post(window.DashboardGlobals.controllers[elem.data('extension').toLowerCase()], {
          dir: window.DashboardGlobals.folderpath,
          file: elem.data('name')
        });
        xhr.identifier = "serviceLoader";
        xhr.done(function(data) {
  //        $ubmenu.addClass('hidden');
          $main.off().html(data);
        });
        return ev.preventDefault();
      }).on('click.folder.filetree', function(ev, el) {
        $(this).filetree('toggle', ev.target);
        return ev.preventDefault();
      }).on('click', '.fav-btn', function(e) {
        var act, btn, t;
        btn = $(e.target);
        act = btn.hasClass('fa-star') ? 'unmark' : 'mark';
        t = btn.siblings('a').eq(0);
        $.post(window.DashboardGlobals.saveReport, {
          reportDirectory: t.data('path').replace(t.data('name'), ''),
          reportFile: t.data('name'),
          operation: 'favourite',
          markAsFavourite: act,
          favouriteLocation: "DataQualityReport/" + dataFilter["favourites"]
        }).done(function(data) {
          if (act === 'mark' && data === 'success') {
            btn.removeClass('fa-star-o').addClass('fa-star');
          }
          if (act === 'unmark' && data === 'unmarked') {
            if (act === 'unmark') {
              return btn.removeClass('fa-star').addClass('fa-star-o');
            }
          }
        });
        return e.stopPropagation();
      }).on('click', '.scd-btn', function(e) {
        var btn, t;
        btn = $(e.target);
        t = btn.siblings('a').eq(0);
        return $.post(window.DashboardGlobals.scheduling.get, {
          id: t.data('schedulingreference')
        }).done(function(data) {
          if (typeof data === 'string') {
            data = JSON.parse(data);
          }
          if (data.hasOwnProperty('ScheduleOptions')) {
            $('#scheduling-modal').populateForm(data.ScheduleOptions);
            $('#scheduling-modal').modal('show');
            return $('#scheduleButton').off('click').one('click', function() {
              data.ScheduleOptions = $.extend(data.ScheduleOptions, $('#scheduling-form').serializeObject());
              return $.post(window.DashboardGlobals.scheduling.update, {
                data: JSON.stringify(data)
              }).done(function() {
                return $('#scheduling-modal').modal('hide');
              });
            });
          } else if (data.hasOwnProperty('message')) {
            return Dashboard.alert(data.message);
          }
        });
      });
      checkboxes = $("#" + id + "-list").find('input[type=checkbox]:not([disabled])');
      $('#selectAll').click(function() {
        var checked;
        checked = $(this).prop('checked');
        if (checked) {
          return checkboxes.prop('checked', true);
        } else {
          return checkboxes.prop('checked', false);
        }
      });
      $('#search-tree').closest('form').removeClass('hidden').end().on('focus', function() {
        return $fileTree.filetree('expandAll');
      }).on('keyup', function() {
        var e;
        e = $(this);
        return $fileTree.filetree('search', e.val());
      });
    });
  };
  treeTable = function() {
    return $.get(window.DashboardGlobals.solutionLoader).done(function(data) {
      var $temp, list, makeTable;
      data = filter(JSON.parse(data), 'Saved Reports');
      list = $("#saved-reports-list").off().empty();
      $temp = $(document.createElement('span')).insertAfter(list);
      list.detach();
      makeTable = function(files, iteration) {
        var arrow, checkbox, column, date, file, i, key, link1, link2, row, span, value, _i, _j, _k, _len;
        if (iteration == null) {
          iteration = 0;
        }
        for (_i = 0, _len = files.length; _i < _len; _i++) {
          file = files[_i];
          row = $(document.createElement('tr'));
          column = $(document.createElement('td'));
          checkbox = $(document.createElement('input')).attr('type', 'checkbox');
          for (key in file) {
            if (!__hasProp.call(file, key)) continue;
            value = file[key];
            checkbox.data(key, value);
          }
          row.append(column.clone().append(checkbox));
          if (file.type === 'folder') {
            span = $(document.createElement('span')).addClass('fa fa-folder');
            row.append(column.clone().append(span).append(' ' + file.name)).append(column.clone()).append(column.clone());
            for (i = _j = 0; 0 <= iteration ? _j <= iteration : _j >= iteration; i = 0 <= iteration ? ++_j : --_j) {
              if (!(i > 0)) {
                continue;
              }
              arrow = $(document.createElement('span')).addClass('fa fa-level-up fa-rotate-90');
              arrow.insertBefore(span);
            }
            list.append(row);
            if (file.children && file.children.length > 0) {
              makeTable(file.children, iteration + 1);
            }
          } else {
            link1 = $(document.createElement('a')).text(file.title).addClass('reportfile').attr({
              href: '#',
              title: file.title
            }).data({
              extension: file.reporttype,
              path: file.reportdir,
              name: file.reportfile
            });
            row.append(column.clone().append(link1));
            for (i = _k = 0; 0 <= iteration ? _k < iteration : _k > iteration; i = 0 <= iteration ? ++_k : --_k) {
              arrow = $(document.createElement('span')).addClass('fa fa-level-up fa-rotate-90');
              arrow.insertBefore(link1);
            }
            if (iteration > 0) {
              link1.text(' ' + link1.text());
            }
            link2 = $(document.createElement('a')).text(file.resultname).addClass('savedfile').attr({
              href: '#',
              title: file.title
            }).data({
              extenstion: file.reporttype,
              dir: file.path.replace(file.name, '').replace(/[\/\\]*$/g, ''),
              filename: file.name
            });
            date = moment(file.rundate, 'YYYY-MM-DD').format('MM/DD/YYYY');
            var ext = file.resultfile.split(".");
            ext = ext.pop();
            row.append(column.clone().append(link2)).append(column.clone().append(ext.toUpperCase())).append(column.clone().append(date));
            list.append(row);
          }
        }
      };
      makeTable(data);
      list.insertBefore($temp);
      $temp.remove();
      list.on('click', 'a.reportfile', function(event) {
        var elem, xhr;
        event.preventDefault();
        elem = $(event.target);
        window.DashboardGlobals.folderpath = elem.data('path');
        window.DashboardGlobals.file = elem.data('name');
        window.DashboardGlobals.extension = elem.data('extension');
        window.DashboardGlobals.fileTitle = elem.data('title');
        if (elem.data('type') === 'file') {
          window.DashboardGlobals.folderpath = window.DashboardGlobals.folderpath.replace(elem.data('name'), '').replace(/[\/\\]*$/g, '');
        }
        xhr = $.post(window.DashboardGlobals.controllers[elem.data('extension').toLowerCase()], {
          dir: window.DashboardGlobals.folderpath,
          file: elem.data('name')
        });
        xhr.identifier = "serviceLoader";
        return xhr.done(function(data) {
 //         $ubmenu.addClass('hidden');
          $main.off().html(data);
        });
      }).on('click', 'a.savedfile', function(event) {
        var elem, iframe, url;
        elem = $(event.target);
        url = "" + window.DashboardGlobals.downloadEnableSavedReport + "?dir=" + (elem.data('dir')) + "&filename=" + (elem.data('filename'));
        $('.download-save-report-iframe').remove();
        iframe = $(document.createElement('iframe')).addClass('download-save-report-iframe');
        iframe.attr({
          src: url,
          style: "visibility:hidden;display:none"
        }).appendTo($('body'));
        iframe.load(function() {
          elem = $(this);
          window.setTimeout(function() {
            elem.remove();
          }, 5000);
        });
        return event.preventDefault();
      });
      $('#selectAll, #selectAllSavedReports').off().click(function() {
        var checked;
        checked = $(this).prop('checked');
        return list.find('input[type=checkbox]').prop('checked', checked);
      });
      $('#search-tree').off().closest('form').submit(function(event) {
        return false;
      });
      return list.closest('table').filterTable({
        inputSelector: '#search-tree',
        highlightClass: 'bg-warning',
        minRows: 0
      });
    });
  };
  renderPages = function(id) {
    var template;
    if (id == null) {
      id = 'home';
    }
    template = $("#" + id + "-tpl");
    $pageTitle.text(template.data('title'));
    $pageIcon.attr('class', "fa fa-lg fa-" + (template.data('icon')));
    $main.html(template.html());
    $('#fs-menu').removeAttr('class').addClass("dropdown-menu " + id);
    $('#search-tree').off().closest('form').addClass('hidden');
    $('#refresh-tree').data('id', id);
    if (['home', 'saved-reports'].indexOf(id) < 0) {
      getTree(id);
    } else if (id === 'saved-reports') {
      treeTable();
      $('#search-tree').closest('form').removeClass('hidden');
    }
    
    if ('new-report' === id) {
        $ubmenu.removeClass('hidden').find('.form-inline').addClass('hidden');
      } else {
        $ubmenu.find('.hidden').removeClass('hidden');
      }
      if ('home' === id) {
        $ubmenu.addClass('hidden');
      } else {
        $ubmenu.removeClass('hidden');
      }
    
    
  };
  AppRouter = (function(_super) {
    __extends(AppRouter, _super);

    function AppRouter() {
      return AppRouter.__super__.constructor.apply(this, arguments);
    }

    AppRouter.prototype.routes = {
      '!/new-report/': 'newReport',
      '!/my-library/': 'myLibrary',
      '!/favourites/': 'favourites',
      '!/saved-reports/': 'savedReports',
      "!/": 'home'
    };

    AppRouter.prototype.home = function() {
      return renderPages('home');
    };

    AppRouter.prototype.newReport = function() {
      return renderPages('new-report');
    };

    AppRouter.prototype.myLibrary = function() {
      return renderPages('my-library');
    };

    AppRouter.prototype.favourites = function() {
      return renderPages('favourites');
    };

    AppRouter.prototype.savedReports = function() {
      return renderPages('saved-reports');
    };

    return AppRouter;

  })(Backbone.Router);
  appRouter = new AppRouter();
  Backbone.history.start();
  $('#refresh-tree').on('click', function() {
    var e, id;
    e = $(this);
    id = e.data('id');
    if (['home', 'saved-reports'].indexOf(id) < 0) {
      getTree(id);
    }
    if (id === 'saved-reports') {
      return treeTable();
    }
  });
  if (window.location.search.length < 1) {
    appRouter.navigate('!/', {
      trigger: true
    });
  }

  /*
      Generates Scheduling Summary
   */
  getScheduleSummary = function() {
    var data, prefixes, startMoment, summary;
    summary = '';
    data = $('#scheduling-form').serializeObject();
    startMoment = moment(data.StartDate, 'YYYY-MM-DD');
    if (data.RepeatsEvery > 1) {
      summary += "Every " + data.RepeatsEvery + " " + (data.Frequency.replace('ly', '').replace('Dai', 'Day')) + "(s)";
    } else {
      summary += "" + data.Frequency;
    }
    switch (data.Frequency) {
      case 'Weekly':
        if ($.isArray(data.DaysofWeek)) {
          summary += " on " + (data.DaysofWeek.join(', '));
        } else {
          summary += " on " + data.DaysofWeek;
        }
        break;
      case 'Monthly':
        if (data.RepeatBy === 'dayOfTheMonth') {
          summary += " on day " + (startMoment.format('D'));
        } else if (data.RepeatBy === 'dayOfTheWeek') {
          prefixes = ['First', 'Second', 'Third', 'Fourth', 'Last'];
          summary += " on  " + prefixes[Math.ceil(startMoment.date() / 7) - 1] + " " + (startMoment.format('dddd'));
        }
        break;
      case 'Yearly':
        summary += " on  " + (startMoment.format('MMMM Do'));
    }
    if (data.endsRadio === 'After') {
      summary += ", " + data.EndAfterExecutions + " times";
    } else if (data.endsRadio === 'On') {
      summary += ", until " + (moment(data.EndDate, 'YYYY-MM-DD').format('Do MMMM, YYYY'));
    }
    return $('#schedule-summary').html(summary);
  };
  days = $("#repeatEvery");
  for (i = _i = 1; _i <= 31; i = ++_i) {
    days.append("<option value='" + i + "'>" + i + "</option>");
  }
  $("#scheduling-modal").on('change', '#repeatOrder', function() {
    var t;
    t = $(this);
    $('#repeatEvery').val(1);
    $('#repeatOrder-text').text(t.val().replace('ly', '').replace('Dai', 'Day').toLowerCase());
    $('#never').prop('checked', true).change();
    if (['Daily', 'Yearly'].indexOf(t.val()) > -1) {
      $('#repeatBy').addClass('hidden');
      return $('#repeatOn').addClass('hidden');
    } else if (t.val() === 'Weekly') {
      $('#repeatOn').removeClass('hidden');
      return $('#repeatBy').addClass('hidden');
    } else if (t.val() === 'Monthly') {
      $('#repeatBy').removeClass('hidden');
      return $('#repeatOn').addClass('hidden');
    }
  }).on('change', 'input.ends', function() {
    var date, oc, t;
    t = $(this);
    date = $('#endsOnDate');
    oc = $('#endsAfterOccurrences');
    return {
      'Never': function() {
        date.prop('disabled', true);
        return oc.prop('disabled', true);
      },
      'After': function() {
        date.prop('disabled', true);
        return oc.prop('disabled', false);
      },
      'On': function() {
        date.prop('disabled', false);
        return oc.prop('disabled', true);
      }
    }[t.val()]();
  }).on('change', 'input, select', getScheduleSummary);
  $('#startsOn, #endsOnDate').daterangepicker({
    singleDatePicker: true,
    showDropdowns: true,
    startDate: moment(),
    endDate: moment(),
    format: 'YYYY-MM-DD'
  }, function(start, end) {
    var e;
    e = $(this.element);
    e.val(start.format('YYYY-MM-DD'));
    return getScheduleSummary();
  }).val(moment().format('YYYY-MM-DD'));
  $("#scheduling-modal input[type=checkbox][value='" + (moment().format('dddd')) + "']").prop('checked', true);
  getScheduleSummary();
  $fsActionModal = $('#fs-actions-modal');
  $('#pcni-sub-menu').on('click', 'a[data-fsaction], button[data-fsaction]', function(event) {
    var action, checked, clone, data, e, isTable, name, oldNames, renList, table, text, tree, _j, _len;
    event.preventDefault();
    e = $(event.target);
    if (event.target.tagName === 'SPAN') {
      e = e.closest('button');
    }
    action = e.attr('data-fsaction');
    isTable = $('#fs-menu').hasClass('saved-reports');
    checked = [];
    if (isTable) {
      checked = $('#saved-reports-list').find('input:checked').get();
    } else {
      checked = $fileTree.filetree('getSelected');
    }
    console.log(checked);
    if (event.target.tagName === 'A' && e.closest('li').hasClass('disabled')) {
      return false;
    }
    if (checked.length < 1 && ["new-folder", "import"].indexOf(action) < 0) {
      $fsActionModal.modal('show');
      return false;
    }
    text = event.target.tagName === 'A' ? e.text() : e.attr('title');
    if (action === 'move') {
      tree = $fsActionModal.find('#move-tree');
      if (isTable) {
        table = $('#saved-reports-list').closest('table').clone(true).off();
        table.find('th').each(function(i, e) {
          if (i > 1) {
            return $(e).remove();
          }
        }).find('input').remove();
        table.find('tr').each(function(i, e) {
          var cb;
          cb = $(e).find('input[type=checkbox]');
          if (cb.data('type') === 'file') {
            $(e).remove();
          }
          $(e).find('td').each(function(j, t) {
            if (j > 1) {
              return $(t).remove();
            }
          });
          return cb.attr({
            type: 'radio',
            name: 'move-to'
          });
        });
        tree.append(table);
      } else {
        clone = $fileTree.clone(true).off().find('li.file').remove().end().find('li.folder').each(function(i, e) {
          if ($(e).find('> input[type=checkbox]').prop('checked')) {
            return $(e).remove();
          }
        }).removeClass('is-collapsed').addClass('is-expanded').end().find('input[type=checkbox]').remove().end();
        tree.append(clone);
        tree.on('click', 'li.folder > a', function(event) {
          event.preventDefault();
          e = $(event.target);
          tree.find('.is-selected').removeClass('is-selected');
          return e.closest('li').addClass('is-selected');
        });
      }
      $fsActionModal.one('hidden.bs.modal', function() {
        return tree.off().html('');
      });
    } else if (action === 'rename') {
      renList = $fsActionModal.find('#rename');
      oldNames = $(checked).map(function(i, e) {
        if ($(e).data('type') === 'folder') {
          return $(e).data('name');
        } else {
          if (isTable) {
            return $(e).data('resultname');
          } else {
            return $(e).data('title');
          }
        }
      }).get();
      for (_j = 0, _len = oldNames.length; _j < _len; _j++) {
        name = oldNames[_j];
        renList.append("<tr><td>" + name + "</td><td><input type=\"text\" class=\"form-control\" value=\"" + name + "\"></td></tr>");
      }
      $fsActionModal.one('hidden.bs.modal', function() {
        return renList.html('');
      });
    }
    $fsActionModal.addClass(action).find('.modal-title').text(text).end().modal('show').one('hidden.bs.modal', function() {
      $fsActionModal.removeClass(action);
      return $(document).off('keydown');
    });
    $(document).on('keydown', function(e) {
      if (e.keyCode === 13) {
        $('#fs-actions-form--submit').click();
        return false;
      }
    });
    data = {
      source: checked.map(function(e) {
        return $(e).data('path');
      }),
      action: action
    };
    $('#fs-actions-form--submit').off('click').one('click', function() {
      var form, temp;
      ({
        "new-folder": function() {
          data.folderName = $('#new-folder-form').find('input[name=folderName]').val();
          data.source = checked.filter(function(e) {
            return $(e).data('type') && $(e).data('type').toLowerCase() === 'folder';
          }).map(function(e) {
            return $(e).data('path');
          });
          if (data.source.length < 1 && !!window.DashboardGlobals.filterBasePath) {
            data.source = [window.DashboardGlobals.filterBasePath];
          }
          data.action = "newFolder";
        },
        "rename": function() {
          var newNames, _k, _len1;
          newNames = $('#rename').find('input[type=text]').map(function(i, e) {
            return $(e).val();
          }).get();
          if (newNames.length !== data.source.length) {
            return false;
          }
          for (i = _k = 0, _len1 = newNames.length; _k < _len1; i = ++_k) {
            name = newNames[i];
            data.source[i] = [data.source[i], name];
          }
        },
        "move": function() {
          data.destination = isTable ? $('#move-tree').find('input:checked').data('path') : $('#move-tree').find('.is-selected > a').data('path');
        },
        "import": function() {},
        "delete": function() {},
        "export": function() {}
      })[action]();
      if (data.source) {
        temp = JSON.stringify(data.source);
        data.sourceArray = temp;
        delete data.source;
      }
      if (action === 'export') {
        form = $("<form method=\"POST\" action=\"" + window.DashboardGlobals.fsop + "\"></form>");
        form.append("<input name='action' value=\"" + data.action + "\">");
        form.append("<textarea name=\"sourceArray\">" + data.sourceArray + "</textarea>");
        form.appendTo('body').submit().remove();
        $fsActionModal.modal('hide');
        return $('#refresh-tree').click();
      } else if (action === 'import') {
        return console.log('upload start');
      } else {
        return $.post(window.DashboardGlobals.fsop, data).fail(function(data) {
          return alert('There was a problem while performing this action');
        }).done(function(data) {
          if (data === 'failure') {
            alert('There was a problem while performing this action');
          } else {
            $('#refresh-tree').click();
          }
        }).always(function() {
          return $fsActionModal.modal('hide');
        });
      }
    });
    $('#fileupload').fileupload({
      url: window.DashboardGlobals.importFile,
      autoUpload: false,
      done: function(e, data) {
        $fsActionModal.modal('hide');
        $('#refresh-tree').click();
        return $('#fileupload-progress-bar').css('width', "0%").text("0%").attr('aria-valuenow', 0);
      },
      add: function(e, data) {
        return $('#fs-actions-form--submit').off('click').on('click', function() {
          $('#fileupload-progress').removeClass('hidden');
          data.submit();
        });
      },
      progressall: function(e, data) {
        var progress;
        progress = parseInt(data.loaded / data.total * 100, 10);
        return $('#fileupload-progress-bar').css('width', "" + progress + "%").text("" + progress + "%").attr('aria-valuenow', progress);
      },
      formData: function() {
        var destination;
        destination = checked.length < 1 && !!window.DashboardGlobals.filterBasePath ? window.DashboardGlobals.filterBasePath : $(checked[0]).data('path');
        return [
          {
            name: 'destination',
            value: destination
          }
        ];
      }
    });
  });
});
