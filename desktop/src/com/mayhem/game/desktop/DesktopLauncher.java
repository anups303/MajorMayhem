package com.mayhem.game.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.mayhem.game.Bomber;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.title = "MajorMayhem";
		config.width = 800;
		config.height = 600;
		new LwjglApplication(new Bomber(), config);
	}
}
