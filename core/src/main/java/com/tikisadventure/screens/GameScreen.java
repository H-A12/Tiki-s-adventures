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
import com.tikisadventure.systems.PhysicsSystem;
import com.tikisadventure.systems.CombatSystem;
import com.tikisadventure.systems.CombatFeedbackSystem;
import com.tikisadventure.systems.MovementSystem;
import com.tikisadventure.effects.EffectManager;
import com.tikisadventure.floors.FloorManager;

public class GameScreen implements Screen {

    private final Game game;
    private Player player;
    private CharacterProfile tikiProfile, mokoProfile, zukiProfile;
    private OrthographicCamera camera;
    private Viewport viewport;
    private final Array<Entity> enemies = new Array<>();
    private final Array<Pickup> pickups = new Array<>();
    private EnemySpawner spawner;
    private HUD hud;
    private ShapeRenderer shapeRenderer;
    private SpriteBatch batch;
    private WaveSystem waveSystem;
    private EffectManager effectManager;
    private ProjectileFactory projectileFactory;
    private WeaponFactory weaponFactory;
    private FloorManager floorManager;
    private PhysicsSystem physicsSystem;
    private CombatSystem combatSystem;
    private CombatFeedbackSystem combatFeedbackSystem;
    private MovementSystem movementSystem;

    private boolean waveInProgress = false;
    private boolean doorAvailable = false;
    private String waveSectionName = "default";

    private float damageCooldown = 0;
    private float restartTimer = 0f;

    public GameScreen(Game game) { this.game = game; }
    public GameScreen(Game game, String waveSection) {
        this.game = game;
        this.waveSectionName = waveSection;
    }

    @Override
    public void show() {
        batch = new SpriteBatch();
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
        physicsSystem = new PhysicsSystem(floorManager);
        combatSystem = new CombatSystem(effectManager);
        combatFeedbackSystem = new CombatFeedbackSystem();
        movementSystem = new MovementSystem(effectManager);
        waveSystem = new WaveSystem(waveSectionName);
        spawner = new EnemySpawner(enemies, floorManager, waveSystem);

        setupPlayerWeapons();
        hud = new HUD(new SpriteBatch());
        shapeRenderer = new ShapeRenderer();
    }

    private void setupPlayerWeapons() {
        WeaponManager manager = player.getWeaponFactory();
        manager.clear();
        manager.addWeapon(weaponFactory.createWeapon("MetralletaEjemplo", player));
     //   manager.addWeapon(weaponFactory.createWeapon("LanzaCohetesEjemplo", player));
        manager.addWeapon(weaponFactory.createWeapon("ArmaEnergiaEjemplo", player));
        manager.addWeapon(weaponFactory.createWeapon("MetralletaEjemplo", player));
    }

    @Override
    public void render(float delta) {
        // Actualizar cámara primero
        float camOffset = floorManager.isTransitionActive() ? floorManager.getCameraOffset() : 0;
        camera.position.set(player.getPosicion().x, player.getPosicion().y + camOffset, 0);
        camera.update();

        // Calcular puntero usando la cámara directamente con Vector3
        com.badlogic.gdx.math.Vector3 mouseWorld3 = new com.badlogic.gdx.math.Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
        camera.unproject(mouseWorld3);
        Vector2 mouseWorld = new Vector2(mouseWorld3.x, mouseWorld3.y);
        
        player.getWeaponFactory().setManualAim(Gdx.input.isButtonPressed(Input.Buttons.LEFT), mouseWorld);

        update(delta);


        ScreenUtils.clear(0.1f, 0.1f, 0.2f, 1);
        
        floorManager.renderMap(camera);

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        floorManager.renderEntities(batch);
        for (Pickup p : pickups) p.render(batch, delta);
        for (Entity e : enemies) if (e.isAlive()) e.render(batch, delta);
        effectManager.render(batch);
        player.render(batch, delta);
        combatFeedbackSystem.render(batch);
        batch.end();

        renderDebugHitboxes();
        hud.render();
    }

    private void update(float delta) {

        if (player.getVida() <= 0) {
            saveScore(player.getScore());
            game.setScreen(new GameScreen(game));
            return;
        }

        if (damageCooldown > 0) damageCooldown -= delta;

        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1)) switchCharacter(tikiProfile);
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_2)) switchCharacter(mokoProfile);
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_3)) switchCharacter(zukiProfile);

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
        hud.update(player.getVida(), player.getExperienceSystem(), player.getScore());
    }

    private void handleGameplay(float delta) {
        boolean nearDoor = floorManager.isPlayerNearDoor(player.getPosicion());
        if (Gdx.input.isKeyJustPressed(Input.Keys.E) && nearDoor) {
            Gdx.app.log("GAME", "Cambiando de nivel...");
            floorManager.useDoor();
            doorAvailable = false;
            return;
        }

        player.update(delta, enemies);
        movementSystem.update(player.getActiveProjectiles(), enemies, delta);
        combatSystem.update(player.getActiveProjectiles(), enemies, delta);
        spawner.update(delta, player);
        updateWaveLogic(delta);
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

    private void switchCharacter(CharacterProfile newProfile) {
        Vector2 pos = new Vector2(player.getPosicion());
        float currentVida = player.getVida();
        int currentScore = player.getScore();
        player = new Player(newProfile);
        player.getPosicion().set(pos);
        player.setVida(currentVida);
        player.setScore(currentScore);
        setupPlayerWeapons();
    }

    private void updateEnemies(float delta) {
        for (int i = enemies.size - 1; i >= 0; i--) {
            Entity enemy = enemies.get(i);
            if (enemy.isAlive()) {
                enemy.update(delta, player);
                physicsSystem.resolveWallCollision(enemy, 0.4f);
            } else {
                spawnDrop(enemy.getPosicion(), enemy.getExperience());
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

    private void updateWaveLogic(float delta) {
        if (!waveInProgress && !floorManager.isTransitionActive()) {
            spawner.resetForNewWave();
            waveInProgress = true;
        }
        if (waveInProgress && spawner.isWaveSpawningComplete() && enemies.size == 0) {
            if (!doorAvailable && floorManager.getCurrentFloor() < floorManager.getTotalFloors()) {
                floorManager.showDoor();
                doorAvailable = true;
            }
        }
    }

    private void handleTransition() {
        floorManager.completeTransition();
        pickups.clear();
        enemies.clear();
        doorAvailable = false;
        waveInProgress = false;
        waveSystem.nextWave();
        int[] spawnPos = floorManager.findValidSpawnPosition(8, 12, 8, 12);
        player.getPosicion().set(spawnPos[0], spawnPos[1]);
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
