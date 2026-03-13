package com.tikisadventure.screens;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;

import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.tikisadventure.combat.weapons.pistol.BasicGun;
import com.tikisadventure.entities.Entity;
import com.tikisadventure.entities.pickup.XPOrb;
import com.tikisadventure.entities.player.Tiki;
import com.tikisadventure.entities.enemies.Slime;
import com.tikisadventure.systems.EnemySpawner;
import com.tikisadventure.hud.HUD;

import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.tikisadventure.systems.ExperienceSystem;
import com.tikisadventure.systems.MapCollisionSystem;

import com.badlogic.gdx.Game;

public class GameScreen implements Screen {

    private Tiki tiki;

    private OrthographicCamera camera;
    private Viewport viewport;
    private OrthogonalTiledMapRenderer renderer;
    private TiledMap map;

    private Array<Entity> enemies= new Array<>();
    private Array<Entity> pickups = new Array<>();

    private EnemySpawner spawner;
    private HUD hud;

    private float damageCooldown = 0;

    private TiledMapTileLayer collisionLayer;

    private MapCollisionSystem mapCollision;

    private ExperienceSystem experienceSystem;

    private float restartTimer = 0f;

    private Game game;


    @Override
    public void show() {
        tiki = new Tiki();
        tiki.getPosicion().set(10,10);

        camera = new OrthographicCamera();
        viewport = new FitViewport(20, 20, camera);
        viewport.apply();

        map = new TmxMapLoader().load("mapa_prueba.tmx");
        renderer = new OrthogonalTiledMapRenderer(map, 1/16f);
        collisionLayer = (TiledMapTileLayer) map.getLayers().get("collisions");

        mapCollision = new MapCollisionSystem(collisionLayer);

        spawner = new EnemySpawner(enemies, collisionLayer);

        spawner.addEnemyType(() -> {
            Slime s = new Slime();
            s.crearSlime();
            return s;
        });

        tiki.setEnemies(enemies);

        tiki.getWeaponManager().addWeapon(new BasicGun(tiki));
        hud = new HUD(renderer.getBatch());

        experienceSystem = new ExperienceSystem();
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

        for(Entity pickup : pickups){
            pickup.render(batch, delta);
        }

        System.out.println(Gdx.graphics.getFramesPerSecond());

        batch.end();

        hud.render();
    }


    private void update(float delta){

        damageCooldown -= delta;

        Vector2 oldPos = new Vector2(tiki.getPosicion());

        tiki.update(delta, tiki);

        mapCollision.resolve(tiki, oldPos);

        spawner.update(delta, tiki);

        for(int i = pickups.size - 1; i >= 0; i--){

            Entity pickup = pickups.get(i);

            pickup.update(delta, tiki);

            if(!pickup.isAlive()){

                if(pickup instanceof XPOrb){
                    experienceSystem.addXP(((XPOrb) pickup).getValue());
                }

                pickups.removeIndex(i);
            }
        }

        for(int i = enemies.size - 1; i >= 0; i--){

            Entity enemy = enemies.get(i);

            if(enemy.isAlive()){
                enemy.update(delta, tiki);
            }else{

                Vector2 pos = enemy.getPosicion();
                pickups.add(new XPOrb(new Vector2(pos.x, pos.y), enemy.getExperience()));

                enemies.removeIndex(i);
            }
        }

        if(Gdx.input.isKeyPressed(Input.Keys.R)){

            restartTimer += delta;

            if(restartTimer > 1f){
                game.setScreen(new GameScreen(game));
            }

        }else{
            restartTimer = 0;
        }

        resolveEnemySeparation(delta);
        resolvePlayerCollision(delta);

        hud.update(tiki.getVida(), experienceSystem);

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

    @Override public void dispose(){
        map.dispose();
        renderer.dispose();
    }

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

    private boolean isBlocked(float x, float y){

        int tileX = (int)x;
        int tileY = (int)y;

        TiledMapTileLayer.Cell cell = collisionLayer.getCell(tileX, tileY);

        return cell != null;
    }

    public GameScreen(Game game){
        this.game = game;
    }
}
