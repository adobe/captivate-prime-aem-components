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

import static java.lang.System.currentTimeMillis;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.http.ParseException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.jackrabbit.api.security.user.User;
import org.apache.jackrabbit.api.security.user.UserManager;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.jcr.base.util.AccessControlUtil;
import org.apache.sling.settings.SlingSettingsService;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adobe.prime.core.Constants;
import com.day.cq.commons.inherit.HierarchyNodeInheritanceValueMap;
import com.day.cq.commons.inherit.InheritanceValueMap;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

@Component(metatype = true, immediate = true, label = "Captivate Prime Embeddable Widget Service",
    description = "Captivate Prime Embeddable Widget Service")
@Service(value = EmbeddableWidgetService.class)
public class EmbeddableWidgetServiceImpl implements EmbeddableWidgetService
{

  @Reference
  ResourceResolverFactory resourceResolverFactory;

  @Reference
  protected SlingSettingsService settingsService;

  private static final Logger LOGGER = LoggerFactory.getLogger(EmbeddableWidgetServiceImpl.class);
  private static final String SUBSERVICE_NAME = "writeService";
  private static final Map<String, Object> SERVICE_PARAMS =
      Collections.<String, Object>singletonMap(ResourceResolverFactory.SUBSERVICE, SUBSERVICE_NAME);

  private static final long ACCESS_TOKEN_EXPIRY_BUFFER_MS = 3600000; // 1Hr
  private final static String DEFAULT_HOST = "https://captivateprime.adobe.com";

  @Property(label = "HostName", description = "Provide hostname to fetch configs in the format (https://captivateprime.adobe.com).",
      value = DEFAULT_HOST)
  private static final String CONFIG_HOST_NAME = "config.hostname";
  private String configHostName;

  protected void activate(ComponentContext componentContext)
  {
    Dictionary<String, Object> properties = componentContext.getProperties();
    configHostName = properties.get(CONFIG_HOST_NAME) != null ? properties.get(CONFIG_HOST_NAME).toString() : DEFAULT_HOST;
  }

  @Override
  public boolean isAuthorMode()
  {
    return settingsService.getRunModes().contains(Constants.RUNMODE_AUTHOR);
  }

  @Override
  public String getAccessTokenOfUser(SlingHttpServletRequest request, ResourceResolver currentUserResolver, Page currentPage)
  {
    ResourceResolver adminResolver = null;
    try
    {
      adminResolver = resourceResolverFactory.getServiceResourceResolver(SERVICE_PARAMS);
      Resource pageRsc = adminResolver.getResource(currentPage.getPath());
      LOGGER.trace("EmbeddableWidgetServiceImpl getAccessTokenOfUser:: CurrentPage {} PageRsc {}", currentPage.getPath(), pageRsc.getPath());
      Map<String, Object> adminConfigs = getAvailaleAdminConfiguration(pageRsc);

      if (adminConfigs.isEmpty())
      {
        LOGGER.error("EmbeddableWidgetServiceImpl getAccessTokenOfUser:: Got empty admin configs.");
        return "";
      }

      String hostName = adminConfigs.get(Constants.AdminConfigurations.ADMIN_CONFIG_HOST_NAME).toString();
      String refreshToken =
          adminConfigs.get(Constants.CP_NODE_PROPERTY_PREFIX + Constants.AdminConfigurations.ADMIN_CONFIG_REFRESH_TOKEN).toString();
      String clientId = adminConfigs.get(Constants.CP_NODE_PROPERTY_PREFIX + Constants.AdminConfigurations.ADMIN_CONFIG_CLIENT_ID).toString();
      String clientSecret =
          adminConfigs.get(Constants.CP_NODE_PROPERTY_PREFIX + Constants.AdminConfigurations.ADMIN_CONFIG_CLIENT_SECRET).toString();

      currentUserResolver = request.getResourceResolver();
      Session session = currentUserResolver.adaptTo(Session.class);
      UserManager userManager = AccessControlUtil.getUserManager(session);
      User currentUser = (User) userManager.getAuthorizable(session.getUserID());
      LOGGER.trace("EmbeddableWidgetServiceImpl getAccessTokenOfUser:: currentUser {} Path {} userId {}", currentUser, currentUser.getPath(),
          session.getUserID());

      String email = currentUser.getProperty(Constants.LearnerConfigurations.USER_EMAIL_PATH) != null
          ? currentUser.getProperty(Constants.LearnerConfigurations.USER_EMAIL_PATH)[0].toString()
          : "";

      LOGGER.debug("EmbeddableWidgetServiceImpl getAccessTokenOfUser:: Current user Email {}", email);
      if (email == null || email.isEmpty())
      {
        return "";
      }

      final String tokenSpecificPath = "_" + DigestUtils.sha512Hex(refreshToken);

      String accessToken = currentUser.getProperty(Constants.LearnerConfigurations.USER_ACCESS_TOKEN_PATH + tokenSpecificPath) != null
          ? currentUser.getProperty(Constants.LearnerConfigurations.USER_ACCESS_TOKEN_PATH + tokenSpecificPath)[0].toString()
          : null;
      long expiryMilliSecond = currentUser.getProperty(Constants.LearnerConfigurations.USER_ACCESS_TOKEN_EXPIRY_PATH + tokenSpecificPath) != null
          ? Long.valueOf(currentUser.getProperty(Constants.LearnerConfigurations.USER_ACCESS_TOKEN_EXPIRY_PATH + tokenSpecificPath)[0].toString())
          : 0;
      long currentTime = currentTimeMillis();

      if (accessToken == null || accessToken.isEmpty() || (currentTime > expiryMilliSecond))
      {
        LOGGER.debug("EmbeddableWidgetServiceImpl getAccessTokenOfUser:: Fetching Access Token");
        String accessTokenResponse = fetchAccessToken(hostName, clientId, clientSecret, refreshToken, email, false);
        Pair<String, Long> resp = getTokenAndExpiry(accessTokenResponse);

        if (resp == null)
        {
          LOGGER.error("EmbeddableWidgetServiceImpl getAccessTokenOfUser:: Exception in fetching access_token. Response- {}", accessTokenResponse);
          return "";
        }

        if (currentTime > resp.getRight())
        {
          LOGGER.debug("EmbeddableWidgetServiceImpl getAccessTokenOfUser:: Force fetch token");
          accessTokenResponse = fetchAccessToken(hostName, clientId, clientSecret, refreshToken, email, true);
          resp = getTokenAndExpiry(accessTokenResponse);

          if (resp == null)
          {
            LOGGER.error("EmbeddableWidgetServiceImpl getAccessTokenOfUser:: Exception in force fetching access_token. Response- {}",
                accessTokenResponse);
            return "";
          }
        }

        accessToken = resp.getLeft();
        expiryMilliSecond = resp.getRight();

        String userNodeProfilePath = currentUser.getPath() + "/profile";
        Resource userNodeRsc = adminResolver.getResource(userNodeProfilePath);
        if (userNodeRsc != null)
        {
          Node userProfileNode = userNodeRsc.adaptTo(Node.class);
          if (userProfileNode != null)
          {
            userProfileNode.setProperty((Constants.LearnerConfigurations.USER_ACCESS_TOKEN_STR + tokenSpecificPath), accessToken);
            userProfileNode.setProperty((Constants.LearnerConfigurations.EXPIRES_IN_STR + tokenSpecificPath), expiryMilliSecond);
            adminResolver.commit();
          }

        }
      }

      return accessToken;
    } catch (RepositoryException | PersistenceException | LoginException exc)
    {
      LOGGER.error("Exception in getting access token for user.", exc);
    } finally
    {
      if (adminResolver != null)
      {
        adminResolver.close();
      }
    }
    LOGGER.error("EmbeddableWidgetServiceImpl getAccessTokenOfUser:: Returning empty access token.");
    return "";
  }

  private Pair<String, Long> getTokenAndExpiry(String accessTokenResponse)
  {
    if (accessTokenResponse != null && !accessTokenResponse.isEmpty())
    {
      if (!accessTokenResponse.contains("access_token") || !accessTokenResponse.contains("expires_in"))
      {
        LOGGER.error("Exception in fetching access_token. Response- {}", accessTokenResponse);
        return null;
      }
      JsonObject jsonObject = new Gson().fromJson(accessTokenResponse, JsonObject.class);
      String accessToken = jsonObject.get("access_token").getAsString();
      Long expiryMilliSecond = (jsonObject.get("expires_in").getAsLong() * 1000) + currentTimeMillis() - ACCESS_TOKEN_EXPIRY_BUFFER_MS;
      return new ImmutablePair<>(accessToken, expiryMilliSecond);
    }
    return null;
  }

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

  @Override
  public String getDefaultHostName()
  {
    return configHostName;
  }

  private static String fetchAccessToken(String hostName, String clientId, String clientSecret, String refreshToken, String email, boolean force)
  {
    LOGGER.debug("EmbeddableWidgetServiceImpl FetchAccessToken:: HostName {}, email {}", hostName, email);
    try
    {
      String url = hostName
          + Constants.CPUrl.ACCESS_TOKEN_URL.replace("{email}", URLEncoder.encode(email, "UTF-8")).replace("{force}", (force ? "true" : "false"));
      HttpPost post = new HttpPost(url);
      post.setHeader("Content-Type", "application/json");

      Map<String, String> requestBodyMap = new HashMap<String, String>();
      requestBodyMap.put("client_id", clientId);
      requestBodyMap.put("client_secret", clientSecret);
      requestBodyMap.put("refresh_token", refreshToken);
      post.setEntity(new StringEntity(new Gson().toJson(requestBodyMap), ContentType.APPLICATION_JSON));

      try (CloseableHttpClient httpClient = HttpClients.createDefault(); CloseableHttpResponse response = httpClient.execute(post))
      {
        return EntityUtils.toString(response.getEntity());
      } catch (ParseException | IOException e)
      {
        LOGGER.error("Exception in http call while fetching access-token", e);
      }

    } catch (UnsupportedEncodingException use)
    {
      LOGGER.error("UnsupportedEncodingException in fetching access-token", use);
    }
    return null;
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
