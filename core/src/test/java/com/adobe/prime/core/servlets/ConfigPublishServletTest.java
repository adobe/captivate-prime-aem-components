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

import static org.mockito.ArgumentMatchers.eq;

import java.lang.reflect.Field;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.day.cq.replication.ReplicationActionType;
import com.day.cq.replication.Replicator;

import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;

@ExtendWith({AemContextExtension.class, MockitoExtension.class})
class ConfigPublishServletTest
{

  private final AemContext ctx = new AemContext();

  private ConfigPublishServlet configServlet;

  @Mock
  private Replicator replicatorMock;

  @BeforeEach
  public void setUp() throws Exception
  {
    configServlet = new ConfigPublishServlet();
    
    ctx.load().json("/files/AdminConfigRsrcParent.json", "/conf/global/captivate-prime");
    ctx.load().json("/files/AdminConfigRsrc.json", "/conf/global/captivate-prime/test-config/test");
    
    Field replicatorField = ConfigPublishServlet.class.getDeclaredField("replicator");
    replicatorField.setAccessible(true);
    replicatorField.set(configServlet, replicatorMock);

    ctx.request().addRequestParameter("itemPath", "test-config");
  }

  @Test
  public void testPost() throws Exception
  {
    configServlet.doPost(ctx.request(), ctx.response());
    Mockito.verify(replicatorMock).replicate(eq(null), eq(ReplicationActionType.ACTIVATE), eq("/conf/global/captivate-prime/test-config"));
    Mockito.verify(replicatorMock).replicate(eq(null), eq(ReplicationActionType.ACTIVATE), eq("/conf/global/captivate-prime/test-config/test"));
    Mockito.verify(replicatorMock).replicate(eq(null), eq(ReplicationActionType.ACTIVATE),
        eq("/conf/global/captivate-prime/test-config/test/jcr:content"));
  }
}
