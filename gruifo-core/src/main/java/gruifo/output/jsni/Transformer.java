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
import gruifo.lang.java.JMethod;
import gruifo.lang.java.JParam;
import gruifo.lang.java.JavaFile;
import gruifo.lang.js.JsFile;
import gruifo.lang.js.JsMethod;
import gruifo.lang.js.JsType;
import gruifo.lang.js.JsElement.JsParam;
import gruifo.lang.js.JsType.JsTypeSpec;

import java.util.ArrayList;
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
    transformEnumFields(jFile, jsFile.getElement().getEnumType(),
        jsFile.getEnumValues());
    transformFields(jFile, jsFile.getFields());
    transformMethods(jsFile, jFile);
    return jFile;
  }

  private void transformEnumFields(final JClass jFile, final JsType enumType,
      final List<String> enumValues) {
    for (final String enumValue: enumValues) {
      jFile.addEnumValue(enumValue, transformType(enumType));
    }
  }

  private void transformMethods(final JsFile jsFile, final JClass jFile) {
    for(final JsMethod jsMethod: jsFile.getMethods()) {
      if (!ignoreMethod(jsMethod)) {
        for (final List<JParam> params : methodParams(
            jsMethod.getElement().getParams())) {
          final JMethod method = transformMethod(jsMethod, params);
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
    }
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

  private void transformFields(final JClass jFile,
      final List<JsParam> jsFields) {
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

  private List<List<JParam>> methodParams(final List<JsParam> jsParams) {
    final List<List<JParam>> params = new ArrayList<>();
    List<JParam> current = new ArrayList<JParam>();
    params.add(current);
    for (int i = 0; i < jsParams.size(); i++) {
      final JsParam jsParam = jsParams.get(i);
      if (jsParam.getType().isOptional()) {
        current = new ArrayList<JParam>(params.get(params.size() - 1));
        params.add(current);
      }
      final JParam param =
          new JParam(jsParam.getName(), transformType(jsParam.getType()));
      current.add(param);
    }
    return params;
  }

  private JMethod transformMethod(final JsMethod jsMethod,
      final List<JParam> params) {
    final JMethod jMethod = new JMethod(jsMethod.getPackageName(),
        jsMethod.getMethodName(), jsMethod.getAccessType());
    jMethod.setJsDoc(jsMethod.getElement().getJsDoc());
    setReturnType(jsMethod, jMethod);
    for (final JParam param : params) {
      jMethod.addParam(param);
    }
    return jMethod;
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
