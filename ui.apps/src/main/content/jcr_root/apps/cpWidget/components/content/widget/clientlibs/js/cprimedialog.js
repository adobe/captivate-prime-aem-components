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

(function (document, window, $) {
    "use strict";
    
    var CP_DIALOG_REL = ".cp-dialog-form-rel";

   $(document).on("foundation-contentloaded", function (e) {
         $(document).on("change", ".selector-widget", function(e) {
             
             $(CP_DIALOG_REL).find("input[type='hidden']").each(function() { 
                 if ($(this).attr("name").indexOf("@Delete") != -1) {
                     $(this).remove();
                 }
             });
             
             var selectedWidget = $('.selector-widget coral-select-item:selected')[0].value;
             
             $(CP_DIALOG_REL).find(":input, coral-select").each(function (e) {
                 var elemName = $(this).attr("name");
                 var itemType = $(this).attr("itemtype");
                 
                 if (elemName && itemType) {
                     if (itemType !== selectedWidget) {
                         $(this).closest("div.coral-Form-fieldwrapper").attr("hidden",'');
                         var deleteName = elemName + "@Delete";
                             $('<input>').attr({type:'hidden', 
                                                name: deleteName}).appendTo(CP_DIALOG_REL);
                            $(this).attr("disabled","");
                     } else {
                         var divWrapper = $(this).closest("div.coral-Form-fieldwrapper");
                         if (divWrapper.find("label#hideOption").length < 1) {
                             divWrapper.removeAttr("hidden");
                         }
                         $(this).removeAttr("disabled");
                     }
                 } 
             });
            
   		 });

    });

})(document, window, jQuery);
