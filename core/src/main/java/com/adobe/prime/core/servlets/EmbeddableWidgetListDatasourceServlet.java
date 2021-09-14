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

package com.adobe.prime.core.servlets;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.Servlet;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.apache.sling.api.wrappers.ValueMapDecorator;

import com.adobe.granite.ui.components.ds.DataSource;
import com.adobe.granite.ui.components.ds.SimpleDataSource;
import com.adobe.granite.ui.components.ds.ValueMapResource;
import com.adobe.prime.core.Constants;
import com.adobe.prime.core.entity.EmbeddableWidgetsConfig;
import com.adobe.prime.core.services.EmbeddableWidgetConfigurationService;
import com.adobe.prime.core.services.EmbeddableWidgetService;
import com.adobe.prime.core.utils.EmbeddableWidgetConfigUtils;

@Component(label = "Captivate Prime Widget List Datasource Servlet", description = "Captivate Prime Widget List Datasource Servlet")
@Properties({@Property(name = "sling.servlet.resourceTypes", value = {EmbeddableWidgetListDatasourceServlet.RESOURCE_TYPE}, propertyPrivate = true),
    @Property(name = "sling.servlet.methods", value = HttpConstants.METHOD_GET, propertyPrivate = true),
    @Property(name = org.osgi.framework.Constants.SERVICE_DESCRIPTION, value = "Captivate Prime Widget List Datasource Servlet")})
@Service(Servlet.class)
public class EmbeddableWidgetListDatasourceServlet extends SlingAllMethodsServlet
{

  private static final long serialVersionUID = 6208632688001248037L;

  @Reference
  private transient EmbeddableWidgetService widgetService;

  @Reference
  private transient EmbeddableWidgetConfigurationService widgetConfigService;

  final static String RESOURCE_TYPE = "cpPrime/widgets/datasource/widgetsSelectDatasource";

  @Override
  protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response)
  {

    String requestSuffix = "";
    List<Resource> resourceList = new ArrayList<>();

    if (null != request.getRequestPathInfo().getSuffix())
    {
      requestSuffix = request.getRequestPathInfo().getSuffix();
      Resource resource = request.getResourceResolver().getResource(requestSuffix);

      if (resource != null)
      {
        Map<String, Object> adminConfigs = widgetConfigService.getAvailaleAdminConfiguration(resource);
        String hostName = adminConfigs.get(Constants.AdminConfigurations.ADMIN_CONFIG_HOST_NAME) != null
            ? adminConfigs.get(Constants.AdminConfigurations.ADMIN_CONFIG_HOST_NAME).toString()
            : widgetService.getDefaultHostName();
        List<EmbeddableWidgetsConfig> widgets = EmbeddableWidgetConfigUtils.getEmbeddableWidgetsConfig(hostName);
        List<EmbeddableWidgetsConfig> availableWidgetsList = getAvailableWidgets(widgets);

        for (EmbeddableWidgetsConfig widgetConfig : availableWidgetsList)
        {
          ValueMap vm = new ValueMapDecorator(new HashMap<String, Object>());
          String value = widgetConfig.getWidgetRef();
          vm.put("value", value);
          vm.put("text", widgetConfig.getName());
          resourceList.add(new ValueMapResource(request.getResourceResolver(), "", "", vm));
        }
      }
    }

    request.setAttribute(DataSource.class.getName(), new SimpleDataSource(resourceList.iterator()));
  }

  private List<EmbeddableWidgetsConfig> getAvailableWidgets(List<EmbeddableWidgetsConfig> widgets)
  {
    return widgets.stream().filter(widget -> widget.getType().equals("widget")).collect(Collectors.toList());
  }
}
