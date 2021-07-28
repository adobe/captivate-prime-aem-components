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

package com.adobe.prime.core.entity;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.google.gson.Gson;

public class EmbeddableWidgetOptionsTest
{

  private String configs;

  @BeforeEach
  public void setUp()
  {
    configs =
        "[{\"name\":\"General Settings\",\"ref\":\"com.adobe.captivateprime\",\"widgetRef\":\"com.adobe.captivateprime.widgetRef\",\"description\":\"These are all the common settings applicable for all the widgets\",\"type\":\"general\",\"options\":[{\"name\":\"Access Token\",\"description\":\"Access token that will be used\",\"ref\":\"auth.accessToken\",\"type\":\"string\",\"mandatory\":false},{\"name\":\"Host Name\",\"ref\":\"commonConfig.captivateHostName\",\"description\":\"Host name to be used. This can be a custom host name as well, which can be used to direct all calls to custom backend\",\"type\":\"string\",\"default\":\"https://captivateprime.adobe.com\",\"mandatory\":true},{\"name\":\"Prime API Prefix\",\"ref\":\"commonConfig.primeapiPrefix\",\"description\":\"Prime API prefix that should be used. This will be useful in case API calls need to be directed to custom backend\",\"type\":\"string\",\"default\":\"/primeapi/v2\",\"mandatory\":false},{\"name\":\"Emit Page Link Events\",\"ref\":\"commonConfig.emitPageLinkEvents\",\"description\":\"This option tells whether to send the link click events to Adobe Captivate Prime application or to pass them to parent window. If enabled, clients can build custom redirects to other pages like Course page etc.\",\"type\":\"top|true\",\"default\":\"true\",\"mandatory\":false},{\"name\":\"Emit Player Launch Events\",\"ref\":\"commonConfig.emitPlayerLaunchEvent\",\"description\":\"This option tells whether to send the player launch link click events to Adobe Captivate Prime application or to pass them to parent window. If enabled, clients can build custom redirects to other pages like Course page etc.\",\"type\":\"boolean\",\"default\":true,\"hidden\":true,\"mandatory\":false},{\"name\":\"Disable/Hide Captivate Prime Links\",\"ref\":\"commonConfig.disableLinks\",\"description\":\"This option can be used to disable/hide links in few independent widgets\",\"type\":\"boolean\",\"default\":true,\"mandatory\":false},{\"name\":\"Primary Color\",\"ref\":\"theme.primaryColor\",\"description\":\"Primary color\",\"type\":\"color\",\"default\":\"rgb(38,118,255)\",\"mandatory\":false},{\"name\":\"Seconday Color\",\"ref\":\"theme.secondaryColor\",\"description\":\"Seconday color\",\"type\":\"color\",\"default\":\"rgb(0,145,255)\",\"mandatory\":false},{\"name\":\"Background Color\",\"ref\":\"theme.background\",\"description\":\"Background css style\",\"type\":\"string\",\"default\":\"transparent\",\"hidden\":true,\"mandatory\":false},{\"name\":\"Font URL\",\"ref\":\"theme.globalCssText\",\"description\":\"Import Font URL. Example: '@import url('https://fonts.googleapis.com/css2?family=Grandstander:ital,wght@0,100;0,200;0,300;0,400;0,500;0,600;0,700;0,800;0,900;1,100;1,200;1,300;1,400;1,500;1,600;1,700;1,800;1,900&family=Montserrat:ital,wght@0,100;0,200;0,300;0,400;0,500;0,600;0,700;0,800;0,900;1,100;1,200;1,300;1,400;1,500;1,600;1,700;1,800;1,900&display=swap');'\",\"type\":\"string\",\"default\":\"\",\"mandatory\":false},{\"name\":\"Font Name\",\"ref\":\"theme.fontNames\",\"description\":\"Add font name. Example: 'Grandstander'\",\"type\":\"string\",\"default\":\"\",\"mandatory\":false}]},{\"name\":\"Admin Recommendation\",\"ref\":\"com.adobe.captivateprime.primeStrip\",\"widgetRef\":\"com.adobe.captivateprime.lostrip.adminreco\",\"description\":\"Admin pushed recommendations. Admin will be able to push these Trainings through Announcements and can target different groups.\",\"type\":\"widget\",\"options\":[]},{\"name\":\"My Learning\",\"ref\":\"com.adobe.captivateprime.primeStrip\",\"widgetRef\":\"com.adobe.captivateprime.lostrip.mylearning\",\"description\":\"My Learning List which shows enrollments for the logged in user\",\"type\":\"widget\",\"options\":[]},{\"name\":\"Trending\",\"ref\":\"com.adobe.captivateprime.primeStrip\",\"widgetRef\":\"com.adobe.captivateprime.lostrip.trending\",\"description\":\"Trending Trainings recommended for the logged in user\",\"type\":\"widget\",\"options\":[{\"name\":\"Number of Rows\",\"ref\":\"widgetConfig.attributes.numRows\",\"description\":\"Number of rows to be displayed\",\"type\":\"1|2\",\"default\":\"1\",\"mandatory\":false}]},{\"name\":\"My Interests\",\"ref\":\"com.adobe.captivateprime.primeStrip\",\"widgetRef\":\"com.adobe.captivateprime.lostrip.myinterest\",\"description\":\"Trainings recommended based on the areas of interest chosen for the logged in user\",\"type\":\"widget\",\"options\":[]},{\"name\":\"Catalog\",\"ref\":\"com.adobe.captivateprime.primeStrip\",\"widgetRef\":\"com.adobe.captivateprime.lostrip.catalog\",\"description\":\"Trainings from a specific or a set of catalogs visible to the logged-in user\",\"type\":\"widget\",\"options\":[{\"name\":\"Catalog Ids\",\"ref\":\"widgetConfig.attributes.catalogIds\",\"description\":\"Comma-separated catalog ids for which the trainings needs to be displayed\",\"type\":\"string\",\"default\":\"\",\"mandatory\":false},{\"name\":\"Sort\",\"ref\":\"widgetConfig.attributes.sort\",\"description\":\"Sort order for the trainging\",\"type\":\"name|date|dateCreated|dateEnrolled|-name|-date|dueDate|-dateCreated|-dateEnrolled|effectiveness|rating|-rating\",\"default\":\"name\",\"mandatory\":false},{\"name\":\"Skill Name\",\"ref\":\"widgetConfig.attributes.skillName\",\"description\":\"Mention the exact skill name to filter results.\",\"type\":\"string\",\"default\":\"\",\"mandatory\":false},{\"name\":\"Tag Name\",\"ref\":\"widgetConfig.attributes.tagName\",\"description\":\"Mention the exact tag name to filter results.\",\"type\":\"string\",\"default\":\"\",\"mandatory\":false},{\"name\":\"Learning Object Types\",\"ref\":\"widgetConfig.attributes.loTypes\",\"description\":\"Choose the filter based on the type of learning object. Supported types are: course,certification,jobAid,learningProgram\",\"type\":\"string\",\"default\":\"course,certification,jobAid,learningProgram\",\"mandatory\":false},{\"name\":\"Heading of strip\",\"ref\":\"widgetConfig.attributes.heading\",\"description\":\"Give a custom heading to the strip\",\"type\":\"string\",\"default\":\"Catalog\",\"mandatory\":false}]},{\"name\":\"Calendar\",\"ref\":\"com.adobe.captivateprime.calendar\",\"widgetRef\":\"com.adobe.captivateprime.calendar\",\"description\":\"Classroom session calendar\",\"type\":\"widget\",\"options\":[]},{\"name\":\"Catalog Browser\",\"ref\":\"com.adobe.captivateprime.primeStrip\",\"widgetRef\":\"com.adobe.captivateprime.lostrip.browsecatalog\",\"description\":\"Browse Catalogs\",\"type\":\"widget\",\"options\":[]},{\"name\":\"Social Widget\",\"ref\":\"com.adobe.captivateprime.social\",\"widgetRef\":\"com.adobe.captivateprime.social\",\"description\":\"Social Posts\",\"type\":\"widget\",\"options\":[]},{\"name\":\"Leaderboard\",\"ref\":\"com.adobe.captivateprime.leaderboard\",\"widgetRef\":\"com.adobe.captivateprime.leaderboard\",\"description\":\"Leaderboard\",\"type\":\"widget\",\"options\":[]}]";
  }

  @Test
  public void testConfigOptionsParse()
  {
    Gson gson = new Gson();
    List<EmbeddableWidgetsConfig> widgetsConfig = Arrays.asList(gson.fromJson(configs, EmbeddableWidgetsConfig[].class));
    assertNotNull(widgetsConfig);
    assertNotNull(widgetsConfig.get(0).getOptions());
  }

  @Test
  public void testGeneralSettingsOptions()
  {
    Gson gson = new Gson();
    EmbeddableWidgetsConfig generalConfig = Arrays.asList(gson.fromJson(configs, EmbeddableWidgetsConfig[].class)).get(0);
    List<EmbeddableWidgetOptions> generalOptions = generalConfig.getOptions();
    EmbeddableWidgetOptions emitPlayerLaunch = generalOptions.get(4);
    assertTrue(emitPlayerLaunch.getName().equals("Emit Player Launch Events"));
    assertTrue(emitPlayerLaunch.getRef().equals("commonConfig.emitPlayerLaunchEvent"));
    assertTrue(emitPlayerLaunch.getDescription().equals(
        "This option tells whether to send the player launch link click events to Adobe Captivate Prime application or to pass them to parent window. If enabled, clients can build custom redirects to other pages like Course page etc."));
    assertTrue(emitPlayerLaunch.getType().equals("boolean"));
    assertTrue(emitPlayerLaunch.getDefaultValue().equals("true"));
    assertTrue(emitPlayerLaunch.getHidden());
    assertFalse(emitPlayerLaunch.getMandatory());
  }

}
