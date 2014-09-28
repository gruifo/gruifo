package gengwtjs.lang.java;

import gengwtjs.lang.AccessType;

import java.util.ArrayList;
import java.util.List;

public class JMethod {
  private String methodName;
  private final String classPath;
  private final AccessType accessType;
  private String _return;
  private final List<JParam> params = new ArrayList<>();
  private String comment;
  private boolean complex;
  private String genericType;

  public JMethod(final String classPath, final String functionName, final AccessType accessType) {
    this.classPath = classPath;
    methodName = functionName;
    this.accessType = accessType;
  }

  public void addParam(final JParam param) {
    params.add(param);
  }

  public AccessType getAccessType() {
    return accessType;
  }

  public String getClassPath() {
    return classPath;
  }

  public String getComment() {
    return comment;
  }

  public String getGenericType() {
    return genericType;
  }

  public String getMethodName() {
    return methodName;
  }

  public List<JParam> getParams() {
    return params;
  }

  public String getReturn() {
    return _return;
  }

  public boolean isComplex() {
    return complex;
  }

  public void setComment(final String comment) {
    this.comment = comment;
  }

  public void setComplex(final boolean complex) {
    this.complex = complex;
  }

  public void setGenericType(final String genericType) {
    this.genericType = genericType;
  }

  public void setMethodName(final String methodName) {
    this.methodName = methodName;
  }

  public void setReturn(final String _return) {
    this._return = _return;
  }
}