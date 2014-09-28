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
package gengwtjs.lang.java;

public class JParam {

  private final String type;
  private String name;
  private boolean _optional;
  private boolean varargs;

  public JParam(final String type, final String name) {
    this.type = type;
    this.name = name;
  }

  public String getType() {
    return type;
  }

  public String getName() {
    return name;
  }

  //  public boolean isOptional() {
  //    return _optional;
  //  }

  public boolean isVarargs() {
    return varargs;
  }

  public void setName(final String name) {
    this.name = name;
  }
}
