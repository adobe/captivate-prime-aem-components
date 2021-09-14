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

package com.adobe.prime.core.sightly.models;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.ScriptVariable;
import org.apache.sling.models.annotations.injectorspecific.Self;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adobe.prime.core.Constants;
import com.adobe.prime.core.entity.EmbeddableWidgetsConfig;
import com.adobe.prime.core.services.EmbeddableWidgetConfigurationService;
import com.adobe.prime.core.services.EmbeddableWidgetService;
import com.adobe.prime.core.utils.EmbeddableWidgetConfigUtils;
import com.day.cq.wcm.api.Page;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

@Model(adaptables = {SlingHttpServletRequest.class, Resource.class})
public class EmbeddableWidgetModel
{

  @Inject
  private transient EmbeddableWidgetService widgetService;

  @Inject
  private transient EmbeddableWidgetConfigurationService widgetConfigService;

  @ScriptVariable
  private Page currentPage;

  @Self
  private SlingHttpServletRequest request;

  private static final Logger LOGGER = LoggerFactory.getLogger(EmbeddableWidgetModel.class);

  private Resource resource;
  private String selectedWidgetRef = "";
  private String selectedRef = "";
  private String widgetConfigs = "";
  private String widgetSrcUrl = "";
  private String widgetCommunicatorUrl = "";
  private ValueMap properties;

  public EmbeddableWidgetModel(final SlingHttpServletRequest request)
  {
  }

  @PostConstruct
  public void init()
  {
    resource = request.getResource();
    properties = resource.getValueMap();
    String accessToken = widgetService.getAccessTokenOfUser(request, currentPage);
    Map<String, Object> adminConfigs = widgetConfigService.getAvailaleAdminConfiguration(resource);
    String hostName = adminConfigs.get(Constants.AdminConfigurations.ADMIN_CONFIG_HOST_NAME) != null
        ? adminConfigs.get(Constants.AdminConfigurations.ADMIN_CONFIG_HOST_NAME).toString()
        : widgetService.getDefaultHostName();

    LOGGER.debug("EmbeddableWidgetModel Init:: currentPage {} hostName {} host {} ", currentPage.getPath(), hostName,
        widgetService.getDefaultHostName());
    ValueMap map = resource.getValueMap();

    if (map != null)
    {
      List<EmbeddableWidgetsConfig> widgets = EmbeddableWidgetConfigUtils.getEmbeddableWidgetsConfig(hostName);
      LOGGER.trace("EmbeddableWidgetModel Init:: Widgets from CP {}", new Gson().toJson(widgets));
      List<EmbeddableWidgetsConfig> availableWidgetsList = getAvailableWidgets(widgets);
      selectedWidgetRef = map.get(Constants.SELECTED_WIDGET_REF) != null ? map.get(Constants.SELECTED_WIDGET_REF).toString() : null;
      if (selectedWidgetRef == null)
      {
        selectedWidgetRef = availableWidgetsList.get(0).getWidgetRef();
      }
      Optional<EmbeddableWidgetsConfig> opSelectedWidgetConfig =
          availableWidgetsList.stream().filter(widget -> widget.getWidgetRef().equals(selectedWidgetRef)).findFirst();
      EmbeddableWidgetsConfig selectedWidgetConfig;
      if (opSelectedWidgetConfig.isPresent())
      {
        selectedWidgetConfig = opSelectedWidgetConfig.get();
      } else
      {
        selectedWidgetConfig = availableWidgetsList.get(0);
      }
      selectedRef = selectedWidgetConfig.getRef();
      widgetSrcUrl = Constants.CPUrl.WIDGET_SRC_URL.replace("{hostName}", hostName).replace("{widgetRef}", selectedWidgetConfig.getRef());
      widgetCommunicatorUrl = Constants.CPUrl.WIDGET_COMMUNICATOR_URL.replace("{hostName}", hostName);
    }

    this.widgetConfigs = getWidgetConfig(map, selectedWidgetRef, accessToken);
  }

  private String getWidgetConfig(Map<String, Object> valueMap, String selectedWidgetRef, String accessToken)
  {
    Gson gson = new GsonBuilder().disableHtmlEscaping().create();
    Map<String, Object> widgetObject = new HashMap<>();
    for (Entry<String, Object> e : valueMap.entrySet())
    {
      String key = e.getKey();
      if (key.startsWith(Constants.CP_NODE_PROPERTY_PREFIX))
      {
        Object value = e.getValue();
        key = key.replace(Constants.CP_NODE_PROPERTY_PREFIX, "");
        if (value instanceof String)
        {
          widgetObject.put(key, value.toString());
        } else if (value instanceof Integer)
        {
          widgetObject.put(key, (Integer) value);
        } else if (value instanceof Boolean)
        {
          widgetObject.put(key, (Boolean) value);
        } else
        {
          widgetObject.put(key, gson.toJson(value));
        }
      }
    }
    widgetObject.put("widgetConfig.widgetRef", selectedWidgetRef);
    widgetObject.put("type", "acapConfig");

    Resource currentRsrc = request.getResourceResolver().getResource(currentPage.getPath());
    Map<String, Object> generalSettingConfig = widgetConfigService.getGeneralConfigs(currentRsrc);
    widgetObject.putAll(generalSettingConfig);

    widgetObject.put("auth.accessToken", accessToken);

    widgetObject.remove(Constants.AdminConfigurations.ADMIN_CONFIG_CLIENT_ID);
    widgetObject.remove(Constants.AdminConfigurations.ADMIN_CONFIG_CLIENT_SECRET);
    widgetObject.remove(Constants.AdminConfigurations.ADMIN_CONFIG_REFRESH_TOKEN);

    JsonObject obj = EmbeddableWidgetConfigUtils.getWidgetConfig(widgetObject);
    return gson.toJson(obj);
  }

  public String getWidgetConfigs()
  {
    return widgetConfigs;
  }

  public String getProperties()
  {
    return new Gson().toJson(properties);
  }

  public String getWidgetSrcUrl()
  {
    return widgetSrcUrl;
  }

  public String getSelectedRef()
  {
    return selectedRef;
  }

  public String getRunMode()
  {
    return Constants.RUNMODE_NON_AUTHOR;
  }

  public String getWidgetCommunicatorUrl()
  {
    return widgetCommunicatorUrl;
  }

  private List<EmbeddableWidgetsConfig> getAvailableWidgets(List<EmbeddableWidgetsConfig> widgets)
  {
    return widgets.stream().filter(widget -> widget.getType().equals("widget")).collect(Collectors.toList());
  }
}
