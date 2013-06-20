package com.eTilbudsavis.sdkdemo;

public class Test {

	public String name = "";
	public int iteration = 0;
	public long start = 0L;
	public long stop = 0L;
	public long accumilated = 0L;
	private Test next = null;
	
	public Test() {
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
		return name;
	}

	public Test setName(String name) {
		this.name = name;
		return this;
	}

}