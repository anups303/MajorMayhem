package com.mayhem.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

public class MajorMayhemGame extends Game {
	
	public SpriteBatch batch, hudSB;
	public BitmapFont hudFont;
	//public Skin skin;
	public Stage stage;
	public Sprite splash;
	
	@Override
	public void create() {
		batch = new SpriteBatch();
		hudSB = new SpriteBatch();
		hudFont = new BitmapFont();
		splash = new Sprite();
		stage = new Stage();
		this.setScreen(new MenuScreen(this));
	}
	
	public void render() {
		super.render();
	}
	
	public void dispose() {
		batch.dispose();
		hudSB.dispose();
		hudFont.dispose();
		splash.getTexture().dispose();
		//skin.dispose();
		stage.dispose();
	}

}
