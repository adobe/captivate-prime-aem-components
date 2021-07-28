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

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.jcr.Session;

import org.apache.jackrabbit.api.JackrabbitSession;
import org.apache.jackrabbit.api.security.user.User;
import org.apache.jackrabbit.api.security.user.UserManager;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.settings.SlingSettingsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.adobe.prime.core.Constants;
import com.day.cq.wcm.api.Page;

import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;

@ExtendWith({AemContextExtension.class, MockitoExtension.class})
public class EmbeddableWidgetServiceImplTest
{
  private final AemContext ctx = new AemContext();
  
  private EmbeddableWidgetServiceImpl serviceImpl;

  private static final String SUBSERVICE_NAME = "writeService";
  private static final Map<String, Object> SERVICE_PARAMS =
      Collections.<String, Object>singletonMap(ResourceResolverFactory.SUBSERVICE, SUBSERVICE_NAME);

  @Mock
  private SlingSettingsService slingSettingService;

  @Mock
  private ResourceResolverFactory resolverFactory;

  @Mock
  UserManager userManager;

  @Mock
  JackrabbitSession jackSession;

  @Mock
  User user;

  @BeforeEach
  public void setUp() throws Exception
  {
    Map<String, String> pageProperties = new HashMap<>();
    pageProperties.put("cq:conf", "/conf/global/captivate-prime/testConfig");
    ctx.create().page("/content/mypage", null, pageProperties);
    ctx.currentResource("/content/mypage");
    ctx.load().json("/files/AdminConfigRsrc.json", "/conf/global/captivate-prime/testConfig/settings/cloudconfigs/cpwidget");

    lenient().when(resolverFactory.getServiceResourceResolver(SERVICE_PARAMS)).thenReturn(ctx.resourceResolver());
    ctx.registerService(ResourceResolverFactory.class, resolverFactory, org.osgi.framework.Constants.SERVICE_RANKING, Integer.MAX_VALUE);
    
    ctx.registerService(SlingSettingsService.class, slingSettingService, org.osgi.framework.Constants.SERVICE_RANKING, Integer.MAX_VALUE);

    serviceImpl = new EmbeddableWidgetServiceImpl();
    Field resourceResolverFactory = EmbeddableWidgetServiceImpl.class.getDeclaredField("resourceResolverFactory");
    resourceResolverFactory.set(serviceImpl, resolverFactory);

    Field settingsService = EmbeddableWidgetServiceImpl.class.getDeclaredField("settingsService");
    settingsService.set(serviceImpl, slingSettingService);

    ctx.load().json("/files/UserRsrc.json", "/home/user/vaishnav");
    ValueMock[] emailValues = new ValueMock[] {new ValueMock("vaishnav@adobe.com")};

    lenient().when(user.getProperty(Constants.LearnerConfigurations.USER_EMAIL_PATH)).thenReturn(emailValues);
    lenient().when(user.getPath()).thenReturn("/home/user/vaishnav");

    lenient().when(userManager.getAuthorizable(eq("vaishnav"))).thenReturn(user);
    
    lenient().when(jackSession.getUserManager()).thenReturn(userManager);
    lenient().when(jackSession.getUserID()).thenReturn("vaishnav");
    ctx.registerAdapter(ResourceResolver.class, Session.class, jackSession);

    /*
     * Field clientId = EmbeddableWidgetServiceImpl.class.getDeclaredField("clientId");
     * clientId.setAccessible(true); clientId.set(serviceImpl,
     * "c495b4f0-2f9b-441c-acd4-22dcfba84718"); Field clientSecret =
     * EmbeddableWidgetServiceImpl.class.getDeclaredField("clientSecret");
     * clientSecret.setAccessible(true); clientSecret.set(serviceImpl,
     * "c08aef1d-51d2-47d8-9b13-481dceca66d3");
     */
  }

  @Test
  public void testAccessTokenOfUserAuthorInstance()
  {
    lenient().when(slingSettingService.getRunModes()).thenReturn(Collections.singleton("author"));
    serviceImpl.getAccessTokenOfUser(ctx.request(), ctx.resourceResolver(), ctx.currentPage());
  }

  @Test
  public void testAccessTokenOfUserPublishInstance()
  {
    lenient().when(slingSettingService.getRunModes()).thenReturn(Collections.singleton("publish"));
    serviceImpl.getAccessTokenOfUser(ctx.request(), ctx.resourceResolver(), ctx.currentPage());
  }

}