package gruifo.lang.java;

import gruifo.lang.AccessType;

import java.util.ArrayList;
import java.util.List;

public class JMethod {
  private String methodName;
  private final String classPath;
  private final AccessType accessType;
  private String returnType;
  private final List<JParam> params = new ArrayList<>();
  private String jsDoc;
  private String genericType;
  private boolean abstractMethod;

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

  public String getJsDoc() {
    return jsDoc;
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
    return returnType;
  }

  public boolean isAbstractMethod() {
    return abstractMethod;
  }

  public void setAbstract(final boolean abstractMethod) {
    this.abstractMethod = abstractMethod;
  }

  public void setJsDoc(final String jsDoc) {
    this.jsDoc = jsDoc;
  }

  public void setGenericType(final String genericType) {
    this.genericType = genericType;
  }

  public void setMethodName(final String methodName) {
    this.methodName = methodName;
  }

  public void setReturn(final String returnType) {
    this.returnType = returnType;
  }
}