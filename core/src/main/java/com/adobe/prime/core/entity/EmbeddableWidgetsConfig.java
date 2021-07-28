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

import java.util.Collections;
import java.util.List;

public class EmbeddableWidgetsConfig
{

  private String name;
  private String ref;
  private String widgetRef;
  private String description;
  private String type;
  private String defaultValue;
  private List<EmbeddableWidgetOptions> options;

  public EmbeddableWidgetsConfig()
  {}

  public String getName()
  {
    return name;
  }

  public void setName(String name)
  {
    this.name = name;
  }

  public String getRef()
  {
    return ref;
  }

  public void setRef(String ref)
  {
    this.ref = ref;
  }

  public String getWidgetRef()
  {
    return widgetRef;
  }

  public void setWidgetRef(String widgetRef)
  {
    this.widgetRef = widgetRef;
  }

  public String getDescription()
  {
    return description;
  }

  public void setDescription(String description)
  {
    this.description = description;
  }

  public String getType()
  {
    return type;
  }

  public void setType(String type)
  {
    this.type = type;
  }

  public List<EmbeddableWidgetOptions> getOptions()
  {
    return Collections.unmodifiableList(options);
  }

  public void setOptions(List<EmbeddableWidgetOptions> options)
  {
    this.options = Collections.unmodifiableList(options);
  }

  public String getDefault()
  {
    return defaultValue;
  }

  public void setDefault(String defaultValue)
  {
    this.defaultValue = defaultValue;
  }
}
