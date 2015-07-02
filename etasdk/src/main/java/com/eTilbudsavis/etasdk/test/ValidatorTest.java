package com.eTilbudsavis.etasdk.test;

import com.eTilbudsavis.etasdk.Constants;
import com.eTilbudsavis.etasdk.utils.Validator;

import junit.framework.Assert;
import junit.framework.TestCase;

public class ValidatorTest extends TestCase {

    public static final String TAG = Constants.getTag(ValidatorTest.class);
    // TODO override all appropriate methods


    public static void test() {

        EtaSdkTest.start(TAG);
        testIsEmailVaild();
        testIsBirthYearValid();
        testIsGenderValid();
        testValidVersion();

    }

    public static void testIsEmailVaild() {

        String[] inValid = new String[]{null, "", "@", "@@", "d@@h", "d@k@h", "@hvam", " @ ", "null", "danny@", " d@h.dk ", "d@h.dk ", " d@h.dk"};
        for (String s : inValid) {
            assertFalse(Validator.isEmailValid(s));
        }

        String[] valid = new String[]{"danny@hvam.dk", "d@h", "d @ h", "d@h.dk", "4321@fdsafd", "[]{}()@[]{}()", "∂∑˙∆´∫∑@˙¨ˆ∂∑∆"};
        for (String s : valid) {
            assertTrue(Validator.isEmailValid(s));
        }

        EtaSdkTest.logTest(TAG, "IsEmailValid");
    }

    public static void testIsBirthYearValid() {
        Assert.assertFalse(Validator.isBirthyearValid(Integer.MIN_VALUE));
        for (int i = -1000; i < 1900; i++) {
            Assert.assertFalse(Validator.isBirthyearValid(i));
        }
        for (int i = 1900; i < 2016; i++) {
            Assert.assertTrue(Validator.isBirthyearValid(i));
        }
        for (int i = 2016; i < 3000; i++) {
            Assert.assertFalse(Validator.isBirthyearValid(i));
        }
        Assert.assertFalse(Validator.isBirthyearValid(Integer.MAX_VALUE));

        EtaSdkTest.logTest(TAG, "IsBirthYesrValid");
    }

    public static void testIsGenderValid() {

        String[] inValid = new String[]{null, "", "danny", "fe male", "  ma le  ", "males", "females"};
        for (String s : inValid) {
            assertFalse(Validator.isGenderValid(s));
        }

        String[] valid = new String[]{"  male  ", "  female  ", "female", "male"};
        for (String s : valid) {
            assertTrue(Validator.isGenderValid(s));
        }

        EtaSdkTest.logTest(TAG, "IsGenderValid");
    }

    public static void testValidVersion() {

        String[] valid = {"2.0.0", "2.0.0-rc.2", "2.0.0-rc.1", "1.0.0", "1.0.0-beta",
                "1.0.0-b", "1.0.0-beta", "1.0.0-chocolate", "1.0.0-15948", "1.0.0-rc-1"};


        // Out regex haven't covered the case "1.0.0-"
        String[] invalid = {null, "jens", "", ".", "v", "v.1", "v1.0", "v1.0.0",
                "1.", "1.0", "1.0.", "1.0.0.0", "v1.0.0-beta.2.2", "v1-beta"};

        for (String s : valid) {
            Assert.assertTrue(Validator.isAppVersionValid(s));
        }

        for (String s : invalid) {
            Assert.assertFalse(Validator.isAppVersionValid(s));
        }

        EtaSdkTest.logTest(TAG, "ValidVersion");
    }


}
