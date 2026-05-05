package com.tikisadventure.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
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

    public static boolean isGamePaused = false;
    private int lastKnownLevel = 1;

    private boolean waveInProgress = false;
    private String waveSectionName;

    private float damageCooldown = 0;
    private float restartTimer = 0f;

    private final Vector3 mouseWorld3 = new Vector3();
    private final Vector2 mouseWorld = new Vector2();
    private PowerUpSystem powerUpSystem;

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
        CharacterProfile profile = CharacterFactory.getInstance().create(GameSession.selectedCharacterId, projectileFactory, effectManager);

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

        setupPlayerWeapons();

        boolean isMobile = Gdx.app.getType().name().equals("Android");
        boolean showTouchpads = isMobile;

        hud = new HUD(batch, player, showTouchpads);
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

        float camOffset = floorManager.isTransitionActive() ? floorManager.getCameraOffset() : 0;
        camera.position.set(player.getPosition().x, player.getPosition().y + camOffset, 0);
        camera.update();

        mouseWorld3.set(Gdx.input.getX(), Gdx.input.getY(), 0);
        camera.unproject(mouseWorld3);
        mouseWorld.set(mouseWorld3.x, mouseWorld3.y);
        InputConfig config = SaveManager.getProfileData().inputConfig;
        int manualAimButton = config.keyboardMapping.get("manualAim");
        boolean manualAimHeld = InputConfig.isValidInput(manualAimButton, true) && Gdx.input.isButtonPressed(manualAimButton);
        player.getWeaponFactory().setManualAim(manualAimHeld, mouseWorld);

        ScreenUtils.clear(0.1f, 0.1f, 0.2f, 1);

        floorManager.renderMap(camera);

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        batch.setShader(null);

        floorManager.renderEntities(batch);
        for (Pickup p : pickups) p.render(batch, delta);
        for (SewerMine mine : activeMines) mine.render(batch, delta);
        if (activeScarecrow != null) activeScarecrow.render(batch, delta);
        for (Turret turret : activeTurrets) turret.render(batch, delta);
        renderSystem.render(enemies, batch, delta);
        renderSystem.renderProjectiles(spawner.getEnemyProjectiles(), batch, delta);
        effectManager.render(batch);

        renderSystem.render(player, batch, delta);

        player.drawEnemyArrow(batch, enemies);

        if (floorManager.isDoorOpen()) {
            player.drawDoorArrow(batch, floorManager.getDoorPosition(), floorManager.isDoorOpen());
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
        updateSystemEvents(delta);

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

        if (isGamePaused) return;

        for (int i = activeMines.size - 1; i >= 0; i--) {
            SewerMine m = activeMines.get(i);
            m.update(delta, enemies);
            if (!m.isAlive()) activeMines.removeIndex(i);
        }

        if (activeScarecrow != null) {
            activeScarecrow.update(delta, enemies);
            if (!activeScarecrow.isAlive()) activeScarecrow = null;
        }

        for (int i = activeTurrets.size - 1; i >= 0; i--) {
            Turret t = activeTurrets.get(i);
            t.update(delta, enemies);
            if (!t.isAlive()) {
                activeTurrets.removeIndex(i);
            } else {
                movementSystem.updateProjectiles(t.getProjectiles(), enemies, delta);
                combatSystem.update(t.getProjectiles(), enemies, delta);
            }
        }

        if (player.getVida() <= 0) {

            if (!GameSession.godMode) {
                SaveManager.addScoreRankProfileData(player.getScore());

                int stageAlcanzado = floorManager.getCurrentFloor();
                int waveAlcanzada = waveSystem.getCurrentWaveNumber();
                SaveManager.updateMaxProgress(waveSectionName, stageAlcanzado, waveAlcanzada);

                int score = player.getScore();
                if (score > 0) {
                    int base = score / 100;
                    int multiplier = (int)(Math.random() * 7) + 7;
                    int coinsEarned = base * multiplier;

                    SaveManager.addCoins(coinsEarned);
                }

                String currentUser = SaveManager.getLastUsername();

                if (currentUser != null && !currentUser.isEmpty()) {
                    com.tikisadventure.database.progress.ProgressRepository progRepo = new com.tikisadventure.database.progress.ProgressRepository();
                    progRepo.actualizarProgreso(currentUser, SaveManager.getProfileData().coins, SaveManager.getProfileData().totalScore, null);

                    StringBuilder jsonBuilder = new StringBuilder();
                    jsonBuilder.append("{");

                    jsonBuilder.append("\"powerup_stats\": {");
                    jsonBuilder.append("\"hp\":").append(player.getHealthComponent().maxHealth).append(",");
                    jsonBuilder.append("\"kin\":").append(player.getKineticDamageBonus()).append(",");
                    jsonBuilder.append("\"exp\":").append(player.getExplosiveDamageBonus()).append(",");
                    jsonBuilder.append("\"fue\":").append(player.getFireDamageBonus()).append(",");
                    jsonBuilder.append("\"ven\":").append(player.getPoisonDamageBonus()).append(",");
                    jsonBuilder.append("\"hie\":").append(player.getIceDamageBonus()).append(",");
                    jsonBuilder.append("\"ene\":").append(player.getEnergyDamageBonus()).append(",");
                    jsonBuilder.append("\"crt\":").append(player.getCritChanceBonus()).append(",");
                    jsonBuilder.append("\"sue\":").append(player.getLuck()).append(",");
                    jsonBuilder.append("\"xp\":").append(player.getXpMultiplier()).append(",");
                    jsonBuilder.append("\"vel\":").append(player.getSpeed());
                    jsonBuilder.append("\"atr\":").append(player.getAttractionRange()).append(",");
                    jsonBuilder.append("\"rob\":").append(player.getLifeLeechPercent());
                    jsonBuilder.append("\"reg\":").append(player.getLifeRegenPercent());
                    jsonBuilder.append("\"eva\":").append(player.getEvasionChance());
                    jsonBuilder.append("},");

                    jsonBuilder.append("\"weapons_used\": [");
                    com.badlogic.gdx.utils.Array<com.tikisadventure.combat.weapons.Weapon> armas = player.getWeaponFactory().getWeapons();
                    for (int i = 0; i < armas.size; i++) {
                        jsonBuilder.append("\"").append(armas.get(i).getName()).append("\"");
                        if (i < armas.size - 1) jsonBuilder.append(",");
                    }
                    jsonBuilder.append("],");

                    jsonBuilder.append("\"kills_detail\": {");
                    int count = 0;
                    for (com.badlogic.gdx.utils.ObjectMap.Entry<String, Integer> entry : player.killDetails) {
                        jsonBuilder.append("\"").append(entry.key).append("\":").append(entry.value);
                        count++;
                        if (count < player.killDetails.size) jsonBuilder.append(",");
                    }
                    jsonBuilder.append("}");

                    jsonBuilder.append("}");
                    String extraDataJson = jsonBuilder.toString();

                    String charId = GameSession.selectedCharacterId;
                    String gadgetId = SaveManager.getEquippedGadget();
                    if (gadgetId == null || gadgetId.isEmpty()) gadgetId = "grenade_kinetic";

                    progRepo.guardarPartidaBD(
                        currentUser, waveSectionName, charId, gadgetId,
                        score, stageAlcanzado, waveAlcanzada, player.totalKills,
                        extraDataJson, null
                    );
                }
            }

            game.setScreen(new MenuMapScreen(game));
            Gdx.app.postRunnable(new Runnable() {
                @Override
                public void run() {
                    GameScreen.this.dispose();
                }
            });
            return;
        }

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
        InputConfig config = SaveManager.getProfileData().inputConfig;
        int manualAimButton = config.keyboardMapping.get("manualAim");
        boolean manualAimHeld = InputConfig.isValidInput(manualAimButton, true) && Gdx.input.isButtonPressed(manualAimButton);
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
        if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
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
