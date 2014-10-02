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
package gengwtjs.lang.js;

import gengwtjs.lang.AccessType;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JsElement {
  private static final Logger LOG = LoggerFactory.getLogger(JsElement.class);

  public static enum ElementType {
    CLASS,
    CONST,
    CONSTRUCTOR,
    DEFINE,
    ENUM,
    INTERFACE,
    METHOD,
    TYPEDEF;
  }

  public static class JsParam {
    private JsType type;
    private String name;
    private JsElement element;

    public JsParam() {
    }

    public JsParam(final String name, final JsElement element) {
      this.name = name;
      this.element = element;
    }

    public JsElement getElement() {
      return element;
    }

    public String getName() {
      return name;
    }

    public JsType getType() {
      return element == null ? type : element.getType();
    }

    public void setName(final String name) {
      this.name = name;
    }

    public void setType(final JsType jsType) {
      this.type = jsType;
    }

    @Override
    public String toString() {
      return "JsParam [name=" + name + ", type=" + type + "]";
    }
  }

  private AccessType accessType = AccessType.PUBLIC;
  private ElementType elementType;
  private boolean classDesc;
  private String jsDoc;
  private JsType _extends;
  private final List<JsParam> params = new ArrayList<>();
  private Object typeDef;
  private JsType type;
  private JsType _return;
  private boolean override;
  private String genericType;
  private JsType define;

  public String getJsDoc() {
    return jsDoc;
  }

  public AccessType getAccessType() {
    return accessType;
  }

  public JsType getDefine() {
    return define;
  }

  public JsType getExtends() {
    return _extends;
  }

  public String getGenericType() {
    return genericType;
  }

  public List<JsParam> getParams() {
    return params;
  }

  public JsType getType() {
    return type;
  }

  public Object getTypeDef() {
    return typeDef;
  }

  public JsType getReturn() {
    return _return;
  }

  public boolean isClass() {
    return elementType == ElementType.CONSTRUCTOR;
  }

  public boolean isClassDescription() {
    return classDesc;
  }

  public boolean isConst() {
    return elementType == ElementType.CONST;
  }

  public boolean isConstructor() {
    return elementType == ElementType.CONSTRUCTOR;
  }

  public boolean isDefine() {
    return elementType == ElementType.DEFINE;
  }

  public boolean isEnum() {
    return elementType == ElementType.ENUM;
  }

  public boolean isOverride() {
    return override;
  }

  public boolean isInterface() {
    return elementType == ElementType.INTERFACE;
  }

  public boolean isMethod() {
    return elementType == ElementType.METHOD;
  }

  public boolean isPrivate() {
    return accessType == AccessType.PRIVATE;
  }

  public boolean isProtected() {
    return accessType == AccessType.PROTECTED;
  }

  //  public void setClass() {
  //    elementType = ElementType.CLASS;
  //  }

  public void setClassDesc() {
    classDesc = true;
  }

  public void setConst() {
    elementType = ElementType.CONST;
  }

  public void setConstructor() {
    elementType = ElementType.CONSTRUCTOR;
    //    setClass();
  }

  public void setDefine(final JsType define) {
    this.define = define;
    elementType = ElementType.DEFINE;
  }

  public void setEnum() {
    elementType = ElementType.ENUM;
  }

  public void setExtends(final JsType _extends) {
    this._extends = _extends;
  }

  public void setGenericType(final String genericType) {
    this.genericType = genericType;
  }

  public void setJsDoc(final String jsDoc) {
    this.jsDoc = jsDoc;
  }

  public void setOverride() {
    this.override = true;
  }

  public void setInterface() {
    elementType = ElementType.INTERFACE;
  }

  public void setMethod() {
    elementType = ElementType.METHOD;
  }

  public void setPrivate() {
    accessType = AccessType.PRIVATE;
    //FIXME:    this._private = !(isClass() || isInterface());
  }

  public void setProtected() {
    accessType = AccessType.PROTECTED;
  }

  public void setReturn(final JsType _return) {
    this._return = _return;
  }

  public void setType(final JsType type) {
    this.type = type;
  }

  public void setTypeDef(final Object typeDef) {
    this.typeDef = typeDef;
    if (elementType != null) {
      LOG.error("ElementType already set: {}", elementType);
      //    elementType = ElementType.TYPEDEF;
    }
  }
  @Override
  public String toString() {
    return "JsElement [accessType=" + accessType + ", elementType="
        + elementType + ", classDesc=" + classDesc + ", comment=" + jsDoc
        + ", _extends=" + _extends + ", params=" + params + ", typeDef="
        + typeDef + ", type=" + type + ", _return=" + _return + ", override="
        + override + ", genericType=" + genericType + ", define=" + define
        + "]";
  }
}
