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
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

public final class TypeMapper {
  static final String GWT_JAVA_SCRIPT_OBJECT = "JavaScriptObject";

  public static final TypeMapper INSTANCE = new TypeMapper();

  private final Map<String, String> mapper = new HashMap<>();
  private final Map<String, String> nativeMapper = new HashMap<>();
  private final Map<String, String> genericMapper = new HashMap<>();

  private TypeMapper() {
    nativeMapper.put("void", "void");
    nativeMapper.put("string", "String");
    nativeMapper.put("int", "int");
    nativeMapper.put("double", "double");
    nativeMapper.put("float", "float");
    nativeMapper.put("boolean", "boolean");
    nativeMapper.put("number", "double");

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
    mapper.put("Document", "com.google.gwt.dom.client.Document");
    mapper.put("Node", "com.google.gwt.dom.client.Node");
    mapper.put("Element", "com.google.gwt.dom.client.Element");
    mapper.put("Event", "com.google.gwt.dom.client.NativeEvent");

    mapper.put("Array", "com.google.gwt.core.client.JsArray");
    mapper.put("Array.<*>", "com.google.gwt.core.client.JsArray");
    mapper.put("Array.<number>", "com.google.gwt.core.client.JsArrayNumber");
    mapper.put("Array.<string>", "com.google.gwt.core.client.JsArrayString");
  }

  public void addMappings(final Properties props) {
    for (final Entry<Object, Object> prop : props.entrySet()) {
      mapper.put((String) prop.getKey(), (String) prop.getValue());
    }
  }

  public String mapType(final String typeToMap, final boolean generic) {
    return mapOtherType(generic
        ? mapGenericType(typeToMap) : mapNativeType(typeToMap));
  }

  public String mapType(final String typeToMap) {
    return mapOtherType(mapNativeType(typeToMap));
  }

  private String mapOtherType(final String typeToMap) {
    return mapper.containsKey(typeToMap)
        ? mapper.get(typeToMap) : typeToMap;
  }

  private String mapGenericType(final String typeToMap) {
    return genericMapper.containsKey(typeToMap) ?
        genericMapper.get(typeToMap) : typeToMap;
  }

  private String mapNativeType(final String typeToMap) {
    return nativeMapper.containsKey(typeToMap)
        ? nativeMapper.get(typeToMap) : typeToMap;
  }
}
