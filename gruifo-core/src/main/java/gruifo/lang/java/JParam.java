/*
 * Copyright Hilbrand Bouwkamp.
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
package gruifo.lang.java;

public class JParam {

  private String type;
  private String name;
  private boolean optional;
  private boolean varargs;
  private String javaDoc;

  public JParam(final String name, final String type) {
    this.name = name;
    this.type = type;
  }

  public String getJavaDoc() {
    return javaDoc;
  }

  public String getName() {
    return name;
  }

  public String getType() {
    return type;
  }

  public boolean isOptional() {
    return optional;
  }

  public boolean isVarargs() {
    return varargs;
  }

  public void setJavaDoc(final String javaDoc) {
    this.javaDoc = javaDoc;
  }

  public void setName(final String name) {
    this.name = name;
  }

  public void setOptional(final boolean optional) {
    this.optional = optional;
  }

  public void setType(final String type) {
    this.type = type;
  }
}