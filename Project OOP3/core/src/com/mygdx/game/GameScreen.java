package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Locale;

public class GameScreen implements Screen {

    private Camera camera;
    private Viewport viewport;
    private SpriteBatch batch;
    private TextureAtlas textureAtlas;
    private Texture gameOverTexture;
    private boolean gameOver = false;
    private Music gameMusic;
    private Sound effect;


    private TextureRegion[] backgrounds;
    private float backgroundHeight;
    private TextureRegion playerShipTextureRegion,playerShieldTextureRegion,
            enemyShipTextureRegion,enemyShieldTextureRegion,playerLaserTextureRegion ,enemyLaserTextureRegion;

    private float[] backgroundOffsets = {0,0,0,0};
    private float backgroundMaxScrollingSpeed;
    private float timeBetweenEnemySpawns = 1f;
    private float enemySpawnTimer =0;

    private final float WORLD_WIDTH = 72;
    private final float WORLD_HEIGHT = 128;
    private final float TOUCH_MOVEMENT = 5f;


    private PlayerShip playerShip;
    private LinkedList<EnemyShip> enemyShipList;
    private LinkedList<Laser> playerLaserList;
    private LinkedList<Laser> enemyLaserList;
    private int score = 0;
    BitmapFont font;

    float hudVerticalMargin, hudLeftX, hudRightX, hudCenterX, hudRow1Y, hudRow2Y, hudSectionWidth;

    GameScreen(){
        camera = new OrthographicCamera();
        viewport = new StretchViewport(WORLD_WIDTH,WORLD_HEIGHT,camera);

        textureAtlas = new TextureAtlas("img4.atlas");

        backgrounds = new TextureRegion[4];
        backgrounds[0] = textureAtlas.findRegion("Starscape00");
        backgrounds[1] = textureAtlas.findRegion("Starscape01");
        backgrounds[2] = textureAtlas.findRegion("Starscape02");
        backgrounds[3] = textureAtlas.findRegion("Starscape03");

        backgroundHeight = WORLD_HEIGHT * 2;
        backgroundMaxScrollingSpeed = (float) (WORLD_HEIGHT) /4;

        gameOverTexture = new Texture("gameover1.png");
        playerShipTextureRegion = textureAtlas.findRegion("playerShip1_blue");
        enemyShipTextureRegion = textureAtlas.findRegion("enemyBlack1");
        playerShieldTextureRegion = textureAtlas.findRegion("shield2");
        enemyShieldTextureRegion = textureAtlas.findRegion("shield1");
        enemyShieldTextureRegion.flip(false,true);

        playerLaserTextureRegion= textureAtlas.findRegion("laserBlue03");
        enemyLaserTextureRegion = textureAtlas.findRegion("laserRed03");



        playerShip = new PlayerShip(WORLD_WIDTH/2,WORLD_HEIGHT/4,
                10,10,48,6,
                0.4f,4,45,0.5f
                ,playerShipTextureRegion,playerShieldTextureRegion,playerLaserTextureRegion);

        enemyShipList = new LinkedList<>();


        playerLaserList = new LinkedList<>();
        enemyLaserList = new LinkedList<>();


        batch = new SpriteBatch();
        prepareHUD();
        gameMusic = Gdx.audio.newMusic(Gdx.files.internal("Its-Raining-Pixels.mp3"));
        effect = Gdx.audio.newSound(Gdx.files.internal("mi_explosion_03_hpx.wav"));
        gameMusic.setLooping(true);
        gameMusic.play();

    }
    private void prepareHUD(){
        FreeTypeFontGenerator fontGenerator = new FreeTypeFontGenerator(Gdx.files.internal("font.otf"));
        FreeTypeFontGenerator.FreeTypeFontParameter fontParameter = new FreeTypeFontGenerator.FreeTypeFontParameter();

        fontParameter.size =72;
        fontParameter.borderWidth = 3.6f;
        fontParameter.color = new Color(1,1,1,0.3f);
        fontParameter.borderColor = new Color(0,0,0,0.3f);

        font = fontGenerator.generateFont(fontParameter);
        font.getData().setScale(0.08f);

         hudVerticalMargin = font.getCapHeight()/2;
         hudLeftX = hudVerticalMargin;
         hudRightX = WORLD_WIDTH * 2/3 - hudLeftX;
         hudCenterX = WORLD_WIDTH /3;
         hudRow1Y = WORLD_HEIGHT - hudVerticalMargin;
         hudRow2Y = hudRow1Y - hudVerticalMargin - font.getCapHeight();
         hudSectionWidth = WORLD_WIDTH /3;
    }

    @Override
    public void render(float deltaTime) {
        if(!gameOver) {
            batch.begin();

            renderBackground(deltaTime);
            detectInput(deltaTime);
            playerShip.update(deltaTime);
            spawnEnemyShips(deltaTime);

            ListIterator<EnemyShip> enemyShipListIterator = enemyShipList.listIterator();
            while (enemyShipListIterator.hasNext()) {
                EnemyShip enemyShip = enemyShipListIterator.next();
                moveEnemy(enemyShip, deltaTime);
                enemyShip.update(deltaTime);
                enemyShip.draw(batch);
            }
            playerShip.draw(batch);

            renderLasers(deltaTime);
            detectCollisions();

            updateAndRenderHUD();

            batch.end();
            if (gameOver) {
                batch.begin();

                float x = (WORLD_WIDTH - gameOverTexture.getWidth() / 2);
                float y = (WORLD_HEIGHT - gameOverTexture.getHeight() / 2);

                batch.draw(gameOverTexture, x, y, 80, 80);
                batch.end();
            }
        }
    }

    private void updateAndRenderHUD(){
        font.draw(batch, "Score" , hudLeftX, hudRow1Y, hudSectionWidth, Align.left, false);
        font.draw(batch, "Shield", hudCenterX, hudRow1Y,hudSectionWidth,Align.center,false);
        font.draw(batch, "Lives" , hudRightX,hudRow1Y,hudSectionWidth,Align.right,false);

        font.draw(batch, String.format(Locale.getDefault(), "%06d", score),hudLeftX,hudRow2Y,
                hudSectionWidth, Align.left, false);
        font.draw(batch, String.format(Locale.getDefault(), "%02d", playerShip.shield),hudCenterX, hudRow2Y,hudSectionWidth,Align.center,false);
        font.draw(batch, String.format(Locale.getDefault(), "%02d",playerShip.lives),hudRightX,hudRow2Y,hudSectionWidth,Align.right,false);
    }

    private void spawnEnemyShips(float deltaTime) {
        enemySpawnTimer += deltaTime;
        if (enemySpawnTimer > timeBetweenEnemySpawns) {
            enemyShipList.add(new EnemyShip(SpaceShooterDame.random.nextFloat() * (WORLD_WIDTH - 10) + 5,
                    WORLD_HEIGHT - 5,
                    10, 10, 48, 1, 0.3f, 5, 55, 0.6f
                    , enemyShipTextureRegion, enemyShieldTextureRegion, enemyLaserTextureRegion));
            enemySpawnTimer -= timeBetweenEnemySpawns;
        }
    }

    private void detectInput(float deltaTime){

        float leftLimit, rightLimit, upLimit, downLimit;
        leftLimit = -playerShip.boundingBox.x;
        downLimit = -playerShip.boundingBox.y;
        rightLimit = WORLD_WIDTH - playerShip.boundingBox.x - playerShip.boundingBox.width;
        upLimit = (float) WORLD_HEIGHT/2 - playerShip.boundingBox.y - playerShip.boundingBox.height;

        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT) && rightLimit > 0){
            playerShip.translate(Math.min(playerShip.movementSpeed*deltaTime, rightLimit),0f);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.UP) && upLimit > 0){
            playerShip.translate(0f,Math.min(playerShip.movementSpeed*deltaTime, upLimit));
        }
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT) && leftLimit < 0){
            playerShip.translate(Math.max(-playerShip.movementSpeed*deltaTime, leftLimit),0f);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN) && downLimit < 0){
            playerShip.translate(0f,Math.max(-playerShip.movementSpeed*deltaTime, downLimit));
        }

        if (Gdx.input.isTouched()) {
            float xTouchPixels = Gdx.input.getX();
            float yTouchPixels = Gdx.input.getY();

            Vector2 touchPoint = new Vector2(xTouchPixels,yTouchPixels);
            touchPoint = viewport.unproject(touchPoint);
            Vector2 playerShipCentre = new Vector2(playerShip.boundingBox.x + playerShip.boundingBox.width/2,
                    playerShip.boundingBox.y + playerShip.boundingBox.height/2);
            float touchDistance = touchPoint.dst(playerShipCentre);
            if (touchDistance > TOUCH_MOVEMENT){
                float xTouchDifference = touchPoint.x - playerShipCentre.x;
                float yTouchDifference = touchPoint.y - playerShipCentre.y;

                float xMove = xTouchDifference / touchDistance * playerShip.laserMovementSpeed*deltaTime;
                float yMove = yTouchDifference / touchDistance * playerShip.laserMovementSpeed*deltaTime;

                if (xMove > 0) xMove = Math.min(xMove, rightLimit);
                else xMove = Math.max(xMove,leftLimit);

                if (yMove > 0) yMove = Math.min(yMove, upLimit);
                else yMove = Math.max(yMove,downLimit);

                playerShip.translate(xMove,yMove);
            }

        }
    }

    private void moveEnemy(EnemyShip enemyShip,float deltaTime){

        float leftLimit, rightLimit, upLimit, downLimit;
        leftLimit = -enemyShip.boundingBox.x;
        downLimit = (float) WORLD_HEIGHT/2-enemyShip.boundingBox.y;
        rightLimit = WORLD_WIDTH - enemyShip.boundingBox.x - enemyShip.boundingBox.width;
        upLimit = WORLD_HEIGHT - enemyShip.boundingBox.y - enemyShip.boundingBox.height;
        float xMove = enemyShip.getDirectionVector().x * enemyShip.laserMovementSpeed*deltaTime;
        float yMove = enemyShip.getDirectionVector().y * enemyShip.laserMovementSpeed*deltaTime;

        if (xMove > 0) xMove = Math.min(xMove, rightLimit);
        else xMove = Math.max(xMove,leftLimit);

        if (yMove > 0) yMove = Math.min(yMove, upLimit);
        else yMove = Math.max(yMove,downLimit);

        enemyShip.translate(xMove,yMove);


    }
    private void detectCollisions() {
        ListIterator<Laser> laserListIterator = playerLaserList.listIterator();
        while (laserListIterator.hasNext()) {
            Laser laser = laserListIterator.next();
            ListIterator<EnemyShip> enemyShipListIterator = enemyShipList.listIterator();
            while (enemyShipListIterator.hasNext()) {
                EnemyShip enemyShip = enemyShipListIterator.next();

                if (enemyShip.intersects(laser.boundingBox)) {
                    if (enemyShip.hitAndCheckDestroyed(laser)){
                        effect.play();
                        enemyShipListIterator.remove();
                        score += 100;
                    }
                    laserListIterator.remove();
                    break;
                }
            }
        }

        laserListIterator =enemyLaserList.listIterator();
        while (laserListIterator.hasNext()) {
            Laser laser = laserListIterator.next();
            if(playerShip.intersects(laser.boundingBox)){
                if( playerShip.hitAndCheckDestroyed(laser)){

                    playerShip.shield = 10;
                    playerShip.lives--;
                    if(playerShip.lives == 0) {
                        gameOver = true;
                    }
                }
                laserListIterator.remove();
            }
        }
    }
    private void renderLasers(float deltaTime){
        if (playerShip.canFireLaser()){
            Laser[] lasers = playerShip.fireLasers();
            for (Laser laser:lasers){
                playerLaserList.add(laser);
            }
        }
        ListIterator<EnemyShip> enemyShipListIterator = enemyShipList.listIterator();
        while (enemyShipListIterator.hasNext()){
            EnemyShip enemyShip = enemyShipListIterator.next();
            if (enemyShip.canFireLaser()) {
                Laser[] lasers = enemyShip.fireLasers();
                enemyLaserList.addAll(Arrays.asList(lasers));
            }
        }
        ListIterator<Laser> iterator =playerLaserList.listIterator();
        while (iterator.hasNext()){
            Laser laser = iterator.next();
            laser.draw(batch);
            laser.boundingBox.y += laser.movementSpeed*deltaTime;
            if(laser.boundingBox.y > WORLD_HEIGHT){
                iterator.remove();
            }
        }
        iterator =enemyLaserList.listIterator();
        while (iterator.hasNext()){
            Laser laser = iterator.next();
            laser.draw(batch);
            laser.boundingBox.y -= laser.movementSpeed*deltaTime;
            if(laser.boundingBox.y + laser.boundingBox.height< 0){
                iterator.remove();
            }
        }
    }
    private void renderBackground(float deltaTime){
        backgroundOffsets[0] += deltaTime * backgroundMaxScrollingSpeed /8;
        backgroundOffsets[1] += deltaTime * backgroundMaxScrollingSpeed /4;
        backgroundOffsets[2] += deltaTime * backgroundMaxScrollingSpeed /2;
        backgroundOffsets[3] += deltaTime * backgroundMaxScrollingSpeed;

        for (int layer =0; layer <backgroundOffsets.length; layer++){
            if(backgroundOffsets[layer] > WORLD_HEIGHT){
                backgroundOffsets[layer] = 0;
            }
            batch.draw(backgrounds[layer] ,0,-backgroundOffsets[layer],WORLD_WIDTH,backgroundHeight);
        }

    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width,height,true);
        batch.setProjectionMatrix(camera.combined);
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
    public void show() {

    }

    @Override
    public void dispose() {
        gameMusic.dispose();
    }
}

