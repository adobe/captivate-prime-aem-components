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

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.lenient;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.sling.api.resource.ResourceResolverFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;

@ExtendWith({AemContextExtension.class, MockitoExtension.class})
public class EmbeddableWidgetConfigurationServiceImplTest
{
  private final AemContext ctx = new AemContext();
  
  private EmbeddableWidgetConfigurationServiceImpl configServiceImpl;

  private static final String SUBSERVICE_NAME = "writeService";
  private static final Map<String, Object> SERVICE_PARAMS =
      Collections.<String, Object>singletonMap(ResourceResolverFactory.SUBSERVICE, SUBSERVICE_NAME);

  @Mock
  private ResourceResolverFactory resolverFactory;

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

    configServiceImpl = new EmbeddableWidgetConfigurationServiceImpl();
    Field resourceResolverFactory = EmbeddableWidgetConfigurationServiceImpl.class.getDeclaredField("resourceResolverFactory");
    resourceResolverFactory.set(configServiceImpl, resolverFactory);

  }

  @Test
  public void testGetAvailaleAdminConfiguration()
  {
	  Map<String, Object> adminConfigs = configServiceImpl.getAvailaleAdminConfiguration(ctx.currentResource());
	  assertTrue("test1".equals(adminConfigs.get("testConfig").toString()));
	  assertTrue("cq:PageContent".equals(adminConfigs.get("jcr:primaryType").toString()));
	  assertTrue("clientSecret".equals(adminConfigs.get("cpWidget#clientSecret").toString()));
  }

  @Test
  public void testGetGeneralConfigs()
  {
	  Map<String, Object> generalConfigs = configServiceImpl.getGeneralConfigs(ctx.currentResource());
	  assertTrue(generalConfigs.get("testConfig") == null);
	  assertTrue("transparent".equals(generalConfigs.get("theme.background").toString()));
	  assertTrue("https://captivateprimeqe.adobe.com".equals(generalConfigs.get("commonConfig.captivateHostName").toString()));
  }

}