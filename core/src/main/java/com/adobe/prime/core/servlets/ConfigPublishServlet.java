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

import java.util.Iterator;

import javax.jcr.Session;
import javax.servlet.Servlet;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adobe.prime.core.Constants;
import com.day.cq.replication.ReplicationActionType;
import com.day.cq.replication.ReplicationException;
import com.day.cq.replication.Replicator;

@Component(service = Servlet.class, property = {"sling.servlet.methods=POST", "sling.servlet.resourceTypes=" + ConfigPublishServlet.RESOURCE_TYPE,
    "sling.servlet.selectors=" + "publishConfig", "sling.servlet.extensions=html"})
public class ConfigPublishServlet extends SlingAllMethodsServlet
{

  private static final long serialVersionUID = 492465267362222317L;

  final static String RESOURCE_TYPE = "sling/servlet/default";

  private final static Logger LOGGER = LoggerFactory.getLogger(ConfigPublishServlet.class);

  @Reference
  private transient Replicator replicator;

  @Override
  protected void doPost(SlingHttpServletRequest request, SlingHttpServletResponse response)
  {

    Resource configResource = null;
    String configName = request.getParameter("itemPath");
    try
    {
      ResourceResolver resolver = request.getResourceResolver();
      if (configName != null && !configName.isEmpty())
      {
        String configPath = Constants.AdminConfigurations.GLOBAL_CONFIG_CP_PATH + "/" + configName;
        configResource = resolver.getResource(configPath);
        if (configResource != null)
        {
          replicate(configResource);
        }
      }

    } catch (ReplicationException re)
    {
      LOGGER.error("ReplicationException in publishing config", re);
    }

  }

  private void replicate(Resource resource) throws ReplicationException
  {
    replicator.replicate(resource.getResourceResolver().adaptTo(Session.class), ReplicationActionType.ACTIVATE, resource.getPath());
    Iterator<Resource> it = resource.getResourceResolver().listChildren(resource);
    while (it.hasNext())
    {
      replicate(it.next());
    }
  }
}
