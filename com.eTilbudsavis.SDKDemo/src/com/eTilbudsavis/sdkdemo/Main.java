package com.eTilbudsavis.sdkdemo;

import java.util.ArrayList;
import java.util.Random;

import Utils.Utilities;
import android.app.Activity;
import android.app.backup.RestoreObserver;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.eTilbudsavis.etasdk.Api;
import com.eTilbudsavis.etasdk.Api.CatalogListListener;
import com.eTilbudsavis.etasdk.Api.CatalogListener;
import com.eTilbudsavis.etasdk.Api.DealerListListener;
import com.eTilbudsavis.etasdk.Api.DealerListener;
import com.eTilbudsavis.etasdk.Api.OfferListListener;
import com.eTilbudsavis.etasdk.Api.OfferListener;
import com.eTilbudsavis.etasdk.Api.StoreListListener;
import com.eTilbudsavis.etasdk.Api.StoreListener;
import com.eTilbudsavis.etasdk.Eta;
import com.eTilbudsavis.etasdk.EtaObjects.Catalog;
import com.eTilbudsavis.etasdk.EtaObjects.Dealer;
import com.eTilbudsavis.etasdk.EtaObjects.Offer;
import com.eTilbudsavis.etasdk.EtaObjects.Session.SessionListener;
import com.eTilbudsavis.etasdk.EtaObjects.Store;
import com.etilbudsavis.sdkdemo.R;

public class Main extends Activity {

	public static final String TAG = "Main";
	
	private Eta mEta;
	private String mApiKey = Keys.API_KEY;
	private String mApiSecret = Keys.API_SECRET;
	private LayoutInflater mInflater;
	LinearLayout llMain;
	
	private ArrayList<Catalog> catalogList;
	private ArrayList<Dealer> dealerList;
	private ArrayList<Store> storeList;
	private ArrayList<Offer> offerList;
	
	private int random;
	private String rCatalogId;
	private String rDealerId;
	private String rStoreId;
	private String rOfferId;
	
	int catalogTestIteration;
	int dealerTestIteration;
	int offerTestIteration;
	int storeTestIteration;
	int locationTestIteration = -1;

	String[] queryOffer = new String[]{"cola", "øl", "kød", "DetVirkerIkke", "sovs"};
	String[] queryDealer = new String[]{"seeland", "ilka","bilka",  "netto", "DetVirkerIkke", "Industrivej Syd 1, Birk"};
	String[] queryStore = queryDealer;
	String[] queryStoreQuick = queryDealer;
	
	String tmpIds = "";
	
	long mStart = 0L;
	long mStop = 0L;
	
	long mAccumilatedTime = 0L;
	
	long mStartAll = System.currentTimeMillis();
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        llMain = (LinearLayout)findViewById(R.id.llMain);
        mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        
        mEta = new Eta(mApiKey, mApiSecret, this);
//        mEta.clearPreferences();
        
        addHeader("TESTING SESSTION - not really, not done yet");
        start();
        mEta.getSession().subscribe(new SessionListener() {
			
			@Override
			public void onUpdate() {
				stop();
				addPositive("Session: ", mStop, mEta.getSession().toString(true));
				location.next();
			}
		}).start();
        
    }
    
    private void setNoIds() {
    	// if a list of Catalogs, Dealers, Offers or Stores is empty
    	// then we will continue the test with crap ids, to fake errors
    	String s = "2aabbs3";
    	rCatalogId = s;
    	rDealerId = s;
    	rStoreId = s;
    	rOfferId = s;
    }
    
    private void resetIterations() {
    	int i = -1;
    	
    	catalogTestIteration = i;
    	dealerTestIteration = i;
    	offerTestIteration = i;
    	storeTestIteration = i;

    	catalogList = new ArrayList<Catalog>();
    	dealerList = new ArrayList<Dealer>();
    	storeList = new ArrayList<Store>();
    	offerList = new ArrayList<Offer>();
    	
    	setNoIds();
    	
    }
    
    Test location = new Test() {
		
		@Override
		public void next() {

			locationTestIteration ++;
			
			switch (locationTestIteration) {
			case 0:
				resetIterations(); 
				addHeader("TESTING LOCATION");
				
		        // Herrup, 10km, should give 1 catalog, 4 stores
		        mEta.getLocation().set(56.40875, 8.91922, 10000, false);
				addHeader("HERRUP - 10KM - ~1 catalog, 4 stores, ~100 offers");
				catalogs.next();
				break;
			case 1:
				mEta.getLocation().set(55.63105, 12.5766, 700000, false);	// Fields
				addHeader("FIELDS - 700KM - ~76 catalogs, ~110 stores, ~110 offers");
				resetIterations();
				catalogs.next();
				break;
			case 2:
				mEta.getLocation().set(57.057582, 9.934028, 5000, false);	// Nørresundby
				addHeader("NØRRESUNDBY - 5KM - ~30 catalogs, ~104 stores, ~110 offers");
				resetIterations();
				catalogs.next();
				break;
			case 3:
				// This location should fail on all tests, except dealers, dealer should work
				mEta.getLocation().set(56.436, 11.707, 5, false);	// Kattegat
				addHeader("KATTEGAT - 5M - 0 catalogs, 0 stores, 0 offers");
				resetIterations();
				catalogs.next();
				break;
			case 4:
				mEta.getLocation().set(55.63105, 12.5766, 700000, false);	// Fields
				addHeader("SEARCH TEST");
				resetIterations();
				searchOffers.next();
				break;
			
			default: 
				locationTestIteration = -1; 
				String timeTotal = String.valueOf(System.currentTimeMillis() - mStartAll);
				String timeHttp = String.valueOf(mAccumilatedTime);
				addHeader("TESTING DONE! time: " + timeTotal + "(ms)" + " http: " + timeHttp + "(ms)"); 
				break;
			}
		}
	};

	int searchOffersIT = 0;
	
	Test searchOffers = new Test() {
		
		@Override
		public void next() {
			
			if (searchOffersIT < queryOffer.length) {
				getOfferSearch(searchOffers, searchOffersIT);
				searchOffersIT++;
			} else {
				searchDealer.next();
			}
			
		}
	};

	int searchDealerIT = 0;
	Test searchDealer = new Test() {
		
		@Override
		public void next() {
			
			if (searchDealerIT < queryDealer.length) {
				getDealerSearch(searchDealer, searchDealerIT);
				searchDealerIT++;
			} else {
				searchStore.next();
			}
		}
	};

	int searchStoreIT = 0;
	Test searchStore = new Test() {
		
		@Override
		public void next() {
			
			if (searchStoreIT < queryStore.length) {
				getStoreSearch(searchStore, searchStoreIT);
				searchStoreIT++;
			} else {
				location.next();
			}
		}
	};
	
    Test catalogs = new Test() {
		
		@Override
		public void next() {

			catalogTestIteration ++;
			
			switch (catalogTestIteration) {
			case 0: 
				addHeader("TESTING CATALOGS");
				getAllCatalogs(catalogs);
				break;
			case 1:
				getCatalog(catalogs);
				break;
				
			case 2:
				getCatalogIds(catalogs);
				break;
				
			case 3:
				getDealer(catalogs);
				break;
				
			case 4:
				getStore(catalogs);
				break;
			
			default:
				dealers.next();
				break;
			}

		}
	};

    Test dealers = new Test() {
		
		@Override
		public void next() {

			dealerTestIteration ++;
			
			switch (dealerTestIteration) {
			case 0:
				addHeader("TESTING DEALERS");
				getAllDealers(dealers);
				break;
				
			case 1:
				getDealer(dealers);
				break;
				
			case 2:
				getDealerIds(dealers);
				break;
			
			default:
				stores.next();
				break;
			}
			
		}
	};

    Test stores = new Test() {
		
		@Override
		public void next() {

			storeTestIteration ++;
			
			switch (storeTestIteration) {
			case 0:
				addHeader("TESTING STORES");
				getAllStores(stores);
				break;
				
			case 1:
				getStore(stores);
				break;
				
			case 2:
				getDealer(stores);
				break;
				

			default:
				offers.next();
				break;
			}

		}
	};

    Test offers = new Test() {
		
		@Override
		public void next() {

			offerTestIteration ++;
			
			switch (offerTestIteration) {
			case 0: addHeader("TESTING OFFERS"); this.next(); break;
			case 1: getAllOffers(offers); break;
			case 2: getDealer(offers); break;
			case 3: getStore(offers); break;
			case 4: getCatalog(offers); break;
			case 5: getOffer(offers); break;
			case 6: getOfferIds(offers); break;
			
			default: location.next(); break;
			}

		}
	};
    
    private void start(){
    	mStart = System.currentTimeMillis();
    }
    
    private void stop() {
		mStop = System.currentTimeMillis() - mStart;
		mAccumilatedTime += mStop;
    }

    private void addPositive(String testName, String id, long time, String body) {
    	addPositive(testName + ": " + id, time, body);
    }

    private void addPositive(String testName, long time, String body) {
    	LinearLayout llCon = (LinearLayout)mInflater.inflate(R.layout.test_layout, null);
    	TextView tvHead = (TextView)llCon.findViewById(R.id.tvHeader);
    	TextView tvBody = (TextView)llCon.findViewById(R.id.tvBody);
    	TextView tvTime = (TextView)llCon.findViewById(R.id.tvTime);
    	tvHead.setText(testName);
    	tvTime.append(String.valueOf(time));
    	tvBody.setText(body);
    	llMain.addView(llCon);
    	tvBody.requestFocus();
    }

    private void addNegative(String testName, String id, int code, Object object) {
    	addNegative(testName + ": " + id, code, object);
    }

    private void addNegative(String testName, int code, Object object) {
    	TextView t = new TextView(getApplicationContext());
    	t.setBackgroundColor(Color.parseColor("#FF0000"));
    	t.setText(testName);
    	llMain.addView(t);
    	TextView i = new TextView(getApplicationContext());
    	i.setText(String.valueOf(code + ": " + object.toString()));
    	llMain.addView(i);
    }

    private void addHeader(String name) {
    	TextView t = new TextView(getApplicationContext());
    	t.setText(name);
    	t.setBackgroundColor(Color.parseColor("#0000FF"));
    	t.setTextAppearance(getApplicationContext(), android.R.style.TextAppearance_Medium);
    	llMain.addView(t);
    }
    
    private String printList(ArrayList<? extends Object> list) {
    	StringBuilder sb = new StringBuilder();
		for (Object o : list)
			sb.append("- ").append(o.toString()).append("\n");
		
		return sb.toString();
    }
    
    private String printCount(ArrayList<? extends Object> list) {
		return new StringBuilder().append("Count: ").append(list.size()).toString();
    }
    
    private void getAllCatalogs(final Test t) {
    	start();
    	mEta.getCatalogList(new CatalogListListener() {
    		
    		@Override
    		public void onComplete(int statusCode, Object object) {
    			stop();
    			if (statusCode == 200) {
    				@SuppressWarnings("unchecked")
    				ArrayList<Catalog> tmp = (ArrayList<Catalog>)object;
    				catalogList.addAll(tmp);
    				if (tmp.size() == Api.DEFAULT_LIMIT) {
    					getAllCatalogs(t);
    				} else {
    					addPositive("Catalog List", mStop, printCount(catalogList));
    					if (catalogList.size() > 0) {
        			    	random = new Random().nextInt(catalogList.size() );
        			    	rCatalogId = catalogList.get(random).getId();
        			    	rDealerId = catalogList.get(random).getDealerId();
        			    	rStoreId = catalogList.get(random).getStoreId();    						
    					} else {
    						setNoIds();
    					}
    					t.next();
    				}
    			} else {
					addNegative("Catalog List", statusCode, object);
					t.next();
				}
    		}
    	}, catalogList.size(), new String[] {Catalog.SORT_NAME}).execute();
    	
    }

    private void getAllDealers(final Test t) {
    	start();
    	mEta.getDealerList(new DealerListListener() {
			
			@Override
			public void onComplete(int statusCode, Object object) {
				stop();
    			if (statusCode == 200) {
    				@SuppressWarnings("unchecked")
    				ArrayList<Dealer> tmp = (ArrayList<Dealer>)object;
    				dealerList.addAll(tmp);
    				if (tmp.size() == Api.DEFAULT_LIMIT) {
    					getAllDealers(t);
    				} else {
    					addPositive("Complete Dealer List", mStop, printCount(dealerList));
    					if (dealerList.size() > 0) {
	    			    	random = new Random().nextInt(dealerList.size());
	    			    	rDealerId = dealerList.get(random).getId();
    					} else {
    						setNoIds();
    					}
    					t.next();
    				}
    			}  else {
					addNegative("Dealer List", statusCode, object);
					t.next();
				}
			}
		}, dealerList.size()).execute();
    	
    }

    private void getAllStores(final Test t) {
    	start();
    	mEta.getStoreList(new StoreListListener() {
			
			@Override
			public void onComplete(int statusCode, Object object) {
				stop();
    			if (statusCode == 200) {
    				@SuppressWarnings("unchecked")
    				ArrayList<Store> tmp = (ArrayList<Store>)object;
    				storeList.addAll(tmp);
    				if (tmp.size() == Api.DEFAULT_LIMIT && storeList.size() <= 100)
    					getAllStores(t);
    				else {
    					addPositive("Store List", mStop, printCount(storeList));
    					if (storeList.size() > 0) {
	    			    	random = new Random().nextInt(storeList.size());
	    			    	rStoreId = storeList.get(random).getId();
	    			    	rDealerId = storeList.get(random).getDealerId();
	    				} else {
	    					setNoIds();
						}
    					t.next();
    				}
    			}  else {
					addNegative("Store List", statusCode, object);
					t.next();
				}
			}
		}, storeList.size()).execute();
    	
    }

    private void getAllOffers(final Test t) {
    	start();
    	mEta.getOfferList(new OfferListListener() {
			
			@Override
			public void onComplete(int statusCode, Object object) {
				if (statusCode == 200) {
					stop();
					@SuppressWarnings("unchecked")
					ArrayList<Offer> tmp = (ArrayList<Offer>)object;
					offerList.addAll(tmp);
					if (tmp.size() == Api.DEFAULT_LIMIT && offerList.size() <= 100) {
						getAllOffers(t);
					} else {
    					addPositive("Offer List", mStop, printCount(offerList));
    					if (offerList.size() > 0) {
    						random = new Random().nextInt(offerList.size());
        			    	rOfferId = offerList.get(random).getId();
        			    	rDealerId = offerList.get(random).getDealerId();
        			    	rStoreId = offerList.get(random).getStoreId();
        			    	rCatalogId = offerList.get(random).getCatalogId();
	    				} else {
	    					setNoIds();
						}
    			    	
    					t.next();
    				}
				} else {
					addNegative("Offer List", statusCode, object);
					t.next();
				}

			}
		}, offerList.size()).execute();
    }
    
    private void getCatalog(final Test t) {
    	start();
    	final String name = "Catalog";
    	CatalogListener cl = new CatalogListener() {
			
			@Override
			public void onComplete(int statusCode, Object object) {
				if (statusCode == 200 ) {
					stop();
					Catalog c = (Catalog)object;
					addPositive(name, rCatalogId, mStop, c.toString(true));
					t.next();
				} else {
					addNegative(name, rCatalogId, statusCode, object);
					t.next();
				}
			}
		};
		
    	mEta.getCatalogId(cl, rCatalogId).execute();
    }
    
    private void getDealer(final Test t) {
    	start();
    	final String name = "Dealer";
    	mEta.getDealerId(new DealerListener() {
			
			@Override
			public void onComplete(int statusCode, Object object) {
				
				if (statusCode == 200) {
					stop();
					Dealer d = (Dealer)object;
					addPositive(name, rDealerId, mStop, d.toString(true));
					t.next();
				} else {
					addNegative(name, rDealerId, statusCode, object);
					t.next();
				}
			}
		}, rDealerId).execute();
    }

    private void getStore(final Test t) {
    	start();
    	final String name = "Store";
    	mEta.getStoreId(new StoreListener() {
			
			@Override
			public void onComplete(int statusCode, Object object) {
				
				if (statusCode == 200) {
					stop();
					Store s = (Store)object;
					addPositive(name, rStoreId, mStop, s.toString(true));
					t.next();
				} else {
					addNegative(name, rStoreId, statusCode, object);
					t.next();
				}
			}
		}, rStoreId).execute();
    }

    private void getOffer(final Test t) {
    	start();
    	final String name = "Offer";
    	mEta.getOfferId(new OfferListener() {
			
			@Override
			public void onComplete(int statusCode, Object object) {

				if (statusCode == 200) {
					stop();
					Offer o = (Offer)object;
					addPositive(name, rOfferId, mStop, o.toString(true));
					t.next();
				} else {
					addNegative(name, rOfferId, statusCode, object);
					t.next();
				}
			}
		}, rOfferId).execute();
    }

    private void getCatalogIds(final Test t) {
    	start();
    	boolean b = catalogList.size() == 0;
    	int size = b ? 0 : (catalogList.size()/2)+1;
    	String[] ids = new String[size];
    	for (int i = 0 ; i < size ; i++ ) {
    		ids[i] = catalogList.get(i).getId();
    		tmpIds += ids[i] + ",";
    	}
    	mEta.getCatalogIds(new CatalogListListener() {
			
			@Override
			public void onComplete(int statusCode, Object object) {
				
				if (statusCode == 200) {
					stop();
					@SuppressWarnings("unchecked")
					ArrayList<Catalog> tmp = (ArrayList<Catalog>)object;
					addPositive("Catalog Ids", tmpIds, mStop, printCount(tmp));
					tmpIds = "";
					t.next();
				} else {
					addNegative("Catalog Ids", statusCode, object);
					t.next();
				}
			}
		}, ids).execute();
    }

    private void getOfferIds(final Test t) {
    	start();
    	int size = offerList.size() == 0 ? 0 : (offerList.size()/2)+1;
    	String[] ids = new String[size];
    	for (int i = 0 ; i < size ; i++ ) {
    		ids[i] = offerList.get(i).getId();
    		tmpIds += ids[i] + ",";
    	}
    	mEta.getOfferIds(new OfferListListener() {
			
			@Override
			public void onComplete(int statusCode, Object object) {
				
				if (statusCode == 200) {
					stop();
					@SuppressWarnings("unchecked")
					ArrayList<Offer> tmp = (ArrayList<Offer>)object;
					addPositive("Offer Ids", tmpIds, mStop, printCount(tmp));
					tmpIds = "";
					t.next();
				}  else {
					addNegative("Offer Ids", statusCode, object);
					t.next();
				}
			}
		}, ids).execute();
    }

    private void getDealerIds(final Test t) {
    	start();
    	int size = dealerList.size() == 0 ? 0 : (dealerList.size()/2)+1;
    	String[] ids = new String[size];
    	for (int i = 0 ; i < size ; i++ ) {
    		ids[i] = dealerList.get(i).getId();
    		tmpIds += ids[i] + ",";
    	}
    	mEta.getDealerIds(new DealerListListener() {
			
			@Override
			public void onComplete(int statusCode, Object object) {
				
				if (statusCode == 200) {
					stop();
					@SuppressWarnings("unchecked")
					ArrayList<Offer> tmp = (ArrayList<Offer>)object;
					addPositive("Dealer Ids", tmpIds, mStop, printCount(tmp));
					tmpIds = "";
					t.next();
				}  else {
					addNegative("Dealer Ids", statusCode, object);
					t.next();
				}
			}
		}, ids).execute();
    }

    private void getOfferSearch(final Test t, final int i) {
    	start();
    	mEta.getOfferSearch(new OfferListListener() {
			
			@Override
			public void onComplete(int statusCode, Object object) {

				if (statusCode == 200) {
					stop();
					@SuppressWarnings("unchecked")
					ArrayList<Offer> o = (ArrayList<Offer>)object;
					addPositive("Offer Search", queryOffer[i], mStop, printCount(o));
					t.next();
				} else {
					addNegative("Offer Search", queryOffer[i], statusCode, object);
					t.next();
				}
			}
		}, queryOffer[i]).execute();
    }

    private void getDealerSearch(final Test t, final int i) {
    	start();
    	mEta.getDealerSearch(new DealerListListener() {
			
			@Override
			public void onComplete(int statusCode, Object object) {

				if (statusCode == 200) {
					stop();
					@SuppressWarnings("unchecked")
					ArrayList<Dealer> o = (ArrayList<Dealer>)object;
					addPositive("Dealer Search", queryDealer[i], mStop, printCount(o));
					t.next();
				} else {
					addNegative("Dealer Search", queryDealer[i], statusCode, object);
					t.next();
				}
			}
		}, queryDealer[i]).execute();
    }

    private void getStoreSearch(final Test t, final int i) {
    	start();
    	mEta.getStoreSearch(new StoreListListener() {
			
			@Override
			public void onComplete(int statusCode, Object object) {

				if (statusCode == 200) {
					stop();
					@SuppressWarnings("unchecked")
					ArrayList<Store> o = (ArrayList<Store>)object;
					addPositive("Store Search", queryStore[i], mStop, printCount(o));
					t.next();
				} else {
					addNegative("Store Search", queryStore[i], statusCode, object);
					t.next();
				}
			}
		}, queryStore[i]).execute();
    }

    public interface Test {
    	public void next();
    }
    
}