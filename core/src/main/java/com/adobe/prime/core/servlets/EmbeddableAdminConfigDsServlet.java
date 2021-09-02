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
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.servlet.Servlet;

import org.apache.commons.lang3.StringUtils;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.apache.sling.api.wrappers.ValueMapDecorator;
import org.osgi.service.component.ComponentContext;

import com.adobe.granite.ui.components.ValueMapResourceWrapper;
import com.adobe.granite.ui.components.ds.DataSource;
import com.adobe.granite.ui.components.ds.SimpleDataSource;
import com.adobe.granite.ui.components.ds.ValueMapResource;
import com.adobe.prime.core.Constants;
import com.adobe.prime.core.entity.EmbeddableWidgetOptions;
import com.adobe.prime.core.entity.EmbeddableWidgetsConfig;
import com.adobe.prime.core.services.EmbeddableWidgetService;
import com.adobe.prime.core.utils.EmbeddableWidgetConfigUtils;
import com.day.cq.commons.jcr.JcrConstants;

@Component(metatype = false, label = "Captivate Prime Admin Configuration Servlet", description = "Get Admin Configuration")
@Properties({@Property(name = "sling.servlet.resourceTypes", value = {EmbeddableAdminConfigDsServlet.RESOURCE_TYPE}, propertyPrivate = true),
    @Property(name = "sling.servlet.methods", value = HttpConstants.METHOD_GET, propertyPrivate = true),
    @Property(name = org.osgi.framework.Constants.SERVICE_DESCRIPTION, value = "Admin Configuration Servlet")})
@Service(Servlet.class)
public class EmbeddableAdminConfigDsServlet extends SlingAllMethodsServlet
{

  @Reference
  private transient EmbeddableWidgetService widgetService;

  private static final long serialVersionUID = 1135270242600328203L;

  final static String RESOURCE_TYPE = "cpWidget/configuration/datasource";

  @Override
  protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response)
  {

    List<Resource> resourceList = new ArrayList<>();
    Resource configResource = null;
    String configName = request.getParameter("item");

    ResourceResolver resolver = request.getResourceResolver();
    if (configName != null && !configName.isEmpty())
    {
      String configPath = Constants.AdminConfigurations.GLOBAL_CONFIG_CP_PATH + "/" + configName + Constants.AdminConfigurations.CP_SUB_CONFIG_PATH;
      configResource = resolver.getResource(configPath);
    }
    ValueMap valueMap;
    if (configResource != null)
    {
      valueMap = configResource.getValueMap();
    } else
    {
      valueMap = new ValueMapDecorator(new HashMap<String, Object>());
    }
    String configHostName = widgetService.getDefaultHostName();
    EmbeddableWidgetsConfig generalSettingsConfig = EmbeddableWidgetConfigUtils.getGeneralSettingsConfig(configHostName);
    createAEMSpecificDataSource(request, resourceList, valueMap);
    createDataSourceForWidget(request, generalSettingsConfig, resourceList, valueMap);

    request.setAttribute(DataSource.class.getName(), new SimpleDataSource(resourceList.iterator()));

  }

  private void createAEMSpecificDataSource(SlingHttpServletRequest request, List<Resource> resourceList, ValueMap map)
  {

    String titleName = Constants.CP_NODE_PROPERTY_PREFIX + "title";
    Object existingTitleValue = map.get(Constants.CP_NODE_PROPERTY_PREFIX + "title");
    if (existingTitleValue != null)
    {
      resourceList.add(createTextFieldResource(request, titleName, existingTitleValue.toString(), "", true, "Config Title", false, true));
    } else
    {
      resourceList.add(createTextFieldResource(request, titleName, "", "", true, "Config Title", false, false));
    }

    String refreshTokenName = Constants.CP_NODE_PROPERTY_PREFIX + Constants.AdminConfigurations.ADMIN_CONFIG_REFRESH_TOKEN;
    String refreshTokenValue = "";
    Object refreshTokenValueObj = map.get(Constants.CP_NODE_PROPERTY_PREFIX + Constants.AdminConfigurations.ADMIN_CONFIG_REFRESH_TOKEN);
    if (refreshTokenValueObj != null)
    {
      refreshTokenValue = map.get(Constants.CP_NODE_PROPERTY_PREFIX + Constants.AdminConfigurations.ADMIN_CONFIG_REFRESH_TOKEN).toString();
      int length = refreshTokenValue.length();
      if (length > 8)
      {
        final String overlay =
            StringUtils.repeat(Constants.AdminConfigurations.MASK_CHAR, length - (2 * Constants.AdminConfigurations.MASK_LENGTH));
        refreshTokenValue = StringUtils.overlay(refreshTokenValue, overlay, Constants.AdminConfigurations.MASK_LENGTH,
            length - Constants.AdminConfigurations.MASK_LENGTH);
      }
    }

    resourceList.add(createTextFieldResource(request, refreshTokenName, refreshTokenValue, "", true, "Admin Refresh Token", false, false));

    String clientIdName = Constants.CP_NODE_PROPERTY_PREFIX + Constants.AdminConfigurations.ADMIN_CONFIG_CLIENT_ID;
    String clientIdValue = map.get(Constants.CP_NODE_PROPERTY_PREFIX + Constants.AdminConfigurations.ADMIN_CONFIG_CLIENT_ID) != null
        ? map.get(Constants.CP_NODE_PROPERTY_PREFIX + Constants.AdminConfigurations.ADMIN_CONFIG_CLIENT_ID).toString()
        : "";
    resourceList.add(createTextFieldResource(request, clientIdName, clientIdValue, "", true, "Client Id", false, false));

    String clientSecretName = Constants.CP_NODE_PROPERTY_PREFIX + Constants.AdminConfigurations.ADMIN_CONFIG_CLIENT_SECRET;
    String clientSecretValue = map.get(Constants.CP_NODE_PROPERTY_PREFIX + Constants.AdminConfigurations.ADMIN_CONFIG_CLIENT_SECRET) != null
        ? map.get(Constants.CP_NODE_PROPERTY_PREFIX + Constants.AdminConfigurations.ADMIN_CONFIG_CLIENT_SECRET).toString()
        : "";
    resourceList.add(createTextFieldResource(request, clientSecretName, clientSecretValue, "", true, "Client Secret", false, false));
  }

  private void createDataSourceForWidget(SlingHttpServletRequest request, EmbeddableWidgetsConfig widgetConfig, List<Resource> resourceList,
      ValueMap map)
  {

    for (EmbeddableWidgetOptions option : widgetConfig.getOptions())
    {
      String type = option.getType();
      String name = Constants.CP_NODE_PROPERTY_PREFIX + option.getRef();
      String value = "", emptyText = "";
      String fieldLabel = option.getName();
      boolean hidden = option.getHidden();
      String helpxLink = option.getHelpx();

      if (option.getRef().equals("auth.accessToken"))
      {
        continue;
      }

      boolean required = option.getMandatory();

      if (hidden)
      {
        value = option.getDefaultValue();
      } else
      {
        value = map.get(Constants.CP_NODE_PROPERTY_PREFIX + option.getRef()) != null
            ? map.get(Constants.CP_NODE_PROPERTY_PREFIX + option.getRef()).toString()
            : (option.getDefaultValue() != null ? option.getDefaultValue() : "");
      }


      switch (type)
      {
        case "color":
          emptyText = option.getDefaultValue();
          resourceList.add(createColorPickerResource(request, name, value, emptyText, required, fieldLabel, hidden));
          break;

        case "string":
          emptyText = option.getDefaultValue();
          resourceList.add(createTextFieldResource(request, name, value, emptyText, required, fieldLabel, hidden, false));
          break;

        case "boolean":
          resourceList.add(createCheckBoxResource(request, name, value, option.getName(), fieldLabel, hidden));
          resourceList.add(createHiddenTypeHint(request, name + "@TypeHint", "boolean", false));
          break;

        default:
          String[] values = type.split("\\|");
          resourceList.add(createDropdownResource(request, name, required, fieldLabel, values, value, hidden));
          break;
      }

      if (helpxLink != null && helpxLink.length() > 0)
      {
        String helpxLinkName = Constants.AdminConfigurations.HELPXLINK_PREFIX + name;
        resourceList.add(createHiddenTypeHint(request, helpxLinkName, helpxLink, true));
      }
    }
  }

  private ValueMapResource createTextFieldResource(SlingHttpServletRequest request, String name, String value, String emptyText, boolean required,
      String fieldLabel, boolean hidden, boolean disabled)
  {
    String resourceType = "granite/ui/components/coral/foundation/form/textfield";
    ValueMap vm = new ValueMapDecorator(new HashMap<String, Object>());
    vm.put("name", name);
    vm.put("emptyText", emptyText);
    vm.put("value", value);
    vm.put("required", required);
    vm.put("fieldLabel", fieldLabel);
    vm.put("renderHidden", hidden);
    if (disabled)
    {
      vm.put("disabled", disabled);
    }
    return new ValueMapResource(request.getResourceResolver(), "", resourceType, vm);
  }

  private ValueMapResource createCheckBoxResource(SlingHttpServletRequest request, String name, String value, String text, String fieldLabel,
      boolean hidden)
  {
    String resourceType = "granite/ui/components/coral/foundation/form/checkbox";
    ValueMap vm = new ValueMapDecorator(new HashMap<String, Object>());
    vm.put("name", name);
    vm.put("text", text);
    vm.put("fieldLabel", fieldLabel);
    if (value.length() > 0)
    {
      vm.put("checked", Boolean.valueOf(value));
    }
    vm.put("value", value);
    vm.put("renderHidden", hidden);
    if (hidden)
    {
      vm.put("fieldDescription", hidden);
      vm.put("wrapperClass", "cp-hide-checkbox-elem");
    }
    return new ValueMapResource(request.getResourceResolver(), "", resourceType, vm);
  }

  private ValueMapResource createHiddenTypeHint(SlingHttpServletRequest request, String name, String value, boolean disabled)
  {
    String resourceType = "granite/ui/components/coral/foundation/form/hidden";
    ValueMap vm = new ValueMapDecorator(new HashMap<String, Object>());
    vm.put("name", name);
    vm.put("value", value);
    vm.put("disabled", disabled);
    return new ValueMapResource(request.getResourceResolver(), "", resourceType, vm);
  }

  private ValueMapResource createHiddenType(SlingHttpServletRequest request, String name, String value)
  {
    String resourceType = "granite/ui/components/coral/foundation/form/hidden";
    ValueMap vm = new ValueMapDecorator(new HashMap<String, Object>());
    vm.put("name", name);
    vm.put("value", value);
    return new ValueMapResource(request.getResourceResolver(), "", resourceType, vm);
  }

  private ValueMapResource createColorPickerResource(SlingHttpServletRequest request, String name, String value, String emptyText, boolean required,
      String fieldLabel, boolean hidden)
  {
    String resourceType = "granite/ui/components/coral/foundation/form/colorfield";
    ValueMap vm = new ValueMapDecorator(new HashMap<String, Object>());
    vm.put("name", name);
    vm.put("value", value);
    vm.put("emptyText", emptyText);
    vm.put("required", required);
    vm.put("fieldLabel", fieldLabel);
    vm.put("renderHidden", hidden);
    return new ValueMapResource(request.getResourceResolver(), "", resourceType, vm);
  }

  private Resource createDropdownResource(SlingHttpServletRequest request, String name, boolean required, String fieldLabel, String[] values,
      String selectedValue, boolean hidden)
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
                if (value.equals(selectedValue))
                {
                  vm.put("selected", true);
                }
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
      valueMap.put("fieldLabel", fieldLabel);
      valueMap.put("renderHidden", hidden);
    }
    return wrapper;
  }
}
