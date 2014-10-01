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

import gengwtjs.lang.java.JParam;
import gengwtjs.lang.java.JavaFile;
import gengwtjs.output.PrintUtil;

/**
 * Prints Field members.
 */
public class JSNIFieldPrinter {

  public void writeFields(final StringBuffer buffer, final int indent,
      final JavaFile jFile) {
    for (final JParam field : jFile.getFields()) {
      printGetter(buffer, indent, field);
      printSetter(buffer, indent, field);
    }
  }

  private void printGetter(final StringBuffer buffer, final int indent,
      final JParam field) {
    PrintUtil.indent(buffer, indent);
    buffer.append("public native ");
    buffer.append(field.getType());
    buffer.append(" get");
    buffer.append(PrintUtil.firstCharUpper(field.getName()));
    buffer.append("() /*-{");
    PrintUtil.nl(buffer);
    PrintUtil.indent(buffer, indent + 1);
    buffer.append("return this['");
    buffer.append(field.getName());
    buffer.append("'];");
    PrintUtil.nl(buffer);
    PrintUtil.indent(buffer, indent);
    buffer.append("}-*/;");
    PrintUtil.nl2(buffer);
  }

  private void printSetter(final StringBuffer buffer, final int indent,
      final JParam field) {
    PrintUtil.indent(buffer, indent);
    buffer.append("public native void");
    buffer.append(" set");
    buffer.append(PrintUtil.firstCharUpper(field.getName()));
    buffer.append('(');
    buffer.append(field.getType());
    buffer.append(' ');
    buffer.append(field.getName());
    buffer.append(") /*-{");
    PrintUtil.nl(buffer);
    PrintUtil.indent(buffer, indent + 1);
    buffer.append("this['");
    buffer.append(field.getName());
    buffer.append("'] = ");
    buffer.append(field.getName());
    buffer.append(';');
    PrintUtil.nl(buffer);
    PrintUtil.indent(buffer, indent);
    buffer.append("}-*/;");
    PrintUtil.nl2(buffer);
  }
}
