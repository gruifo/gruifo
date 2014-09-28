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
package gengwtjs.output.jsinterface;

import gengwtjs.lang.java.JavaFile;
import gengwtjs.lang.js.JsFile;
import gengwtjs.output.FilePrinter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;

public class JsInterfacePrinter implements FilePrinter {

  private static final String GWT_JS_PACKAGE = "com.google.gwt.core.client.js";

  @Override
  public String printFile(final JsFile javaFile) {
    return "TODO";
  }

  private String printFile(final JavaFile jFile) {
    final StringBuffer buffer = new StringBuffer();
    buffer.append(jFile.getHeaderComment());
    writePackageName(buffer, jFile.getPackageName());
    writeImports(buffer, jFile.getImports());
    writeInterface(buffer, jFile);
    writeMethods(buffer, jFile);
    // write methods;
    // create file
    // write properties
    buffer.append("}\n"); // close file
    return buffer.toString();
  }

  private void writePackageName(final StringBuffer buffer,
      final String packageName) {
    buffer.append("package ");
    buffer.append(packageName);
    buffer.append(";\n\n");
  }

  private void writeImports(final StringBuffer buffer, final Set<String> imports) {
    final ArrayList<String> importList = new ArrayList<>(imports);
    addGwtImports(importList);
    Collections.sort(importList);
    for (final String imp : importList) {
      buffer.append("import ");
      buffer.append(imp);
      buffer.append(";\n");
    }
    buffer.append("\n");
  }

  private void addGwtImports(final ArrayList<String> importList) {
    importList.add(GWT_JS_PACKAGE + "JsInterface");
    importList.add(GWT_JS_PACKAGE + "JsProperty");
  }

  private void writeInterface(final StringBuffer buffer, final JavaFile jFile) {
    /*    if (jFile.isClass()) {
      buffer.append("@JsInterface\n"); //(prototype="Window")
    }
    buffer.append("public interface ");
    buffer.append(jFile.getClassOrInterfaceName());
    if (jFile.getElement().getExtends() != null) {
      buffer.append(" extends ");
      buffer.append(jFile.getElement().getExtends());
    }
    buffer.append(" {\n");
     */  }

  private void writeMethods(final StringBuffer buffer, final JavaFile jFile) {
    /*    for (final JMethod method : jFile.getMethods()) {
      buffer.append(method.getElement().getComment());
      buffer.append("\n  ");
      final JsType returnType = method.getElement().getReturn();
      if (returnType != null) {
        //        buffer.append(typeMapper.mapType(returnType.getType()));
      }
      buffer.append(' ');
      buffer.append(method.getMethodName());
      buffer.append('(');
      boolean first = true;
      for (final JsParam param : method.getElement().getParams()) {
        if (!first) {
          buffer.append(", ");
        }
        //FIXME        buffer.append(typeMapper.mapType(param.getType()));
        //        if (param.isVarargs()) {
        //          buffer.append("...");
        //        }
        buffer.append(' ');
        buffer.append(param.getName());
        first = false;
      }
      buffer.append(");\n\n");
    }
     */
  }
}
