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

package com.google.health.examples.ccr;

import java.util.LinkedList;
import java.util.List;

@SuppressWarnings("serial")
public class Result extends CCRObject {

  private String name;
  private String date;
  private List<TestResult> tests = new LinkedList<TestResult>();

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

  public void addTest(TestResult test) {
    tests.add(test);
  }

  public List<TestResult> getTests() {
    return tests;
  }

  public void setTests(List<TestResult> tests) {
    this.tests = tests;
  }

  @Override
  public String toCCR() {
    StringBuilder sb = new StringBuilder("<Result>");

    if (date != null) {
      sb.append(String.format(DATE_TIME, DateType.COLLECTION_START_DATE, date));
    }

    if (name != null) {
      sb.append(String.format(DESCRIPTION, name));
    }

    for (TestResult test : tests) {
      sb.append(test.toCCR());
    }

    sb.append("</Result>");

    return sb.toString();
  }
}
