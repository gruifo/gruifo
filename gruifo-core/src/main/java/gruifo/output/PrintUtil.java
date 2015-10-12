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

public final class PrintUtil {

  private static final String NL_S = "\n";
  private static final char NL = '\n';

  private PrintUtil() {
    // util
  }

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

  public static void nlIndent(final StringBuffer buffer, final int indent) {
    nl(buffer);
    indent(buffer, indent);
  }

  public static void indent(final StringBuffer buffer, final int indent) {
    buffer.append(new String(new char[indent * 2]).replace('\0', ' '));
  }

  public static void nl(final StringBuffer buffer) {
    buffer.append(NL);
  }

  public static void nl2(final StringBuffer buffer) {
    nl(buffer, 2);
  }

  public static void nl(final StringBuffer buffer, final int times) {
    buffer.append(new String(new char[times]).replace('\0', NL));
  }

  public static String firstCharUpper(final String string) {
    return string == null ? null :
      String.valueOf(string.charAt(0)).toUpperCase() + string.substring(1);
  }
}
