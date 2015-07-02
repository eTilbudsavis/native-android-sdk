/*******************************************************************************
 * Copyright 2015 eTilbudsavis
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.eTilbudsavis.etasdk.test;

import java.lang.reflect.Method;

/**
 * Proper use of this class is
 * Method me = (new MethodNameHelper(){}).getMethod();
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