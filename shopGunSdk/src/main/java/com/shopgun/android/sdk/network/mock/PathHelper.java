/*******************************************************************************
 * Copyright 2015 ShopGun
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

package com.shopgun.android.sdk.network.mock;

import com.shopgun.android.sdk.network.Request;

import java.net.MalformedURLException;
import java.net.URL;

public class PathHelper {

    int version = -1;
    int type = -1;
    int action_or_id = -1;
    int itemAction = -1;

    String[] mPath;

    public PathHelper(Request<?> request) {

        try {
            String path = new URL(request.getUrl()).getPath();
            mPath = path.split("/");
            offset();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

    }

    public PathHelper(String[] path) {
        mPath = path;
        offset();
    }

    private void offset() {
        // assuming api v2
        for (int i = 0; i < mPath.length; i++) {
            if ("v2".equals(mPath[i])) {
                setVersionOffset(i);
            }

        }
    }

    private void setVersionOffset(int versionIndex) {
        version = versionIndex;
        type = versionIndex + 1;
        action_or_id = versionIndex + 2;
        itemAction = versionIndex + 3;
    }

    public String getApiVersion() {
        return get(version);
    }

    public boolean hasType() {
        return mPath.length >= type;
    }

    public String getType() {
        return get(type);
    }

    public boolean hasActionOrId() {
        return mPath.length >= action_or_id;
    }

    public String getActionOrId() {
        return get(action_or_id);
    }

    public boolean hasAction() {
        return mPath.length >= type;
    }

    public String getItemAction() {
        return get(itemAction);
    }

    private String get(int index) {
        try {
            return mPath[index];
        } catch (ArrayIndexOutOfBoundsException e) {
            return null;
        }
    }

}
