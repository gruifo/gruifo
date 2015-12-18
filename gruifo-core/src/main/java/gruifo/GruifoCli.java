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
package gruifo;

import gruifo.output.FilePrinter;
import gruifo.output.jsinterop.JsInteropPrinter;
import gruifo.output.jsni.JSNIPrinter;
import gruifo.output.jsni.TypeMapper;

import java.io.IOException;

import org.apache.commons.cli.ParseException;

/**
 * Main class.
 */
public final class GruifoCli {

  private GruifoCli() {
    // main class, only static access.
  }

  public static void main(final String[] args)
      throws IOException, ParseException {
    final CmdOptions cmdOptions = new CmdOptions(args);
    if (cmdOptions.printIfInfoOption()) {
      return;
    }
    final FilePrinter fp = detectFilePrinter(cmdOptions);
    TypeMapper.INSTANCE.addMappings(cmdOptions.getTypeMappingProperties());

    final Controller controller =
        new Controller(cmdOptions.getSourcePaths(), cmdOptions.getTargetDir());
    controller.run(fp);
  }

  private static FilePrinter detectFilePrinter(final CmdOptions cmdOptions) {
    final FilePrinter fp;
    if (cmdOptions.isJSInterop()) {
      fp = new JsInteropPrinter();
    } else {
      fp = new JSNIPrinter();
    }
    return fp;
  }
}