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

package com.shopgun.android.sdk;

import java.util.ArrayList;
import java.util.Collection;

public class FixedArrayList<E> extends ArrayList<E> {

    public static final String TAG = Constants.getTag(FixedArrayList.class);

    private static final long serialVersionUID = -2709268219112197508L;

    int mMaxSize = 16;

    public FixedArrayList(int size) {
        mMaxSize = size <= 0 ? 1 : size;
    }

    @Override
    public boolean addAll(Collection<? extends E> collection) {
        return false;
    }

    @Override
    public boolean addAll(int index, Collection<? extends E> collection) {
        return false;
    }

    @Override
    public boolean add(E object) {
        cleanUp();
        return super.add(object);
    }

    @Override
    public void add(int index, E object) {
        cleanUp();
        super.add(index, object);
    }

    private void cleanUp() {
        while (size() >= mMaxSize) {
            remove(0);
        }
    }

}
