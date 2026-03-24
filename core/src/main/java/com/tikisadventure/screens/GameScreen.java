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
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import com.tikisadventure.combat.weapons.SimplePistol;
import com.tikisadventure.combat.weapons.NormalBullet;
import com.tikisadventure.combat.weapons.PiercingBullet;
import com.tikisadventure.combat.weapons.SplitBullet;
import com.tikisadventure.entities.Entity;
import com.tikisadventure.entities.enemies.Slime;
import com.tikisadventure.entities.pickup.MiniHeal;
import com.tikisadventure.entities.pickup.Pickup;
import com.tikisadventure.entities.pickup.XPOrb;
import com.tikisadventure.entities.player.CharacterProfile;
import com.tikisadventure.entities.player.Player;
import com.tikisadventure.entities.abilities.Ability;
import com.tikisadventure.hud.HUD;
import com.tikisadventure.systems.EnemySpawner;
import com.tikisadventure.systems.MapCollisionSystem;

public class GameScreen implements Screen {

    private Game game;
    private Player player;
    private CharacterProfile tikiProfile;
    private Texture tikiTexture;

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
        // 1. DEFINIR HABILIDAD (DASH) - Corregido el acceso al profile
        Ability dash = new Ability() {
            @Override
            public void activate(Player owner, Array<Entity> enemies) {
                final float originalSpeed = owner.getProfile().speed;
                owner.setSpeed(originalSpeed * 3f);
                Timer.schedule(new Timer.Task() {
                    @Override
                    public void run() { owner.setSpeed(originalSpeed); }
                }, 0.15f);
            }
            @Override public float getCooldown() { return 2.0f; }
            @Override public String getName() { return "Dash"; }
        };

        // 2. CREAR PERFIL
        tikiProfile = new CharacterProfile("Tiki", 100f, 5f, dash);
        tikiTexture = new Texture("tiki.png");
        tikiProfile.sprite = new TextureRegion(tikiTexture);

        // 3. INICIALIZAR JUGADOR
        player = new Player(tikiProfile);
        player.getPosicion().set(10, 10);

        // 4. MUNDO Y SISTEMAS
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

        // 5. EQUIPAR ARMAS - Importante: Usar NormalBullet corregida con 'player'
        player.getWeaponManager().addWeapon(new SimplePistol(player,
            (pos, dir, spd, dmg, sz) -> new NormalBullet(player, pos, dir, spd, dmg, sz)
        ));
        player.getWeaponManager().addWeapon(new SimplePistol(player,
            (pos, dir, spd, dmg, sz) -> new PiercingBullet(player, pos, dir, spd, dmg, sz)
        ));
        player.getWeaponManager().addWeapon(new SimplePistol(player,
            (pos, dir, spd, dmg, sz) -> new SplitBullet(player, pos, dir, spd, dmg, sz)
        ));

        hud = new HUD(mapRenderer.getBatch());
        shapeRenderer = new ShapeRenderer();
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

        // Dibujamos pickups primero para que estén "bajo" los pies
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
            // Podrías añadir una pantalla de Game Over aquí
            Gdx.app.exit();
            return;
        }

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

        // CRÍTICO: Actualizamos el HUD con el ExperienceSystem real de Tiki
        hud.update(player.getVida(), player.getExperienceSystem());
    }

    private void updatePickups(float delta) {
        for (int i = pickups.size - 1; i >= 0; i--) {
            Pickup p = pickups.get(i);
            p.update(delta, player); // Esto activa onPickup y suma la XP
            if (!p.isAlive()) {
                pickups.removeIndex(i);
            }
        }
    }

    private void updateEnemies(float delta) {
        for (int i = enemies.size - 1; i >= 0; i--) {
            Entity enemy = enemies.get(i);
            if (enemy.isAlive()) {
                enemy.update(delta, player);
            } else {
                // El enemigo muere: soltamos experiencia basada en sus stats
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
        if (tikiTexture != null) tikiTexture.dispose();
    }
}
