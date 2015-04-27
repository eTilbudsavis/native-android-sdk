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

package com.eTilbudsavis.etasdk.request;

import java.util.HashMap;
import java.util.Map;

public abstract class ParameterBuilder implements RequestParameter {
	
	private Map<String, String> mParam = new HashMap<String, String>();
	
	public ParameterBuilder() {
		
	}
	
	public ParameterBuilder(Map<String, String> parameters) {
		mParam.putAll(parameters);
	}
	
	public ParameterBuilder(RequestParameter parameters) {
		mParam.putAll(parameters.getParameters());
	}

	protected void put(Map<String, String> parameters) {
		mParam.putAll(parameters);
	}
	
	protected String put(String parameter, String value) {
		return mParam.put(parameter, value);
	}
	
	protected void clear() {
		mParam.clear();
	}
	
	protected String remove(String parameter) {
		return mParam.remove(parameter);
	}
	
	public Map<String, String> getParameters() {
		return mParam;
	}
	
}
