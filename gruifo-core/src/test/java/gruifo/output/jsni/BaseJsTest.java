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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import gruifo.GruifoCli;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.tools.JavaCompiler;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import org.apache.commons.cli.ParseException;


public abstract class BaseJsTest {

  private final String jsFileName;
  private final String targetPath;
  private final boolean[] compileResult;

  public BaseJsTest(final String jsFileName, final String... javaFiles)
      throws IOException, ParseException {
    this.jsFileName = jsFileName;
    targetPath = getClass().getProtectionDomain()
        .getCodeSource().getLocation().getFile();
    parse();
    compileResult = new boolean[javaFiles.length];
    for (int i = 0; i < javaFiles.length; i++) {
      compileResult[i] = compile(javaFiles[i]);
    }
  }

  private void parse() throws IOException, ParseException {
    final String typeMappingFile =
        getClass().getResource("type_mapper.properties").getPath();
    final String src = getClass().getResource(".").getFile()
        + "../../parser/"+ jsFileName + ".js";
    GruifoCli.main(new String[] {"-src", src, "-target", targetPath,
        "-type_mapping", typeMappingFile, });
  }

  protected void assertJavaFileExists(final String file) {
    final File targetFile = getJavaSourceFile(file);
    assertTrue("targetFile doesn't exist:" + targetFile, targetFile.exists());
  }

  private boolean compile(final String file) {
    final File fileToCompile = getJavaSourceFile(file);
    final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
    assertNotNull("No compiler installed", compiler);
    final List<String> options = new ArrayList<>();
    options.add("-source");
    options.add("1.6");
    options.add("-Xlint:-options");
    options.add("-classpath");
    options.add(getClass().getProtectionDomain()
        .getCodeSource().getLocation().getFile());
    final StandardJavaFileManager fileManager =
        compiler.getStandardFileManager(null,null,null);
    final JavaCompiler.CompilationTask task = compiler.getTask(
        /*default System.err*/ null,
        /*std file manager*/ null,
        /*std DiagnosticListener */  null,
        /*compiler options*/ options,
        /*no annotation*/  null,
        fileManager.getJavaFileObjects(fileToCompile));
    return task.call();
  }

  protected void assertCompile(final String... javaFiles) throws IOException {
    for (int i = 0; i < javaFiles.length; i++) {
      assertTrue("Compilation Failed for:" + javaFiles[i], compileResult[i]);
    }
  }

  private File getJavaSourceFile(final String file) {
    return new File(targetPath, file + ".java");
  }
}
