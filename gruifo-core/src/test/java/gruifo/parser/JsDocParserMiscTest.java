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
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Test;

/**
 * Test class for {@link JavaScriptDocParser} for misc annotations.
 */
public class JsDocParserMiscTest extends JsDocParserTestBase {

  public JsDocParserMiscTest() throws IOException {
    super("jsdoc-misc");
  }

  @Test
  public void testMisc() {
    assertTrue("class description", jsElement.isClassDescription());
    assertTrue("constructor", jsElement.isConstructor());
    assertTrue("protected", jsElement.isProtected());
    assertTrue("override", jsElement.isOverride());
  }

  @Test
  public void testExtends() {
    assertEquals("extends", "nl.Object", jsElement.getExtends().getName());
  }

  @Test
  public void testImplements() {
    assertEquals("implements", "nl.Object2",
        jsElement.getImplements().get(0).getName());
  }

  @Test
  public void testReturn() {
    assertEquals("return type 1", "nl.Object", jsElement.getReturn().getName());
  }
}
