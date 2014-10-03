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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 * Command line options for the tool.
 */
class CmdOptions {

  private static final String HELP = "help";
  private static final String JSNI = "JSNI";
  private static final String JSINTERFACE = "JSInterface";
  private static final String SRC_PATH = "src";
  private static final String SRC_PATH_ARG = "directory;...";
  private static final String TARGET_PATH = "target";
  private static final String TARGET_PATH_ARG = "directory";
  private static final String TYPE_MAPPING = "type_mapping";
  private static final String TYPE_MAPPING_ARG = "file";


  private static final Option HELP_OPTION =
      new Option(HELP, "print this message");
  private static final Option JSNI_OPTION =
      new Option(JSNI, "generate JSNI files");
  private static final Option JSINTERFACE_OPTION =
      new Option(JSINTERFACE, "generate JSInterface files");
  private static final Option SRC_PATH_OPTION =
      new Option(SRC_PATH, true, "one or more ; separated source directories");
  private static final Option TARGET_PATH_OPTION =
      new Option(TARGET_PATH, true, "output directory");
  private static final Option TYPE_MAPPING_OPTION =
      new Option(TYPE_MAPPING, true, "properties file with type mapping");

  private final Options options;
  private final CommandLine cmd;

  public CmdOptions(final String[] args) throws ParseException {
    options = new Options();
    options.addOption(HELP_OPTION);
    options.addOption(JSNI_OPTION);
    options.addOption(JSINTERFACE_OPTION);
    SRC_PATH_OPTION.setArgName(SRC_PATH_ARG);
    SRC_PATH_OPTION.setRequired(true);
    options.addOption(SRC_PATH_OPTION);
    TARGET_PATH_OPTION.setArgName(TARGET_PATH_ARG);
    TARGET_PATH_OPTION.setRequired(true);
    options.addOption(TARGET_PATH_OPTION);
    options.addOption(TYPE_MAPPING_OPTION);
    final CommandLineParser parser = new GnuParser();
    cmd = parser.parse(options, args);
  }

  public List<File> getSourcePaths() throws FileNotFoundException {
    final List<File> dirs = new ArrayList<>();
    final String paths = cmd.getOptionValue(SRC_PATH);
    for (final String dir: paths.split(";")) {
      dirs.add(getPath(dir, SRC_PATH));
    }
    return dirs;
  }

  public File getTargetDir() throws FileNotFoundException {
    return getPath(cmd.getOptionValue(TARGET_PATH), TARGET_PATH);
  }

  private File getPath(final String path, final String option)
      throws FileNotFoundException {
    final File file = new File(path);

    if (!file.exists()) {
      throw new FileNotFoundException(
          path + " as supplied by -" + option + " does not exist. ");
    }
    return file;
  }

  public Properties getTypeMappingProperties()
      throws FileNotFoundException, IOException {
    final Properties props = new Properties();
    if (cmd.hasOption(TYPE_MAPPING)) {
      final File localFile = new File(cmd.getOptionValue(TYPE_MAPPING));
      if (localFile.exists()) {
        try (final InputStream is = new FileInputStream(localFile)) {
          props.load(is);
        }
      }
    }
    return props;
  }

  public boolean isJSInterface() {
    return cmd.hasOption(JSINTERFACE);
  }

  /**
   * Print help or version information if arguments specified. Returns true if the information was printed.
   * @return true if information was printed
   */
  public boolean printIfInfoOption() {
    if (cmd.hasOption(HELP)) {
      printHelp();
    } else {
      return false;
    }
    return true;
  }

  private void printHelp() {
    final HelpFormatter formatter = new HelpFormatter();
    formatter.printHelp("gen-gwt-wrapper", options, true);
  }
}
