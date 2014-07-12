package com.mayhem.game;

import java.util.Random;

import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.mayhem.mediator.Mediator;
import com.mayhem.overlay.IRegionStateListener;
import com.mayhem.overlay.Region;

public class BotLauncher {

	class Bot implements Runnable, IRegionStateListener {
		Mediator mediator;
		long x, y;
		Random r;

		public Bot() {
			this.mediator = new Mediator();
			r = new Random();
		}

		public void run() {
			Integer mapId;
			Region init = null;
			init = mediator.joinGame(null, 9001, this);
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
