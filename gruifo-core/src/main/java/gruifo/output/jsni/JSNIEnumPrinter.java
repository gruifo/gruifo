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

import gruifo.lang.java.JClass.EnumValue;
import gruifo.output.PrintUtil;

import java.util.List;

/**
 * Prints JavaScipt @enum annotated code as Java enum objects.
 */
public class JSNIEnumPrinter {

  public void printEnum(final StringBuffer buffer, final int indent,
      final String packageName, final String enumName,
      final List<EnumValue> enumValues) {
    buffer.append("public enum ");
    buffer.append(enumName);
    buffer.append(" {");
    PrintUtil.nl(buffer);
    printEnumContent(buffer, indent + 1, packageName, enumName, enumValues);
  }

  private void printEnumContent(final StringBuffer buffer, final int indent,
      final String packageName, final String enumName,
      final List<EnumValue> enumValues) {
    boolean first = true;
    for (final EnumValue enumValue : enumValues) {
      if (first) {
        first = false;
      } else {
        buffer.append(',');
        PrintUtil.nl(buffer);
      }
      PrintUtil.indent(buffer, enumValue.getJavaDoc(), indent);
      PrintUtil.indent(buffer, indent);
      buffer.append(enumValue.getName());
      buffer.append(" {");
      PrintUtil.nlIndent(buffer, indent + 1);
      buffer.append("@Override");
      PrintUtil.nlIndent(buffer, indent + 1);
      buffer.append("public native final ");
      buffer.append(enumValue.getType());
      buffer.append(" getValue() /*-{");
      PrintUtil.nlIndent(buffer, indent + 2);
      buffer.append("return $wnd.");
      buffer.append(packageName + "." + enumName);
      buffer.append('.');
      buffer.append(enumValue.getName());
      buffer.append(';');
      PrintUtil.nlIndent(buffer, indent + 1);
      buffer.append("}-*/;");
      PrintUtil.nlIndent(buffer, indent);
      buffer.append('}');
    }
    if (!enumValues.isEmpty()) {
      buffer.append(';');
      PrintUtil.nl2(buffer);
      printMethods(buffer, indent, enumName, enumValues.get(0).getType());
    }
  }

  private void printMethods(final StringBuffer buffer, final int indent,
      final String enumName, final String enumType) {
    PrintUtil.indent(buffer, indent);
    buffer.append("public abstract " + enumType + " getValue();");
    PrintUtil.nl2(buffer);
    PrintUtil.indent(buffer, indent);
    buffer.append("public static ");
    buffer.append(enumName);
    buffer.append(" getEnumFromValue(final ");
    buffer.append(enumType);
    buffer.append(" value) {");
    PrintUtil.nlIndent(buffer, indent+1);
    buffer.append("for (final " + enumName + " e : values()) {");
    PrintUtil.nlIndent(buffer, indent+2);
    buffer.append("if (");
    if (TypeMapper.INSTANCE.isPrimitive(enumType)) {
      buffer.append("e.getValue() == value");
    } else {
      buffer.append("e.getValue().equals(value)");
    }
    buffer.append(") {");
    PrintUtil.nlIndent(buffer, indent+3);
    buffer.append("return e;");
    PrintUtil.nlIndent(buffer, indent+2);
    buffer.append("}");
    PrintUtil.nlIndent(buffer, indent+1);
    buffer.append("}");
    PrintUtil.nlIndent(buffer, indent+1);
    buffer.append("return null;");
    PrintUtil.nlIndent(buffer, indent);
    buffer.append("}");
  }
}
