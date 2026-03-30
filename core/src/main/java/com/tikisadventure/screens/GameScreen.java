package com.tikisadventure.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import com.tikisadventure.assets.Assets;
import com.tikisadventure.entities.base.Entity;
import com.tikisadventure.entities.player.*;
import com.tikisadventure.systems.*;
import com.tikisadventure.combat.projectiles.ProjectileFactory;
import com.tikisadventure.effects.EffectManager;
import com.tikisadventure.hud.HUD;

public class GameScreen implements Screen {

    private final Game game;
    private final SpriteBatch batch; // El pincel compartido inyectado desde Main

    private Player player;
    private CharacterProfile playerProfile;

    private SystemManager systems;
    private RenderSystem renderSystem;
    private EffectManager effectManager;
    private HUD hud;

    private OrthographicCamera camera;
    private Viewport viewport;

    private final Array<Entity> allEntities = new Array<>();

    /**
     * Constructor mejorado: Recibe el batch directamente para evitar casts.
     */
    public GameScreen(Game game, SpriteBatch batch) {
        this.game = game;
        this.batch = batch;
    }

    @Override
    public void show() {
        // Configuración de visualización (20x20 metros/unidades de mundo)
        camera = new OrthographicCamera();
        viewport = new FitViewport(20, 20, camera);

        // Inicialización de sistemas usando el batch inyectado
        renderSystem = new RenderSystem(batch);
        effectManager = new EffectManager(300);

        // 1. Cargar configuración de personajes desde el nuevo JSON
        JsonReader reader = new JsonReader();
        JsonValue base = reader.parse(Gdx.files.internal("data/characters_config.json"));
        JsonValue tikiData = base.get("characters").get("tiki");

        // 2. Inicializar ProjectileFactory con textura base del Atlas
        TextureRegion bulletRegion = Assets.getTexture("projectiles/redbullet");
        ProjectileFactory pf = new ProjectileFactory(allEntities, effectManager, bulletRegion);

        // 3. Crear Perfil y Jugador (Factory basada en JSON)
        playerProfile = CharacterFactory.create(tikiData, pf, effectManager);
        player = new Player(playerProfile);
        player.getPosicion().set(10, 10);
        allEntities.add(player);

        // 4. Inicializar Lógica Global y UI
        systems = new SystemManager(allEntities, null, effectManager);
        hud = new HUD(batch);
    }

    @Override
    public void render(float delta) {
        // A. Lógica: Movimiento, Colisiones y Procesamiento de Sistemas
        systems.update(delta, allEntities, player);

        // B. Cámara: Seguimiento con suavizado (Lerp)
        float lerp = 0.1f;
        camera.position.x += (player.getPosicion().x - camera.position.x) * lerp;
        camera.position.y += (player.getPosicion().y - camera.position.y) * lerp;
        camera.update();

        // C. Dibujado
        ScreenUtils.clear(0.1f, 0.1f, 0.2f, 1);

        // Renderizado de Entidades con Y-Sorting
        renderSystem.process(allEntities, camera, delta, effectManager);

        // Interfaz de Usuario (HUD)
        hud.update(
            player.getVida(),
            player.getExperienceSystem(), // <--- Esto es lo que faltaba o estaba mal
            systems.getWaveSystem()
        );
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
        hud.resize(width, height);
    }

    @Override
    public void dispose() {
        effectManager.dispose();
        hud.dispose();
        // Nota: batch y assets no se disponen aquí, se gestionan en Main.java
    }

    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
}
