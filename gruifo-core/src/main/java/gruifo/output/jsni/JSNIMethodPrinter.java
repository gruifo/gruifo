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
package gruifo.output.jsni;

import gruifo.lang.AccessType;
import gruifo.lang.java.JClass;
import gruifo.lang.java.JMethod;
import gruifo.lang.java.JParam;
import gruifo.output.PrintUtil;

/**
 * Prints java methods.
 */
class JSNIMethodPrinter {

  public void printMethods(final StringBuffer buffer, final int indent,
      final JClass jFile) {
    for (final JMethod method : jFile.getMethods()) {
      if (method.isAbstractMethod() && !jFile.isInterface()) {
        continue;
      }
      PrintUtil.indent(buffer, method.getJsDoc(), indent);
      PrintUtil.indent(buffer, indent);
      printModifiers(buffer, jFile, method);
      if (method.getGenericType() != null) {
        buffer.append('<');
        buffer.append(method.getGenericType());
        buffer.append(" extends ");
        buffer.append(TypeMapper.GWT_JAVA_SCRIPT_OBJECT); //FIXME not hardcode extends generics
        buffer.append("> ");
      }
      buffer.append(method.getReturn());
      buffer.append(' ');
      buffer.append(method.getMethodName());
      buffer.append('(');
      printMethodParam(buffer, method, true);
      buffer.append(')');
      if (method.isAbstractMethod() || jFile.isInterface()) {
        buffer.append(';');
      } else {
        buffer.append(" /*-{");
        PrintUtil.nl(buffer);
        printMethodBody(buffer, indent + 1, method);
        PrintUtil.indent(buffer, indent);
        buffer.append("}-*/;");
      }
      PrintUtil.nl2(buffer);
    }
  }

  private void printModifiers(final StringBuffer buffer, final JClass jFile,
      final JMethod method) {
    if (!jFile.isInterface()) {
      buffer.append(appendAccessType(method.getAccessType()));
      if (method.isAbstractMethod()) {
        buffer.append("abstract ");
      } else {
        if (method.isStaticMethod()) {
          buffer.append("static ");
        }
        buffer.append("final native ");
      }
    }
  }

  public static void printMethodParam(final StringBuffer buffer,
      final JMethod method, final boolean withType) {
    boolean first = true;
    for (final JParam param : method.getParams()) {
      if (!first) {
        buffer.append(", ");
      }
      if (withType) {
        buffer.append(param.getType());
        buffer.append(' ');
      }
      buffer.append(param.getName());
      first = false;
    }
  }

  private String appendAccessType(final AccessType accessType) {
    final String asType;
    switch (accessType) {
    case DEFAULT:
      asType = " ";
      break;
    case PRIVATE:
      asType = "private ";
      break;
    case PROTECTED:
      asType = "protected ";
      break;
    case PUBLIC:
    default:
      asType = "public ";
      break;
    }
    return asType;
  }

  private void printMethodBody(final StringBuffer buffer,
      final int indent, final JMethod method) {
    PrintUtil.indent(buffer, indent);
    buffer.append(isVoidType(method) ? "" : "return ");
    buffer.append("this.");
    buffer.append(method.getMethodName());
    buffer.append('(');
    printMethodParam(buffer, method, false);
    buffer.append(");");
    PrintUtil.nl(buffer);
  }

  private boolean isVoidType(final JMethod method) {
    return "void".equals(method.getReturn());
  }
}
