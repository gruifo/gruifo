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

import java.io.IOException;

import org.junit.Test;

/**
 * Test class for {@link JavaScriptDocParser} for typedef annotation with single
 * type.
 */
public class JsDocParserTypeDef2Test extends JsDocParserTestBase {

  public JsDocParserTypeDef2Test() throws IOException {
    super("jsdoc-typedef2");
  }

  @Test
  public void testExtends() {
    assertEquals("extends", "Array", jsElement.getExtends().getName());
    assertEquals("extends", "number",
        jsElement.getExtends().getTypeList().get(0).getName());
  }
}
