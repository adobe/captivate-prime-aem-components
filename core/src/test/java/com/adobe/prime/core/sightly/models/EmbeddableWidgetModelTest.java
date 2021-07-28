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

package com.adobe.prime.core.sightly.models;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.scripting.SlingBindings;
import org.apache.sling.testing.mock.sling.ResourceResolverType;
import org.junit.Rule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.adobe.prime.core.Constants;
import com.adobe.prime.core.services.EmbeddableWidgetService;
import com.adobe.prime.core.servlets.EmbeddableAdminConfigDsServlet;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.scripting.WCMBindingsConstants;

import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;

@ExtendWith({AemContextExtension.class, MockitoExtension.class})
public class EmbeddableWidgetModelTest
{
  @Rule
  private final AemContext ctx = new AemContext(ResourceResolverType.JCR_MOCK);

  private EmbeddableWidgetModel widgetModel;
  private Page page;
  private Resource resource;

  @Mock
  private EmbeddableWidgetService widgetService;

  @Mock
  private Page currentPage;

  @BeforeEach
  void setUp()
  {
    ctx.addModelsForClasses(EmbeddableWidgetModel.class);
    page = ctx.create().page("/content/mypage");
    resource = ctx.create().resource(page, "embeddablewidget", "sling:resourceType", "cprime/components/embeddablewidget");

    lenient().when(widgetService.getAccessTokenOfUser(eq(ctx.request()), eq(ctx.request().getResourceResolver()), any(Page.class)))
        .thenReturn("123456");
    lenient().when(widgetService.getDefaultHostName()).thenReturn("https://captivateprimeqe.adobe.com");
    Map<String, Object> adminConfigs = new HashMap<>();
    adminConfigs.put(Constants.CP_NODE_PROPERTY_PREFIX + "commonConfig.captivateHostName", "https://captivateprimeqe.adobe.com");
    adminConfigs.put(Constants.CP_NODE_PROPERTY_PREFIX + "refreshToken", "1234");
    adminConfigs.put(Constants.CP_NODE_PROPERTY_PREFIX + "theme.background", "transparent");
    lenient().when(widgetService.getAvailaleAdminConfiguration(any(Resource.class))).thenReturn(adminConfigs);
    lenient().when(widgetService.getGeneralConfigs(any(Resource.class))).thenReturn(adminConfigs);
    lenient().when(widgetService.isAuthorMode()).thenReturn(true);
    ctx.registerService(EmbeddableWidgetService.class, widgetService, org.osgi.framework.Constants.SERVICE_RANKING, Integer.MAX_VALUE);

    SlingBindings slingBindings = (SlingBindings) ctx.request().getAttribute(SlingBindings.class.getName());
    slingBindings.put(WCMBindingsConstants.NAME_CURRENT_PAGE, currentPage);
    ctx.request().setAttribute(SlingBindings.class.getName(), slingBindings);

    ctx.load().json("/files/widgetModelTest.json", "/content/prime");
    ctx.currentResource("/content/prime/widgetModel");
    widgetModel = ctx.request().adaptTo(EmbeddableWidgetModel.class);

  }

  @Test
  void testWidgetConfigs()
  {
    String expectedConfigs =
        "{\"widgetRefSelected\":\"\\\"com.adobe.captivateprime.lostrip.trending\\\"\",\"auth\":{\"accessToken\":\"123456\"},\"type\":\"acapConfig\",\"widgetConfig\":{\"widgetRef\":\"com.adobe.captivateprime.lostrip.trending\"}}";
    String configs = widgetModel.getWidgetConfigs();
    assertTrue(expectedConfigs.equals(configs));
  }

  @Test
  void testProperties()
  {
    String expectedProperties =
        "{\"cpWidget#widgetRefSelected\":\"com.adobe.captivateprime.lostrip.trending\",\"name\":\"Admin Recommendation\",\"sling:resourceType\":\"cprime/components/widget\",\"jcr:primaryType\":\"nt:unstructured\"}";
    String properties = widgetModel.getProperties();
    assertTrue(properties.contains("com.adobe.captivateprime.lostrip.trending"));
    assertTrue(properties.contains("Admin Recommendation"));
    assertTrue(properties.contains("cprime/components/widget"));
  }

  @Test
  void testWidgetSrcUrl()
  {
    String expectedWidgteSrcUrl =
        "https://captivateprimeqe.adobe.com/app/embeddablewidget?widgetRef=com.adobe.captivateprime.primeStrip&resourceType=html";
    String widgetSrcUrl = widgetModel.getWidgetSrcUrl();
    assertTrue(expectedWidgteSrcUrl.equals(widgetSrcUrl));
  }

  @Test
  void testSelectedRef()
  {
    String expectedSelectedRef = "com.adobe.captivateprime.primeStrip";
    String selectedRef = widgetModel.getSelectedRef();
    assertTrue(expectedSelectedRef.equals(selectedRef));
  }

  @Test
  void testRunMode()
  {
    String expectedRunMode = "author";
    String runMode = widgetModel.getRunMode();
    assertTrue(expectedRunMode.equals(runMode));
  }

  @Test
  void testWidgetCommunicatorUrl()
  {
    String expectedWidgetCommUrl = "https://captivateprimeqe.adobe.com/app/embeddablewidget?widgetRef=com.adobe.captivateprime.widgetcommunicator";
    String widgetCommUrl = widgetModel.getWidgetCommunicatorUrl();
    assertTrue(expectedWidgetCommUrl.equals(widgetCommUrl));
  }
}
