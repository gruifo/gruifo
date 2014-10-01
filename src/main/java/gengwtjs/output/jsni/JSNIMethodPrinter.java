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
package gengwtjs.output.jsni;

import gengwtjs.lang.AccessType;
import gengwtjs.lang.java.JMethod;
import gengwtjs.lang.java.JParam;
import gengwtjs.lang.java.JavaFile;
import gengwtjs.output.PrintUtil;

class JSNIMethodPrinter {

  public void writeMethods(final StringBuffer buffer, final int indent,
      final JavaFile jFile) {
    for (final JMethod method : jFile.getMethods()) {
      PrintUtil.indent(buffer, method.getComment(), indent);
      PrintUtil.indent(buffer, indent);
      if (method.isComplex()) {
        buffer.append("/*");
      }
      buffer.append(appendAccessType(method.getAccessType()));
      buffer.append("native ");
      if (method.getGenericType() != null) {
        buffer.append('<');
        buffer.append(method.getGenericType());
        buffer.append(" extends JavaScriptObject> "); //FIXME
      }
      buffer.append(method.getReturn());
      buffer.append(' ');
      buffer.append(method.getMethodName());
      buffer.append('(');
      writeMethodParam(buffer, method, true);
      buffer.append(") /*-{");
      PrintUtil.nl(buffer);
      writeMethodBody(buffer, indent + 1, method);
      PrintUtil.indent(buffer, indent);
      buffer.append("}-*/;");
      PrintUtil.nl2(buffer);
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

  private void writeMethodParam(final StringBuffer buffer, final JMethod method,
      final boolean withType) {
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

  private void writeMethodBody(final StringBuffer buffer, final int indent,
      final JMethod method) {
    PrintUtil.indent(buffer, indent);
    buffer.append("void".equals(method.getReturn()) ? "" : "return ");
    buffer.append("this.");
    buffer.append(method.getMethodName());
    buffer.append('(');
    writeMethodParam(buffer, method, false);
    buffer.append(");");
    PrintUtil.nl(buffer);
  }
}
