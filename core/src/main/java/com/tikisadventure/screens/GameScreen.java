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

    @Override
    public void show() {
        batch = new SpriteBatch();
        effectManager = new EffectManager(300);
        this.projectileFactory = new ProjectileFactory(effectManager, Assets.getRegion("shared", "RedBullet"), 200);
        this.weaponFactory = new WeaponFactory(projectileFactory, effectManager);

        // Cargar personaje y mapa desde la sesión
        waveSectionName = (com.tikisadventure.core.GameSession.selectedMapName != null)
            ? com.tikisadventure.core.GameSession.selectedMapName : "bosque";
        CharacterProfile profile = CharacterFactory.getInstance().create(com.tikisadventure.core.GameSession.selectedCharacterId, projectileFactory, effectManager);

        player = new Player(profile);
        player.getPosition().set(10, 10);

        camera = new OrthographicCamera();
        viewport = new FitViewport(20, 20, camera);
        floorManager = new FloorManager(true);
        physicsSystem = new PhysicsSystem(floorManager);
        combatSystem = new CombatSystem(effectManager);
        combatFeedbackSystem = new CombatFeedbackSystem();
        movementSystem = new MovementSystem(effectManager);
        renderSystem = new RenderSystem();
        waveSystem = new WaveSystem(waveSectionName);
        spawner = new EnemySpawner(enemies, floorManager, waveSystem, effectManager);

        setupPlayerWeapons();
        hud = new HUD(new SpriteBatch());
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

        hud.update(
            player.getVida(),
            player.getExperienceSystem(),
            player.getScore(),
            player.getAbility1CooldownPercent(),
            player.getAbility2CooldownPercent()
        );

        int currentLevel = player.getExperienceSystem().getLevel();
        if (currentLevel > lastKnownLevel) {
            hud.showLevelUpWindow();
            lastKnownLevel = currentLevel;
        }

        if (isGamePaused) {
            return;
        }

        if (player.getVida() <= 0) {
            saveScore(player.getScore());
            game.setScreen(new GameScreen(game));
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

        updateSystemEvents(delta);
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
            if (!p.isAlive()) pickups.removeIndex(i);
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
        if (Gdx.input.isKeyPressed(Input.Keys.R)) {
            restartTimer += delta;
            if (restartTimer > 1f) game.setScreen(new GameScreen(game));
        } else { restartTimer = 0; }
    }

    private void spawnDrop(Vector2 pos, int exp) {
        if (Math.random() < 0.8f) pickups.add(new XPOrb(new Vector2(pos), exp));
        else if (Math.random() < 0.1f) pickups.add(new MiniHeal(new Vector2(pos)));
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
    @Override public void pause() {} @Override public void resume() {} @Override public void hide() {}
    @Override
    public void dispose() {
        if (batch != null) batch.dispose();
        if (shapeRenderer != null) shapeRenderer.dispose();
        if (floorManager != null) floorManager.dispose();
        if (combatFeedbackSystem != null) combatFeedbackSystem.dispose();
        if (effectManager != null) effectManager.dispose();
        if (trajectoryRenderer != null) trajectoryRenderer.dispose();
    }

    private void saveScore(int newScore) {
        com.badlogic.gdx.Preferences prefs = Gdx.app.getPreferences("TikiScores");
        java.util.List<Integer> scores = new java.util.ArrayList<>();
        for (int i = 0; i < 5; i++) {
            scores.add(prefs.getInteger("score_" + i, 0));
        }
        scores.add(newScore);
        java.util.Collections.sort(scores, java.util.Collections.reverseOrder());
        for (int i = 0; i < 5; i++) {
            prefs.putInteger("score_" + i, scores.get(i));
        }
        prefs.flush();
        System.out.println("[Guardado Local] Puntuacion guardada.");
    }
}
