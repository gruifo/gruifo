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
package gruifo.output.jsni;

import static org.junit.Assert.assertTrue;
import gruifo.GruifoCli;

import java.io.File;
import java.io.IOException;

import org.apache.commons.cli.ParseException;


public abstract class BaseJsTest {

  private final String jsFileName;
  private final String targetPath;

  public BaseJsTest(final String jsFileName) throws IOException, ParseException {
    this.jsFileName = jsFileName;
    targetPath = getClass().getProtectionDomain()
        .getCodeSource().getLocation().getFile();
    parse();
  }

  private void parse() throws IOException, ParseException {
    final String typeMappingFile =
        getClass().getResource("type_mapper.properties").getPath();
    final String src = getClass().getResource(".").getFile()
        + "../../parser/"+ jsFileName + ".js";
    GruifoCli.main(new String[] {"-src", src, "-target", targetPath,
        "-type_mapping", typeMappingFile, });
  }

  public final void assertJavaFileExists(final String file) {
    final File targetFile = new File(targetPath, file + ".java");
    assertTrue("targetFile doesn't exist:" + targetFile, targetFile.exists());
  }
}
