package com.tikisadventure.hud;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.tikisadventure.entities.player.Tiki;

public class HUD {

    private Tiki tiki;

    private ShapeRenderer shapeRenderer;

    private OrthographicCamera cameraHUD;
    private Viewport hudViewport;

    public HUD(Tiki tiki){

        this.tiki = tiki;

        shapeRenderer = new ShapeRenderer();

        cameraHUD = new OrthographicCamera();
        hudViewport = new ScreenViewport(cameraHUD);
    }

    public void update(float delta){}

    public void render(){

        hudViewport.apply();
        shapeRenderer.setProjectionMatrix(cameraHUD.combined);

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        drawHealthBar();
        shapeRenderer.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        drawBorder();
        shapeRenderer.end();
    }

    public void resize(int width, int height){
        hudViewport.update(width, height, true);
    }

    private void drawHealthBar(){

        float width = 200;
        float height = 20;

        float x = 20;
        float y = hudViewport.getWorldHeight() - 40;

        shapeRenderer.setColor(0.1f,0.1f,0.1f,0.8f);
        shapeRenderer.rect(x,y,width,height);

        float percent = Math.max(0, tiki.getVida()/tiki.getVida_max());

        if(percent > 0.5f)
            shapeRenderer.setColor(0.2f,0.8f,0.2f,1);
        else if(percent > 0.25f)
            shapeRenderer.setColor(0.8f,0.8f,0.2f,1);
        else
            shapeRenderer.setColor(0.8f,0.2f,0.2f,1);

        shapeRenderer.rect(x,y,width*percent,height);
    }

    private void drawBorder(){

        float width = 200;
        float height = 20;

        float x = 20;
        float y = hudViewport.getWorldHeight() - 40;

        shapeRenderer.setColor(0,0,0,1);
        shapeRenderer.rect(x,y,width,height);
    }
}
