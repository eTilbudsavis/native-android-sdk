package com.eTilbudsavis.etasdk.test;

import java.util.Date;

import com.eTilbudsavis.etasdk.EtaObjects.Offer;
import com.eTilbudsavis.etasdk.EtaObjects.helper.Images;
import com.eTilbudsavis.etasdk.EtaObjects.helper.Links;
import com.eTilbudsavis.etasdk.EtaObjects.helper.Pieces;
import com.eTilbudsavis.etasdk.EtaObjects.helper.Pricing;
import com.eTilbudsavis.etasdk.EtaObjects.helper.Quantity;
import com.eTilbudsavis.etasdk.EtaObjects.helper.Si;
import com.eTilbudsavis.etasdk.EtaObjects.helper.Size;
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
	
	public static Images getImages(String id) {
		Images i = new Images();
		i.setThumb(getUrl(id, "thumb"));
		i.setView(getUrl(id, "view"));
		i.setZoom(getUrl(id, "zoom"));
		return i;
		
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
		Pieces p = new Pieces();
		p.setFrom(1);
		p.setTo(1);
		return p;
	}

	public static Si getSi() {
		Si s = new Si();
		s.setFactor(1.0d);
		s.setSymbol("FOOBAR");
		return s;
	}

	public static Unit getUnit() {
		Unit u = new Unit();
		u.setSi(getSi());
		u.setSymbol("FOOBAR");
		return u;
	}

	public static Size getSize() {
		Size s = new Size();
		s.setFrom(1.0d);
		s.setTo(1.0d);
		return s;
	}
	
	public static Quantity getQuantity() {
		Quantity q = new Quantity();
		q.setPieces(getPieces());
		q.setUnit(getUnit());
		q.setSize(getSize());
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
