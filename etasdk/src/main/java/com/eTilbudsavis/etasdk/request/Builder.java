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
