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
import java.util.Iterator;
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

import com.adobe.granite.ui.components.ValueMapResourceWrapper;
import com.adobe.granite.ui.components.ds.DataSource;
import com.adobe.granite.ui.components.ds.SimpleDataSource;
import com.adobe.granite.ui.components.ds.ValueMapResource;
import com.adobe.prime.core.Constants;
import com.adobe.prime.core.entity.EmbeddableWidgetOptions;
import com.adobe.prime.core.entity.EmbeddableWidgetsConfig;
import com.adobe.prime.core.services.EmbeddableWidgetConfigurationService;
import com.adobe.prime.core.services.EmbeddableWidgetService;
import com.adobe.prime.core.utils.EmbeddableWidgetConfigUtils;
import com.day.cq.commons.jcr.JcrConstants;

@Component(label = "Adobe Learning Manager Widget Datasource Servlet", description = "Adobe Learning Manager Widget Datasource Servlet")
@Properties({@Property(name = "sling.servlet.resourceTypes", value = {EmbeddableWidgetDatasourceServlet.RESOURCE_TYPE}, propertyPrivate = true),
    @Property(name = "sling.servlet.methods", value = HttpConstants.METHOD_GET, propertyPrivate = true),
    @Property(name = org.osgi.framework.Constants.SERVICE_DESCRIPTION, value = "Adobe Learning Manager Widget Datasource Servlet")})
@Service(Servlet.class)
public class EmbeddableWidgetDatasourceServlet extends SlingAllMethodsServlet
{

  private static final long serialVersionUID = 6208450620001248037L;

  @Reference
  private transient EmbeddableWidgetService widgetService;

  @Reference
  private transient EmbeddableWidgetConfigurationService widgetConfigService;

  final static String RESOURCE_TYPE = "cpPrime/widgets/datasource/widgetsdatasource";

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
        ValueMap valueMap = resource.getValueMap();
        Map<String, Object> adminConfigs = widgetConfigService.getAvailaleAdminConfiguration(resource);
        String hostName = adminConfigs.get(Constants.AdminConfigurations.ADMIN_CONFIG_HOST_NAME) != null
            ? adminConfigs.get(Constants.AdminConfigurations.ADMIN_CONFIG_HOST_NAME).toString()
            : widgetService.getDefaultHostName();
        List<EmbeddableWidgetsConfig> widgets = EmbeddableWidgetConfigUtils.getEmbeddableWidgetsConfig(hostName);
        List<EmbeddableWidgetsConfig> availableWidgetsList = getAvailableWidgets(widgets);
        String selectedWidgetRef =
            valueMap.get(Constants.SELECTED_WIDGET_REF) != null ? valueMap.get(Constants.SELECTED_WIDGET_REF).toString() : null;
        if (selectedWidgetRef == null)
        {
          selectedWidgetRef = availableWidgetsList.get(0).getWidgetRef();
        }

        Resource widgetSelectDropdown = request.getResource().getChild("widgetSelect");
        resourceList.add(widgetSelectDropdown);

        for (EmbeddableWidgetsConfig widgetConfig : availableWidgetsList)
        {
          createDataSourceForWidget(request, widgetConfig, selectedWidgetRef, resourceList, valueMap);
        }
      }
    }

    request.setAttribute(DataSource.class.getName(), new SimpleDataSource(resourceList.iterator()));

  }

  private void createDataSourceForWidget(SlingHttpServletRequest request, EmbeddableWidgetsConfig widgetConfig, String selectedWidgetRef,
      List<Resource> resourceList, ValueMap map)
  {
    boolean hide = !selectedWidgetRef.equals(widgetConfig.getWidgetRef());
    String itemType = widgetConfig.getWidgetRef();

    for (EmbeddableWidgetOptions option : widgetConfig.getOptions())
    {
      String type = option.getType();
      String name = "./" + Constants.CP_NODE_PROPERTY_PREFIX + option.getRef();
      String value = "", emptyText = "";
      String fieldLabel = option.getName();
      boolean hideOption = option.getHidden();

      boolean required = option.getMandatory();
      if (hideOption)
      {
        value = option.getDefaultValue();
      } else if ((widgetConfig.getWidgetRef() != null) && selectedWidgetRef != null && widgetConfig.getWidgetRef().equals(selectedWidgetRef))
      {
        value = map.get(Constants.CP_NODE_PROPERTY_PREFIX + option.getRef()) != null
            ? map.get(Constants.CP_NODE_PROPERTY_PREFIX + option.getRef()).toString()
            : "";
      }

      switch (type)
      {
        case "color":
          emptyText = widgetConfig.getDefault();
          resourceList.add(createColorPickerResource(request, name, value, emptyText, required, fieldLabel, hide, itemType, hideOption));
          break;

        case "string":
          emptyText = widgetConfig.getDefault();
          resourceList.add(createTextFieldResource(request, name, value, emptyText, required, fieldLabel, hide, itemType, hideOption));
          break;

        case "boolean":
          resourceList.add(createCheckBoxResource(request, name, value, widgetConfig.getName(), fieldLabel, hide, itemType, hideOption));
          break;

        default:
          String[] values = type.split("\\|");
          resourceList.add(createDropdownResource(request, name, required, fieldLabel, hide, values, itemType, hideOption));
          break;
      }
    }
  }

  private ValueMapResource createTextFieldResource(SlingHttpServletRequest request, String name, String value, String emptyText, boolean required,
      String fieldLabel, boolean hide, String itemType, boolean hideOption)
  {
    String resourceType = "granite/ui/components/coral/foundation/form/textfield";
    ValueMap vm = new ValueMapDecorator(new HashMap<String, Object>());
    vm.put("name", name);
    vm.put("emptyText", emptyText);
    vm.put("value", value);
    vm.put("required", required);
    vm.put("renderHidden", hide);
    vm.put("fieldLabel", fieldLabel);
    vm.put("granite:itemtype", itemType);
    if (hideOption)
    {
      vm.put("labelId", "hideOption");
    }
    return new ValueMapResource(request.getResourceResolver(), "", resourceType, vm);
  }

  private ValueMapResource createCheckBoxResource(SlingHttpServletRequest request, String name, String value, String text, String fieldLabel,
      boolean hide, String itemType, boolean hideOption)
  {
    String resourceType = "granite/ui/components/coral/foundation/form/checkbox";
    ValueMap vm = new ValueMapDecorator(new HashMap<String, Object>());
    vm.put("name", name);
    vm.put("value", value);
    vm.put("text", text);
    vm.put("renderHidden", hide);
    vm.put("fieldLabel", fieldLabel);
    vm.put("granite:itemtype", itemType);
    if (hideOption)
    {
      vm.put("labelId", "hideOption");
    }
    return new ValueMapResource(request.getResourceResolver(), "", resourceType, vm);
  }

  private ValueMapResource createColorPickerResource(SlingHttpServletRequest request, String name, String value, String emptyText, boolean required,
      String fieldLabel, boolean hide, String itemType, boolean hideOption)
  {
    String resourceType = "granite/ui/components/coral/foundation/form/colorfield";
    ValueMap vm = new ValueMapDecorator(new HashMap<String, Object>());
    vm.put("name", name);
    vm.put("value", value);
    vm.put("emptyText", emptyText);
    vm.put("required", required);
    vm.put("renderHidden", hide);
    vm.put("fieldLabel", fieldLabel);
    vm.put("granite:itemtype", itemType);
    if (hideOption)
    {
      vm.put("labelId", "hideOption");
    }
    return new ValueMapResource(request.getResourceResolver(), "", resourceType, vm);
  }

  private Resource createDropdownResource(SlingHttpServletRequest request, String name, boolean required, String fieldLabel, boolean hide,
      String[] values, String itemType, boolean hideOption)
  {
    String resourceType = "granite/ui/components/coral/foundation/form/select";
    ValueMap vm = new ValueMapDecorator(new HashMap<String, Object>());

    Resource res = new ValueMapResource(request.getResourceResolver(), "", resourceType, vm);

    Resource wrapper = new ValueMapResourceWrapper(res, resourceType)
    {
      public Resource getChild(String relPath)
      {
        if ("items".equals(relPath))
        {
          Resource dataWrapper = new ValueMapResourceWrapper(res, JcrConstants.NT_UNSTRUCTURED)
          {
            public Iterator<Resource> listChildren()
            {
              List<Resource> itemsResourceList = new ArrayList<Resource>();

              for (String value : values)
              {
                ValueMap vm = new ValueMapDecorator(new HashMap<String, Object>());
                vm.put("value", value);
                vm.put("text", value);
                itemsResourceList.add(new ValueMapResource(request.getResourceResolver(), "", JcrConstants.NT_UNSTRUCTURED, vm));
              }

              return itemsResourceList.iterator();
            }
          };
          return dataWrapper;
        } else
        {
          return super.getChild(relPath);
        }
      }
    };
    ValueMap valueMap = wrapper.adaptTo(ValueMap.class);
    if (valueMap != null)
    {
      valueMap.put("name", name);
      valueMap.put("required", required);
      valueMap.put("renderHidden", hide);
      valueMap.put("fieldLabel", fieldLabel);
      valueMap.put("granite:itemtype", itemType);
      if (hideOption)
      {
        vm.put("labelId", "hideOption");
      }
    }
    return wrapper;

  }

  private List<EmbeddableWidgetsConfig> getAvailableWidgets(List<EmbeddableWidgetsConfig> widgets)
  {
    return widgets.stream().filter(widget -> widget.getType().equals("widget")).collect(Collectors.toList());
  }
}
