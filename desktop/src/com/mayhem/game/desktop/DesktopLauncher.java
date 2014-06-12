package com.mayhem.game.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.mayhem.game.Bomber;
import com.mayhem.game.BotLauncher;

public class DesktopLauncher {
	public static void main(String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.title = "MajorMayhem";
		config.width = 800;
		config.height = 600;
		new LwjglApplication(new Bomber(), config);

		String botCount = System.getenv("bot");
		if (botCount != null && !botCount.isEmpty()) {
			Thread one = new Thread() {
				public void run() {
					try {

						Thread.sleep(5000);

						String botCount = System.getenv("bot");
						new BotLauncher(Integer.parseInt(botCount));
					} catch (InterruptedException v) {
						System.out.println(v);
					}
				}
			};

			one.start();

		}
	}
}
