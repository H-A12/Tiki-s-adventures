package com.tikisadventure.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.tikisadventure.ui.CharacterPreviewActor;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.StretchViewport; // Cambiado de FitViewport
import com.tikisadventure.entities.player.CharacterFactory;
import com.tikisadventure.core.GameSession;
import com.tikisadventure.core.SaveManager;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;

public class MenuMapScreen implements Screen {
    private final Game game;
    private Stage stage;
    private Skin uiSkin;
    private SpriteBatch batch; // Necesario para el truco antiparpadeo

    // Gestión de fondos
    private Group grupoFondos;
    private ImagenFondo fondoBosque, fondoDesierto, fondoCueva;
    private int mapaActualIndex = 0;
    private final String[] nombresMapas = {"BOSQUE MUCOSO", "DESIERTO SECAROCAS", "MUSGOCUEVA"};
    private final String[] descripcionesMapas = {
        "BOSQUE MUCOSO: El amanecer de la aventura de Tiki.",
        "DESIERTO SECAROCAS: Recuerda mantenerte hidratado.",
        "MUSGOCUEVA: Todo es muy negro y húmedo aquí dentro..."
    };

    // UI y Texturas
    private Label labelTituloMapa, labelDesc;
    private TextButton btnJugar;
    private Image btnFlechaAbajo;
    private Texture texJugar, texJugarP, texTienda, texTiendaP, texVolver, texVolverP, texFlecha;

    // TRANSICIÓN
    private Texture blackScreen;
    private boolean iniciandoPantalla = true;

    private MenuGodMode godModeManager;
    private float resetTimer = 0f;

    public MenuMapScreen(Game game) {
        this.game = game;
        this.batch = new SpriteBatch();
    }

    private static class ImagenFondo extends Actor {
        private Texture textura;
        public ImagenFondo(Texture textura) {
            this.textura = textura;
            setBounds(0, 0, 800, 480);
        }
        @Override
        public void draw(Batch batch, float parentAlpha) {
            Color color = getColor();
            batch.setColor(color.r, color.g, color.b, color.a * parentAlpha);
            batch.draw(textura, getX(), getY(), getWidth(), getHeight());
            batch.setColor(Color.WHITE);
        }
    }

    @Override
    public void show() {
        // 1. Inicialización de Cámara y Stage con StretchViewport para cubrir toda la pantalla sin barras
        stage = new Stage(new StretchViewport(800, 480));
        Gdx.input.setInputProcessor(stage);
        uiSkin = new Skin(Gdx.files.internal("uiskin.json"));

        // Aseguramos que el batch para el truco antiparpadeo esté listo
        if (batch == null) batch = new SpriteBatch();

        // 2. MODO DIOS (Inicializar antes que la interfaz)
        godModeManager = new MenuGodMode(stage, uiSkin);

        // 3. CONFIGURACIÓN DE FONDOS (CARRETE VERTICAL HACIA ABAJO)
        // Los inicializamos en sus posiciones de "carrete"
        grupoFondos = new Group();
        fondoBosque = new ImagenFondo(new Texture(Gdx.files.internal("Menu/MenuMapas/fondo_bosque.png")));
        fondoDesierto = new ImagenFondo(new Texture(Gdx.files.internal("Menu/MenuMapas/fondo_desierto.png")));
        fondoCueva = new ImagenFondo(new Texture(Gdx.files.internal("Menu/MenuMapas/fondo_cueva.png")));

        fondoBosque.setPosition(0, 0);
        fondoDesierto.setPosition(0, -480); // Cambiado para movimiento hacia abajo
        fondoCueva.setPosition(0, -960);   // Cambiado para movimiento hacia abajo

        grupoFondos.addActor(fondoBosque);
        grupoFondos.addActor(fondoDesierto);
        grupoFondos.addActor(fondoCueva);
        stage.addActor(grupoFondos);

        // 4. CARGA DE ASSETS DE INTERFAZ
        texJugar = new Texture(Gdx.files.internal("Menu/ButtonPlay.png"));
        texJugarP = new Texture(Gdx.files.internal("Menu/ButtonPlayPressed.png"));
        texTienda = new Texture(Gdx.files.internal("Menu/MenuMapas/buttonTienda.png"));
        texTiendaP = new Texture(Gdx.files.internal("Menu/MenuMapas/buttonTiendaPressed.png"));
        texVolver = new Texture(Gdx.files.internal("Menu/MenuMapas/buttonVolver.png"));
        texVolverP = new Texture(Gdx.files.internal("Menu/MenuMapas/buttonVolverPressed.png"));
        texFlecha = new Texture(Gdx.files.internal("Menu/MenuMapas/flecha_down.png"));

        // 5. TEXTURA NEGRA (Para el fundido)
        if (blackScreen == null) {
            Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
            pixmap.setColor(Color.BLACK);
            pixmap.fill();
            blackScreen = new Texture(pixmap);
            pixmap.dispose();
        }

        // 6. CREAR TODA LA INTERFAZ (Botones, Personajes, etc.)
        crearInterfaz();

        // 7. GESTIÓN DE TRANSICIÓN DE ENTRADA (SOLUCIÓN AL PARPADEO CON TELÓN INMEDIATO)
        iniciandoPantalla = true;

        // Añadimos un telón negro inmediato que cubre toda la pantalla física
        final Image telonInmediato = new Image(blackScreen);
        telonInmediato.setSize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        telonInmediato.setTouchable(Touchable.disabled);
        telonInmediato.getColor().a = 1f; // Comienza negro
        stage.addActor(telonInmediato); // Se añade al final para estar encima de todo

        // Fade out rápido para revelar la pantalla
        telonInmediato.addAction(Actions.sequence(
            Actions.delay(0.1f), // Pequeño delay para asegurar cobertura
            Actions.run(new Runnable() {
                @Override
                public void run() {
                    iniciandoPantalla = false; // Permitimos que el stage se dibuje normalmente
                }
            }),
            Actions.fadeOut(0.5f), // Transición suave
            Actions.removeActor() // Eliminamos el telón
        ));

        // Lanzamos el fundido normal (opcional, pero mantenemos para consistencia)
        ejecutarFading(true, null); // Sin Runnable adicional, ya que el telón maneja la transición
    }

    private void crearInterfaz() {
        // Estilos
        TextButton.TextButtonStyle styleJugar = new TextButton.TextButtonStyle(null, null, null, uiSkin.getFont("default-font"));
        styleJugar.up = new TextureRegionDrawable(new TextureRegion(texJugar));
        styleJugar.down = new TextureRegionDrawable(new TextureRegion(texJugarP));

        TextButton.TextButtonStyle styleTienda = new TextButton.TextButtonStyle(null, null, null, uiSkin.getFont("default-font"));
        styleTienda.up = new TextureRegionDrawable(new TextureRegion(texTienda));
        styleTienda.down = new TextureRegionDrawable(new TextureRegion(texTiendaP));

        TextButton.TextButtonStyle styleVolver = new TextButton.TextButtonStyle(null, null, null, uiSkin.getFont("default-font"));
        styleVolver.up = new TextureRegionDrawable(new TextureRegion(texVolver));
        styleVolver.down = new TextureRegionDrawable(new TextureRegion(texVolverP));

        // TEXTOS IZQUIERDA
        labelTituloMapa = new Label(nombresMapas[0], uiSkin);
        labelTituloMapa.setFontScale(1.5f);
        labelTituloMapa.setColor(Color.RED);
        labelTituloMapa.setPosition(40, 420);
        stage.addActor(labelTituloMapa);

        labelDesc = new Label(descripcionesMapas[0], uiSkin);
        labelDesc.setWrap(true);
        labelDesc.setAlignment(Align.topLeft);
        labelDesc.setSize(240, 100);
        labelDesc.setPosition(40, 320);
        stage.addActor(labelDesc);

        // BOTONES
        btnJugar = new TextButton("", styleJugar);
        btnJugar.setSize(200, 100);
        btnJugar.setPosition(800 - btnJugar.getWidth() - 20, 20);
        btnJugar.setTransform(true);
        btnJugar.setOrigin(Align.center);
        stage.addActor(btnJugar);

        btnFlechaAbajo = new Image(texFlecha);
        btnFlechaAbajo.setSize(60, 40);
        btnFlechaAbajo.setPosition((800 - 60) / 2f, 20);
        btnFlechaAbajo.setOrigin(Align.center);
        stage.addActor(btnFlechaAbajo);

        TextButton btnTienda = new TextButton("", styleTienda);
        btnTienda.setSize(90, 90);
        btnTienda.setPosition(800 - btnTienda.getWidth() - 20, 180);
        btnTienda.setTransform(true);
        btnTienda.setOrigin(Align.center);
        stage.addActor(btnTienda);

        TextButton btnVolver = new TextButton("", styleVolver);
        btnVolver.setSize(100, 45);
        btnVolver.setPosition(800 - 130, 420);
        stage.addActor(btnVolver);

        // MODO DIOS
        Table tableGod = new Table();
        tableGod.setPosition(100, 100);
        godModeManager.inyectarInterfaz(tableGod);
        stage.addActor(tableGod);

        // PERSONAJES (DEBajo DE LA DESCRIPCIÓN, centro izquierda)
        Table charTable = new Table();
        final ButtonGroup<Button> group = new ButtonGroup<>();
        JsonValue characterData = new JsonReader().parse(Gdx.files.internal("data/player_config.json"));
        int charIndex = 1;

        for (JsonValue charEntry : characterData.get("characters")) {
            final String id = charEntry.getString("id");
            final boolean isUnlocked = SaveManager.isCharacterUnlocked(charIndex);
            Animation<TextureRegion> idleAnim = CharacterFactory.getCharacterIdleAnimation(id);
            final Button btn = new Button(uiSkin);

            if (!isUnlocked) {
                Image staticImage = new Image(idleAnim.getKeyFrame(0f));
                staticImage.setColor(Color.BLACK);
                btn.add(staticImage).size(40, 40);
            } else {
                btn.add(new CharacterPreviewActor(idleAnim)).size(40, 40);
            }

            btn.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    if (!isUnlocked) return;
                    MenuCharacter modal = new MenuCharacter("", uiSkin, id, idleAnim, () -> {
                        btn.setChecked(true);
                        actualizarColoresPersonajes(group);
                        GameSession.selectedCharacterId = id;
                    });
                    modal.setPosition((stage.getWidth()-modal.getWidth())/2, (stage.getHeight()-modal.getHeight())/2);
                    stage.addActor(modal);
                }
            });
            group.add(btn);
            charTable.add(btn).size(55, 55).pad(5);
            charIndex++;
        }
        charTable.pack();
        charTable.setPosition(40, 200); // Cambiado para estar debajo de la descripción, centro izquierda
        stage.addActor(charTable);
        actualizarColoresPersonajes(group);

        // LISTENERS
        btnFlechaAbajo.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                cambiarSiguienteMapa();
            }
        });

        configurarListenerBoton(btnJugar, () -> {
            btnJugar.setDisabled(true);
            ejecutarFading(false, () -> game.setScreen(new GameScreen(game)));
        });

        configurarListenerBoton(btnVolver, () -> {
            btnVolver.setDisabled(true);
            ejecutarFading(false, () -> game.setScreen(new MenuScreen(game)));
        });

        configurarListenerBoton(btnTienda, () -> {
            ShopScreen shop = new ShopScreen(uiSkin, null);
            shop.setPosition((800-shop.getWidth())/2, (480-shop.getHeight())/2);
            stage.addActor(shop);
        });

        // Inicializar interfaz para el mapa actual (bosque por defecto)
        actualizarInterfazMapa(0);
    }

    private void ejecutarFading(boolean entrar, final Runnable accionAlTerminar) {
        final Image fadeOverlay = new Image(blackScreen);
        // Usamos el ancho y alto del stage para que sea consistente
        fadeOverlay.setSize(stage.getWidth(), stage.getHeight());
        fadeOverlay.setPosition(0, 0);

        if (entrar) {
            fadeOverlay.setTouchable(Touchable.disabled);
            fadeOverlay.getColor().a = 1f; // Empieza negro
        } else {
            fadeOverlay.setTouchable(Touchable.enabled);
            fadeOverlay.getColor().a = 0f; // Empieza invisible
        }

        float alphaDestino = entrar ? 0f : 1f;

        fadeOverlay.addAction(Actions.sequence(
            Actions.alpha(alphaDestino, 0.5f),
            Actions.run(new Runnable() {
                @Override
                public void run() {
                    if (accionAlTerminar != null) accionAlTerminar.run();
                    if (entrar) fadeOverlay.remove();
                }
            })
        ));

        stage.addActor(fadeOverlay); // Se añade al final, por lo que está arriba
    }

    // Nuevo método para actualizar textos y estado del botón según el mapa
    private void actualizarInterfazMapa(int index) {
        String clave = (index == 1) ? "desierto" : (index == 2) ? "cueva" : "bosque";
        boolean isUnlocked = SaveManager.isMapUnlocked(clave);
        labelDesc.setText(isUnlocked ? descripcionesMapas[index] : "???");
        btnJugar.setDisabled(!isUnlocked);
        GameSession.selectedMapName = clave;
    }

    private void cambiarSiguienteMapa() {
        int anteriorIndex = mapaActualIndex;
        mapaActualIndex = (mapaActualIndex + 1) % 3;
        float duracion = 0.6f;

        // Fade out del fondo anterior
        getFondo(anteriorIndex).addAction(Actions.alpha(0, duracion));

        // Movimiento del grupo de fondos hacia abajo
        if (anteriorIndex == 2 && mapaActualIndex == 0) {
            // Loop de cueva a bosque: reposicionar para efecto continuo hacia abajo
            fondoBosque.setPosition(0, -1440); // Posición relativa para aparecer desde arriba
            grupoFondos.addAction(Actions.sequence(
                Actions.moveTo(0, 1440, duracion, Interpolation.exp5Out),
                Actions.run(() -> {
                    // Resetear posiciones después de la animación
                    grupoFondos.setPosition(0, 0);
                    fondoBosque.setPosition(0, 0);
                })
            ));
        } else {
            // Movimiento normal hacia abajo
            grupoFondos.addAction(Actions.moveTo(0, mapaActualIndex * 480, duracion, Interpolation.exp5Out));
        }

        // Fade in del fondo entrante
        getFondo(mapaActualIndex).addAction(Actions.alpha(1, duracion));

        // Actualizar textos y estado
        labelTituloMapa.setText(nombresMapas[mapaActualIndex]);
        actualizarInterfazMapa(mapaActualIndex);

        btnFlechaAbajo.addAction(Actions.sequence(Actions.scaleTo(1.2f, 0.8f, 0.1f), Actions.scaleTo(1f, 1f, 0.1f)));
    }

    // Nuevo método para obtener el fondo por índice
    private ImagenFondo getFondo(int index) {
        switch (index) {
            case 0: return fondoBosque;
            case 1: return fondoDesierto;
            case 2: return fondoCueva;
            default: return null;
        }
    }

    private void actualizarColoresPersonajes(ButtonGroup<Button> group) {
        int i = 1;
        for (Button b : group.getButtons()) {
            if (!SaveManager.isCharacterUnlocked(i)) b.setColor(Color.DARK_GRAY);
            else b.setColor(b.isChecked() ? Color.WHITE : Color.GRAY);
            i++;
        }
    }

    private void configurarListenerBoton(final Button btn, final Runnable accion) {
        btn.addListener(new ClickListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                btn.addAction(Actions.scaleTo(0.92f, 0.92f, 0.1f));
                return super.touchDown(event, x, y, pointer, button);
            }
            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                btn.addAction(Actions.scaleTo(1f, 1f, 0.1f));
                super.touchUp(event, x, y, pointer, button);
            }
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (!btn.isDisabled()) accion.run();
            }
        });
    }

    @Override
    public void render(float delta) {
        // 1. Limpiamos a negro absoluto
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // 3. Actuamos y dibujamos el Stage (el telón negro cubre inicialmente)
        stage.act(delta);
        stage.draw();

        // 4. Lógica de Reset (opcional)
        if (Gdx.input.isKeyPressed(com.badlogic.gdx.Input.Keys.R)) {
            resetTimer += delta;
            if (resetTimer >= 1.0f) {
                SaveManager.getProfileData().totalScore = 0;
                SaveManager.saveProfileData();
                game.setScreen(new MenuMapScreen(game));
            }
        } else resetTimer = 0f;
    }

    @Override public void resize(int width, int height) { stage.getViewport().update(width, height, true); }
    @Override public void hide() { dispose(); }
    @Override public void pause() {}
    @Override public void resume() {}

    @Override
    public void dispose() {
        if (stage != null) stage.dispose();
        if (uiSkin != null) uiSkin.dispose();
        if (batch != null) batch.dispose();
        if (fondoBosque != null) fondoBosque.textura.dispose();
        if (fondoDesierto != null) fondoDesierto.textura.dispose();
        if (fondoCueva != null) fondoCueva.textura.dispose();
        if (blackScreen != null) blackScreen.dispose();
        Texture[] texs = {texJugar, texJugarP, texTienda, texTiendaP, texVolver, texVolverP, texFlecha};
        for(Texture t : texs) if(t != null) t.dispose();
    }
}
