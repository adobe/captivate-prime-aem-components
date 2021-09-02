/*
 * Copyright 2021 Adobe. All rights reserved. This file is licensed to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance with the License. You
 * may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR REPRESENTATIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */

(function (document, $) {

    "use strict";
    var GLOBAL_CONFIG_CP_PATH = "/conf/global/captivate-prime/";
    var HELPX_PREFIX = "cphelpx";
    
    var COMMAND_URL= Granite.HTTP.externalize("/bin/wcmcommand");
    var REPLICATION_URL = Granite.HTTP.externalize("/bin/replicate.json");

    var deleteConfigActivatorClass = ".cp-widget-configurations-delete-activator",
        publishConfigActivatorClass = ".cp-widget-configurations-publish-activator",
        unpublishConfigActivatorClass = ".cp-widget-configurations-unpublish-activator";

    $(document).on("click", deleteConfigActivatorClass, deleteConfigMessage);
    $(document).on("click", publishConfigActivatorClass, publishConfigMessage);
    $(document).on("click", unpublishConfigActivatorClass, unpublishConfigMessage);
    
    var ui = $(window).adaptTo("foundation-ui");
    
    $(document).ready(function() {
        $("input[name^=" + HELPX_PREFIX + "]").each(function (e) {
            var label = $(this).prev("div .coral-Form-fieldwrapper").first().find("label").first();
            var hrefVal = $(this).val();
            label.addClass("label-helpx");
            var coralIcon = new Coral.Icon().set({
                icon: "help",
                size: "S"
            });
            coralIcon.on("click", function(e) {
               var href = $(e.target).attr("data-href");
                window.open(href, "_blank");
            });
            $(coralIcon).attr("data-href", hrefVal);
            $(coralIcon).addClass("icon-helpx");
            label.after(coralIcon);
        });
    });
    
    function publishConfigMessage() {
        ui.wait();
        
        var item = $(".foundation-selections-item");
        if (item.length) {
            
            var itemPath = item.data("foundation-collection-item-id");
            var publishUrl = Granite.HTTP.externalize("/" + itemPath + ".publishConfig.html?itemPath=" + itemPath);

            $.ajax({
                url: publishUrl,
                type: "POST"
            }).done(function(data, textStatus, jqXHR) {
                window.location.reload();
            }).fail(function(jqXHR, textStatus, errorThrown) {
                var message = Granite.I18n.get("Exception in Publishing the config");
                ui.alert(Granite.I18n.get("Error"), message, "error");
            }).always(function() {
                ui.clearWait();
            });
        }
    }
    
    function unpublishConfigMessage() {
        ui.wait();
        
        var paths=[];
        var items = $(".foundation-selections-item");
        if (items.length) {

            items.each(function(i) {
                var item = $(this);
                var itemPath = item.data("foundation-collection-item-id");
                paths.push(GLOBAL_CONFIG_CP_PATH + itemPath);

            });

            $.ajax({
                url: REPLICATION_URL,
                type: "POST",
                data: {
                    _charset_: "UTF-8",
                    cmd: "Deactivate",
                    path: paths
                }
            }).done(function(data, textStatus, jqXHR) {
                window.location.reload();
            }).fail(function(jqXHR, textStatus, errorThrown) {
                var message = Granite.I18n.get("Exception in Unpublishing the config");
                ui.alert(Granite.I18n.get("Error"), message, "error");
            }).always(function() {
                ui.clearWait();
            });
        }
    }

    function deleteConfigMessage(){
        var message = createEl("div");
        var intro = createEl("p").appendTo(message);
        var selections = $(".foundation-selections-item");
        intro.text(Granite.I18n.get("You are going to delete the following item:"));
 
        var list = [];
        var maxCount = Math.min(selections.length, 12);
        for (var i = 0, ln = maxCount; i < ln; i++) {
            var title = $(selections[i]).find(".foundation-collection-item-title").text();
            list.push(createEl("b").text(title).prop("outerHTML"));
        }

        createEl("p").html(list.join("<br>")).appendTo(message);

        var ui = $(window).adaptTo("foundation-ui");

        ui.prompt(getDeleteText(), message.html(), "notice", [{
            text: getCancelText()
        }, {
            text: getDeleteText(),
            warning: true,
            handler: function() {
                unpublishConfigMessage();
                deleteConfig();
            }
        }]);
    }

    function deleteConfig() {
        
        var paths=[];
        var items = $(".foundation-selections-item");
        if (items.length) {

            items.each(function(i) {
                var item = $(this);
                var itemPath = item.data("foundation-collection-item-id");
                paths.push(GLOBAL_CONFIG_CP_PATH + itemPath);

            });

            $.ajax({
                url: COMMAND_URL,
                type: "POST",
                async: false,
                data: {
                    _charset_: "UTF-8",
                    cmd: "deletePage",
                    path: paths,
                    force: false,
                    checkChildren: true
                }
                })
                .done(function(data, textStatus, jqXHR) {
                    window.location.reload();
                });
        }
    }
    
      // Deletion related methods and click handler
    var deleteText;
    function getDeleteText() {
        if (!deleteText) {
            deleteText = Granite.I18n.get("Delete");
        }
        return deleteText;
    }

    var cancelText;
    function getCancelText() {
        if (!cancelText) {
            cancelText = Granite.I18n.get("Cancel");
        }
        return cancelText;
    }

    function createEl(name) {
        return $(document.createElement(name));
    }

})(document, jQuery);