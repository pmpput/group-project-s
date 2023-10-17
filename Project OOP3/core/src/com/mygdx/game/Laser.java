package com.mygdx.game;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;

public class Laser {
    Rectangle boundingBox;
    float movementSpeed;

    TextureRegion textureRegion;

    public Laser(float xCenter, float yBottom,
                 float width, float height, float movementSpeed, TextureRegion textureRegion) {
        this.boundingBox = new Rectangle(xCenter - width/2,yBottom ,width,height);

        this.movementSpeed = movementSpeed;
        this.textureRegion = textureRegion;
    }
    public void draw(Batch batch){
        batch.draw(textureRegion,boundingBox.x,boundingBox.y,boundingBox.width,boundingBox.height);

    }
//    public Rectangle getBoundingBox(){
//        return boundingBox;
//    }
}
