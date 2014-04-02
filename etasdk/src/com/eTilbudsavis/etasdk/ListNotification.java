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
package com.eTilbudsavis.etasdk;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.eTilbudsavis.etasdk.EtaObjects.Shoppinglist;
import com.eTilbudsavis.etasdk.EtaObjects.ShoppinglistItem;

public class ListNotification {
	
	public static final String TAG ="ListNofitication";
	
	/* 
	 * Lists for collecting individual changes in items and lists, to do a single
	 * notification to any subscribers
	 */
	public Map<String, ShoppinglistItem> mItemAdded = Collections.synchronizedMap(new HashMap<String, ShoppinglistItem>());
	public Map<String, ShoppinglistItem> mItemDeleted = Collections.synchronizedMap(new HashMap<String, ShoppinglistItem>());
	public Map<String, ShoppinglistItem> mItemEdited = Collections.synchronizedMap(new HashMap<String, ShoppinglistItem>());
	public Map<String, Shoppinglist> mListAdded = Collections.synchronizedMap(new HashMap<String, Shoppinglist>());
	public Map<String, Shoppinglist> mListDeleted = Collections.synchronizedMap(new HashMap<String, Shoppinglist>());
	public Map<String, Shoppinglist> mListEdited = Collections.synchronizedMap(new HashMap<String, Shoppinglist>());
	
	boolean mIsServer = false;
	
	boolean mFirstSync = false;
	
	public ListNotification(boolean isServer) {
		mIsServer = isServer;
	}
	
	public boolean isServer() {
		return mIsServer;
	}
	
	public void setFirstSync(boolean firstSync) {
		mFirstSync = firstSync;
	}
	
	public boolean isFirstSync() {
		return mFirstSync;
	}
	
	/*
	 * Add new notification items to the maps
	 */
	public void add(Shoppinglist s) {
		mListAdded.put(s.getId(), s);
	}

	public void del(Shoppinglist s) {
		mListDeleted.put(s.getId(), s);
	}

	public void edit(Shoppinglist s) {
		mListEdited.put(s.getId(), s);
	}

	public void add(ShoppinglistItem s) {
		mItemAdded.put(s.getId(), s);
	}

	public void del(ShoppinglistItem s) {
		mItemDeleted.put(s.getId(), s);
	}

	public void edit(ShoppinglistItem s) {
		mItemEdited.put(s.getId(), s);
	}

	/*
	 * Convert maps to lists
	 */
	public List<ShoppinglistItem> getAddedItems() {
		return new ArrayList<ShoppinglistItem>(mItemAdded.values());
	}

	public List<ShoppinglistItem> getDeletedItems() {
		return new ArrayList<ShoppinglistItem>(mItemDeleted.values());
	}

	public List<ShoppinglistItem> getEditedItems() {
		return new ArrayList<ShoppinglistItem>(mItemEdited.values());
	}
	
	public List<Shoppinglist> getAddedLists() {
		return new ArrayList<Shoppinglist>(mListAdded.values());
	}

	public List<Shoppinglist> getDeletedLists() {
		return new ArrayList<Shoppinglist>(mListDeleted.values());
	}

	public List<Shoppinglist> getEditedLists() {
		return new ArrayList<Shoppinglist>(mListEdited.values());
	}
	
	public boolean hasListNotifications() {
		boolean empty = mListAdded.isEmpty() && mListDeleted.isEmpty() && mListEdited.isEmpty();
		return !empty;
	}
	
	public boolean hasItemNotifications() {
		boolean empty = mItemAdded.isEmpty() && mItemDeleted.isEmpty() && mItemEdited.isEmpty();
		return !empty;
	}
	
}
