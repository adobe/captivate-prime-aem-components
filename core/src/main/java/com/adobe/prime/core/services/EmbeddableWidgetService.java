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

import java.util.Map;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;

import com.day.cq.wcm.api.Page;

public interface EmbeddableWidgetService
{

  public boolean isAuthorMode();

  public String getAccessTokenOfUser(SlingHttpServletRequest request, ResourceResolver resolver, Page currentPage);

  public Map<String, Object> getGeneralConfigs(Resource resource);

  public Map<String, Object> getAvailaleAdminConfiguration(Resource resource);

  public String getDefaultHostName();
}
