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
import gengwtjs.lang.java.JMethod;
import gengwtjs.lang.java.JParam;
import gengwtjs.lang.java.JavaFile;
import gengwtjs.lang.js.JsElement.JsParam;
import gengwtjs.lang.js.JsFile;
import gengwtjs.lang.js.JsMethod;
import gengwtjs.lang.js.JsType;
import gengwtjs.lang.js.JsType.JsTypeSpec;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Transforms JavaScript into Java.
 */
class Transformer {
  private static final String GWT_JSNI_PACKAGE = "com.google.gwt.core.client.";
  private static final Logger LOG = LoggerFactory.getLogger(Transformer.class);

  private final Set<String> ignoreMethods = new HashSet<>();

  public Transformer() {
    ignoreMethods.add("toString");
  }

  public JavaFile transform(final JsFile jsFile) {
    final JClass jFile =
        new JClass(jsFile.getPackageName(), jsFile.getClassOrInterfaceName());
    addImports(jFile);
    jFile.setClassDescription(jsFile.getElement().getJsDoc());
    for (final JsFile subFile: jsFile.getInnerJFiles()) {
      jFile.addInnerJFile(transform(subFile));
    }
    if (jsFile.getElement().getTypeDef() instanceof List) {
      transformFields(jFile, (List<JsParam>) jsFile.getElement().getTypeDef());
      jFile.setDataClass(true);
    }
    setExtends(jFile, jsFile);
    transformFields(jFile, jsFile.getFields());
    for(final JsMethod jsMethod: jsFile.getMethods()) {
      if (!ignoreMethod(jsMethod)) {
        final JMethod method = transformMethod(jsMethod);
        if (jsMethod.getElement().isClassDescription()) {
          jFile.setClassDescription(jsMethod.getElement().getJsDoc());
        }
        if (jsMethod.getElement().isConstructor()) {
          jFile.addConstructor(method);
        } else {
          jFile.addMethod(method);
        }
      }
    }
    return jFile;
  }

  private void addImports(final JavaFile javaFile) {
    javaFile.getImports().add(
        GWT_JSNI_PACKAGE + TypeMapper.GWT_JAVA_SCRIPT_OBJECT);
  }

  private void setExtends(final JClass jFile, final JsFile jsFile) {
    final JsType extendsType = jsFile.getElement().getExtends();
    if (jsFile.getElement().getGenericType() != null) {
      jFile.setClassGeneric(
          TypeMapper.INSTANCE.mapType(jsFile.getElement().getGenericType()));
    }
    if (!jFile.isDataClass()) {
      jFile.setExtends(extendsType == null
          ? TypeMapper.GWT_JAVA_SCRIPT_OBJECT
              : transformType(extendsType));
    }
  }

  private void transformFields(final JClass jFile, final List<JsParam> jsFields) {
    for (final JsParam jsParam : jsFields) {
      final JParam field =
          jFile.addField(jsParam.getName(), transformType(jsParam.getType()));
      if (jsParam.getElement() != null) {
        field.setJavaDoc(jsParam.getElement().getJsDoc());
      }
    }
  }

  private boolean ignoreMethod(final JsMethod jsMethod) {
    return ignoreMethods.contains(jsMethod.getMethodName())
        || jsMethod.getElement().isOverride()
        || jsMethod.getElement().isPrivate()
        || "clone".equals(jsMethod.getMethodName()); // FIXME clone
  }

  private JMethod transformMethod(final JsMethod jsMethod) {
    final JMethod jMethod = new JMethod(jsMethod.getPackageName(),
        jsMethod.getMethodName(), jsMethod.getAccessType());
    jMethod.setJsDoc(jsMethod.getElement().getJsDoc());
    //    jMethod.setComplex(isComplex(jsMethod));
    setReturnType(jsMethod, jMethod);
    for (final JsParam jsParam: jsMethod.getElement().getParams()) {
      final JParam param =
          new JParam(jsParam.getName(), transformType(jsParam.getType()));
      param.setOptional(jsParam.getType().isOptional());
      jMethod.addParam(param);
    }
    return jMethod;
  }

  private boolean isComplex(final JsMethod jsMethod) {
    if (jsMethod.getElement().getReturn() != null
        && jsMethod.getElement().getReturn().isFunction()) {
      return true;
    }
    for (final JsParam jsParam : jsMethod.getElement().getParams()) {
      if (jsParam.getType().isFunction()) {
        return true;
      }
    }
    return false;
  }

  private void setReturnType(final JsMethod jsMethod, final JMethod jMethod) {
    jMethod.setGenericType(jsMethod.getElement().getGenericType());
    if (jsMethod.getElement().getReturn() == null) {
      jMethod.setReturn("void");
    } else {
      jMethod.setReturn(transformType(jsMethod.getElement().getReturn()));
    }
  }

  // if multiple types, see if JsArrayMixed, JsArrayNumber
  private String transformType(final JsType jsType) {
    String type = "";
    if (!TypeMapper.INSTANCE.mapType(jsType.getRawType()).equals(jsType.getRawType())) {
      return TypeMapper.INSTANCE.mapType(jsType.getRawType());
    }
    type = tranformSpecific(jsType);
    if (type != null) {
      return type;
    } else {
      type = "";
    }
    if (jsType.getTypes().isEmpty()) {
      LOG.error("Type empty: {}", jsType);
    } else if (jsType.getTypes().size() > 1) {
      final JsTypeSpec other = containsUndefined(jsType.getTypes());
      if (jsType.getTypes().size() == 2 && other != null) {
        type = TypeMapper.INSTANCE.mapType(other.getName());
      } else {
        type = TypeMapper.GWT_JAVA_SCRIPT_OBJECT;
      }
    } else {
      type = transformType(jsType.getTypes().get(0), false);
      if (jsType.isVarArgs()) {
        type = type + "...";
      }
    }
    return type;
  }
  private String transformType(final JsTypeSpec jsTypeSpec,
      final boolean generic) {
    String type = "";
    if (jsTypeSpec.isGeneric()) {
      if (jsTypeSpec.getGenerics().size() > 1) {
        type = TypeMapper.GWT_JAVA_SCRIPT_OBJECT;
      } else {
        final String sType = tranformSpecific(jsTypeSpec.getGenerics().get(0));
        type = TypeMapper.INSTANCE.mapType(jsTypeSpec.getName(), generic) + "<"
            + (sType == null
            ? transformType(jsTypeSpec.getGenerics().get(0), true) : sType)
            + ">";
      }
    } else {
      type = TypeMapper.INSTANCE.mapType(jsTypeSpec.getName(), generic);
    }
    return type;
  }

  private String tranformSpecific(final JsType jsType) {
    final String ts;
    if (jsType.isFunction()) {
      ts = TypeMapper.GWT_JAVA_SCRIPT_OBJECT;
    } else if (jsType.getTypes().size() == 1) {
      ts = tranformSpecific(jsType.getTypes().get(0));
    } else if (jsType.getTypes().size() == 2) {
      final JsTypeSpec other = containsUndefined(jsType.getTypes());
      ts = other == null ? null : tranformSpecific(other);
    } else {
      ts = null;
    }
    return ts;
  }

  private String tranformSpecific(final JsTypeSpec jsTypeSpec) {
    String specific = null;
    if (jsTypeSpec.getGenerics().size() == 1
        && "Array".equals(jsTypeSpec.getName())) {
      if ("string".equals(jsTypeSpec.getGenerics().get(0).getName())) {
        specific = "Array.<string>";
      } else if ("number".equals(jsTypeSpec.getGenerics().get(0).getName())) {
        specific = "Array.<number>";
      }
    }
    return specific == null ? null : TypeMapper.INSTANCE.mapType(specific);
  }

  private JsTypeSpec containsUndefined(final List<JsTypeSpec> list) {
    boolean undefined = false;
    JsTypeSpec other = null;
    for (final JsTypeSpec jsTypeSpec : list) {
      if ("undefined".equals(jsTypeSpec.getName())) {
        undefined = true;
      } else {
        other = jsTypeSpec;
      }
    }
    return undefined ? other : null;
  }
}
