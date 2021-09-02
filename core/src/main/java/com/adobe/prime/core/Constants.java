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
package com.adobe.prime.core;

import com.day.cq.commons.jcr.JcrConstants;

public final class Constants
{
  public static final String GENERAL_SETTINGS_KEY = "general";
  public static final String OPTIONS_KEY = "options";
  public static final String CONFIG_HOST_NAME = "configHostName";
  public static final String GENERAL_SETTINGS_CONFIG_TYPE = "general";

  public static final String CONF_PROP_NAME = "cq:conf";
  public static final String RUNMODE_AUTHOR = "author";
  public static final String RUNMODE_NON_AUTHOR = "non-author";

  public static final String CP_NODE_PROPERTY_PREFIX = "cpWidget#";
  public static final String SELECTED_WIDGET_REF = "cpWidget#widgetRefSelected";

  public static final String DEFAULT_HOST = "https://captivateprime.adobe.com";

  public final class CPUrl
  {
    public static final String CONFIG_URL = "/app/embeddablewidget?widgetRef=widgets-aem";
    public static final String ACCESS_TOKEN_URL = "/oauth/o/learnerToken?learner_email={email}&force={force}";
    public static final String WIDGET_CONFIG_URL = "/app/embeddablewidget?widgetRef=widgets-aem";
    public static final String WIDGET_SRC_URL = "{hostName}/app/embeddablewidget?widgetRef={widgetRef}&resourceType=html";
    public static final String WIDGET_COMMUNICATOR_URL = "{hostName}/app/embeddablewidget?widgetRef=com.adobe.captivateprime.widgetcommunicator";
  }

  public final class AdminConfigurations
  {
    public static final String ADMIN_CONFIG_CLIENT_ID = "clientId";
    public static final String ADMIN_CONFIG_CLIENT_SECRET = "clientSecret";
    public static final String ADMIN_CONFIG_REFRESH_TOKEN = "refreshToken";

    public static final String ADMIN_CONFIG_HOST_NAME = CP_NODE_PROPERTY_PREFIX + "commonConfig.captivateHostName";
    public static final String GLOBAL_CONFIG_SETTINGS = "/conf/global/settings";
    public static final String CLOUD_CONFIG = "cloudconfigs";
    public static final String CP_WIDGET_CONFIG = "cpwidget";
    public static final String CP_WIDGET_CONFIG_PATH = GLOBAL_CONFIG_SETTINGS + "/" + CLOUD_CONFIG + "/" + CP_WIDGET_CONFIG;
    public static final String CP_WIDGET_CONFIG_PROP_PATH = CP_WIDGET_CONFIG_PATH + "/" + JcrConstants.JCR_CONTENT;

    public static final String GLOBAL_CONFIG_PATH = "/conf/global";
    public static final String GLOBAL_CONFIG_CP = "captivate-prime";
    public static final String GLOBAL_CONFIG_CP_PATH = GLOBAL_CONFIG_PATH + "/" + GLOBAL_CONFIG_CP;
    public static final String CLOUD_CONFIG_SETTINGS = "settings";
    public static final String CP_SUB_CONFIG_PATH =
        "/" + CLOUD_CONFIG_SETTINGS + "/" + CLOUD_CONFIG + "/" + CP_WIDGET_CONFIG + "/" + JcrConstants.JCR_CONTENT;
    public static final String HELPXLINK_PREFIX = "cphelpx#";

    public static final String MASK_CHAR = "X";
    public static final int MASK_LENGTH = 4;
  }

  public final class LearnerConfigurations
  {
    public static final String USER_ACCESS_TOKEN_STR = "accessToken";
    public static final String EXPIRES_IN_STR = "expiresIn";
    public static final String EMAIL_STR = "email";
    public static final String USER_ACCESS_TOKEN_PATH = "./profile/" + USER_ACCESS_TOKEN_STR;
    public static final String USER_ACCESS_TOKEN_EXPIRY_PATH = "./profile/" + EXPIRES_IN_STR;
    public static final String USER_EMAIL_PATH = "./profile/" + EMAIL_STR;
  }

  public final class EmbeddableWidgetConfig
  {
    public static final String WIDGET_REF = "widgetRef";
  }
}
