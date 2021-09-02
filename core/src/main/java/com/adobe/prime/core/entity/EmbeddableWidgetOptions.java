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

import com.google.gson.annotations.SerializedName;

public class EmbeddableWidgetOptions
{

  private String name;
  private String description;
  private String ref;
  private String type;
  private String helpx;

  @SerializedName(value = "default")
  private String defaultValue;

  private boolean mandatory;
  private boolean hidden;

  public EmbeddableWidgetOptions()
  {}

  public String getName()
  {
    return name;
  }

  public void setName(String name)
  {
    this.name = name;
  }

  public String getDescription()
  {
    return description;
  }

  public void setDescription(String description)
  {
    this.description = description;
  }

  public String getRef()
  {
    return ref;
  }

  public void setRef(String ref)
  {
    this.ref = ref;
  }

  public String getType()
  {
    return type;
  }

  public void setType(String type)
  {
    this.type = type;
  }

  public String getHelpx()
  {
    return helpx;
  }

  public void setHelpx(String helpx)
  {
    this.helpx = helpx;
  }

  public String getDefaultValue()
  {
    return defaultValue;
  }

  public void setDefaultValue(String defaultValue)
  {
    this.defaultValue = defaultValue;
  }

  public boolean getMandatory()
  {
    return mandatory;
  }

  public void setMandatory(boolean mandatory)
  {
    this.mandatory = mandatory;
  }

  public boolean getHidden()
  {
    return hidden;
  }

  public void setHidden(boolean hidden)
  {
    this.hidden = hidden;
  }
}
