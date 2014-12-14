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
import gruifo.lang.js.JsElement.JsParam;
import gruifo.lang.js.JsEnum;
import gruifo.lang.js.JsFile;
import gruifo.lang.js.JsMethod;
import gruifo.lang.js.JsType;
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

  private static final Logger LOG = LoggerFactory.getLogger(Transformer.class);
  private static final TypeMapper TYPE_MAPPER = TypeMapper.INSTANCE;

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
    if (jsFile.getElement().isTypeDef()) {
      transformFields(jFile, jsFile.getElement().getTypeDef());
      jFile.setDataClass(true);
      jFile.setExtends(null); // FIXME: setExtends(null) needed?
    }
    setExtends(jFile, jsFile);
    transformEnumFields(jFile, jsFile.getElement().getEnumType(),
        jsFile.getEnumValues());
    transformFields(jFile, jsFile.getFields());
    transformMethods(jsFile, jFile);
    return jFile;
  }

  private void transformEnumFields(final JClass jFile, final JsType enumType,
      final List<JsEnum> list) {
    if (!list.isEmpty()) {
      jFile.setDataClass(true);
      jFile.setExtends(null); //FIXME why doesn't set dataclass alone not work?
    }
    for (final JsEnum enumValue: list) {
      jFile.addEnumValue(enumValue.getFieldName(), transformType(enumType),
          enumValue.getJsDoc());
    }
  }

  private void transformMethods(final JsFile jsFile, final JClass jFile) {
    for(final JsMethod jsMethod: jsFile.getMethods()) {
      if (!ignoreMethod(jFile.getFullClassName(), jsMethod)) {
        for (final List<JParam> params : methodParams(
            jsMethod.getElement().getParams())) {
          final JMethod method = transformMethod(jFile, jsMethod, params);
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
  }

  private void setExtends(final JClass jFile, final JsFile jsFile) {
    final JsType extendsType = jsFile.getElement().getExtends();
    if (jsFile.getElement().getGenericType() != null) {
      jFile.setClassGeneric(
          TYPE_MAPPER.mapType(jsFile.getElement().getGenericType()));
    }
    if (jFile.isDataClass()) {
      jFile.setExtends(null);
    } else {
      jFile.setExtends(extendsType == null
          ? TypeMapper.GWT_JAVA_SCRIPT_OBJECT
              : transformType(extendsType));
    }
  }

  private void transformFields(final JClass jFile,
      final List<JsParam> jsFields) {
    for (final JsParam jsParam : jsFields) {
      if (!TYPE_MAPPER.ignore(jFile.getFullClassName(), jsParam.getName())) {
        final JParam field =
            jFile.addField(jsParam.getName(), transformType(jsParam.getType()));
        if (jsParam.getElement() != null) {
          field.setJavaDoc(jsParam.getElement().getJsDoc());
        }
      }
    }
  }

  private boolean ignoreMethod(final String clazz, final JsMethod jsMethod) {
    return ignoreMethods.contains(jsMethod.getMethodName())
        || jsMethod.getElement().isOverride()
        || jsMethod.getElement().isPrivate()
        || TYPE_MAPPER.ignore(clazz, jsMethod.getMethodName())
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

  private JMethod transformMethod(final JClass jFile, final JsMethod jsMethod,
      final List<JParam> params) {
    final JMethod jMethod = new JMethod(jsMethod.getPackageName(),
        jsMethod.getMethodName(), jsMethod.getAccessType());
    jMethod.setJsDoc(jsMethod.getElement().getJsDoc());
    setReturnType(jsMethod, jMethod);
    for (final JParam param : params) {
      jMethod.addParam(filterParam(jFile, jMethod, param));
    }
    return jMethod;
  }

  /**
   * Replace the type for the parameter if a type is set in the configuration.
   * @param jFile
   * @param jMethod
   * @param param
   * @return
   */
  private JParam filterParam(final JClass jFile, final JMethod jMethod,
      final JParam param) {
    final String replaceType = TYPE_MAPPER.replaceType(jFile.getFullClassName(),
        jMethod.getMethodName(), param.getName());
    if (replaceType != null) {
      param.setType(replaceType);
    }
    return param;
  }

  private void setReturnType(final JsMethod jsMethod, final JMethod jMethod) {
    jMethod.setGenericType(jsMethod.getElement().getGenericType());
    if (jsMethod.getElement().getReturn() == null) {
      jMethod.setReturn("void");
    } else {
      jMethod.setReturn(transformType(jsMethod.getElement().getReturn()));
    }
  }

  private String transformType(final JsType jsType) {
    String type = "";
    if (!TYPE_MAPPER.mapType(jsType.getRawType()).equals(jsType.getRawType())) {
      return TYPE_MAPPER.mapType(jsType.getRawType());
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
        type = TYPE_MAPPER.mapType(other.getName());
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
        type = TYPE_MAPPER.mapType(jsTypeSpec.getName(), generic) + "<"
            + (sType == null
            ? transformType(jsTypeSpec.getGenerics().get(0), true) : sType)
            + ">";
      }
    } else {
      type = TYPE_MAPPER.mapType(jsTypeSpec.getName(), generic);
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
    return specific == null ? null : TYPE_MAPPER.mapType(specific);
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
