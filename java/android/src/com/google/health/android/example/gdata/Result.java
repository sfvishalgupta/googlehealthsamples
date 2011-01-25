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

import java.util.LinkedList;
import java.util.List;

@SuppressWarnings("serial")
public class Result extends CCRObject implements Comparable<Result> {

  private String name;
  private String date;
  private List<TestResult> testResults = new LinkedList<TestResult>();

  public String getName() {
    if (name == null && testResults.size() > 0) {
      name = testResults.get(0).getName();
    }
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDate() {
    if (date == null && testResults.size() > 0) {
      date = testResults.get(0).getDate();
    }
    return date;
  }

  public void setDate(String date) {
    this.date = date;
  }

  public void addTestResult(TestResult test) {
    testResults.add(test);
  }

  public List<TestResult> getTestResults() {
    return testResults;
  }

  public void setTestResults(List<TestResult> tests) {
    this.testResults = tests;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    // Simply return the tests in a single string; acceptable since they're 1:1 now.
    for (TestResult test : testResults) {
      sb.append(test.toString() + "\n");
    }
    return sb.toString().trim();
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

    for (TestResult test : testResults) {
      sb.append(test.toCCR());
    }

    sb.append("</Result>");

    return sb.toString();
  }

  @Override
  public int compareTo(Result result) {
    int x = 0;
    if (getDate() != null) {
      x = getDate().compareTo(result.getDate());
    }
    if (x == 0 && getName() != null) {
      x = getName().compareTo(result.getName());
    }
    if (x == 0) {
      x = -1;
    }
    return x;
  }
}
