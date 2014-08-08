package com.mayhem.game;

//for sprite
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

//for input
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputProcessor;

//for camera
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.maps.MapObjects;
//for map
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapRenderer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;

//for collision detection
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer.Cell;
import com.badlogic.gdx.physics.bullet.linearmath.int4;
import com.mayhem.mediator.Mediator;
import com.mayhem.overlay.BombState;
import com.mayhem.overlay.IRegionStateListener;
import com.mayhem.overlay.PlayerState;
import com.mayhem.overlay.Region;

import com.mayhem.game.Timer;

//for randomization
import java.util.*;

//for debug
import javax.swing.JOptionPane;

import rice.environment.Environment;
import rice.p2p.commonapi.Id;

public class Bomber extends ApplicationAdapter implements InputProcessor,
		IRegionStateListener {
	class GUIBombState extends BombState {
		private static final long serialVersionUID = 2798204437146947850L;
		Sprite sprite;
		long timer;

		public GUIBombState(Id playerId, int x, int y, Sprite sprite, long timer) {
			super(playerId, x, y);
			this.sprite = sprite;
			this.timer = timer;
		}

	}

	private BitmapFont hudFont;
	private int score;
	private Timer timer;
	private int moveAmount = 32;
	private Mediator mediator;
	private static final int MAX_NUMBER_OF_BOMBS = 5;
	private static final int BOMB_EXPLOSION_TIME = 1000;// mili second
	protected int bombCounter = 0;
	// for sprite
	private SpriteBatch batch, hudSB;
	private Texture texture, bombTex, textureOfOtherPlayers, flameTexture,
			hudTexture;
	private Sprite sprite, flameSprite;
	private HashMap<Long, GUIBombState> bombSprite;
	private HashMap<Id, Sprite> players;
	private boolean died = false;
	private long timeToBackToGame;

	// for input
	private float posX, posY;

	// for camera
	private OrthographicCamera camera, hudCam;

	// for map
	private TiledMap tiledMap;
	private TiledMapRenderer tiledMapRenderer;
	private float w, h;

	// for randomization
	private Random rand = new Random();

	// for collision detection
	private TiledMapTileLayer collisionLayer;
	private int mapheight, mapwidth;
	private Integer mapId, newMapId;

	@Override
	public void create() {

		// for sprite
		batch = new SpriteBatch();
		hudSB = new SpriteBatch();
		hudFont = new BitmapFont();
		hudFont.scale(0.75f);
		timer = new Timer();
		timer.start();
		flameTexture = new Texture(Gdx.files.internal("Explosion_CN.png"));
		texture = new Texture(Gdx.files.internal("Bman_f_f00.png"));
		hudTexture = new Texture(Gdx.files.internal("hudbg1.png"));
		textureOfOtherPlayers = new Texture(
				Gdx.files.internal("Bman_f_f01.png"));
		sprite = new Sprite(texture);
		flameSprite = new Sprite(flameTexture);

		bombTex = new Texture(Gdx.files.internal("Bomb_f01.png"));
		bombSprite = new HashMap<Long, GUIBombState>();

		sprite.setSize(32.0f, 64.0f);

		// for players
		players = new HashMap<Id, Sprite>();

		// for overlay configuration
		mediator = new Mediator();
		Region init = null;
		boolean coordinator = System.getenv("newGame").equalsIgnoreCase("1");
		String bootsrapperIP = System.getenv("IP");
		if (bootsrapperIP != null && bootsrapperIP.equals(""))
			bootsrapperIP = null;
		if (coordinator) {
			mapId = mediator.newGame(this);
			if (mapId == -1) {
				// TODO: Let user know about it!
			}
			posX = posY = 1 * moveAmount;
		} else {
			int bootstrapperPort = 9001;
			if (System.getenv("bootstrapperPort") != null) {
				bootstrapperPort = Integer.parseInt(System
						.getenv("bootstrapperPort"));
			}
			int localPort = 9001;
			if (System.getenv("localPort") != null) {
				localPort = Integer.parseInt(System.getenv("localPort"));
			}
			init = mediator.joinGame(bootsrapperIP, bootstrapperPort, this,
					localPort);
			if (init == null) {
				// TODO: Let user know about it!
				return;
			} else {
				mapId = init.getMapId();
				for (int i = 0; i < init.getPlayers().size(); i++)
					if (init.getPlayers().get(i).getId() == mediator
							.GetNodeId()) {
						posX = init.getPlayers().get(i).getX() * moveAmount;
						posY = init.getPlayers().get(i).getY() * moveAmount;
					}
			}
		}

		// for map
		System.out.println("mapid:" + mapId);
		newMapId = mapId;
		tiledMap = new TmxMapLoader().load("maps/Map" + mapId + ".tmx");
		tiledMapRenderer = new OrthogonalTiledMapRenderer(tiledMap);

		// for collision detection
		collisionLayer = (TiledMapTileLayer) tiledMap.getLayers().get("Fore");
		mapheight = collisionLayer.getHeight();
		mapwidth = collisionLayer.getWidth();

		w = Gdx.graphics.getWidth();
		h = Gdx.graphics.getHeight();

		if (posX == 0 && posY == 0) {
			// for sprite position

			posX = 32 * (rand.nextInt(mapwidth - 2) + 1);
			posY = 32 * (rand.nextInt(mapheight - 2) + 1);
			// check whether overlapping with another block
			while (collisionLayer.getCell((int) posX / 32, (int) posY / 32) != null) {
				posX = 32 * (rand.nextInt(mapwidth - 2) + 1);
				posY = 32 * (rand.nextInt(mapheight - 2) + 1);
			}
		}
		if (init != null && init.getDestroyedBlocks() != null) {
			explodeDestroyedBlocks(init);
		}

		sprite.setPosition(posX, posY);
		Gdx.input.setInputProcessor(this);

		// for camera
		camera = new OrthographicCamera(w, h);
		camera.setToOrtho(false);
		switch ((int) (posX / w)) {
		case 0:
			camera.position.x = 32 * 10;
			break;
		case 1:
			camera.position.x = 32 * 30;
			break;
		case 2:
			camera.position.x = 32 * 50;
			break;
		default:
			break;
		}
		switch ((int) (posY / h)) {
		case 0:
			camera.position.y = 32 * 10;
			break;
		case 1:
			camera.position.y = 32 * 30;
			break;
		case 2:
			camera.position.y = 32 * 50;
			break;
		default:
			break;
		}
		// camera.position.x = posX;
		// camera.position.y = posY;
		camera.update();

		// for HUD camera
		hudCam = new OrthographicCamera(w, h);

		sprite.setPosition(posX, posY);
		mediator.updatePosition(((int) (posX)) / moveAmount, (int) (posY)
				/ moveAmount);

	}

	protected void explodeDestroyedBlocks(Region init) {
		if (init != null && init.getDestroyedBlocks() != null)
			for (int i = 0; i < init.getDestroyedBlocks().size(); i++) {
				explodeCellAt(null, init.getDestroyedBlocks().get(i).getLeft(),
						init.getDestroyedBlocks().get(i).getRight());
			}
	}

	@Override
	public void dispose() {
		mediator.leaveGame(null);
		batch.dispose();
		hudSB.dispose();
		texture.dispose();
		textureOfOtherPlayers.dispose();
		flameTexture.dispose();
		hudTexture.dispose();
	}

	private void die(Id killedByPlayer) {
		timeToBackToGame = System.currentTimeMillis() + 5000;
		mediator.died(killedByPlayer);
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
		// hudSB.setProjectionMatrix(camera.combined); //do not set projection
		// matrix!
		// hudCam.position.set(x, y, z)

		// for map
		tiledMapRenderer.setView(camera);
		tiledMapRenderer.render();

		// hudSB.enableBlending(); //not needed? will try with external
		// transparency
		batch.begin();

		synchronized (mapId) {
			if (mapId != newMapId) {
				mapId = newMapId;
				System.out.println("mapid:" + mapId);
				tiledMap = new TmxMapLoader().load("maps/Map" + mapId + ".tmx");
				tiledMapRenderer = new OrthogonalTiledMapRenderer(tiledMap);

				// for collision detection
				collisionLayer = (TiledMapTileLayer) tiledMap.getLayers().get(
						"Fore");
				mapheight = collisionLayer.getHeight();
				mapwidth = collisionLayer.getWidth();

				w = Gdx.graphics.getWidth();
				h = Gdx.graphics.getHeight();
			}
		}

		if (!died) {

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
					GUIBombState bi = bombSprite.get(key);
					if (System.currentTimeMillis() >= bi.timer) {
						if (bi.sprite == flameSprite) {

							toBeRemoved.add(key);
							int x = (int) bi.sprite.getX() / moveAmount, y = (int) bi.sprite
									.getY() / moveAmount;

							explodeCellAt(bi, x + 1, y);
							explodeCellAt(bi, x - 1, y);
							explodeCellAt(bi, x, y + 1);
							explodeCellAt(bi, x, y - 1);
						}
						flameSprite.setPosition(bi.sprite.getX(),
								bi.sprite.getY());
						flameSprite.draw(batch);

						bi.sprite = flameSprite;

						bi.timer += BOMB_EXPLOSION_TIME;
					} else {
						bi.sprite.draw(batch);
						if (bi.sprite == flameSprite) {
							Sprite right = new Sprite(flameTexture);
							right.setPosition(bi.sprite.getX() + moveAmount,
									bi.sprite.getY());
							right.draw(batch);

							Sprite left = new Sprite(flameTexture);
							left.setPosition(bi.sprite.getX() - moveAmount,
									bi.sprite.getY());
							left.draw(batch);

							Sprite down = new Sprite(flameTexture);
							down.setPosition(bi.sprite.getX(), bi.sprite.getY()
									+ moveAmount);
							down.draw(batch);

							Sprite up = new Sprite(flameTexture);
							up.setPosition(bi.sprite.getX(), bi.sprite.getY()
									- moveAmount);
							up.draw(batch);
						}
					}
				}
				if (toBeRemoved.size() > 0) {
					for (long id : toBeRemoved) {
						bombSprite.remove(id);
					}
				}
			}
		}

		batch.end();

		// start different sprite batch for heads up display
		hudSB.begin();
		hudSB.draw(hudTexture, 0, 565);
		hudFont.setColor(Color.YELLOW);

		// hudFont.draw(hudSB, "Time: " + timer.elapsedTime(), 50, 595);
		if (died) {
			long d = ((timeToBackToGame - System.currentTimeMillis()) / 1000);
			hudFont.draw(hudSB,
					"You have been killed. You will be back to game in " + d
							+ "s", 50, 595);
			if (d <= 0) {
				mediator.updatePosition(((int) (posX)) / moveAmount,
						(int) (posY) / moveAmount);
				died = false;
			}
		} else
			hudFont.draw(hudSB, "Score: " + score, 50, 595);
		hudSB.end();
	}

	protected void explodeCellAt(GUIBombState bi, int x, int y) {
		Cell c = collisionLayer.getCell(x, y);
		if (c != null && c.getTile().getProperties().containsKey("destroyable")) {
			collisionLayer.setCell(x, y, null);
		}
		if ((bi != null) && (posX / moveAmount == x && posY / moveAmount == y)) {
			died = true;
			die(bi.getPlayerId());
		}
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

		boolean collisionWithBombFlag = false;
		if (bombSprite.size() > 0) {
			Iterator<Long> itr = bombSprite.keySet().iterator();
			while (itr.hasNext()) {
				long key = itr.next();
				GUIBombState bi = bombSprite.get(key);
				if (bi.sprite.getX() == posX + xVar
						&& bi.sprite.getY() == posY + yVar) {
					collisionWithBombFlag = true;
					break;
				}
			}
		}

		if ((!collisionWithBombFlag)
				&& (cell == null || (cell != null && !cell.getTile()
						.getProperties().containsKey("blocked")))) {
			int xMod = ((int) (posX / moveAmount)) / 20;
			int yMod = ((int) (posY / moveAmount)) / 20;
			posX += xVar;
			posY += yVar;
			if (mediator.updatePosition(((int) (posX)) / moveAmount,
					(int) (posY) / moveAmount)) {
				sprite.setPosition(posX, posY);
				if ((((int) (posX / moveAmount)) / 20) != xMod
						|| (((int) (posY / moveAmount)) / 20) != yMod) {
					// Change the region controller
					camera.translate(xVar * 20, yVar * 20);
					camera.update();
				}
			}
		}

		if (keycode == Keys.SPACE) {
			if (allowAddBomb(mediator.GetNodeId()))
				mediator.bombPlacement(((int) (posX)) / moveAmount,
						(int) (posY) / moveAmount);
		}
		return true;
	}

	protected boolean allowAddBomb(Id player) {
		boolean allowBombToAdd = false;
		if (player == this.mediator.GetNodeId()) {
			int localBombCounter = 0;
			Iterator<Long> itr = bombSprite.keySet().iterator();
			while (itr.hasNext()) {
				long key = itr.next();
				GUIBombState bi = bombSprite.get(key);
				if (bi.sprite != flameSprite
						&& bi.getPlayerId() == this.mediator.GetNodeId())
					localBombCounter++;
			}
			allowBombToAdd = (localBombCounter < MAX_NUMBER_OF_BOMBS);
		} else
			allowBombToAdd = true;
		return allowBombToAdd;
	}

	protected void addBomb(float posX, float posY, Id player) {
		if (allowAddBomb(player)) {
			Sprite bomb = new Sprite(bombTex);
			bomb.setSize(32, 32);
			bomb.setPosition(posX, posY);
			bombSprite.put(new Random().nextLong(), new GUIBombState(player,
					(int) (posX / moveAmount), (int) (posY / moveAmount), bomb,
					System.currentTimeMillis() + 3000));

		}

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
		if (players != null)
			return players.get(id);
		else
			return null;
	}

	@Override
	public void regionStateReceived(Region region) {
		try {
			List<PlayerState> playerList = region.getPlayers();
			List<BombState> bombList = region.getBombs();
			if (playerList != null)
				synchronized (players) {
					List<Id> toBeRemoved = new ArrayList<Id>();

					for (PlayerState player : playerList) {
						if (player.getId() != mediator.GetNodeId()) {
							if (player.isAlive()) {
								// Render each player in their new position
								Sprite p = findPlayerById(player.getId());
								if (p == null) {
									p = new Sprite(textureOfOtherPlayers);
									this.players.put(player.getId(), p);
								}
								p.setSize(32.0f, 64.0f);
								p.setPosition(player.getX() * moveAmount,
										player.getY() * moveAmount);
							} else
								toBeRemoved.add(player.getId());
						} else {
							score = player.getScore();
						}
					}

					Iterator<Id> itr = players.keySet().iterator();

					while (itr.hasNext()) {
						Id key = itr.next();
						boolean found = false;
						for (PlayerState player : playerList)
							if (player.getId() == key) {
								found = true;
								break;
							}
						if (!found)
							toBeRemoved.add(key);
					}
					if (toBeRemoved.size() > 0)
						for (Id id : toBeRemoved)
							players.remove(id);

				}
			if (bombList != null)
				synchronized (bombList) {
					for (BombState bomb : bombList) {
						addBomb(bomb.getX() * moveAmount, bomb.getY()
								* moveAmount, bomb.getPlayerId());
					}
				}

			explodeDestroyedBlocks(region);

			synchronized (mapId) {
				if (mapId != region.getMapId()) {
					newMapId = region.getMapId();
					System.out.println("change mapId to:" + newMapId);
				}
			}
		} catch (Exception e) {
			System.out.println(e);
		}
	}
}
