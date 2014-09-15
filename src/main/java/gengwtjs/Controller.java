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
package gengwtjs;

import gengwtjs.lang.js.JsFile;
import gengwtjs.output.FilePrinter;
import gengwtjs.parser.JavaScriptFileParser;

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
class Controller {

  private static final String JAVA_SCRIPT_EXT = "js";
  private static final String JAVA_EXT = ".java";

  private static final Logger LOG = LoggerFactory.getLogger(Controller.class);

  private List<String> srcPaths;
  private String outputPath;

  public Controller() {
  }

  public Controller(final List<String> srcPaths, final String outputPath) {
    this.srcPaths = srcPaths;
    this.outputPath = outputPath;
  }

  void run(final FilePrinter printer) {
    final List<File> files = new ArrayList<>();
    for (final String srcPath : srcPaths) {
      scanJsFiles(files, srcPath);
    }
    for (final File file : files) {
      final Collection<JsFile> jsFiles;
      try {
        jsFiles = groupFiles(parseFile(file.getPath()));
        writeFiles(printer, jsFiles, outputPath);
      } catch (final IOException e) {
        LOG.error("Exception parsing file:" + file, e);
      }
    }
  }

  void scanJsFiles(final List<File> files, final String srcPath) {
    final File folder = new File(srcPath);
    for (final File file : folder.listFiles()) {
      if (file.isDirectory()) {
        scanJsFiles(files, file.getPath());
      } else if (file.getPath().endsWith(JAVA_SCRIPT_EXT)) {
        files.add(file);
      }
    }
  }

  Collection<JsFile> parseFile(final String file)
      throws FileNotFoundException, IOException {
    try (final Reader reader = new FileReader(file)) {
      final CompilerEnvirons env = new CompilerEnvirons();
      env.setRecordingLocalJsDocComments(true);
      env.setAllowSharpComments(true);
      env.setRecordingComments(true);
      final AstRoot node = new Parser(env).parse(reader, file, 1);
      final JavaScriptFileParser parser = new JavaScriptFileParser(file);
      node.visitAll(parser);
      return parser.getFiles();
    }
  }

  Collection<JsFile> groupFiles(final Collection<JsFile> files) {
    final Map<String, JsFile> filesMap = new HashMap<>();
    for (final JsFile jsFile : files) {
      filesMap.put(jsFile.getClassOrInterfaceName(), jsFile);
    }
    final Collection<JsFile> groupedFiles = new ArrayList<>();
    for (final JsFile jsFile : files) {
      final String[] split = jsFile.getPackageName().split("\\.");
      if (split.length > 0 && filesMap.containsKey(split[split.length - 1])) {
        filesMap.get(split[split.length - 1]).addSubJsFile(jsFile);
        //      } else if (split.length > 1 && filesMap.containsKey(split[split.length - 2])
        //          && "prototype".equals(split[split.length - 1])) {
        //        filesMap.get(split[split.length - 2]).addSubJFile(javaFile);
      } else {
        groupedFiles.add(jsFile);
      }
    }
    return groupedFiles;
  }

  void writeFiles(final FilePrinter printer, final Collection<JsFile> jsFiles,
      final String outputPath) throws IOException {
    for (final JsFile javaFile : jsFiles) {
      final String packagePath = javaFile.getPackageName().replace('.', '/');
      final File path = new File(outputPath + '/' + packagePath);
      path.mkdirs();
      try (final FileWriter writer = new FileWriter(
          new File(path, javaFile.getClassOrInterfaceName() + JAVA_EXT))) {
        writer.append(printer.printFile(javaFile));
        writer.flush();
      }
    }
  }
}
