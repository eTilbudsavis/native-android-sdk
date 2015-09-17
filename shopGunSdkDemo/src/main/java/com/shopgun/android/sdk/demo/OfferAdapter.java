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

package com.shopgun.android.sdk.demo;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.shopgun.android.sdk.model.Offer;

import java.util.List;

public class OfferAdapter extends BaseAdapter {

    private List<Offer> mOffers;
    private int mDarkBackground = Color.argb(0x10, 0, 0, 0);
    private LayoutInflater mInflater;

    public OfferAdapter(Context c, List<Offer> mOffers) {
        this.mInflater = LayoutInflater.from(c);
        this.mOffers = mOffers;
    }

    @Override
    public int getCount() {
        return mOffers.size();
    }

    @Override
    public Object getItem(int position) {
        return mOffers.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        Offer o = mOffers.get(position);

        View v = mInflater.inflate(R.layout.offer_list_view, parent, false);
        TextView heading = (TextView) v.findViewById(R.id.heading);
        TextView price = (TextView) v.findViewById(R.id.price);

        heading.setText(o.getHeading());
        price.setText(o.getPricing().getPrice() + o.getPricing().getCurrency().getSymbol());

        if (position%2==0) {
            v.setBackgroundColor(mDarkBackground);
        }

        return v;
    }

}
