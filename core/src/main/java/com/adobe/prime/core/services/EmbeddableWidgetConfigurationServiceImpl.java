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

package com.adobe.prime.core.services;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adobe.prime.core.Constants;
import com.day.cq.commons.inherit.HierarchyNodeInheritanceValueMap;
import com.day.cq.commons.inherit.InheritanceValueMap;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;

@Component(metatype = false, immediate = true)
@Service(value = EmbeddableWidgetConfigurationService.class)
public class EmbeddableWidgetConfigurationServiceImpl implements EmbeddableWidgetConfigurationService
{

  @Reference
  ResourceResolverFactory resourceResolverFactory;

  private static final Logger LOGGER = LoggerFactory.getLogger(EmbeddableWidgetConfigurationServiceImpl.class);
  private static final String SUBSERVICE_NAME = "writeService";
  private static final Map<String, Object> SERVICE_PARAMS =
      Collections.<String, Object>singletonMap(ResourceResolverFactory.SUBSERVICE, SUBSERVICE_NAME);

  @Override
  public Map<String, Object> getGeneralConfigs(Resource resource)
  {
    Map<String, Object> generalConfigs = new HashMap<String, Object>();
    ResourceResolver adminResolver = null;

    try
    {
      adminResolver = resourceResolverFactory.getServiceResourceResolver(SERVICE_PARAMS);

      Map<String, Object> adminConfigs = getAvailaleAdminConfiguration(resource);

      for (Entry<String, Object> e : adminConfigs.entrySet())
      {
        if (e.getKey().startsWith(Constants.CP_NODE_PROPERTY_PREFIX))
        {
          String objectType = e.getValue().getClass().getSimpleName();
          if ("Boolean".equals(objectType))
          {
            generalConfigs.put(e.getKey().replace(Constants.CP_NODE_PROPERTY_PREFIX, ""), (Boolean) e.getValue());
          } else
          {
            generalConfigs.put(e.getKey().replace(Constants.CP_NODE_PROPERTY_PREFIX, ""), e.getValue().toString());
          }
        }
      }
    } catch (LoginException le)
    {
      LOGGER.error("LoginException in fetching general configurations", le);
    } finally
    {
      if (adminResolver != null)
      {
        adminResolver.close();
      }
    }
    return generalConfigs;
  }

  @Override
  public Map<String, Object> getAvailaleAdminConfiguration(Resource resource)
  {
    LOGGER.debug("EmbeddableWidgetServiceImpl getAvailaleAdminConfiguration:: Rsrc {}", resource.getPath());
    ResourceResolver adminResolver = null;
    Map<String, Object> adminConfigs = new HashMap<String, Object>();

    try
    {
      adminResolver = resourceResolverFactory.getServiceResourceResolver(SERVICE_PARAMS);
      PageManager pageManager = adminResolver.adaptTo(PageManager.class);
      if (pageManager != null)
      {
        Page containingPage = pageManager.getContainingPage(resource.getPath());

        InheritanceValueMap inheritedVM = new HierarchyNodeInheritanceValueMap(containingPage.getContentResource());
        String cpConfPath = inheritedVM.getInherited(Constants.CONF_PROP_NAME, String.class);

        LOGGER.debug("EmbeddableWidgetServiceImpl getAvailaleAdminConfiguration:: Resource Path {}, Page Path {}, Page Rsrc Path {}, Config Path {}",
            resource.getPath(), containingPage.getPath(), containingPage.getContentResource().getPath(), cpConfPath);

        if (cpConfPath == null || cpConfPath.isEmpty() || !cpConfPath.startsWith(Constants.AdminConfigurations.GLOBAL_CONFIG_CP_PATH))
        {
          cpConfPath = getFirstAvailableCPConfigPath(adminResolver);
        }

        if (cpConfPath != null)
        {
          String configNodePath = cpConfPath + Constants.AdminConfigurations.CP_SUB_CONFIG_PATH;
          LOGGER.debug("EmbeddableWidgetServiceImpl getAvailaleAdminConfiguration:: ConfigNodePath {}", configNodePath);
          Resource configResource = adminResolver.getResource(configNodePath);
          if (configResource != null)
          {
            for (Entry<String, Object> e : configResource.getValueMap().entrySet())
            {
              adminConfigs.put(e.getKey(), e.getValue());
            }
          }
        }
      }
    } catch (LoginException exc)
    {
      LOGGER.error("LoginException in fetching configuration for resource path- {}", resource.getPath(), exc);
    } finally
    {
      if (adminResolver != null)
      {
        adminResolver.close();
      }
    }
    return adminConfigs;
  }

  private String getFirstAvailableCPConfigPath(ResourceResolver adminResolver)
  {
    LOGGER.debug(
        "EmbeddableWidgetServiceImpl GetFirstAvailableCPConfigPath:: Config not mapped. Fetching first available config in ascending sorted order");
    Resource configResource = adminResolver.getResource(Constants.AdminConfigurations.GLOBAL_CONFIG_CP_PATH);
    if (configResource != null)
    {
      Iterator<Resource> myResources = configResource.listChildren();
      List<String> resourcesName = new ArrayList<String>();
      while (myResources.hasNext())
      {
        String resName = myResources.next().getName();
        if (!resName.equalsIgnoreCase(Constants.AdminConfigurations.CLOUD_CONFIG_SETTINGS))
        {
          resourcesName.add(resName);
        }
      }
      if (!resourcesName.isEmpty())
      {
        Collections.sort(resourcesName);
        return Constants.AdminConfigurations.GLOBAL_CONFIG_CP_PATH + "/" + resourcesName.get(0);
      }
    }
    return null;
  }

}
