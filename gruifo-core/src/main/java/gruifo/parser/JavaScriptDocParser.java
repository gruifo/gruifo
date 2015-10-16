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
import gruifo.lang.js.JsParam;
import gruifo.lang.js.JsType;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Parses the annotations in the JavaScript doc.
 */
public class JavaScriptDocParser {
  private static final Logger LOG =
      LoggerFactory.getLogger(JavaScriptDocParser.class);

  /**
   * Annotation: @api
   */
  private static final String API = "api";
  /**
   * Annotation: @class
   */
  private static final String CLASS = "class";
  /**
   * Annotation: @classdesc
   */
  private static final String CLASSDESC = "classdesc";
  /**
   * Annotation: @const
   */
  private static final String CONST = "const";
  /**
   * Annotation: @constructor
   */
  private static final String CONSTRUCTOR = "constructor";
  /**
   * Annotation: @define
   */
  private static final String DEFINE = "define";
  /**
   * Annotation: @deprecated
   */
  private static final String DEPRECATED = "deprecated";
  /**
   * Annotation: @extends {[class]}
   */
  private static final String EXTENDS = "extends";
  /**
   * Annotation: @enum
   */
  private static final String ENUM = "enum";
  /**
   * Annotation: @fires {[type]}
   */
  private static final String FIRES = "fires";
  /**
   * Annotation: @function
   */
  private static final String FUNCTION = "function";
  /**
   * Annotation: @implements
   */
  private static final String IMPLEMENTS = "implements";
  /**
   * Annotation: @inheritDoc
   * <p>When this annotation is found on a method the method can be ignored,
   * since the wrapper doesn't add extra functionality.
   */
  private static final String INHERITDOC = "inheritDoc";
  /**
   * Annotation: @interface
   */
  private static final String INTERFACE = "interface";
  /**
   * Annotation: @link
   */
  private static final String LINK = "link";
  /**
   * Annotation: @nosideeffects
   */
  private static final String NOSIDEEFFECTS = "nosideeffects";
  /**
   * Annotation: @param {[type]} [name] [description]
   * <p>[type] can be one or more types, separated by a bar: |
   */
  private static final String PARAM = "param";
  /**
   * Annotation: @private
   */
  private static final String PRIVATE = "private";
  /**
   * Annotation: @protected
   */
  private static final String PROTECTED = "protected";
  /**
   * Annotation: @observable
   */
  private static final String OBSERVABLE = "observable";
  /**
   * Annotation: @override
   */
  private static final String OVERRIDE = "override";

  /**
   * Annotation: @return {[type}] [description]
   */
  private static final String RETURN = "return";
  /**
   * Annotation: @see
   */
  private static final String SEE = "see";
  /**
   * Annotation: @struct
   */
  private static final String STRUCT = "struct";
  /**
   * Annotation: @suppress
   */
  private static final String SUPPRESS = "suppress";
  /**
   * Annotation: @template
   */
  private static final String TEMPLATE = "template";
  /**
   * Annotation: @this
   */
  private static final String THIS = "this";
  /**
   * Annotation: @todo
   */
  private static final String TODO = "todo";
  /**
   * Annotation: @type
   */
  private static final String TYPE = "type";
  /**
   * Annotation: @typedef
   */
  private static final String TYPEDEF = "typedef";

  private static final Pattern ANNOTATION_PATTERN =
      Pattern.compile("@([^ ]+) ?.*");
  private static final Pattern COMMENT_PATTERN =
      Pattern.compile("^ *[^ ]+ (.*)");

  private final JsTypeParser jsTypeParser = new JsTypeParser();
  private final JsDocTypedefParser typedefParser =
      new JsDocTypedefParser(jsTypeParser);

  public JsElement parse(final String fileName, final String comment) {
    final JsElement doc = new JsElement();
    doc.setJsDoc(convertComment(comment));
    if (comment == null) {
      LOG.error("Comment in file {} is empty.", fileName);
      return null;
    }
    final String lines[] = comment.split("\\r?\\n");
    for (int i = 0; i < lines.length; i++) {
      final String line = lines[i];
      final String annotation = findAnnotation(line);
      switch(annotation) {
      case CLASS:
        //doc.setClass();
        LOG.error("dectected @class in file {}", fileName);
        break;
      case CLASSDESC:
        doc.setClassDesc();
        break;
      case CONST:
        doc.setConst();
        break;
      case CONSTRUCTOR:
        doc.setConstructor();
        break;
      case DEFINE:
        doc.setDefine(parseType(line, fileName));
        break;
      case ENUM:
        doc.setEnum(parseType(line, fileName));
        break;
      case EXTENDS:
        doc.setExtends(parseType(line, fileName));
        break;
      case FUNCTION:
        doc.setMethod();
        break;
      case IMPLEMENTS:
        doc.addImplements(parseType(line, fileName));
        break;
      case INHERITDOC:
      case OVERRIDE:
        doc.setOverride();
        break;
      case INTERFACE:
        doc.setInterface();
        break;
      case PARAM:
        parseParam(fileName, doc, lines, i, line);
        break;
      case PRIVATE:
        doc.setPrivate();
        break;
      case PROTECTED:
        doc.setProtected();
        break;
      case RETURN:
        doc.setReturn(parseType(line, fileName));
        break;
      case TYPE:
        doc.setType(parseType(line, fileName));
        break;
      case TEMPLATE:
        doc.setGenericType(parseTemplateType(line, fileName));
        // TODO add support for @template
        //LOG.error("Annotation template not supported, found in file:{}", annotation, fileName);
        break;
      case TYPEDEF:
        i = typedefParser.parseTypeDef(doc, lines, i, fileName);
        break;
      case API:
      case DEPRECATED:
      case FIRES:
      case LINK:
      case NOSIDEEFFECTS:
      case OBSERVABLE:
      case SEE:
      case STRUCT:
      case SUPPRESS:
      case THIS:
      case TODO:
        // ignore annotation, contains no information for the generation.
        break;
      default:
        if (!annotation.isEmpty()) {
          LOG.error("Annotation '{}' unknown, found in file:{}", annotation, fileName);
        }
        break;
      }
    }
    return doc;
  }

  private String convertComment(final String comment) {
    return comment;
  }

  private String findAnnotation(final String line) {
    final Matcher matcher = ANNOTATION_PATTERN.matcher(line);
    return matcher.find() ? matcher.group(1) : "";
  }

  private void parseParam(final String fileName, final JsElement doc,
      final String[] lines, final int i, final String line) {
    JsParam param = parseParam(line, fileName);
    if (param == null) {
      param = tryDoubleLineParam(line, lines, i, fileName);
    }
    if (param == null) {
      LOG.error("Parameter could not be parsed, line:{}, file:{}", line, fileName);
    } else {
      doc.getParams().add(param);
    }
  }

  private JsParam tryDoubleLineParam(final String line, final String[] lines,
      final int i, final String fileName) {
    JsParam jsParam;
    if (lines.length > i + 1) {
      final Matcher matcher = COMMENT_PATTERN.matcher(lines[i + 1]);
      final String nextLine = matcher.find() ? matcher.group(1) : "";
      if (nextLine.isEmpty()) {
        jsParam = null;
      } else {
        jsParam = parseParam(line + nextLine, fileName);
      }
    } else {
      jsParam = null;
    }
    return jsParam;
  }

  private JsParam parseParam(final String line, final String fileName) {
    final Pattern pattern = Pattern.compile("\\{([^\\}]+)\\} +([^ ]+)");
    final Matcher matcher = pattern.matcher(line);
    final JsParam param = new JsParam();
    if (matcher.find()) {
      param.setType(jsTypeParser.parseType(matcher.group(1).trim()));
      param.setName(matcher.group(2).trim());
      return param;
    } else {
      return null;
    }
  }

  private String parseTemplateType(final String line, final String fileName) {
    final Pattern pattern = Pattern.compile("@template +([^ ]+)");
    final Matcher matcher = pattern.matcher(line);
    return matcher.find() ? matcher.group(1).trim() : "";
  }

  private JsType parseType(final String line, final String fileName) {
    final Pattern pattern = Pattern.compile("\\{([^\\}]+)\\}");
    final Matcher matcher = pattern.matcher(line);
    return matcher.find() ? jsTypeParser.parseType(matcher.group(1).trim()) : null;
  }
}
