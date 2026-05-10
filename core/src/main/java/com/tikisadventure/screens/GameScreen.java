package com.tikisadventure.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import com.tikisadventure.combat.projectiles.Projectile;
import com.tikisadventure.combat.projectiles.ProjectileFactory;
import com.tikisadventure.combat.weapons.WeaponFactory;
import com.tikisadventure.combat.weapons.WeaponManager;
import com.tikisadventure.core.Assets;
import com.tikisadventure.core.GameSession;
import com.tikisadventure.core.SaveManager;
import com.tikisadventure.entities.base.Entity;
import com.tikisadventure.entities.gadgets.SewerMine;
import com.tikisadventure.entities.gadgets.Scarecrow;
import com.tikisadventure.entities.gadgets.Turret;
import com.tikisadventure.input.InputConfig;
import com.tikisadventure.entities.enemies.ConfigurableEnemy;
import com.tikisadventure.entities.pickup.MiniHeal;
import com.tikisadventure.entities.pickup.Pickup;
import com.tikisadventure.entities.pickup.XPOrb;
import com.tikisadventure.entities.player.Player;
import com.tikisadventure.entities.player.CharacterProfile;
import com.tikisadventure.entities.player.CharacterFactory;
import com.tikisadventure.input.ControllerInput;
import com.tikisadventure.input.InputHandler;
import com.tikisadventure.input.KeyboardInput;
import com.tikisadventure.input.TouchpadInput;
import com.tikisadventure.systems.*;
import com.tikisadventure.systems.powerUps.PowerUp;
import com.tikisadventure.ui.HUD;
import com.tikisadventure.ui.TrajectoryRenderer;
import com.tikisadventure.effects.EffectManager;
import com.tikisadventure.floors.FloorManager;

import java.util.Random;

public class GameScreen implements Screen {

    private final Game game;
    private Player player;
    private InputHandler inputHandler;
    private KeyboardInput keyboardInput;
    private ControllerInput controllerInput;
    private TouchpadInput touchpadInput;
    private static OrthographicCamera camera;
    private Viewport viewport;
    private final Array<Entity> enemies = new Array<>();
    private final Array<Pickup> pickups = new Array<>();
    private EnemySpawner spawner;
    private static HUD hud;
    private ShapeRenderer shapeRenderer;
    private SpriteBatch batch;
    private TrajectoryRenderer trajectoryRenderer;
    private RenderSystem renderSystem;
    private WaveSystem waveSystem;
    private static EffectManager effectManager;
    private ProjectileFactory projectileFactory;
    private WeaponFactory weaponFactory;
    private FloorManager floorManager;
    private PhysicsSystem physicsSystem;
    private CombatSystem combatSystem;
    private CombatFeedbackSystem combatFeedbackSystem;
    private MovementSystem movementSystem;
    private Random dropRng;

    public static boolean isGamePaused = false;
    private boolean isGameOver = false; // <-- NUEVO: Control de la cinemática de muerte
    private int lastKnownLevel = 1;

    private boolean waveInProgress = false;
    private String waveSectionName;

    private float damageCooldown = 0;
    private float restartTimer = 0f;

    private final Vector3 mouseWorld3 = new Vector3();
    private final Vector2 mouseWorld = new Vector2();
    private PowerUpSystem powerUpSystem;
    private com.tikisadventure.ui.PauseUI pauseUI;

    public GameScreen(Game game) { this.game = game; }

    public static final Array<SewerMine> activeMines = new Array<>();

    public static Array<Turret> activeTurrets = new Array<>();
    public static Scarecrow activeScarecrow = null;
    public static boolean scarecrowLocked = false;

    private final Pool<XPOrb> xpPool = new Pool<XPOrb>(200) {
        @Override protected XPOrb newObject() { return new XPOrb(); }
    };

    private final Pool<MiniHeal> healPool = new Pool<MiniHeal>(50) {
        @Override protected MiniHeal newObject() { return new MiniHeal(); }
    };

    @Override
    public void show() {
        activeMines.clear();
        activeTurrets.clear();
        activeScarecrow = null;
        scarecrowLocked = false;

        isGamePaused = false;
        batch = new SpriteBatch();
        effectManager = new EffectManager(300);
        this.projectileFactory = new ProjectileFactory(effectManager, Assets.getRegion("shared", "particle_assets/RedBullet"), 200);
        this.weaponFactory = new WeaponFactory(projectileFactory, effectManager);

        powerUpSystem = new PowerUpSystem(weaponFactory);

        waveSectionName = (GameSession.selectedMapName != null)
            ? GameSession.selectedMapName : "bosque";
        String characterId = GameSession.godMode ? "TikiBot" : GameSession.selectedCharacterId;
        CharacterProfile profile = CharacterFactory.getInstance().create(characterId, projectileFactory, effectManager);

        String gadgetToEquip = null;

        if (GameSession.godMode && GameSession.godModeAbility2Id != null && !GameSession.godModeAbility2Id.isEmpty()) {
            gadgetToEquip = GameSession.godModeAbility2Id;
        } else {
            gadgetToEquip = SaveManager.getEquippedGadget();
        }

        if (gadgetToEquip != null && !gadgetToEquip.isEmpty()) {
            profile.ability2Name = gadgetToEquip;
            profile.specialAbility2 = com.tikisadventure.combat.abilities.AbilityFactory.create(gadgetToEquip, projectileFactory, effectManager);
        }

        camera = new OrthographicCamera();
        viewport = new FitViewport(20, 20, camera);
        GameSession.generateNewSeed();
        Gdx.app.log("SEED", "New game seed: " + GameSession.currentSeed);
        floorManager = new FloorManager(true);

        player = new Player(profile);

        com.badlogic.gdx.math.Vector2 playerSpawnPos = floorManager.getPlayerSpawnPosition();
        if (playerSpawnPos == null) {
            Gdx.app.error("GAME", "No Player_spawn layer or positions found in map! Returning to menu.");
            game.setScreen(new MenuScreen(game));
            return;
        }
        player.getPosition().set(playerSpawnPos.x, playerSpawnPos.y);

        java.util.ArrayList<com.badlogic.gdx.math.Vector2> enemySpawnPositions = new java.util.ArrayList<>();
        com.badlogic.gdx.utils.Array<com.badlogic.gdx.math.Vector2> enemyPosArray = floorManager.getEnemySpawnPositions();
        if (enemyPosArray != null) {
            for (com.badlogic.gdx.math.Vector2 pos : enemyPosArray) {
                enemySpawnPositions.add(pos);
            }
        }

        physicsSystem = new PhysicsSystem(floorManager);
        combatSystem = new CombatSystem(effectManager);
        combatFeedbackSystem = new CombatFeedbackSystem();
        movementSystem = new MovementSystem(effectManager, projectileFactory);
        renderSystem = new RenderSystem();
        waveSystem = new WaveSystem(waveSectionName);
        spawner = new EnemySpawner(enemies, floorManager, waveSystem, effectManager, enemySpawnPositions);
        dropRng = GameSession.getSeededRandomForFloor(floorManager.getCurrentFloor());

        setupPlayerWeapons();

        boolean isMobile = Gdx.app.getType().name().equals("Android");
        boolean showTouchpads = isMobile;

        hud = new HUD(batch, player, showTouchpads);
        if (player.getProfile() != null && player.getProfile().ability2Name != null) {
            hud.setGadgetId(player.getProfile().ability2Name);
        }
        shapeRenderer = new ShapeRenderer();
        trajectoryRenderer = new TrajectoryRenderer();

        inputHandler = new InputHandler();
        keyboardInput = new KeyboardInput(inputHandler);
        keyboardInput.setCamera(camera);
        controllerInput = new ControllerInput(inputHandler);

        if (showTouchpads && hud.getTouchpadInput() != null) {
            touchpadInput = hud.getTouchpadInput();
        } else {
            touchpadInput = null;
        }

        InputMultiplexer multiplexer = new InputMultiplexer();
        multiplexer.addProcessor(hud.getStage());
        multiplexer.addProcessor(keyboardInput);
        Gdx.input.setInputProcessor(multiplexer);

        // --- NUEVO: Inicializamos la interfaz de pausa ---
        pauseUI = new com.tikisadventure.ui.PauseUI(hud.getSkin(), game, this, new Runnable() {
            @Override
            public void run() {
                // Callback de reanudar
                isGamePaused = false;
                pauseUI.setVisible(false);
            }
        });
        pauseUI.setVisible(false);
        hud.getStage().addActor(pauseUI);
    }

    private void setupPlayerWeapons() {
        WeaponManager manager = player.getWeaponFactory();
        manager.clear();

        if (GameSession.godMode) {
            for (int i = 0; i < 6; i++) {
                String weaponId = GameSession.godModeWeapons[i];
                if (weaponId != null && !weaponId.isEmpty()) {
                    manager.addWeapon(weaponFactory.createWeapon(weaponId, player));
                }
            }
        } else {
            String startingWeapon = player.getProfile().startingWeapon;
            if (startingWeapon != null && !startingWeapon.isEmpty()) {
                manager.addWeapon(weaponFactory.createWeapon(startingWeapon, player));
            }
        }
    }

    @Override
    public void render(float delta) {
        update(delta);

        if (!isGameOver) {
            float camOffset = floorManager.isTransitionActive() ? floorManager.getCameraOffset() : 0;
            camera.position.set(player.getPosition().x, player.getPosition().y + camOffset, 0);
            camera.update();
        }

        mouseWorld3.set(Gdx.input.getX(), Gdx.input.getY(), 0);
        camera.unproject(mouseWorld3);
        mouseWorld.set(mouseWorld3.x, mouseWorld3.y);
        InputConfig config = SaveManager.getProfileData().inputConfig;
        int manualAimButton = config.keyboardMapping.get("manualAim");
        // No permitir apuntado manual si el juego ha terminado
        boolean manualAimHeld = !isGameOver && InputConfig.isValidInput(manualAimButton, true) && Gdx.input.isButtonPressed(manualAimButton);
        player.getWeaponFactory().setManualAim(manualAimHeld, mouseWorld);

        ScreenUtils.clear(0.1f, 0.1f, 0.2f, 1);

        floorManager.renderMap(camera);

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        batch.setColor(Color.WHITE);
        batch.setShader(null);

        floorManager.renderEntities(batch);
        floorManager.renderProceduralDecorations(batch);
        for (Pickup p : pickups) p.render(batch, delta);
        for (SewerMine mine : activeMines) mine.render(batch, delta);
        if (activeScarecrow != null) activeScarecrow.render(batch, delta);
        for (Turret turret : activeTurrets) turret.render(batch, delta);

        // ¡IMPORTANTE! Forzamos el color blanco antes de dibujar a los enemigos
        batch.setColor(Color.WHITE);
        renderSystem.render(enemies, batch, delta);
        renderSystem.renderProjectiles(spawner.getEnemyProjectiles(), batch, delta);
        effectManager.render(batch);

        // Dibujamos al jugador (que puede estar desvaneciéndose)
        renderSystem.render(player, batch, delta);

        floorManager.renderProceduralObjects(batch);

        // Forzamos el color blanco de nuevo
        batch.setColor(Color.WHITE);

        // Solo dibujar flechas de apuntado y puertas si está vivo
        if (player.getVida() > 0) {
            player.drawEnemyArrow(batch, enemies);

            if (floorManager.isDoorOpen()) {
                Vector2 doorPos = floorManager.getDoorPosition();
                if (doorPos != null) {
                    player.drawDoorArrow(batch, doorPos, floorManager.isDoorOpen());
                }
            }
        }

        combatFeedbackSystem.render(batch);

        if (manualAimHeld) {
            TextureRegion crosshairRegion = Assets.getRegion("shared", "UI_assets/UI_Crosshair");
            float size = 1.0f;
            batch.draw(crosshairRegion, mouseWorld.x - size / 2f, mouseWorld.y - size / 2f, size, size);
        }
        batch.end();

        floorManager.renderTransparentLayer(camera);
        if (player.isAiming()) {
            batch.setProjectionMatrix(camera.combined);
            batch.begin();
            batch.setColor(Color.WHITE);
            trajectoryRenderer.render(batch, player.getPosition(), player.getAimingTarget());
            batch.setColor(1f, 1f, 1f, 1f);
            batch.end();
        }

        hud.render();
    }

    // --- PROTECCIÓN CONTRA CRASHEOS AL RESUCITAR ---
    public static void triggerScarecrowReviveEffects(Player p) {
        try {
            if (camera != null) {
                camera.position.set(p.getPosition().x, p.getPosition().y, 0);
                camera.update();
            }
            if (effectManager != null) {
                EffectManager.ExplosionProfile exp = effectManager.getExplosionProfile("REVIVE");
                if (exp != null) {
                    if (exp.smoke != null && !exp.smoke.isEmpty()) {
                        effectManager.spawnEffect(exp.smoke, p.getPosition(), new Vector2(0,0));
                    }
                    if (exp.sparks != null && !exp.sparks.isEmpty()) {
                        effectManager.spawnEffect(exp.sparks, p.getPosition(), new Vector2(0,0));
                    }
                }
            }
            if (hud != null) {
                if (p.getProfile() != null) {
                    p.getProfile().specialAbility2 = null;
                }
                hud.lockAbility2();
            }
        } catch (Exception e) {
            Gdx.app.error("REVIVE_SYSTEM", "Excepción silenciada al generar partículas. Resurrección completada.", e);
        }
    }
    // -----------------------------------------------

    private void update(float delta) {
        float realDelta = delta;
        // Si el juego acaba, ralentizamos todo al 35%
        float gameDelta = isGameOver ? delta * 0.35f : delta;

        updateSystemEvents(realDelta);

        if (!isGameOver) {
            inputHandler.reset();
            keyboardInput.update(inputHandler);
            if (touchpadInput != null) {
                touchpadInput.update(inputHandler);
            }

            hud.update(
                player.getVida(),
                player.getExperienceSystem(),
                player.getScore(),
                player.getAbility1CooldownRemaining(),
                player.getAbility2CooldownRemaining(),
                player
            );

            if (Gdx.input.isKeyJustPressed(Input.Keys.TAB)) {
                hud.toggleStatsPanel();
            }

            if (player.getExperienceSystem().getLevelsPending() > 0 && !isGamePaused) {
                isGamePaused = true;
                int currentLevel = player.getExperienceSystem().getLevel();
                Array<PowerUp> opciones = powerUpSystem.rollOptions(player, currentLevel, 3);

                InputMultiplexer currentMultiplexer = (InputMultiplexer) Gdx.input.getInputProcessor();
                if (currentMultiplexer != null) {
                    hud.setInputMultiplexer(currentMultiplexer);
                }

                hud.showLevelUpWindow(opciones, powerUpSystem, currentLevel);
            }
        } else {
            // Jugador muerto: sin input y se desvanece
            inputHandler.reset();
            float currentAlpha = player.getTintColor().a;
            if (currentAlpha > 0) {
                player.getTintColor().a = Math.max(0, currentAlpha - realDelta * 0.5f);
            }
        }

        // --- TRIGGER DEL GAME OVER ---
        // Chequeo de muerte seguro (usando los métodos del combat system)
        boolean playerDied = (player.getVida() <= 0 || !player.isAlive()) && !(GameSession.godMode && GameSession.godModeIsImmortal);

        if (playerDied && !isGameOver) {
            isGameOver = true;

            // Congelar todos los enemigos en animación idle
            for (com.tikisadventure.entities.base.Entity enemy : enemies) {
                if (enemy instanceof ConfigurableEnemy) {
                    ((ConfigurableEnemy) enemy).setGameOver();
                }
                enemy.setStateTime(0);
            }

            // Procesar el guardado de BD en el evento
            com.tikisadventure.systems.events.GameOverEvent.processGameOver(player, floorManager, waveSystem, waveSectionName);

            // Limpiar interfaz
            hud.getStage().clear();

            // Lanzar la interfaz animada
            com.tikisadventure.ui.EndGameUI endGameUI = new com.tikisadventure.ui.EndGameUI(hud.getSkin(), player.getScore(), game, this);
            hud.getStage().addActor(endGameUI);
            Gdx.input.setInputProcessor(hud.getStage());
        }

        if (isGamePaused) return;

        // Actualizaciones con gameDelta (ralentizadas si hay Game Over)
        for (int i = activeMines.size - 1; i >= 0; i--) {
            SewerMine m = activeMines.get(i);
            m.update(gameDelta, enemies);
            if (!m.isAlive()) activeMines.removeIndex(i);
        }

        if (activeScarecrow != null) {
            activeScarecrow.update(gameDelta, enemies);
            if (!activeScarecrow.isAlive()) activeScarecrow = null;
        }

        for (int i = activeTurrets.size - 1; i >= 0; i--) {
            Turret t = activeTurrets.get(i);
            t.update(gameDelta, enemies);
            if (!t.isAlive()) {
                activeTurrets.removeIndex(i);
            } else {
                movementSystem.updateProjectiles(t.getProjectiles(), enemies, gameDelta);
                combatSystem.update(t.getProjectiles(), enemies, gameDelta);
            }
        }

        if (damageCooldown > 0) damageCooldown -= gameDelta;

        floorManager.update(gameDelta);
        effectManager.update(gameDelta);
        combatFeedbackSystem.update(gameDelta);

        if (!floorManager.isTransitionActive()) {
            handleGameplay(gameDelta);
        }

        if (floorManager.isTransitionComplete()) {
            handleTransition();
        }
    }

    private void handleGameplay(float delta) {
        InputConfig config = SaveManager.getProfileData().inputConfig;
        int manualAimButton = config.keyboardMapping.get("manualAim");
        boolean manualAimHeld = !isGameOver && InputConfig.isValidInput(manualAimButton, true) && Gdx.input.isButtonPressed(manualAimButton);
        player.getWeaponFactory().setManualAim(manualAimHeld, mouseWorld);

        boolean nearDoorOpen = floorManager.isPlayerNearDoorOpen(player.getPosition());
        if (inputHandler.isInteracting && nearDoorOpen) {
            floorManager.startTransition();
            return;
        }

        player.update(delta, enemies, inputHandler);

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
        if (!isGameOver) {
            updateEnemies(delta);
        }

        if (!isGameOver) {
            resolvePhysics(delta);
        }
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

                int droppedExp = Math.max(1, Math.round(enemy.getExperience() * 0.3f));
                spawnDrop(enemy.getPosition(), droppedExp);

                player.addScore(enemy.getScoreValue());

                String enemyName = "Desconocido";
                if (enemy instanceof ConfigurableEnemy) {
                    enemyName = ((ConfigurableEnemy) enemy).getEnemyId();
                } else {
                    enemyName = enemy.getClass().getSimpleName();
                }
                player.registerKill(enemyName);

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
        dropRng = GameSession.getSeededRandomForFloor(floorManager.getCurrentFloor());
        pickups.clear();
        enemies.clear();
        activeMines.clear();
        activeTurrets.clear();
        activeScarecrow = null;
        waveInProgress = false;
        waveSystem.nextWave();

        com.badlogic.gdx.math.Vector2 newSpawnPos = floorManager.getPlayerSpawnPosition();
        if (newSpawnPos == null) {
            game.setScreen(new MenuScreen(game));
            return;
        }
        player.getPosition().set(newSpawnPos.x, newSpawnPos.y);
    }

    private void updateSystemEvents(float delta) {
        // --- NUEVO: Control de Pausa ---
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE) && !isGameOver) {
            // Evitamos que se pueda pausar por encima de la ventana de subir de nivel
            if (player.getExperienceSystem().getLevelsPending() <= 0) {
                isGamePaused = !isGamePaused;
                pauseUI.setVisible(isGamePaused);
                if (isGamePaused) {
                    pauseUI.toFront(); // Aseguramos que se ponga por encima de todo
                }
            }
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.K)) {
            if (player != null && player.getHealthComponent() != null) {
                player.getHealthComponent().currentHealth = 0;
            }
        }
    }

    private void spawnDrop(Vector2 pos, int exp) {
        if (dropRng.nextDouble() < 0.8f) {
            XPOrb orb = xpPool.obtain();
            orb.init(new Vector2(pos), exp);
            pickups.add(orb);
        } else if (dropRng.nextDouble() < 0.5f) {
            MiniHeal heal = healPool.obtain();
            heal.init(new Vector2(pos));
            pickups.add(heal);
        }
    }

    @Override public void resize(int w, int h) {
        viewport.update(w, h, true);
        hud.resize(w, h);
    }

    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() { if (player != null) player.dispose(); }

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
