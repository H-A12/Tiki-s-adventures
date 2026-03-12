package com.tikisadventure.screens;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;

import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.tikisadventure.combat.weapons.pistol.BasicGun;
import com.tikisadventure.entities.Entity;
import com.tikisadventure.entities.player.Tiki;
import com.tikisadventure.entities.enemies.Slime;
import com.tikisadventure.systems.EnemySpawner;
import com.tikisadventure.hud.HUD;

public class GameScreen implements Screen {

    private Tiki tiki;

    private OrthographicCamera camera;
    private Viewport viewport;
    private OrthogonalTiledMapRenderer renderer;

    private Array<Entity> enemies= new Array<>();

    private EnemySpawner spawner;
    private HUD hud;

    private float damageCooldown = 0;

    @Override
    public void show() {

        tiki = new Tiki();

        camera = new OrthographicCamera();
        viewport = new FitViewport(32, 18, camera);
        viewport.apply();

        renderer = new OrthogonalTiledMapRenderer(new TiledMap(),1/16f);

        spawner = new EnemySpawner(enemies);

        spawner.addEnemyType(() -> {
            Slime s = new Slime();
            s.crearSlime();
            return s;
        });

        tiki.setEnemies(enemies);

        tiki.getWeaponManager().addWeapon(new BasicGun(tiki));

        hud = new HUD(tiki);
    }

    @Override
    public void render(float delta) {

        update(delta);

        ScreenUtils.clear(0.7f,0.7f,1,1);

        camera.position.set(tiki.getPosicion(),0);
        camera.update();

        renderer.setView(camera);
        renderer.render();

        Batch batch = renderer.getBatch();

        batch.begin();

        tiki.render(batch,delta);

        for(Entity enemy : enemies){
            if(enemy.isAlive()){
                enemy.render(batch, delta);
            }
        }

        batch.end();

        hud.render();
    }



    private void update(float delta){

        damageCooldown -= delta;

        tiki.update(delta, tiki);

        spawner.update(delta, tiki);

        for(Entity enemy : enemies){
            if(enemy.isAlive()){
                enemy.update(delta, tiki);
            }
        }

        resolveEnemySeparation(delta);
        resolvePlayerCollision(delta);

        hud.update(delta);

        if(tiki.getVida() <= 0){
            Gdx.app.exit();
        }
    }

    @Override
    public void resize(int width, int height) {

        viewport.update(width, height, true);

        hud.resize(width, height);
    }

    @Override public void pause(){}
    @Override public void resume(){}
    @Override public void hide(){}
    @Override public void dispose(){}

    private void resolveEnemySeparation(float delta){

        float separationStrength = 3f;

        for(int i = 0; i < enemies.size; i++){

            Entity a = enemies.get(i);
            if(!a.isAlive()) continue;

            for(int j = i + 1; j < enemies.size; j++){

                Entity b = enemies.get(j);
                if(!b.isAlive()) continue;

                Vector2 dir = new Vector2(
                    b.getPosicion().x - a.getPosicion().x,
                    b.getPosicion().y - a.getPosicion().y
                );

                float dist = dir.len();

                float minDist =
                    a.getHitboxActionTrigger().radius +
                        b.getHitboxActionTrigger().radius;

                if(dist < minDist && dist > 0){

                    dir.nor();

                    float force = (minDist - dist) * separationStrength * delta;

                    a.getPosicion().mulAdd(dir, -force);
                    b.getPosicion().mulAdd(dir, force);

                    a.actualizarHitboxes();
                    b.actualizarHitboxes();
                }
            }
        }
    }

    private void resolvePlayerCollision(float delta){

        float pushStrength = 4f;

        for(Entity enemy : enemies){

            if(!enemy.isAlive()) continue;

            Vector2 dir = new Vector2(
                enemy.getPosicion().x - tiki.getPosicion().x,
                enemy.getPosicion().y - tiki.getPosicion().y
            );

            float dist = dir.len();

            float minDist =
                enemy.getHitboxActionTrigger().radius +
                    tiki.getHitboxActionTrigger().radius;

            if(dist < minDist && dist > 0){

                dir.nor();

                float force = (minDist - dist) * pushStrength * delta;

                enemy.getPosicion().mulAdd(dir, force);
                tiki.getPosicion().mulAdd(dir, -force);

                enemy.actualizarHitboxes();
                tiki.actualizarHitboxes();

                if(damageCooldown <= 0){
                    tiki.receiveDamage(enemy.getDanyo());
                    damageCooldown = 0.5f;
                }
            }
        }
    }

}
