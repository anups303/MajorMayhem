package com.mayhem.overlay;

public interface IActionAcknowledgmentListner {
	public void acknowledgmentReceived(long messageid, Object result);
}
