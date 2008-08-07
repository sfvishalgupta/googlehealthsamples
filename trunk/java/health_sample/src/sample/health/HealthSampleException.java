/*
 * Copyright (c) 2006 Google Inc.
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
package sample.health;

/**
 * Generic exception to wrap problems interacting with the Health API.
 * 
 * @author api.jfisher@google.com (Jeff Fisher)
 */
public class HealthSampleException extends Exception {

  public static final long serialVersionUID = 1L;

  public HealthSampleException(String message) {
    super(message);
  }

  public HealthSampleException(String message, Throwable cause) {
    super(message, cause);
  }

  public HealthSampleException(Throwable cause) {
    super(cause);
  }
}
