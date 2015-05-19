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
package gruifo.parser;

import gruifo.lang.js.JsElement;
import gruifo.lang.js.JsElement.JsParam;
import gruifo.lang.js.JsFile;
import gruifo.lang.js.JsMethod;
import gruifo.output.PrintUtil;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.mozilla.javascript.Token;
import org.mozilla.javascript.ast.Assignment;
import org.mozilla.javascript.ast.AstNode;
import org.mozilla.javascript.ast.AstRoot;
import org.mozilla.javascript.ast.Block;
import org.mozilla.javascript.ast.ExpressionStatement;
import org.mozilla.javascript.ast.FunctionNode;
import org.mozilla.javascript.ast.Name;
import org.mozilla.javascript.ast.NodeVisitor;
import org.mozilla.javascript.ast.ObjectLiteral;
import org.mozilla.javascript.ast.ObjectProperty;
import org.mozilla.javascript.ast.PropertyGet;
import org.mozilla.javascript.ast.Scope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JavaScriptFileParser implements NodeVisitor {
  private static final Logger LOG = LoggerFactory
      .getLogger(JavaScriptFileParser.class);

  private static final String PROTOTYPE = "prototype";
  private static final Pattern PROTOTYPE_PATTERN =
      Pattern.compile("((.+)\\.([^\\.]+))\\." + PROTOTYPE + "\\.(.+)");
  private final Map<String, JsFile> files = new HashMap<>();
  private final JavaScriptDocParser parser = new JavaScriptDocParser();

  private final String fileName;

  public JavaScriptFileParser(final String fileName) {
    this.fileName = fileName;
  }

  @Override
  public boolean visit(final AstNode node) {
    if (node.getType() == Token.ASSIGN) {
      visitAssignment((Assignment) node);
    } else if (node.getType() == Token.CONST) {
      LOG.error("FIXME: Const node detected, not parsed");
    } else {
      checkAndAddTypedef(node);
    }
    return true;
  }

  public Collection<JsFile> getFiles() {
    return files.values();
  }

  private void visitAssignment(final Assignment node) {
    if (node.getRight() instanceof FunctionNode) {
      // only visit functions at root node, not functions inside other
      // functions.
      if (node.getParent() instanceof ExpressionStatement
          && node.getLeft() instanceof PropertyGet
          && isRootNode(node.getParent())) {
        try {
          visitMethodOrClass(((PropertyGet) node.getLeft()).toSource(),
              node.getJsDoc(), (FunctionNode) node.getRight());
        } catch (final ClassCastException e) {
          LOG.error("Node different then file:{},", fileName, e);
        }
      } else {
        LOG.debug("Node at linenr {} ignored: {}", node.getLineno(), fileName);
      }
    } else if (node.getParent() instanceof ExpressionStatement
        && node.getLeft() instanceof PropertyGet
        && isRootNode(node.getParent())) {
      visitEnum(((PropertyGet) node.getLeft()).toSource(), node.getJsDoc(),
          node.getRight());
      // TODO functions calling other functions.
      // LOG.debug("Node found: {} in file: {}", ((PropertyGet)
      // node.getLeft()).toSource(), fileName);
    }
  }

  private boolean isRootNode(final AstNode parent) {
    return !(parent.getParent() instanceof Block
        || (parent.getParent() instanceof Scope
            && ((Scope) (parent.getParent())).getParentScope() instanceof FunctionNode));
  }

  private void visitEnum(final String enumName, final String jsDoc,
      final AstNode astNode) {
    if (jsDoc == null) {
      //TODO sometimes values are recognized as enums even if they are not.
      //      LOG.error("Comment in enum {} for file {} is empty.", enumName, fileName);
      return;
    }
    final JsElement element = parser.parse(fileName, jsDoc);
    if (element == null || element.isPrivate()) {
      return; // ignore private stuff...
    }
    if (element.isEnum()) {
      final JsFile jsFile = parseClassOrInterfaceName(enumName, false);
      jsFile.setElement(element);

      files.put(enumName, jsFile);
      if (astNode instanceof ObjectLiteral) {
        final ObjectLiteral ol = (ObjectLiteral) astNode;
        for (final ObjectProperty op : ol.getElements()) {
          final Name left = (Name) op.getLeft();
          jsFile.addEnumValue(left.toSource(), left.getJsDoc());
        }
        // LOG.warn("Yes this is a ArrayLiteral: {} in file: {}", enumName,
        // fileName);
      }
    } else if (isPrototype(enumName)) {
      addMethod(enumName, element, false).setAbstract(true);
    } else if (element.isConst()){
      LOG.error("Const element '{}' not parsed in: {}", enumName, fileName);
      //FIXME if const added before class itself file is created twice.
      // also const element not at toplevel because it's a field not toplevel.
      //addConst(enumName, element);
    } else {
      LOG.warn("We missed something: {} in file: {}", enumName, fileName);
    }
  }

  private void visitMethodOrClass(final String methodOrClassName,
      final String jsDoc, final FunctionNode right) {
    if (jsDoc == null) {
      LOG.error("Comment in for {} file {} is empty.",
          methodOrClassName, fileName);
      return;
    }
    final JsElement element = parser.parse(fileName, jsDoc);
    if (element == null || element.isPrivate() && !element.isConstructor()) {
      return; // ignore private stuff...
    }
    if (element.isClass() || element.isInterface() || element.isEnum()) {
      if (files.containsKey(methodOrClassName)) {
        LOG.warn("Class twice in javascript file? class: {}",
            methodOrClassName);
      }
      final JsFile jFile = parseClassOrInterfaceName(methodOrClassName,
          element.isInterface());

      jFile.setElement(element);
      files.put(methodOrClassName, jFile);
      addMethodOrField(methodOrClassName, element, true);
    } else if (isPrototype(methodOrClassName)) {
      addMethodOrField(methodOrClassName, element, false);
    }
  }

  private void addMethodOrField(final String methodOrClassName,
      final JsElement element, final boolean constructor) {
    if (element.getType() == null) {
      addMethod(methodOrClassName, element, constructor);
    } else {
      addAsField(methodOrClassName, element);
    }
  }

  private JsMethod addMethod(final String methodOrClassName,
      final JsElement element, final boolean constructor) {
    final JsMethod method = parseMethod(methodOrClassName, constructor);
    if (method != null) {
      method.setElement(element);
      final JsFile jsFile = files.get(method.getPackageName());
      if (jsFile == null) {
        LOG.warn("class not in file for: {}", method.getPackageName());
      } else {
        jsFile.addMethod(method);
      }
    }
    return method;
  }

  private void addAsField(final String name, final JsElement element) {
    final Matcher nameMatcher = PROTOTYPE_PATTERN.matcher(name);
    if (nameMatcher.find()) {
      final String packageName =
          nameMatcher.group(1);// + "." + nameMatcher.group(2);
      final String fieldName = nameMatcher.group(4);
      final JsParam field = new JsParam(fieldName, element);
      final JsFile jsFile = files.get(packageName);
      if (jsFile == null) {
        LOG.warn("Class not in file for: {}", packageName);
      } else {
        jsFile.addField(field);
      }
    } else {
      LOG.warn("Field didn't match prototype pattern: {}", name);
    }
  }

  //FIXME fix Const parseing
  private void addConst(final String constName, final JsElement element) {
    final String packageName =
        constName.substring(0, constName.lastIndexOf('.'));
    final int packLastIdx = packageName.lastIndexOf('.');
    final String className = PrintUtil.firstCharUpper(
        packLastIdx == -1 ? constName : packageName.substring(packLastIdx + 1));
    final JsFile jsFile;
    if (files.containsKey(className)) {
      jsFile = files.get(className);
    } else {
      jsFile = new JsFile(fileName, packageName, className, false);
      jsFile.setElement(element);
      files.put(className, jsFile);
    }
    jsFile.addConst(constName, element);
  }


  JsFile parseClassOrInterfaceName(final String name, final boolean _interface) {
    final int classNameIdx = name.lastIndexOf('.');
    return new JsFile(fileName, name.substring(0, classNameIdx),
        name.substring(classNameIdx + 1), _interface);
  }

  JsMethod parseMethod(final String name, final boolean constructor) {
    if (constructor) {
      final int prot = name.lastIndexOf(".") + 1;
      final String methodName = name.substring(prot);
      return new JsMethod(name, methodName);
    } else {
      return parsePrototypeMethod(name);
    }
  }

  JsMethod parsePrototypeMethod(final String name) {
    final Matcher nameMatcher = PROTOTYPE_PATTERN.matcher(name);
    if (nameMatcher.find()) {
      final String packageName = nameMatcher.group(1);
      final String methodName = nameMatcher.group(4);
      return new JsMethod(packageName, methodName);
    } else {
      LOG.warn("Field didn't match prototype pattern: {}", name);
      return null;
    }
  }

  private boolean isPrototype(final String functionName) {
    return functionName.indexOf(PROTOTYPE) > 0;
  }

  private void checkAndAddTypedef(final AstNode node) {
    if (node instanceof PropertyGet
        && node.getParent() instanceof ExpressionStatement
        && node.getParent().getParent() instanceof AstRoot) {
      if (node.getJsDoc() == null) {
        LOG.error("Node {} has empty comment: {}", node.toSource(), fileName);
        return;
      }
      final JsElement element = parser.parse(fileName, node.getJsDoc());
      final String typedef = node.toSource();
      if (isPrototype(typedef)) {
        addMethodOrField(typedef, element, false);
      } else if (element.getType() == null) {
        final JsFile jFile = parseClassOrInterfaceName(typedef,
            element.isInterface());
        jFile.setElement(element);
        files.put(typedef, jFile);
      } else {
        LOG.error("Type '{}' ignored: {}", typedef, fileName);
      }
    }
  }
}
