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
package gruifo.lang.js;

public class JsParam {
  private JsType type;
  private String name;
  private JsElement element;

  public JsParam() {
  }

  public JsParam(final String name, final JsElement element) {
    this.name = name;
    this.element = element;
  }

  public JsElement getElement() {
    return element;
  }

  public String getName() {
    return name;
  }

  public JsType getType() {
    return element == null ? type : element.getType();
  }

  public void setName(final String name) {
    this.name = name;
  }

  public void setType(final JsType jsType) {
    this.type = jsType;
  }

  @Override
  public String toString() {
    return "JsParam [name=" + name + ", type=" + type + "]";
  }
}