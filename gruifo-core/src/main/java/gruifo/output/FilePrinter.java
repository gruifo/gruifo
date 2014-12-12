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

import gruifo.lang.js.JsFile;

/**
 * Generic interface for outputing generated code to file.
 */
public interface FilePrinter {

  /**
   * Prints the java file to a source file. The returned string is written to
   * file.
   * @param javaFile file to print
   * @return String representation of the file.
   */
  String printFile(JsFile javaFile);
}
