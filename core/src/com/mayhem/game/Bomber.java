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
import com.mayhem.overlay.IRegionStateListener;
import com.mayhem.overlay.PlayerState;

//for randomization
import java.util.*;

//for debug
import javax.swing.JOptionPane;

import rice.environment.Environment;
import rice.p2p.commonapi.Id;

public class Bomber extends ApplicationAdapter implements InputProcessor,
		IRegionStateListener {
	private Mediator mediator;

	// for sprite
	private SpriteBatch batch;
	private Texture texture, bombTex, textureOfOtherPlayers;
	private Sprite sprite;
	private HashMap<Long, Sprite> bombSprite;
	private HashMap<Id, Sprite> players;

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
	private long elapsedTime = 0;
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
		textureOfOtherPlayers = new Texture(
				Gdx.files.internal("Bman_f_f01.png"));
		sprite = new Sprite(texture);
		bombTex = new Texture(Gdx.files.internal("Bomb_f01.png"));
		bombSprite = new HashMap<Long, Sprite>();

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
		Gdx.input.setInputProcessor(this);

		// for camera
		camera = new OrthographicCamera(w, h);
		camera.setToOrtho(false);
		camera.position.x = posX;
		camera.position.y = posY;
		camera.update();

		// for players

		players = new HashMap<Id, Sprite>();

		// for overlay configuration
		mediator = new Mediator();

		boolean coordinator = true;
		// coordinator = false;
		if (coordinator) {
			if (!mediator.newGame(this)) {
				// TODO: Let user know about it!
			}
		} else {
			if (!mediator.joinGame("130.83.114.42", 9001, this)) {
				// TODO: Let user know about it!
			}
		}
	}

	@Override
	public void dispose() {
		// for sprite
		batch.dispose();
		texture.dispose();
		textureOfOtherPlayers.dispose();
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

		synchronized (players) {

			Iterator<Id> itr = players.keySet().iterator();
			while (itr.hasNext())
				players.get(itr.next()).draw(batch);
		}
		sprite.draw(batch);

		// if (Gdx.input.isKeyPressed(Input.Keys.SPACE)) {
		// bombX = sprite.getX();
		// bombY = sprite.getY();
		// for (elapsedTime = 0; elapsedTime < 3; elapsedTime += dt) {
		// dt = Gdx.graphics.getDeltaTime();
		// bombSprite.setPosition(bombX, bombY);
		// bombSprite.draw(batch);
		// }
		// }

		if (System.currentTimeMillis() >= elapsedTime) {
			bombSprite.clear();
		} else {
			Iterator<Long> itr = bombSprite.keySet().iterator();
			while (itr.hasNext())
				bombSprite.get(itr.next()).draw(batch);
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
		int moveAmount = 32;
		Cell cell = null;
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
		cell = collisionLayer.getCell(((int) (posX + xVar)) / moveAmount,
				(int) (posY + yVar) / moveAmount);

		if (cell == null
				|| (cell != null && !cell.getTile().getProperties()
						.containsKey("blocked"))) {
			posX += xVar;
			posY += yVar;
			if (mediator.updatePosition(((int) (posX)) / moveAmount,
					(int) (posY) / moveAmount)) {
				camera.translate(xVar, yVar);
				camera.update();
			}

		}

		if (keycode == Keys.SPACE) {

			Sprite bomb = new Sprite(bombTex);
			bomb.setSize(32, 32);
			bomb.setPosition(posX, posY);

			bombSprite.put(new Random().nextLong(), bomb);

			elapsedTime = System.currentTimeMillis() + 5000;

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

	protected Sprite findPlayerById(Id id) {
		return players.get(id);
	}

	@Override
	public void regionStateReceived(List<PlayerState> list) {
		synchronized (players) {
			for (PlayerState player : list) {
				if (player.getId() != mediator.GetNodeId()) {
					// TODO: Render each player in their new position
					Sprite p = findPlayerById(player.getId());
					if (p == null) {
						p = new Sprite(textureOfOtherPlayers);
						this.players.put(player.getId(), p);
					}
					p.setSize(32.0f, 64.0f);
					p.setPosition(player.getX() * 32, player.getY() * 32);

					System.out.println("position changes:(" + player.getX()
							* 32 + "," + player.getY() * 32 + ")");
				}
			}
		}
	}
}
