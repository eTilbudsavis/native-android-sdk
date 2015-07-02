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

package com.eTilbudsavis.etasdk.request.impl;

import com.eTilbudsavis.etasdk.network.Delivery;
import com.eTilbudsavis.etasdk.network.EtaError;
import com.eTilbudsavis.etasdk.network.Request;
import com.eTilbudsavis.etasdk.network.Response.Listener;
import com.eTilbudsavis.etasdk.network.impl.JsonObjectRequest;
import com.eTilbudsavis.etasdk.request.RequestAutoFill;
import com.eTilbudsavis.etasdk.request.RequestAutoFill.AutoFillParams;

import org.json.JSONObject;

public abstract class ObjectRequest<T> extends JsonObjectRequest {

    private RequestAutoFill<T> mAutoFiller;

    public ObjectRequest(String url, Listener<T> listener) {
        super(url, null);
        init(listener);
    }

    public ObjectRequest(Method method, String url, JSONObject requestBody, Listener<T> listener) {
        super(method, url, requestBody, null);
        init(listener);
    }

    private void init(Listener<T> listener) {
        super.setDelivery(new DeliveryHelper<T>(this, listener));
    }

    public Request<?> setAutoFill(RequestAutoFill<T> filler) {
        mAutoFiller = filler;
        return this;
    }

    public RequestAutoFill<T> getAutoFill() {
        return mAutoFiller;
    }

    protected void runAutoFill(final T response, final EtaError error) {
        addEvent("delivery-intercepted");
        getAutoFill().prepare(new AutoFillParams(this), response, error, new Listener<T>() {

            public void onComplete(T response, EtaError error) {
                ((DeliveryHelper<T>) getDelivery()).deliver(response, error);
            }
        });
        getAutoFill().execute(getRequestQueue());
    }

    @Override
    public void cancel() {
        super.cancel();
        if (mAutoFiller != null) {
            mAutoFiller.cancel();
        }
    }

    @Override
    public Request<?> setDelivery(Delivery delivery) {
        String msg = "ObjectRequest does not support setting Delivery. All requests are returned to UI Thread";
        throw new UnsupportedOperationException(msg);
    }

    public static abstract class Builder<T> extends com.eTilbudsavis.etasdk.request.Builder<ObjectRequest<T>> {

        private RequestAutoFill<T> mAutofill;

        public Builder(ObjectRequest<T> r) {
            super(r);
        }

        public ObjectRequest<T> build() {
            ObjectRequest<T> r = super.build();
            if (mAutofill != null) {
                r.setAutoFill(mAutofill);
            }
            return r;
        }

        protected RequestAutoFill<T> getAutofill() {
            return mAutofill;
        }

        protected void setAutoFiller(RequestAutoFill<T> filler) {
            mAutofill = filler;
        }

    }

}
