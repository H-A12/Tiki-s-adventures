package com.tikisadventure.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.tikisadventure.core.GameSession;
import com.tikisadventure.entities.player.CharacterFactory;
import com.tikisadventure.ui.CharacterPreviewActor;

public class MenuMapScreen implements Screen {
    private final Game game;
    private Stage stage;
    private Skin uiSkin;

    private Group grupoFondos;
    private ImagenFondo fondoBosque, fondoDesierto, fondoCueva, fondoMostradoActualmente;
    private Label labelDesc;
    private TextButton btnJugar;
    private Texture blackTexture;

    public MenuMapScreen(Game game) {
        this.game = game;
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.BLACK);
        pixmap.fill();
        this.blackTexture = new Texture(pixmap);
        pixmap.dispose();
    }

    // 1. Cambiamos a ExtendViewport para evitar el "recorte" de las barras negras
    @Override
    public void show() {
        // ExtendViewport: Mínimo 800x480, pero se expande si la ventana es más grande
        stage = new Stage(new com.badlogic.gdx.utils.viewport.ExtendViewport(800, 480));
        Gdx.input.setInputProcessor(stage);
        uiSkin = new Skin(Gdx.files.internal("uiskin.json"));

        grupoFondos = new Group();
        // Importante: No les damos tamaño 0, les damos el tamaño base
        fondoBosque = new ImagenFondo(new Texture(Gdx.files.internal("Menu/MenuMapas/fondo_bosque.png")));
        fondoDesierto = new ImagenFondo(new Texture(Gdx.files.internal("Menu/MenuMapas/fondo_desierto.png")));
        fondoCueva = new ImagenFondo(new Texture(Gdx.files.internal("Menu/MenuMapas/fondo_cueva.png")));

        // Inicialización correcta de alfas para que la primera transición funcione
        fondoBosque.getColor().a = 1f;
        fondoDesierto.getColor().a = 0f;
        fondoCueva.getColor().a = 0f;

        grupoFondos.addActor(fondoBosque);
        grupoFondos.addActor(fondoDesierto);
        grupoFondos.addActor(fondoCueva);
        stage.addActor(grupoFondos);

        fondoMostradoActualmente = fondoBosque;

        crearTablaInterfaz();

        // Dentro de show(), reemplaza el telón inicial por esto:
        final Image telonIn = new Image(blackTexture);
        // Usamos el tamaño del mundo calculado tras el primer resize automático
        telonIn.setSize(stage.getViewport().getWorldWidth(), stage.getViewport().getWorldHeight());
        telonIn.setPosition(0, 0);
        telonIn.setTouchable(Touchable.disabled);
        stage.addActor(telonIn);
        telonIn.toFront();
        telonIn.addAction(Actions.sequence(Actions.fadeOut(0.6f), Actions.removeActor()));
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // 1. Dibujamos el fondo estirado MANUALMENTE (Ignorando el Viewport)
        // Usamos la matriz de proyección ortográfica de la ventana real
        stage.getBatch().getProjectionMatrix().setToOrtho2D(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        stage.getBatch().begin();
        for (Actor a : grupoFondos.getChildren()) {
            ImagenFondo f = (ImagenFondo) a;
            if (f.getColor().a > 0) {
                stage.getBatch().setColor(1, 1, 1, f.getColor().a);
                stage.getBatch().draw(f.textura, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
            }
        }
        stage.getBatch().end();

        // 2. Dibujamos la UI normalmente
        stage.getViewport().apply();
        stage.getBatch().setProjectionMatrix(stage.getCamera().combined);
        stage.act(delta);
        stage.draw();
    }

    private void actualizarSeleccion(ImagenFondo siguiente, String desc) {
        labelDesc.setText(desc);
        boolean bloqueado = desc.contains("Bloqueado");
        btnJugar.setDisabled(bloqueado);
        btnJugar.setColor(bloqueado ? new Color(1, 1, 1, 0.5f) : Color.WHITE);

        if (siguiente != fondoMostradoActualmente) {
            // Aseguramos que el que entra esté encima y limpie sus acciones previas
            siguiente.toFront();
            siguiente.clearActions();
            fondoMostradoActualmente.clearActions();

            // Sincronizamos los alfas manualmente para evitar el fallo del "primer cambio"
            siguiente.addAction(Actions.fadeIn(0.5f));
            fondoMostradoActualmente.addAction(Actions.fadeOut(0.5f));

            fondoMostradoActualmente = siguiente;
        }
    }

    private void crearTablaInterfaz() {
        Table mainTable = new Table();
        mainTable.setFillParent(true);
        mainTable.padTop(30);

        Table tablaMapas = new Table();
        final Button btnBosque = crearBotonMapa("Menu/MenuMapas/icon_bosque.png");
        final Button btnDesierto = crearBotonMapa("Menu/MenuMapas/icon_desierto.png");
        final Button btnCueva = crearBotonMapa("Menu/MenuMapas/icon_cueva.png");

        new ButtonGroup<>(btnBosque, btnDesierto, btnCueva);
        btnBosque.setChecked(true);
        btnBosque.setTransform(true); // Necesario para escalar
        btnBosque.setScale(1.2f);

        labelDesc = new Label("BOSQUE: Peligros y tesoros ocultos.", uiSkin);
        labelDesc.setWrap(true);

        tablaMapas.add(new Label("SELECCIONA MAPA", uiSkin)).padBottom(20).center().row();
        tablaMapas.add(btnBosque).size(120, 80).padBottom(10).center().row();
        tablaMapas.add(btnDesierto).size(120, 80).padBottom(10).center().row();
        tablaMapas.add(btnCueva).size(120, 80).padBottom(10).center().row();
        tablaMapas.add(labelDesc).width(200).height(40).center();

        btnBosque.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (btnBosque.isChecked()) {
                    actualizarSeleccion(fondoBosque, "BOSQUE: Peligros y tesoros.");
                    btnBosque.addAction(Actions.scaleTo(1.2f, 1.2f, 0.15f));
                } else btnBosque.addAction(Actions.scaleTo(1f, 1f, 0.15f));
            }
        });

        btnDesierto.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (btnDesierto.isChecked()) {
                    actualizarSeleccion(fondoDesierto, "DESIERTO: Bloqueado.");
                    btnDesierto.addAction(Actions.scaleTo(1.2f, 1.2f, 0.15f));
                } else btnDesierto.addAction(Actions.scaleTo(1f, 1f, 0.15f));
            }
        });

        btnCueva.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (btnCueva.isChecked()) {
                    actualizarSeleccion(fondoCueva, "CUEVA: Bloqueado.");
                    btnCueva.addAction(Actions.scaleTo(1.2f, 1.2f, 0.15f));
                } else btnCueva.addAction(Actions.scaleTo(1f, 1f, 0.15f));
            }
        });

        // --- SELECTOR PERSONAJES (Mantenemos tu lógica original) ---
        Table charTable = new Table();
        ButtonGroup<Button> groupChars = new ButtonGroup<>();
        JsonValue characterData = new JsonReader().parse(Gdx.files.internal("data/player_config.json"));
        for (JsonValue charEntry : characterData.get("characters")) {
            final String id = charEntry.getString("id");
            Animation<TextureRegion> idleAnim = CharacterFactory.getCharacterIdleAnimation(id);
            Button btnChar = new Button(uiSkin);
            btnChar.add(new CharacterPreviewActor(idleAnim)).size(48, 48);
            btnChar.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    if (btnChar.isChecked()) GameSession.selectedCharacterId = id;
                }
            });
            groupChars.add(btnChar);
            charTable.add(btnChar).size(64, 64).pad(10);
        }

        btnJugar = new TextButton("¡EMPEZAR!", uiSkin);
        btnJugar.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (!btnJugar.isDisabled()) {
                    ejecutarFading(false, () -> game.setScreen(new GameScreen(game)));
                }
            }
        });

        TextButton btnVolver = new TextButton("Volver", uiSkin);
        btnVolver.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                ejecutarFading(false, () -> game.setScreen(new MenuScreen(game)));
            }
        });

        mainTable.add(tablaMapas).width(250).padLeft(40).top();
        mainTable.add(btnJugar).size(180, 80).expandX().center();
        mainTable.row();
        mainTable.add(charTable).colspan(2).expandY().center().padBottom(40);

        // Al final de crearTablaInterfaz, después de añadir todo a la mainTable:
        mainTable.layout(); // Fuerza a calcular las posiciones
        // Opcionalmente, si notas que sigue saltando:
        tablaMapas.setRound(false);
        mainTable.setRound(false);

        stage.addActor(mainTable);

        btnVolver.setSize(120, 50);
        btnVolver.setPosition(800 - 160, 40);
        stage.addActor(btnVolver);
    }

    private void ejecutarFading(boolean entrar, final Runnable accion) {
        final Image fade = new Image(blackTexture);

        // CORRECCIÓN CLAVE: Usamos el ancho y alto del mundo del Viewport,
        // que es el que sabe cuánto mide realmente la pantalla extendida.
        float anchoReal = stage.getViewport().getWorldWidth();
        float altoReal = stage.getViewport().getWorldHeight();

        fade.setSize(anchoReal, altoReal);
        fade.setPosition(0, 0); // Lo centramos en el origen del mundo

        fade.getColor().a = entrar ? 1f : 0f;

        // Aseguramos que el telón esté por encima de TODO (botones y fondos)
        fade.setTouchable(Touchable.disabled);
        stage.addActor(fade);
        fade.toFront();

        fade.addAction(Actions.sequence(
            Actions.alpha(entrar ? 0f : 1f, 0.4f),
            Actions.run(accion)
        ));
    }

    private Button crearBotonMapa(String ruta) {
        // 1. Cargar la textura
        Texture t = new Texture(Gdx.files.internal(ruta));
        t.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

        // 2. Crear el estilo (Aquí estaba el fallo, faltaba esta sección)
        ImageButton.ImageButtonStyle style = new ImageButton.ImageButtonStyle();
        style.imageUp = new TextureRegionDrawable(new TextureRegion(t));
        // Opcional: puedes añadir un color diferente cuando se presiona
        // style.pressedOffsetX = 2;
        // style.pressedOffsetY = -2;

        // 3. Crear el botón con el estilo definido
        ImageButton btn = new ImageButton(style);

        // 4. Configuración para que la animación de escala no mueva la tabla
        btn.setTransform(true);

        // El origen debe ser la mitad del size que definas en la celda (120x80 -> 60,40)
        btn.setOrigin(60, 40);

        return btn;
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void dispose() {
        stage.dispose();
        uiSkin.dispose();

        // Accedemos a la textura que está guardada dentro de cada Actor
        if (fondoBosque != null && fondoBosque.textura != null) fondoBosque.textura.dispose();
        if (fondoDesierto != null && fondoDesierto.textura != null) fondoDesierto.textura.dispose();
        if (fondoCueva != null && fondoCueva.textura != null) fondoCueva.textura.dispose();

        if (blackTexture != null) blackTexture.dispose();
    }

    @Override public void hide() {}
    @Override public void pause() {}
    @Override public void resume() {}

    private static class ImagenFondo extends Actor {
        public Texture textura;
        public ImagenFondo(Texture t) { this.textura = t; }
        public Texture getTextura() { return textura; }
        @Override public void draw(Batch batch, float alpha) {} // Dibujo manual en render
    }
}
