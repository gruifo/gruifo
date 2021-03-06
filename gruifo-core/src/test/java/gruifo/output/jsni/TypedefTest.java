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

import java.io.IOException;

import org.apache.commons.cli.ParseException;
import org.junit.Test;

public class TypedefTest extends BaseJsTest {

  private static final String JAVA_CLASS = "nl/test/SomeTypedef";

  public TypedefTest() throws IOException, ParseException {
    super("test_typedef", JAVA_CLASS);
  }

  @Test
  public void testGeneratedFile() {
    assertJavaFileExists(JAVA_CLASS);
  }

  @Test
  public void testCompile() throws IOException {
    assertCompile(JAVA_CLASS);
  }
}
