package com.tikisadventure.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import com.tikisadventure.combat.projectiles.Projectile;
import com.tikisadventure.combat.projectiles.ProjectileFactory;
import com.tikisadventure.combat.weapons.WeaponFactory;
import com.tikisadventure.combat.weapons.WeaponManager;
import com.tikisadventure.core.Assets;
import com.tikisadventure.entities.base.Entity;
import com.tikisadventure.entities.enemies.ConfigurableEnemy;
import com.tikisadventure.entities.pickup.MiniHeal;
import com.tikisadventure.entities.pickup.Pickup;
import com.tikisadventure.entities.pickup.XPOrb;
import com.tikisadventure.entities.player.Player;
import com.tikisadventure.entities.player.CharacterProfile;
import com.tikisadventure.entities.player.CharacterFactory;
import com.tikisadventure.ui.HUD;
import com.tikisadventure.systems.RenderSystem;
import com.tikisadventure.systems.EnemySpawner;
import com.tikisadventure.systems.WaveSystem;
import com.tikisadventure.systems.PhysicsSystem;
import com.tikisadventure.systems.CombatSystem;
import com.tikisadventure.systems.CombatFeedbackSystem;
import com.tikisadventure.systems.MovementSystem;
import com.tikisadventure.ui.HUD;
import com.tikisadventure.ui.TrajectoryRenderer;
import com.tikisadventure.effects.EffectManager;
import com.tikisadventure.floors.FloorManager;

public class GameScreen implements Screen {

    private final Game game;
    private Player player;
    private OrthographicCamera camera;
    private Viewport viewport;
    private final Array<Entity> enemies = new Array<>();
    private final Array<Pickup> pickups = new Array<>();
    private EnemySpawner spawner;
    private HUD hud;
    private ShapeRenderer shapeRenderer;
    private SpriteBatch batch;
    private TrajectoryRenderer trajectoryRenderer;
    private RenderSystem renderSystem;
    private WaveSystem waveSystem;
    private EffectManager effectManager;
    private ProjectileFactory projectileFactory;
    private WeaponFactory weaponFactory;
    private FloorManager floorManager;
    private PhysicsSystem physicsSystem;
    private CombatSystem combatSystem;
    private CombatFeedbackSystem combatFeedbackSystem;
    private MovementSystem movementSystem;

    public static boolean isGamePaused = false;
    private int lastKnownLevel = 1;

    private boolean waveInProgress = false;
    private String waveSectionName;

    private float damageCooldown = 0;
    private float restartTimer = 0f;

    private final com.badlogic.gdx.math.Vector3 mouseWorld3 = new com.badlogic.gdx.math.Vector3();
    private final Vector2 mouseWorld = new Vector2();

    public GameScreen(Game game) { this.game = game; }

    // Piscinas de reciclaje
    private final com.badlogic.gdx.utils.Pool<XPOrb> xpPool = new com.badlogic.gdx.utils.Pool<XPOrb>(200) {
        @Override protected XPOrb newObject() { return new XPOrb(); }
    };

    private final com.badlogic.gdx.utils.Pool<MiniHeal> healPool = new com.badlogic.gdx.utils.Pool<MiniHeal>(50) {
        @Override protected MiniHeal newObject() { return new MiniHeal(); }
    };

    @Override
    public void show() {

        isGamePaused = false;
        batch = new SpriteBatch();
        effectManager = new EffectManager(300);
        this.projectileFactory = new ProjectileFactory(effectManager, Assets.getRegion("shared", "RedBullet"), 200);
        this.weaponFactory = new WeaponFactory(projectileFactory, effectManager);

        // Cargar personaje y mapa desde la sesión
        waveSectionName = (com.tikisadventure.core.GameSession.selectedMapName != null)
            ? com.tikisadventure.core.GameSession.selectedMapName : "bosque";
        CharacterProfile profile = CharacterFactory.getInstance().create(com.tikisadventure.core.GameSession.selectedCharacterId, projectileFactory, effectManager);

        camera = new OrthographicCamera();
        viewport = new FitViewport(20, 20, camera);
        floorManager = new FloorManager(true);

        player = new Player(profile);

        com.badlogic.gdx.math.Vector2 playerSpawnPos = floorManager.getPlayerSpawnPosition();
        if (playerSpawnPos == null) {
            Gdx.app.error("GAME", "No Player_spawn layer or positions found in map! Returning to menu.");
            game.setScreen(new MenuScreen(game));
            return;
        }
        player.getPosition().set(playerSpawnPos.x, playerSpawnPos.y);
        physicsSystem = new PhysicsSystem(floorManager);
        combatSystem = new CombatSystem(effectManager);
        combatFeedbackSystem = new CombatFeedbackSystem();
        movementSystem = new MovementSystem(effectManager, projectileFactory);
        renderSystem = new RenderSystem();
        waveSystem = new WaveSystem(waveSectionName);
        spawner = new EnemySpawner(enemies, floorManager, waveSystem, effectManager);

        setupPlayerWeapons();
        hud = new HUD(batch, player);
        shapeRenderer = new ShapeRenderer();
        trajectoryRenderer = new TrajectoryRenderer();

        hud.setAbilityNames(player.getProfile().ability1Name, player.getProfile().ability2Name);
    }

    private void setupPlayerWeapons() {
        WeaponManager manager = player.getWeaponFactory();
        manager.clear();

        if (com.tikisadventure.core.GameSession.godMode) {
            //Modo dios, equipa las armas de los parametros:
            for (int i = 0; i < 6; i++) {
                String weaponId = com.tikisadventure.core.GameSession.godModeWeapons[i];
                // Si el hueco no es nulo y no está vacío ("- Sin arma -")
                if (weaponId != null && !weaponId.isEmpty()) {
                    manager.addWeapon(weaponFactory.createWeapon(weaponId, player));
                }
            }
        } else {
            //Modo normal, equipa solo arma del perfil:
            String startingWeapon = player.getProfile().startingWeapon;
            if (startingWeapon != null && !startingWeapon.isEmpty()) {
                manager.addWeapon(weaponFactory.createWeapon(startingWeapon, player));
            }
        }
    }

    @Override
    public void render(float delta) {

        update(delta);
        ScreenUtils.clear(0.1f, 0.1f, 0.2f, 1);

        float camOffset = floorManager.isTransitionActive() ? floorManager.getCameraOffset() : 0;
        camera.position.set(player.getPosition().x, player.getPosition().y + camOffset, 0);
        camera.update();

        // Calcular puntero usando la cámara directamente con Vector3
        mouseWorld3.set(Gdx.input.getX(), Gdx.input.getY(), 0);
        camera.unproject(mouseWorld3);
        mouseWorld.set(mouseWorld3.x, mouseWorld3.y);

        player.getWeaponFactory().setManualAim(Gdx.input.isButtonPressed(Input.Buttons.LEFT), mouseWorld);

        floorManager.renderMap(camera);

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        floorManager.renderEntities(batch);
        for (Pickup p : pickups) p.render(batch, delta);
        renderSystem.render(enemies, batch, delta);
        renderSystem.renderProjectiles(spawner.getEnemyProjectiles(), batch, delta);
        effectManager.render(batch);
        renderSystem.render(player, batch, delta);
        combatFeedbackSystem.render(batch);
        // Draw crosshair if manual aiming
        if (Gdx.input.isButtonPressed(Input.Buttons.LEFT)) {
            com.badlogic.gdx.graphics.g2d.TextureRegion crosshairRegion = com.tikisadventure.core.Assets.getRegion("shared", "UI_Crosshair");
            float size = 1.0f;
            batch.draw(crosshairRegion, mouseWorld.x - size / 2f, mouseWorld.y - size / 2f, size, size);
        }
        batch.end();

        floorManager.renderTransparentLayer(camera);

        // Draw trajectory if player is aiming
        if (player.isAiming()) {
            batch.setProjectionMatrix(camera.combined);
            batch.begin();
            trajectoryRenderer.render(batch, player.getPosition(), player.getAimingTarget());
            batch.setColor(1f, 1f, 1f, 1f);
            batch.end();
        }

        renderDebugHitboxes();
        hud.render();
    }

    private void update(float delta) {

        //Leemos atajos desarrollador primero
        updateSystemEvents(delta);

        //Actualizar interfaz
        hud.update(
            player.getVida(),
            player.getExperienceSystem(),
            player.getScore(),
            player.getAbility1CooldownPercent(),
            player.getAbility2CooldownPercent()
        );

        // Vigilante de niveles
        if (player.getExperienceSystem().getLevelsPending() > 0 && !isGamePaused) {
            isGamePaused = true;
            hud.showLevelUpWindow();
        }

        if (isGamePaused) {
            return;
        }

        // Congelado en pausa

        // Jugador muere
        if (player.getVida() <= 0) {
            if (!com.tikisadventure.core.GameSession.godMode) {
                com.tikisadventure.core.SaveManager.addScoreRankProfileData(player.getScore());
                int oleadaAlcanzada = floorManager.getCurrentFloor();
                com.tikisadventure.core.SaveManager.updateMaxWave(waveSectionName, oleadaAlcanzada);
            }
            game.setScreen(new MenuMapScreen(game));

            // Seguridad nativa
            Gdx.app.postRunnable(new Runnable() {
                @Override
                public void run() {
                    GameScreen.this.dispose();
                }
            });
            return;
        }

        //Gameplay normal
        if (damageCooldown > 0) damageCooldown -= delta;

        floorManager.update(delta);
        effectManager.update(delta);
        combatFeedbackSystem.update(delta);

        if (!floorManager.isTransitionActive()) {
            handleGameplay(delta);
        }

        if (floorManager.isTransitionComplete()) {
            handleTransition();
        }
    }

    private void handleGameplay(float delta) {

        player.getWeaponFactory().setManualAim(Gdx.input.isButtonPressed(Input.Buttons.LEFT), mouseWorld);

        boolean nearDoorOpen = floorManager.isPlayerNearDoorOpen(player.getPosition());
        if (Gdx.input.isKeyJustPressed(Input.Keys.E) && nearDoorOpen) {
            Gdx.app.log("GAME", "Cambiando de nivel...");
            floorManager.startTransition();
            return;
        }

        player.update(delta, enemies, mouseWorld);

        Array<Entity> allEntities = new Array<>(enemies);
        allEntities.add(player);
        movementSystem.update(allEntities, delta);

        movementSystem.updateProjectiles(player.getActiveProjectiles(), enemies, delta);
        combatSystem.update(player.getActiveProjectiles(), enemies, delta);

        Array<Projectile> enemyProjectiles = spawner.getEnemyProjectiles();
        movementSystem.updateProjectiles(enemyProjectiles, enemies, delta);
        if (combatSystem.checkEnemyProjectileCollisions(enemyProjectiles, player)) {
            damageCooldown = 0.8f;
        }

        spawner.update(delta, player);
        updateWaveLogic();
        updatePickups(delta);
        updateEnemies(delta);

        resolvePhysics(delta);
    }

    private void resolvePhysics(float delta) {
        physicsSystem.resolveEnemySeparation(enemies, delta);
        if (physicsSystem.resolvePlayerCollision(player, enemies, delta, damageCooldown)) {
            damageCooldown = 0.8f;
        }
        physicsSystem.resolveWallCollision(player, 0.5f);
    }

    private void updateEnemies(float delta) {
        for (int i = enemies.size - 1; i >= 0; i--) {
            Entity enemy = enemies.get(i);
            if (enemy.isAlive()) {
                enemy.update(delta, player);

                if (enemy instanceof ConfigurableEnemy && ((ConfigurableEnemy) enemy).hasPouncingBehavior()) {
                    physicsSystem.resolveWallCollisionWithBounce(enemy, 0.4f);
                } else {
                    physicsSystem.resolveWallCollision(enemy, 0.4f);
                }
            } else {
                spawnDrop(enemy.getPosition(), enemy.getExperience());
                player.addScore(enemy.getScoreValue());
                enemies.removeIndex(i);
            }
        }
    }

    private void updatePickups(float delta) {
        for (int i = pickups.size - 1; i >= 0; i--) {
            Pickup p = pickups.get(i);
            p.update(delta, player);

            if (!p.isAlive()) {
                if (p instanceof XPOrb) xpPool.free((XPOrb) p);
                else if (p instanceof MiniHeal) healPool.free((MiniHeal) p);

                pickups.removeIndex(i);
            }
        }
    }

    private void updateWaveLogic() {
        if (!waveInProgress && !floorManager.isTransitionActive()) {
            spawner.resetForNewWave();
            waveInProgress = true;
        }
        if (waveInProgress && spawner.isWaveSpawningComplete() && enemies.size == 0) {
            if (floorManager.getCurrentFloor() < floorManager.getTotalFloors()) {
                floorManager.showDoorOpen();
            }
        }
    }

    private void handleTransition() {
        floorManager.completeTransition();
        pickups.clear();
        enemies.clear();
        waveInProgress = false;
        waveSystem.nextWave();

        com.badlogic.gdx.math.Vector2 newSpawnPos = floorManager.getPlayerSpawnPosition();
        if (newSpawnPos == null) {
            Gdx.app.error("GAME", "No Player_spawn position found in handleTransition! Returning to menu.");
            game.setScreen(new MenuScreen(game));
            return;
        }
        player.getPosition().set(newSpawnPos.x, newSpawnPos.y);
    }

    private void updateSystemEvents(float delta) {

        if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.R)) {

            game.setScreen(new GameScreen(game));
            Gdx.app.postRunnable(new Runnable() {
                @Override
                public void run() {
                    GameScreen.this.dispose();
                }
            });
            return;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.K)) {
            Gdx.app.log("DEV", "Kill");

            if (player != null && player.getHealthComponent() != null) {
                player.getHealthComponent().currentHealth = 0;
            }
        }
    }

    private void spawnDrop(Vector2 pos, int exp) {
        if (Math.random() < 0.8f) {
            XPOrb orb = xpPool.obtain();
            orb.init(new Vector2(pos), exp);
            pickups.add(orb);
        } else if (Math.random() < 0.1f) {
            MiniHeal heal = healPool.obtain();
            heal.init(new Vector2(pos));
            pickups.add(heal);
        }
    }

    private void renderDebugHitboxes() {
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(0, 1, 0, 1);
        shapeRenderer.circle(player.getHitboxActionTrigger().x, player.getHitboxActionTrigger().y, player.getHitboxActionTrigger().radius, 32);
        for (Entity e : enemies) shapeRenderer.circle(e.getHitboxActionTrigger().x, e.getHitboxActionTrigger().y, e.getHitboxActionTrigger().radius, 32);
        shapeRenderer.end();
    }

    @Override public void resize(int w, int h) { viewport.update(w, h, true); hud.resize(w, h); }
    @Override public void pause() {} @Override public void resume() {}
    @Override
    public void hide() {
        if (player != null) {
            player.dispose();
        }
    }
    @Override
    public void dispose() {
        if (batch != null) batch.dispose();
        if (shapeRenderer != null) shapeRenderer.dispose();
        if (floorManager != null) floorManager.dispose();
        if (combatFeedbackSystem != null) combatFeedbackSystem.dispose();
        if (effectManager != null) effectManager.dispose();
        if (trajectoryRenderer != null) trajectoryRenderer.dispose();
    }
}
