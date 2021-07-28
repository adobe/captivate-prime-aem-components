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

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.junit.jupiter.MockitoExtension;

import com.adobe.granite.ui.components.ds.DataSource;
import com.adobe.granite.ui.components.ds.SimpleDataSource;
import com.day.cq.commons.jcr.JcrConstants;

import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;

@ExtendWith({AemContextExtension.class, MockitoExtension.class})
public class EmbeddableAdminConfigGetServletTest
{
  private final AemContext ctx = new AemContext();

  private EmbeddableAdminConfigGetServlet getServlet;

  @BeforeEach
  public void setUp() throws Exception
  {
    getServlet = new EmbeddableAdminConfigGetServlet();

    ctx.load().json("/files/AdminConfigRsrc.json", "/conf/global/captivate-prime/test-config/settings/cloudconfigs/cpwidget");
    ctx.load().json("/files/AdminConfigRsrc.json", "/conf/global/captivate-prime/test-config1/settings/cloudconfigs/cpwidget");
  }

  @Test
  public void testGet()
  {
    getServlet.doGet(ctx.request(), ctx.response());
    SimpleDataSource sds = (SimpleDataSource) ctx.request().getAttribute(DataSource.class.getName());
    Iterator<Resource> rsrcs = sds.iterator();
    List<String> resourcesNames = new ArrayList<>();
    while (rsrcs.hasNext())
    {
      Resource rsc = rsrcs.next();
      ValueMap map = rsc.getValueMap();
      resourcesNames.add(map.get(JcrConstants.JCR_TITLE).toString());
    }
    assertTrue(resourcesNames.contains("test-config"));
    assertTrue(resourcesNames.contains("test-config1"));
  }
}
