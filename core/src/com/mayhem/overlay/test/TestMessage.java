package com.mayhem.overlay.test;

import com.mayhem.overlay.Message;

public class TestMessage extends Message {
	private String _message;

	public TestMessage(String msg) {
		this._message = msg;
	}

	public String toString() {
		return "Content:" + this._message;
	}
}
