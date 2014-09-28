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

public class JsElement {

  public static enum ElementType {
    CLASS,
    CONST,
    CONSTRUCTOR,
    ENUM,
    INTERFACE,
    METHOD,
    TYPEDEF;
  }

  public static class JsParam {
    private JsType types;
    private String name;

    public String getName() {
      return name;
    }

    public JsType getTypes() {
      return types;
    }

    public void setName(final String name) {
      this.name = name;
    }

    public void setType(final JsType jsType) {
      this.types = jsType;
    }
  }

  public static class JsField {

  }

  private AccessType accessType = AccessType.PUBLIC;
  private ElementType elementType;
  private boolean classDesc;
  private String comment;
  private JsType _extends;
  private final List<JsParam> params = new ArrayList<>();
  private JsType _return;
  private boolean override;
  private String genericType;

  public String getComment() {
    return comment;
  }
  public void setComment(final String comment) {
    this.comment = comment;
  }

  public boolean isClass() {
    return elementType == ElementType.CONSTRUCTOR;
  }

  public AccessType getAccessType() {
    return accessType;
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

  public JsType getReturn() {
    return _return;
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

  public void setEnum() {
    elementType = ElementType.ENUM;
  }

  public void setExtends(final JsType _extends) {
    this._extends = _extends;
  }

  public void setAsField(final JsType parseSingleType) {
  }

  public void setGenericType(final String genericType) {
    this.genericType = genericType;
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

  public void setTypeDef() {
    elementType = ElementType.TYPEDEF;
  }
}
