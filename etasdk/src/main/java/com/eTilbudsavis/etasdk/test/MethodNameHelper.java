package com.eTilbudsavis.etasdk.test;

import java.lang.reflect.Method;

/**
 * Proper use of this class is
 *     Method me = (new MethodNameHelper(){}).getMethod();
 * the anonymous class allows easy access to the method name of the enclosing scope.
 */
public class MethodNameHelper {
  public Method getMethod() {
    return this.getClass().getEnclosingMethod();
  }
  public String getName() {
	  Method m = getMethod();
	  return m == null ? "null" : m.getName();
  }
}