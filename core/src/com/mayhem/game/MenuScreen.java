package com.mayhem.game;

//import javax.sound.midi.Sequence;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.moveTo;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.run;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.sequence;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.MoveToAction;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;


public class MenuScreen implements Screen {

	final MajorMayhemGame g;
	private OrthographicCamera camera;
	private SpriteBatch batch;
	private Sprite splash;
	
	//menu stuff
	private Stage stage;
	private TextButton buttonNew, buttonJoin, buttonExit;
	private Table table;
	private Label ipLabel, portLabel;
	private TextField ipField, portField;
	private Skin skin, skin2;
	private BitmapFont white, black;
	private TextureAtlas atlas;
	
	public MenuScreen(final MajorMayhemGame game) {
		g = game;
		
		camera = new OrthographicCamera();
		camera.setToOrtho(false, 640, 640);
		
		stage = new Stage();
		atlas = new TextureAtlas("button.pack");		//TODO didn't work with files in ui folder
		skin = new Skin(atlas);
		skin2 = new Skin(Gdx.files.internal("uiskin.json"));
		table = new Table(skin);
		table.setBounds(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		ipLabel = new Label("IP: ", skin2);
		portLabel = new Label("Port: ", skin2);
		ipField = new TextField("", skin2);
		portField = new TextField("", skin2);
		white = new BitmapFont(Gdx.files.internal("font/white.fnt"), false);
		black = new BitmapFont(Gdx.files.internal("font/black.fnt"), false);
		TextButtonStyle textButtonStyle = new TextButtonStyle();
		textButtonStyle.up = skin.getDrawable("button.up");
		textButtonStyle.down = skin.getDrawable("button.down");
		textButtonStyle.pressedOffsetX = 1;
		textButtonStyle.pressedOffsetY = -1;
		textButtonStyle.font = black;
		buttonNew = new TextButton("NEW GAME", textButtonStyle);
		buttonNew.addListener(new ClickListener() {
			 @Override
			public void clicked(InputEvent event, float x, float y) {
				 //TODO change this back!!
				 ((Game)Gdx.app.getApplicationListener()).setScreen(new Bomber(g, true, null, 0, 0));
				 //int score=50;
				 //((Game)Gdx.app.getApplicationListener()).setScreen(new ScoreScreen(g, score));
				 dispose();
			}
		 });
		 buttonJoin = new TextButton("JOIN GAME", textButtonStyle);
		 buttonJoin.addListener(new ClickListener() {
			 @Override
			 public void clicked(InputEvent event, float x, float y) {
				 ((Game)Gdx.app.getApplicationListener()).setScreen(new Bomber(g, false, ipField.getText(), Integer.parseInt(portField.getText()), 0));
				 dispose();
			 }
		 });
		 buttonExit = new TextButton("EXIT", textButtonStyle);
		 buttonExit.addListener(new ClickListener() {
			 @Override
			public void clicked(InputEvent event, float x, float y) {
				Gdx.app.exit();
			}
		 });
	}
	
	@Override
	public void render(float delta) {
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		
		g.batch.setProjectionMatrix(camera.combined);
		camera.update();
		
		g.batch.begin();
		g.splash.draw(g.batch);
		//Table.drawDebug(stage);
		g.batch.end();
		
		stage.act(delta);
		stage.draw();
		
		if(ipField.getText().isEmpty()||portField.getText().isEmpty())
			buttonJoin.setVisible(false);
		else if(!(ipField.getText().isEmpty())&&!(portField.getText().isEmpty()))
			buttonJoin.setVisible(true);
				
//		if(Gdx.input.isTouched()) {
//			g.setScreen(new Bomber(g));
//			dispose();
//		}
	}

	@Override
	public void resize(int width, int height) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void show() {
		 //batch = new SpriteBatch();
		 g.splash = new Sprite(new Texture("splash.jpg"));
		 
		 
		 Gdx.input.setInputProcessor(stage);
		 
		 black.setScale(0.75f);
		 
		 
		 buttonNew.pad(20);
		 buttonJoin.pad(20);
		 buttonExit.pad(20);
		 buttonExit.padLeft(57);
		 buttonExit.padRight(57);
		 //buttonExit.setSize(buttonJoin.getWidth(), buttonJoin.getHeight());
		 
		 //removing heading since splash already has name
//		 LabelStyle headingStyle = new LabelStyle(white, Color.WHITE);
//		 heading = new Label("Major Mayhem", headingStyle);
//		 heading.setFontScale(2);
//		 table.add(heading);
//		 table.row();
		 table.setPosition(0,60);
		 table.add(buttonNew);
		 table.row();
		 table.add(buttonJoin);
		 table.add(ipLabel);
		 table.add(ipField).width(150);
		 table.row();
		 table.add();
		 table.add(portLabel);
		 table.add(portField).width(100).align(Align.left);
		 table.row();
		 table.add(buttonExit);
		 //table.debug();		//TODO remove later
		 stage.addActor(table);
	}

	@Override
	public void hide() {
		dispose();
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
		//splash.getTexture().dispose();
		//skin.dispose();
		//stage.dispose();
		//batch.dispose();
	}

}
