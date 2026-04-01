package com.tikisadventure.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import com.tikisadventure.combat.projectiles.ProjectileFactory;
import com.tikisadventure.combat.weapons.WeaponFactory;
import com.tikisadventure.combat.weapons.WeaponManager;
import com.tikisadventure.core.Assets;
import com.tikisadventure.entities.base.Entity;
import com.tikisadventure.entities.pickup.MiniHeal;
import com.tikisadventure.entities.pickup.Pickup;
import com.tikisadventure.entities.pickup.XPOrb;
import com.tikisadventure.entities.player.Player;
import com.tikisadventure.entities.player.CharacterProfile;
import com.tikisadventure.entities.player.CharacterFactory;
import com.tikisadventure.ui.HUD;
import com.tikisadventure.systems.EnemySpawner;
import com.tikisadventure.systems.WaveSystem;
import com.tikisadventure.effects.EffectManager;
import com.tikisadventure.floors.FloorManager;

public class GameScreen implements Screen {

    private Game game;
    private Player player;
    private CharacterProfile tikiProfile, mokoProfile, zukiProfile;
    private OrthographicCamera camera;
    private Viewport viewport;
    private Array<Entity> enemies = new Array<>();
    private Array<Pickup> pickups = new Array<>();
    private EnemySpawner spawner;
    private HUD hud;
    private ShapeRenderer shapeRenderer;
    private WaveSystem waveSystem;
    private EffectManager effectManager;
    private ProjectileFactory projectileFactory;
    private WeaponFactory weaponFactory;
    private FloorManager floorManager;
    private boolean waveInProgress = false;
    private boolean doorAvailable = false;
    private String waveSectionName = "default";
    private float damageCooldown = 0;
    private float restartTimer = 0f;

    public GameScreen(Game game) {
        this.game = game;
    }

    public GameScreen(Game game, String waveSection) {
        this.game = game;
        this.waveSectionName = waveSection;
    }

    @Override
    public void show() {
        effectManager = new EffectManager(300);
        this.projectileFactory = new ProjectileFactory(effectManager, Assets.getRegion("shared", "RedBullet"));
        this.weaponFactory = new WeaponFactory(projectileFactory, effectManager);

        tikiProfile = CharacterFactory.create("TIKI", projectileFactory, effectManager);
        mokoProfile = CharacterFactory.create("MOKO", projectileFactory, effectManager);
        zukiProfile = CharacterFactory.create("ZUKI", projectileFactory, effectManager);

        player = new Player(tikiProfile);
        player.getPosicion().set(10, 10);

        camera = new OrthographicCamera();
        viewport = new FitViewport(20, 20, camera);
        floorManager = new FloorManager(true);
        waveSystem = new WaveSystem(waveSectionName);
        spawner = new EnemySpawner(enemies, floorManager, waveSystem);

        setupPlayerWeapons();

        Batch hudBatch = new SpriteBatch();
        hud = new HUD(hudBatch);
        shapeRenderer = new ShapeRenderer();
    }

    private void setupPlayerWeapons() {
        WeaponManager manager = player.getWeaponFactory();
        manager.clear();
        //manager.addWeapon(weaponFactory.createWeapon("laser_gun", player));
        //manager.addWeapon(weaponFactory.createWeapon("shotgun", player));
        //manager.addWeapon(weaponFactory.createWeapon("handgun", player));
        //manager.addWeapon(weaponFactory.createWeapon("machinegun", player));
        //manager.addWeapon(weaponFactory.createWeapon("bomb", player));
        //manager.addWeapon(weaponFactory.createWeapon("rocket_launcher", player));
        manager.addWeapon(weaponFactory.createWeapon("sword", player));
        manager.addWeapon(weaponFactory.createWeapon("sword", player));
        manager.addWeapon(weaponFactory.createWeapon("sword", player));
        manager.addWeapon(weaponFactory.createWeapon("sword", player));
    }


    @Override
    public void render(float delta) {
        update(delta);
        ScreenUtils.clear(0.1f, 0.1f, 0.2f, 1);
        float camOffset = floorManager.isTransitionActive() ? floorManager.getCameraOffset() : 0;
        camera.position.set(player.getPosicion().x, player.getPosicion().y + camOffset, 0);
        camera.update();
        floorManager.renderMap(camera);
        Batch batch = new SpriteBatch();
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        floorManager.renderEntities(batch);
        for (Pickup pickup : pickups) pickup.render(batch, delta);
        for (Entity enemy : enemies) if (enemy.isAlive()) enemy.render(batch, delta);
        effectManager.render(batch);
        player.render(batch, delta);
        batch.end();
        renderDebugHitboxes();
        hud.render();
    }

    private void update(float delta) {
        if (player.getVida() <= 0) { Gdx.app.exit(); return; }
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1)) switchCharacter(tikiProfile);
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_2)) switchCharacter(mokoProfile);
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_3)) switchCharacter(zukiProfile);
        damageCooldown -= delta;
        floorManager.update(delta);
        effectManager.update(delta);

        if (!floorManager.isTransitionActive()) {
            boolean nearDoor = doorAvailable && floorManager.isPlayerNearDoor(player.getPosicion());
            if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE) && nearDoor) {
                floorManager.useDoor();
                doorAvailable = false;
            } else {
                player.update(delta, enemies);
                spawner.update(delta, player);
                updateWaveLogic(delta);
                updatePickups(delta);
                updateEnemies(delta);
                resolvePhysics(delta);
            }
        }
        if (floorManager.isTransitionComplete()) {
            floorManager.completeTransition();
            pickups.clear();
            enemies.clear();
            doorAvailable = false;
            waveInProgress = false;
            waveSystem.nextWave();
            int[] spawnPos = floorManager.findValidSpawnPosition(8, 12, 8, 12);
            player.getPosicion().set(spawnPos[0], spawnPos[1]);
        }
        if (!waveInProgress && !floorManager.isTransitionActive()) {
            spawner.resetForNewWave();
            waveInProgress = true;
            Gdx.app.log("WAVE", "Started wave: " + waveSystem.getCurrentWave());
        }
        if (Gdx.input.isKeyPressed(Input.Keys.R)) {
            restartTimer += delta;
            if (restartTimer > 1f) game.setScreen(new GameScreen(game));
        } else { restartTimer = 0; }
        hud.update(player.getVida(), player.getExperienceSystem());
    }

    private void switchCharacter(CharacterProfile newProfile) {
        Vector2 pos = new Vector2(player.getPosicion());
        player = new Player(newProfile);
        player.getPosicion().set(pos);
        setupPlayerWeapons();
    }

    private void updatePickups(float delta) { for (int i = pickups.size - 1; i >= 0; i--) { Pickup p = pickups.get(i); p.update(delta, player); if (!p.isAlive()) pickups.removeIndex(i); } }
    private void updateEnemies(float delta) { for (int i = enemies.size - 1; i >= 0; i--) { Entity enemy = enemies.get(i); if (enemy.isAlive()) { enemy.update(delta, player); resolveEnemyWallCollision(enemy); } else { spawnDrop(enemy.getPosicion(), enemy.getExperience()); enemies.removeIndex(i); } } }

    private void resolvePhysics(float delta) {
        resolveEnemySeparation(delta);
        resolvePlayerCollision(delta);
        resolveMapCollision();
    }

    private void resolveMapCollision() {
        float x = player.getPosicion().x;
        float y = player.getPosicion().y;
        float halfSize = 0.5f;
        if (floorManager.isWall(x - halfSize, y)) player.getPosicion().x = (float)Math.floor(x - halfSize) + 1 + halfSize;
        if (floorManager.isWall(x + halfSize, y)) player.getPosicion().x = (float)Math.floor(x + halfSize) - halfSize;
        if (floorManager.isWall(x, y - halfSize)) player.getPosicion().y = (float)Math.floor(y - halfSize) + 1 + halfSize;
        if (floorManager.isWall(x, y + halfSize)) player.getPosicion().y = (float)Math.floor(y + halfSize) - halfSize;
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

    private void resolveEnemyWallCollision(Entity entity) {
        float x = entity.getPosicion().x;
        float y = entity.getPosicion().y;
        float halfSize = 0.4f;
        if (floorManager.isWall(x - halfSize, y)) entity.getPosicion().x = (float)Math.floor(x - halfSize) + 1 + halfSize;
        if (floorManager.isWall(x + halfSize, y)) entity.getPosicion().x = (float)Math.floor(x + halfSize) - halfSize;
        if (floorManager.isWall(x, y - halfSize)) entity.getPosicion().y = (float)Math.floor(y - halfSize) + 1 + halfSize;
        if (floorManager.isWall(x, y + halfSize)) entity.getPosicion().y = (float)Math.floor(y + halfSize) - halfSize;
    }

    private void updateWaveLogic(float delta) {
        if (waveInProgress && spawner.isWaveSpawningComplete() && enemies.size == 0) {
            if (!doorAvailable && floorManager.getCurrentFloor() < floorManager.getTotalFloors()) {
                floorManager.showDoor();
                doorAvailable = true;
            }
        }
    }

    private void spawnDrop(Vector2 pos, int exp) {
        if (Math.random() < 0.8f) { pickups.add(new XPOrb(new Vector2(pos), exp)); }
        else if (Math.random() < 0.1f) { pickups.add(new MiniHeal(new Vector2(pos))); }
    }

    private void renderDebugHitboxes() {
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(0, 1, 0, 1);
        shapeRenderer.circle(player.getHitboxActionTrigger().x, player.getHitboxActionTrigger().y, player.getHitboxActionTrigger().radius, 32);
        for (Entity e : enemies) shapeRenderer.circle(e.getHitboxActionTrigger().x, e.getHitboxActionTrigger().y, e.getHitboxActionTrigger().radius, 32);
        shapeRenderer.end();
    }

    @Override public void resize(int width, int height) { viewport.update(width, height, true); hud.resize(width, height); }
    @Override public void pause() {} @Override public void resume() {} @Override public void hide() {}
    @Override public void dispose() {
        if (floorManager != null) floorManager.dispose();
        shapeRenderer.dispose();
    }
}
