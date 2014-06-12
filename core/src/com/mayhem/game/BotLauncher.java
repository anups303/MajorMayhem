package com.mayhem.game;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.mayhem.mediator.Mediator;
import com.mayhem.overlay.BombState;
import com.mayhem.overlay.IRegionStateListener;
import com.mayhem.overlay.PlayerState;

public class BotLauncher {

	class Bot implements Runnable, IRegionStateListener {
		Mediator mediator;
		int x, y;

		public Bot() {
			this.mediator = new Mediator();
		}

		public void run() {
			mediator.joinGame(null, 9001, this);
			x = y = 12;
			mediator.updatePosition(x, y);

			try {
				for (;;) {
					Thread.sleep(500);
					int d = new Random().nextInt(3) - 1;

					if (new Random().nextInt(2) == 0)
						x += d;
					else
						y += d;

					if (x < 1)
						x = 1;
					if (y < 1)
						y = 1;
					mediator.updatePosition(x, y);
				}

			} catch (Exception v) {
				System.out.println(v);
			}
		}

		@Override
		public void regionStateReceived(List<PlayerState> playerList,
				List<BombState> bombList) {
		}
	}

	public BotLauncher(int count) {
		try {
			for (int c = 0; c < count; c++) {
				Thread.sleep(1500);
				Runnable r = new Bot();
				new Thread(r).start();
				new Bot();
			}
		} catch (Exception v) {
			System.out.println(v);
		}
	}

}
