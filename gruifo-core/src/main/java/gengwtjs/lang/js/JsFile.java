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
package gengwtjs.lang.js;

import gengwtjs.lang.js.JsElement.JsParam;

import java.util.ArrayList;
import java.util.List;

public class JsFile {

  private final List<String> enumValues = new ArrayList<>();
  private final List<JsConst> constants = new ArrayList<>();
  private final List<JsParam> fields = new ArrayList<>();
  private final List<JsMethod> methods = new ArrayList<>();
  private final List<JsFile> innerJsFiles = new ArrayList<>();
  private final String packageName;
  private final String classOrInteraceName;
  private final boolean _interface;
  private JsElement element;

  public JsFile(final String packageName, final String className,
      final boolean _interface) {
    this.packageName = packageName;
    this.classOrInteraceName = className;
    this._interface = _interface;
  }

  public void addConst(final String constName, final JsElement element) {
    constants.add(new JsConst(constName, element));
  }

  public void addEnumValue(final String name) {
    enumValues.add(name);
  }

  public void addField(final JsParam field) {
    fields.add(field);
  }

  public void addMethod(final JsMethod method) {
    methods.add(method);
  }

  public void addInnerJsFile(final JsFile jsFile) {
    innerJsFiles.add(jsFile);
  }

  public String getClassOrInterfaceName() {
    return classOrInteraceName;
  }

  public List<JsConst> getConstants() {
    return constants;
  }

  public JsElement getElement() {
    return element;
  }

  public List<String> getEnumValues() {
    return enumValues;
  }

  public List<JsParam> getFields() {
    return fields;
  }

  public List<JsMethod> getMethods() {
    return methods;
  }

  public String getPackageName() {
    return packageName;
  }

  public List<JsFile> getInnerJFiles() {
    return innerJsFiles;
  }

  public boolean isInterface() {
    return _interface;
  }

  public void setElement(final JsElement element) {
    this.element = element;
  }
}
