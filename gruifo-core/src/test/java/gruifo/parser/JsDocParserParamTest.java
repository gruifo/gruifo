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
 * Test class for {@link JavaScriptDocParser} for param annotation.
 */
public class JsDocParserParamTest extends JsDocParserTestBase {

  public JsDocParserParamTest() throws IOException {
    super("jsdoc-param");
  }

  @Test
  public void testParam() {
    assertEquals("params size", 17, jsElement.getParams().size());
    assertEquals("params 0 name",
        "options", jsElement.getParams().get(0).getName());
    assertEquals("params 0 type", "nl.Options",
        jsElement.getParams().get(0).getType().getName());
    assertTrue("params 1",
        jsElement.getParams().get(1).getType().isFunction());
  }

  @Test
  public void testObjectGeneric() {
    assertEquals("params size", 2,
        jsElement.getParams().get(14).getType().getTypeList().size());
    assertEquals("params size", "string",
        jsElement.getParams().get(14).getType().getTypeList().get(0).getName());
    assertEquals("params size", "*",
        jsElement.getParams().get(14).getType().getTypeList().get(1).getName());
  }

  @Test
  public void testVarArgs() {
    assertTrue("params 1 should be var args",
        jsElement.getParams().get(8).getType().isVarArgs());
    assertEquals("Name should be without dots", "nl.Varargs",
        jsElement.getParams().get(8).getType().getName());
  }

  @Test
  public void test2LineParam() {
    assertEquals("params size next line not found", 3,
        jsElement.getParams().get(16).getType().getChoices().size());
  }

  @Test
  public void testOptional() {
    assertTrue("Type should be optional",
        jsElement.getParams().get(4).getType().isOptional());
    assertEquals("Type should be normal object", "nl.Object",
        jsElement.getParams().get(4).getType().getName());
    assertEquals("Type should be Single Object", "S",
        jsElement.getParams().get(9).getType().getName());
  }

  @Test
  public void testFunctionDetection() {
    assertTrue("params 1 should be function",
        jsElement.getParams().get(1).getType().isFunction());
    assertTrue("params 11 should be function",
        jsElement.getParams().get(11).getType().isFunction());
  }

  @Test
  public void testReturn() {
    assertEquals("return type 1", "Array.<number>",
        jsElement.getReturn().getRawType());
    assertEquals("return type 1", "number",
        jsElement.getReturn().getTypeList().get(0).getName());
  }
}
