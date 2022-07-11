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

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.testing.mock.sling.ResourceResolverType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;

@ExtendWith({AemContextExtension.class, MockitoExtension.class})
public class EmbeddableAdminConfigPostServletTest
{
  private final AemContext ctx = new AemContext(ResourceResolverType.JCR_MOCK);

  private EmbeddableAdminConfigPostServlet postServlet;

  @BeforeEach
  public void setUp()
  {
    postServlet = new EmbeddableAdminConfigPostServlet();
    ctx.load().json("/files/AdminConfigRsrc.json", "/conf/global/captivate-prime/test-config/settings/cloudconfigs/cpwidget");

    ctx.request().addRequestParameter("item", "test-config");
    ctx.request().addRequestParameter("cpWidget#refreshToken", "12345");
    ctx.request().addRequestParameter("cpWidget#commonConfig.disableLinks", "true");
    ctx.request().addRequestParameter("cpWidget#commonConfig.disableLinks@TypeHint", "boolean");
    ctx.request().addRequestParameter("cpWidget#commonConfig.value", "5678");
    ctx.request().addRequestParameter("cpWidget#commonConfig.value@TypeHint", "number");
    ctx.request().addRequestParameter("cpWidget#commonConfig.captivateHostName", "https://learningmanagerqe.adobe.com");
    ctx.request().addRequestParameter("cpWidget#accountId", "7110");
    ctx.request().addRequestParameter("cpWidget#theme.background@Delete", "");
  }
  
  @Test
  public void testPost()
  {
    postServlet.doPost(ctx.request(), ctx.response());
    Resource rsc =
        ctx.request().getResourceResolver().getResource("/conf/global/captivate-prime/test-config/settings/cloudconfigs/cpwidget/jcr:content");
    ValueMap vm = rsc.getValueMap();
    assertTrue(vm.get("cpWidget#refreshToken").equals("12345"));
    assertTrue(vm.get("cpWidget#commonConfig.disableLinks").equals(true));
    assertTrue(vm.get("cpWidget#commonConfig.value").equals(5678l));
    assertTrue(vm.get("cpWidget#theme.background").equals(false));
  }

}
