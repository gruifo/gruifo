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
        final List<List<JsParam>> list =
            splitMethodParamsOptional(jsMethod.getElement().getParams());
        final List<List<JParam>> jParamList = new ArrayList<>();
        for (final List<JsParam> innerList : list) {
          jParamList.addAll(split2MethodParamsMulti(innerList));
        }
        for (final List<JParam> params : jParamList) {
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

  /**
   * Creates multiple parameter lists if a parameter is optional. If a
   * parameter is optional a parameter list if added without this parameter.
   * @param jsParams List of parameters
   * @return List of List of parameters
   */
  private List<List<JsParam>> splitMethodParamsOptional(final List<JsParam> jsParams) {
    final List<List<JsParam>> params = new ArrayList<>();
    List<JsParam> current = new ArrayList<JsParam>();
    params.add(current);
    for (int i = 0; i < jsParams.size(); i++) {
      final JsParam jsParam = jsParams.get(i);
      if (jsParam.getType().isOptional()) {
        current = new ArrayList<JsParam>(params.get(params.size() - 1));
        params.add(current);
      }
      current.add(jsParam);
    }
    return params;
  }

  /**
   * Creates multiple parameters lists if a parameter type contains multiple types.
   * @param jsParams
   * @return
   */
  private List<List<JParam>> split2MethodParamsMulti(final List<JsParam> jsParams) {
    final List<List<JParam>> params = new ArrayList<>();
    params.add(new ArrayList<JParam>());
    //FIXME This can results in methods with same arguments, because tranformType returns the
    //    same type. Should check if already added...
    for (int i = 0; i < jsParams.size(); i++) {
      final JsParam jsParam = jsParams.get(i);
      if (jsParam.getType().getTypes().size() > 1) {
        final List<JParam> splitParams = new ArrayList<>();
        for (final JsTypeSpec innerJsParam : jsParam.getType().getTypes()) {
          final String transformedType = transformType(innerJsParam, true);
          boolean duplicate = false;
          for (final JParam jParam : splitParams) {
            if (transformedType.equals(jParam.getType())) {
              duplicate = true;
            }
          }
          if (!duplicate) {
            splitParams.add(new JParam(jsParam.getName(), transformedType));
          }
        }
        final int currentSize = params.size();
        for (int k = 1; k < splitParams.size(); k++) {
          for (int j = 0; j < currentSize; j++) {
            params.add(new ArrayList<>(params.get(j)));
          }
        }
        for (int j = 0; j < params.size();) {
          for (final JParam jParam : splitParams) {
            params.get(j).add(jParam);
            j++;
          }
        }
      } else {
        final JParam jParam =
            new JParam(jsParam.getName(), transformType(jsParam.getType()));
        for (final List<JParam> list : params) {
          list.add(jParam);
        }
      }
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
    if (type == null) {
      type = "";
    } else {
      return type;
    }
    if (jsType.getTypes().isEmpty()) {
      LOG.error("Type empty: {}", jsType);
    } else if (jsType.getTypes().size() > 1) {
      // FIXME containsUndefined
      //      final JsTypeSpec other = containsUndefined(jsType.getTypes());
      //      if (jsType.getTypes().size() == 2 && other != null) {
      //        type = TYPE_MAPPER.mapType(other.getName());
      //      } else {
      type = TypeMapper.GWT_JAVA_SCRIPT_OBJECT;
      //    }
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
      // FIXME containsUndefined
      //    } else if (jsType.getTypes().size() == 2) {
      //      final JsTypeSpec other = containsUndefined(jsType.getTypes());
      //      ts = other == null ? null : tranformSpecific(other);
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
}
