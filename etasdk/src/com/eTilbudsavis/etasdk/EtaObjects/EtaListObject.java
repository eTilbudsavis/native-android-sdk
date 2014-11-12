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
package com.eTilbudsavis.etasdk.EtaObjects;

import java.io.Serializable;

import com.eTilbudsavis.etasdk.Eta;

public abstract class EtaListObject<T> implements Comparable<T>, Serializable {
	
	public static final String TAG = Eta.TAG_PREFIX + EtaListObject.class.getSimpleName();
	
	private static final long serialVersionUID = 8166712456946780878L;
	
	/**
	 * The state an EtaListObject can be in, this is an indication of
	 * whether the item needs synchronization with the API or not.
	 */
	public interface State {
		int TO_SYNC	= 0;
		int SYNCING	= 1;
		int SYNCED	= 2;
		int DELETE	= 4;
		int ERROR	= 5;
	}
	
	/** 
	 * 
	 * A string indication the first item in a list of items.
	 * <p>Represented by the static value:
	 * "00000000-0000-0000-0000-000000000000"</p>
	 */
	public final static String FIRST_ITEM = "00000000-0000-0000-0000-000000000000";
	
	private int mState = State.TO_SYNC;
	
	/**
	 * Get the current state of this object. The state is <i>not</i> a feature of the API, and only for usage client-side.
	 * This is used throughout the SDK, to handle synchronization of e.g. lists, and their items.
	 * @return A {@link State}
	 */
	public int getState() {
		return mState;
	}
	
	/**
	 * Set a new {@link State} for this object.
	 * @param state A {@link State} (where state >= {@link State#TO_SYNC} && state <= {@link State#ERROR})
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public T setState(int state) {
		if (State.TO_SYNC <= state && state <= State.ERROR) {
			mState = state;
		}
		return (T)this;
	}
	
	public String getStateString() {
		
		switch (mState) {
		case State.TO_SYNC:
			return "TO_SYNC";

		case State.SYNCING:
			return "SYNCING";

		case State.SYNCED:
			return "SYNCED";

		case State.DELETE:
			return "DELETE";
			
		case State.ERROR:
			return "ERROR";
			
		default:
			break;
		}
		return null;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + mState;
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		return true;
	}
	
	
	
}
