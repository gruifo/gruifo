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

import gruifo.lang.java.JClass;
import gruifo.lang.java.JParam;
import gruifo.output.PrintUtil;

/**
 * Prints Field members.
 */
public class JSNIFieldPrinter {

  public void printFields(final StringBuffer buffer, final int indent,
      final JClass jFile) {
    if (!jFile.isInterface()) {
      for (final JParam field : jFile.getFields()) {
        printGetter(buffer, indent, field);
        if (!field.isFinal()) {
          printSetter(buffer, indent, field);
        }
      }
    }
  }

  private void printGetter(final StringBuffer buffer, final int indent,
      final JParam field) {
    PrintUtil.indent(buffer, field.getJavaDoc(), indent);
    PrintUtil.indent(buffer, indent);
    buffer.append("public ");
    if (field.isStatic()) {
      buffer.append("static ");
    }
    buffer.append("final native ");
    buffer.append(field.getType());
    buffer.append(" get");
    printFieldName(buffer, field);
    if (field.isMultiField()) {
      buffer.append(fixMultiTypeField(field));
    }
    buffer.append("() /*-{");
    PrintUtil.nl(buffer);
    PrintUtil.indent(buffer, indent + 1);
    buffer.append("return ");
    printFieldVariable(buffer, field);
    buffer.append(';');
    PrintUtil.nl(buffer);
    PrintUtil.indent(buffer, indent);
    buffer.append("}-*/;");
    PrintUtil.nl2(buffer);
  }

  private String fixMultiTypeField(final JParam field) {
    final int genericIdx = field.getType().indexOf('<');
    final String subString = genericIdx < 0 ? field.getType()
        : field.getType().substring(0, genericIdx);
    final int dotIdx = subString.lastIndexOf('.');
    return  PrintUtil.firstCharUpper(
        dotIdx < 0 ? subString : subString.substring(dotIdx + 1));
  }

  private void printSetter(final StringBuffer buffer, final int indent,
      final JParam field) {
    PrintUtil.indent(buffer, field.getJavaDoc(), indent);
    PrintUtil.indent(buffer, indent);
    buffer.append("public ");
    if (field.isStatic()) {
      buffer.append("static ");
    }
    buffer.append("final native void");
    buffer.append(" set");
    printFieldName(buffer, field);
    buffer.append('(');
    buffer.append(field.getType());
    buffer.append(' ');
    printFieldAsVar(buffer, field);
    buffer.append(") /*-{");
    PrintUtil.nl(buffer);
    PrintUtil.indent(buffer, indent + 1);
    printFieldVariable(buffer, field);
    buffer.append(" = ");
    printFieldAsVar(buffer, field);
    buffer.append(';');
    PrintUtil.nl(buffer);
    PrintUtil.indent(buffer, indent);
    buffer.append("}-*/;");
    PrintUtil.nl2(buffer);
  }

  private void printFieldName(final StringBuffer buffer, final JParam field) {
    buffer.append(PrintUtil.firstCharUpper(getFieldName(field)));
  }

  private void printFieldAsVar(final StringBuffer buffer, final JParam field) {
    buffer.append(PrintUtil.firstCharLower(getFieldName(field)));
  }

  private String getFieldName(final JParam field) {
    String name = field.getName();
    if (field.isStatic()) {
      final int classSep = name.lastIndexOf('.');
      name = name.substring(classSep + 1);
    }
    return name;
  }

  private void printFieldVariable(final StringBuffer buffer, final JParam field) {
    if (field.isStatic()) {
      buffer.append("$wnd.");
      buffer.append(field.getName());
    } else {
      buffer.append("this['");
      buffer.append(field.getName());
      buffer.append("']");
    }
  }

}
