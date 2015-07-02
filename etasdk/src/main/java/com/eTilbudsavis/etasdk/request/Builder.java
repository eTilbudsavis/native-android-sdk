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

import com.eTilbudsavis.etasdk.network.Request;

public abstract class Builder<T extends Request<?>> {

    private T mRequest;
    private RequestParameter mParameters;

    public Builder(T r) {
        mRequest = r;
    }

    public T build() {
        if (mParameters != null) {
            mRequest.putParameters(mParameters.getParameters());
        }
        return mRequest;
    }

    protected RequestParameter getParameters() {
        return mParameters;
    }

    protected void setParameters(RequestParameter params) {
        mParameters = params;
    }

}
