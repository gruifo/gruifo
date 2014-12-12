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
import gruifo.lang.js.JsElement;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

/**
 * Test class for {@link JavaScriptDocParser}.
 */
public class JavaScriptDocParserTest {

  private static final String COMMENT = "comment";
  private final JsElement[] docs = new JsElement[4];

  @Before
  public void parseFiles() throws IOException {
    for (int i = 0; i < docs.length; i++) {
      docs[i] = parseFile(COMMENT + i);
    }
  }

  private JsElement parseFile(final String comment) throws IOException {
    final URL commentFile =
        JavaScriptDocParserTest.class.getResource(comment + ".txt");
    final Path file = new File(commentFile.getPath()).toPath();
    final String comment1 = new String(Files.readAllBytes(file));
    final JavaScriptDocParser parser = new JavaScriptDocParser();
    return parser.parse(file.toString(), comment1);
  }

  @Test
  public void testParser() {
    assertTrue("class description", docs[0].isClassDescription());
    assertTrue("constructor", docs[0].isConstructor());
    assertTrue("protected", docs[0].isProtected());
  }

  @Test
  public void testExtends() {
    assertEquals("extends", "nl.Object",
        docs[0].getExtends().getTypes().get(0).getName());
  }

  @Test
  public void testParam() {
    assertEquals("params size", 12, docs[0].getParams().size());
    assertEquals("params 0 name",
        "options", docs[0].getParams().get(0).getName());
    assertEquals("params 0 type",
        "nl.Options", docs[0].getParams().get(0).getType().getTypes().get(0).getName());
    assertTrue("params 1",
        docs[0].getParams().get(1).getType().isFunction());
  }

  @Test
  public void testReturn() {
    assertEquals("return type 1", "nl.Object",
        docs[0].getReturn().getTypes().get(0).getName());
    assertEquals("return type 2", "undefined",
        docs[0].getReturn().getTypes().get(1).getName());
  }

  @Test
  public void testTypeDef() {
    assertEquals("Size of typedef fields", 5, ((List) docs[1].getTypeDef()).size());
  }

  @Test
  public void testSingleLineTypeDef() {
    assertEquals("Size of typedef fields", 3, ((List) docs[3].getTypeDef()).size());
  }
}
