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
import gruifo.lang.js.JsFile;
import gruifo.lang.js.JsMethod;
import gruifo.lang.js.JsParam;
import gruifo.output.PrintUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.mozilla.javascript.Token;
import org.mozilla.javascript.ast.Assignment;
import org.mozilla.javascript.ast.AstNode;
import org.mozilla.javascript.ast.AstRoot;
import org.mozilla.javascript.ast.ExpressionStatement;
import org.mozilla.javascript.ast.FunctionNode;
import org.mozilla.javascript.ast.Name;
import org.mozilla.javascript.ast.NodeVisitor;
import org.mozilla.javascript.ast.ObjectLiteral;
import org.mozilla.javascript.ast.ObjectProperty;
import org.mozilla.javascript.ast.PropertyGet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JavaScriptFileParser implements NodeVisitor {
  private static final Logger LOG = LoggerFactory
      .getLogger(JavaScriptFileParser.class);

  private static final String PROTOTYPE = "prototype";
  private static final Pattern PROTOTYPE_PATTERN =
      Pattern.compile("((.+\\.)?([^\\.]+))\\." + PROTOTYPE + "\\.(.+)");
  private static final Pattern STATIC_PATTERN =
      Pattern.compile("(([^\\.]+))\\.(.+)");
  private final Map<String, JsFile> files = new HashMap<>();
  private final Map<String, JsElement> consts = new HashMap<>();
  private final List<JsMethod> staticMethods = new ArrayList<>();
  private final JavaScriptDocParser parser = new JavaScriptDocParser();

  private final String fileName;

  public JavaScriptFileParser(final String fileName) {
    this.fileName = fileName;
  }

  @Override
  public boolean visit(final AstNode node) {
    if (isRootNode(node)) {
      if (node.getType() == Token.ASSIGN) {
        visitAssignment((Assignment) node);
      } else if (node.getType() == Token.CONST) {
        LOG.error("FIXME:Const node detected, not parsed in file:{}", fileName);
      } else {
        visitOtherNode(node);
      }
    }
    return true;
  }


  private void visitAssignment(final Assignment node) {
    if (node.getLeft() instanceof PropertyGet) {
      visitAssignmentAtRoot(node);
    }
  }

  private void visitAssignmentAtRoot(final Assignment node) {
    if (node.getRight() instanceof FunctionNode
        || ingoreAbstractMethod(node)) {
      if (node.getParent() instanceof ExpressionStatement) {
        try {
          visitMethodOrClass(((PropertyGet) node.getLeft()).toSource(),
              node.getJsDoc());
          if (node.getRight() instanceof PropertyGet) {
            LOG.info("left:{}, right:{}",
                ((PropertyGet) node.getLeft()).toSource(),
                ((PropertyGet) node.getRight()).toSource());
          }
        } catch (final ClassCastException e) {
          LOG.error("Node different then expcected in file:{},", fileName, e);
        }
      } else {
        //        LOG.debug("Node at linenr {} ignored in file:{}",
        //            node.getLineno(), fileName);
      }
    } else if (node.getParent() instanceof ExpressionStatement) {
      visitMethodOrEnum(((PropertyGet) node.getLeft()).toSource(),
          node.getJsDoc(), node.getRight());
    }
  }

  private boolean ingoreAbstractMethod(final Assignment node) {
    return node.getRight() instanceof PropertyGet
        && !"goog.abstractMethod".equals(
            ((PropertyGet) node.getRight()).toSource());
  }

  private boolean isRootNode(final AstNode node) {
    return node.getParent() != null
        && node.getParent().getParent() instanceof AstRoot;
  }

  private void visitMethodOrEnum(final String name, final String jsDoc,
      final AstNode astNode) {
    if (jsDoc == null) {
      //TODO sometimes values are recognized as enums even if they are not.
      LOG.error("Comment in node {} for file {} is empty.", name, fileName);
      return;
    }
    final JsElement element = parser.parse(fileName, jsDoc);
    if (element == null || element.isPrivate()) {
      return; // ignore private stuff...
    }
    if (element.isEnum()) {
      final JsFile jsFile = parseClassOrInterfaceName(name, false, element);

      files.put(name, jsFile);
      if (astNode instanceof ObjectLiteral) {
        final ObjectLiteral ol = (ObjectLiteral) astNode;
        for (final ObjectProperty op : ol.getElements()) {
          final Name left = (Name) op.getLeft();
          jsFile.addEnumValue(left.toSource(), left.getJsDoc());
        }
      }
    } else if (isMethod(name, element)) {
      //method assigned as method variable.
      final JsMethod method = addMethod(name, element, false);
      if (method == null) {
        LOG.warn("Should this be abstract: {} in file:{}", name, fileName);
      } else {
        method.setAbstract(true);
      }
    } else if (element.isConst() || element.isDefine()){
      consts.put(name, element);
    } else {
      LOG.warn("We missed something: {}: {} in file:{}", name, element,
          fileName);
    }
  }

  private void visitMethodOrClass(final String methodOrClassName,
      final String jsDoc) {
    if (jsDoc == null) {
      LOG.error("Comment in for {} is empty in file:{}", methodOrClassName,
          fileName);
      return;
    }
    final JsElement element = parser.parse(fileName, jsDoc);
    if (element == null || element.isPrivate() && !element.isConstructor()) {
      return; // ignore private stuff...
    }
    if (element.isClass() || element.isInterface() || element.isEnum()) {
      if (files.containsKey(methodOrClassName)) {
        LOG.warn("Class twice in javascript file? class: {} in file:{}",
            methodOrClassName, fileName);
      }
      final JsFile jFile = parseClassOrInterfaceName(methodOrClassName,
          element.isInterface(), element);
      files.put(methodOrClassName, jFile);
      addMethodOrField(methodOrClassName, element, true);
    } else if (isMethod(methodOrClassName, element)) {
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

  /**
   * Add as method.
   * 
   * If it's an @interface it's parsed as method, but should not be added as
   * method.
   * 
   * if no file object could be found then it's a static method, do it's added
   * to the global list of static methods.
   * @param methodOrClassName
   * @param element
   * @param constructor
   * @return
   */
  private JsMethod addMethod(final String methodOrClassName,
      final JsElement element, final boolean constructor) {
    final JsMethod method =
        parseMethod(methodOrClassName, element, constructor);
    if (method != null && !element.isInterface()) {
      method.setElement(element);
      final JsFile jsFile = files.get(method.getPackageName());
      if (jsFile == null) {
        staticMethods.add(method);
      } else {
        jsFile.addMethod(method);
      }
    }
    return method;
  }

  private void addAsField(final String name, final JsElement element) {
    final Matcher nameMatcher = PROTOTYPE_PATTERN.matcher(name);
    if (nameMatcher.find()) {
      final String packageName = nameMatcher.group(1);
      final String fieldName = nameMatcher.group(4);
      final JsParam field = new JsParam(fieldName, element);
      final JsFile jsFile = files.get(packageName);
      if (jsFile == null) {
        LOG.warn("Class for package name:{}, from name:{} not in file:{}"
            , packageName, name, fileName);
      } else {
        jsFile.addField(field);
      }
    } else {
      LOG.warn("Field didn't match prototype pattern: {} (type:{}) in file:{}",
          name, element.getType(), fileName);
    }
  }

  JsFile parseClassOrInterfaceName(final String name, final boolean interfce,
      final JsElement element) {
    final int classNameIdx = name.lastIndexOf('.');
    final JsFile jsFile = new JsFile(fileName, name.substring(0, classNameIdx),
        name.substring(classNameIdx + 1), interfce);
    jsFile.setElement(element);
    return jsFile;
  }

  JsMethod parseMethod(final String name, final JsElement element,
      final boolean constructor) {
    if (constructor) {
      final int prot = name.lastIndexOf(".") + 1;
      final String methodName = name.substring(prot);
      return new JsMethod(name, methodName);
    } else {
      return parsePrototypeMethod(name, element);
    }
  }

  JsMethod parsePrototypeMethod(final String name, final JsElement element) {
    JsMethod method = matchMethod(PROTOTYPE_PATTERN, 4, name);
    if (method == null) {
      method = matchMethod(STATIC_PATTERN, 2, name);
      if (method == null) {
        LOG.error("Missed method {} in: {}", name, fileName);
      } else {
        method.setStaticMethod(true);
      }
    }
    return method;
  }

  private JsMethod matchMethod(final Pattern pattern, final int methodNameIndex,
      final String name) {
    final Matcher nameMatcher = pattern.matcher(name);
    if (nameMatcher.find()) {
      final String packageName = nameMatcher.group(1);
      final String methodName = nameMatcher.group(methodNameIndex);
      return new JsMethod(packageName, methodName);
    } else {
      return null;
    }
  }

  private boolean isMethod(final String functionName, final JsElement element) {
    return functionName.indexOf(PROTOTYPE) > 0 || element.isMethod();
  }

  private void visitOtherNode(final AstNode node) {
    if (node instanceof PropertyGet
        && node.getParent() instanceof ExpressionStatement) {
      if (node.getJsDoc() == null) {
        LOG.error("Node {} has empty comment in file:{}",
            node.toSource(), fileName);
        return;
      }
      final JsElement element = parser.parse(fileName, node.getJsDoc());
      final String typedef = node.toSource();
      if (isMethod(typedef, element)) {
        addMethodOrField(typedef, element, false);
      } else if (element.getType() == null) {
        final JsFile jFile = parseClassOrInterfaceName(typedef,
            element.isInterface(), element);
        files.put(typedef, jFile);
      } else {
        LOG.error("Type '{}' ignored in file:{}", typedef, fileName);
      }
    }
  }

  public Collection<JsFile> getFiles() {
    collectConsts();
    return files.values();
  }

  public Map<String, JsElement> getConsts() {
    return consts;
  }

  public List<JsMethod> getStaticMethods() {
    return staticMethods;
  }

  private void collectConsts() {
    final List<Entry<String, JsElement>> removed = new ArrayList<>();
    for (final Entry<String, JsElement> cnst: consts.entrySet()) {
      if (addConst(cnst.getKey(), cnst.getValue())) {
        removed.add(cnst);
      }
    }
    consts.entrySet().removeAll(removed);
  }

  private boolean addConst(final String constName, final JsElement element) {
    final String fullClassName =
        constName.substring(0, constName.lastIndexOf('.'));
    final boolean added;
    if (files.containsKey(fullClassName)) {
      final JsFile jsFile = files.get(fullClassName);
      jsFile.addField(new JsParam(constName, element));
      added = true;
    } else {
      final String fullConstName = getFullConstName(fullClassName);
      if (fullConstName.isEmpty()) {
        added = false;
      } else {
        final JsFile cjsFile;
        if (files.containsKey(fullConstName)) {
          cjsFile = files.get(fullConstName);
          cjsFile.addField(new JsParam(constName, element));
          added = true;
        } else {
          added = false;
        }
      }
    }
    return added;
  }

  private String getFullConstName(final String fullClassName) {
    final int classSep = fullClassName.lastIndexOf('.');
    final String fullConstName;
    if (classSep > 0) {
      final String newClassName = PrintUtil.firstCharUpper(
          fullClassName.substring(classSep + 1)) + "Constants";
      fullConstName = fullClassName.substring(0, classSep) + '.' + newClassName;
    } else {
      fullConstName = "";
    }
    return fullConstName;
  }
}
