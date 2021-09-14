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

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.adobe.granite.ui.components.ds.DataSource;
import com.adobe.granite.ui.components.ds.SimpleDataSource;
import com.adobe.prime.core.Constants;
import com.adobe.prime.core.services.EmbeddableWidgetConfigurationService;
import com.adobe.prime.core.services.EmbeddableWidgetService;

import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;

@ExtendWith({AemContextExtension.class, MockitoExtension.class})
public class EmbeddableAdminConfigDsServletTest
{
  private final AemContext ctx = new AemContext();

  private EmbeddableAdminConfigDsServlet dsServlet;

  @Captor
  ArgumentCaptor<SimpleDataSource> simpleDS;

  @Captor
  ArgumentCaptor<String> ds;

  @Mock
  private EmbeddableWidgetService widgetService;

  @Mock
  private EmbeddableWidgetConfigurationService widgetConfigService;

  @BeforeEach
  public void setUp() throws Exception
  {
    dsServlet = new EmbeddableAdminConfigDsServlet();

    Map<String, Object> adminConfigs = new HashMap<>();
    adminConfigs.put(Constants.CP_NODE_PROPERTY_PREFIX + "commonConfig.captivateHostName", "https://captivateprimeqe.adobe.com");
    adminConfigs.put(Constants.CP_NODE_PROPERTY_PREFIX + "refreshToken", "f85a9acef88772630c7a55ea3ed9db96");
    adminConfigs.put(Constants.CP_NODE_PROPERTY_PREFIX + "theme.background", "transparent");
    lenient().when(widgetConfigService.getAvailaleAdminConfiguration(any(Resource.class))).thenReturn(adminConfigs);
    lenient().when(widgetService.getDefaultHostName()).thenReturn("https://captivateprimeqe.adobe.com");

    Field replicatorField = EmbeddableAdminConfigDsServlet.class.getDeclaredField("widgetService");
    replicatorField.setAccessible(true);
    replicatorField.set(dsServlet, widgetService);

    ctx.registerService(EmbeddableWidgetService.class, widgetService, org.osgi.framework.Constants.SERVICE_RANKING, Integer.MAX_VALUE);

    ctx.load().json("/files/AdminConfigRsrc.json", "/conf/global/captivate-prime/test-config/settings/cloudconfigs/cpwidget");

    ctx.request().addRequestParameter("item", "test-config");
  }

  @Test
  public void testGet()
  {
    dsServlet.doGet(ctx.request(), ctx.response());
    SimpleDataSource sds = (SimpleDataSource) ctx.request().getAttribute(DataSource.class.getName());
    Iterator<Resource> rsrcs = sds.iterator();
    List<String> resourcesNames = new ArrayList<>();
    while (rsrcs.hasNext())
    {
      Resource rsc = rsrcs.next();
      ValueMap map = rsc.getValueMap();
      resourcesNames.add(map.get("name").toString());
    }
    assertTrue(resourcesNames.contains("cpWidget#refreshToken"));
    assertTrue(resourcesNames.contains("cpWidget#title"));
    assertTrue(resourcesNames.contains("cpWidget#theme.background"));
    assertTrue(resourcesNames.contains("cpWidget#commonConfig.captivateHostName"));
    resourcesNames.size();
  }
}
