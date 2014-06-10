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
import com.mayhem.overlay.BombState;
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
	class BombInfo {
		Sprite sprite;
		long timer;

		public BombInfo(Sprite sprite, long timer) {
			this.sprite = sprite;
			this.timer = timer;
		}
	}

	private Mediator mediator;

	// for sprite
	private SpriteBatch batch;
	private Texture texture, bombTex, textureOfOtherPlayers;
	private Sprite sprite;
	private HashMap<Long, BombInfo> bombSprite;
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
		bombSprite = new HashMap<Long, BombInfo>();

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
		 coordinator = false;
		if (coordinator) {
			if (!mediator.newGame(this)) {
				// TODO: Let user know about it!
			}
		} else {
			if (!mediator.joinGame("130.83.112.83", 9001, this)) {
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

		// for input
		sprite.setPosition(posX, posY);

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

		if (bombSprite.size() > 0) {
			List<Long> toBeRemoved = new ArrayList<Long>();
			Iterator<Long> itr = bombSprite.keySet().iterator();
			while (itr.hasNext()) {
				long key = itr.next();
				BombInfo bi = bombSprite.get(key);
				if (System.currentTimeMillis() >= bi.timer) {
					toBeRemoved.add(key);
					// TODO: render the bomb explosion
					
//					bi.sprite.getX()
//					bi.sprite.getY()
				} else {
					bi.sprite.draw(batch);
				}
			}
			if (toBeRemoved.size() > 0) {
				for (long id : toBeRemoved)
					bombSprite.remove(id);
			}
		}
		batch.end();

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
			if (mediator.bombPlacement(((int) (posX)) / moveAmount,
					(int) (posY) / moveAmount)) {
				addBomb(posX, posY);
			}
		}
		return true;
	}

	protected void addBomb(float posX, float posY) {
		Sprite bomb = new Sprite(bombTex);
		bomb.setSize(32, 32);
		bomb.setPosition(posX, posY);

		bombSprite.put(new Random().nextLong(),
				new BombInfo(bomb, System.currentTimeMillis() + 5000));
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
	public void regionStateReceived(List<PlayerState> playerList,
			List<BombState> bombList) {
		if (playerList != null)
			synchronized (players) {
				for (PlayerState player : playerList) {
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
		if (bombList != null)
			synchronized (bombList) {
				for (BombState bomb : bombList) {
					System.out.println("Bomb placement:(" + bomb.getX()
							* 32 + "," + bomb.getY() * 32 + ")");
					addBomb(bomb.getX()*32, bomb.getY()*32);
				}
			}
	}
}
