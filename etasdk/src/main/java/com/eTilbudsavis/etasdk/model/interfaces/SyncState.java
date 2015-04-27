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

package com.eTilbudsavis.etasdk.model.interfaces;



/**
 * The state an object can be in, this is an indication of
 * whether the item needs synchronization with the API or not.
 */
public interface SyncState<T> {

	int TO_SYNC	= 0;
	int SYNCING	= 1;
	int SYNCED	= 2;
	int DELETE	= 4;
	int ERROR	= 5;
	
	/**
	 * Get the current state of this object. The state is <i>not</i> a feature of the API, and only for usage client-side.
	 * This is used throughout the SDK, to handle synchronization of e.g. lists, and their items.
	 * @return A {@link SyncState}
	 */
	public int getState();
	
	/**
	 * Set a new {@link SyncState} for this object.
	 * @param state A {@link SyncState} (where state >= {@link SyncState#TO_SYNC} && state <= {@link SyncState#ERROR})
	 * @return
	 */
	public T setState(int state);
	
}
