package com.mayhem.game;

import java.util.ArrayList;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapRenderer;
//import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
//import com.badlogic.gdx.maps.tiled.TiledMapTileLayer.Cell;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.files.*;

//import com.badlogic.gdx.math.Rectangle;
//import com.badlogic.gdx.utils.viewport.FitViewport;
import javax.swing.JOptionPane;
import java.util.*;

public class Bomber extends ApplicationAdapter implements InputProcessor {
	Texture img;
	TiledMap tiledMap;
	OrthographicCamera camera;
    TiledMapRenderer tiledMapRenderer;
    SpriteBatch sb;
    Texture texture;
    Texture txtrBomb;
    Sprite sprite;
    Sprite sprBomb;
    float posX, posY;
    //Rectangle glViewport;
    TiledMapTileLayer collisionLayer;
    MapProperties prop;
    int mapWidth, mapHeight, tilePixelWidth, tilePixelHeight, mapPixelWidth, mapPixelHeight;
    float tileWidth, tileHeight;
    boolean collisionX = false, collisionY = false;
    String points, line, xypt[];
    String pattern = "(\\d+)([\\,])(\\d+)";
    MyCoord coords;
    ArrayList<MyCoord> coordlist;
    //ArrayList<MyCoord> coordcheck;
	
    @Override
	public void create () {
		float w = Gdx.graphics.getWidth();
        float h = Gdx.graphics.getHeight();

        camera = new OrthographicCamera();
        camera.setToOrtho(false, w, h);						//visible game world
        camera.update();
        //camera.position.set(w/2, h/2, 0);					//centered sprite by commenting this out!!
        //glViewport = new Rectangle(0, 0, w, h);
        
        //coords = new MyCoord();								//adding list of blocks
        coordlist = new ArrayList<Bomber.MyCoord>();
        FileHandle handle = Gdx.files.internal("BlockPoints.txt");
        points = handle.readString();
        Scanner scanner = new Scanner(points);
        while(scanner.hasNextLine()) {
        	coords = new MyCoord();
        	String line = scanner.nextLine();
        	xypt = line.split(",");
        	coords.setX(Integer.parseInt(xypt[0]));
        	coords.setY(Integer.parseInt(xypt[1]));
        	coordlist.add(coords);
        }
        scanner.close();
        //List<Point> points = new ArrayList<Point>();
        
        
        tiledMap = new TmxMapLoader().load("MapOne.tmx");					//Map created using Tiled;present in assets
        prop = tiledMap.getProperties();
        mapWidth = prop.get("width", Integer.class);
        mapHeight = prop.get("height", Integer.class);
        tilePixelWidth = prop.get("tilewidth", Integer.class);
        tilePixelHeight = prop.get("tileheight", Integer.class);
        mapPixelWidth = mapWidth * tilePixelWidth;
        mapPixelHeight = mapHeight * tilePixelHeight;
        tiledMapRenderer = new OrthogonalTiledMapRenderer(tiledMap);
        collisionLayer = new TiledMapTileLayer(mapWidth, mapHeight, tilePixelWidth, tilePixelHeight);
        
        tileWidth = collisionLayer.getTileWidth();
        tileHeight = collisionLayer.getTileHeight();
        Gdx.input.setInputProcessor(this);
        
        sb = new SpriteBatch();
        texture = new Texture(Gdx.files.internal("Bman_f_f00.png"));		//Character sprite;present in assets
        texture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
        sprite = new Sprite(texture);
        
        txtrBomb = new Texture(Gdx.files.internal("Bomb_f01.png"));
        txtrBomb.setFilter(TextureFilter.Linear, TextureFilter.Linear);
        sprBomb = new Sprite(txtrBomb);
        sprBomb.setSize(32, 32);
        //sprite.setOrigin(50, 50);
        sprite.setSize(32, 64);
        //posX = 320.0f;
        posX = 320.0f;
        //posY = 2944.0f;
        posY = 2944.0f;
        sprite.setPosition(posX, posY);
        //sprBomb.setPosition(posX, posY);
        camera.position.x = posX;
        camera.position.y = posY;
	}

	@Override
	public void dispose () {
		sb.dispose();
		texture.dispose();
		txtrBomb.dispose();
	}
	
	@Override
	public void render () {
		float deltaTime = Gdx.graphics.getDeltaTime();
		Gdx.gl.glClearColor(1, 1, 1, 1);									//clear screen RGB values
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        
        /*camera.position.x = sprite.getX();
        camera.position.y = sprite.getY();*/
        camera.update();
        
        //if polling for KB input
        /*if(Gdx.input.isKeyPressed(Input.Keys.LEFT))
        	sprite.translateX(-1f);
        if(Gdx.input.isKeyPressed(Input.Keys.RIGHT))
        	sprite.translateX(1f);
        if(Gdx.input.isKeyPressed(Input.Keys.UP))
        	sprite.translateY(1f);
        if(Gdx.input.isKeyPressed(Input.Keys.DOWN))
        	sprite.translateY(-1f);*/
        
        tiledMapRenderer.setView(camera);
        tiledMapRenderer.render();
        sb.setProjectionMatrix(camera.combined);
        sprite.setPosition(posX, posY);
        sb.begin();
        sprite.draw(sb);
        if(Gdx.input.isKeyPressed(Input.Keys.SPACE)) {
        	sprBomb.setPosition(sprite.getX(), sprite.getY());
			sprBomb.draw(sb);
        	//JOptionPane.showMessageDialog(null, points);
        }
        //sprBomb.draw(sb);
        sb.end();
	}
	
	@Override
    public boolean keyDown(int keycode) {
		float moveAmount = 32.0f;
		MyCoord coords1 = new MyCoord();
		if(keycode == Keys.LEFT) {
			/*collisionX = collisionLayer.getCell((int)(sprite.getX()/tileWidth), (int)(sprite.getY()/tileHeight))
					.getTile().getProperties().containsKey("blocked");*/
			//JOptionPane.showMessageDialog(null, (int)(sprite.getX()-moveAmount)+" "+(int)(sprite.getY()));
			
			/*Cell c = collisionLayer.getCell((int)(sprite.getX()/tileWidth),(int)(sprite.getY()/tileHeight));
			TiledMapTile tl = c.getTile();
			MapProperties mp = tl.getProperties();
			if(mp.containsKey("blocked"))
				JOptionPane.showMessageDialog(null, "blocked");*/
			
			coords1.setX((int)(sprite.getX()-moveAmount));
			coords1.setY((int)(sprite.getY()));
						
			//JOptionPane.showMessageDialog(null, (int)(sprite.getX()-moveAmount)+"+"+(int)sprite.getY());
			if(!coordlist.contains(coords1)) {
				if((int)sprite.getX()>32)
					posX -= moveAmount;
				if((int)sprite.getX()>320 && (int)sprite.getX()<=2880)
					camera.translate((int) -moveAmount,0,0);
			}
		}
		if(keycode == Keys.RIGHT) {
			/*collisionX = collisionLayer.getCell((int)(sprite.getX()/tileWidth), (int)(sprite.getY()/tileHeight))
					.getTile().getProperties().containsKey("blocked");*/
			coords1.setX((int)(sprite.getX()+moveAmount));
			coords1.setY((int)(sprite.getY()));
			
			if(!coordlist.contains(coords1)) {
				if((int)sprite.getX()<3136)
					posX += moveAmount;
				if((int)sprite.getX()>=320 && (int)sprite.getX()<2880)
					camera.translate((int) moveAmount,0,0);
			}
		}
		if(keycode == Keys.UP) {
			/*collisionY = collisionLayer.getCell((int)(sprite.getX()/tileWidth), (int)(sprite.getY()/tileHeight))
					.getTile().getProperties().containsKey("blocked");*/
			coords1.setX((int)(sprite.getX()));
			coords1.setY((int)(sprite.getY()+moveAmount));
			
			if(!coordlist.contains(coords1)) {
				if((int)sprite.getY()<3136)
					posY += moveAmount;
				if((int)sprite.getY()>=256 && (int)sprite.getY()<2944)
					camera.translate(0,(int) moveAmount,0);
			}
		}
		if(keycode == Keys.DOWN) {
			/*collisionY = collisionLayer.getCell((int)(sprite.getX()/tileWidth), (int)(sprite.getY()/tileHeight))
					.getTile().getProperties().containsKey("blocked");*/
			coords1.setX((int)(sprite.getX()));
			coords1.setY((int)(sprite.getY()-moveAmount));
			
			if(!coordlist.contains(coords1)) {
				if((int)sprite.getY()>32)
					posY -= moveAmount;
				if((int)sprite.getY()>256 && (int)sprite.getY()<=2944)
					camera.translate(0,(int) -moveAmount,0);
			}
		}
		if(keycode == Keys.SPACE) {
			//JOptionPane.showMessageDialog(null, (int)sprite.getX()+"+"+(int)sprite.getY());
			//JOptionPane.showMessageDialog(null, coordlist.get(0));
			//sprBomb.setPosition(sprite.getX(), sprite.getY());
			//sprBomb.draw(sb);
			
			/*coords = new MyCoord();								//adding list of blocks
	        coordlist = new ArrayList<Bomber.MyCoord>();
	        FileHandle handle = Gdx.files.internal("BlockPoints.txt");
	        points = handle.readString();
	        Scanner scanner = new Scanner(points);
	        while(scanner.hasNextLine()) {
	        	coords = new MyCoord();
	        	String line = scanner.nextLine();
	        	xypt = line.split(",");
	        	//JOptionPane.showMessageDialog(null, Integer.parseInt(xypt[0])+" "+Integer.parseInt(xypt[1]));
	        	coords.setX(Integer.parseInt(xypt[0]));
	        	coords.setY(Integer.parseInt(xypt[1]));
	        	coordlist.add(coords);
	        }
	        scanner.close();*/
	        
	        /*for(MyCoord item: coordlist)
	        	JOptionPane.showMessageDialog(null, item.X+" "+item.Y);*/
	        
	        /*coords1.setX((int)posX);
	        coords1.setY((int)posY);
	        if(coordlist.contains(coords1))
	        	JOptionPane.showMessageDialog(null, (int)posX+" "+(int)posY+" present");*/
		}
        return true;
    }

    @Override
    public boolean keyUp(int keycode) {
    	//to pan camera - commented out
        /*if(keycode == Input.Keys.LEFT)
            camera.translate(-32,0);
        if(keycode == Input.Keys.RIGHT)
            camera.translate(32,0);
        if(keycode == Input.Keys.UP)
            camera.translate(0,32);
        if(keycode == Input.Keys.DOWN)
            camera.translate(0,-32);
        if(keycode == Input.Keys.NUM_1)
            tiledMap.getLayers().get(0).setVisible(!tiledMap.getLayers().get(0).isVisible());
        if(keycode == Input.Keys.NUM_2)
            tiledMap.getLayers().get(1).setVisible(!tiledMap.getLayers().get(1).isVisible());*/
        return false;
    }

    @Override
    public boolean keyTyped(char character) {

        return false;
    }

    @Override
    public boolean scrolled(int amount) {
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
	
	static class MyCoord{
	    private int X;
	    private int Y;

	    public MyCoord() {
	        this(0,0);
	    }        
	    public MyCoord(int X, int Y) {
	        this.X = X;
	        this.Y = Y;
	    }
	    public int getX() {
	        return X;
	    }
	    public int getY() {
	        return Y;
	    }
	    public void setX(int X) {
	        this.X = X;
	    }
	    public void setY(int Y) {
	        this.Y = Y;
	    }
	    
	    @Override
	    public boolean equals(Object obj) {
	    	if(obj == this)
	    		return true;
	    	if(obj == null || obj.getClass() != this.getClass())
	    		return false;
	    	MyCoord coord1 = (MyCoord) obj;
	    	return X == coord1.X && Y == coord1.Y;
	    }
	    
	}
}

