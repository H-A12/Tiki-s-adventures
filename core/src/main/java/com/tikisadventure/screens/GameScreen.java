package com.tikisadventure.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
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
import com.tikisadventure.combat.weapons.Weapon;
import com.tikisadventure.combat.weapons.WeaponFactory;
import com.tikisadventure.combat.weapons.WeaponManager;
import com.tikisadventure.core.Assets;
import com.tikisadventure.core.GameSession;
import com.tikisadventure.core.SaveManager;
import com.tikisadventure.entities.base.Entity;
import com.tikisadventure.entities.enemies.ConfigurableEnemy;
import com.tikisadventure.entities.pickup.MiniHeal;
import com.tikisadventure.entities.pickup.Pickup;
import com.tikisadventure.entities.pickup.XPOrb;
import com.tikisadventure.entities.player.Player;
import com.tikisadventure.entities.player.CharacterProfile;
import com.tikisadventure.entities.player.CharacterFactory;
import com.tikisadventure.systems.*;
import com.tikisadventure.systems.powerUps.PowerUp;
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

    private final Vector3 mouseWorld3 = new Vector3();
    private final Vector2 mouseWorld = new Vector2();
    private PowerUpSystem powerUpSystem;

    // --- NUEVAS VARIABLES PARA EL MULTI-PASE (SHADERS Y BUFFERS) ---
    private ShaderProgram shaderMascara;
    private ShaderProgram shaderContorno;
    private FrameBuffer sceneFBO;
    private FrameBuffer maskFBO;
    private Mesh screenQuad;
    private SpriteBatch postProcessBatch;
    // ---------------------------------------------------------------

    public GameScreen(Game game) { this.game = game; }

    private final Pool<XPOrb> xpPool = new Pool<XPOrb>(200) {
        @Override protected XPOrb newObject() { return new XPOrb(); }
    };

    private final Pool<MiniHeal> healPool = new Pool<MiniHeal>(50) {
        @Override protected MiniHeal newObject() { return new MiniHeal(); }
    };

    @Override
    public void show() {

        isGamePaused = false;
        batch = new SpriteBatch();
        effectManager = new EffectManager(300);
        this.projectileFactory = new ProjectileFactory(effectManager, Assets.getRegion("shared", "RedBullet"), 200);
        this.weaponFactory = new WeaponFactory(projectileFactory, effectManager);

        powerUpSystem = new PowerUpSystem(weaponFactory);

        // --- INICIALIZACIÓN DEL SISTEMA DE SHADERS Y FRAMEBUFFERS ---
        ShaderProgram.pedantic = false;

        shaderMascara = new ShaderProgram(Gdx.files.internal("shaders/mask.vert"), Gdx.files.internal("shaders/mask.frag"));
        if (!shaderMascara.isCompiled()) Gdx.app.error("SHADER", "Error en Máscara: " + shaderMascara.getLog());

        shaderContorno = new ShaderProgram(Gdx.files.internal("shaders/outline.vert"), Gdx.files.internal("shaders/outline.frag"));
        if (!shaderContorno.isCompiled()) Gdx.app.error("SHADER", "Error en Contorno: " + shaderContorno.getLog());

        int w = Gdx.graphics.getWidth();
        int h = Gdx.graphics.getHeight();

        sceneFBO = new FrameBuffer(Pixmap.Format.RGBA8888, w, h, true);
        maskFBO = new FrameBuffer(Pixmap.Format.RGBA8888, w, h, false);
        postProcessBatch = new SpriteBatch();

        float[] vertices = {
            -1.0f, -1.0f, 0.0f,      0.0f, 0.0f,
            1.0f, -1.0f, 0.0f,      1.0f, 0.0f,
            1.0f,  1.0f, 0.0f,      1.0f, 1.0f,
            -1.0f,  1.0f, 0.0f,      0.0f, 1.0f
        };
        short[] indices = { 0, 1, 2, 2, 3, 0 };

        screenQuad = new Mesh(true, 4, 6,
            new VertexAttribute(VertexAttributes.Usage.Position, 3, "a_position"),
            new VertexAttribute(VertexAttributes.Usage.TextureCoordinates, 2, "a_texCoord0"));
        screenQuad.setVertices(vertices);
        screenQuad.setIndices(indices);
        // ---------------------------------------------------------------

        // Cargar personaje y mapa
        waveSectionName = (GameSession.selectedMapName != null)
            ? GameSession.selectedMapName : "bosque";
        CharacterProfile profile = CharacterFactory.getInstance().create(GameSession.selectedCharacterId, projectileFactory, effectManager);

        player = new Player(profile);
        player.getPosition().set(10, 10);

        camera = new OrthographicCamera();
        viewport = new FitViewport(20, 20, camera);
        floorManager = new FloorManager(true);
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

    // --- NUEVO MÉTODO RENDER CON MULTI-PASE ---
    @Override
    public void render(float delta) {
        update(delta);

        float camOffset = floorManager.isTransitionActive() ? floorManager.getCameraOffset() : 0;
        camera.position.set(player.getPosition().x, player.getPosition().y + camOffset, 0);
        camera.update();

        mouseWorld3.set(Gdx.input.getX(), Gdx.input.getY(), 0);
        camera.unproject(mouseWorld3);
        mouseWorld.set(mouseWorld3.x, mouseWorld3.y);
        player.getWeaponFactory().setManualAim(Gdx.input.isButtonPressed(Input.Buttons.LEFT), mouseWorld);

        // ==========================================================
        // PASO 1: RENDERIZAR LA ESCENA NORMAL (EN EL FBO DE ESCENA)
        // ==========================================================
        sceneFBO.begin();
        ScreenUtils.clear(0.1f, 0.1f, 0.2f, 1);

        floorManager.renderMap(camera);

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        batch.setShader(null); // Aseguramos shader normal

        floorManager.renderEntities(batch);
        for (Pickup p : pickups) p.render(batch, delta);
        renderSystem.render(enemies, batch, delta);
        renderSystem.renderProjectiles(spawner.getEnemyProjectiles(), batch, delta);
        effectManager.render(batch);
        renderSystem.render(player, batch, delta);
        combatFeedbackSystem.render(batch);

        if (Gdx.input.isButtonPressed(Input.Buttons.LEFT)) {
            TextureRegion crosshairRegion = Assets.getRegion("shared", "UI_Crosshair");
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
        renderDebugHitboxes();
        sceneFBO.end();


        // ==========================================================
        // PASO 2: RENDERIZAR LA MÁSCARA DEL ARMA (EN EL FBO DE MÁSCARA)
        // ==========================================================
        maskFBO.begin();
        ScreenUtils.clear(0, 0, 0, 0); // Fondo transparente

        batch.begin();
        batch.setShader(shaderMascara); // Activamos el shader que pinta blanco

        int maxTier = 1; // Para guardar el tier más alto que tenga el jugador

        // Solo dibujamos las armas que tengan Tier > 1 para crear su máscara
        for (Weapon arma : player.getWeaponFactory().getWeapons()) {
            if (arma.getTier() > 1) {
                // ATENCIÓN: Necesitarás que RenderSystem tenga un método renderWeapon()
                // para dibujar solo el arma en su posición actual.
                // Si no lo tienes, créalo en RenderSystem.java
                renderSystem.renderWeapon(arma, batch, delta);

                if(arma.getTier() > maxTier) {
                    maxTier = arma.getTier();
                }
            }
        }

        batch.setShader(null);
        batch.end();
        maskFBO.end();


        // ==========================================================
        // PASO 3: COMBINAR TODO CON EL SHADER DE CONTORNO
        // ==========================================================
        ScreenUtils.clear(0, 0, 0, 1);

        shaderContorno.begin();

        Texture sceneTexture = sceneFBO.getColorBufferTexture();
        Texture maskTexture = maskFBO.getColorBufferTexture();

        sceneTexture.bind(0);
        maskTexture.bind(1);

        shaderContorno.setUniformi("u_sceneTexture", 0);
        shaderContorno.setUniformi("u_maskTexture", 1);

        shaderContorno.setUniformf("u_texelSize", 1.0f / sceneFBO.getWidth(), 1.0f / sceneFBO.getHeight());

        Object outlineColor = null;
        shaderContorno.setUniformf("u_outlineColor", outlineColor.r, outlineColor.g, outlineColor.b, outlineColor.a);

        shaderContorno.setUniformf("u_outlineThreshold", 1.5f);

        screenQuad.render(shaderContorno, GL20.GL_TRIANGLES);

        shaderContorno.end();
        hud.render();
    }

    private void update(float delta) {
        updateSystemEvents(delta);

        hud.update(
            player.getVida(),
            player.getExperienceSystem(),
            player.getScore(),
            player.getAbility1CooldownPercent(),
            player.getAbility2CooldownPercent()
        );

        if (player.getExperienceSystem().getLevelsPending() > 0 && !isGamePaused) {
            isGamePaused = true;
            int currentLevel = player.getExperienceSystem().getLevel();
            Array<PowerUp> opciones = powerUpSystem.rollOptions(player, currentLevel, 3);
            hud.showLevelUpWindow(opciones);
        }

        if (isGamePaused) return;

        if (player.getVida() <= 0) {
            if (!GameSession.godMode) {
                SaveManager.addScoreRankProfileData(player.getScore());
                int oleadaAlcanzada = floorManager.getCurrentFloor();
                SaveManager.updateMaxWave(waveSectionName, oleadaAlcanzada);
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
        int[] spawnPos = floorManager.findValidSpawnPosition(8, 12, 8, 12);
        player.getPosition().set(spawnPos[0], spawnPos[1]);
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

    @Override public void resize(int w, int h) {
        viewport.update(w, h, true);
        hud.resize(w, h);
        // Si la ventana cambia de tamaño, idealmente hay que recrear los FBOs aquí,
        // pero por ahora para mantenerlo simple lo dejamos así.
    }
    @Override public void pause() {} @Override public void resume() {}
    @Override
    public void hide() {
        if (player != null) {
            player.dispose();
        }
    }

    private Color getColorFromTier(int tier) {
        switch (tier) {
            case 2: return new Color(0.0f, 1.0f, 0.0f, 1.0f); // Verde
            case 3: return new Color(0.0f, 0.5f, 1.0f, 1.0f); // Azul
            case 4: return new Color(0.6f, 0.0f, 0.8f, 1.0f); // Morado
            case 5: return new Color(1.0f, 0.8f, 0.0f, 1.0f); // Dorado
            default: return new Color(0, 0, 0, 0); // Transparente (Tier 1)
        }
    }

    // --- NO OLVIDES DESTRUIR LA MEMORIA DE LOS NUEVOS OBJETOS ---
    @Override
    public void dispose() {
        if (batch != null) batch.dispose();
        if (shapeRenderer != null) shapeRenderer.dispose();
        if (floorManager != null) floorManager.dispose();
        if (combatFeedbackSystem != null) combatFeedbackSystem.dispose();
        if (effectManager != null) effectManager.dispose();
        if (trajectoryRenderer != null) trajectoryRenderer.dispose();

        // Disposes del Multi-Pase
        if (postProcessBatch != null) postProcessBatch.dispose();
        if (sceneFBO != null) sceneFBO.dispose();
        if (maskFBO != null) maskFBO.dispose();
        if (screenQuad != null) screenQuad.dispose();
        if (shaderMascara != null) shaderMascara.dispose();
        if (shaderContorno != null) shaderContorno.dispose();
    }
}
