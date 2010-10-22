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

import java.io.Serializable;

@SuppressWarnings("serial")
public abstract class CCRObject implements Serializable {
  
  /** Params: type, date */
  protected static final String DATE_TIME =
    "<DateTime><Type><Text>%s</Text></Type><ExactDateTime>%s</ExactDateTime></DateTime>";
  
  /** Params: name */
  protected static final String DESCRIPTION = "<Description><Text>%s</Text></Description>";
  
  public enum DateType {
    COLLECTION_START_DATE("Collection start date");
    
    private String type;
    
    private DateType(String type) {
      this.type = type;
    }
    
    @Override
    public String toString() {
      return type;
    }
  }
  
  public abstract String toCCR();
}
