!function(t){function e(n){if(r[n])return r[n].exports;var a=r[n]={exports:{},id:n,loaded:!1};return t[n].call(a.exports,a,a.exports,e),a.loaded=!0,a.exports}var r={};return e.m=t,e.c=r,e.p="",e(0)}([function(t,e,r){t.exports=r(1)},function(t,e,r){"use strict";function n(t){return t&&t.__esModule?t:{"default":t}}var a=r(2),i=n(a),o=r(24),s=n(o);$(function(){App.events.sub("hmt.tab.shown",function(){i["default"].plugin(".js-set-credits"),$('[data-toggle="tooltip"]').tooltip(),s["default"].plugin(".js-set-api")})})},function(t,e,r){"use strict";function n(t){return t&&t.__esModule?t:{"default":t}}Object.defineProperty(e,"__esModule",{value:!0});var a=r(3),i=n(a),o=r(4),s=n(o),u=r(23),c=n(u),l=function(){function t(e){(0,i["default"])(this,t),this.$root=$(e),this.locals=this._getDom(),this.brandId=this.$root.data("brand-id"),this.validation=new c["default"](this.locals.$input),this._assignEvents()}return(0,s["default"])(t,[{key:"_getDom",value:function(){var t=this.$root;return{$activateBtn:t.find("[data-setcredit-activate]"),$deActivateBtn:t.find("[data-setcredit-deactivate]"),$form:t.find("[data-setcredit-form]"),$input:t.find("[data-setcredit-input]"),$errors:t.find("[data-setcredit-errors]")}}},{key:"_assignEvents",value:function(){var t=this;this.$root.on("click","[data-setcredit-activate]",this._onClickActivate.bind(this)).on("click","[data-setcredit-deactivate]",this._onClickDeActivate.bind(this)).on("submit","[data-setcredit-form]",this._onClickSaveCredit.bind(this)).on("input","[data-setcredit-input]",function(e){t.$root.removeClass("b-setcredit_state_error"),t.locals.$errors.text("")})}},{key:"_onClickActivate",value:function(t){var e=this;t.preventDefault(),e._sendActivate(e.brandId).done(function(){e.$root.addClass("b-setcredit_state_active")})}},{key:"_onClickDeActivate",value:function(t){var e=this;t.preventDefault(),e._sendDeActivate(e.brandId).done(function(){e.$root.removeClass("b-setcredit_state_active")})}},{key:"_onClickSaveCredit",value:function(t){var e=this;t.preventDefault(),e.isFormValid()&&e._sendFormData().done(function(){success("Credit limit was updated"),e.$root.addClass("b-setcredit_state_sended"),setTimeout(function(){e.$root.removeClass("b-setcredit_state_sended")},4500)}).fail(function(t){var r=$.parseJSON(t.responseText).data,n=e.validation.getErrorsText(r.errors);r.errors&&(e.locals.$error.text(n),e.$root.addClass("b-setcredit_state_error"),e.validation.setErrors(r.errors))})}},{key:"isFormValid",value:function(){var t=this.locals,e=t.$input.val()>0,r=!0,n="";return e||(r=!1,n+="Spending limit has to be above 0. We recommend set in 100.",this.validation._setError(t.$input)),r||(this.$root.addClass("b-setcredit_state_error"),t.$errors.text(n)),r}},{key:"_sendActivate",value:function(t){var e=jsRoutes.controllers.cm.brand.Credits.activate(t).url;return $.post(e,{brandid:t})}},{key:"_sendDeActivate",value:function(t){var e=jsRoutes.controllers.cm.brand.Credits.deactivate(t).url;return $.post(e,{brandid:t})}},{key:"_sendFormData",value:function(){var t=this.locals;return $.post(t.$form.attr("action"),{limit:t.$input.val()})}}],[{key:"plugin",value:function(e){var r=$(e);if(r.length)return r.each(function(e,r){var n=$(r),a=n.data("widget");a||(a=new t(r),n.data("widget",a))})}}]),t}();e["default"]=l},function(t,e){"use strict";e.__esModule=!0,e["default"]=function(t,e){if(!(t instanceof e))throw new TypeError("Cannot call a class as a function")}},function(t,e,r){"use strict";function n(t){return t&&t.__esModule?t:{"default":t}}e.__esModule=!0;var a=r(5),i=n(a);e["default"]=function(){function t(t,e){for(var r=0;r<e.length;r++){var n=e[r];n.enumerable=n.enumerable||!1,n.configurable=!0,"value"in n&&(n.writable=!0),(0,i["default"])(t,n.key,n)}}return function(e,r,n){return r&&t(e.prototype,r),n&&t(e,n),e}}()},function(t,e,r){t.exports={"default":r(6),__esModule:!0}},function(t,e,r){r(7);var n=r(10).Object;t.exports=function(t,e,r){return n.defineProperty(t,e,r)}},function(t,e,r){var n=r(8);n(n.S+n.F*!r(18),"Object",{defineProperty:r(14).f})},function(t,e,r){var n=r(9),a=r(10),i=r(11),o=r(13),s="prototype",u=function(t,e,r){var c,l,f,d=t&u.F,v=t&u.G,p=t&u.S,h=t&u.P,_=t&u.B,m=t&u.W,b=v?a:a[e]||(a[e]={}),$=b[s],y=v?n:p?n[e]:(n[e]||{})[s];v&&(r=e);for(c in r)l=!d&&y&&void 0!==y[c],l&&c in b||(f=l?y[c]:r[c],b[c]=v&&"function"!=typeof y[c]?r[c]:_&&l?i(f,n):m&&y[c]==f?function(t){var e=function(e,r,n){if(this instanceof t){switch(arguments.length){case 0:return new t;case 1:return new t(e);case 2:return new t(e,r)}return new t(e,r,n)}return t.apply(this,arguments)};return e[s]=t[s],e}(f):h&&"function"==typeof f?i(Function.call,f):f,h&&((b.virtual||(b.virtual={}))[c]=f,t&u.R&&$&&!$[c]&&o($,c,f)))};u.F=1,u.G=2,u.S=4,u.P=8,u.B=16,u.W=32,u.U=64,u.R=128,t.exports=u},function(t,e){var r=t.exports="undefined"!=typeof window&&window.Math==Math?window:"undefined"!=typeof self&&self.Math==Math?self:Function("return this")();"number"==typeof __g&&(__g=r)},function(t,e){var r=t.exports={version:"2.2.0"};"number"==typeof __e&&(__e=r)},function(t,e,r){var n=r(12);t.exports=function(t,e,r){if(n(t),void 0===e)return t;switch(r){case 1:return function(r){return t.call(e,r)};case 2:return function(r,n){return t.call(e,r,n)};case 3:return function(r,n,a){return t.call(e,r,n,a)}}return function(){return t.apply(e,arguments)}}},function(t,e){t.exports=function(t){if("function"!=typeof t)throw TypeError(t+" is not a function!");return t}},function(t,e,r){var n=r(14),a=r(22);t.exports=r(18)?function(t,e,r){return n.f(t,e,a(1,r))}:function(t,e,r){return t[e]=r,t}},function(t,e,r){var n=r(15),a=r(17),i=r(21),o=Object.defineProperty;e.f=r(18)?Object.defineProperty:function(t,e,r){if(n(t),e=i(e,!0),n(r),a)try{return o(t,e,r)}catch(s){}if("get"in r||"set"in r)throw TypeError("Accessors not supported!");return"value"in r&&(t[e]=r.value),t}},function(t,e,r){var n=r(16);t.exports=function(t){if(!n(t))throw TypeError(t+" is not an object!");return t}},function(t,e){t.exports=function(t){return"object"==typeof t?null!==t:"function"==typeof t}},function(t,e,r){t.exports=!r(18)&&!r(19)(function(){return 7!=Object.defineProperty(r(20)("div"),"a",{get:function(){return 7}}).a})},function(t,e,r){t.exports=!r(19)(function(){return 7!=Object.defineProperty({},"a",{get:function(){return 7}}).a})},function(t,e){t.exports=function(t){try{return!!t()}catch(e){return!0}}},function(t,e,r){var n=r(16),a=r(9).document,i=n(a)&&n(a.createElement);t.exports=function(t){return i?a.createElement(t):{}}},function(t,e,r){var n=r(16);t.exports=function(t,e){if(!n(t))return t;var r,a;if(e&&"function"==typeof(r=t.toString)&&!n(a=r.call(t)))return a;if("function"==typeof(r=t.valueOf)&&!n(a=r.call(t)))return a;if(!e&&"function"==typeof(r=t.toString)&&!n(a=r.call(t)))return a;throw TypeError("Can't convert object to primitive value")}},function(t,e){t.exports=function(t,e){return{enumerable:!(1&t),configurable:!(2&t),writable:!(4&t),value:e}}},function(t,e,r){"use strict";function n(t){return t&&t.__esModule?t:{"default":t}}Object.defineProperty(e,"__esModule",{value:!0});var a=r(3),i=n(a),o=r(4),s=n(o),u=function(){function t(e){(0,i["default"])(this,t),this.$controls=e,this.arrErrors=[],this._assignEvents()}return(0,s["default"])(t,[{key:"_assignEvents",value:function(){var t=this;this.$controls.on("input change",function(e){var r=$(e.currentTarget);t._validateImmediate(r),t._removeError(r)})}},{key:"_validateImmediate",value:function(t){t.hasClass("type-numeric")&&t.val(t.val().replace(/[^\d]+/g,""))}},{key:"isValidInputs",value:function(){var t=this,e=this.$controls,r=0;return e.each(function(e,n){var a=$(n);t._isValidInput(a)||(r+=1)}),Boolean(!r)}},{key:"_isValidInput",value:function(t){var e=$.trim(t.val());return e||t.hasClass("type-optional")?t.hasClass("type-email")&&!this._isValidEmail(e)?(this._setError(t,"Email is not valid"),!1):!0:(this._setError(t,"Empty"),!1)}},{key:"_isValidEmail",value:function(t){var e=/^(([^<>()\[\]\\.,;:\s@"]+(\.[^<>()\[\]\\.,;:\s@"]+)*)|(".+"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/;return e.test(t)}},{key:"_setError",value:function(t,e){var r=arguments.length<=2||void 0===arguments[2]?!0:arguments[2],n=t.parent(),a=n.find(".b-error");a.length||(n.addClass("b-error_show"),r&&$('<div class="b-error" />').text(e).appendTo(n),this.arrErrors.push({name:t.attr("name"),error:e}))}},{key:"_removeError",value:function(t){var e=t.parent();e.removeClass("b-error_show").find(".b-error").remove(),this.arrErrors=this.arrErrors.filter(function(e){return e.name!==t.attr("name")})}},{key:"setErrors",value:function(t){var e=this,r=arguments.length<=1||void 0===arguments[1]?!0:arguments[1];t.forEach(function(t){var n=e.$controls.filter('[name="'+t.name+'"]').first();n.length&&e._setError(n,t.error,r)})}},{key:"getErrorsText",value:function(t){var e=t||this.arrErrors,r="";return e.forEach(function(t){var e=t.name[0].toUpperCase()+t.name.substr(1);r+=e+": "+t.error+". "}),r}},{key:"getErrorsFull",value:function(t){var e=this,r=t||this.arrErrors,n=($("body"),"");return r.forEach(function(t){var r=e.$controls.filter('[name="'+t.name+'"]').first(),a=r.length?r.attr("title"):t.name;n+="<b>"+a+"</b>: "+t.error+".  <br><br>"}),n}},{key:"getFormData",value:function(){var t={};return this.$controls.map(function(e,r){var n=$(r),a=n.attr("name");a&&(t[a]=n.val())}),t}},{key:"removeErrors",value:function(){var t=this;this.$controls.each(function(e,r){var n=$(r);t._removeError(n)})}},{key:"getFormData",value:function(){var t={};return this.$controls.map(function(e,r){var n=$(r),a=n.attr("name");a&&(t[a]=n.val())}),t}},{key:"clearForm",value:function(){this.$controls.each(function(t,e){var r=$(e);r.attr("disabled")||r.val("")})}}]),t}();e["default"]=u},function(t,e,r){"use strict";function n(t){return t&&t.__esModule?t:{"default":t}}Object.defineProperty(e,"__esModule",{value:!0});var a=r(3),i=n(a),o=r(4),s=n(o),u=r(23),c=n(u),l=function(){function t(e){(0,i["default"])(this,t),this.$root=$(e),this.locals=this._getDom(),this.brandId=this.$root.data("brand-id"),this.formData={},this.formHelper=new c["default"](this.locals.$inputs),this._prepareView(this.locals.$inputs),this._saveFormData(this.locals.$inputs),this._assignEvents()}return(0,s["default"])(t,[{key:"_getDom",value:function(){var t=this.$root;return{$form:t.find("[data-setapi-form]"),$view:t.find("[data-setapi-view]"),$modal:t.find("[data-setapi-modal]"),$inputs:t.find(".b-apiform__input"),$error:t.find("[data-setapi-errors]")}}},{key:"_assignEvents",value:function(){this.$root.on("click","[data-setapi-activate]",this._onClickActivate.bind(this)).on("click","[data-setapi-promptbtn]",this._onClickShowPrompt.bind(this)).on("click","[data-setapi-specify]",this._onClickSpecify.bind(this)).on("click","[data-setapi-deacticate]",this._onClickDeactivate.bind(this)).on("submit","[data-setapi-form]",this._onClickSubmit.bind(this)).on("click","[data-setapi-form-save]",this._onClickSubmit.bind(this)).on("click","[data-setapi-form-cancel]",this._onClickFormCancel.bind(this)).on("click","[data-setapi-editform]",this._onClickEditForm.bind(this)).on("click",".b-apiview__link",this._onClickAddUrl.bind(this))}},{key:"_onClickActivate",value:function(t){t.preventDefault();var e=this,r=e.$root;e._sendActivate(e.brandId).done(function(){r.hasClass("b-setapi_state_active")||r.addClass("b-setapi_state_active"),success("API was successfully activated")})}},{key:"_onClickShowPrompt",value:function(t){t.preventDefault(),this.locals.$modal.modal("show")}},{key:"_onClickDeactivate",value:function(t){var e=this,r=e.$root;t.preventDefault(),t.stopPropagation(),e._sendDeactivate(e.brandId).done(function(){r.removeClass("b-setapi_state_active"),success("API was successfully deactivated")})}},{key:"_onClickSpecify",value:function(t){t.preventDefault();var e=this.$root;e.hasClass("b-setapi_state_form")||e.addClass("b-setapi_state_form")}},{key:"_onClickSubmit",value:function(t){var e=this;t.preventDefault();var r=this,n=this.locals.$inputs;if(r.isFormValid()){var a=this.formHelper.getFormData();r._sendUrlsData(r.brandId,a).done(function(){r.locals.$error.html(""),e._saveFormData(n),e._prepareView(n),e._showView(),success("You are successfully update urls")}).fail(function(t){var e=$.parseJSON(t.responseText).data,n=r.formHelper.getErrorsFull(e.errors);e.errors&&(r.formHelper.setErrors(e.errors),r.locals.$error.html(n))})}}},{key:"_onClickFormCancel",value:function(t){t.preventDefault(),this._restoreFormData(this.locals.$inputs),this.formHelper.removeErrors(),this.locals.$error.html(""),this._showView()}},{key:"_onClickEditForm",value:function(t){t.preventDefault(),this._saveFormData(this.locals.$inputs),this._showForm()}},{key:"_onClickAddUrl",value:function(t){t.preventDefault();var e=$(t.currentTarget).closest("[data-setapi-field]").data("setapi-field");this._showForm($.trim(e))}},{key:"_showForm",value:function(){var t=arguments.length<=0||void 0===arguments[0]?"":arguments[0],e=this.$root,r=this.locals;e.hasClass("b-setapi_state_form")||e.addClass("b-setapi_state_form").removeClass("b-setapi_state_view");var n=t?'input[name="'+t+'"]':"input";r.$form.find(n).first().trigger("focus")}},{key:"_showView",value:function(){var t=this.$root;t.hasClass("b-setapi_state_view")||t.addClass("b-setapi_state_view").removeClass("b-setapi_state_form")}},{key:"_restoreFormData",value:function(t){var e=this.formData;t.each(function(t,r){var n=$(r),a=e[n.attr("name")];n.val(a)})}},{key:"_saveFormData",value:function(t){var e={};t.each(function(t,r){var n=$(r),a=n.attr("name");a&&(e[a]=n.val())}),this.formData=e}},{key:"_prepareView",value:function(t){var e=this.locals.$view;t.each(function(t,r){var n=$(r),a=n.attr("name"),i=e.find('[data-setapi-field="'+a+'"]');if(!i.length)return!1;var o=n.val();$.trim(o)?i.removeClass("state_url").find(".b-apiview__text").text(o):i.addClass("state_url").find(".b-apiview__text").text("")})}},{key:"isFormValid",value:function(){var t=(this.locals,!0);return this.formHelper.isValidInputs()||(t=!1),t}},{key:"_sendActivate",value:function(t){var e=jsRoutes.controllers.cm.brand.API.activate(t).url;return $.post(e)}},{key:"_sendDeactivate",value:function(t){var e=jsRoutes.controllers.cm.brand.API.deactivate(t).url;return $.post(e)}},{key:"_sendUrlsData",value:function(t,e){var r=jsRoutes.controllers.cm.brand.API.update(t).url;return $.post(r,e)}}],[{key:"plugin",value:function(e){var r=$(e);if(r.length)return r.each(function(e,r){var n=$(r),a=n.data("widget");a||(a=new t(r),n.data("widget",a))})}}]),t}();e["default"]=l}]);