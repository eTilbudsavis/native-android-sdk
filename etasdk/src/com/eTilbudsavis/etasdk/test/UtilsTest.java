package com.eTilbudsavis.etasdk.test;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

import junit.framework.Assert;
import android.graphics.Color;

import com.eTilbudsavis.etasdk.Constants;
import com.eTilbudsavis.etasdk.log.EtaLog;
import com.eTilbudsavis.etasdk.network.Request;
import com.eTilbudsavis.etasdk.network.impl.JsonObjectRequest;
import com.eTilbudsavis.etasdk.utils.ColorUtils;
import com.eTilbudsavis.etasdk.utils.Utils;

public class UtilsTest {
	
	public static final String TAG = Constants.getTag(UtilsTest.class);
	
	public static final String REGEX_UUID = "[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}";
	
	public static void test() {
		
		EtaSdkTest.start(TAG);
		testCreateUUID();
		testMapToQueryString();
		testRequestToUrlAndQueryString();
		testIsSuccess();
//		testStringToDate();
//		testDateToString();
		
	}
	
	public static void testCreateUUID() {
		
		String notUuid = "this should not match the REGEX_UUID";
		Assert.assertFalse(notUuid.matches(REGEX_UUID));
		
		String uuid = Utils.createUUID();
		Assert.assertTrue(uuid.matches(REGEX_UUID));
		
		String otherUuid = Utils.createUUID();
		Assert.assertTrue(otherUuid.matches(REGEX_UUID));
		
		Assert.assertNotSame(uuid, otherUuid); // They can theoretically be equal
		
		// replacing [0-9a-f] with any other char in [0-9a-f] shouldn't matter
		otherUuid = otherUuid.replace("a", "2").replace("b", "2").replace("c", "2").replace("d", "2").replace("e", "2").replace("f", "2");
		Assert.assertTrue(otherUuid.matches(REGEX_UUID));
		
		// G is not in a uuid
		String uuidFake = "83g24023-3225-4392-9362-332619620638";
		Assert.assertFalse(uuidFake.matches(REGEX_UUID));
		
		EtaSdkTest.logTest(TAG, "CreateUUID");
		
	}
	
	public static void testMapToQueryString() {

		String UTF8 = "utf-8";
		String expected = "";
		
		// null test
		String actual = Utils.mapToQueryString(null, null);
		Assert.assertEquals(expected, actual);
		
		actual = Utils.mapToQueryString(null, UTF8);
		Assert.assertEquals(expected, actual);
		
		// empty map test
		Map<String, String> map = new HashMap<String, String>();
		actual = Utils.mapToQueryString(map, UTF8);
		Assert.assertEquals(expected, actual);
		
		// fill the map
		map.put("b", "blue");
		map.put("a", "red");
		map.put("d", "green");
		
		// Creating incorrect encoding
		actual = Utils.mapToQueryString(map, null);
		// order by input order to the map - this is false
		String expectedFalse = "b=blue&a=red&d=green";
		Assert.assertNotSame(expectedFalse, actual);
		// alphabetical order - should be true
		expected = "a=red&b=blue&d=green";
		Assert.assertEquals(expected, actual);
		
		// Creating incorrect encoding
		actual = Utils.mapToQueryString(map, "not utf-8");
		// order by input order to the map - this is false
		expectedFalse = "b=blue&a=red&d=green";
		Assert.assertNotSame(expectedFalse, actual);
		// alphabetical order - should be true
		expected = "a=red&b=blue&d=green";
		Assert.assertEquals(expected, actual);
		
		// Testing with expected parameters
		actual = Utils.mapToQueryString(map, UTF8);
		// order by input order to the map - this is false
		expectedFalse = "b=blue&a=red&d=green";
		Assert.assertNotSame(expectedFalse, actual);
		// alphabetical order - should be true
		expected = "a=red&b=blue&d=green";
		Assert.assertEquals(expected, actual);
		
		EtaSdkTest.logTest(TAG, "MapToQueryString");
		
	}
	
	public static void testRequestToUrlAndQueryString() {
		
		// null test - no request
		String actual = Utils.requestToUrlAndQueryString(null);
		Assert.assertNull(actual);
		
		// null test - no url or parameters
		Request<?> r = new JsonObjectRequest(null, null);
		actual = Utils.requestToUrlAndQueryString(r);
		Assert.assertNull(actual);
		
		// null test - an url but no parameters
		r = new JsonObjectRequest("http://eta.dk/", null);
		String expected = "http://eta.dk/";
		actual = Utils.requestToUrlAndQueryString(r);
		Assert.assertEquals(expected, actual);
		
		Map<String, String> map = new HashMap<String, String>();
		map.put("b", "blue");
		map.put("a", "red");
		map.put("d", "green");
		r.getParameters().putAll(map);
		
		expected = "http://eta.dk/?a=red&b=blue&d=green";
		actual = Utils.requestToUrlAndQueryString(r);
		Assert.assertEquals(expected, actual);
		
		EtaSdkTest.logTest(TAG, "RequestToUrlAndQueryString");
	}

	public static void testIsSuccess() {
		// 200 <= statusCode && statusCode < 300 || statusCode == 304
		

		for (int i = 200; i < 300; i++) {
			Assert.assertTrue(Utils.isSuccess(i));
		}
		Assert.assertTrue(Utils.isSuccess(304));
		
		Assert.assertFalse(Utils.isSuccess(Integer.MIN_VALUE));
		for (int i = -1; i < 200; i++) {
			Assert.assertFalse(Utils.isSuccess(i));
		}
		for (int i = 300; i < 304; i++) {
			Assert.assertFalse(Utils.isSuccess(i));
		}
		for (int i = 305; i < 600; i++) {
			Assert.assertFalse(Utils.isSuccess(i));
		}
		Assert.assertFalse(Utils.isSuccess(Integer.MAX_VALUE));
		
		EtaSdkTest.logTest(TAG, "IsGenderValid");
	}
	
	public static void testStringToDate() {

		// TODO How do we handle date tests
		
		// "yyyy-MM-dd'T'HH:mm:ssZZZZ"
		
		String[] valid = {
				"1985-02-13T02:34:00+0000",
				"2100-03-03T13:37:00+0000",
				"2013-03-03T13:37:00+0000"
				};

		String[] invalid = {
				null,
				"",
				"danny",
				"1970",
				"1970-",
				"1970-00-01T00:00:00+0000",
				"1970-01-01T00:00:00",
				"1970-01-01T00:00:00-0000",
				"1970-01-01t00:00:00+0000",
				"0000-00-00T00:00:00+0000",
				"0000-00-00T00:00:00+0000",
				"9999-99-99T99:99:99+9999"
				};
		
		Date epoch = new Date(0);
		
		// Epoch test is separate because in false case stringToDate-method returns epoch
		Date actual = Utils.stringToDate("1970-01-01T00:00:00+0000"); 
		Assert.assertEquals(epoch, actual);
		
		Calendar c = GregorianCalendar.getInstance();
		// time-zone and day-light-saving offset in milliseconds
		int offset = (c.get(Calendar.ZONE_OFFSET) + c.get(Calendar.DST_OFFSET) );
		
		for (String s : valid) {
			actual = Utils.stringToDate(s);
			Assert.assertNotSame(epoch, actual);
			String tmp = Utils.dateToString(new Date(actual.getTime()-offset));
			EtaLog.d(TAG, "in:  " + s);
			EtaLog.d(TAG, "out: " + tmp);
			Assert.assertEquals(s, tmp);
		}
		
		for (String s : invalid) {
			actual = Utils.stringToDate(s);
			Assert.assertEquals(epoch, actual);
		}

		EtaSdkTest.logTest(TAG, "StringToDate");
	}
	
	public static void testDateToString() {
		
		// TODO How do we handle date tests
		
		Calendar c = GregorianCalendar.getInstance();
		
		String actual = Utils.dateToString(null);
		Assert.assertEquals("1970-01-01T00:00:00+0000", actual);

		actual = Utils.dateToString(new Date(0));
		Assert.assertEquals("1970-01-01T00:00:00+0000", actual);
		
		c.set(1970, 1, 1, 0, 0, 0);
		actual = Utils.dateToString(c.getTime());
		Assert.assertEquals("1970-01-01T00:00:00+0000", actual);

		c.set(2013, 03, 03, 13, 37, 00);
		actual = Utils.dateToString(c.getTime());
		Assert.assertEquals("2013-03-03T13:37:00+0000", actual);
		
		c.set(2100, 12, 12, 23, 59, 59);
		actual = Utils.dateToString(c.getTime());
		Assert.assertEquals("2100-12-12T23:59:59+0000", actual);

		c.set(2000, 1, 1, 1, 1, 1);
		actual = Utils.dateToString(c.getTime());
		Assert.assertNotSame("2100-12-12T23:59:59+0000", actual);

		EtaSdkTest.logTest(TAG, "DateToString");
	}

}
