/*******************************************************************************
* Copyright 2014 eTilbudsavis
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
*******************************************************************************/
package com.eTilbudsavis.etasdk.network.impl;

import com.eTilbudsavis.etasdk.network.EtaError;


public class ApiError extends EtaError {
	
	private static final long serialVersionUID = 1L;
	
	public ApiError (int code, String message, String id, String details, String failedOnField) {
		super(code, message, id, details, failedOnField);
	}
	
}
