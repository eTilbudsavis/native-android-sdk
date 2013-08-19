package com.eTilbudsavis.sdkdemo;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

import com.eTilbudsavis.etasdk.Eta;
import com.eTilbudsavis.sdkdemo.helpers.Test;
import com.eTilbudsavis.sdkdemo.helpers.TestVarDump;
import com.etilbudsavis.sdkdemo.R;

public class Testing extends Activity {

	public static final String TAG = "Testing";
	
	public static final boolean PRINT_CONSOL_BODY = true;
	public static final boolean PRINT_CONSOL_HEADER = true;
	public static final boolean PRINT_CONSOL_SUB_HEADER = true;
	public static final boolean PRINT_VIEW = true;
	
	private Eta mEta;
	private LayoutInflater mInflater;
	private LinearLayout llMain;
	TestVarDump mTvd = new TestVarDump();
	
	long mHttpTime = 0L;
	long mTestStart = System.currentTimeMillis();
	
	ArrayList<Test> tests = new ArrayList<Test>();
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.testing);

        llMain = (LinearLayout)findViewById(R.id.llMain);
        mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        // TODO: Un comment line below and add own API KEY/SECRET
//        mEta = new Eta(Keys.API_KEY, Keys.API_SECRET, this);
        mEta.debug(true);
//        mEta.clearPreferences();
//        mEta.getLocation().set(55.63105, 12.5766, false).setRadius(700000);	// Fields
        
        
//        tests.add(tSessionStart);
//        tests.add(tSessionLogin);
//        tests.add(tDeleteRandom);
//        tests.add(tShoppinglist);
//        tests.add(tEndpointAndListenerMismatch);
//        tests.add(tCache);
//        tests.add(tLocation);
        
//        main.setEta(mEta).setVarDump(mTvd).run();
        
    }
//
//    PrintListener printer = new PrintListener() {
//
//		@Override
//		public void header(String name) {
//			if (PRINT_CONSOL_HEADER) {
//	    		StringBuilder sb = new StringBuilder();
//	    		sb.append("### ").append(name).append(" ###");
//
//	    		Utilities.logd(TAG, sb.toString());
//	    	}
//	    	if (PRINT_VIEW) {
//		    	TextView t = new TextView(getApplicationContext());
//		    	t.setText(name);
//		    	t.setBackgroundColor(Color.parseColor("#0000FF"));
//		    	t.setTextAppearance(getApplicationContext(), android.R.style.TextAppearance_Medium);
//		    	llMain.addView(t);
//	    	}
//		}
//		@Override
//		public void subHeader(String name) {
//			if (PRINT_CONSOL_SUB_HEADER) {
//	    		StringBuilder sb = new StringBuilder();
//	    		sb.append("*** ").append(name).append(" ***");
//	    		Utilities.logd(TAG, sb.toString());
//	    	}
//	    	if (PRINT_VIEW) {
//		    	TextView t = new TextView(getApplicationContext());
//		    	t.setText(name);
//		    	t.setBackgroundColor(Color.parseColor("#0000aa"));
//		    	t.setTextAppearance(getApplicationContext(), android.R.style.TextAppearance_Medium);
//		    	llMain.addView(t);
//	    	}
//		}
//		
//		@Override
//		public void positive(String testName, String id, long time, String body) {
//			if (id != null)
//	    		testName = testName + ": " + id;
//	    	
//	    	if (PRINT_CONSOL_BODY) {
//	    		StringBuilder sb = new StringBuilder();
//	    		sb.append("--- ").append(testName).append(" ---\n");
//	    		sb.append("Time: ").append(time).append("\n");
//	    		sb.append(body);
//	        	Utilities.logd(TAG, sb.toString());
//			}
//	    	if (PRINT_VIEW) {
//	        	LinearLayout llCon = (LinearLayout)mInflater.inflate(R.layout.test_layout, null);
//	        	TextView tvHead = (TextView)llCon.findViewById(R.id.tvHeader);
//	        	TextView tvBody = (TextView)llCon.findViewById(R.id.tvBody);
//	        	TextView tvTime = (TextView)llCon.findViewById(R.id.tvTime);
//	        	tvHead.setText(testName);
//	        	tvTime.append(String.valueOf(time));
//	        	tvBody.setText(body);
//	        	llMain.addView(llCon);
//	    	}
//	    	mHttpTime += time;
//		}
//		
//		@Override
//		public void negative(String testName, String id, long time, int code, Object object) {
//			if (id != null)
//	    		testName = testName + ": " + id;
//	    	
//	    	if (PRINT_CONSOL_BODY) {
//	    		StringBuilder sb = new StringBuilder();
//	    		sb.append("--- ").append(testName).append(" ---\n");
//	    		sb.append("Time: ").append(time).append("\n");
//	    		sb.append("StatusCode: ").append(code).append("\n");
//	    		sb.append(object.toString());
//	        	Utilities.logd(TAG, sb.toString());
//	    	}
//	    	if (PRINT_VIEW) {
//		    	TextView t = new TextView(getApplicationContext());
//		    	t.setBackgroundColor(Color.parseColor("#FF0000"));
//		    	t.setText(testName);
//		    	llMain.addView(t);
//		    	TextView i = new TextView(getApplicationContext());
//		    	i.setText(String.valueOf(code + ": " + object.toString()));
//		    	llMain.addView(i);
//	    	}
//	    	mHttpTime += time;
//		}
//		
//	};
//	
//    Test main = new Test("Main", printer) {
//    	
//		@Override
//		public void run() {
//			
//			if (currentTest < tests.size()) {
//				Test t = tests.get(currentTest);
//				currentTest++;
//				t.setEta(mEta).setVarDump(mTvd).setNext(main).init();
//				printer.header(t.getName());
//				t.run();
//			} else {
//				String timeTotal = String.valueOf(System.currentTimeMillis() - mTestStart);
//				String timeHttp = String.valueOf(mHttpTime);
//				printer.header("Main test done. Time: " + timeTotal + "(ms)" + " http: " + timeHttp + "(ms)");
////				printToFile("etaobj.txt");
//			}
//		}
//    };
//    
//    Test tDeleteRandom = new Test("Delete randomShoppinglist", printer) {
//    	
//    	String s = "RandomTestList";
//    	boolean sync = true;
//    	
//    	ShoppinglistListener sll = new ShoppinglistListener() {
//			
//			@Override
//			public void onListUpdate(List<String> added, List<String> deleted,
//					List<String> edited) {
//				if (sync) {
//					sync = false;
//					tDeleteRandom.run();
//				}
//			}
//			
//			@Override
//			public void onItemUpdate(String shoppinglistId) {
//			}
//		};
//		
//		@Override
//		public void init() {
//			mEta.getShoppinglistManager().subscribe(sll);
//			mEta.getShoppinglistManager().openDB();
//		}
//		
//		@Override
//		public void run() {
//
//			if (sync) {
//				mEta.getShoppinglistManager().syncLists();
//			} else {
//				ArrayList<Shoppinglist> sls = mEta.getShoppinglistManager().getListFromName(s);
//				Tools.logd(TAG, "Lists to delete: " + String.valueOf(sls.size()));
//				for (Shoppinglist sl : sls) {
//					mEta.getShoppinglistManager().deleteList(sl);
//				}
//			}
//			
//		}
//    };
//    
//    /**
//     * Testing shoppinglist, and it's functionality
//     */
//    Test tShoppinglist = new Test("Shoppinglist", printer) {
//    	
//    	Shoppinglist tmpList, currentList;
//    	boolean callback = true;
//    	ShoppinglistManager slm;
//    	
//    	ShoppinglistListener sll = new ShoppinglistListener() {
//			
//			@Override
//			public void onListUpdate(List<String> added, List<String> deleted,
//					List<String> edited) {
//				
//				Utilities.logd(TAG, "onListUpdate");
//				
//				if (added != null)
//					Utilities.logd(TAG, "List(s) Added:" + String.valueOf(added.size()));
//
//				if (deleted != null)
//					Utilities.logd(TAG, "List(s) Deleted:" + String.valueOf(deleted.size()));
//					
//				if (edited != null)
//					Utilities.logd(TAG, "List(s) Edited:" + String.valueOf(edited.size()));
//				
//				run();
//			}
//			
//			@Override
//			public void onItemUpdate(String shoppinglistId) {
//				Utilities.logd(TAG, "onItemUpdate");
//				run();
//			}
//			
//		};
//		
//		@Override
//		public void init() {
//			slm = mEta.getShoppinglistManager();
//			slm.subscribe(sll);
//			slm.openDB();
//			slm.clearDatabase();
//		}
//		
//		@Override
//		public void run() {
//			
//			switch (currentTest) {
//			case 0:
//				printer.subHeader("List Sync");
//				slm.syncLists();
//				break;
//
//			case 1:
//				printer.subHeader("Items Sync");
//				slm.syncItems();
//				break;
//
//				
//			case 2:
//				printer.subHeader("Current list");
//				currentList = slm.getCurrentList();
//				break;
//
//			case 3:
//				printer.subHeader("Add List");
//				tmpList = Shoppinglist.fromName("RandomTestList");
//				slm.addList(tmpList);
//				break;
//				
//			case 4:
//				printer.subHeader("Delete List - waiting " + String.valueOf(30000) + "ms to delete");
//				runDeleteList(tmpList);
//				break;
//				
//			case 5:
//				printer.subHeader("Add Item");
//				currentList = slm.getCurrentList();
//				Utilities.logd(TAG, currentList.getId());
//				ShoppinglistItem sli = new ShoppinglistItem(currentList, "Remu Testen");
//				slm.addItem(currentList, sli);
//				break;
//				
//			default:
//				int c = slm.getAllLists().size();
//				Utilities.logd(TAG, "Shoppinglist switch hit default - Num of lists: " + String.valueOf(c));
//				if (callback) {
//					callback = false;
//					getNext().run();
//				}
//				break;
//			}
//			currentTest++;
//		}
//    };
//    
//    private void runDeleteList(final Shoppinglist sl) {
//    	mEta.getHandler().postDelayed(new Runnable() {
//			
//			@Override
//			public void run() {
//				mEta.getShoppinglistManager().deleteList(sl);
//			}
//		}, 15000);
//    }
//    
//    /**
//     * Session testing, no user
//     */
//    Test tSessionStart = new Test("Session Start", printer) {
//
//		@Override
//		public void run() {
//			startTimer();
//			
//			SessionListener sl = new SessionListener() {
//				
//				@Override
//				public void onUpdate() {
//					stopTimer();
//					mEta.getSession().unSubscribe(this);
//					printer.positive("Session: ", null, stop, mEta.getSession().toString(true));
//					getNext().run();
//				}
//			};
//			
//			mEta.getSession().subscribe(sl).start();
//		}
//    };
//
//    /**
//     * Session test with user login
//     */
//    Test tSessionLogin = new Test("Session Login", printer) {
//
//		@Override
//		public void run() {
//			startTimer();
//			
//			SessionListener sl = new SessionListener() {
//				
//				@Override
//				public void onUpdate() {
//					stopTimer();
//					mEta.getSession().unSubscribe(this);
//					printer.positive("Session Login: ", null, stop, mEta.getSession().toString(true));
//					getNext().run();
//				}
//			};
//			
//			mEta.getSession().subscribe(sl).login(Keys.LOGIN_USER, Keys.LOGIN_PASS);
//		}
//    };
//    
//    Test tCache = new Test("Cache", printer) {
//
//		@Override
//		public void run() {
//			
//			switch (currentTest) {
//			case 0:
//				currentTest ++;
//				tCatalogs.setEta(mEta).setVarDump(mTvd).setNext(tCache).run();
//				break;
//
//			case 1:
//				currentTest ++;
//				getCatalog();
//				break;
//
//			case 2:
//				currentTest ++;
//				getCatalog();
//				break;
//				
//			default:
//				getNext().run();
//				return;
//			}
//			
//		}
//    };
//    
//    Test tEndpointAndListenerMismatch = new Test("Endpoint", printer) {
//
//		@Override
//		public void run() {
//
//			startTimer();
//			mEta.api().get(Endpoint.CATALOG_LIST, new Api.CallbackOffer() {
//				
//				@Override
//				public void onComplete(int statusCode, Offer offer, EtaError error) {
//
//					stopTimer();
//					if (statusCode == 200)
//						printer.positive(getName(), null, stop, offer.toString());
//					else
//						printer.negative(getName(), null, stop, statusCode, error.toString());
//					
//					getNext().run();
//				}
//			}).execute();
//		}
//    };
//    
//    Test tLocation = new Test("Location", printer) {
//
//    	private void exec() {
//
//			tCatalogs.setEta(mEta).setVarDump(mTvd).setNext(tDealers.setEta(mEta).setVarDump(mTvd))
//			.getNext().setNext(tStores.setEta(mEta).setVarDump(mTvd))
//			.getNext().setNext(tOffers.setEta(mEta).setVarDump(mTvd))
//			.getNext().setNext(tLocation.setEta(mEta).setVarDump(mTvd));
//			tCatalogs.run();
//    	}
//    	
//    	private void execSearch() {
//			tOfferSearch.setEta(mEta).setVarDump(mTvd).setNext(tDealerSearch.setEta(mEta).setVarDump(mTvd))
//			.getNext().setNext(tStoreSearch.setEta(mEta).setVarDump(mTvd))
//			.getNext().setNext(tLocation.setEta(mEta).setVarDump(mTvd));
//			tOfferSearch.run();
//    	}
//    	
//		@Override
//		public void run() {
//
//			switch (currentTest) {
//			case 0:
//		        // Herrup, 10km, should give 1 catalog, 4 stores
//				mEta.getLocation().set(56.40875, 8.91922, 10000, false);
//		        printer.header("HERRUP - 10KM - ~1 catalog, 4 stores, ~100 offers");
//				exec();
//				break;
//				
//			case 1:
//				mEta.getLocation().set(55.63105, 12.5766, 700000, false);	// Fields
//				printer.header("FIELDS - 700KM - ~76 catalogs, ~110 stores, ~110 offers");
//				exec();
//				break;
//				
//			case 2:
//				mEta.getLocation().set(57.057582, 9.934028, 5000, false);	// Nørresundby
//				printer.header("NØRRESUNDBY - 5KM - ~30 catalogs, ~104 stores, ~110 offers");
//				exec();
//				break;
//				
//			case 3:
//				// This location should fail on all tests, except dealers, dealer should work
//				mEta.getLocation().set(56.436, 11.707, 5, false);	// Kattegat
//				printer.header("KATTEGAT - 5M - 0 catalogs, 0 stores, 0 offers");
//				exec();
//				break;
//				
//			case 4:
//				printer.header("SEARCH TEST - KATTEGAT");
//				execSearch();
//				break;
//			
//			case 5:
//				mEta.getLocation().set(55.63105, 12.5766, 700000, false);	// Fields
//				printer.header("SEARCH TEST - FIELDS");
//				execSearch();
//				break;
//			
//			default:
//				currentTest = 0;
//				getNext().run();
//				return;
//			}
//
//			currentTest ++;
//			
//		}
//	};
//	
//	Test tOfferSearch = new Test("Offer Search", printer) {
//
//		@Override
//		public void run() {
//			
//			if (currentTest < mTvd.queryOffer.length) {
//				getOfferSearch( currentTest);
//				currentTest++;
//			} else {
//				currentTest = 0;
//				getNext().run();
//			}
//			
//		}
//	};
//
//	Test tDealerSearch = new Test("Dealer Search", printer) {
//
//		@Override
//		public void run() {
//			
//			if (currentTest < mTvd.queryDealer.length) {
//				getDealerSearch( currentTest);
//				currentTest++;
//			} else {
//				currentTest = 0;
//				getNext().run();
//			}
//		}
//	};
//
//	Test tStoreSearch = new Test("Store Search", printer) {
//
//		@Override
//		public void run() {
//			
//			if (currentTest < mTvd.queryStore.length) {
//				getStoreSearch( currentTest);
//				currentTest++;
//			} else {
//				currentTest = 0;
//				getNext().run();
//			}
//		}
//	};
//	
//    Test tCatalogs = new Test("Catalogs",  printer) {
//
//		@Override
//		public void run() {
//
//			switch (currentTest) {
//			case 0: 
//				currentTest ++;
//				printer.header("TESTING CATALOGS");
//				getAllCatalogs();
//				break;
//			case 1:
//				currentTest ++;
//				getCatalog();
//				break;
//				
//			case 2:
//				currentTest ++;
//				getCatalogIds();
//				break;
//				
//			case 3:
//				currentTest ++;
//				getDealer();
//				break;
//				
//			case 4:
//				currentTest ++;
//				getStore();
//				break;
//			
//			default:
//				currentTest = 0;
//				getNext().run();
//				return;
//			}
//
//			
//		}
//	};
//
//    Test tDealers = new Test("Dealers",  printer) {
//
//		@Override
//		public void run() {
//			
//			switch (currentTest) {
//			case 0:
//				currentTest ++;
//				printer.header("TESTING DEALERS");
//				getAllDealers();
//				break;
//				
//			case 1:
//				currentTest ++;
//				getDealer();
//				break;
//				
//			case 2:
//				currentTest ++;
//				getDealerIds();
//				break;
//			
//			default:
//				currentTest = 0;
//				getNext().run();
//				return;
//			}
//
//		}
//	};
//
//    Test tStores = new Test("Stores", printer) {
//
//		@Override
//		public void run() {
//
//			switch (currentTest) {
//			case 0:
//				currentTest ++;
//				printer.header("TESTING STORES");
//				getAllStores();
//				break;
//				
//			case 1:
//				currentTest ++;
//				getStore();
//				break;
//				
//			case 2:
//				currentTest ++;
//				getDealer();
//				break;
//				
//
//			default:
//				currentTest = 0;
//				getNext().run();
//				return;
//			}
//
//			
//		}
//	};
//
//    Test tOffers = new Test("Offers",  printer) {
//
//		@Override
//		public void run() {
//
//			switch (currentTest) {
//			case 0: 
//				currentTest ++;
//				printer.header("TESTING OFFERS"); 
//				getAllOffers();
//				break;
//			case 1: 
//				currentTest ++;
//				getDealer(); 
//				break;
//			case 2: 
//				currentTest ++;
//				getStore(); 
//				break;
//			case 3: 
//				currentTest ++;
//				getCatalog(); 
//				break;
//			case 4: 
//				currentTest ++;
//				getOffer(); 
//				break;
//			case 5: 
//				currentTest ++;
//				getOfferIds(); 
//				break;
//			
//			default:
//				currentTest = 0;
//				getNext().run();
//				return;
//			}
//
//			
//		}
//	};

}