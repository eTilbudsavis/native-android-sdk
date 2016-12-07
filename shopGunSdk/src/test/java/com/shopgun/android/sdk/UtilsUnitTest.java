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


import com.shopgun.android.sdk.log.SgnLog;
import com.shopgun.android.sdk.model.Si;
import com.shopgun.android.sdk.network.Request;
import com.shopgun.android.sdk.network.impl.JsonObjectRequest;
import com.shopgun.android.sdk.utils.Constants;
import com.shopgun.android.sdk.utils.SgnUtils;
import com.shopgun.android.utils.ParcelableUtils;

import junit.framework.Assert;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RunWith(RobolectricTestRunner.class)
@Config(manifest=Config.NONE)
public class UtilsUnitTest {

    public static final String TAG = Constants.getTag(UtilsUnitTest.class);

    public static final String REGEX_UUID = "[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}";

    @Test
    public void testCreateUUID() throws Exception {

        String notUuid = "this should not match the REGEX_UUID";
        Assert.assertFalse(notUuid.matches(REGEX_UUID));

        String uuid = SgnUtils.createUUID();
        Assert.assertTrue(uuid.matches(REGEX_UUID));

        String otherUuid = SgnUtils.createUUID();
        Assert.assertTrue(otherUuid.matches(REGEX_UUID));

        Assert.assertNotSame(uuid, otherUuid); // They can theoretically be equal

        // replacing [0-9a-f] with any other char in [0-9a-f] shouldn't matter
        otherUuid = otherUuid.replace("a", "2").replace("b", "2").replace("c", "2").replace("d", "2").replace("e", "2").replace("f", "2");
        Assert.assertTrue(otherUuid.matches(REGEX_UUID));

        // G is not a valid char in a UUID
        String uuidFake = "83g24023-3225-4392-9362-332619620638";
        Assert.assertFalse(uuidFake.matches(REGEX_UUID));

    }

    @Test
    public void testMapToQueryString() throws Exception {

        String UTF8 = "utf-8";
        String expected = "";

        // null test
        String actual = SgnUtils.mapToQueryString(null, null);
        Assert.assertEquals(expected, actual);

        actual = SgnUtils.mapToQueryString(null, UTF8);
        Assert.assertEquals(expected, actual);

        // empty map test
        Map<String, String> map = new HashMap<String, String>();
        actual = SgnUtils.mapToQueryString(map, UTF8);
        Assert.assertEquals(expected, actual);

        // fill the map
        map.put("b", "blue");
        map.put("a", "red");
        map.put("d", "green");

        // Creating incorrect encoding
        actual = SgnUtils.mapToQueryString(map, null);
        // order by input order to the map - this is false
        String expectedFalse = "b=blue&a=red&d=green";
        Assert.assertNotSame(expectedFalse, actual);
        // alphabetical order - should be true
        expected = "a=red&b=blue&d=green";
        Assert.assertEquals(expected, actual);

        // Creating incorrect encoding
        actual = SgnUtils.mapToQueryString(map, "not utf-8");
        // order by input order to the map - this is false
        expectedFalse = "b=blue&a=red&d=green";
        Assert.assertNotSame(expectedFalse, actual);
        // alphabetical order - should be true
        expected = "a=red&b=blue&d=green";
        Assert.assertEquals(expected, actual);

        // Testing with expected parameters
        actual = SgnUtils.mapToQueryString(map, UTF8);
        // order by input order to the map - this is false
        expectedFalse = "b=blue&a=red&d=green";
        Assert.assertNotSame(expectedFalse, actual);
        // alphabetical order - should be true
        expected = "a=red&b=blue&d=green";
        Assert.assertEquals(expected, actual);

    }

    @Test
    public void testRequestToUrlAndQueryString() throws Exception {

        // null test - no request
        String actual = SgnUtils.requestToUrlAndQueryString(null);
        Assert.assertNull(actual);

        // null test - no url or parameters
        Request<?> r = new JsonObjectRequest(null, null);
        actual = SgnUtils.requestToUrlAndQueryString(r);
        Assert.assertNull(actual);

        // null test - an url but no parameters
        r = new JsonObjectRequest("http://eta.dk/", null);
        String expected = "http://eta.dk/";
        actual = SgnUtils.requestToUrlAndQueryString(r);
        Assert.assertEquals(expected, actual);

        Map<String, String> map = new HashMap<String, String>();
        map.put("b", "blue");
        map.put("a", "red");
        map.put("d", "green");
        r.getParameters().putAll(map);

        expected = "http://eta.dk/?a=red&b=blue&d=green";
        actual = SgnUtils.requestToUrlAndQueryString(r);
        Assert.assertEquals(expected, actual);

    }

    @Test
    public void testIsSuccess() throws Exception {
        // 200 <= statusCode && statusCode < 300 || statusCode == 304


        for (int i = 200; i < 300; i++) {
            Assert.assertTrue(SgnUtils.isSuccess(i));
        }
        Assert.assertTrue(SgnUtils.isSuccess(304));

        Assert.assertFalse(SgnUtils.isSuccess(Integer.MIN_VALUE));
        for (int i = -1; i < 200; i++) {
            Assert.assertFalse(SgnUtils.isSuccess(i));
        }
        for (int i = 300; i < 304; i++) {
            Assert.assertFalse(SgnUtils.isSuccess(i));
        }
        for (int i = 305; i < 600; i++) {
            Assert.assertFalse(SgnUtils.isSuccess(i));
        }
        Assert.assertFalse(SgnUtils.isSuccess(Integer.MAX_VALUE));

    }

    @Ignore("UtilsUnitTest.StringToDate test not ready yet")
    @Test
    public void testStringToDate() throws Exception {

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
        Date actual = SgnUtils.stringToDate("1970-01-01T00:00:00+0000");
        Assert.assertEquals(epoch, actual);

        Calendar c = GregorianCalendar.getInstance();
        // time-zone and day-light-saving offset in milliseconds
        int offset = (c.get(Calendar.ZONE_OFFSET) + c.get(Calendar.DST_OFFSET));

        for (String s : valid) {
            actual = SgnUtils.stringToDate(s);
            Assert.assertNotSame(epoch, actual);
            String tmp = SgnUtils.dateToString(new Date(actual.getTime() - offset));
            SgnLog.d(TAG, "in:  " + s);
            SgnLog.d(TAG, "out: " + tmp);
            Assert.assertEquals(s, tmp);
        }

        for (String s : invalid) {
            actual = SgnUtils.stringToDate(s);
            Assert.assertEquals(epoch, actual);
        }

    }

    @Ignore("UtilsUnitTest.DateToString test not ready yet")
    @Test
    public void testDateToString() throws Exception {

        // TODO How do we handle date tests

        Calendar c = GregorianCalendar.getInstance();

        String actual = SgnUtils.dateToString(null);
        Assert.assertEquals("1970-01-01T00:00:00+0000", actual);

        actual = SgnUtils.dateToString(new Date(0));
        Assert.assertEquals("1970-01-01T00:00:00+0000", actual);

        c.set(1970, 1, 1, 0, 0, 0);
        actual = SgnUtils.dateToString(c.getTime());
        Assert.assertEquals("1970-01-01T00:00:00+0000", actual);

        c.set(2013, 3, 3, 13, 37, 0);
        actual = SgnUtils.dateToString(c.getTime());
        Assert.assertEquals("2013-03-03T13:37:00+0000", actual);

        c.set(2100, 12, 12, 23, 59, 59);
        actual = SgnUtils.dateToString(c.getTime());
        Assert.assertEquals("2100-12-12T23:59:59+0000", actual);

        c.set(2000, 1, 1, 1, 1, 1);
        actual = SgnUtils.dateToString(c.getTime());
        Assert.assertNotSame("2100-12-12T23:59:59+0000", actual);

    }

    @Test
    public void testCopyParcelable() throws Exception {

		/*
		 * We are assuming (at this point) that Si class is okay.
		 * Si will be unit testet later, so don't worry
		 */
        Si obj = new Si();
        Si copy = ParcelableUtils.copyParcelable(obj, Si.CREATOR);
        Assert.assertNotSame(obj, null);
        Assert.assertNotSame(obj, copy);
        Assert.assertEquals(obj, copy);

        copy = ParcelableUtils.copyParcelable(obj, Si.CREATOR);
        // They must not refer to the same object (as it's been deep copied)
        Assert.assertNotSame(obj, copy);
        // But they must be equal
        Assert.assertEquals(obj, copy);
        obj.setSymbol("USD");
        Assert.assertNotSame(obj, copy);
        Assert.assertFalse(obj.equals(copy));

        copy = ParcelableUtils.copyParcelable(obj, Si.CREATOR);
        // They must not refer to the same object (as it's been deep copied)
        Assert.assertNotSame(obj, copy);
        // But they must be equal
        Assert.assertEquals(obj, copy);

        List<Si> list = new ArrayList<Si>();
        list.add(new Si());
        list.add(new Si());
        list.add(new Si());

        List<Si> listCopy = ParcelableUtils.copyParcelable(list, Si.CREATOR);

    }

}
