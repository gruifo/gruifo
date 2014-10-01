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

import gengwtjs.lang.java.JClass;
import gengwtjs.lang.java.JavaFile;
import gengwtjs.lang.java.JavaFile.EnumValue;
import gengwtjs.lang.js.JsFile;
import gengwtjs.output.FilePrinter;
import gengwtjs.output.PrintUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class JSNIPrinter implements FilePrinter {

  private final TypeMapper typeMapper = TypeMapper.INSTANCE;
  private final Transformer transformer = new Transformer();
  private final JSNIMethodPrinter mPrinter = new JSNIMethodPrinter();
  private final JSNIFieldPrinter fPrinter = new JSNIFieldPrinter();

  @Override
  public String printFile(final JsFile jsFile) {
    return printFile(transformer.transform(jsFile));
  }

  public String printFile(final JavaFile jFile) {
    final int indent = 0;
    final StringBuffer buffer = new StringBuffer();
    buffer.append(jFile.getHeaderComment());
    writePackageName(buffer, jFile.getPackageName());
    writeImports(buffer, jFile.getImports());
    jFile.setStatic(false); //FIXME setting static should not be done here
    printJavaFile(jFile, indent, buffer);
    return buffer.toString();
  }

  private void printJavaFile(final JavaFile jFile, int indent,
      final StringBuffer buffer) {
    writeClass(buffer, (JClass) jFile, indent);
    indent++;
    writeEnumFields(buffer, jFile.getPackageName()
        + "." + jFile.getClassOrInterfaceName(), jFile.getEnumValues());
    if (jFile.getEnumValues().isEmpty()) {
      writeConstructors(indent, buffer, (JClass) jFile);
    }
    fPrinter.writeFields(buffer, indent, jFile);
    mPrinter.writeMethods(buffer, indent, jFile);
    for (final JavaFile innerFile: jFile.getInnerJFiles()) {
      innerFile.setStatic(true); //FIXME setting static should not be done here
      printJavaFile(innerFile, indent, buffer);
    }
    buffer.append('}'); // close file
    PrintUtil.nl(buffer);
  }

  private void writePackageName(final StringBuffer buffer,
      final String packageName) {
    buffer.append("package ");
    buffer.append(packageName);
    buffer.append(';');
    PrintUtil.nl2(buffer);
  }

  private void writeImports(final StringBuffer buffer, final Set<String> imports) {
    final ArrayList<String> importList = new ArrayList<>(imports);
    Collections.sort(importList);
    for (final String imp : importList) {
      buffer.append("import ");
      buffer.append(typeMapper.mapType(imp));
      buffer.append(';');
      PrintUtil.nl(buffer);
    }
    PrintUtil.nl(buffer);
  }

  private void writeClass(final StringBuffer buffer, final JClass jFile,
      final int indent) {
    PrintUtil.indent(buffer, jFile.getClassDescription(), indent);
    PrintUtil.indent(buffer, indent);
    buffer.append("public ");
    if (jFile.isStatic()) {
      buffer.append("static ");
    }
    buffer.append("class ");
    buffer.append(jFile.getClassOrInterfaceName());
    appendClassExtend(buffer, jFile.getClassGeneric());
    if (jFile instanceof JClass) {
      buffer.append(" extends ");
      buffer.append(jFile.getExtends());
      buffer.append(' ');
    }
    /*    if (jFile.isEnum()) {
      // no extends
    } else {
      if (jFile.getElement().getExtends() == null
          || jFile.getElement().getExtends().getType() == null) {
        buffer.append(GWT_JAVA_SCRIPT_OBJECT);
      } else {
        final JsType type = jFile.getElement().getExtends();

        //        if ("number".equals(type.getFullType())) {
        //          buffer.append(GWT_JAVA_SCRIPT_OBJECT);
        //        } else {
        JSNIMethodPrinter.writeType(buffer, type);
        //        }
      }
    }
     */
    buffer.append('{');
    PrintUtil.nl(buffer);
  }

  private void appendClassExtend(final StringBuffer buffer, final String classGeneric) {
    if (classGeneric != null) {
      buffer.append('<');
      buffer.append(classGeneric);
      buffer.append(" extends ");
      buffer.append(TypeMapper.GWT_JAVA_SCRIPT_OBJECT);
      buffer.append("> ");
    }
  }

  private void writeEnumFields(final StringBuffer buffer,
      final String fullClassName, final List<EnumValue> enumValues) {
    for (final EnumValue enumValue : enumValues) {
      buffer.append("\tpublic static final ");
      if (enumValue.getValue() instanceof String) {
        buffer.append("String ");
      } else if (enumValue.getValue() instanceof Double) {
        buffer.append("double ");
      } else {
        //        LOG.warn("something wrong: {}, {}", enumValue.getName(), enumValue.getValue());
      }
      buffer.append(enumValue.getName());
      buffer.append(" = get");
      buffer.append(enumValue.getName().toLowerCase());
      buffer.append("();\n");
    }
    buffer.append('\n');
    for (final EnumValue enumValue : enumValues) {
      buffer.append("\tprivate static native final ");
      if (enumValue.getValue() instanceof String) {
        buffer.append("String ");
      } else if (enumValue.getValue() instanceof Double) {
        buffer.append("double ");
      } else {
        //        LOG.warn("something wrong: {}, {}", enumValue.getName(), enumValue.getValue());
      }
      buffer.append("get");
      buffer.append(enumValue.getName().toLowerCase());
      buffer.append("() /*-{\n\t\t return $wnd.");
      buffer.append(fullClassName);
      buffer.append(".");
      buffer.append(enumValue.getName());
      buffer.append(";\n\t}-*/;\n");
    }
  }

  private void writeConstructors(final int indent, final StringBuffer buffer,
      final JClass jFile) {
    writeConstructorCreator(indent, buffer, jFile);
    writeProtectedConstructor(indent, buffer, jFile);
  }

  private void writeConstructorCreator(int indent, final StringBuffer buffer,
      final JClass jFile) {
    PrintUtil.indent(buffer, indent);
    buffer.append("public static native ");
    appendClassExtend(buffer, jFile.getClassGeneric());
    buffer.append(jFile.getClassOrInterfaceName());
    if (jFile.getClassGeneric() != null) {
      buffer.append('<');
      buffer.append(jFile.getClassGeneric());
      buffer.append('>');
    }
    buffer.append(" new");
    buffer.append(PrintUtil.firstCharUpper(jFile.getClassOrInterfaceName()));
    buffer.append("() /*-{");
    PrintUtil.nl(buffer);
    PrintUtil.indent(buffer, ++indent);
    buffer.append("return new $wnd.");
    if (!jFile.getPackageName().isEmpty()) {
      buffer.append(jFile.getPackageName());
      buffer.append('.');
    }
    buffer.append(jFile.getClassOrInterfaceName());
    buffer.append('(');
    // FIXME write parameters
    buffer.append(");");
    PrintUtil.nl(buffer);
    PrintUtil.indent(buffer, --indent);
    buffer.append("}-*/;");
    PrintUtil.nl2(buffer);
  }

  /**
   * Generate protected constructor.
   * @param indent
   * @param buffer
   * @param jFile
   */
  private void writeProtectedConstructor(final int indent,
      final StringBuffer buffer, final JClass jFile) {
    PrintUtil.indent(buffer, indent);
    buffer.append("protected ");
    buffer.append(jFile.getClassOrInterfaceName());
    buffer.append("(){ }");
    PrintUtil.nl2(buffer);
  }
}

