package com.eTilbudsavis.etasdk.Database;


public class RunnableQuery extends DbQuery<Runnable>{

	private final Runnable mRunnable;
	
	public RunnableQuery(Runnable r) {
		mRunnable = r;
	}
	
	@Override
	public void run() {
		mRunnable.run();
	}
	
	

}
