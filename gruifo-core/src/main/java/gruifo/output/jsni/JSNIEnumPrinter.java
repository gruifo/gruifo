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
 * Prints JavaScript @enum {[type]} annotated code. This code looks like a map
 * with keys and values. However, it's a class with variables and initialized
 * with a specific value. Where the variable is like a key. For example:
 * <pre>
 * /**
 *  * @enum {number}
 *  * /
 * MyEnum = {
 *   A: 1,
 *   B: 2
 * };
 * </pre>
 * 
 * In Java the enum keys are represented by static fields that are initialized
 * with a call to a native method. The method returns the JavaScript key.
 * The code makes use of the fact that while Java is typed the compiled
 * JavaScript is not. Thus in Java the return type is cast to a class, while
 * it just is value of the enum key. In Java you can code with the specific
 * class, while in the compiled code this information is stripped and just uses
 * the JavaScript key.
 * <pre>
 * public class MyEnum extends JavaScriptObject {
 * 
 *   public static final MyEnum A = createA();
 * 
 *   private static final native createA() /*-{
 *     return $wnd.MyEnum.A;
 *   * /-};
 *
 *   protected MyEnum { }
 * 
 *   public static final native double value() /*-{ return this; }-* /;
 * }
 * </pre>
 * The actual enum value can be accessed via <code>value()</code>.
 */
public class JSNIEnumPrinter {

  public void printEnum(final StringBuffer buffer, final int indent,
      final String packageName, final String enumName,
      final boolean _static, final List<EnumValue> enumValues) {
    PrintUtil.indent(buffer, indent);
    buffer.append("public ");
    if (_static) {
      buffer.append("static ");
    }
    buffer.append("class ");
    buffer.append(enumName);
    buffer.append(" extends ");
    buffer.append(TypeMapper.GWT_JAVA_SCRIPT_OBJECT);
    buffer.append(" {");
    PrintUtil.nl(buffer);
    printEnumValues(buffer, indent + 1, packageName, enumName, enumValues);
    printConstructor(buffer, indent + 1, enumName);
    printValueMethod(buffer, indent + 1, enumValues.get(0).getType());
  }

  private void printEnumValues(final StringBuffer buffer, final int indent,
      final String packageName, final String enumName,
      final List<EnumValue> enumValues) {
    for (final EnumValue enumValue : enumValues) {
      final String name = enumValue.getName();
      PrintUtil.nlIndent(buffer, indent);
      buffer.append("public static final ");
      buffer.append(enumName);
      buffer.append(' ');
      buffer.append(name);
      buffer.append(" = create");
      buffer.append(name);
      buffer.append("();");
      PrintUtil.nl(buffer);
      PrintUtil.nlIndent(buffer, indent);
      buffer.append("private static final native ");
      buffer.append(enumName);
      buffer.append(" create");
      buffer.append(name);
      buffer.append("() /*-{");
      PrintUtil.nlIndent(buffer, indent + 1);
      buffer.append("return $wnd.");
      buffer.append(packageName + "." + enumName);
      buffer.append('.');
      buffer.append(name);
      buffer.append(';');
      PrintUtil.nlIndent(buffer, indent);
      buffer.append("}-*/;");
      PrintUtil.nl(buffer);
    }
  }

  private void printConstructor(final StringBuffer buffer, final int indent,
      final String enumName) {
    PrintUtil.nlIndent(buffer, indent);
    buffer.append("protected ");
    buffer.append(enumName);
    buffer.append("() {}");
    PrintUtil.nlIndent(buffer, indent);
  }

  private void printValueMethod(final StringBuffer buffer, final int indent,
      final String type) {
    PrintUtil.nlIndent(buffer, indent);
    buffer.append("public final native ");
    buffer.append(type);
    buffer.append(" value() /*-{ return this; }-*/;");
  }
}
