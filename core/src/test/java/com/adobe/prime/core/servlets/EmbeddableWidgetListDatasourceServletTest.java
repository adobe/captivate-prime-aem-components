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
public class EmbeddableWidgetListDatasourceServletTest
{
  private final AemContext ctx = new AemContext();

  private EmbeddableWidgetListDatasourceServlet dsServlet;

  @Mock
  private EmbeddableWidgetConfigurationService widgetConfigService;

  @BeforeEach
  public void setUp() throws Exception
  {
    dsServlet = new EmbeddableWidgetListDatasourceServlet();

    Map<String, Object> adminConfigs = new HashMap<>();
    adminConfigs.put(Constants.CP_NODE_PROPERTY_PREFIX + "commonConfig.captivateHostName", "https://captivateprimeqe.adobe.com");
    adminConfigs.put(Constants.CP_NODE_PROPERTY_PREFIX + "refreshToken", "f85a9acef88772630c7a55ea3ed9db96");
    adminConfigs.put(Constants.CP_NODE_PROPERTY_PREFIX + "theme.background", "transparent");
    lenient().when(widgetConfigService.getAvailaleAdminConfiguration(any(Resource.class))).thenReturn(adminConfigs);

    Field replicatorField = EmbeddableWidgetListDatasourceServlet.class.getDeclaredField("widgetConfigService");
    replicatorField.setAccessible(true);
    replicatorField.set(dsServlet, widgetConfigService);

    ctx.registerService(EmbeddableWidgetConfigurationService.class, widgetConfigService, org.osgi.framework.Constants.SERVICE_RANKING,
        Integer.MAX_VALUE);

    ctx.requestPathInfo().setSuffix("/widget/page");
    ctx.load().json("/files/AdminConfigRsrc.json", "/widget/page");
  }

  @Test
  public void testGet()
  {
    dsServlet.doGet(ctx.request(), ctx.response());
    SimpleDataSource sds = (SimpleDataSource) ctx.request().getAttribute(DataSource.class.getName());
    Iterator<Resource> rsrcs = sds.iterator();
    List<String> resourcesValues = new ArrayList<>();
    while (rsrcs.hasNext())
    {
      Resource rsc = rsrcs.next();
      ValueMap map = rsc.getValueMap();
      if (map.get("value") != null && !map.get("value").toString().isEmpty())
      {
        resourcesValues.add(map.get("value").toString());
      }
    }
    assertTrue(resourcesValues.contains("com.adobe.captivateprime.lostrip.adminreco"));
    assertTrue(resourcesValues.contains("com.adobe.captivateprime.lostrip.mylearning"));
    assertTrue(resourcesValues.contains("com.adobe.captivateprime.lostrip.trending"));
    assertTrue(resourcesValues.contains("com.adobe.captivateprime.lostrip.myinterest"));
  }

}
