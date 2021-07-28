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

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.Calendar;

import javax.jcr.Binary;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.ValueFormatException;

public class ValueMock implements Value
{
  private boolean bValue;
  private String sValue;
  private long lValue;
  private double dValue;
  private int propertyType;

  public ValueMock(String s)
  {
    sValue = s;
    propertyType = PropertyType.STRING;
  }

  public boolean getBoolean()
  {
    return bValue;
  }

  public Calendar getDate()
  {
    return null;
  }

  public double getDouble()
  {
    return dValue;
  }

  public long getLong()
  {
    return lValue;
  }

  public InputStream getStream()
  {
    return null;
  }

  public String getString()
  {
    return sValue;
  }

  public int getType()
  {
    return propertyType;
  }

  @Override
  public Binary getBinary() throws RepositoryException
  {
    return null;
  }

  @Override
  public BigDecimal getDecimal() throws ValueFormatException, RepositoryException
  {
    return null;
  }

  public void setValue(String sValue)
  {
    this.sValue = sValue;
  }

  @Override
  public String toString()
  {
    return getString();
  }
}