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

(function(document, $) {
    
    "use strict";
    
    var WIDET_CONFIG_DATA = "data-cp-widget-configs",
        WIDGET_REF_DATA = "cp-widget-ref",
        DATA_RUN_MODE = "cp-runmode",
        WIDGET_SRC_URL_DATA = "cp-widget-src-url",
        WIDGET_WRAPPER_DIV = ".cpWidgetWrapperDiv",
        WIDGET_COMMUNICATOR_URL = "cp-widget-communicator-url";
    
    var scriptLoaded = false;

    $(document).ready(function() {
        
        loadWidgetCommunicatorScript().then(function (data) {
            
            $(WIDGET_WRAPPER_DIV).each(function (e) {

			var rendered = $(this).attr("rendered");

            if (rendered === "true") {
            	return true;
            }

			$(this).attr("rendered", true);
            var configsDivWrapper = $(this).find("div").first();
            var widgetConfigObj = {};
            var widgetConfigs = configsDivWrapper.attr(WIDET_CONFIG_DATA);
            if (widgetConfigs)
            {
               widgetConfigObj = JSON.parse(widgetConfigs);
            }
            
            var containerObj = $(this).get(0);
            var ref = configsDivWrapper.data(WIDGET_REF_DATA);
             
            var isAuthorMode = configsDivWrapper.data(DATA_RUN_MODE) === "author" ? true : false;

		    let primeWidget = window.primecommunicator.createWidget({
                                previewMode: isAuthorMode,
                                host: "aem",
                                ref: ref,
                                config: widgetConfigObj,
                                container: containerObj,
                                initialWidth: 300,
                                initialHeight: 200,
                                autoFitWidth: false,
                                autoFitHeight: true
                            }); 
        }); 
            
        });
     });
    
    function loadWidgetCommunicatorScript() {
        
        if (!scriptLoaded) {
            var configsDivWrapper = $(WIDGET_WRAPPER_DIV).first().find("div").first();
            var url = configsDivWrapper.data(WIDGET_COMMUNICATOR_URL);
            return $.ajax({
                url: url,
                async: false,
                dataType: "script"
            }).done(function(data, textStatus, jqXHR) {
                scriptLoaded = true;
            }).fail(function( jqxhr, settings, exception ) {
                if (typeof console !== 'undefined' && console.error) {
                    console.error("error in loading widget communicator script");
                }
            });
        }
    }
    
})(document, jQuery);