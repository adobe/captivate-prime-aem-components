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

import javax.servlet.Servlet;

import org.apache.sling.api.SlingException;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.apache.sling.api.wrappers.ValueMapDecorator;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adobe.granite.ui.components.ds.DataSource;
import com.adobe.granite.ui.components.ds.SimpleDataSource;
import com.adobe.granite.ui.components.ds.ValueMapResource;
import com.adobe.prime.core.Constants;
import com.day.cq.commons.jcr.JcrConstants;

@Component(service = Servlet.class,
    property = {"sling.servlet.methods=GET", "sling.servlet.resourceTypes=" + EmbeddableAdminConfigGetServlet.RESOURCE_TYPE})
public class EmbeddableAdminConfigGetServlet extends SlingAllMethodsServlet
{

  private static final long serialVersionUID = 1135289242600345203L;

  final static String RESOURCE_TYPE = "cpWidget/configurations";
  private static final Logger LOGGER = LoggerFactory.getLogger(EmbeddableAdminConfigGetServlet.class);

  @Override
  protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response)
  {
    ResourceResolver resolver = request.getResourceResolver();
    try
    {
      List<Resource> resourceList = new ArrayList<Resource>();
      Resource configNode = resolver.getResource(Constants.AdminConfigurations.GLOBAL_CONFIG_CP_PATH);

      if (configNode != null)
      {
        if (configNode.hasChildren())
        {
          Iterator<Resource> configItr = configNode.listChildren();
          while (configItr.hasNext())
          {
            Resource configRes = configItr.next();
            if (Constants.AdminConfigurations.CLOUD_CONFIG_SETTINGS.equals(configRes.getName()))
            {
              continue;
            }
            resourceList.add(createValueMapResource(resolver, configRes.getName(), configRes));
          }
        }
      }

      request.setAttribute(DataSource.class.getName(), new SimpleDataSource(resourceList.iterator()));

    } catch (IllegalStateException | SlingException exc)
    {
      LOGGER.error("Exception in fetching Admin Configurations", exc);
    }
  }

  private ValueMapResource createValueMapResource(ResourceResolver resolver, String configName, Resource configRes)
  {
    ValueMap vm = new ValueMapDecorator(new HashMap<String, Object>());
    vm.put(JcrConstants.JCR_TITLE, configRes.getName());
    vm.put("isValid", true);
    return new ValueMapResource(resolver, configName, "cpWidget/components/views/card", vm);
  }

}
