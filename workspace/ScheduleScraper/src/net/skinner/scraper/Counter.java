package net.skinner.scraper;

public class Counter {
	private int count;
	
	public void set (int count) {
		this.count = count;
	}
	
	public int getCount() {
		return count;
	}
	
	public void add() {
		count++;
	}
}