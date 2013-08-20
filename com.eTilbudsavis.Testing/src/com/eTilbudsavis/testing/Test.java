package com.eTilbudsavis.testing;

import java.util.ArrayList;

import com.eTilbudsavis.etasdk.Eta;

public class Test {

	private String mName;
	private PrintListener mPl;
	private TestVarDump mTvd;
	private Eta mEta;
	
	public int currentTest = 0;
	public long start = 0L;
	public long stop = 0L;
	public long accumilated = 0L;
	private Test next = null;
	
	public Test(String name, PrintListener pl) {
		mName = name;
		mPl = pl;
	}

	public Test setVarDump(TestVarDump tvd) {
		mTvd = tvd;
		return this;
	}

	public Test setEta(Eta eta) {
		mEta = eta;
		return this;
	}
	
	public void startTimer() {
		start = System.currentTimeMillis();
	}
	
    public void stopTimer() {
		stop = System.currentTimeMillis() - start;
		accumilated += stop;
    }

    public void init() {
    }
    
	public void run() {
	}
	
	public Test setNext(Test next) {
		this.next = next;
		return this;
	}

	public Test getNext() {
		return next;
	}

	public String getName() {
		return mName;
	}

    private String printCount(ArrayList<? extends Object> list) {
		return list == null ? "null" : new StringBuilder().append("Count: ").append(list.size()).toString();
    }
//
//	public void getAllCatalogs() {
//    	startTimer();
//    	mEta.getCatalogList(new CallbackCatalogList() {
//    		
//			@Override
//			public void onComplete(int statusCode, ArrayList<Catalog> catalogs, EtaError error) {
//				stopTimer();
//    			if (statusCode == 200) {
//    				mTvd.listCatalog.addAll(catalogs);
//    				if (catalogs.size() == Api.DEFAULT_LIMIT) {
//    					getAllCatalogs();
//    				} else {
//    					mPl.positive("Catalog List", null, stop, printCount(mTvd.listCatalog));
//    					if (mTvd.listCatalog.size() > 0) {
//        			    	int random = new Random().nextInt(mTvd.listCatalog.size() );
//        			    	Catalog c = mTvd.listCatalog.get(random);
//        			    	mTvd.idCatalog = c.getId();
//        			    	mTvd.idDealer = c.getDealerId();
//        			    	mTvd.idStore = c.getStoreId();
//    					} else {
//    						mTvd.setNoIds();
//    					}
//    					run();
//    				}
//    			} else {
//    				mPl.negative("Catalog List", null, stop, statusCode, error);
//					run();
//				}
//				
//			}
//    	}, mTvd.listCatalog.size(), new String[] {Catalog.SORT_NAME}).execute();
//    	
//    }
//
//	public void getAllDealers() {
//    	startTimer();
//    	
//    	mEta.getDealerList(new CallbackDealerList() {
//			
//			@Override
//			public void onComplete(int statusCode, ArrayList<Dealer> dealers, EtaError error) {
//				stopTimer();
//    			if (statusCode == 200) {
//    				mTvd.listDealer.addAll(dealers);
//    				if (dealers.size() == Api.DEFAULT_LIMIT) {
//    					getAllDealers();
//    				} else {
//    					mPl.positive("Complete Dealer List", null, stop, printCount(mTvd.listDealer));
//    					if (mTvd.listDealer.size() > 0) {
//    						int random = new Random().nextInt(mTvd.listDealer.size());
//    						mTvd.idDealer = mTvd.listDealer.get(random).getId();
//    					} else {
//    						mTvd.setNoIds();
//    					}
//    					run();
//    				}
//    			}  else {
//    				mPl.negative("Dealer List", null, stop, statusCode, error);
//					run();
//				}
//			}
//
//		}, mTvd.listDealer.size()).execute();
//    	
//    }
//
//	public void getAllStores() {
//    	startTimer();
//    	mEta.getStoreList(new CallbackStoreList() {
//			
//			@Override
//			public void onComplete(int statusCode, ArrayList<Store> stores, EtaError error) {
//				stopTimer();
//    			if (statusCode == 200) {
//    				mTvd.listStore.addAll(stores);
//    				if (stores.size() == Api.DEFAULT_LIMIT && mTvd.listStore.size() <= 100)
//    					getAllStores();
//    				else {
//    					mPl.positive("Store List", null, stop, printCount(mTvd.listStore));
//    					if (mTvd.listStore.size() > 0) {
//    						int random = new Random().nextInt(mTvd.listStore.size());
//    						mTvd.idStore = mTvd.listStore.get(random).getId();
//    						mTvd.idDealer = mTvd.listStore.get(random).getDealerId();
//	    				} else {
//	    					mTvd.setNoIds();
//						}
//    					run();
//    				}
//    			}  else {
//    				mPl.negative("Store List", null, stop, statusCode, error);
//					run();
//				}
//			}
//
//		}, mTvd.listStore.size()).execute();
//    	
//    }
//
//	public void getAllOffers() {
//    	startTimer();
//    	mEta.getOfferList(new CallbackOfferList() {
//			
//			@Override
//			public void onComplete(int statusCode, ArrayList<Offer> offers, EtaError error) {
//				
//				if (statusCode == 200) {
//					stopTimer();
//					mTvd.listOffer.addAll(offers);
//					if (offers.size() == Api.DEFAULT_LIMIT && mTvd.listOffer.size() <= 100) {
//						getAllOffers();
//					} else {
//						mPl.positive("Offer List", null, stop, printCount(mTvd.listOffer));
//    					if (mTvd.listOffer.size() > 0) {
//    						int random = new Random().nextInt(mTvd.listOffer.size());
//    						Offer o = mTvd.listOffer.get(random);
//    						mTvd.idOffer = o.getId();
//    						mTvd.idDealer = o.getDealerId();
//    						mTvd.idStore = o.getStoreId();
//    						mTvd.idCatalog = o.getCatalogId();
//	    				} else {
//	    					mTvd.setNoIds();
//						}
//    			    	
//    					run();
//    				}
//				} else {
//					mPl.negative("Offer List", null, stop, statusCode, error);
//					run();
//				}
//
//			}
//			
//		}, mTvd.listOffer.size()).execute();
//    }
//    
//	public void getCatalog() {
//    	startTimer();
//    	final String name = "Catalog";
//    	CallbackCatalog cl = new CallbackCatalog() {
//			
//			@Override
//			public void onComplete(int statusCode, Catalog catalog, EtaError error) {
//				stopTimer();
//				if (statusCode == 200 ) {
//					mPl.positive(name, mTvd.idCatalog, stop, catalog.toString(true));
//				} else {
//					mPl.negative(name, mTvd.idCatalog, stop, statusCode, error);
//				}
//				run();
//			}
//
//		};
//		
//    	mEta.getCatalogId(cl, mTvd.idCatalog).execute();
//    }
//    
//	public void getDealer() {
//    	startTimer();
//    	final String name = "Dealer";
//
//    	
//    	mEta.getDealerId(new CallbackDealer() {
//			
//			@Override
//			public void onComplete(int statusCode, Dealer dealer, EtaError error) {
//				stopTimer();
//				if (statusCode == 200) {
//					mPl.positive(name, mTvd.idDealer, stop, dealer.toString(true));
//				} else {
//					mPl.negative(name, mTvd.idDealer, stop, statusCode, error);
//				}
//				run();
//			}
//
//		}, mTvd.idDealer).execute();
//    }
//
//	public void getStore() {
//    	startTimer();
//    	final String name = "Store";
//    	mEta.getStoreId(new CallbackStore() {
//			
//			@Override
//			public void onComplete(int statusCode, Store store, EtaError error) {
//				stopTimer();
//				if (statusCode == 200) {
//					mPl.positive(name, mTvd.idStore, stop, store.toString(true));
//				} else {
//					mPl.negative(name, mTvd.idStore, stop, statusCode, error);
//				}
//				run();
//			}
//
//		}, mTvd.idStore).execute();
//    }
//
//	public void getOffer() {
//    	startTimer();
//    	final String name = "Offer";
//    	mEta.getOfferId(new CallbackOffer() {
//			
//			@Override
//			public void onComplete(int statusCode, Offer offer, EtaError error) {
//				stopTimer();
//				if (statusCode == 200) {
//					mPl.positive(name, mTvd.idOffer, stop, offer.toString(true));
//				} else {
//					mPl.negative(name, mTvd.idOffer, stop, statusCode, error);
//				}
//				run();
//			}
//
//		}, mTvd.idOffer).execute();
//    }
//
//	public void getCatalogIds() {
//    	startTimer();
//    	boolean b = mTvd.listCatalog.size() == 0;
//    	int size = b ? 0 : (mTvd.listCatalog.size()/2)+1;
//    	String[] ids = new String[size];
//    	for (int i = 0 ; i < size ; i++ ) {
//    		ids[i] = mTvd.listCatalog.get(i).getId();
//    		mTvd.tmpIds += ids[i] + ",";
//    	}
//    	mEta.getCatalogIds(new CallbackCatalogList() {
//			
//			@Override
//			public void onComplete(int statusCode, ArrayList<Catalog> catalogs, EtaError error) {
//				stopTimer();
//				if (statusCode == 200) {
//					mPl.positive("Catalog Ids", mTvd.tmpIds, stop, printCount(catalogs));
//					mTvd.tmpIds = "";
//				} else {
//					mPl.negative("Catalog Ids", null, stop, statusCode, error);
//				}
//				run();
//			}
//
//		}, ids).execute();
//    }
//
//	public void getOfferIds() {
//    	startTimer();
//    	int size = mTvd.listOffer.size() == 0 ? 0 : (mTvd.listOffer.size()/2)+1;
//    	String[] ids = new String[size];
//    	for (int i = 0 ; i < size ; i++ ) {
//    		ids[i] = mTvd.listOffer.get(i).getId();
//    		mTvd.tmpIds += ids[i] + ",";
//    	}
//    	mEta.getOfferIds(new CallbackOfferList() {
//			
//			@Override
//			public void onComplete(int statusCode, ArrayList<Offer> offers,
//					EtaError error) {
//				stopTimer();
//				if (statusCode == 200) {
//					mPl.positive("Offer Ids", mTvd.tmpIds, stop, printCount(offers));
//					mTvd.tmpIds = "";
//				}  else {
//					mPl.negative("Offer Ids", null, stop, statusCode, error);
//				}
//				run();
//			}
//
//		}, ids).execute();
//    }
//
//	public void getDealerIds() {
//    	startTimer();
//    	int size = mTvd.listDealer.size() == 0 ? 0 : (mTvd.listDealer.size()/2)+1;
//    	String[] ids = new String[size];
//    	for (int i = 0 ; i < size ; i++ ) {
//    		ids[i] = mTvd.listDealer.get(i).getId();
//    		mTvd.tmpIds += ids[i] + ",";
//    	}
//    	mEta.getDealerIds(new CallbackDealerList() {
//			
//			@Override
//			public void onComplete(int statusCode, ArrayList<Dealer> dealers, EtaError error) {
//
//				stopTimer();
//				if (statusCode == 200) {
//					mPl.positive("Dealer Ids", mTvd.tmpIds, stop, printCount(dealers));
//					mTvd.tmpIds = "";
//				}  else {
//					mPl.negative("Dealer Ids", null, stop, statusCode, error);
//				}
//				run();
//			}
//		}, ids).execute();
//    }
//
//	public void getOfferSearch(final int i) {
//    	startTimer();
//    	mEta.getOfferSearch(new CallbackOfferList() {
//			
//			@Override
//			public void onComplete(int statusCode, ArrayList<Offer> offers, EtaError error) {
//
//				stopTimer();
//				if (statusCode == 200) {
//					mPl.positive("Offer Search", mTvd.queryOffer[i], stop, printCount(offers));
//				} else {
//					mPl.negative("Offer Search", mTvd.queryOffer[i], stop, statusCode, error);
//				}
//				run();
//			}
//		}, mTvd.queryOffer[i]).execute();
//    }
//
//	public void getDealerSearch(final int i) {
//    	startTimer();
//    	mEta.getDealerSearch(new CallbackDealerList() {
//			
//			@Override
//			public void onComplete(int statusCode, ArrayList<Dealer> dealers, EtaError error) {
//
//				stopTimer();
//				if (statusCode == 200) {
//					mPl.positive("Dealer Search", mTvd.queryDealer[i], stop, printCount(dealers));
//				} else {
//					mPl.negative("Dealer Search", mTvd.queryDealer[i], stop, statusCode, error);
//				}
//				run();
//			}
//
//		}, mTvd.queryDealer[i]).execute();
//    }
//
//	public void getStoreSearch(final int i) {
//    	startTimer();
//    	mEta.getStoreSearch(new CallbackStoreList() {
//			
//			@Override
//			public void onComplete(int statusCode, ArrayList<Store> stores, EtaError error) {
//				stopTimer();
//				if (statusCode == 200) {
//					mPl.positive("Store Search", mTvd.queryStore[i], stop, printCount(stores));
//				} else {
//					mPl.negative("Store Search", mTvd.queryStore[i], stop, statusCode, error);
//				}
//				run();
//			}
//
//		}, mTvd.queryStore[i]).execute();
//    }
    
	public interface PrintListener {
		public void positive(String testName, String id, long time, String body);
		public void negative(String testName, String id, long time, int code, Object object);
		public void header(String name);
		public void subHeader(String name);
	}
}