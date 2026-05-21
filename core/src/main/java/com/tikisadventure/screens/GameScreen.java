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

import com.tikisadventure.audio.AudioEventSubscriber;
import com.tikisadventure.audio.AudioManager;
import com.tikisadventure.audio.AudioType;
import com.tikisadventure.combat.ExplosionUtility;
import com.tikisadventure.combat.projectiles.Projectile;
import com.tikisadventure.combat.projectiles.ProjectileFactory;
import com.tikisadventure.combat.weapons.WeaponFactory;
import com.tikisadventure.combat.weapons.WeaponManager;
import com.tikisadventure.core.Assets;
import com.tikisadventure.core.GameSession;
import com.tikisadventure.core.SaveManager;
import com.tikisadventure.entities.base.Entity;
import com.tikisadventure.entities.gadgets.LootBox;
import com.tikisadventure.entities.gadgets.Scarecrow;
import com.tikisadventure.entities.gadgets.SewerMine;
import com.tikisadventure.entities.gadgets.Turret;
import com.tikisadventure.input.InputConfig;
import com.tikisadventure.enemies.behavior.DesertBossBehavior;
import com.tikisadventure.enemies.behavior.EnemyBehavior;
import com.tikisadventure.entities.enemies.ConfigurableEnemy;
import com.tikisadventure.entities.pickup.CoinPickup;
import com.tikisadventure.entities.pickup.MiniHeal;
import com.tikisadventure.entities.pickup.Pickup;
import com.tikisadventure.entities.pickup.StatPickup;
import com.tikisadventure.entities.pickup.XPOrb;
import com.tikisadventure.entities.player.Player;
import com.tikisadventure.entities.player.CharacterProfile;
import com.tikisadventure.entities.player.CharacterFactory;
import com.tikisadventure.input.ControllerInput;
import com.tikisadventure.input.InputHandler;
import com.tikisadventure.input.KeyboardInput;
import com.tikisadventure.input.TouchpadInput;
import com.tikisadventure.systems.*;
import com.tikisadventure.systems.powerUps.DebugStats;
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
    private final Array<LootBox> lootBoxes = new Array<>();
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
    private float beamDamageCooldown = 0;
    private float restartTimer = 0f;

    private final Vector3 mouseWorld3 = new Vector3();
    private final Vector2 mouseWorld = new Vector2();
    private PowerUpSystem powerUpSystem;
    private com.tikisadventure.ui.PauseUI pauseUI;
    private boolean isCursorHidden = false;
    private com.badlogic.gdx.graphics.Texture doorIndicatorTexture;
    private float doorIndicatorTimer;

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

    private final Pool<StatPickup> statPool = new Pool<StatPickup>(30) {
        @Override protected StatPickup newObject() { return new StatPickup(); }
    };

    private final Pool<CoinPickup> coinPool = new Pool<CoinPickup>(50) {
        @Override protected CoinPickup newObject() { return new CoinPickup(); }
    };

    @Override
    public void show() {
        activeMines.clear();
        activeTurrets.clear();
        activeScarecrow = null;
        scarecrowLocked = false;
        GameSession.coinsCollectedThisRun = 0;

        isGamePaused = false;
        batch = new SpriteBatch();
        effectManager = new EffectManager(300);
        this.projectileFactory = new ProjectileFactory(effectManager, Assets.getRegion("shared", "particle_assets/RedBullet"), 200);
        this.weaponFactory = new WeaponFactory(projectileFactory, effectManager);

        powerUpSystem = new PowerUpSystem(weaponFactory);
        loadDoorIndicatorTexture();

        waveSectionName = (GameSession.selectedMapName != null)
            ? GameSession.selectedMapName : "bosque";
        Gdx.app.log("GAME", "Starting game with map: " + waveSectionName);
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
        ensureSpawnNotOnVoidOrQuicksand(player.getPosition());

        physicsSystem = new PhysicsSystem(floorManager);
        combatSystem = new CombatSystem(effectManager);
        combatFeedbackSystem = new CombatFeedbackSystem();
        movementSystem = new MovementSystem(effectManager, projectileFactory);
        renderSystem = new RenderSystem();
        waveSystem = new WaveSystem(waveSectionName);
        waveSystem.initStage(floorManager.getCurrentStage(), WaveSystem.WAVES_PER_STAGE);
        spawner = new EnemySpawner(enemies, floorManager, waveSystem, effectManager);
        dropRng = GameSession.getSeededRandomForStage(floorManager.getCurrentStage());

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

        // --- Inicializamos la interfaz de pausa ---
        pauseUI = new com.tikisadventure.ui.PauseUI(hud.getSkin(), game, this, new Runnable() {
            @Override
            public void run() {
                // Callback de reanudar
                isGamePaused = false;
                pauseUI.setVisible(false);
                AudioManager.unduckFromPause();
            }
        });
        pauseUI.setVisible(false);
        hud.getStage().addActor(pauseUI);

        // NUEVO: Mostrar aviso de la Fase 1 al entrar a la partida
        hud.showStageMessage(floorManager.getCurrentStage());
        spawnLootBoxes();

        AudioEventSubscriber.init();
        AudioManager.setMusicBiome(waveSectionName);
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
            String startingWeapon = SaveManager.getEquippedStartingWeapon();
            if (startingWeapon == null) {
                startingWeapon = player.getProfile().startingWeapon;
            }
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
        floorManager.renderCactusSprites(batch);
        for (Pickup p : pickups) p.render(batch, delta);
        for (LootBox box : lootBoxes) if (box.isAlive()) box.render(batch, delta);
        for (SewerMine mine : activeMines) mine.render(batch, delta);
        if (activeScarecrow != null) activeScarecrow.render(batch, delta);
        for (Turret turret : activeTurrets) turret.render(batch, delta);

        batch.setColor(Color.WHITE);
        Entity forestBossToRender = null;
        Entity desertBossToRender = null;
        for (Entity e : enemies) {
            if (e instanceof ConfigurableEnemy) {
                String bt = ((ConfigurableEnemy) e).getBehavior().getBehaviorType();
                if ("forest_boss".equals(bt)) {
                    forestBossToRender = e;
                    continue;
                }
                if ("desert_boss".equals(bt)) {
                    desertBossToRender = e;
                    continue;
                }
            }
            if (e != null && e.isAlive()) e.render(batch, delta);
        }
        renderSystem.renderProjectiles(spawner.getEnemyProjectiles(), batch, delta);
        effectManager.render(batch);

        floorManager.renderProceduralObjectsBg(batch);
        renderSystem.render(player, batch, delta);

        floorManager.renderProceduralObjects(batch);

        batch.setColor(Color.WHITE);

        floorManager.renderProceduralAbovePlayer(batch);

        batch.setColor(1f, 1f, 1f, player.getTintColor().a);
        player.getWeaponFactory().render(batch);
        for (com.tikisadventure.combat.projectiles.Projectile p : player.getActiveProjectiles()) p.render(batch);
        batch.setColor(Color.WHITE);
        if (player.getVida() > 0) {
            player.drawEnemyArrow(batch, enemies);
            if (floorManager.isDoorOpen()) {
                Vector2 doorPos = floorManager.getDoorPosition();
                if (doorPos != null) {
                    player.drawDoorArrow(batch, doorPos, floorManager.isDoorOpen());
                    if (doorIndicatorTexture != null && floorManager.isPlayerNearDoorOpen(player.getPosition())) {
                        doorIndicatorTimer += 0.05f;
                        float bob = (float)Math.sin(doorIndicatorTimer * 2f) * 0.15f;
                        float sx = doorPos.x + 0.5f;
                        float sy = doorPos.y + 1f + bob;
                        float half = 0.5f;
                        batch.draw(doorIndicatorTexture, sx - half, sy - half, half, half, 1f, 1f, 1f, 1f, 0, 0, 0, 16, 16, false, false);
                    }
                }
            }
        }

        combatFeedbackSystem.render(batch);

        if (forestBossToRender != null && forestBossToRender.isAlive()) {
            forestBossToRender.render(batch, delta);
        }
        if (desertBossToRender != null && desertBossToRender.isAlive()) {
            desertBossToRender.render(batch, delta);
            DesertBossBehavior db = (DesertBossBehavior) ((ConfigurableEnemy) desertBossToRender).getBehavior();
            TextureRegion beamTex = db.getBeamTexture();
            if (beamTex != null) {
                DesertBossBehavior.LaserBeam beam = db.getActiveBeam();
                if (beam != null) {
                    float camLeft = camera.position.x - camera.viewportWidth / 2f;
                    float camRight = camera.position.x + camera.viewportWidth / 2f;
                    float startX, endX;
                    if (beam.facingRight) {
                        startX = beam.position.x;
                        endX = camRight;
                    } else {
                        startX = camLeft;
                        endX = beam.position.x;
                    }
                    float bx = startX;
                    float bw = endX - startX;
                    float by = beam.position.y - DesertBossBehavior.BEAM_HEIGHT / 2f;
                    float bh = DesertBossBehavior.BEAM_HEIGHT;
                    batch.draw(beamTex, bx, by, bw, bh);
                }
            }
        }

        if (manualAimHeld) {
            TextureRegion crosshairRegion = Assets.getRegion("shared", "UI_assets/UI_Crosshair");
            float size = SaveManager.getProfileData().inputConfig.mouseSize;
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
        batch.setColor(Color.WHITE);
        hud.render();
    }

    public static void triggerScarecrowReviveEffects(Player p) {
        try {
            if (camera != null) {
                camera.position.set(p.getPosition().x, p.getPosition().y, 0);
                camera.update();
            }
            if (effectManager != null) {
                ExplosionUtility.spawnVisuals(effectManager, p.getPosition(), "REVIVE");
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

    private void update(float delta) {
        float realDelta = delta;
        float gameDelta = isGameOver ? delta * 0.35f : delta;

        AudioManager.update(realDelta);
        updateSystemEvents(realDelta);

        if (!isGameOver) {
            if (player != null && player.getVida_max() > 0) {
                float healthPercent = player.getVida() / player.getVida_max();
                AudioManager.setMusicPitch(healthPercent < 0.4f ? 0.65f : 1.0f);
            }

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
                player,
                waveSystem.getCurrentWaveNumber()
            );

            if (inputHandler.toggleStatsJustPressed) {
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
                AudioManager.duckForPause();
            }
        } else {
            inputHandler.reset();
            float currentAlpha = player.getTintColor().a;
            if (currentAlpha > 0) {
                player.getTintColor().a = Math.max(0, currentAlpha - realDelta * 0.5f);
            }
        }

        boolean playerDied = (player.getVida() <= 0 || !player.isAlive()) && !(GameSession.godMode && GameSession.godModeIsImmortal);

        if (playerDied && !isGameOver) {
            isGameOver = true;

            for (com.tikisadventure.entities.base.Entity enemy : enemies) {
                if (enemy instanceof ConfigurableEnemy) {
                    ((ConfigurableEnemy) enemy).setGameOver();
                }
                enemy.setStateTime(0);
            }

            int totalCoins = com.tikisadventure.systems.events.GameOverEvent.processGameOver(player, floorManager, waveSystem, waveSectionName);
            AudioManager.playSFX(AudioType.PLAYER_DEATH);
            AudioManager.playGameOverMusic(waveSectionName);

            hud.getStage().clear();

            com.tikisadventure.ui.EndGameUI endGameUI = new com.tikisadventure.ui.EndGameUI(hud.getSkin(), player.getScore(), totalCoins, game, this);
            hud.getStage().addActor(endGameUI);
            Gdx.input.setInputProcessor(hud.getStage());
        }

        if (isGamePaused) return;

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

        boolean shouldHideCursor = manualAimHeld || player.isAiming();
        if (shouldHideCursor != isCursorHidden) {
            if (shouldHideCursor) {
                Assets.hideSystemCursor();
            } else {
                Assets.setDefaultCursor();
            }
            isCursorHidden = shouldHideCursor;
        }

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

        waveSystem.update(delta);
        updateWaveLogic();
        spawner.update(delta, player);
        updatePickups(delta);
        if (!isGameOver) {
            updateEnemies(delta);
        }

        if (!isGameOver) {
            resolvePhysics(delta);
        }
        resolvePhysics(delta);

        updateLootBoxes(delta);
        resolveLootBoxPhysics(delta);

        boolean onQuicksand = floorManager.isQuicksand(player.getPosition().x, player.getPosition().y);
        if (onQuicksand && !player.isDashing()) {
            int tileX = (int)Math.floor(player.getPosition().x);
            int tileY = (int)Math.floor(player.getPosition().y);
            player.getPosition().set(tileX + 0.5f, tileY + 0.5f);
            player.isInQuicksand = true;
        } else if (!onQuicksand) {
            player.isInQuicksand = false;
        }

        if (damageCooldown <= 0 && floorManager.isCactus(player.getPosition().x, player.getPosition().y)) {
            player.receiveDamage(10, false, com.tikisadventure.combat.DamageType.KINETIC);
            damageCooldown = 0.8f;
            floorManager.startCactusShake(
                (int)Math.floor(player.getPosition().x),
                (int)Math.floor(player.getPosition().y));
        }

        if (floorManager.isVoidTile(player.getPosition().x, player.getPosition().y) && player.voidDeathTimer <= 0) {
            int tileX = (int)Math.floor(player.getPosition().x);
            int tileY = (int)Math.floor(player.getPosition().y);
            player.getPosition().set(tileX + 0.5f, tileY + 0.5f);
            player.isInVoidTile = true;
            player.voidDeathTimer = 0.001f;
        }

        if (beamDamageCooldown > 0) beamDamageCooldown -= delta;
        for (Entity e : enemies) {
            if (e instanceof ConfigurableEnemy && "desert_boss".equals(((ConfigurableEnemy) e).getBehavior().getBehaviorType())) {
                DesertBossBehavior db = (DesertBossBehavior) ((ConfigurableEnemy) e).getBehavior();
                DesertBossBehavior.LaserBeam beam = db.getActiveBeam();
                if (beam != null && beamDamageCooldown <= 0) {
                    float py = player.getPosition().y;
                    float beamY = beam.position.y;
                    float halfH = DesertBossBehavior.BEAM_HEIGHT / 2f;
                    if (py >= beamY - halfH && py <= beamY + halfH) {
                        float px = player.getPosition().x;
                        boolean inBeam = beam.facingRight ? px >= beam.position.x : px <= beam.position.x;
                        if (inBeam) {
                            player.receiveDamage(db.getAttackDamage(), false, com.tikisadventure.combat.DamageType.ENERGY);
                            beamDamageCooldown = 0.3f;
                        }
                    }
                }
                break;
            }
        }
    }

    private void resolvePhysics(float delta) {
        if (player.voidDeathTimer <= 0) {
            physicsSystem.resolveEnemySeparation(enemies, delta);
            if (physicsSystem.resolvePlayerCollision(player, enemies, delta, damageCooldown)) {
                damageCooldown = 0.8f;
            }
        }
        physicsSystem.resolveWallCollision(player, 0.5f);
        physicsSystem.resolveObstacleCollision(player);
    }

    private void updateEnemies(float delta) {
        for (int i = enemies.size - 1; i >= 0; i--) {
            Entity enemy = enemies.get(i);
            if (enemy.isAlive()) {
                enemy.update(delta, player);

                if (enemy instanceof ConfigurableEnemy) {
                    EnemyBehavior eb = ((ConfigurableEnemy) enemy).getBehavior();
                    if (eb != null && ("forest_boss".equals(eb.getBehaviorType()) || "desert_boss".equals(eb.getBehaviorType()))) {
                        // boss has no wall collision
                    } else if (eb != null && "castle_boss".equals(eb.getBehaviorType())) {
                        physicsSystem.resolveWallCollision(enemy, 0.4f);
                    } else if (((ConfigurableEnemy) enemy).hasPouncingBehavior()) {
                        physicsSystem.resolveEnemyWallCollisionWithBounce(enemy, 0.4f);
                        physicsSystem.resolveObstacleCollision(enemy);
                    } else {
                        physicsSystem.resolveEnemyWallCollision(enemy, 0.4f);
                        physicsSystem.resolveObstacleCollision(enemy);
                    }
                } else {
                    physicsSystem.resolveEnemyWallCollision(enemy, 0.4f);
                    physicsSystem.resolveObstacleCollision(enemy);
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
                if (p instanceof CoinPickup) {
                    CoinPickup cp = (CoinPickup) p;
                    hud.showCoinNotification("+" + cp.getCoinAmount() + " monedas");
                    coinPool.free(cp);
                } else if (p instanceof XPOrb) {
                    xpPool.free((XPOrb) p);
                } else if (p instanceof MiniHeal) {
                    healPool.free((MiniHeal) p);
                } else if (p instanceof StatPickup) {
                    statPool.free((StatPickup) p);
                }
                pickups.removeIndex(i);
            }
        }
    }

    private float getWaveDelay() {
        for (Entity e : enemies) {
            if (e instanceof ConfigurableEnemy) {
                String bt = ((ConfigurableEnemy) e).getBehavior().getBehaviorType();
                if ("forest_boss".equals(bt) || "desert_boss".equals(bt) || "castle_boss".equals(bt)) {
                    return WaveSystem.BOSS_WAVE_DELAY;
                }
            }
        }
        return WaveSystem.WAVE_DELAY;
    }

    private void updateWaveLogic() {
        if (!waveInProgress && !floorManager.isTransitionActive()) {
            if (waveSystem.isWaveDelayActive()) return;
            waveSystem.nextWave();
            spawner.resetForNewWave();
            waveInProgress = true;
        }
        if (!waveInProgress || !spawner.isWaveSpawningComplete()) return;

        if (enemies.size > 0) {
            if (waveSystem.hasMoreWavesInStage() && !waveSystem.isWaveDelayActive() && spawner.isWaveSpawningComplete()) {
                if (waveSystem.isInfiniteMode() || enemies.size <= 5) {
                    waveSystem.startWaveDelay(getWaveDelay());
                }
            }
            if (waveSystem.isWaveDelayActive() && waveSystem.isWaveDelayComplete()) {
                waveSystem.clearWaveDelay();
                waveInProgress = false;
            }
            return;
        }

        if (waveSystem.hasMoreWavesInStage()) {
            if (!waveSystem.isWaveDelayActive()) {
                waveSystem.startWaveDelay(getWaveDelay());
            }
            if (waveSystem.isWaveDelayComplete()) {
                waveSystem.clearWaveDelay();
                waveInProgress = false;
            }
        } else {
            if (waveSystem.isBossStage()) {
                if (!waveSystem.isInfiniteMode()) {
                    waveSystem.enterInfiniteMode();
                }
                waveSystem.startWaveDelay(getWaveDelay());
                if (waveSystem.isWaveDelayComplete()) {
                    waveSystem.clearWaveDelay();
                    waveInProgress = false;
                }
            } else if (waveSystem.hasMoreStages()) {
                floorManager.showDoorOpen();
            }
        }
    }

    private void handleTransition() {
        floorManager.completeTransition();
        dropRng = GameSession.getSeededRandomForStage(floorManager.getCurrentStage());
        pickups.clear();
        lootBoxes.clear();
        enemies.clear();
        activeMines.clear();
        activeTurrets.clear();
        activeScarecrow = null;
        waveInProgress = false;
        waveSystem.initStage(floorManager.getCurrentStage(), WaveSystem.WAVES_PER_STAGE);

        com.badlogic.gdx.math.Vector2 newSpawnPos = floorManager.getPlayerSpawnPosition();
        if (newSpawnPos == null) {
            game.setScreen(new MenuScreen(game));
            return;
        }
        player.getPosition().set(newSpawnPos.x, newSpawnPos.y);

        // NUEVO: Mostrar aviso de la nueva fase
        hud.showStageMessage(floorManager.getCurrentStage());
        spawnLootBoxes();
    }

    private void updateSystemEvents(float delta) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.F11)) {
            toggleFullscreen();
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE) && !isGameOver) {
            if (player.getExperienceSystem().getLevelsPending() <= 0) {
                isGamePaused = !isGamePaused;
                pauseUI.setVisible(isGamePaused);
                if (isGamePaused) {
                    pauseUI.toFront();
                    AudioManager.duckForPause();
                } else {
                    AudioManager.unduckFromPause();
                }
            }
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.K)) {
            if (player != null && player.getHealthComponent() != null) {
                player.getHealthComponent().currentHealth = 0;
            }
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.H)) {
            if (player != null) {
                DebugStats.add25PercentAllStats(player);
                Gdx.app.log("DEBUG", "Stats aumentadas 25% para verificar caps");
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

    private void spawnLootBoxes() {
        int count = Math.min(15, Math.max(6, 6 + floorManager.getCurrentStage()));
        Vector2 playerSpawnPos = new Vector2(player.getPosition());

        int mapW = 48;
        int mapH = 48;

        for (int i = 0; i < count * 3; i++) {
            if (lootBoxes.size >= count) break;

            int x, y;
            int edge = dropRng.nextInt(4);
            switch (edge) {
                case 0: x = 1 + dropRng.nextInt(4); y = 1 + dropRng.nextInt(mapH - 2); break;
                case 1: x = mapW - 5 + dropRng.nextInt(4); y = 1 + dropRng.nextInt(mapH - 2); break;
                case 2: x = 1 + dropRng.nextInt(mapW - 2); y = 1 + dropRng.nextInt(4); break;
                default: x = 1 + dropRng.nextInt(mapW - 2); y = mapH - 5 + dropRng.nextInt(4); break;
            }

            if (!floorManager.isWall(x, y)) {
                Vector2 pos = new Vector2(x + 0.5f, y + 0.5f);
                if (pos.dst(playerSpawnPos) < 10f) continue;

                LootBox box = new LootBox(pos, dropRng);
                lootBoxes.add(box);
            }
        }
    }

    private void updateLootBoxes(float delta) {
        for (int i = lootBoxes.size - 1; i >= 0; i--) {
            LootBox box = lootBoxes.get(i);
            if (!box.isAlive()) {
                spawnLootBoxDrop(box);
                lootBoxes.removeIndex(i);
                continue;
            }
            box.update(delta, player);
        }

        combatSystem.updateLootBoxes(player.getActiveProjectiles(), lootBoxes, delta);
        combatSystem.updateLootBoxes(spawner.getEnemyProjectiles(), lootBoxes, delta);
    }

    private void resolveLootBoxPhysics(float delta) {
        physicsSystem.resolveLootBoxCollision(player, lootBoxes, delta);
        physicsSystem.resolveLootBoxSeparation(lootBoxes, enemies, delta);
        for (LootBox box : lootBoxes) {
            if (box.isAlive()) {
                physicsSystem.resolveWallCollision(box, 0.4f);
            }
        }
    }

    private void spawnLootBoxDrop(LootBox box) {
        Vector2 pos = new Vector2(box.getPosition());
        switch (box.getDropType()) {
            case COINS:
                int stage = floorManager.getCurrentStage();
                int total = Math.round(box.getCoinAmount() * (1f + 0.15f * stage));
                if (GameSession.godMode) {
                    XPOrb orb = xpPool.obtain();
                    orb.init(pos, total);
                    pickups.add(orb);
                    break;
                }
                CoinPickup coin = coinPool.obtain();
                coin.init(pos, total);
                pickups.add(coin);
                break;
            case HEAL:
                MiniHeal heal = healPool.obtain();
                heal.init(pos);
                pickups.add(heal);
                break;
            case STAT:
                if (player.isStatCapped(box.getStatType())) break;
                StatPickup statPickup = statPool.obtain();
                statPickup.init(pos, box.getStatType(), box.getStatAmount());
                pickups.add(statPickup);
                hud.showStatNotification(statPickup.getLabelText());
                break;
        }
    }

    @Override public void resize(int w, int h) {
        viewport.update(w, h, true);
        hud.resize(w, h);
    }

    private void toggleFullscreen() {
        if (Gdx.graphics.isFullscreen()) {
            Gdx.graphics.setWindowedMode(1280, 720);
            SaveManager.saveFullscreen(false);
            SaveManager.saveResolution(1280, 720);
        } else {
            Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());
            SaveManager.saveFullscreen(true);
        }
        pauseUI.sincronizarSelectorResolucion();
    }

    private void loadDoorIndicatorTexture() {
        if (doorIndicatorTexture != null) doorIndicatorTexture.dispose();
        doorIndicatorTexture = null;
        doorIndicatorTimer = 0;

        int keycode = com.tikisadventure.input.InputConfig.getInteractKey();
        String keyName = com.badlogic.gdx.Input.Keys.toString(keycode);
        if (keyName == null || keyName.isEmpty()) keyName = "e";
        String fileName = "sprites/shared/map_assets/door_indicators/door_" + keyName.toLowerCase() + ".png";
        try {
            doorIndicatorTexture = new com.badlogic.gdx.graphics.Texture(com.badlogic.gdx.Gdx.files.internal(fileName));
        } catch (Exception e) {
            try {
                doorIndicatorTexture = new com.badlogic.gdx.graphics.Texture(com.badlogic.gdx.Gdx.files.internal("sprites/shared/map_assets/Door_indicator.png"));
            } catch (Exception e2) {
                doorIndicatorTexture = null;
            }
        }
    }

    private void ensureSpawnNotOnVoidOrQuicksand(com.badlogic.gdx.math.Vector2 pos) {
        for (int attempt = 0; attempt < 50; attempt++) {
            boolean onVoid = floorManager.isVoidTile(pos.x, pos.y);
            boolean onQuicksand = floorManager.isQuicksand(pos.x, pos.y);
            if (!onVoid && !onQuicksand) return;
            pos.x += 1f;
            if (pos.x >= 48) { pos.x = 1; pos.y += 1; }
            if (pos.y >= 48) { pos.y = 1; break; }
        }
    }

    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() { if (player != null) player.dispose(); }

    @Override
    public void dispose() {
        AudioEventSubscriber.dispose();
        if (batch != null) batch.dispose();
        if (shapeRenderer != null) shapeRenderer.dispose();
        if (floorManager != null) floorManager.dispose();
        if (combatFeedbackSystem != null) combatFeedbackSystem.dispose();
        if (effectManager != null) effectManager.dispose();
        if (trajectoryRenderer != null) trajectoryRenderer.dispose();
        if (pauseUI != null) pauseUI.dispose();
        if (doorIndicatorTexture != null) doorIndicatorTexture.dispose();
    }
}
