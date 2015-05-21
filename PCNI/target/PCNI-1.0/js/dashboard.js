
/* global $, jQuery, Backbone, _, window, document, console */
var Dashboard,
    __hasProp = {}.hasOwnProperty;

Dashboard = {};

(function(window, document, $, _, Backbone, Dashboard) {
    var Component, Components, Error, Modal, Variable, Variables, Views, alert, button, chart, custom, datepicker, daterangepicker, getAllVariables, getVariable, info, init, resetAll, resetComponent, resetComponents, select, select2, setVariable, showAlert, table, template, updateComponent, warn;
    Dashboard.VERSION = "0.1.0";
    Backbone.View.prototype.close = function() {
        if (typeof this.onClose === 'function') {
            this.onClose();
        }
        this.remove();
        this.unbind();
    };
    Variable = Backbone.Model.extend({
        initialize: function(options) {
            this.set('id', options.name);
        },
        defaults: {
            name: null,
            value: null
        },
        idAttribute: 'name'
    });
    Component = Backbone.Model.extend({
        initialize: function(options) {

            /*
                    Use Name as ID for model
             */
            var difference, filter, noparams, param, params, self, vars;
            this.set('id', options.name);
            _.bindAll(this, 'update');

            /*
                    Do some setup if there are any variables to be observed
             */
            if (_.isArray(this.attributes.listeners) && this.attributes.listeners.length > 0) {
                filter = this.attributes.listeners;

                /*
                        Get The variables that do not exist in Variables Collection.
                        If found any, warn the user
                 */
                difference = _.difference(filter, Dashboard.variables.pluck("name"));
                if (difference.length > 0) {
                    console.warn('These variables do not exist : ' + difference.join(','));
                }

                /*
                        Make Virtual-Collection of the observed parameters in Model._subject
                 */
                vars = new Backbone.VirtualCollection(Dashboard.variables, {
                    filter: function(variable) {
                        return $.inArray(variable.get('id'), filter) >= 0;
                    }
                });
                this.set('_subjects', vars);
            }

            /*
                    Do some setup if there are any parameters to be set
             */
            if (_.isArray(this.attributes.parameters) && this.attributes.parameters.length > 0) {
                param = this.attributes.parameters;

                /*
                        Get The variables that do not exist in Variables Collection.
                        If found any, warn the user
                 */
                noparams = _.difference(param, Dashboard.variables.pluck("name"));
                if (noparams.length > 0) {
                    console.warn('These variables do not exist : ' + noparams.join(','));
                }

                /*
                        Make Virtual-Collection of the parameters to be set in Model._params
                 */
                params = new Backbone.VirtualCollection(Dashboard.variables, {
                    filter: function(variable) {
                        return $.inArray(variable.get('id'), param) >= 0;
                    }
                });
                this.set('_params', params);
                self = this;
            }
        },
        defaults: {
            name: null,
            options: {},
            listeners: [],
            parameters: [],
            requestParameters: {},
            executeAtStart: false,
            _data: null,
            _subjects: null,
            _params: null
        },
        update: function() {
            var dataToSend, key, that, value, xhr, _ref;
            that = this;
            dataToSend = {};
            _ref = this.attributes.requestParameters;
            for (key in _ref) {
                if (!__hasProp.call(_ref, key)) continue;
                value = _ref[key];
                dataToSend[key] = Dashboard.getVariable(value);
            }
            dataToSend.dir = window.DashboardGlobals.folderpath;
            dataToSend.map_id = this.attributes.map;
            xhr = $.post(window.DashboardGlobals.updateService, {
                "data": JSON.stringify(dataToSend).replace(/"(\[)\\/g, "$1").replace(/\\("\])"/g, "$1")
            }, function(data, textStatus, xhr) {
                that.set('_data', data);
            });
            xhr.identifier = this.attributes.name;
            return xhr;
        },
        idAttribute: 'name',
        validate: function(model, options) {
            if (typeof model.name !== 'string') {
                return "A name must be provided for " + model.type + " component";
            } else if (typeof model.type !== 'string') {
                return "Component type must be specified";
            } else if (typeof model.htmlElementId !== 'string') {
                return "An HTML id attribute must be provided";
            }
        }
    });
    Variables = Backbone.Collection.extend({
        model: Variable
    });
    Components = Backbone.Collection.extend({
        model: Component,
        initialize: function(options) {
            this.listenTo(this, 'add', this.addComponent);
        },
        addComponent: function(model, collection, options) {
            var component, view;
            component = model.attributes;
            if (Dashboard.Components.hasOwnProperty(component.type)) {
                view = new Dashboard.Components[component.type.toLowerCase()]({
                    model: model,
                    el: component.htmlElementId
                });
                view.render();

                /*
                        If executeAtStart is set to true then update
                 */
                if (view.model.get("executeAtStart") === true && typeof view.model.update === 'function') {
                    view.model.update();
                }
                Dashboard.componentViews.add(view);
            } else {
                Dashboard.Error("Component of type <b>\'" + (component.type.toString()) + "\'<\/b> does not exist");
            }
        }
    });
    Views = Backbone.Collection.extend({});
    Dashboard.components = new Components([]);
    Dashboard.variables = new Variables([]);
    Dashboard.componentViews = new Views([]);
    Dashboard.components.on('invalid', function(model, error) {
        return Dashboard.Error(error);
    });
    Dashboard.components.on('reset', function(collection, options) {
        var model, _i, _len, _ref, _results;
        _ref = options.previousModels;
        _results = [];
        for (_i = 0, _len = _ref.length; _i < _len; _i++) {
            model = _ref[_i];
            _results.push(model.trigger('remove'));
        }
        return _results;
    });
    Dashboard.componentViews.on('reset', function(collection, options) {
        var model, _i, _len, _ref;
        _ref = options.previousModels;
        for (_i = 0, _len = _ref.length; _i < _len; _i++) {
            model = _ref[_i];
            model.attributes.close();
        }
        return true;
    });
    Dashboard.variables.on('invalid', function(model, error) {
        throw error;
    });
    Dashboard.Components = {};
    setVariable = function() {
        var key, value, vars, _ref;
        if (arguments.length === 0) {
            Dashboard.Error('Invalid Arguments for Dashboard.setVariable()');
            return false;
        }
        vars = [];
        if (arguments.length === 2 && typeof arguments[0] === 'string') {
            vars.push({
                name: arguments[0],
                value: arguments[1]
            });
        } else if (typeof arguments[0] === 'object') {
            _ref = arguments[0];
            for (key in _ref) {
                if (!__hasProp.call(_ref, key)) continue;
                value = _ref[key];
                vars.push({
                    name: key,
                    value: value
                });
            }
        }
        Dashboard.variables.add(vars, {
            merge: true
        });
    };
    init = function(arr) {
        resetComponents();
        Dashboard.components.add(arr, {
            validate: true
        });
    };
    resetComponents = function() {
        Dashboard.components.reset([]);
        Dashboard.componentViews.reset([]);
    };
    updateComponent = function(component) {
        var obj;
        obj = Dashboard.components.get(component);
        if (obj && typeof obj.update === 'function') {
            obj.update();
            return true;
        }
        return false;
    };
    resetAll = function() {
        resetComponents();
        Dashboard.variables.reset([]);
    };
    getVariable = function(name) {
        if (Dashboard.variables.get(name)) {
            return Dashboard.variables.get(name).get('value');
        }
        return null;
    };
    getAllVariables = function() {
        var names, values;
        names = Dashboard.variables.pluck('name');
        values = Dashboard.variables.pluck('value');
        return _.object(names, values);
    };
    resetComponent = function(component) {
        var obj;
        obj = Dashboard.components.get(component);
        if (obj) {
            obj.attributes._data = null;
            return true;
        }
        return false;
    };
    showAlert = function(message, type, position) {
        var alert, alertTemplate;
        if (position == null) {
            position = 'bottom';
        }
        alert = $('.alert');
        if (alert.length < 1) {
            alertTemplate = Dashboard.Templates.alert({
                message: "<p>" + message + "</p>",
                type: type,
                position: position
            });
            return $(alertTemplate).appendTo('body').addClass("alert-" + type);
        } else {
            if (alert.data('timer')) {
                clearTimeout(alert.data('timer'));
            }
            return alert.append("<p>" + message + "</p>").addClass("alert-" + type);
        }
    };

    /*
            Precedence Order
            info < alert < warn

            info will disappear in 3 secs
            alert and warn will stay
     */
    info = function(message) {
        var alert, timer;
        alert = showAlert(message, 'info');
        if (alert.data('timer')) {
            clearTimeout(alert.data('timer'));
        }
        timer = setTimeout(function() {
            alert.alert('close');
        }, 6000);
        return alert.data('timer', timer);
    };
    alert = function(message) {
        return showAlert(message, 'warning');
    };
    warn = function(message) {
        return showAlert(message, 'danger');
    };
    window.onerror = function(msg, url, line, col, error) {
        var err_msg, extra;
        if (typeof msg !== 'undefined') {
            extra = !col ? "" : ", column: " + col;
            extra += !error ? "" : " (" + error + ")";
            err_msg = "Error: " + msg + "<br><b>url: " + url + ", line: " + line + extra + "</b>";
            Dashboard.warn(err_msg);
        }
        return false;
    };
    Error = function(msg) {
        var error;
        error = new TypeError(msg);
        Dashboard.warn(error);
        throw "Stop Dashboard Execution";
    };

    /*
            Dashboard Modal
     */
    Modal = function(title, content, type, value) {
        var modal, template;
        if (type == null) {
            type = 'primary';
        }
        if (value == null) {
            value = 'OK';
        }
        template = Dashboard.Templates.modal({
            title: title,
            content: content,
            type: type,
            value: value
        });
        modal = $(template).appendTo('body');
        return modal.modal('show').on('hidden.bs.modal', function() {
            return modal.remove();
        });
    };
    Dashboard.setVariable = setVariable;
    Dashboard.init = init;
    Dashboard.updateComponent = updateComponent;
    Dashboard.resetAll = resetAll;
    Dashboard.getVariable = getVariable;
    Dashboard.getAllVariables = getAllVariables;
    Dashboard.resetComponent = resetComponent;
    Dashboard.alert = alert;
    Dashboard.warn = warn;
    Dashboard.info = info;
    Dashboard.Error = Error;
    Dashboard.modal = Modal;
    _.templateSettings = {
        interpolate: /\{\{(.*?)\}\}/g
    };
    Dashboard.Templates = {};
    Dashboard.Templates.select = '<select {{ multiple }} class="form-control"></select>';
    Dashboard.Templates.option = '<option value="{{ value }}">{{ display }}</option>';
    Dashboard.Templates.select2 = '<input type="hidden" style="width:100%">';
    Dashboard.Templates.datepicker = '<div class="input-group"> <input name="date" class="form-control sb-disable-close"> <span class="input-group-addon pointer"> <i class="glyphicon glyphicon-calendar"></i> </span> </div>';
    Dashboard.Templates.daterangepicker = '<div class="input-group"> <input name="daterange" class="form-control sb-disable-close"> <span class="input-group-addon pointer"> <i class="glyphicon glyphicon-calendar"></i> </span> </div>';
    Dashboard.Templates.button = '<button class="btn {{ classes }}">{{ display }}</button>';
    Dashboard.Templates.chart = '<div class="chart"></div>';
    Dashboard.Templates.chartMenu = '<div class="print-menu group"> <div class="btn-group"> <button type="button" class="btn btn-default dropdown-toggle" data-toggle="dropdown"> {{ label }} <span class="caret"></span> </button> <ul class="dropdown-menu" role="menu"></ul> </div> </div>';
    Dashboard.Templates.chartMenuItem = '<li><a href="#" id="{{ id }}">{{ display }}</a></li>';
    Dashboard.Templates.chartMenuItemSeparator = '<li class="divider"></li>';
    Dashboard.Templates.alert = '<div class="alert alert-{{type}} alert-fixed-{{position}} fade in"> <button type="button" class="close" data-dismiss="alert"> <span aria-hidden="true">&times;</span> <span class="sr-only">Close</span> </button> {{message}} </div>';
    Dashboard.Templates.modals = '<div id="loading-panel" class="modal fade" data-backdrop="static"> <div   class="modal-dialog"> <div  class="modal-content"> <div class="modal-header"> <h4 class="modal-title"><b>Updating</b><span class="glyphicon glyphicon-repeat rotate pull-right"></span></h4> </div> <div class="modal-body"> <p><b>Please wait while dashboard is being updated.</b></p> <p class="">Pending requests: <span id="request-count">0</span></p> </div> <div class="modal-footer"> <p class="pull-left text-danger">Time elapsed: <span id="elpased_time">0:00</span></p> <input type="button" id="cancel_all_requests" class="btn btn-danger" data-dismiss="modal" value="Cancel"> </div> </div> </div> </div> <div id="error-panel" class="modal fade"> <div   class="modal-dialog"> <div  class="modal-content"> <div class="modal-header"> <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button> <h4 class="modal-title text-danger"><b>ERROR OCCURED</b></h4> </div> <div class="modal-body"> <p><b>An Error Occurred</b></p> <p class="text-danger"><span id="error-generated"></span></p> </div> <div class="modal-footer"> <input type="button" class="btn btn-warning" value="OK" data-dismiss="modal"> </div> </div> </div> </div>';
    Dashboard.Templates.modal = '<div id="dashboard-modal" class="modal fade" data-backdrop="static"> <div class="modal-dialog"> <div class="modal-content"> <div class="modal-header"> <h4 class="modal-title">{{ title }}</h4> </div> <div class="modal-body"> {{ content }} </div> <div class="modal-footer"> <input type="button" class="btn btn-{{ type }}" data-dismiss="modal" value="{{ value }}"> </div> </div> </div> </div>';
    Dashboard.Templates.table = '<table class="table {{ classes }}"> <thead><tr></tr></thead> <tbody></tbody> </table>';
    for (template in Dashboard.Templates) {
        if (Dashboard.Templates.hasOwnProperty(template)) {
            Dashboard.Templates[template] = _.template(Dashboard.Templates[template.trim()]);
        }
    }
    Dashboard.Defaults = {};
    Dashboard.Defaults.button = {
        display: 'Submit',
        classes: 'btn-primary'
    };
    Dashboard.Defaults.chart = {
        menuLabel: 'Actions'
    };
    Dashboard.Defaults.datepicker = {
        displayFormat: 'YYYY-MM-DD',
        outputFormat: 'YYYY-MM-DD'
    };
    Dashboard.Defaults.daterangepicker = {
        displayFormat: 'YYYY-MM-DD',
        outputFormat: 'YYYY-MM-DD'
    };
    Dashboard.Defaults.select2 = {
        placeholder: 'Select an option'
    };
    select = Backbone.View.extend({
        template: Dashboard.Templates.select,
        initialize: function() {
            this.options = this.model.attributes.options;
            this.timer = false;
            this.timeout = 0;
            if (typeof this.options.display !== 'string') {
                Dashboard.Error("options.display is not specified for " + this.model.attributes.name);
            }
            if (typeof this.options.value !== 'string') {
                Dashboard.Error("options.value is not specified for " + this.model.attributes.name);
            }
            if (_.isBoolean(this.options.multiple) && this.options.multiple === true) {
                this.options.multiple = 'multiple';
                this.timeout = 1000;
            } else {
                this.options.multiple = '';
            }
            this.listenTo(this.model, 'change:_data', this.render);
            if (this.model.attributes._subjects !== null) {
                this.model.attributes._subjects.on('change', this.model.update);
            }
        },
        onClose: function() {
            this.$el.empty();
            this.$el.off();
            if (this.model.attributes._params !== null) {
                this.unstickit();
            }
            if (this.model.attributes._subjects !== null) {
                this.model.attributes._subjects.off();
            }
        },
        bindings: {
            "select": {
                observe: "value",
                events: ['select.update'],
                onGet: function(value) {
                    if ($.isArray(value)) {
                        value = _.map(value, function(num) {
                            return num.toString();
                        });
                        return value;
                    } else {
                        return value.toString();
                    }
                }
            }
        },
        render: function() {
            var entries, entry, that, _i, _len;
            template = $(this.template(this.options));
            entries = JSON.parse(this.model.attributes._data);
            if (entries !== null && entries.hasOwnProperty('data')) {
                entries = entries.data;
                for (_i = 0, _len = entries.length; _i < _len; _i++) {
                    entry = entries[_i];
                    template.append(Dashboard.Templates.option({
                        value: entry[this.options.value],
                        display: entry[this.options.display]
                    }));
                }
            }
            this.$el.empty().append(template);
            that = this;
            this.$el.on("change", "select", function(event) {
                var update;
                update = $.Event('select.update');
                if (that.timer) {
                    clearTimeout(that.timer);
                }
                that.timer = setTimeout(function() {
                    return $(event.target).trigger(update);
                }, that.timeout);
            });
            if (this.model.attributes._params !== null) {
                this.stickit(this.model.attributes._params.at(0));
            }
            return this;
        }
    });
    Dashboard.Components.select = select;
    select2 = Backbone.View.extend({
        template: Dashboard.Templates.select2,
        initialize: function() {
            this.options = this.model.attributes.options;
            this.timer = false;
            this.timeout = 0;
            if (typeof this.options.display !== 'string') {
                Dashboard.Error("options.display is not specified for " + this.model.attributes.name);
            }
            if (typeof this.options.value !== 'string') {
                Dashboard.Error("options.value is not specified for " + this.model.attributes.name);
            }
            this.options.multiple = !!this.options.multiple;
            if (this.options.multiple) {
                this.timeout = 1000;
            }
            this.listenTo(this.model, 'change:_data', this.render);
            if (this.model.attributes._subjects !== null) {
                this.model.attributes._subjects.on('change', this.model.update);
            }
        },
        onClose: function() {
            this.$('input[type=hidden]').select2("destroy");
            this.$el.empty();
            this.$el.off();
            if (this.model.attributes._params !== null) {
                this.unstickit();
            }
            if (this.model.attributes._subjects !== null) {
                this.model.attributes._subjects.off();
            }
        },
        bindings: {
            "input[type=hidden]": {
                observe: "value",
                events: ['select2.update'],
                update: function($el, val, model, options) {
                    $el.select2('val', val);
                },
                getVal: function($el, event, options) {
                    return $el.select2('val');
                }
            }
        },
        render: function() {
            var entries, formatedData, that;
            template = $(this.template());
            entries = JSON.parse(this.model.attributes._data);
            formatedData = [];
            if (entries !== null && entries.hasOwnProperty('data')) {
                entries = entries.data;
                _.each(entries, function(obj) {
                    var temp;
                    temp = {};
                    temp['id'] = obj[this.options.value];
                    temp['text'] = obj[this.options.display];
                    formatedData.push(temp);
                }, this);
            }
            this.$el.empty().append(template);
            that = this;
            this.$('input[type=hidden]').select2({
                placeholder: this.options.placeholder || Dashboard.Defaults.select2.placeholder,
                width: 'resolve',
                closeOnSelect: false,
                data: formatedData,
                multiple: this.options.multiple
            }).on("change", function(event) {
                var update;
                update = $.Event('select2.update');
                if (that.timer) {
                    clearTimeout(that.timer);
                }
                that.timer = setTimeout(function() {
                    return $(event.target).trigger(update);
                }, that.timeout);
            });
            if (this.model.attributes._params !== null) {
                this.stickit(this.model.attributes._params.at(0));
            }
            return this;
        }
    });
    Dashboard.Components.select2 = select2;
    datepicker = Backbone.View.extend({
        template: Dashboard.Templates.datepicker,
        initialize: function() {
            this.options = this.model.attributes.options;
            if (typeof this.options.displayFormat !== 'string' || this.options.displayFormat === '') {
                Dashboard.info("Using default displayFormat as " + Dashboard.Defaults.datepicker.displayFormat + " for <b>" + this.model.attributes.name + "</b>");
                this.options.displayFormat = Dashboard.Defaults.datepicker.displayFormat;
            }
            if (typeof this.options.outputFormat !== 'string' || this.options.outputFormat === '') {
                Dashboard.info("Using default outputFormat as " + Dashboard.Defaults.datepicker.outputFormat + " for <b>" + this.model.attributes.name + "</b>");
                this.options.outputFormat = Dashboard.Defaults.datepicker.outputFormat;
            }
            this.model.update = null;
        },
        events: {
            "click .input-group-addon": 'triggerFocus'
        },
        onClose: function() {
            console.log(this.$('input[name=date]').data('daterangepicker'));
            this.$el.off();
            this.$el.empty();
            if (this.model.attributes._params !== null) {
                this.unstickit();
            }
        },
        triggerFocus: function() {
            this.$('input[name=date]').focus();
        },
        bindings: {
            "input[name=date]": {
                observe: "value",
                onGet: 'toDisplay',
                onSet: 'toModel',
                events: ['hide.daterangepicker'],
                update: function($el, val, model, options) {
                    $el.data('daterangepicker').setStartDate(val);
                    $el.data('daterangepicker').setEndDate(val);
                    $el.val(val);
                }
            }
        },
        toDisplay: function(value) {
            return moment(value, this.options.outputFormat).format(this.options.displayFormat);
        },
        toModel: function(value) {
            return moment(value, this.options.displayFormat).format(this.options.outputFormat);
        },
        render: function() {
            template = $(this.template());
            this.$el.empty().append(template);
            template = null;
            this.$('input[name=date]').daterangepicker({
                format: this.options.displayFormat,
                showDropdowns: true,
                singleDatePicker: true
            });
            if (this.model.attributes._params !== null) {
                this.stickit(this.model.attributes._params.at(0));
            }
            return this;
        }
    });
    Dashboard.Components.datepicker = datepicker;
    daterangepicker = Backbone.View.extend({
        template: Dashboard.Templates.daterangepicker,
        initialize: function() {
            this.options = this.model.attributes.options;
            this.model.update = null;
            if (typeof this.options.displayFormat !== 'string' || this.options.displayFormat === '') {
                Dashboard.info("Using default displayFormat as " + Dashboard.Defaults.daterangepicker.displayFormat + " for <b>" + this.model.attributes.name + "</b>");
                this.options.displayFormat = Dashboard.Defaults.daterangepicker.displayFormat;
            }
            if (typeof this.options.outputFormat !== 'string' || this.options.outputFormat === '') {
                Dashboard.info("Using default outputFormat as " + Dashboard.Defaults.daterangepicker.outputFormat + " for <b>" + this.model.attributes.name + "</b>");
                this.options.outputFormat = Dashboard.Defaults.daterangepicker.outputFormat;
            }
            this.options.separator = this.options.separator || ' to ';
        },
        onClose: function() {
            console.log("removing daterangepicker");
            this.$el.empty();
            this.$el.off();
            if (this.model.attributes._params !== null) {
                this.unstickit();
            }
        },
        events: {
            "click .input-group-addon": 'triggerFocus'
        },
        triggerFocus: function() {
            this.$('input[name=daterange]').focus();
        },
        startDatebindings: {
            "input[name=daterange]": {
                observe: "value",
                onGet: 'toStartDateDisplay',
                onSet: 'toStartDateModel',
                events: ['apply.daterangepicker'],
                update: function($el, val, model, options) {
                    var display;
                    if (typeof this.model.get('_params').at(1) !== 'undefined') {
                        display = this.model.get('_params').at(1).get("value");
                    } else {
                        display = '';
                    }
                    display = val + this.options.separator + moment(display, this.options.outputFormat).format(this.options.displayFormat);
                    $el.data('daterangepicker').setStartDate(val);
                    $el.val(display);
                }
            }
        },
        endDatebindings: {
            "input[name=daterange]": {
                observe: "value",
                onGet: 'toEndDateDisplay',
                onSet: 'toEndDateModel',
                events: ['apply.daterangepicker'],
                update: function($el, val, model, options) {
                    var display;
                    if (typeof this.model.get('_params').at(0) !== 'undefined') {
                        display = this.model.get('_params').at(0).get("value");
                    } else {
                        display = '';
                    }
                    display = moment(display, this.options.outputFormat).format(this.options.displayFormat) + this.options.separator + val;
                    $el.data('daterangepicker').setEndDate(val);
                    $el.val(display);
                }
            }
        },
        toStartDateDisplay: function(value) {
            return moment(value, this.options.outputFormat).format(this.options.displayFormat);
        },
        toStartDateModel: function(value) {
            value = value.split(this.options.separator)[0];
            return moment(value, this.options.displayFormat).format(this.options.outputFormat);
        },
        toEndDateDisplay: function(value) {
            return moment(value, this.options.outputFormat).format(this.options.displayFormat);
        },
        toEndDateModel: function(value) {
            value = value.split(this.options.separator)[1];
            return moment(value, this.options.displayFormat).format(this.options.outputFormat);
        },
        render: function() {
            var opts, _ref;
            template = $(this.template());
            this.$el.empty().append(template);
            opts = {
                format: this.options.displayFormat,
                showDropdowns: true,
                separator: this.options.separator,
                timePicker: !!this.options.timePicker,
                timePickerIncrement: _.isNumber(this.options.range) && (0 < (_ref = this.options.range) && _ref < 60) ? this.options.timePickerIncrement : 0
            };
            if (_.isBoolean(this.options.ranges) && this.options.ranges === true) {
                opts.ranges = Dashboard.Defaults.dateranges;
            } else if (_.isObject(this.options.ranges)) {
                opts.ranges = this.options.ranges;
            }
            this.$('input[name=daterange]').daterangepicker(opts);
            template = opts = null;
            if (this.model.attributes._params !== null) {
                if (typeof this.model.attributes._params.at(0) !== 'undefined') {
                    this.stickit(this.model.attributes._params.at(0), this.startDatebindings);
                }
                if (typeof this.model.attributes._params.at(1) !== 'undefined') {
                    this.stickit(this.model.attributes._params.at(1), this.endDatebindings);
                }
            }
            return this;
        }
    });
    Dashboard.Components.daterangepicker = daterangepicker;
    button = Backbone.View.extend({
        template: Dashboard.Templates.button,
        initialize: function() {
            var triggers;
            this.options = this.model.attributes.options;
            this.model.update = _.bind(this.trigger, this);
            this.options.classes = this.options.classes || Dashboard.Defaults.button.classes;
            this.options.display = this.options.display || Dashboard.Defaults.button.display;
            triggers = this.model.attributes.triggers || [];
            this.toTrigger = new Backbone.VirtualCollection(Dashboard.components, {
                filter: function(model) {
                    return $.inArray(model.get('id'), triggers) >= 0;
                }
            });
            _.bindAll(this, 'trigger');
        },
        events: {
            "click button": 'trigger'
        },
        trigger: function() {
            if (typeof this.toTrigger !== 'undefined') {
                this.toTrigger.each(function(model) {
                    if (typeof model.update === 'function') {
                        model.update();
                    }
                });
            }
        },
        render: function() {
            template = $(this.template(this.options));
            this.$el.empty().append(template);
            return this;
        }
    });
    Dashboard.Components.button = button;
    chart = Backbone.View.extend({
        template: Dashboard.Templates.chart,
        initialize: function() {
            this.options = this.model.attributes.options;
            // if (typeof this.model.attributes.vf === 'undefined') {
            //   Dashboard.Error("vf information is required for " + this.model.attributes.name + " chart");
            // }
            this.listenTo(this.model, 'change:_data', this.render);
            this.model.update = function() {
                var data, key, request, that, value, _ref;
                that = this;
                data = {};
                data.dir = window.DashboardGlobals.folderpath;
                _ref = this.attributes.requestParameters;
                for (key in _ref) {
                    if (!__hasProp.call(_ref, key)) continue;
                    value = _ref[key];
                    data[key] = Dashboard.getVariable(value);
                }
                data.map = this.attributes.map;
                // if (typeof this.attributes.vf !== 'undefined') {
                //     data.vf_id = this.attributes.vf.id;
                //     data.vf_file = this.attributes.vf.file;
                // } else {
                //     Dashboard.warn("vf property isn't defined for " + this.attributes.name);
                //     return false;
                // }
                request = $.ajax({
                    type: 'POST',
                    url: window.DashboardGlobals.chartingService,
                    data: {
                        "data": JSON.stringify(data).replace(/"(\[)\\/g, "$1").replace(/\\("\])"/g, "$1")
                    }
                });
                request.identifier = this.attributes.name;
                request.done(function(data, textStatus, xhr) {
                    that.set("_data", data);
                });
                return request.always(function() {});
            };
            if (this.model.attributes._subjects !== null) {
                this.model.attributes._subjects.on('change', _.bind(this.model.update, this.model));
            }
        },
        render: function() {
            var data, menu, menuRoot;
            if (this.model.get("_data") === null) {
                return this;
            }
            data = JSON.parse(this.model.get("_data"));
            console.log(data);
           
           //  if (data.hasOwnProperty('id') && data.hasOwnProperty('script')) {
                 template = $(this.template());
                 if (_.isArray(this.options.actions) && this.options.actions.length > 0) {
                     menu = $(Dashboard.Templates.chartMenu({
                         label: this.options.menuLabel || Dashboard.Defaults.chart.menuLabel
                     }));
                     menuRoot = menu.find('ul.dropdown-menu');
                     _.each(this.options.actions, function(group, index) {
                         var key, value;
                         if (index > 0) {
                             menuRoot.append(Dashboard.Templates.chartMenuItemSeparator());
                         }
                         for (key in group) {
                             if (!__hasProp.call(group, key)) continue;
                             value = group[key];
                             menuRoot.append(Dashboard.Templates.chartMenuItem({
                                 id: key,
                                 display: value
                             }));
                         }
                     });
                     template.prepend(menu);
                }
                 this.$el.empty().append(template);
                 if (_.isFunction(this.model.attributes.customScript)) {
                     this.model.attributes.customScript.call(null, data);
                 }
//            //}
            return this;
        }
    });
    Dashboard.Components.chart = chart;
    table = Backbone.View.extend({
        template: Dashboard.Templates.table,
        initialize: function() {
            var classes;
            this.options = this.model.attributes.options;
            classes = [];
            if (this.options.stripped) {
                classes.push('table-stripped');
            }
            this.options.classes = classes.join(" ");
            this.listenTo(this.model, 'change:_data', this.render);
            if (this.model.attributes._subjects !== null) {
                this.model.attributes._subjects.on('change', this.model.update);
            }
        },
        render: function() {
            var columns, entries, entry, h, key, row, tbody, thead, _i, _j, _k, _len, _len1, _len2;
            template = $(this.template(this.options));
            entries = JSON.parse(this.model.attributes._data);
            if (entries !== null && entries.hasOwnProperty('data') && entries.data.length > 0) {
                thead = [];
                tbody = [];
                entries = entries.data;
                columns = $.isArray(this.options.columns) ? this.options.columns : Object.keys(entries[0]);
                for (_i = 0, _len = entries.length; _i < _len; _i++) {
                    entry = entries[_i];
                    row = ["<tr>"];
                    for (_j = 0, _len1 = columns.length; _j < _len1; _j++) {
                        key = columns[_j];
                        row.push("<td>" + entry[key] + "</td>");
                    }
                    row.push("</tr>");
                    tbody.push(row.join(""));
                }
                for (_k = 0, _len2 = columns.length; _k < _len2; _k++) {
                    h = columns[_k];
                    thead.push("<th data-column-id=\"" + h + "\">" + h + "</th>");
                }
                template.find('thead > tr').html(thead.join("")).end().find('tbody').html(tbody.join(""));
            }
            this.$el.empty().html(template);
            this.$el.find('table').bootgrid();
            return this;
        }
    });
    Dashboard.Components.table = table;
    custom = Backbone.View.extend({
        initialize: function() {
            var element;
            element = this.$el;
            this.model.update = function() {
                var values;
                values = _.object(this.attributes._subjects.pluck('name'), this.attributes._subjects.pluck('value'));
                this.attributes.customScript.call(this, element, values);
            };
            if (this.model.attributes._subjects !== null) {
                this.model.attributes._subjects.on('change', _.bind(this.model.update, this.model));
            }
        },
        render: function() {
            return this;
        }
    });
    Dashboard.Components.custom = custom;
})(window, document, jQuery || $HDI, _ || _HDI, Backbone || BackboneHDI, Dashboard);
