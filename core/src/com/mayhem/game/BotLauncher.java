package com.mayhem.game;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import rice.p2p.commonapi.Id;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.mayhem.game.Bomber.GUIBombState;
import com.mayhem.mediator.Mediator;
import com.mayhem.overlay.BombState;
import com.mayhem.overlay.IRegionStateListener;
import com.mayhem.overlay.Region;

public class BotLauncher {

	class Bot implements Runnable, IRegionStateListener {
		class BotBombState extends BombState {

			private static final long serialVersionUID = 2299401070931813152L;
			long timer;

			public BotBombState(Id playerId, int x, int y, long timer) {
				super(playerId, x, y);
				this.timer = timer;
			}
		}

		Mediator mediator;
		long x, y;
		Random r;
		List<BotBombState> bombs;

		public Bot() {
			this.mediator = new Mediator();
			r = new Random();
			bombs = new ArrayList<BotBombState>();
		}

		public void run() {
			Integer mapId;
			Region init = null;
			init = mediator.joinGame("130.83.116.78", 9001, this);
			if (init != null) {
				mapId = init.getMapId();
				for (int i = 0; i < init.getPlayers().size(); i++)
					if (init.getPlayers().get(i).getId() == mediator
							.GetNodeId()) {
						x = init.getPlayers().get(i).getX();
						y = init.getPlayers().get(i).getY();
					}
			}

			x = y = 12;
			mediator.updatePosition((int) x, (int) y);

			try {
				while (true) {
					long dX = x + r.nextInt(20) - 10, dY = y + r.nextInt(20)
							- 10;
					if (dX < 2)
						dX = 2;
					if (dY < 2)
						dY = 2;
					System.out.println("(" + dX + "," + dY + ")");

					// boolean flag = false;
					while (!(x == dX && y == dY)) {
						Thread.sleep(2 * 500);

						long now = System.currentTimeMillis();
						Iterator<BotBombState> i = bombs.iterator();
						while (i.hasNext()) {
							BotBombState bbs = i.next();
							if (now >= bbs.timer ) {
								if ((bbs.getX() - 1 == x && bbs.getY() == y)
										|| (bbs.getX() + 1 == x && bbs.getY() == y)
										|| (bbs.getX() == x && bbs.getY() == y - 1)
										|| (bbs.getX() == x && bbs.getY() == y + 1)) {
									mediator.died(bbs.getPlayerId());
									return;
								}
								i.remove();
							}
						}

						if (x < dX) {
							x++;
						} else if (x > dX) {
							x--;
						} else if (y < dY) {
							y++;
						} else if (y > dY) {
							y--;
						}
						mediator.updatePosition((int) x, (int) y);
					}
				}
			} catch (Exception v) {
				System.out.println(v);
			}
		}

		@Override
		public void regionStateReceived(Region region) {
			List<BombState> bombList = region.getBombs();

			if (bombList != null)
				synchronized (bombList) {
					for (BombState bomb : bombList) {
						bombs.add(new BotBombState(bomb.getPlayerId(), bomb
								.getX(), bomb.getY(), System
								.currentTimeMillis() + 3000));
					}
				}
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
