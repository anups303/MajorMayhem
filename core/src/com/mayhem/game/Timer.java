package com.mayhem.game;

public class Timer {
	private long startTime;
	
	public void start() {
		this.startTime = System.nanoTime();
	}
	
	public String elapsedTime() {
		long elapsedTime = System.nanoTime() - startTime;
		long millis = (elapsedTime/1000000);				//convert to milliseconds
		long sec = (elapsedTime/1000000000);
		long min = sec/60;
		String formatTime = String.valueOf(min)+":"+String.valueOf(sec%60)+":"+String.valueOf(millis%1000);
		return formatTime;
	}
}
