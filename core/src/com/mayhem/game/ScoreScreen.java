package com.mayhem.game;

import java.util.HashMap;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.mayhem.mediator.Mediator;

public class ScoreScreen implements InputProcessor, Screen {
	
	final MajorMayhemGame g;
	private SpriteBatch batch;
	private BitmapFont font;
	private int score, port;
	private String ip;
	private Mediator mediator;
	private HashMap<String, Integer> scoreMap;
	
	public ScoreScreen(final MajorMayhemGame game, int score, String ip, int port, Mediator mediator, HashMap<String, Integer> scoreMap) {
		this.g = game;
		this.score = score;
		batch = new SpriteBatch();
		font = new BitmapFont();
		this.port = port;
		this.ip = ip;
		this.mediator = mediator;
		this.scoreMap = scoreMap;
//		System.out.println(scoreMap.values());
	}

	@Override
	public void render(float delta) {
		Gdx.gl.glClearColor(0, 0, 0, 0);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		batch.begin();
		font.setColor(Color.YELLOW);
		font.setScale(1.5f);
		font.draw(batch, "Top Scores", 275, 600);
		font.setColor(Color.RED);
		font.draw(batch, "You - "+score, 20, 500);
		font.setColor(Color.YELLOW);
		//makeshift score list
/*		for(int i=0; i<10; i++) {
			font.draw(batch, "Player "+(i+1)+" - ??", 20, 450-(50*i));
		}*/
		batch.end();
		
		if(Gdx.input.isKeyPressed(Keys.ESCAPE)) {
			((Game)Gdx.app.getApplicationListener()).setScreen(new Bomber(g, false, ip, port, score, mediator));
			dispose();
		}
	}

	@Override
	public void resize(int width, int height) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void show() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void hide() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void pause() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void resume() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void dispose() {
		batch.dispose();
		font.dispose();
	}

	@Override
	public boolean keyDown(int keycode) {
		
		return false;
	}

	@Override
	public boolean keyUp(int keycode) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean keyTyped(char character) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean mouseMoved(int screenX, int screenY) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean scrolled(int amount) {
		// TODO Auto-generated method stub
		return false;
	}

}
