package com.mayhem.overlay;

import java.util.List;

public class RegionStateChannelContent extends ChannelContent {
	private static final long serialVersionUID = 2522899394681916995L;

	private List<PlayerState> list;

	public RegionStateChannelContent(List<PlayerState> list) {
		this.list = list;
	}

	public List<PlayerState> getList() {
		return this.list;
	}
}
