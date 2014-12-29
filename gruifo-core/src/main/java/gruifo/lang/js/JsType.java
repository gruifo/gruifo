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
package gruifo.lang.js;

import java.util.ArrayList;
import java.util.List;

public class JsType {

  public static class JsTypeSpec {
    private final String name;
    private final String rawType;
    private final List<JsTypeSpec> generics = new ArrayList<>();

    public JsTypeSpec(final String name, final String rawType) {
      this.name = name;
      this.rawType = rawType;
    }

    public void addGeneric(final JsTypeSpec generic) {
      generics.add(generic);
    }

    public List<JsTypeSpec> getGenerics() {
      return generics;
    }

    public String getName() {
      return name;
    }

    public String getRawType() {
      return rawType;
    }

    public boolean isGeneric() {
      return !generics.isEmpty();
    }

    @Override
    public String toString() {
      return "JsTypeSpec [name=" + name + ", rawType=" + rawType
          + ", generics=" + generics + "]";
    }
  }

  private boolean _function;
  private final List<JsTypeSpec> types = new ArrayList<>();
  private boolean optional;
  private boolean notNull;
  private boolean canNull;
  private boolean varArgs;
  private final String rawType;

  public JsType(final String rawType) {
    this.rawType = rawType;
  }

  public void addType(final JsTypeSpec type) {
    types.add(type);
  }

  public String getRawType() {
    return rawType;
  }

  public List<JsTypeSpec> getTypes() {
    return types;
  }

  public boolean isCanNull() {
    return canNull;
  }

  public boolean isFunction() {
    return _function;
  }

  public boolean isOptional() {
    return optional;
  }

  public boolean isNotNull() {
    return notNull;
  }

  public boolean isVarArgs() {
    return varArgs;
  }

  public void setFunction() {
    _function = true;
  }

  /**
   * Type is optional.
   */
  public void setOptional() {
    optional = true;
  }

  /**
   * Value may not be null.
   */
  public void setNotNull() {
    notNull = true;
  }

  /**
   * Value may be null.
   */
  public void setNull() {
    canNull = true;
  }

  /**
   * Type is a var args type.
   */
  public void setVarArgs() {
    varArgs = true;
  }

  @Override
  public String toString() {
    return getRawType();
  }
}