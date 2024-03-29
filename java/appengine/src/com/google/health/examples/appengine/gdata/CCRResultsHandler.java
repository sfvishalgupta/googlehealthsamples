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

package com.google.health.examples.appengine.gdata;

import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.google.health.examples.appengine.gdata.CCRObject.DateType;

/**
 * Unsupported:
 * CCRDataObjectIds (unused anyhow)
 * Codes
 * NormalResults
 * Source/Actors
 */
class CCRResultsHandler extends DefaultHandler {
  
  private static final String CCR_RESULT = "Result";
  private static final String CCR_TEST = "Test";
  private static final String CCR_DATETIME = "DateTime";
  private static final String CCR_VALUE = "Value";
  private static final String CCR_UNIT = "Unit";
  private static final String CCR_EXACTDATETIME = "ExactDateTime";
  private static final String CCR_TEXT = "Text";
  private static final String CCR_DESCRIPTION = "Description";
  private static final String CCR_TYPE = "Type";
  
  private List<Result> results;
  private Stack<String> stack;

  private TestResult test;
  private Result result;  

  private String dateType;
  private String date;
  
  private String text;
  
  @Override
  public void startDocument() {
    results = new LinkedList<Result>();
    stack = new Stack<String>();
  }
  
  @Override
  public void startElement(String nsURI, String localName, String qName, Attributes atts)
      throws SAXException {
    
    stack.push(qName);
    
    if (qName.equals(CCR_RESULT)) {
      result = new Result();
    } else if (qName.equals(CCR_TEST)) {
      test = new TestResult();
    }
  }

  @Override
  public void endElement(String nsURI, String localName, String qName) throws SAXException {
    
    stack.pop();
    
    // Code only processing Results and sub-elements.
    if (result == null) {
      return;
    }
    
    if (qName.equals(CCR_RESULT)) {
      results.add(result);
      result = null;
    } else if (qName.equals(CCR_TEST)) {
      result.addTest(test);
      test = null;
    } else if (qName.equals(CCR_DATETIME)) {
      // If we have an appropriate date, assign it to the test or result.
      if (date != null && dateType != null
          && dateType.equals(DateType.COLLECTION_START_DATE.toString())) {
        if (test != null) {
          test.setDate(date);
        } else {
          result.setDate(date);
        }
      }
      
      dateType = null;
      date = null;
    } else if (qName.equals(CCR_DESCRIPTION)) {
      if (test != null) {
        test.setName(text);
      } else {
        result.setName(text);
      }
      
      text = null;
    } else if (qName.equals(CCR_TYPE)) {
      if (stack.peek().equals(CCR_DATETIME)) {
        dateType = text;
      }
      
      text = null;
    }
  }
  
  @Override
  public void characters(char ch[], int start, int length) {
    
    // Code only processing Results and sub-elements.
    if (result == null) {
      return;
    }
    
    String text = new String(ch, start, length);
    
    // Will only get a units or values if we're processing a test;
    // although, they could be in NormalResults
    if (stack.peek().equals(CCR_VALUE)) {
      if (test != null) {
        test.setValue(text);
      }
    } else if (stack.peek().equals(CCR_UNIT)) {
      if (test != null) {
        test.setUnits(text);
      }
    } else if (stack.peek().equals(CCR_EXACTDATETIME)) {
      date = text;
    } else if (stack.peek().equals(CCR_TEXT)) {
      this.text = text;
    }
  }
  
  public List<Result> getResults() {
    return results;
  }
}
