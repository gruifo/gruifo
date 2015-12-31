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
package gruifo.output;

/**
 * Util class to add new lines and indentation and other useful methods for
 * printing.
 */
public final class PrintUtil {

  private static final String NL_S = "\n";
  private static final char NL = '\n';

  private PrintUtil() {
    // util
  }

  /**
   * Appends the given (multi-line) text to the buffer with the given indent
   * count. The text is split by new lines. So each line is appended with the
   * given indent.
   * @param buffer buffer to append
   * @param text multi-line text to append
   * @param indent number of times to append the 2 spaces indentation
   */
  public static void indent(final StringBuffer buffer, final String text,
      final int indent) {
    if (text != null) {
      for (final String row : text.split(NL_S)) {
        indent(buffer, indent);
        buffer.append(row);
        nl(buffer);
      };
    }
  }

  /**
   * Append a new line followed by an indentation to the buffer.
   * @param buffer buffer to append
   * @param indent number of times to append the 2 spaces indentation
   */
  public static void nlIndent(final StringBuffer buffer, final int indent) {
    nl(buffer);
    indent(buffer, indent);
  }

  /**
   * Appends times the indent 2 spaces indentation to the buffer.
   * @param buffer buffer to append
   * @param indent number of times to append the 2 spaces indentation
   */
  public static void indent(final StringBuffer buffer, final int indent) {
    buffer.append(new String(new char[indent * 2]).replace('\0', ' '));
  }

  /**
   * Appends a new line to the buffer.
   * @param buffer buffer to append
   */
  public static void nl(final StringBuffer buffer) {
    buffer.append(NL);
  }

  /**
   * Appends 2 new lines to the buffer.
   * @param buffer buffer to append
   */
  public static void nl2(final StringBuffer buffer) {
    nl(buffer, 2);
  }

  /**
   * Appends the times number count a new line to the buffer.
   * @param buffer buffer to append
   * @param count number of times to append new line
   */
  public static void nl(final StringBuffer buffer, final int count) {
    buffer.append(new String(new char[count]).replace('\0', NL));
  }

  /**
   * Returns the string with the first char of the string in upper case.
   * @param string to change
   * @return string with first char in upper case
   */
  public static String firstCharUpper(final String string) {
    return string == null ? null :
      String.valueOf(string.charAt(0)).toUpperCase() + string.substring(1);
  }

  /**
   * Returns the string with the first char of the string in lower case.
   * @param string to change
   * @return string with first char in lower case
   */
  public static String firstCharLower(final String string) {
    return string == null ? null :
      String.valueOf(string.charAt(0)).toLowerCase() + string.substring(1);
  }
}
