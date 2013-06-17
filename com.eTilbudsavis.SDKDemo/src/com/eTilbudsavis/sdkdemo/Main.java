package com.eTilbudsavis.sdkdemo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import Utils.Endpoint;
import Utils.Utilities;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.location.Address;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.eTilbudsavis.etasdk.Api;
import com.eTilbudsavis.etasdk.Api.CallbackCatalog;
import com.eTilbudsavis.etasdk.Api.CallbackCatalogList;
import com.eTilbudsavis.etasdk.Api.CallbackDealer;
import com.eTilbudsavis.etasdk.Api.CallbackDealerList;
import com.eTilbudsavis.etasdk.Api.CallbackOffer;
import com.eTilbudsavis.etasdk.Api.CallbackOfferList;
import com.eTilbudsavis.etasdk.Api.CallbackStore;
import com.eTilbudsavis.etasdk.Api.CallbackStoreList;
import com.eTilbudsavis.etasdk.Eta;
import com.eTilbudsavis.etasdk.Session.SessionListener;
import com.eTilbudsavis.etasdk.ShoppinglistManager.ShoppinglistListener;
import com.eTilbudsavis.etasdk.EtaObjects.Catalog;
import com.eTilbudsavis.etasdk.EtaObjects.Dealer;
import com.eTilbudsavis.etasdk.EtaObjects.EtaError;
import com.eTilbudsavis.etasdk.EtaObjects.Offer;
import com.eTilbudsavis.etasdk.EtaObjects.Shoppinglist;
import com.eTilbudsavis.etasdk.EtaObjects.Store;
import com.etilbudsavis.sdkdemo.R;

public class Main extends Activity {

	public static final String TAG = "Main";

	public static final boolean CONSOL_OUTPUT_BODY = false;
	public static final boolean CONSOL_OUTPUT_HEADER = true;
	public static final boolean DISPLAY_OUTPUT = true;
	
	private Eta mEta;
	private String mApiKey = Keys.API_KEY;
	private String mApiSecret = Keys.API_SECRET;
	
	private LayoutInflater mInflater;
	LinearLayout llMain;
	
	private ArrayList<Catalog> catalogList;
	private ArrayList<Dealer> dealerList;
	private ArrayList<Store> storeList;
	private ArrayList<Offer> offerList;
	
	private String rCatalogId;
	private String rDealerId;
	private String rStoreId;
	private String rOfferId;

	String tmpIds = "";
	
	String[] queryOffer = new String[]{"cola", "øl", "kød", "DetVirkerIkke", "sovs"};
	String[] queryDealer = new String[]{"seeland", "ilka", "bilka", "blka",  "netto", "DetVirkerIkke", "Industrivej Syd 1, Birk"};
	String[] queryStore = queryDealer;
	String[] queryStoreQuick = queryDealer;
	
	long mHttpTime = 0L;
	long mTestStart = System.currentTimeMillis();
	
	ArrayList<Test> tests = new ArrayList<Test>();
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        llMain = (LinearLayout)findViewById(R.id.llMain);
        mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        
        mEta = new Eta(mApiKey, mApiSecret, this);
        mEta.debug(true);
//        mEta.clearPreferences();
        mEta.getLocation().set(55.63105, 12.5766, 700000, false);	// Fields
        
        // Session test should always be first (no session, no data)
        // unless for specific testing purposes
        
//        tests.add(tSessionStart);
        tests.add(tSessionLogin);
        tests.add(tShoppinglist);
//        tests.add(tEndpointAndListenerMismatch);
//        tests.add(tCache);
//        tests.add(tLocation);
        
        resetTestVars();
        
        main.run();
        
    }
    
    Test main = new Test() {
    	
    	int it = -1;
    	
		@Override
		public void run() {
			it ++;
			if (it < tests.size()) {
				tests.get(it).setNext(main).run();
			} else {
				String timeTotal = String.valueOf(System.currentTimeMillis() - mTestStart);
				String timeHttp = String.valueOf(mHttpTime);
				addHeader("TESTING DONE! time: " + timeTotal + "(ms)" + " http: " + timeHttp + "(ms)");
//				printToFile("etaobj.txt");
			}
		}
    };
    
    /**
     * Testing shoppinglist, and it's functionality
     */
    Test tShoppinglist = new Test() {

    	ShoppinglistListener sll = new ShoppinglistListener() {
			
			@Override
			public void onUpdate() {
				tShoppinglist.run();
			}
		};
		
		public void init() {
			mEta.getShoppinglistManager().subscribe(sll);
			mEta.getShoppinglistManager().openDB();
		}
		
		@Override
		public void run() {
			
			switch (iteration) {
			case 0:
				addHeader("TESTING SHOPPINGLIST");
				init();
				iteration ++;
				mEta.getShoppinglistManager().listSync();
				break;

			case 1:
				iteration ++;
				mEta.getShoppinglistManager().itemSync();
				break;

			case 2:
				iteration ++;
				mEta.getShoppinglistManager().addList("SomeRandomList");
				break;

			default:
				getNext().run();
				break;
			}
		}
    };
    
    /**
     * Session testing, no user
     */
    Test tSessionStart = new Test() {

		@Override
		public void run() {
			addHeader("TESTING SESSTION - not really, not done yet");
			startTimer();
	        mEta.getSession().subscribe(new SessionListener() {
				
				@Override
				public void onUpdate() {
					stopTimer();
					mEta.getSession().unSubscribe(this);
					addPositive("Session: ", stop, mEta.getSession().toString(true));
					getNext().run();
				}
			}).start();
		}
    };

    /**
     * Session test with user login
     */
    Test tSessionLogin = new Test() {

		@Override
		public void run() {
			addHeader("TESTING SESSTION LOGIN - not really, not done yet");
			startTimer();
			mEta.getSession().subscribe(new SessionListener() {
				
				@Override
				public void onUpdate() {
					stopTimer();
					mEta.getSession().unSubscribe(this);
					addPositive("Session Login: ", stop, mEta.getSession().toString(true));
					getNext().run();
				}
			}).login(Keys.LOGIN_USER, Keys.LOGIN_PASS);
		}
    };
    
    Test tCache = new Test() {

		@Override
		public void run() {

			switch (iteration) {
			case 0:
				iteration ++;
				addHeader("TESTING CACHE");
				resetTestVars(); 
				tCatalogs.setNext(tCache).run();
				break;

			case 1:
				iteration ++;
				getCatalog(tCache);
				break;

			case 2:
				iteration ++;
				getCatalog(tCache);
				break;
				
			default:
				iteration = 0;
				getNext().run();
				return;
			}
			
			
		}
    };
    
    Test tEndpointAndListenerMismatch = new Test() {

    	String testName = "Endpoint";
    	
		@Override
		public void run() {

			addHeader("TESTING ENDPOINT");
			
			startTimer();
			mEta.api().get(Endpoint.CATALOG_LIST, new Api.CallbackOffer() {
				
				@Override
				public void onComplete(int statusCode, Offer offer, EtaError error) {

					stopTimer();
					if (statusCode == 200)
						addPositive(testName, stop, offer.toString());
					else
						addNegative(testName, stop, statusCode, error.toString());
					
					getNext().run();
				}
			}).execute();
		}
    };
    
    Test tLocation = new Test() {
		
    	private void exec() {

			tCatalogs.setNext(tDealers)
			.getNext().setNext(tStores)
			.getNext().setNext(tOffers)
			.getNext().setNext(tLocation);
			tCatalogs.run();
    	}
    	
    	private void execSearch() {
			tOfferSearch.setNext(tDealerSearch)
			.getNext().setNext(tStoreSearch)
			.getNext().setNext(tLocation);
			tOfferSearch.run();
    	}
    	
		@Override
		public void run() {

			resetTestVars(); 
			
			switch (iteration) {
			case 0:
				addHeader("TESTING LOCATION");
				
		        // Herrup, 10km, should give 1 catalog, 4 stores
		        mEta.getLocation().set(56.40875, 8.91922, 10000, false);
				addHeader("HERRUP - 10KM - ~1 catalog, 4 stores, ~100 offers");
				exec();
				break;
				
			case 1:
				mEta.getLocation().set(55.63105, 12.5766, 700000, false);	// Fields
				addHeader("FIELDS - 700KM - ~76 catalogs, ~110 stores, ~110 offers");
				exec();
				break;
				
			case 2:
				mEta.getLocation().set(57.057582, 9.934028, 5000, false);	// Nørresundby
				addHeader("NØRRESUNDBY - 5KM - ~30 catalogs, ~104 stores, ~110 offers");
				exec();
				break;
				
			case 3:
				// This location should fail on all tests, except dealers, dealer should work
				mEta.getLocation().set(56.436, 11.707, 5, false);	// Kattegat
				addHeader("KATTEGAT - 5M - 0 catalogs, 0 stores, 0 offers");
				exec();
				break;
				
			case 4:
				addHeader("SEARCH TEST - KATTEGAT");
				execSearch();
				break;
			
			case 5:
				mEta.getLocation().set(55.63105, 12.5766, 700000, false);	// Fields
				addHeader("SEARCH TEST - FIELDS");
				execSearch();
				break;
			
			default:
				iteration = 0;
				getNext().run();
				return;
			}

			iteration ++;
			
		}
	};
	
	Test tOfferSearch = new Test() {
		
		@Override
		public void run() {
			
			if (iteration < queryOffer.length) {
				getOfferSearch(tOfferSearch, iteration);
				iteration++;
			} else {
				iteration = 0;
				getNext().run();
			}
			
		}
	};

	Test tDealerSearch = new Test() {
		
		@Override
		public void run() {
			
			if (iteration < queryDealer.length) {
				getDealerSearch(tDealerSearch, iteration);
				iteration++;
			} else {
				iteration = 0;
				getNext().run();
			}
		}
	};

	Test tStoreSearch = new Test() {
		
		@Override
		public void run() {
			
			if (iteration < queryStore.length) {
				getStoreSearch(tStoreSearch, iteration);
				iteration++;
			} else {
				iteration = 0;
				getNext().run();
			}
		}
	};
	
    Test tCatalogs = new Test() {
		
		@Override
		public void run() {

			switch (iteration) {
			case 0: 
				iteration ++;
				addHeader("TESTING CATALOGS");
				getAllCatalogs(tCatalogs);
				break;
			case 1:
				iteration ++;
				getCatalog(tCatalogs);
				break;
				
			case 2:
				iteration ++;
				getCatalogIds(tCatalogs);
				break;
				
			case 3:
				iteration ++;
				getDealer(tCatalogs);
				break;
				
			case 4:
				iteration ++;
				getStore(tCatalogs);
				break;
			
			default:
				iteration = 0;
				getNext().run();
				return;
			}

			
		}
	};

    Test tDealers = new Test() {
		
		@Override
		public void run() {
			
			switch (iteration) {
			case 0:
				iteration ++;
				addHeader("TESTING DEALERS");
				getAllDealers(tDealers);
				break;
				
			case 1:
				iteration ++;
				getDealer(tDealers);
				break;
				
			case 2:
				iteration ++;
				getDealerIds(tDealers);
				break;
			
			default:
				iteration = 0;
				getNext().run();
				return;
			}

		}
	};

    Test tStores = new Test() {
		
		@Override
		public void run() {

			switch (iteration) {
			case 0:
				iteration ++;
				addHeader("TESTING STORES");
				getAllStores(tStores);
				break;
				
			case 1:
				iteration ++;
				getStore(tStores);
				break;
				
			case 2:
				iteration ++;
				getDealer(tStores);
				break;
				

			default:
				iteration = 0;
				getNext().run();
				return;
			}

			
		}
	};

    Test tOffers = new Test() {
		
		@Override
		public void run() {

			switch (iteration) {
			case 0: 
				iteration ++;
				addHeader("TESTING OFFERS"); 
				getAllOffers(tOffers);
				break;
			case 1: 
				iteration ++;
				getDealer(tOffers); 
				break;
			case 2: 
				iteration ++;
				getStore(tOffers); 
				break;
			case 3: 
				iteration ++;
				getCatalog(tOffers); 
				break;
			case 4: 
				iteration ++;
				getOffer(tOffers); 
				break;
			case 5: 
				iteration ++;
				getOfferIds(tOffers); 
				break;
			
			default:
				iteration = 0;
				getNext().run();
				return;
			}

			
		}
	};

	public void printToFile(String filename) {
		
		String directoryFile = Environment.getExternalStorageDirectory().getAbsolutePath();
		
		try{
            File myFile = new File(directoryFile, filename);
            myFile.createNewFile();
            FileOutputStream fOut = new FileOutputStream(myFile);

            OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
           
            	myOutWriter.append("").append("\n\n");
    		
            myOutWriter.close();
            fOut.close();
            Utilities.logd(TAG, "Done writing SD " + myFile.getPath());
        } catch (Exception e) {
        	Utilities.logd(TAG, e.getMessage());
        }
		
	}
	
	private void httpOverhead(long time) {
		mHttpTime += time;
	}
	
    private void addPositive(String testName, String id, long time, String body) {
    	addPositive(testName + ": " + id, time, body);
    }

    private void addPositive(String testName, long time, String body) {
    	if (CONSOL_OUTPUT_BODY) {
    		StringBuilder sb = new StringBuilder();
    		sb.append("*********************************************\n");
    		sb.append(testName).append("\n");
    		sb.append("Time: ").append(time).append("\n");
    		sb.append(body);
        	Utilities.logd(TAG, sb.toString());
		}
    	if (DISPLAY_OUTPUT) {
        	LinearLayout llCon = (LinearLayout)mInflater.inflate(R.layout.test_layout, null);
        	TextView tvHead = (TextView)llCon.findViewById(R.id.tvHeader);
        	TextView tvBody = (TextView)llCon.findViewById(R.id.tvBody);
        	TextView tvTime = (TextView)llCon.findViewById(R.id.tvTime);
        	tvHead.setText(testName);
        	tvTime.append(String.valueOf(time));
        	tvBody.setText(body);
        	llMain.addView(llCon);
    	}
    	httpOverhead(time);
    }

    private void addNegative(String testName, String id, long time, int code, Object object) {
    	addNegative(testName + ": " + id, time, code, object);
    }

    private void addNegative(String testName, long time, int code, Object object) {
    	if (CONSOL_OUTPUT_BODY) {
    		StringBuilder sb = new StringBuilder();
    		sb.append("*********************************************\n");
    		sb.append(testName).append("\n");
    		sb.append("Time: ").append(time).append("\n");
    		sb.append("StatusCode: ").append(code).append("\n");
    		sb.append(object.toString());
        	Utilities.logd(TAG, sb.toString());
    	}
    	if (DISPLAY_OUTPUT) {
	    	TextView t = new TextView(getApplicationContext());
	    	t.setBackgroundColor(Color.parseColor("#FF0000"));
	    	t.setText(testName);
	    	llMain.addView(t);
	    	TextView i = new TextView(getApplicationContext());
	    	i.setText(String.valueOf(code + ": " + object.toString()));
	    	llMain.addView(i);
    	}
    	httpOverhead(time);
    }

    private void addHeader(String name) {
    	if (CONSOL_OUTPUT_HEADER) {
    		StringBuilder sb = new StringBuilder();
    		sb.append("*********************************************\n");
    		sb.append(name);
    		Utilities.logd(TAG, sb.toString());
    	}
    	if (DISPLAY_OUTPUT) {
	    	TextView t = new TextView(getApplicationContext());
	    	t.setText(name);
	    	t.setBackgroundColor(Color.parseColor("#0000FF"));
	    	t.setTextAppearance(getApplicationContext(), android.R.style.TextAppearance_Medium);
	    	llMain.addView(t);
    	}
    }
    
    private String printList(ArrayList<? extends Object> list) {
    	if (list == null)
    		return "null";
    	
    	StringBuilder sb = new StringBuilder();
		for (Object o : list)
			sb.append("- ").append(o.toString()).append("\n");
		
		return sb.toString();
    }
    
    private String printCount(ArrayList<? extends Object> list) {
		return list == null ? "null" : new StringBuilder().append("Count: ").append(list.size()).toString();
    }

    private void resetTestVars() {
    	catalogList = new ArrayList<Catalog>();
    	dealerList = new ArrayList<Dealer>();
    	storeList = new ArrayList<Store>();
    	offerList = new ArrayList<Offer>();
    	setNoIds();
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

	public void printVars() {
		Utilities.logd("Test", "CID: " + rCatalogId + "DID: " + rDealerId + "SID: " + rStoreId + "OID: " + rOfferId);
	}
	
    private void getAllCatalogs(final Test t) {
    	t.startTimer();
    	mEta.getCatalogList(new CallbackCatalogList() {
    		
			@Override
			public void onComplete(int statusCode, ArrayList<Catalog> catalogs,
					EtaError error) {
				t.stopTimer();
    			if (statusCode == 200) {
    				catalogList.addAll(catalogs);
    				if (catalogs.size() == Api.DEFAULT_LIMIT) {
    					getAllCatalogs(t);
    				} else {
    					addPositive("Catalog List", t.stop, printCount(catalogList));
    					if (catalogList.size() > 0) {
        			    	int random = new Random().nextInt(catalogList.size() );
        			    	rCatalogId = catalogList.get(random).getId();
        			    	rDealerId = catalogList.get(random).getDealerId();
        			    	rStoreId = catalogList.get(random).getStoreId();    						
    					} else {
    						setNoIds();
    					}
    					t.run();
    				}
    			} else {
					addNegative("Catalog List", t.stop, statusCode, error);
					t.run();
				}
				
			}
    	}, catalogList.size(), new String[] {Catalog.SORT_NAME}).execute();
    	
    }

    private void getAllDealers(final Test t) {
    	t.startTimer();
    	
    	mEta.getDealerList(new CallbackDealerList() {
			
			@Override
			public void onComplete(int statusCode, ArrayList<Dealer> dealers, EtaError error) {
				t.stopTimer();
    			if (statusCode == 200) {
    				dealerList.addAll(dealers);
    				if (dealers.size() == Api.DEFAULT_LIMIT) {
    					getAllDealers(t);
    				} else {
    					addPositive("Complete Dealer List", t.stop, printCount(dealerList));
    					if (dealerList.size() > 0) {
    						int random = new Random().nextInt(dealerList.size());
	    			    	rDealerId = dealerList.get(random).getId();
    					} else {
    						setNoIds();
    					}
    					t.run();
    				}
    			}  else {
					addNegative("Dealer List", t.stop, statusCode, error);
					t.run();
				}
			}

		}, dealerList.size()).execute();
    	
    }

    private void getAllStores(final Test t) {
    	t.startTimer();
    	mEta.getStoreList(new CallbackStoreList() {
			
			@Override
			public void onComplete(int statusCode, ArrayList<Store> stores, EtaError error) {
				t.stopTimer();
    			if (statusCode == 200) {
    				storeList.addAll(stores);
    				if (stores.size() == Api.DEFAULT_LIMIT && storeList.size() <= 100)
    					getAllStores(t);
    				else {
    					addPositive("Store List", t.stop, printCount(storeList));
    					if (storeList.size() > 0) {
    						int random = new Random().nextInt(storeList.size());
	    			    	rStoreId = storeList.get(random).getId();
	    			    	rDealerId = storeList.get(random).getDealerId();
	    				} else {
	    					setNoIds();
						}
    					t.run();
    				}
    			}  else {
					addNegative("Store List", t.stop, statusCode, error);
					t.run();
				}
			}

		}, storeList.size()).execute();
    	
    }

    private void getAllOffers(final Test t) {
    	t.startTimer();
    	mEta.getOfferList(new CallbackOfferList() {
			
			@Override
			public void onComplete(int statusCode, ArrayList<Offer> offers, EtaError error) {
				
				if (statusCode == 200) {
					t.stopTimer();
					offerList.addAll(offers);
					if (offers.size() == Api.DEFAULT_LIMIT && offerList.size() <= 100) {
						getAllOffers(t);
					} else {
    					addPositive("Offer List", t.stop, printCount(offerList));
    					if (offerList.size() > 0) {
    						int random = new Random().nextInt(offerList.size());
        			    	rOfferId = offerList.get(random).getId();
        			    	rDealerId = offerList.get(random).getDealerId();
        			    	rStoreId = offerList.get(random).getStoreId();
        			    	rCatalogId = offerList.get(random).getCatalogId();
	    				} else {
	    					setNoIds();
						}
    			    	
    					t.run();
    				}
				} else {
					addNegative("Offer List", t.stop, statusCode, error);
					t.run();
				}

			}
			
		}, offerList.size()).execute();
    }
    
    private void getCatalog(final Test t) {
    	t.startTimer();
    	final String name = "Catalog";
    	CallbackCatalog cl = new CallbackCatalog() {
			
			@Override
			public void onComplete(int statusCode, Catalog catalog, EtaError error) {
				t.stopTimer();
				if (statusCode == 200 ) {
					addPositive(name, rCatalogId, t.stop, catalog.toString(true));
				} else {
					addNegative(name, rCatalogId, t.stop, statusCode, error);
				}
				t.run();
			}

		};
		
    	mEta.getCatalogId(cl, rCatalogId).execute();
    }
    
    private void getDealer(final Test t) {
    	t.startTimer();
    	final String name = "Dealer";

    	
    	mEta.getDealerId(new CallbackDealer() {
			
			@Override
			public void onComplete(int statusCode, Dealer dealer, EtaError error) {
				t.stopTimer();
				if (statusCode == 200) {
					addPositive(name, rDealerId, t.stop, dealer.toString(true));
				} else {
					addNegative(name, rDealerId, t.stop, statusCode, error);
				}
				t.run();
			}

		}, rDealerId).execute();
    }

    private void getStore(final Test t) {
    	t.startTimer();
    	final String name = "Store";
    	mEta.getStoreId(new CallbackStore() {
			
			@Override
			public void onComplete(int statusCode, Store store, EtaError error) {
				t.stopTimer();
				if (statusCode == 200) {
					addPositive(name, rStoreId, t.stop, store.toString(true));
				} else {
					addNegative(name, rStoreId, t.stop, statusCode, error);
				}
				t.run();
			}

		}, rStoreId).execute();
    }

    private void getOffer(final Test t) {
    	t.startTimer();
    	final String name = "Offer";
    	mEta.getOfferId(new CallbackOffer() {
			
			@Override
			public void onComplete(int statusCode, Offer offer, EtaError error) {
				t.stopTimer();
				if (statusCode == 200) {
					addPositive(name, rOfferId, t.stop, offer.toString(true));
				} else {
					addNegative(name, rOfferId, t.stop, statusCode, error);
				}
				t.run();
			}

		}, rOfferId).execute();
    }

    private void getCatalogIds(final Test t) {
    	t.startTimer();
    	boolean b = catalogList.size() == 0;
    	int size = b ? 0 : (catalogList.size()/2)+1;
    	String[] ids = new String[size];
    	for (int i = 0 ; i < size ; i++ ) {
    		ids[i] = catalogList.get(i).getId();
    		tmpIds += ids[i] + ",";
    	}
    	mEta.getCatalogIds(new CallbackCatalogList() {
			
			@Override
			public void onComplete(int statusCode, ArrayList<Catalog> catalogs, EtaError error) {
				t.stopTimer();
				if (statusCode == 200) {
					addPositive("Catalog Ids", tmpIds, t.stop, printCount(catalogs));
					tmpIds = "";
				} else {
					addNegative("Catalog Ids", t.stop, statusCode, error);
				}
				t.run();
			}

		}, ids).execute();
    }

    private void getOfferIds(final Test t) {
    	t.startTimer();
    	int size = offerList.size() == 0 ? 0 : (offerList.size()/2)+1;
    	String[] ids = new String[size];
    	for (int i = 0 ; i < size ; i++ ) {
    		ids[i] = offerList.get(i).getId();
    		tmpIds += ids[i] + ",";
    	}
    	mEta.getOfferIds(new CallbackOfferList() {
			
			@Override
			public void onComplete(int statusCode, ArrayList<Offer> offers,
					EtaError error) {
				t.stopTimer();
				if (statusCode == 200) {
					addPositive("Offer Ids", tmpIds, t.stop, printCount(offers));
					tmpIds = "";
				}  else {
					addNegative("Offer Ids", t.stop, statusCode, error);
				}
				t.run();
			}

		}, ids).execute();
    }

    private void getDealerIds(final Test t) {
    	t.startTimer();
    	int size = dealerList.size() == 0 ? 0 : (dealerList.size()/2)+1;
    	String[] ids = new String[size];
    	for (int i = 0 ; i < size ; i++ ) {
    		ids[i] = dealerList.get(i).getId();
    		tmpIds += ids[i] + ",";
    	}
    	mEta.getDealerIds(new CallbackDealerList() {
			
			@Override
			public void onComplete(int statusCode, ArrayList<Dealer> dealers, EtaError error) {

				t.stopTimer();
				if (statusCode == 200) {
					addPositive("Dealer Ids", tmpIds, t.stop, printCount(dealers));
					tmpIds = "";
				}  else {
					addNegative("Dealer Ids", t.stop, statusCode, error);
				}
				t.run();
			}

		}, ids).execute();
    }

    private void getOfferSearch(final Test t, final int i) {
    	t.startTimer();
    	mEta.getOfferSearch(new CallbackOfferList() {
			
			@Override
			public void onComplete(int statusCode, ArrayList<Offer> offers, EtaError error) {

				t.stopTimer();
				if (statusCode == 200) {
					addPositive("Offer Search", queryOffer[i], t.stop, printCount(offers));
				} else {
					addNegative("Offer Search", queryOffer[i], t.stop, statusCode, error);
				}
				t.run();
			}
		}, queryOffer[i]).execute();
    }

    private void getDealerSearch(final Test t, final int i) {
    	t.startTimer();
    	mEta.getDealerSearch(new CallbackDealerList() {
			
			@Override
			public void onComplete(int statusCode, ArrayList<Dealer> dealers, EtaError error) {

				t.stopTimer();
				if (statusCode == 200) {
					addPositive("Dealer Search", queryDealer[i], t.stop, printCount(dealers));
				} else {
					addNegative("Dealer Search", queryDealer[i], t.stop, statusCode, error);
				}
				t.run();
			}

		}, queryDealer[i]).execute();
    }

    private void getStoreSearch(final Test t, final int i) {
    	t.startTimer();
    	mEta.getStoreSearch(new CallbackStoreList() {
			
			@Override
			public void onComplete(int statusCode, ArrayList<Store> stores, EtaError error) {
				t.stopTimer();
				if (statusCode == 200) {
					addPositive("Store Search", queryStore[i], t.stop, printCount(stores));
				} else {
					addNegative("Store Search", queryStore[i], t.stop, statusCode, error);
				}
				t.run();
			}

		}, queryStore[i]).execute();
    }
    
}