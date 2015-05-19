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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Parse the types of @param, @return and @type elements.
 */
public class JsTypeParser {

  private static final String FUNCTION = "function(";

  public JsType parseType(final String rawType) {
    return typeParser(stripParentheses(rawType));
  }

  private String stripParentheses(final String type) {
    String strippedType;
    if (type.isEmpty()) {
      strippedType = type;
    } else {
      final int beginIndex = type.charAt(0) == '(' ? 1 : 0;
      final int length = type.length();
      final int endIndex = length - (type.charAt(length - 1) == ')' ? 1 : 0);
      strippedType = type.substring(beginIndex, endIndex);
    }
    return strippedType;
  }

  private JsType typeParser(final String rawType) {
    final char[] chars = rawType.toCharArray();
    final JsType root;
    final List<JsType> types = typeParser(rawType, chars, new AtomicInteger());
    if (types.size() == 1) {
      root = types.get(0);
    } else {
      root = new JsType(rawType);
      root.addChoices(types);
    }
    return root;
  }

  private List<JsType> typeParser(final String rawType, final char[] chars,
      final AtomicInteger idx) {
    int startPos = idx.get();
    int startPosChoices = startPos;
    int nameEndPos = startPos;
    int endPos;
    boolean notNull = false, canNull = false, optional = false, varArgs = false,
        newType = false, decreaseDepth = false, inFunction = false,
        endFunction = false, param = false;
    final List<JsType> types = new ArrayList<>();
    final List<JsType> choices = new ArrayList<>();
    List<JsType> subTypes = null;
    for (; idx.get() < chars.length; idx.incrementAndGet()) {
      final int i = idx.get();
      endPos = i;
      switch (chars[i]) {
      case 'f':
        if (rawType.substring(i).startsWith(FUNCTION)) {
          inFunction = true;
        }
        break;
      case ')':
        endFunction = true;
        break;
      case ' ':
        if (startPos == endPos) {
          startPos++;
        } else {
          newType = true;
          endPos--;
        }
        break;
      case '.': // generic or varargs .... varargs before
        if (chars[i+1] == '<') {
          endPos--;
          nameEndPos = endPos;
          idx.incrementAndGet(); // skip past '<'
          idx.incrementAndGet();
          subTypes = typeParser(rawType, chars, idx);
          endPos = idx.get();
        } else if (chars[i+1] == '.' && (chars[i+2] == '.')) {
          varArgs = true;
          idx.addAndGet(2); // skip ...
          startPos = i + 3;
        }
        break;
      case ',':
        // set inFunction to false  when we passed the end of the function,
        // But ONLY when inFunction is already true
        inFunction = inFunction && !endFunction;
        param = true;
        newType = true;
        endPos--;
        break;
      case '<':
        // should not happen...
        break;
      case '>':
        decreaseDepth = true;
        newType = true;
        endPos--;
        break;
      case '|': // new choice argument
        newType = true;
        endPos--;
        break;
      case '!': // argument can't be null. !  is positioned before type
        notNull = true;
        startPos++;
        break;
      case '?': // argument can be null. ? is positioned before type
        if (startPos == endPos) {
          canNull = true;
          startPos++;
        }
        break;
      case '=': // optional argument. = is positioned after type
        optional = true;
        endPos--;
        // is last so we can finish type
        break;
      default:
        break;
      }
      if (subTypes == null) {
        nameEndPos = endPos;
      }
      boolean lastToken = idx.get() == chars.length - 1;
      if ((!inFunction && newType) || lastToken) {
        final String sType = rawType.substring(startPos, endPos + 1);
        final String name = rawType.substring(startPos, nameEndPos + 1);
        boolean withNull = false;
        if ("undefined".equals(sType) || "null".equals(sType)) {
          withNull = true;
        } else {
          final JsType jsType = new JsType(name, sType);
          jsType.setFunction(sType.startsWith(FUNCTION));
          jsType.setVarArgs(varArgs);
          jsType.setNotNull(notNull);
          jsType.setNull(canNull);
          jsType.setOptional(optional);
          jsType.addSubTypes(subTypes);
          choices.add(jsType);
        }
        lastToken = idx.get() == chars.length - 1;
        if (param || decreaseDepth || lastToken) {
          if (choices.size() == 1) {
            choices.get(0).setNull(withNull);
            types.add(choices.get(0));
          } else {
            final JsType choicesType =
                new JsType(rawType.substring(startPosChoices, endPos + 1));
            choicesType.setNull(withNull);
            choicesType.addChoices(choices);
            types.add(choicesType);
          }
          choices.clear();
          if (decreaseDepth || lastToken) {
            return types;
          }
          if (param) {
            startPosChoices = i + 1;
          }
        }
        startPos = idx.get() + 1;
        newType = false;
        param = false;
        varArgs = false;
        notNull = false;
        canNull = false;
        optional = false;
        inFunction = false;
        endFunction = false;
        decreaseDepth = false;
        subTypes = null;
      }
    }
    return types;
  }
}
