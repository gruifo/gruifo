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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

public final class TypeMapper {
  private static final String GWT_JSNI_PACKAGE = "com.google.gwt.core.client.";
  static final String GWT_JAVA_SCRIPT_OBJECT =
      GWT_JSNI_PACKAGE + "JavaScriptObject";

  public static final TypeMapper INSTANCE = new TypeMapper();

  private final Map<String, String> mapper = new HashMap<>();
  private final Map<String, String> primitiveMapper = new HashMap<>();
  private final Map<String, String> genericMapper = new HashMap<>();
  private final Set<String> ignores = new HashSet<>();
  private final Map<String, String> replaceTypes = new HashMap<>();

  private TypeMapper() {
    primitiveMapper.put("void", "void");
    primitiveMapper.put("string", "String");
    primitiveMapper.put("int", "int");
    primitiveMapper.put("double", "double");
    primitiveMapper.put("float", "float");
    primitiveMapper.put("boolean", "boolean");
    primitiveMapper.put("number", "double");

    genericMapper.put("void", "Void");
    genericMapper.put("string", "String");
    genericMapper.put("int", "Integer");
    genericMapper.put("double", "Double");
    genericMapper.put("float", "Float");
    genericMapper.put("boolean", "Boolean");
    genericMapper.put("number", "Double");

    mapper.put("*", GWT_JAVA_SCRIPT_OBJECT);
    mapper.put("object", GWT_JAVA_SCRIPT_OBJECT);
    mapper.put("Object", GWT_JAVA_SCRIPT_OBJECT);
    mapper.put("undefined", GWT_JAVA_SCRIPT_OBJECT);
    // JSNI specific
    mapper.put("HTMLDocument", "com.google.gwt.dom.client.Node");
    mapper.put("Document", "com.google.gwt.dom.client.Document");
    mapper.put("Node", "com.google.gwt.dom.client.Node");
    mapper.put("Element", "com.google.gwt.dom.client.Element");
    mapper.put("Event", "com.google.gwt.dom.client.NativeEvent");
    mapper.put("Touch", "com.google.gwt.dom.client.Touch");

    mapper.put("Array", "com.google.gwt.core.client.JsArray");
    mapper.put("Array.<*>", "com.google.gwt.core.client.JsArray");
    mapper.put("Array.<number>", "com.google.gwt.core.client.JsArrayNumber");
    mapper.put("Array.<string>", "com.google.gwt.core.client.JsArrayString");
  }

  public void addMappings(final Properties props) {
    for (final Entry<Object, Object> prop : props.entrySet()) {
      if (((String) prop.getKey()).charAt(0) == '-') {
        ignores.add(((String) prop.getKey()).substring(1));
      } else if (((String) prop.getKey()).charAt(0) == '&') {
        replaceTypes.put(((String) prop.getKey()).substring(1),
            (String) prop.getValue());
      } else {
        mapper.put((String) prop.getKey(), (String) prop.getValue());
      }
    }
  }

  public boolean ignore(final String clazz, final String method) {
    return ignores.contains(clazz + "$" + method);
  }

  /**
   * Returns true if typeToCheck is a primitive type.
   * @param typeToCheck type to check
   * @return true if is primitive
   */
  public boolean isPrimitive(final String typeToCheck) {
    return primitiveMapper.containsKey(typeToCheck);
  }

  public String mapType(final String typeToMap, final boolean generic) {
    return mapOtherType(generic
        ? mapGenericType(typeToMap) : mapPrimitiveType(typeToMap));
  }

  /**
   * Returns the type matching the typeToMap or the value passed if it doesn't
   * map any types.
   * @param typeToMap
   * @return
   */
  public String mapType(final String typeToMap) {
    return mapOtherType(mapPrimitiveType(typeToMap));
  }

  private String mapOtherType(final String typeToMap) {
    return mapper.containsKey(typeToMap)
        ? mapper.get(typeToMap) : typeToMap;
  }

  private String mapGenericType(final String typeToMap) {
    return genericMapper.containsKey(typeToMap) ?
        genericMapper.get(typeToMap) : typeToMap;
  }

  private String mapPrimitiveType(final String typeToMap) {
    return primitiveMapper.containsKey(typeToMap)
        ? primitiveMapper.get(typeToMap) : typeToMap;
  }

  public String replaceType(final String fullClassName, final String methodName,
      final String name) {
    return replaceTypes.get(fullClassName + '$' + methodName + '$' + name);
  }

  public String replaceType(final String fullClassName, final String name) {
    return replaceTypes.get(fullClassName + '$' + name);
  }
}
