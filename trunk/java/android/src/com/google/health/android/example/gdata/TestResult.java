/*
 * Copyright (c) 2010 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.google.health.android.example.gdata;


/**
 * Unsupported:
 * - NormalResults
 * - Codes
 */
@SuppressWarnings("serial")
public class TestResult extends CCRObject {

  /** Params: value, units */
  static final String TEST_RESULT =
    "<TestResult><Value>%s</Value><Units><Unit>%s</Unit></Units></TestResult>";

  private String name;
  private String date;
  private String value;
  private String units;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDate() {
    return date;
  }

  public void setDate(String date) {
    this.date = date;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  public String getUnits() {
    return units;
  }

  public void setUnits(String units) {
    this.units = units;
  }

  @Override
  public String toString() {
    return name + "\n" + value + " " + units + "\n" + date;
  }

  @Override
  public String toCCR() {
    StringBuilder sb = new StringBuilder("<Test>");

    if (date != null) {
      sb.append(String.format(DATE_TIME, DateType.COLLECTION_START_DATE, date));
    }

    if (name != null) {
      sb.append(String.format(DESCRIPTION, name));
    }

    if (units != null && value != null) {
      sb.append(String.format(TEST_RESULT, value, units));
    }

    sb.append("</Test>");

    return sb.toString();
  }
}
