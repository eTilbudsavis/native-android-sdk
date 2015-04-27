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

package com.eTilbudsavis.sdkdemo;

import java.util.HashMap;
import java.util.Map;


public class Constants {
	
	private Constants() { }
	
	public static final Map<String, LatLng> LOC_LIST = new HashMap<String, LatLng>();
	
	public static final LatLng ETA_HQ = new LatLng(55.6310771f,12.5771624f);
	public static final LatLng COPENHAGEN = new LatLng(55.676065f,12.5689284f);
	public static final LatLng AALBORG = new LatLng(57.0433192f,9.9198792f);
	public static final LatLng AARHUS = new LatLng(56.1499752f,10.2009121f);
	public static final LatLng ODENSE = new LatLng(55.3949f,10.3915109f);
	public static final LatLng HERNING = new LatLng(56.1355306f,8.9757374f);
	public static final LatLng VIBORG = new LatLng(56.4531611f,9.404133f);
	public static final LatLng HOLSTEBRO = new LatLng(56.3619483f,8.6166098f);
	public static final LatLng KOLDING = new LatLng(55.4907499f,9.4814275f);
	
	static {
		LOC_LIST.put("ETA_HQ", ETA_HQ);
		LOC_LIST.put("COPENHAGEN", COPENHAGEN);
		LOC_LIST.put("AALBORG", AALBORG);
		LOC_LIST.put("AARHUS", AARHUS);
		LOC_LIST.put("ODENSE", ODENSE);
		LOC_LIST.put("HERNING", HERNING);
		LOC_LIST.put("VIBORG", VIBORG);
		LOC_LIST.put("HOLSTEBRO", HOLSTEBRO);
		LOC_LIST.put("KOLDING", KOLDING);
	}
	
	public static class LatLng {
		float lat;
		float lng;
		public LatLng(float lat, float lng) {
			this.lat = lat;
			this.lng = lng;
		}
	}
	
}
