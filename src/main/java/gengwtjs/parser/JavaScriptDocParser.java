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
package gengwtjs.parser;

import gengwtjs.lang.js.JsElement;
import gengwtjs.lang.js.JsElement.JsParam;
import gengwtjs.lang.js.JsType;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Parses the annotations in the JavaScript doc.
 */
public class JavaScriptDocParser {
  private static final Logger LOG = LoggerFactory.getLogger(JavaScriptDocParser.class);

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
  private static final String TODO = "todo";
  private static final String TYPE = "type";
  private static final String TYPEDEF = "typedef";

  private static final Pattern ANNOTATION_PATTERN = Pattern.compile("@([^ ]+) ?.*");

  private final JsTypeParser jsTypeParser = new JsTypeParser();

  public JsElement parse(final String fileName, final String comment) {
    final JsElement doc = new JsElement();
    doc.setComment(convertComment(comment));
    if (comment == null) {
      LOG.error("Comment in file {} is empty.", fileName);
      return null;
    }
    final String lines[] = comment.split("\\r?\\n");
    for (final String line : lines) {
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
      case ENUM:
        doc.setEnum();
        break;
      case EXTENDS:
        doc.setExtends(parseType(line, fileName));
        break;
      case FUNCTION:
        doc.setMethod();
        break;
      case IMPLEMENTS:
        //TODO
        break;
      case INHERITDOC:
      case OVERRIDE:
        doc.setOverride();
        break;
      case INTERFACE:
        doc.setInterface();
        break;
      case PARAM:
        final JsParam param = parseParam(line, fileName);
        if (param != null) {
          doc.getParams().add(param);
        }
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
        doc.setAsField(parseType(line, fileName));
        break;
      case TEMPLATE:
        doc.setGenericType(parseTemplateType(line, fileName));
        // TODO add support for @template
        //LOG.error("Annotation template not supported, found in file:{}", annotation, fileName);
        break;
      case TYPEDEF:
        doc.setTypeDef();
        doc.setExtends(parseType(line, fileName));
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
      case TODO:
        // ignore no additional value
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

  private String findAnnotation(final String line) {
    final Matcher matcher = ANNOTATION_PATTERN.matcher(line);
    return matcher.find() ? matcher.group(1) : "";
  }

  private String parseTemplateType(final String line, final String fileName) {
    final Pattern pattern = Pattern.compile("@template +([^ ]+)");
    final Matcher matcher = pattern.matcher(line);
    return matcher.find() ? matcher.group(1) : "";
  }

  private String convertComment(final String comment) {
    return comment;
  }

  private JsType parseType(final String line, final String fileName) {
    final Pattern pattern = Pattern.compile("\\{([^\\}]+)\\}");
    final Matcher matcher = pattern.matcher(line);
    return matcher.find() ? jsTypeParser.parseType(matcher.group(1)) : null;
  }

  private JsParam parseParam(final String line, final String fileName) {
    final Pattern pattern = Pattern.compile("\\{(.+)\\} +([^ ]+)");
    final Matcher matcher = pattern.matcher(line);
    final JsParam param = new JsParam();
    if (matcher.find()) {
      param.setType(jsTypeParser.parseType(matcher.group(1)));
      param.setName(matcher.group(2));
      return param;
    } else {
      LOG.error("Parameter could not be parsed, line:{}, file:{}", line, fileName);
      return null;
    }
  }
}
