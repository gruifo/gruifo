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
package gengwtjs.parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import gengwtjs.lang.js.JsElement;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.Before;
import org.junit.Test;

/**
 * Test class for {@link JavaScriptDocParser}.
 */
public class JavaScriptDocParserTest {

  private static final String COMMENT1 = "comment1";
  private JsElement docs;

  @Before
  public void parseFile() throws IOException {
    final URL commentFile =
        JavaScriptDocParserTest.class.getResource(COMMENT1 + ".txt");
    final FileReader fr = new FileReader(commentFile.getFile());
    final Path file = new File(commentFile.getPath()).toPath();
    final String comment1 =
        new String(Files.readAllBytes(file));
    final JavaScriptDocParser parser = new JavaScriptDocParser();
    docs = parser.parse(file.toString(), comment1);
  }

  @Test
  public void testParser() {
    assertTrue("class description", docs.isClassDescription());
    assertTrue("constructor", docs.isConstructor());
    assertTrue("protected", docs.isProtected());
  }

  @Test
  public void testExtends() {
    assertEquals("extends", "nl.Object",
        docs.getExtends().getTypes().get(0).getName());
  }

  @Test
  public void testParam() {
    assertEquals("params size", 12, docs.getParams().size());
    assertEquals("params 0 name",
        "options", docs.getParams().get(0).getName());
    assertEquals("params 0 type",
        "nl.Options", docs.getParams().get(0).getTypes().getTypes().get(0).getName());
    assertTrue("params 1",
        docs.getParams().get(1).getTypes().isFunction());
  }

  @Test
  public void testReturn() {
    assertEquals("return type 1", "nl.Object",
        docs.getReturn().getTypes().get(0).getName());
    assertEquals("return type 2", "undefined",
        docs.getReturn().getTypes().get(1).getName());
  }

}
