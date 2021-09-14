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

import java.util.Collections;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.jackrabbit.api.security.user.User;
import org.apache.jackrabbit.api.security.user.UserManager;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.jcr.base.util.AccessControlUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adobe.prime.core.Constants;
import com.day.cq.wcm.api.Page;

@Component(metatype = false, immediate = true)
@Service(value = EmbeddableWidgetUserService.class)
public class EmbeddableWidgetUserServiceImpl implements EmbeddableWidgetUserService
{

  @Reference
  ResourceResolverFactory resourceResolverFactory;

  @Reference
  private transient EmbeddableWidgetConfigurationService widgetConfigService;

  private static final Logger LOGGER = LoggerFactory.getLogger(EmbeddableWidgetUserServiceImpl.class);
  private static final String SUBSERVICE_NAME = "writeService";
  private static final Map<String, Object> SERVICE_PARAMS =
      Collections.<String, Object>singletonMap(ResourceResolverFactory.SUBSERVICE, SUBSERVICE_NAME);

  @Override
  public String getUserEmail(SlingHttpServletRequest request)
  {
    try
    {
      Session session = request.getResourceResolver().adaptTo(Session.class);
      UserManager userManager = AccessControlUtil.getUserManager(session);
      User currentUser = (User) userManager.getAuthorizable(session.getUserID());
      LOGGER.trace("EmbeddableWidgetUserServiceImpl getUserEmail:: currentUser {} Path {} userId {}", currentUser, currentUser.getPath(),
          session.getUserID());

      String email = currentUser.getProperty(Constants.LearnerConfigurations.USER_EMAIL_PATH) != null
          ? currentUser.getProperty(Constants.LearnerConfigurations.USER_EMAIL_PATH)[0].toString()
          : "";
      return email;
    } catch (RepositoryException re)
    {
      LOGGER.error("EmbeddableWidgetUserServiceImpl getUserEmail:: Exception in getting email of user.", re);
    }
    return "";

  }

  @Override
  public Pair<String, Long> getAccessTokenWithExpiry(SlingHttpServletRequest request, Page currentPage, String email)
  {
    ResourceResolver adminResolver = null;
    String accessToken = "";
    long expiryMilliSecond = 0L;

    try
    {
      adminResolver = resourceResolverFactory.getServiceResourceResolver(SERVICE_PARAMS);
      Resource pageRsc = adminResolver.getResource(currentPage.getPath());
      LOGGER.trace("EmbeddableWidgetUserServiceImpl getAccessTokenOfUser:: CurrentPage {} PageRsc {}", currentPage.getPath(), pageRsc.getPath());
      Map<String, Object> adminConfigs = widgetConfigService.getAvailaleAdminConfiguration(pageRsc);

      if (adminConfigs.isEmpty())
      {
        LOGGER.error("EmbeddableWidgetServiceImpl getAccessTokenWithExpiry:: Got empty admin configs.");
        return null;
      }

      String refreshToken = adminConfigs.get(Constants.CP_NODE_PROPERTY_PREFIX + Constants.AdminConfigurations.ADMIN_CONFIG_REFRESH_TOKEN).toString();

      Session session = request.getResourceResolver().adaptTo(Session.class);
      UserManager userManager = AccessControlUtil.getUserManager(session);
      User currentUser = (User) userManager.getAuthorizable(session.getUserID());
      LOGGER.trace("EmbeddableWidgetServiceImpl getAccessTokenWithExpiry:: currentUser {} Path {} userId {}", currentUser, currentUser.getPath(),
          session.getUserID());

      final String tokenSpecificPath = "_" + DigestUtils.sha512Hex(refreshToken);

      accessToken = currentUser.getProperty(Constants.LearnerConfigurations.USER_ACCESS_TOKEN_PATH + tokenSpecificPath) != null
          ? currentUser.getProperty(Constants.LearnerConfigurations.USER_ACCESS_TOKEN_PATH + tokenSpecificPath)[0].toString()
          : "";

      expiryMilliSecond = currentUser.getProperty(Constants.LearnerConfigurations.USER_ACCESS_TOKEN_EXPIRY_PATH + tokenSpecificPath) != null
          ? Long.valueOf(currentUser.getProperty(Constants.LearnerConfigurations.USER_ACCESS_TOKEN_EXPIRY_PATH + tokenSpecificPath)[0].toString())
          : 0L;

      long currentTime = currentTimeMillis();

      if (accessToken == null || accessToken.isEmpty() || (currentTime > expiryMilliSecond))
      {
        return null;
      }
    } catch (RepositoryException | LoginException exc)
    {
      LOGGER.error("EmbeddableWidgetServiceImpl getAccessTokenWithExpiry:: Exception in fetching access token.", exc);
    } finally
    {
    	if (adminResolver != null)
    	{
    		adminResolver.close();
    	}
    }

    return new ImmutablePair<>(accessToken, expiryMilliSecond);
  }

  @Override
  public boolean setAccessTokenWithExpiry(SlingHttpServletRequest request, Page currentPage, String accessToken, Long expiryMilliSecond, String email)
  {
    ResourceResolver adminResolver = null;

    try
    {
      adminResolver = resourceResolverFactory.getServiceResourceResolver(SERVICE_PARAMS);
      
      Resource pageRsc = adminResolver.getResource(currentPage.getPath());
      Map<String, Object> adminConfigs = widgetConfigService.getAvailaleAdminConfiguration(pageRsc);
      if (adminConfigs.isEmpty())
      {
        LOGGER.error("EmbeddableWidgetServiceImpl setAccessTokenWithExpiry:: Got empty admin configs.");
        return false;
      }

      final String refreshToken =
          adminConfigs.get(Constants.CP_NODE_PROPERTY_PREFIX + Constants.AdminConfigurations.ADMIN_CONFIG_REFRESH_TOKEN).toString();
      final String tokenSpecificPath = "_" + DigestUtils.sha512Hex(refreshToken);
          
      Session session = request.getResourceResolver().adaptTo(Session.class);
      UserManager userManager = AccessControlUtil.getUserManager(session);
      User currentUser = (User) userManager.getAuthorizable(session.getUserID());
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
          return true;
        }
      }
    } catch (RepositoryException | LoginException | PersistenceException exc)
    {
      LOGGER.error("EmbeddableWidgetServiceImpl setAccessTokenWithExpiry:: Exception in setting access token of user", exc);
    } finally
    {
      if (adminResolver != null)
      {
        adminResolver.close();
      }
    }
    LOGGER.error("EmbeddableWidgetServiceImpl setAccessTokenWithExpiry:: Unable to set access token of user");
    return false;
  }

}
