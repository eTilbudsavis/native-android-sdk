package com.eTilbudsavis.etasdk.test;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import android.graphics.Color;

import com.eTilbudsavis.etasdk.EtaObjects.Country;
import com.eTilbudsavis.etasdk.EtaObjects.Offer;
import com.eTilbudsavis.etasdk.EtaObjects.Share;
import com.eTilbudsavis.etasdk.EtaObjects.Store;
import com.eTilbudsavis.etasdk.EtaObjects.User;
import com.eTilbudsavis.etasdk.EtaObjects.helper.Branding;
import com.eTilbudsavis.etasdk.EtaObjects.helper.Dimension;
import com.eTilbudsavis.etasdk.EtaObjects.helper.Hotspot;
import com.eTilbudsavis.etasdk.EtaObjects.helper.HotspotMap;
import com.eTilbudsavis.etasdk.EtaObjects.helper.Images;
import com.eTilbudsavis.etasdk.EtaObjects.helper.Links;
import com.eTilbudsavis.etasdk.EtaObjects.helper.Pageflip;
import com.eTilbudsavis.etasdk.EtaObjects.helper.Permission;
import com.eTilbudsavis.etasdk.EtaObjects.helper.Pieces;
import com.eTilbudsavis.etasdk.EtaObjects.helper.Pricing;
import com.eTilbudsavis.etasdk.EtaObjects.helper.Quantity;
import com.eTilbudsavis.etasdk.EtaObjects.helper.Si;
import com.eTilbudsavis.etasdk.EtaObjects.helper.Size;
import com.eTilbudsavis.etasdk.EtaObjects.helper.Subscription;
import com.eTilbudsavis.etasdk.EtaObjects.helper.Typeahead;
import com.eTilbudsavis.etasdk.EtaObjects.helper.Unit;
import com.eTilbudsavis.etasdk.Utils.Utils;

public class ObjectCreator {

	private static String getUrl(String id, String path) {
		return String.format("https://eta.dk/%s/%s", id, path);
	}

	private static String getUrl(String id) {
		return String.format("https://eta.dk/%s", id);
	}
	
	private static String getID(String type, String id) {
		return String.format("%s-%s", type, id);
	}

	public static Country getCountry() {
		return getCountry("89azf82");
	}
	
	public static Country getCountry(String id) {
		Country c = new Country();
		c.setId(id);
		c.setUnsubscribePrintUrl(getUrl(id));
		return c;
	}

	public static Store getStore() {
		return getStore("fds893r");
	}
	
	public static Store getStore(String id) {
		Store s = new Store();
		s.setBranding(getBranding());
		s.setCity("fake-city");
		s.setContact("fake-email@fake.com");
		s.setCountry(getCountry(id));
		String dealerId = getID("dealer", id);
		s.setDealerId(dealerId);
		s.setDealerUrl(getUrl(dealerId));
		s.setId(id);
		s.setLatitude(12.5d);
		s.setLongitude(9.5d);
		s.setStreet("fake-street");
		s.setZipcode("fake-zipcode");
		return s;
	}

	public static Share getShare() {
		return getShare("fake-mail@eta.dk", Share.ACCESS_OWNER, "fake-accept-url");
	}
	
	public static Share getShare(String email, String access, String acceptUrl) {
		Share s = new Share(email, access, acceptUrl);
		return s;
	}

	public static User getUser() {
		return getUser(1932, "fake-mail@eta.dk", "female", "fake-user-name", getPermission(), 1992);
	}
	
	public static User getUser(int year, String email, String gender, String name, Permission permissions, int id) {
		User u = new User();
		u.setBirthYear(year);
		u.setEmail(email);
		u.setGender(gender);
		u.setName(name);
		u.setPermissions(permissions);
		u.setUserId(id);
		return u;
	}

	public static Branding getBranding() {
		return getBranding(Color.GREEN, "fake-logo-url", Color.GREEN, "fake-branding-name", 
				getPageflip(), "fake-url-name", "fake-website-url");
	}
	
	public static Branding getBranding(int color, String logoUrl, int logoBgColor, String name, 
			Pageflip pageflip, String urlName, String website) {
		Branding b = new Branding();
		b.setColor(color);
		b.setLogo(logoUrl);
		b.setLogoBackground(logoBgColor);
		b.setName(name);
		b.setPageflip(pageflip);
		b.setUrlName(urlName);
		b.setWebsite(website);
		return b;
	}

	public static Dimension getDimension() {
		return getDimension(1.30d, 1.0d);
	}

	public static Dimension getDimension(double height, double width) {
		Dimension d = new Dimension();
		d.setHeight(height);
		d.setWidth(width);
		return d;
	}

	public static Hotspot getHotspot() {
		return getHotspot(0, 100, 100, 0);
	}

	public static Hotspot getHotspot(int top, int right, int bottom, int left) {
		Hotspot h = new Hotspot();
		h.mAbsBottom = bottom;
		h.mAbsTop = top;
		h.mAbsLeft = left;
		h.mAbsRight = right;
		return h;
	}
	
	public static HotspotMap getHotspotMap() {
		return getHotspotMap("fake-logo-url", Color.GREEN);
	}
	
	public static HotspotMap getHotspotMap(String logoUrl, int color) {
		HotspotMap h = new HotspotMap();
		List<Hotspot> list = new ArrayList<Hotspot>();
		for (int i = 0; i < 10; i++) {
			list.add(getHotspot(0+i, 100+i, 100+i, 0+i));
		}
		h.put(1, list);
		return h;
	}

	public static Pageflip getPageflip() {
		return getPageflip("fake-logo-url", Color.GREEN);
	}
	
	public static Pageflip getPageflip(String logoUrl, int color) {
		Pageflip p = new Pageflip();
		p.setLogo(logoUrl);
		p.setColor(color);
		return p;
	}

	public static Permission getPermission() {
		
		Permission p = new Permission();
		
		String group = "group";
		ArrayList<String> groupPermissions = new ArrayList<String>();
		groupPermissions.add("group-permission-read");
		groupPermissions.add("group-permission-write");
		p.put(group, groupPermissions);
		
		String user = "user";
		ArrayList<String> permissions = new ArrayList<String>();
		permissions.add("user-permission-read");
		permissions.add("user-permission-write");
		HashMap<String, ArrayList<String>> userPermissions = new HashMap<String, ArrayList<String>>();
		userPermissions.put(user, permissions);
		p.putAll(userPermissions);
		
		return p;
	}

	public static Typeahead getTypeahead() {
		return getTypeahead("fake-subject", 0, 3);
	}
	
	public static Typeahead getTypeahead(String subject, int offset, int length) {
		Typeahead t = new Typeahead();
		t.setSubject(subject);
		t.setOffset(offset);
		t.setLength(length);
		return t;
	}
	
	public static Images getImages() {
		return getImages("fake-id");
	}
	
	public static Images getImages(String id) {
		Images i = new Images();
		i.setThumb(getUrl(id, "thumb"));
		i.setView(getUrl(id, "view"));
		i.setZoom(getUrl(id, "zoom"));
		return i;
		
	}
	
	public static Links getLinks() {
		return getLinks("fake-id");
	}
	
	public static Links getLinks(String id) {
		Links l = new Links();
		l.setWebshop(getUrl(id, "webshop"));
		return l;
	}

	public static Pricing getPricing() {
		return getPricing("DKK", 20.00d, 15.00d);
	}
	
	public static Pricing getPricing(String currency, double prePrice, double price) {
		Pricing p = new Pricing();
		p.setCurrency(currency);
		p.setPrePrice(prePrice);
		p.setPrice(price);
		return p;
	}
	
	public static Pieces getPieces() {
		return getPieces(1, 1);
	}
	
	public static Pieces getPieces(int from, int to) {
		Pieces p = new Pieces();
		p.setFrom(from);
		p.setTo(to);
		return p;
	}


	public static Si getSi() {
		return getSi("FOOBAR", 1.0d);
	}
	
	public static Si getSi(String symbol, double factor) {
		Si s = new Si();
		s.setFactor(factor);
		s.setSymbol(symbol);
		return s;
	}

	public static Unit getUnit() {
		return getUnit("fake-symbol", getSi());
	}
	
	public static Unit getUnit(String symbol, Si si) {
		Unit u = new Unit();
		u.setSi(si);
		u.setSymbol(symbol);
		return u;
	}

	public static Size getSize() {
		return getSize(1.0d, 1.0d);
	}
	
	public static Size getSize(double from, double to) {
		Size s = new Size();
		s.setFrom(from);
		s.setTo(to);
		return s;
	}
	
	public static Subscription getSubscription() {
		return getSubscription("fake-dealer", true);
	}
	
	public static Subscription getSubscription(String dealerId, boolean subscribed) {
		Subscription s = new Subscription();
		s.setDealerId(dealerId);
		s.setSubscribed(subscribed);
		return s;
	}
	
	public static Quantity getQuantity() {
		return getQuantity(getPieces(), getUnit(), getSize());
	}
	
	public static Quantity getQuantity(Pieces pieces, Unit unit, Size size) {
		Quantity q = new Quantity();
		q.setPieces(pieces);
		q.setUnit(unit);
		q.setSize(size);
		return q;
	}
	
	public static Offer getOffer() {
		String name = "Chicken Soup";
		String id = "32abf85";
		String description = "The great taste of homemade chicken soup.";
		return getOffer(name, description, id);
	}
	
	public static Offer getOffer(String name, String description, String id) {
		
		Offer o = new Offer();
		String offerId = getID("offer", id);
		o.setId(offerId);
		o.setHeading(name);
		o.setDescription(description);
		o.setCatalogPage(32);
		o.setImages(getImages(offerId));
		o.setLinks(getLinks(offerId));
		o.setPricing(getPricing());
		o.setQuantity(getQuantity());
		
		long now = System.currentTimeMillis();
		o.setRunFrom(new Date(now - (Utils.DAY_IN_MILLIS*3)));
		o.setRunTill(new Date(now + (Utils.DAY_IN_MILLIS*3)));
		
		String catalogId = getID("catalog", id);
		o.setCatalogId(catalogId);
		o.setCatalogUrl(getUrl(catalogId));
		
		String dealerId = getID("dealer", id);
		o.setDealerId(dealerId);
		o.setDealerUrl(getUrl(dealerId));
		
		String storeId = getID("store", id);
		o.setStoreId(storeId);
		o.setStoreUrl(getUrl(storeId));
		
		return o;
	}
	
}
