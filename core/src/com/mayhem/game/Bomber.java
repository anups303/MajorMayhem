package com.mayhem.game;

//for sprite
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

//for input
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputProcessor;

//for camera
import com.badlogic.gdx.graphics.OrthographicCamera;

//for map
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapRenderer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;

//for collision detection
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer.Cell;
import com.mayhem.mediator.Mediator;

//for randomization
import java.util.*;

//for debug
import javax.swing.JOptionPane;

import rice.environment.Environment;

public class Bomber extends ApplicationAdapter implements InputProcessor {
	private Mediator mediator;

	// for sprite
	private SpriteBatch batch;
	private Texture texture, bombTex;
	private Sprite sprite, bombSprite;

	// for input
	private float posX, posY;

	// for camera
	private OrthographicCamera camera;

	// for map
	private TiledMap tiledMap;
	private TiledMapRenderer tiledMapRenderer;

	// for randomization
	private Random rand = new Random();

	// for collision detection
	private TiledMapTileLayer collisionLayer;

	private float dt = 0;
	private float elapsedTime = 0;
	private float bombX, bombY;

	@Override
	public void create() {
		// for map
		tiledMap = new TmxMapLoader().load("MapTwo.tmx");
		tiledMapRenderer = new OrthogonalTiledMapRenderer(tiledMap);

		// for collision detection
		collisionLayer = (TiledMapTileLayer) tiledMap.getLayers().get("Fore");

		// for sprite
		batch = new SpriteBatch();
		texture = new Texture(Gdx.files.internal("Bman_f_f00.png"));
		sprite = new Sprite(texture);
		bombTex = new Texture(Gdx.files.internal("Bomb_f01.png"));
		bombSprite = new Sprite(bombTex);
		bombSprite.setSize(32, 32);

		// for sprite position
		float w = Gdx.graphics.getWidth();
		float h = Gdx.graphics.getHeight();
		sprite.setSize(32.0f, 64.0f);
		posX = 32 * (rand.nextInt(29) + 1);
		posY = 32 * (rand.nextInt(29) + 1);
		// check whether overlapping with another block
		while (collisionLayer.getCell((int) posX / 32, (int) posY / 32) != null) {
			posX = 32 * (rand.nextInt(29) + 1);
			posY = 32 * (rand.nextInt(29) + 1);
		}
		sprite.setPosition(posX, posY);
		bombSprite.setPosition(posX, posY);
		Gdx.input.setInputProcessor(this);

		// for camera
		camera = new OrthographicCamera(w, h);
		camera.setToOrtho(false);
		camera.position.x = posX;
		camera.position.y = posY;
		camera.update();

		// for overlay configuration
		mediator = new Mediator();

		// if (!mediator.newGame()){
		// // TODO: Let user know about it!
		// }

		if (!mediator.joinGame("130.83.116.225", 9001)) {
			// TODO: Let user know about it!
		}
	}

	@Override
	public void dispose() {
		// for sprite
		batch.dispose();
		texture.dispose();
	}

	@Override
	public void render() {
		// for sprite
		Gdx.gl.glClearColor(1, 1, 1, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		// dt = Gdx.graphics.getDeltaTime();
		// JOptionPane.showMessageDialog(null, dt);

		// for input
		sprite.setPosition(posX, posY);
		// bombSprite.setPosition(posX, posY);

		// for camera
		batch.setProjectionMatrix(camera.combined);

		// for map
		tiledMapRenderer.setView(camera);
		tiledMapRenderer.render();

		batch.begin();
		sprite.draw(batch);
		if (Gdx.input.isKeyPressed(Input.Keys.SPACE)) {
			bombX = sprite.getX();
			bombY = sprite.getY();
			for (elapsedTime = 0; elapsedTime < 3; elapsedTime += dt) {
				dt = Gdx.graphics.getDeltaTime();
				bombSprite.setPosition(bombX, bombY);
				bombSprite.draw(batch);
			}
		}
		batch.end();

		/*
		 * batch.begin(); if(Gdx.input.isKeyPressed(Input.Keys.SPACE)) {
		 * bombSprite.setPosition(sprite.getX(),sprite.getY());
		 * bombSprite.draw(batch); } batch.end();
		 */
	}

	@Override
	public void resize(int width, int height) {
	}

	@Override
	public void pause() {
	}

	@Override
	public void resume() {
	}

	@Override
	public boolean keyDown(int keycode) {
		// tile width and height set at 32 px
		float moveAmount = 32.0f;
		Cell cell = null;
		boolean changed = false;
		float xVar = 0, yVar = 0;

		switch (keycode) {
		case Keys.LEFT:
			xVar = -moveAmount;
			break;
		case Keys.RIGHT:
			xVar = +moveAmount;
			break;
		case Keys.DOWN:
			yVar = -moveAmount;
			break;
		case Keys.UP:
			yVar = +moveAmount;
			break;
		}

		// convert pixel position to tile position
		cell = collisionLayer.getCell(((int) (posX + xVar)) / 32,
				(int) (posY + yVar) / 32);

		if (cell == null
				|| (cell != null && !cell.getTile().getProperties()
						.containsKey("blocked"))) {
			posX += xVar;
			posY += yVar;
			if (mediator.updatePosition(((int) (posX + xVar)) / 32,
					(int) (posY + yVar) / 32)) {
				camera.translate(xVar, yVar);
				camera.update();
			}
			
		}

		if (keycode == Keys.SPACE) {
			// JOptionPane.showMessageDialog(null,
			// ((int)posX)/32+"+"+(992-(int)posY)/32);
		}
		return true;
	}

	@Override
	public boolean keyUp(int keycode) {
		return false;
	}

	@Override
	public boolean keyTyped(char character) {
		return false;
	}

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		return false;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		return false;
	}

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		return false;
	}

	@Override
	public boolean mouseMoved(int screenX, int screenY) {
		return false;
	}

	@Override
	public boolean scrolled(int amount) {
		return false;
	}
}
