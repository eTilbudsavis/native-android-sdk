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

package com.eTilbudsavis.etasdk.test;

import junit.framework.Assert;
import android.graphics.Bitmap;

import com.eTilbudsavis.etasdk.Constants;
import com.eTilbudsavis.etasdk.imageloader.impl.CatalogThumbBitmapProcessor;
import com.eTilbudsavis.etasdk.model.Dimension;

public class CatalogThumbBitmapProcessorTest {
	
	public static final String TAG = Constants.getTag(CatalogThumbBitmapProcessorTest.class);
	
	/** The current default height from the API */
	public static final int HEIGHT = 212;
	
	/** The current default width from the API */
	public static final int WIDTH = 177; 
	
	public static void test() {

		EtaSdkTest.start(TAG);
		testCropBitmap();
		
	}
	
	public static void testCropBitmap() {

		Bitmap b = Bitmap.createBitmap(WIDTH, HEIGHT, Bitmap.Config.ARGB_8888);
		Dimension d = new Dimension();
		
		d.setHeight(2);
		d.setWidth(1);
		CatalogThumbBitmapProcessor p = new CatalogThumbBitmapProcessor(d);
//		p.setPrint(true);
		Bitmap tmp = p.process(b);
		Assert.assertEquals(106, tmp.getWidth());
		Assert.assertEquals(HEIGHT, tmp.getHeight());

		d.setHeight(0.95238007243699);
		d.setWidth(1);
		tmp = p.process(b);
		Assert.assertEquals(WIDTH, tmp.getWidth());
		Assert.assertEquals(168, tmp.getHeight());

		d.setHeight(1);
		d.setWidth(1);
		tmp = p.process(b);
		Assert.assertEquals(WIDTH, tmp.getWidth());
		Assert.assertEquals(HEIGHT, tmp.getHeight());

		d.setHeight(0);
		d.setWidth(0);
		tmp = p.process(b);
		Assert.assertEquals(WIDTH, tmp.getWidth());
		Assert.assertEquals(HEIGHT, tmp.getHeight());

		// We cannot expect things to be thrown in a non-JUnit project
//		d.setHeight(0);
//		d.setWidth(1);
//		tmp = p.process(b); // expect throw
//		Assert.assertEquals(WIDTH, tmp.getWidth());
//		Assert.assertEquals(0, tmp.getHeight());
		
		EtaSdkTest.logTest(TAG, "CropBitmap");
		
	}
	
}
