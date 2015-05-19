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

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

public class JsDocParserTestBase {

  protected JsElement jsElement;

  protected JsDocParserTestBase(final String filename) throws IOException {
    final URL fileUrl =
        JsDocParserTestBase.class.getResource(filename + ".txt");
    final Path file = new File(fileUrl.getPath()).toPath();
    final String fileContent = new String(Files.readAllBytes(file));
    final JavaScriptDocParser parser = new JavaScriptDocParser();
    jsElement = parser.parse(file.toString(), fileContent);
  }

}
