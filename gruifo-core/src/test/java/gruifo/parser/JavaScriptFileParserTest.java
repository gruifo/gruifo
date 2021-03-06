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

import static org.junit.Assert.assertEquals;
import gruifo.lang.js.JsElement;
import gruifo.lang.js.JsFile;
import gruifo.lang.js.JsMethod;

import org.junit.Test;

/**
 * Test class for {@link JavaScriptFileParser}.
 */
public class JavaScriptFileParserTest {

  @Test
  public void testParseClassName() {
    final JavaScriptFileParser parser = new JavaScriptFileParser("");
    final JsFile jFile =
        parser.parseClassOrInterfaceName("this.is.a.Class", false,
            new JsElement());
    assertEquals("Package name", "this.is.a", jFile.getPackageName());
    assertEquals("Class name", "Class", jFile.getClassOrInterfaceName());
  }

  @Test
  public void testParseMethodName() {
    final JavaScriptFileParser parser = new JavaScriptFileParser("");
    final JsMethod method =
        parser.parseMethod("this.is.a.Class.prototype.getEventPixel",
            null, false);
    assertEquals("Package name", "this.is.a.Class", method.getPackageName());
    assertEquals("Package name", "getEventPixel", method.getMethodName());
  }
}
