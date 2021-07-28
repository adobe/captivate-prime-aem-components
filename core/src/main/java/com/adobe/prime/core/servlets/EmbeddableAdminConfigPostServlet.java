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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.servlet.Servlet;

import org.apache.commons.lang3.StringUtils;
import org.apache.jackrabbit.commons.JcrUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.request.RequestParameter;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.apache.sling.jcr.resource.api.JcrResourceConstants;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adobe.prime.core.Constants;
import com.day.cq.commons.jcr.JcrConstants;
import com.day.cq.wcm.api.NameConstants;

@Component(service = Servlet.class,
    property = {"sling.servlet.methods=POST", "sling.servlet.resourceTypes=" + EmbeddableAdminConfigPostServlet.RESOURCE_TYPE})
public class EmbeddableAdminConfigPostServlet extends SlingAllMethodsServlet
{

  private static final long serialVersionUID = 1133289200600345103L;

  final static String RESOURCE_TYPE = "cpWidget/updateConfiguration";

  private static final String CQ_PAGE_CONTENT = "cq:PageContent";

  private static final Logger LOGGER = LoggerFactory.getLogger(EmbeddableAdminConfigPostServlet.class);

  @Override
  protected void doPost(SlingHttpServletRequest request, SlingHttpServletResponse response)
  {
    ResourceResolver resolver = request.getResourceResolver();
    String configName = request.getParameter("item");
    try
    {
      Session session = resolver.adaptTo(Session.class);
      Map<String, String> properties = extractProperties(request);
      if (configName == null || configName.isEmpty())
      {
        configName = properties.get(Constants.CP_NODE_PROPERTY_PREFIX + "title");
      }

      Node configNode = getConfigNode(session, configName);

      if (configNode != null)
      {
        String refreshTokenPropName = Constants.CP_NODE_PROPERTY_PREFIX + Constants.AdminConfigurations.ADMIN_CONFIG_REFRESH_TOKEN;
        if (configNode.hasProperty(refreshTokenPropName))
        {
          String refreshTokenUserValue = properties.get(refreshTokenPropName);
          String refreshTokeDbValue = configNode.getProperty(refreshTokenPropName).getValue().getString();
          final String overlay = StringUtils.repeat(Constants.AdminConfigurations.MASK_CHAR,
              refreshTokeDbValue.length() - (2 * Constants.AdminConfigurations.MASK_LENGTH));
          String maskRefreshTokenValue = StringUtils.overlay(refreshTokeDbValue, overlay, Constants.AdminConfigurations.MASK_LENGTH,
              refreshTokeDbValue.length() - Constants.AdminConfigurations.MASK_LENGTH);
          if (maskRefreshTokenValue.equals(refreshTokenUserValue))
          {
            properties.remove(refreshTokenPropName);
          }
        }

        for (Entry<String, String> e : properties.entrySet())
        {
          String name = e.getKey();
          String value = e.getValue();

          if (name.endsWith("@TypeHint"))
          {
            continue;
          } else if (name.endsWith("@Delete"))
          {
            String nonDeletePropName = name.replace("@Delete", "");
            if (properties.get(nonDeletePropName) == null)
            {
              configNode.setProperty(nonDeletePropName, false);
            }
          } else
          {
            String deletePropName = name + "@TypeHint";
            String type = properties.get(deletePropName) != null ? properties.get(deletePropName) : "string";
            switch (type)
            {
              case "boolean":
                configNode.setProperty(name, true);
                break;
              case "number":
                configNode.setProperty(name, Long.valueOf(value));
                break;
              default:
                configNode.setProperty(name, value);
            }
          }
        }
      }

      resolver.commit();

    } catch (RepositoryException | IOException e)
    {
      LOGGER.error("Exception in saving configurations", e);
    }
  }

  private Node getConfigNode(Session session, String configName)
  {
    try
    {
      Node globalConfNode = session.getNode(Constants.AdminConfigurations.GLOBAL_CONFIG_PATH);
      Node configNode = session.nodeExists(Constants.AdminConfigurations.GLOBAL_CONFIG_CP_PATH)
          ? globalConfNode.getNode(Constants.AdminConfigurations.GLOBAL_CONFIG_CP)
          : globalConfNode.addNode(Constants.AdminConfigurations.GLOBAL_CONFIG_CP, JcrResourceConstants.NT_SLING_FOLDER);
      if (!session.nodeExists(configNode.getPath() + "/" + Constants.AdminConfigurations.CLOUD_CONFIG_SETTINGS))
      {
        configNode.addNode(Constants.AdminConfigurations.CLOUD_CONFIG_SETTINGS, JcrResourceConstants.NT_SLING_FOLDER);
      }
      Node subConfigurationNode = JcrUtils.getOrCreateByPath(configNode.getPath() + "/" + configName, JcrResourceConstants.NT_SLING_FOLDER, session);
      Node settingsNode = JcrUtils.getOrCreateByPath(subConfigurationNode.getPath() + "/" + Constants.AdminConfigurations.CLOUD_CONFIG_SETTINGS,
          JcrResourceConstants.NT_SLING_FOLDER, session);
      Node cloudConfigsNode = JcrUtils.getOrCreateByPath(settingsNode.getPath() + "/" + Constants.AdminConfigurations.CLOUD_CONFIG,
          JcrResourceConstants.NT_SLING_FOLDER, session);
      Node cpWidgetNode = JcrUtils.getOrCreateByPath(cloudConfigsNode.getPath() + "/" + Constants.AdminConfigurations.CP_WIDGET_CONFIG,
          NameConstants.NT_PAGE, session);
      Node cpConfigPropertiesNode = JcrUtils.getOrCreateByPath(cpWidgetNode.getPath() + "/" + JcrConstants.JCR_CONTENT, CQ_PAGE_CONTENT, session);
      return cpConfigPropertiesNode;
    } catch (RepositoryException re)
    {
      LOGGER.error("Exception in creating config node", re);
    }
    return null;
  }

  private Map<String, String> extractProperties(SlingHttpServletRequest request) throws IOException
  {
    Map<String, String> properties = new HashMap<String, String>();
    for (Entry<String, RequestParameter[]> entry : request.getRequestParameterMap().entrySet())
    {
      RequestParameter param = entry.getValue()[0];
      if (param.isFormField() && entry.getKey().startsWith(Constants.CP_NODE_PROPERTY_PREFIX))
      {
        properties.put(entry.getKey(), param.getString());
      }
    }
    return properties;
  }

}
