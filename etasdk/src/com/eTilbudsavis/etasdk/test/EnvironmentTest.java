package com.eTilbudsavis.etasdk.test;

import junit.framework.Assert;
import junit.framework.TestCase;

import com.eTilbudsavis.etasdk.Constants;
import com.eTilbudsavis.etasdk.utils.Api.Environment;
import com.eTilbudsavis.etasdk.utils.Validator;

public class EnvironmentTest extends TestCase {
	
	public static final String TAG = Constants.getTag(EnvironmentTest.class);
	// TODO override all appropriate methods
	
	public static void test() {
		
		EtaSdkTest.start(TAG);
		testApply();
		testFromString();
		
	}

	public static void testApply() {
		
		String path = "/some/path";
		String noSlashPath = "some/path";
		String production = "https://api.etilbudsavis.dk/";

		String etaApiUrl = "https://api.etilbudsavis.dk/some/path";
		String httpUrl = "http://www.jubii.dk/";
		String httpsUrl = "https://www.jubii.dk/";
		
		Environment e = Environment.PRODUCTION;
		
		// null 
		String nul = e.apply(null);
		assertEquals(production, nul);
		
		// empty
		String empty = e.apply("");
		assertEquals(production, empty);

		// non prefixed path
		String noSlash = e.apply(noSlashPath);
		assertEquals(etaApiUrl, noSlash);

		// full eTilbudsavis url
		String fullBuild = e.apply(etaApiUrl);
		assertEquals(etaApiUrl, fullBuild);

		// Any http prefixed url
		String http = e.apply(httpUrl);
		assertEquals(httpUrl, http);

		// Any https prefixed url
		String https = e.apply(httpsUrl);
		assertEquals(httpsUrl, https);
		
		// Standard case
		String standard = e.apply(path);
		assertEquals(etaApiUrl, standard);
		
		EtaSdkTest.logTest(TAG, "Apply");
		
		
		
	}

	public static void testFromString() {
		
		String prod = "https://api.etilbudsavis.dk";
		String edge = "https://edge.api.etilbudsavis.dk";
		String staging = "https://staging.api.etilbudsavis.dk";

		// enums must match them selves
		String prod1 = Environment.PRODUCTION.toString();
		assertEquals(Environment.PRODUCTION, Environment.fromString(prod1));
		
		String edge1 = Environment.EDGE.toString();
		assertEquals(Environment.EDGE, Environment.fromString(edge1));
		
		String staging1 = Environment.STAGING.toString();
		assertEquals(Environment.STAGING, Environment.fromString(staging1));
		
		// Some sane defaults on the custom environment
		assertEquals(Environment.CUSTOM, Environment.fromString(null));
		assertEquals(Environment.PRODUCTION.toString(), Environment.CUSTOM.toString());
		assertEquals(Environment.CUSTOM, Environment.fromString(""));
		assertEquals(Environment.PRODUCTION.toString(), Environment.CUSTOM.toString());
		
		// But allow pretty much any thing
		assertEquals(Environment.CUSTOM, Environment.fromString("random"));
		assertEquals(Environment.CUSTOM, Environment.fromString("https://api.etilbudsavis.d"));
		assertEquals(Environment.CUSTOM, Environment.fromString("https://api.etilbudsavis.dk/"));
		assertEquals(Environment.CUSTOM, Environment.fromString("https://etilbudsavis.dk/"));
		
		// Only one custon environment allowed
		String first_url = "first_url";
		String second_url = "second_url";
		
		Environment custom = Environment.fromString(first_url);
		
		// Now custom is a instance of Environment.CUSTOM, and has the string value of "first_url" (both custom, and CUSTOM)
		assertTrue(custom == Environment.CUSTOM);
		assertEquals(first_url, custom.toString());
		assertEquals(first_url, Environment.CUSTOM.toString());
		
		// Lets change Environment.CUSTOM 
		Environment.fromString(second_url);
		
		// custom (instance of CUSTOM) now has a different value - as only one CUSTOM environment is allowed
		assertNotSame(first_url, custom.toString());
		
		// But both CUSTOM, and custom matches "second_url"
		assertEquals(second_url, Environment.CUSTOM.toString());
		assertEquals(second_url, custom.toString());
		
		// Any string matching an environment must return the environment
		assertEquals(Environment.PRODUCTION, Environment.fromString(prod));
		assertEquals(Environment.EDGE, Environment.fromString(edge));
		assertEquals(Environment.STAGING, Environment.fromString(staging));

		assertNotSame(Environment.PRODUCTION, Environment.EDGE);
		assertNotSame(Environment.PRODUCTION, Environment.STAGING);
		assertNotSame(Environment.PRODUCTION, Environment.CUSTOM);
		assertNotSame(Environment.EDGE, Environment.STAGING);
		assertNotSame(Environment.EDGE, Environment.CUSTOM);
		assertNotSame(Environment.STAGING, Environment.CUSTOM);
		
		EtaSdkTest.logTest(TAG, "FromString");
		
	}

	public static void testValidVersion() {
		
		String[] valid = { "2.0.0", "2.0.0-rc.2", "2.0.0-rc.1", "1.0.0", "1.0.0-beta", 
				"1.0.0-b", "1.0.0-beta", "1.0.0-chocolate", "1.0.0-15948", "1.0.0-rc-1" };
		
		
		// Out regex haven't covered the case "1.0.0-" 
		String[] invalid = { null, "jens", "", ".", "v", "v.1", "v1.0", "v1.0.0",
				"1.", "1.0", "1.0.", "1.0.0.0", "v1.0.0-beta.2.2", "v1-beta" };
		
		for (String s : valid) {
			assertTrue(Validator.isAppVersionValid(s));
		}

		for (String s : invalid) {
			assertFalse(Validator.isAppVersionValid(s));
		}

		EtaSdkTest.logTest(TAG, "ValidVersion");
	}

	
}
