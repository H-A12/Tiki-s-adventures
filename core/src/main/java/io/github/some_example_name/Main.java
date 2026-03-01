package io.github.some_example_name;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import io.github.some_example_name.personajes.Personaje;
import io.github.some_example_name.personajes.jugables.Tiki;
import io.github.some_example_name.personajes.no_jugable.Slime;

/**
 * {@link com.badlogic.gdx.ApplicationListener} implementation shared by all
 * platforms.
 */
public class Main extends ApplicationAdapter {
    private SpriteBatch batch;
    private Tiki tiki;
    private OrthogonalTiledMapRenderer renderer;
    private OrthographicCamera camara;

    // ------------PRUEBA----------------------------

    private Array<Slime> slimes = new Array<>();
    private float tiempoDesdeUltimoSpawn = 0;
    private final float TIEMPO_ENTRE_SPAWNS = 1f; // segundos
    private final int MAX_SLIMES = 100000;
    private final float RADIO_SPAWN = 5f;
    private float tiempoDesdeUltimoGolpe;
    // ------------PRUEBA----------------------------

    // PRUEBA BARRA DE VIDA

    private ShapeRenderer shapeRenderer;
    private OrthographicCamera camaraHUD;

    // ---------------------------------------

    @Override
    public void create() {
        batch = new SpriteBatch();
        tiki = new Tiki();
        tiki.crearTiki();

        renderer = new OrthogonalTiledMapRenderer(new TiledMap(), 1 / 16f);

        camara = new OrthographicCamera();
        camara.setToOrtho(false, 30, 20);
        camara.update();
        tiki.getPosicion().set(
                camara.viewportWidth / 2 - tiki.getANCHO() / 2,
                camara.viewportHeight / 2 - tiki.getALTO() / 2);

        // PRUEBA BARRA DE VIDA

        shapeRenderer = new ShapeRenderer();

        camaraHUD = new OrthographicCamera();
        camaraHUD.setToOrtho(
                false,
                Gdx.graphics.getWidth(),
                Gdx.graphics.getHeight());
        camaraHUD.update();

        // ------------------
    }

    @Override
    public void render() {
        ScreenUtils.clear(0.7f, 0.7f, 1.0f, 1);
        float deltaTime = Gdx.graphics.getDeltaTime();

        // 1️⃣ Update jugador
        tiki.update(deltaTime);

        // 2️⃣ Mover cámara
        camara.position.set(tiki.getPosicion(), 0);
        camara.update();
        renderer.setView(camara);
        renderer.render();

        // 3️⃣ Generar / updatear Slimes
        generarSlimes(deltaTime, tiki);

        tiempoDesdeUltimoGolpe += deltaTime;

        // 4️⃣ Lógica de daño al jugador
        for (Slime slime : slimes) {
            if (!slime.isAlive()) continue;

            if (slime.getHitboxActionTrigger().overlaps(tiki.getHitboxActionTrigger())
                && tiempoDesdeUltimoGolpe >= 0.5f) {

                tiki.setVida(tiki.getVida() - slime.getDanyo());
                System.out.println("Vida Tiki: " + tiki.getVida());

                if (tiki.getVida() <= 0)
                    Gdx.app.exit();

                tiempoDesdeUltimoGolpe = 0;
            }
        }

        // 5️⃣ Empuje entre slimes (física)
        float SUAVIDAD_EMPUJE = 50f;
        for (int i = 0; i < slimes.size; i++) {
            for (int j = i + 1; j < slimes.size; j++) {
                Slime a = slimes.get(i);
                Slime b = slimes.get(j);

                if (!a.isAlive() || !b.isAlive()) continue;

                Vector2 dir = new Vector2(a.getPosicion()).sub(b.getPosicion());
                float distancia = dir.len();
                float radioMinimo = a.getHitboxActionTrigger().radius + b.getHitboxActionTrigger().radius;

                if (distancia > 0 && distancia < radioMinimo) {
                    float penetracion = radioMinimo - distancia;
                    dir.nor();
                    float fuerza = penetracion * SUAVIDAD_EMPUJE * deltaTime;
                    a.getPosicion().add(dir.scl(fuerza * 0.5f));
                    b.getPosicion().sub(dir.scl(fuerza * 0.5f));
                    a.actualizarHitboxes();
                    b.actualizarHitboxes();
                }
            }
        }

        // 6️⃣ Renderizar todo
        Batch batch = renderer.getBatch();
        batch.begin();

        // Render jugador
        tiki.render(batch, deltaTime);

        // Render Slimes
        for (Slime slime : slimes) {
            if (!slime.isAlive()) continue; // no renderizar muertos
            slime.render(batch, deltaTime);
        }

        batch.end();

        // 7️⃣ HUD
        shapeRenderer.setProjectionMatrix(camaraHUD.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        dibujarBarraVidaHUD();
        shapeRenderer.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        dibujarBordeBarraHUD();
        shapeRenderer.end();
    }

    @Override
    public void dispose() {
        batch.dispose();
    }

    // --------------------------prueba-----------------------------

    private void generarSlimes(float deltaTime, Personaje jugador) {
        tiempoDesdeUltimoSpawn += deltaTime;

        // comprobar si se puede generar un nuevo slime
        if (tiempoDesdeUltimoSpawn >= TIEMPO_ENTRE_SPAWNS && slimes.size < MAX_SLIMES) {
            tiempoDesdeUltimoSpawn = 0;

            // posición aleatoria alrededor del jugador
            float angulo = MathUtils.random(0f, 360f);
            float x = jugador.getPosicion().x + MathUtils.cosDeg(angulo) * RADIO_SPAWN;
            float y = jugador.getPosicion().y + MathUtils.sinDeg(angulo) * RADIO_SPAWN;

            // crear y configurar el slime
            Slime slime = new Slime();
            slime.crearSlime();
            slime.getPosicion().set(x, y);

            // añadir a la lista de enemigos activos
            slimes.add(slime);
        }

        // actualizar todos los slimes
        for (int i = slimes.size - 1; i >= 0; i--) {

            Slime slime = slimes.get(i);

            if (!slime.isAlive()) {
                slimes.removeIndex(i);  // elimina al instante
                continue;
            }

            slime.update(deltaTime, jugador);  // solo vivos
        }
    }

    private void dibujarBarraVidaHUD() {
        float anchoMax = 200;
        float alto = 20; // Un poco más alta

        float x = 20;
        float y = Gdx.graphics.getHeight() - 40;

        // Fondo oscuro de la barra
        shapeRenderer.setColor(0.1f, 0.1f, 0.1f, 0.8f);
        shapeRenderer.rect(x, y, anchoMax, alto);

        // Barra de vida con color dinámico
        float porcentaje = Math.max(0, tiki.getVida() / tiki.getVida_max());

        if (porcentaje > 0.5f) {
            shapeRenderer.setColor(0.2f, 0.8f, 0.2f, 1); // Verde suave
        } else if (porcentaje > 0.25f) {
            shapeRenderer.setColor(0.8f, 0.8f, 0.2f, 1); // Amarillo
        } else {
            shapeRenderer.setColor(0.8f, 0.2f, 0.2f, 1); // Rojo
        }

        shapeRenderer.rect(x, y, anchoMax * porcentaje, alto);
    }

    private void dibujarBordeBarraHUD() {
        float anchoMax = 200;
        float alto = 20;

        float x = 20;
        float y = Gdx.graphics.getHeight() - 40;

        shapeRenderer.setColor(0, 0, 0, 1); // Borde negro
        shapeRenderer.rect(x, y, anchoMax, alto);
    }

}
