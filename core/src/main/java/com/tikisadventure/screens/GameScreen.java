package com.tikisadventure.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import com.tikisadventure.combat.weapons.*;
import com.tikisadventure.entities.Entity;
import com.tikisadventure.entities.enemies.Slime;
import com.tikisadventure.entities.pickup.MiniHeal;
import com.tikisadventure.entities.pickup.Pickup;
import com.tikisadventure.entities.pickup.XPOrb;
import com.tikisadventure.entities.player.*; // Importamos el pack de personajes
import com.tikisadventure.abilities.DashAbility;
import com.tikisadventure.systems.EnemySpawner;
import com.tikisadventure.systems.WaveSystem;
import com.tikisadventure.projectile.ProjectileFactory;
import com.tikisadventure.projectile.Projectile;
import com.tikisadventure.effects.EffectManager;
import com.tikisadventure.effects.EffectType;
import com.tikisadventure.floors.FloorManager;

public class GameScreen implements Screen {

    private Game game;
    private Player player;

    // Ahora solo necesitamos los perfiles, las texturas viven dentro de ellos
    private CharacterProfile tikiProfile, mokoProfile, zukiProfile;

    private OrthographicCamera camera;
    private Viewport viewport;

    private Array<Entity> enemies = new Array<>();
    private Array<Pickup> pickups = new Array<>();

    private EnemySpawner spawner;
    private ShapeRenderer shapeRenderer;

    private WaveSystem waveSystem;
    private EffectManager effectManager;
    private ProjectileFactory projectileFactory;
    private FloorManager floorManager;
    private float waveCompleteTimer = 0f;
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

        TextureRegion bulletTex = new TextureRegion(new Texture("redbullet.png"));
        this.projectileFactory = new ProjectileFactory(effectManager, bulletTex);

        tikiProfile = CharacterFactory.create(CharacterType.TIKI, projectileFactory, effectManager);
        mokoProfile = CharacterFactory.create(CharacterType.MOKO, projectileFactory, effectManager);
        zukiProfile = CharacterFactory.create(CharacterType.ZUKI, projectileFactory, effectManager);

        player = new Player(tikiProfile);
        player.getPosicion().set(10, 10);

        camera = new OrthographicCamera();
        viewport = new FitViewport(20, 20, camera);

        floorManager = new FloorManager(true);
        
        waveSystem = new WaveSystem(waveSectionName);
        spawner = new EnemySpawner(enemies, floorManager, waveSystem);

        setupPlayerWeapons();

        shapeRenderer = new ShapeRenderer();
    }

    // Método auxiliar para no repetir código al equipar o cambiar personaje
    private void setupPlayerWeapons() {
        player.getWeaponManager().clear();

        player.getWeaponManager().addWeapon(new LaserGun(player,
            (pos, dir, spd, dmg, sz, tex, em, tType, tInt) ->
                new Projectile(player, pos, dir, spd, dmg, sz, tex, em, tType, tInt),
            effectManager
        ));

        player.getWeaponManager().addWeapon(new SimpleShotgun(player,
            (pos, dir, spd, dmg, sz, tex, em, tType, tInt) ->
                new Projectile(player, pos, dir, spd, dmg, sz, tex, em, tType, tInt),
            effectManager
        ));

        player.getWeaponManager().addWeapon(new SimplePistol(player,
            (pos, dir, spd, dmg, sz, tex, em, tType, tInt) ->
                new Projectile(player, pos, dir, spd, dmg, sz, tex, em, tType, tInt),
            effectManager
        ));

        player.getWeaponManager().addWeapon(new SimpleMachineGun(player,
            (pos, dir, spd, dmg, sz, tex, em, tType, tInt) ->
                new Projectile(player, pos, dir, spd, dmg, sz, tex, em, tType, tInt),
            effectManager
        ));

        player.getWeaponManager().addWeapon(new Grenade(player,
            (pos, dir, spd, dmg, sz, tex, em, tType, tInt) ->
                new Projectile(player, pos, dir, spd, dmg, sz, tex, em, tType, tInt),
            effectManager
        ));

        player.getWeaponManager().addWeapon(new RocketLauncher(player,
            (pos, dir, spd, dmg, sz, tex, em, tType, tInt) ->
                new Projectile(player, pos, dir, spd, dmg, sz, tex, em, tType, tInt),
            effectManager
        ));
    }

    @Override
    public void render(float delta) {
        update(delta);
        ScreenUtils.clear(0.1f, 0.1f, 0.2f, 1);

        float camOffset = floorManager.getCameraOffset();
        camera.position.set(player.getPosicion().x, player.getPosicion().y + camOffset, 0);
        camera.update();

        Batch batch = camera.combined == null ? null : new com.badlogic.gdx.graphics.g2d.SpriteBatch();
        if (batch == null) {
            batch = new com.badlogic.gdx.graphics.g2d.SpriteBatch();
        }

        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        floorManager.render(batch);

        for (Pickup pickup : pickups) {
            pickup.render(batch, delta);
        }

        for (Entity enemy : enemies) {
            if (enemy.isAlive()) enemy.render(batch, delta);
        }

        effectManager.render(batch);
        player.render(batch, delta);
        batch.end();

        renderDebugHitboxes();
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

        floorManager.update(delta);
        effectManager.update(delta);
        
        if (!floorManager.isTransitionActive()) {
            player.update(delta, enemies);
            spawner.update(delta, player);
            updateWaveLogic(delta);
            updatePickups(delta);
            updateEnemies(delta);
            resolvePhysics(delta);
        }

        handleDoorInteraction();

        if (Gdx.input.isKeyPressed(Input.Keys.R)) {
            restartTimer += delta;
            if (restartTimer > 1f) game.setScreen(new GameScreen(game));
        } else {
            restartTimer = 0;
        }
    }

    private void handleDoorInteraction() {
        if (doorAvailable && floorManager.isPlayerNearDoor(player.getPosicion())) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
                floorManager.useDoor();
                doorAvailable = false;
            }
        }

        if (floorManager.isTransitionComplete()) {
            floorManager.completeTransition();
            doorAvailable = false;
        }
    }

    // Permite cambiar de personaje manteniendo la posición
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

    private void updateWaveLogic(float delta) {
        if (waveSystem.getCurrentWave() == 0) {
            waveSystem.nextWave();
            spawner.resetForNewWave();
            waveInProgress = true;
        }

        if (waveInProgress && spawner.isWaveSpawningComplete() && enemies.size == 0) {
            if (!doorAvailable) {
                floorManager.showDoor();
                doorAvailable = true;
            }
            
            if (!waveSystem.hasMoreWaves()) {
                waveCompleteTimer += delta;
                if (waveCompleteTimer >= 1f) {
                    waveCompleteTimer = 0f;
                }
            } else {
                waveCompleteTimer += delta;
                if (waveCompleteTimer >= 1f) {
                    waveSystem.nextWave();
                    spawner.resetForNewWave();
                    waveCompleteTimer = 0f;
                }
            }
        } else {
            waveCompleteTimer = 0f;
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
        resolveMapCollision();
    }

    private void resolveMapCollision() {
        float x = player.getPosicion().x;
        float y = player.getPosicion().y;
        
        if (floorManager.isWall(x - 0.5f, y)) {
            player.getPosicion().x = (float)(Math.floor(x - 0.5f) + 1.5f);
        }
        if (floorManager.isWall(x + 0.5f, y)) {
            player.getPosicion().x = (float)(Math.floor(x + 0.5f) + 0.5f);
        }
        if (floorManager.isWall(x, y - 0.5f)) {
            player.getPosicion().y = (float)(Math.floor(y - 0.5f) + 1.5f);
        }
        if (floorManager.isWall(x, y + 0.5f)) {
            player.getPosicion().y = (float)(Math.floor(y + 0.5f) + 0.5f);
        }
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
    }

    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override public void dispose() {
        if (floorManager != null) floorManager.dispose();
        shapeRenderer.dispose();

        if (tikiProfile != null && tikiProfile.sprite != null) tikiProfile.sprite.getTexture().dispose();
        if (mokoProfile != null && mokoProfile.sprite != null) mokoProfile.sprite.getTexture().dispose();
        if (zukiProfile != null && zukiProfile.sprite != null) zukiProfile.sprite.getTexture().dispose();
    }
}
