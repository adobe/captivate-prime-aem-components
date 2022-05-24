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

    function handleHiddenOptions()
    {
        $("input.hideOption").closest("div.coral-Form-fieldwrapper").attr("hidden",'');
    }

    function handleSelection() {
      $(CP_DIALOG_REL).find("input[type='hidden']").each(function () {
          if ($(this).attr("name").indexOf("@Delete") != -1) {
            $(this).remove();
          }
        });

      var selectedWidget = $(".selector-widget coral-select-item:selected")[0]
        .value;

      $(CP_DIALOG_REL).find(":input, coral-select").each(function (e) {
          var elemName = $(this).attr("name");
          var itemType = $(this).attr("itemtype");

          if (elemName && itemType) {
            if (itemType !== selectedWidget) {
              let divWrapper = $(this).closest("div.coral-Form-fieldwrapper");
              divWrapper.attr("hidden", "");
              if (divWrapper.find("label#hideOption").length < 1 && divWrapper.find("input.hideOption").length < 1) {
                let deleteName = elemName + "@Delete";
                $("<input>").attr({ type: "hidden", name: deleteName }).appendTo(CP_DIALOG_REL);
                $(this).attr("disabled", "");
              }
            } else {
              let divWrapper = $(this).closest("div.coral-Form-fieldwrapper");
              if (divWrapper.find("label#hideOption").length < 1 && divWrapper.find("input.hideOption").length < 1) {
                divWrapper.removeAttr("hidden");
              }
              $(this).removeAttr("disabled");
            }
          }
        });
    }

   $(document).on("foundation-contentloaded", function (e) {
        let usageTypeSelectElem = $(".selector-widget").get(0);
        Coral.commons.ready(usageTypeSelectElem, function() {
            handleHiddenOptions();
            handleSelection();
            usageTypeSelectElem.on('change', handleSelection);
        });
    });

})(document, window, jQuery);
