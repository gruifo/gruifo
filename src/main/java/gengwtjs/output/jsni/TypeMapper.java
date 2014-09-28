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

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

public final class TypeMapper {
  static final String GWT_JAVA_SCRIPT_OBJECT = "JavaScriptObject";

  public static final TypeMapper INSTANCE = new TypeMapper();

  private final Map<String, String> mapper = new HashMap<>();
  //  private static final List<String> NATIVE_TYPES = Arrays.asList(new String[] {
  //      "void", "int", "long", "double", "float", "boolean", "string", "number",
  //      "*", "object" });

  private TypeMapper() {
    mapper.put("void", "void");
    mapper.put("string", "String");
    mapper.put("int", "int");
    mapper.put("double", "double");
    mapper.put("float", "float");
    mapper.put("boolean", "boolean");
    mapper.put("number", "double");
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

  public String mapType(final String typeToMap) {
    return mapper.containsKey(typeToMap) ? mapper.get(typeToMap) : typeToMap;
  }
}
