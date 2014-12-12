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

import gruifo.lang.js.JsType;
import gruifo.lang.js.JsType.JsTypeSpec;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parse the types of @param, @return and @type elements.
 */
public class JsTypeParser {

  private static final Pattern GENERIC_TYPE_PATTERN =
      Pattern.compile("(.+?)\\.<(.+)>");

  public JsType parseType(final String rawType) {
    final JsType jsType = new JsType(rawType);
    final String pType =
        parseVarArgs(jsType, parseNull(jsType, parseOptional(jsType, rawType)));
    final String[] pTypes = pType.split("\\|");
    for (final String sType : pTypes) {
      if (!parseAsFunction(jsType, sType))
        jsType.addType(parseAsGenericType(sType));
    }
    return jsType;
  }

  String parseNull(final JsType jsType, final String type) {
    final String rType;
    if (type.startsWith("!")) {
      jsType.setNotNull();
      rType = type.substring(1);
    } else if (type.startsWith("?")) {
      jsType.setNull();
      rType = type.substring(1);
    } else {
      rType = type;
    }
    return rType;
  }

  String parseOptional(final JsType jsType, final String type) {
    final String rType;
    if (type.endsWith("=")) {
      jsType.setOptional();
      rType = type.substring(0, type.length() - 1);
    } else {
      rType = type;
    }
    return rType;
  }

  String parseVarArgs(final JsType jsType, final String type) {
    final String rType;
    if (type.startsWith("...")) {
      jsType.setVarArgs();
      rType = type.substring(3);
    } else {
      rType = type;
    }
    return rType;
  }

  /**
   * Checks if type is function and if so mark type as function.
   * @param jsType type object
   * @param type type to parse
   * @return true if type is function
   */
  private boolean parseAsFunction(final JsType jsType, final String type) {
    final boolean function;
    if (type.contains("{") || type.contains("function(")) {
      jsType.setFunction();
      function = true;
    } else {
      function = false;
    }
    return function;
  }

  JsTypeSpec parseAsGenericType(final String type) {
    final Matcher matcher2 = GENERIC_TYPE_PATTERN.matcher(type);
    final JsTypeSpec ts;
    if (matcher2.find()) {
      ts = new JsTypeSpec(matcher2.group(1));
      final List<String> generics = parseGenericArgs(matcher2.group(2));
      for (final String arg: generics) {
        ts.addGeneric(parseAsGenericType(arg));
      }
    } else {
      ts = new JsTypeSpec(type);
    }
    return ts;
  }

  private List<String> parseGenericArgs(final String args) {
    final char[] chars = args.toCharArray();
    final List<String> gArgs = new ArrayList<>();
    int depth = 0;
    int spos = 0;
    int i = 0;
    for (; i < chars.length; i++) {
      if (chars[i] == '<') {
        depth++;
      } else if (chars[i] == '>') {
        depth--;
      } else if (chars[i] == ',' && depth == 0) {
        gArgs.add(args.substring(spos, i).trim());
        spos = i;
      }
    }
    if (spos != i) {
      gArgs.add(args.substring(spos, i).trim());
    }
    return gArgs;
  }
}
