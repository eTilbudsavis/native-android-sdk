package com.eTilbudsavis.etasdk.test;

import junit.framework.Assert;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

import com.eTilbudsavis.etasdk.Constants;
import com.eTilbudsavis.etasdk.model.Shoppinglist;

public class SerializationSpeedTest {
	
	public static final String TAG = Constants.getTag(SerializationSpeedTest.class);

	public static void test() {
		test(100);
	}

	public static void test(int count) {
		
		EtaSdkTest.start(TAG);
		EtaSdkTest.logTest(TAG, "testing " + count + " iterations of serilization");
		
		long start = System.currentTimeMillis();
		test(count, ObjectCreator.getShoppinglist(), Shoppinglist.CREATOR);
		print("Shoppinglist", count, start);
		
	}
	
	private static void print(String name, int count, long start) {
		long time = (System.currentTimeMillis() - start);
		float avg = (time)/(float) count;
		String format = "total: %sms, avg: %.2fms";
		EtaSdkTest.logTest(TAG, String.format(format, time, avg));
		
	}
	
	private static <T extends Parcelable> void test(int count, T obj, Creator<? extends Object> c) {
		
		for (int i = 0; i < count; i++) {
			Parcel parcel = Parcel.obtain();
			obj.writeToParcel(parcel, 0);
	        parcel.setDataPosition(0);
	        Object parceledObj = c.createFromParcel(parcel);
	        Assert.assertEquals(obj, parceledObj);
		}
		
	}
	
	private static void testShoppinglistSerilization(int count) {
		
		Shoppinglist sl = ObjectCreator.getShoppinglist();
		for (int i = 0; i < count; i++) {
			Parcel parcel = Parcel.obtain();
			sl.writeToParcel(parcel, 0);
	        parcel.setDataPosition(0);
	        Shoppinglist parceledObj = Shoppinglist.CREATOR.createFromParcel(parcel);
		}
		
	}
	
}
