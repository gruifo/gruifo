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
package gengwtjs.parser;

import gengwtjs.lang.js.JsElement;
import gengwtjs.lang.js.JsFile;
import gengwtjs.lang.js.JsMethod;
import gengwtjs.output.PrintUtil;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

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

  public Map<String, JsFile> files = new HashMap<>();
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
          jsFile.addEnumValue(left.toSource());
        }
        // LOG.warn("Yes this is a ArrayLiteral: {} in file: {}", enumName,
        // fileName);
      }
    } else if (isPrototype(enumName)) {
      addMethod(enumName, element);
    } else if (element.isConst()){
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
      LOG.error("Comment in for {} file {} is empty.", methodOrClassName, fileName);
      return;
    }
    final JsElement element = parser.parse(fileName, jsDoc);
    if (element == null || element.isPrivate()) {
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
    } else if (isPrototype(methodOrClassName)) {
      addMethod(methodOrClassName, element);
    }
  }

  private void addMethod(final String methodOrClassName, final JsElement element) {
    final JsMethod method = parseMethod(methodOrClassName);
    method.setElement(element);
    final JsFile JsFile = files.get(method.getClassPath());
    if (JsFile == null) {
      LOG.warn("class not in file for: {}", method.getClassPath());
    } else {
      JsFile.addMethod(method);
    }
  }

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
      jsFile = new JsFile(packageName, className, false);
      jsFile.setElement(element);
      files.put(className, jsFile);
    }
    jsFile.addConst(constName, element);
  }


  JsFile parseClassOrInterfaceName(final String name, final boolean _interface) {
    final int classNameIdx = name.lastIndexOf('.');
    return new JsFile(name.substring(0, classNameIdx),
        name.substring(classNameIdx + 1), _interface);
  }

  JsMethod parseMethod(final String name) {
    final int prot = name.indexOf(PROTOTYPE);
    final String packageName = name.substring(0, prot - 1);
    final String functionName = name.substring(prot + PROTOTYPE.length() + 1);
    return new JsMethod(packageName, functionName);
  }

  private boolean isPrototype(final String functionName) {
    return functionName.indexOf(PROTOTYPE) > 0;
  }

  private void addImports(final Set<String> importSet, final JsElement jsElement) {
    // FIXME addImports
    //    for (final JsParam param : jsElement.getParams()) {
    //      if (shouldImport(param)) {
    //        importSet.add(param.getType());
    //      }
    //    }
    //    if (shouldImport(jsElement.getReturn())) {
    //      importSet.add(jsElement.getReturn().getType());
    //    }
  }

  //  private boolean shouldImport(final JsType type) {
  //    return type == null || type.getType() == null ? false : !NATIVE_TYPES
  //        .contains(type.getType().toLowerCase());
  //  }

  private void checkAndAddTypedef(final AstNode node) {
    if (node instanceof PropertyGet
        && node.getParent() instanceof ExpressionStatement
        && node.getParent().getParent() instanceof AstRoot) {
      if (node.getJsDoc() == null) {
        LOG.error("Node {} has empty comment in file {}", node.toSource(), fileName);
        return;
      }
      final JsElement element = parser.parse(fileName, node.getJsDoc());
      final String typedef = node.toSource();
      if (isPrototype(typedef)) {
        addMethod(typedef, element);
      } else {
        final JsFile jFile = parseClassOrInterfaceName(typedef,
            element.isInterface());
        jFile.setElement(element);
        files.put(typedef, jFile);
      }
    }
  }
}
