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

package com.adobe.prime.core.utils;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.adobe.prime.core.entity.EmbeddableWidgetsConfig;
import com.google.gson.JsonObject;

public class EmbeddableWidgetConfigUtilsTest
{

  private String hostName;
  private Map<String, Object> widgetObject;

  @BeforeEach
  public void setUp()
  {
    hostName = "https://captivateprimeqe.adobe.com";
    widgetObject = new HashMap<>();
    widgetObject.put("auth.accessToken", "1234");
    widgetObject.put("commonConfig.captivateHostName", "https://captivateprimeqe.adobe.com");
    widgetObject.put("commonConfig.disableLinks", true);
    widgetObject.put("theme.primaryColor", "rgb(38,118,255)");
    widgetObject.put("theme.background", "transparent");
  }

  @Test
  public void testEmbeddableWidgetsConfig()
  {
    List<EmbeddableWidgetsConfig> widgetsConfig = EmbeddableWidgetConfigUtils.getEmbeddableWidgetsConfig(hostName);
    assertNotNull(widgetsConfig);
    assertTrue(widgetsConfig.size() > 0);
  }

  @Test
  public void testGeneralSettingsConfig()
  {
    EmbeddableWidgetsConfig genrealConfig = EmbeddableWidgetConfigUtils.getGeneralSettingsConfig(hostName);
    assertNotNull(genrealConfig);
  }

  @Test
  public void testWidgetConfig()
  {
    JsonObject objects = EmbeddableWidgetConfigUtils.getWidgetConfig(widgetObject);
    String accessToken = objects.get("auth").getAsJsonObject().get("accessToken").getAsString();
    assertTrue(accessToken.equals("1234"));
    JsonObject commonObject = objects.get("commonConfig").getAsJsonObject();
    String hostName = commonObject.get("captivateHostName").getAsString();
    assertTrue(hostName.equals("https://captivateprimeqe.adobe.com"));
    String disableLink = commonObject.get("disableLinks").getAsString();
    assertTrue(disableLink.equals("true"));
    JsonObject themeObject = objects.get("theme").getAsJsonObject();
    String primaryColor = themeObject.get("primaryColor").getAsString();
    assertTrue(primaryColor.equals("rgb(38,118,255)"));
    String background = themeObject.get("background").getAsString();
    assertTrue(background.equals("transparent"));
    assertNotNull(objects);
  }

}
