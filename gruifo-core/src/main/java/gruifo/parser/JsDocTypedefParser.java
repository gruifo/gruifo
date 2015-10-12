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
import gruifo.lang.js.JsElement.JsParam;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Parses the type of a @typedef annotation.
 */
class JsDocTypedefParser {
  private static final Logger LOG = LoggerFactory
      .getLogger(JsDocTypedefParser.class);

  private static final Pattern TYPE_DEF_PATTERN = Pattern
      .compile("\\{\\{(.*)\\}\\}");
  private static final Pattern TYPE_DEF__RETURN_PATTERN = Pattern
      .compile("\\{(.*)\\}");

  private final JsTypeParser jsTypeParser;

  public JsDocTypedefParser(final JsTypeParser jsTypeParser) {
    this.jsTypeParser = jsTypeParser;
  }

  public int parseTypeDef(final JsElement doc, final String[] lines, int i,
      final String fileName) {
    if (lines[i].contains("{{")) {
      i = parseTypeClass(doc, lines, i, fileName);
    } else {
      doc.setTypeDef(null);
      doc.setExtends(jsTypeParser.parseType(
          findValues(lines[i], TYPE_DEF__RETURN_PATTERN)));
    }
    return i;
  }

  private int parseTypeClass(final JsElement doc, final String[] lines, int i,
      final String fileName) {
    final List<JsParam> fields = new ArrayList<>();
    final StringBuffer sb = new StringBuffer(stripAndReplace(lines[i]));
    for (; !lines[i].contains("}}"); i++) {
      sb.append(stripAndReplace(lines[i + 1]));
    }
    final String values = findValues(sb.toString(), TYPE_DEF_PATTERN);
    if (values.isEmpty()) {
      LOG.error("Missing typedef pattern, {} in file {}", sb.toString().trim(),
          fileName);
    } else {
      parseValues(values, fields);
    }
    doc.setTypeDef(fields);
    return i;
  }

  private String findValues(final String string, final Pattern pattern) {
    final Matcher tdp = pattern.matcher(string);
    return tdp.find() ? tdp.group(1) : "";
  }

  private void parseValues(final String values, final List<JsParam> fields) {
    String var = "";
    boolean varFound = false;
    int offset = 0;
    int functionDepth = 0;
    for (int i = 0; i < values.length(); i++) {
      if (values.charAt(i) == ':' && functionDepth == 0 &&
          values.charAt(i - 1) != ')') { // check if not return type of method
        var = values.substring(offset, i);
        offset = i + 1;
        varFound = true;
      } else if (values.charAt(i) == '('
          || values.charAt(i) == '<') {
        functionDepth++;
      } else if (values.charAt(i) == ')' || values.charAt(i) == '>') {
        functionDepth--;
      }
      if (varFound && functionDepth == 0
          && (values.charAt(i) == ',' || i == (values.length() - 1))) {
        addField(fields, var,
            values.substring(offset, i + (values.charAt(i) == ',' ? 0 : 1)));
        offset = i + 1;
        varFound = false;
      }
    }
  }

  private void addField(final List<JsParam> fields, final String var,
      final String type) {
    final JsParam field = new JsParam();
    field.setName(var);
    field.setType(jsTypeParser.parseType((type)));
    fields.add(field);
  }

  /**
   * Replace comment token '*' at beginning of line and remove all spaces.
   *
   * @param string
   * @return
   */
  private String stripAndReplace(final String string) {
    return string.replaceFirst("^ +\\*", "").replace(" ", "");
  }
}
