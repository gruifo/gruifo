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

import java.util.ArrayList;
import java.util.List;

public class JClass extends JavaFile {

  private final List<JMethod> constructors = new ArrayList<>();
  private String _extends;
  private String classGeneric;
  private boolean dataClass;

  public JClass(final String packageName, final String className) {
    super(packageName, className);
  }

  public void addConstructor(final JMethod constructor) {
    constructors.add(constructor);
  }

  public String getClassGeneric() {
    return classGeneric;
  }

  public List<JMethod> getConstructors() {
    return constructors;
  }

  public String getExtends() {
    return _extends;
  }

  public boolean isDataClass() {
    return dataClass;
  }

  public void setClassGeneric(final String classGeneric) {
    this.classGeneric = classGeneric;

  }

  public void setDataClass(final boolean dataClass) {
    this.dataClass = dataClass;
  }

  public void setExtends(final String _extends) {
    this._extends = _extends;
  }

}
