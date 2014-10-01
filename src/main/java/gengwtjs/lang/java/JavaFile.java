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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class JavaFile {
  public static class EnumValue {
    private final String name;
    private final Object value;

    public EnumValue(final String name, final Object value) {
      this.name = name;
      this.value = value;
    }
    public String getName() {
      return name;
    }
    public Object getValue() {
      return value;
    }
  }
  private final String packageName;
  private final boolean _interface;
  private final String classOrInteraceName;
  private final Set<String> imports = new HashSet<>();
  private final List<JParam> fields = new ArrayList<>();;
  private final List<JMethod> methods = new ArrayList<>();
  private final List<EnumValue> enumValues = new ArrayList<>();
  private String headerComment = "";
  private final List<JavaFile> innerJFil = new ArrayList<>();
  private boolean _static;
  private String classDescription;

  public JavaFile(final String packageName, final String className
      /*final boolean _interface*/) {
    this.packageName = packageName;
    this.classOrInteraceName = className;
    this._interface = false; //FIXME;
  }

  public void addEnumValue(final String name, final Object value) {
    enumValues.add(new EnumValue(name, value));
  }

  public void addField(final String name, final String type) {
    fields.add(new JParam(name, type));
  }

  public void addInnerJFile(final JavaFile javaFile) {
    _static = true;
    innerJFil.add(javaFile);
  }

  public void addMethod(final JMethod method) {
    methods.add(method);
  }

  public String getClassOrInterfaceName() {
    return classOrInteraceName;
  }

  public String getClassDescription() {
    return classDescription;
  }

  public List<EnumValue> getEnumValues() {
    return enumValues;
  }

  public List<JParam> getFields() {
    return fields;
  }

  public String getHeaderComment() {
    return headerComment;
  }

  public Set<String> getImports() {
    return imports;
  }

  public List<JavaFile> getInnerJFiles() {
    return innerJFil;
  }

  public List<JMethod> getMethods() {
    return methods;
  }

  public String getPackageName() {
    return packageName;
  }

  public boolean isClass() {
    return !_interface;
  }

  public boolean isInterface() {
    return _interface;
  }

  public boolean isStatic() {
    return _static;
  }

  public void setClassDescription(final String classDescription) {
    this.classDescription = classDescription;
  }

  public void setHeaderComment(final String headerComment) {
    this.headerComment = headerComment;
  }

  public void setStatic(final boolean _static) {
    this._static = _static;
  }
}
