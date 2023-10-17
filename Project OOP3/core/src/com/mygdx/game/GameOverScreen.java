package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;

public class GameOverScreen implements Screen {

    final SpaceShooterDame gameOver;
    OrthographicCamera camera;
    private static final int WIDTH = 350;
    private static final int HEIGHT = 100;
    public  GameOverScreen(final SpaceShooterDame gameOver){
        this.gameOver = gameOver;

        camera = new OrthographicCamera();
        camera.setToOrtho(false,800,480);
    }

    int score, hightscore;
    Texture gameOverpng;
    BitmapFont scoreFont;
    public GameOverScreen (SpaceShooterDame gameOver, int score){
        this.gameOver = gameOver;
        this.score = score;
        Preferences prefs = Gdx.app.getPreferences("spaceshootergame");
        this.hightscore = prefs.getInteger("highscore" ,0);

        if (score > hightscore){
            prefs.putInteger("highscore",score);
            prefs.flush();
        }
        gameOverpng = new Texture("gameover.png");
        scoreFont = new BitmapFont(Gdx.files.internal("font.otf"));
    }
    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0,0,0,1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();
        gameOver.batch.setProjectionMatrix(camera.combined);

        gameOver.batch.begin();
        gameOver.batch.draw(gameOverpng,0 ,0
        ,WIDTH,HEIGHT);
        gameOver.batch.end();

        if (Gdx.input.isKeyPressed(Input.Keys.SPACE) || Gdx.input.isTouched()){
            gameOver.setScreen(new GameOverScreen(gameOver));
            dispose();
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
    public void hide() {

    }

    @Override
    public void dispose() {

    }
}
