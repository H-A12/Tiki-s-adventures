package com.tikisadventure.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import com.tikisadventure.combat.weapons.SimpleMachineGun;
import com.tikisadventure.combat.weapons.SimplePistol;
import com.tikisadventure.combat.weapons.SimpleShotgun;
import com.tikisadventure.entities.Entity;
import com.tikisadventure.entities.enemies.Slime;
import com.tikisadventure.entities.pickup.MiniHeal;
import com.tikisadventure.entities.pickup.Pickup;
import com.tikisadventure.entities.pickup.XPOrb;
import com.tikisadventure.entities.player.*;
import com.tikisadventure.abilities.DashAbility;
import com.tikisadventure.hud.HUD;
import com.tikisadventure.systems.EnemySpawner;
import com.tikisadventure.systems.MapCollisionSystem;
import com.tikisadventure.projectile.ProjectileFactory;

public class GameScreen implements Screen {

    private Game game;
    private Player player;

    private CharacterProfile tikiProfile, mokoProfile, zukiProfile;

    private OrthographicCamera camera;
    private Viewport viewport;
    private OrthogonalTiledMapRenderer mapRenderer;
    private TiledMap map;
    private TiledMapTileLayer collisionLayer;

    private Array<Entity> enemies = new Array<>();
    private Array<Pickup> pickups = new Array<>();

    private EnemySpawner spawner;
    private HUD hud;
    private MapCollisionSystem mapCollision;
    private ShapeRenderer shapeRenderer;

    private float damageCooldown = 0;
    private float restartTimer = 0f;

    public GameScreen(Game game) {
        this.game = game;
    }

    @Override
    public void show() {
        DashAbility dash = new DashAbility();

        tikiProfile = CharacterFactory.create(CharacterType.TIKI, dash);
        mokoProfile = CharacterFactory.create(CharacterType.MOKO, dash);
        zukiProfile = CharacterFactory.create(CharacterType.ZUKI, dash);

        player = new Player(tikiProfile);
        player.getPosicion().set(10, 10);

        camera = new OrthographicCamera();
        viewport = new FitViewport(20, 20, camera);

        map = new TmxMapLoader().load("mapa_prueba.tmx");
        mapRenderer = new OrthogonalTiledMapRenderer(map, 1/16f);
        collisionLayer = (TiledMapTileLayer) map.getLayers().get("collisions");
        mapCollision = new MapCollisionSystem(collisionLayer);

        spawner = new EnemySpawner(enemies, collisionLayer);
        spawner.addEnemyType(() -> {
            Slime s = new Slime();
            s.crearSlime();
            return s;
        });

        setupPlayerWeapons();

        hud = new HUD(mapRenderer.getBatch());
        shapeRenderer = new ShapeRenderer();
    }

    private void setupPlayerWeapons() {
        player.getWeaponManager().clear();

        // CAMBIO: La lambda ahora recibe 'tex' (la textura que el arma prefiere usar)
        player.getWeaponManager().addWeapon(new SimpleMachineGun(player,
            (pos, dir, spd, dmg, sz, tex) -> ProjectileFactory.createBullet(player, pos, dir, spd, dmg, sz, tex)
        ));

        player.getWeaponManager().addWeapon(new SimpleShotgun(player,
            (pos, dir, spd, dmg, sz, tex) -> ProjectileFactory.createBullet(player, pos, dir, spd, dmg, sz, tex)
        ));

        player.getWeaponManager().addWeapon(new SimplePistol(player,
            (pos, dir, spd, dmg, sz, tex) -> ProjectileFactory.createBullet(player, pos, dir, spd, dmg, sz, tex)
        ));
    }

    @Override
    public void render(float delta) {
        update(delta);
        ScreenUtils.clear(0.1f, 0.1f, 0.2f, 1);

        camera.position.set(player.getPosicion(), 0);
        camera.update();

        mapRenderer.setView(camera);
        mapRenderer.render();

        Batch batch = mapRenderer.getBatch();
        batch.begin();

        for (Pickup pickup : pickups) {
            pickup.render(batch, delta);
        }

        for (Entity enemy : enemies) {
            if (enemy.isAlive()) enemy.render(batch, delta);
        }

        player.render(batch, delta);
        batch.end();

        renderDebugHitboxes();
        hud.render();
    }

    private void update(float delta) {
        if (player.getVida() <= 0) {
            Gdx.app.exit();
            return;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1)) switchCharacter(tikiProfile);
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_2)) switchCharacter(mokoProfile);
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_3)) switchCharacter(zukiProfile);

        damageCooldown -= delta;
        Vector2 oldPos = new Vector2(player.getPosicion());

        player.update(delta, enemies);
        mapCollision.resolve(player, oldPos);
        spawner.update(delta, player);
        updatePickups(delta);
        updateEnemies(delta);
        resolvePhysics(delta);

        if (Gdx.input.isKeyPressed(Input.Keys.R)) {
            restartTimer += delta;
            if (restartTimer > 1f) game.setScreen(new GameScreen(game));
        } else {
            restartTimer = 0;
        }

        hud.update(player.getVida(), player.getExperienceSystem());
    }

    private void switchCharacter(CharacterProfile newProfile) {
        Vector2 pos = new Vector2(player.getPosicion());
        player = new Player(newProfile);
        player.getPosicion().set(pos);
        setupPlayerWeapons();
    }

    private void updatePickups(float delta) {
        for (int i = pickups.size - 1; i >= 0; i--) {
            Pickup p = pickups.get(i);
            p.update(delta, player);
            if (!p.isAlive()) pickups.removeIndex(i);
        }
    }

    private void updateEnemies(float delta) {
        for (int i = enemies.size - 1; i >= 0; i--) {
            Entity enemy = enemies.get(i);
            if (enemy.isAlive()) {
                enemy.update(delta, player);
            } else {
                spawnDrop(enemy.getPosicion(), enemy.getExperience());
                enemies.removeIndex(i);
            }
        }
    }

    private void spawnDrop(Vector2 pos, int exp) {
        if (Math.random() < 0.8f) {
            pickups.add(new XPOrb(new Vector2(pos), exp));
        } else if (Math.random() < 0.1f) {
            pickups.add(new MiniHeal(new Vector2(pos)));
        }
    }

    private void resolvePhysics(float delta) {
        resolveEnemySeparation(delta);
        resolvePlayerCollision(delta);
        mapCollision.resolve(player, player.getPosicion());
    }

    private void resolveEnemySeparation(float delta) {
        float strength = 3f;
        for (int i = 0; i < enemies.size; i++) {
            Entity a = enemies.get(i);
            for (int j = i + 1; j < enemies.size; j++) {
                Entity b = enemies.get(j);
                float dist = a.getPosicion().dst(b.getPosicion());
                float minDist = a.getHitboxActionTrigger().radius + b.getHitboxActionTrigger().radius;
                if (dist < minDist && dist > 0) {
                    Vector2 dir = new Vector2(b.getPosicion()).sub(a.getPosicion()).nor();
                    float force = (minDist - dist) * strength * delta;
                    a.getPosicion().mulAdd(dir, -force);
                    b.getPosicion().mulAdd(dir, force);
                }
            }
        }
    }

    private void resolvePlayerCollision(float delta) {
        float push = 4f;
        for (Entity enemy : enemies) {
            float dist = enemy.getPosicion().dst(player.getPosicion());
            float minDist = enemy.getHitboxActionTrigger().radius + player.getHitboxActionTrigger().radius;
            if (dist < minDist && dist > 0) {
                Vector2 dir = new Vector2(enemy.getPosicion()).sub(player.getPosicion()).nor();
                float force = (minDist - dist) * push * delta;
                enemy.getPosicion().mulAdd(dir, force);
                player.getPosicion().mulAdd(dir, -force);

                if (damageCooldown <= 0) {
                    player.receiveDamage(enemy.getDanyo());
                    damageCooldown = 0.5f;
                }
            }
        }
    }

    private void renderDebugHitboxes() {
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(0, 1, 0, 1);
        shapeRenderer.circle(player.getHitboxActionTrigger().x, player.getHitboxActionTrigger().y, player.getHitboxActionTrigger().radius, 32);
        for (Entity e : enemies) {
            shapeRenderer.circle(e.getHitboxActionTrigger().x, e.getHitboxActionTrigger().y, e.getHitboxActionTrigger().radius, 32);
        }
        shapeRenderer.end();
    }

    @Override public void resize(int width, int height) {
        viewport.update(width, height, true);
        hud.resize(width, height);
    }

    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override public void dispose() {
        map.dispose();
        mapRenderer.dispose();
        shapeRenderer.dispose();

        if (tikiProfile != null) tikiProfile.sprite.getTexture().dispose();
        if (mokoProfile != null) mokoProfile.sprite.getTexture().dispose();
        if (zukiProfile != null) zukiProfile.sprite.getTexture().dispose();
    }
}
