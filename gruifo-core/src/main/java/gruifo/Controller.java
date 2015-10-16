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

import gruifo.lang.js.JsElement;
import gruifo.lang.js.JsFile;
import gruifo.lang.js.JsMethod;
import gruifo.lang.js.JsParam;
import gruifo.output.FilePrinter;
import gruifo.output.jsinterface.JsInterfacePrinter;
import gruifo.output.jsni.JSNIPrinter;
import gruifo.parser.JavaScriptFileParser;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mozilla.javascript.CompilerEnvirons;
import org.mozilla.javascript.Parser;
import org.mozilla.javascript.ast.AstRoot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Control flow from parsing to generating output files.
 */
public class Controller {

  private static final String JAVA_SCRIPT_EXT = "js";
  private static final String JAVA_EXT = ".java";

  private static final Logger LOG = LoggerFactory.getLogger(Controller.class);

  private final List<File> srcPaths;
  private final File outputPath;

  public Controller(final List<File> srcPaths, final File outputPath) {
    this.srcPaths = srcPaths;
    this.outputPath = outputPath;
  }

  public void run(final OutputType outputType) {
    final FilePrinter fp;
    if (outputType == OutputType.JSI) {
      fp = new JsInterfacePrinter();
    } else if (outputType == OutputType.JSNI) {
      fp = new JSNIPrinter();
    } else {
      throw new RuntimeException("Output type '" + outputType
          + "' not supported");
    }
    run(fp);
  }

  public void run(final FilePrinter printer) {
    final List<JsFile> jsFiles = new ArrayList<>();
    final List<JsMethod> staticMethods = new ArrayList<>();
    final Map<String, JsElement> staticConsts = new HashMap<>();

    for (final File srcPath : srcPaths) {
      final List<File> files = new ArrayList<>();
      scanJsFiles(files, srcPath);
      for (final File file : files) {
        try {
          jsFiles.addAll(prepareFiles(
              parseFile(file.getPath(), staticMethods, staticConsts)));
        } catch (final IOException e) {
          LOG.error("Exception parsing file:" + file, e);
        }
      }
    }
    processStaticConsts(staticConsts);
    processStaticMethods(staticMethods);
    writeFiles(printer, jsFiles, outputPath);
  }

  /**
   * Scans the srcPath for JavaScript files and adds them to the files list.
   *
   * @param files list of JavaScript files found
   * @param srcPath source path to search for files
   */
  void scanJsFiles(final List<File> files, final File srcPath) {
    if (srcPath.isFile()) {
      files.add(srcPath);
    } else {
      for (final File file : srcPath.listFiles()) {
        if (file.isDirectory()) {
          scanJsFiles(files, file);
        } else if (file.getPath().endsWith(JAVA_SCRIPT_EXT)) {
          files.add(file);
        }
      }
    }
  }

  /**
   * 
   * @param fileName
   * @param staticMethods
   * @param staticFields
   * @return
   * @throws FileNotFoundException
   * @throws IOException
   */
  Collection<JsFile> parseFile(final String fileName,
      final List<JsMethod> staticMethods,
      final Map<String, JsElement> staticFields)
          throws FileNotFoundException, IOException {
    try (final Reader reader = new FileReader(fileName)) {
      final CompilerEnvirons env = new CompilerEnvirons();
      env.setRecordingLocalJsDocComments(true);
      env.setAllowSharpComments(true);
      env.setRecordingComments(true);
      final AstRoot node = new Parser(env).parse(reader, fileName, 1);
      final JavaScriptFileParser parser = new JavaScriptFileParser(fileName);
      node.visitAll(parser);
      staticMethods.addAll(parser.getStaticMethods());
      staticFields.putAll(parser.getConsts());
      return parser.getFiles();
    }
  }

  /**
   * 
   * @param files
   * @return
   */
  Collection<JsFile> prepareFiles(final Collection<JsFile> files) {
    return groupFiles(prepareFields(files));
  }

  /**
   * Remove any fields specified in @typedef if the field is also specified as
   * prototype field in the JavaScript file.
   *
   * @param files JavaScript parsed files
   * @return same list of JavaScript parsed files
   */
  private Collection<JsFile> prepareFields(final Collection<JsFile> files) {
    for (final JsFile jsFile : files) {
      if (jsFile.getElement().isTypeDef()) {
        final List<JsParam> typeDefs = jsFile.getElement().getTypeDef();
        for (final JsParam field : jsFile.getFields()) {
          for (int i = 0; i < typeDefs.size(); i++) {
            if (field.getName().equals(typeDefs.get(i).getName())) {
              typeDefs.remove(i);
              break;
            }
          }
        }
      }
    }
    return files;
  }

  /**
   * 
   * @param files
   * @return
   */
  Collection<JsFile> groupFiles(final Collection<JsFile> files) {
    final Map<String, JsFile> filesMap = new HashMap<>();
    for (final JsFile jsFile : files) {
      filesMap.put(jsFile.getClassOrInterfaceName(), jsFile);
    }
    final Collection<JsFile> groupedFiles = new ArrayList<>();
    for (final JsFile jsFile : files) {
      final String[] split = jsFile.getPackageName().split("\\.");
      if (split.length > 0 && filesMap.containsKey(split[split.length - 1])) {
        final JsFile jsFile2 = filesMap.get(split[split.length - 1]);
        jsFile2.addInnerJsFile(jsFile);
      } else {
        groupedFiles.add(jsFile);
      }
    }
    return groupedFiles;
  }

  private void processStaticConsts(final Map<String, JsElement> staticConsts) {
    LOG.error("Missed #{} static fields.", staticConsts.entrySet().size());
  }
  private void processStaticMethods(final List<JsMethod> staticMethods) {
    LOG.error("Missed #{} static methods.", staticMethods.size());
  }

  void writeFiles(final FilePrinter printer, final Collection<JsFile> jsFiles,
      final File outputPath) {
    for (final JsFile jsFile : jsFiles) {
      if (!printer.ignored(jsFile)) {
        final String packagePath = jsFile.getPackageName().replace('.', '/');
        final File path = new File(outputPath, packagePath);
        path.mkdirs();
        try {
          try (final FileWriter writer = new FileWriter(new File(path,
              jsFile.getClassOrInterfaceName() + JAVA_EXT))) {
            writer.append(printer.printFile(jsFile));
            writer.flush();
          }
        } catch (final IOException e) {
          LOG.error("Exception parsing file:" + jsFile.getOriginalFileName(), e);
        }
      }
    }
  }
}
