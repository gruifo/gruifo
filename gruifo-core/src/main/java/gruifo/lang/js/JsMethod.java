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

import gruifo.lang.AccessType;

public class JsMethod {

  private JsElement element;
  private boolean abstractMethod;
  private String methodName;
  private final String packageName;

  public JsMethod(final String packageName, final String functionName) {
    this.packageName = packageName;
    methodName = functionName;
  }

  public AccessType getAccessType() {
    return element.getAccessType();
  }

  public String getPackageName() {
    return packageName;
  }

  public JsElement getElement() {
    return element;
  }

  public String getMethodName() {
    return methodName;
  }

  public boolean isAbstractMethod() {
    return abstractMethod;
  }

  public void setAbstract(final boolean abstractMethod) {
    this.abstractMethod = abstractMethod;
  }

  public void setElement(final JsElement element) {
    this.element = element;
  }

  public void setMethodName(final String methodName) {
    this.methodName = methodName;
  }
}
