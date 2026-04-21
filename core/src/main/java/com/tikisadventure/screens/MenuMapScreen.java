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
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.tikisadventure.core.GameSession;
import com.tikisadventure.core.SaveManager;
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

    // Nuevos gestores de la versión alternativa
    private MenuGodMode godModeManager;
    private float resetTimer = 0f;

    public MenuMapScreen(Game game) {
        this.game = game;
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.BLACK);
        pixmap.fill();
        this.blackTexture = new Texture(pixmap);
        pixmap.dispose();
    }

    @Override
    public void show() {
        stage = new Stage(new ExtendViewport(800, 480));
        Gdx.input.setInputProcessor(stage);
        uiSkin = new Skin(Gdx.files.internal("uiskin.json"));

        // Inicializamos el gestor de Modo Dios
        godModeManager = new MenuGodMode(stage, uiSkin);

        grupoFondos = new Group();
        fondoBosque = new ImagenFondo(new Texture(Gdx.files.internal("Menu/MenuMapas/fondo_bosque.png")));
        fondoDesierto = new ImagenFondo(new Texture(Gdx.files.internal("Menu/MenuMapas/fondo_desierto.png")));
        fondoCueva = new ImagenFondo(new Texture(Gdx.files.internal("Menu/MenuMapas/fondo_cueva.png")));

        fondoBosque.getColor().a = 1f;
        fondoDesierto.getColor().a = 0f;
        fondoCueva.getColor().a = 0f;

        grupoFondos.addActor(fondoBosque);
        grupoFondos.addActor(fondoDesierto);
        grupoFondos.addActor(fondoCueva);
        stage.addActor(grupoFondos);

        fondoMostradoActualmente = fondoBosque;

        crearTablaInterfaz();

        // Telón elástico de entrada
        final Image telonIn = new Image(blackTexture);
        telonIn.setSize(stage.getViewport().getWorldWidth(), stage.getViewport().getWorldHeight());
        telonIn.setPosition(0, 0);
        telonIn.setTouchable(Touchable.disabled);
        stage.addActor(telonIn);
        telonIn.toFront();
        telonIn.addAction(Actions.sequence(Actions.fadeOut(0.6f), Actions.removeActor()));
    }

    @Override
    public void render(float delta) {
        // Lógica de Reset (Developer)
        if (Gdx.input.isKeyPressed(com.badlogic.gdx.Input.Keys.R)) {
            resetTimer += delta;
            if (resetTimer >= 1.0f) {
                SaveManager.getProfileData().globalScore = 0;
                SaveManager.saveProfileData();
                game.setScreen(new MenuMapScreen(game));
                return;
            }
        } else { resetTimer = 0f; }

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Dibujo manual del fondo estirado
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

        stage.getViewport().apply();
        stage.getBatch().setProjectionMatrix(stage.getCamera().combined);
        stage.act(delta);
        stage.draw();
    }

    private void actualizarSeleccion(ImagenFondo siguiente, String desc) {
        labelDesc.setText(desc);
        // El botón jugar se habilita siempre que no sea un texto de "Bloqueado"
        boolean bloqueado = desc.contains("Bloqueado") || desc.contains("BLOQUEADO");
        btnJugar.setDisabled(bloqueado);
        btnJugar.setColor(bloqueado ? new Color(1, 1, 1, 0.5f) : Color.WHITE);

        if (siguiente != fondoMostradoActualmente) {
            siguiente.toFront();
            siguiente.clearActions();
            fondoMostradoActualmente.clearActions();
            siguiente.addAction(Actions.fadeIn(0.5f));
            fondoMostradoActualmente.addAction(Actions.fadeOut(0.5f));
            fondoMostradoActualmente = siguiente;
        }
    }

    private void crearTablaInterfaz() {
        // 1. Limpiamos la tabla principal y configuramos el espacio superior
        Table mainTable = new Table();
        mainTable.setFillParent(true);
        mainTable.padTop(60); // Ajusta este valor para bajar el texto "SELECCIONA MAPA"

        // --- SECCIÓN IZQUIERDA: MAPAS Y MODO DIOS ---
        Table tablaMapas = new Table();
        final Button btnBosque = crearBotonMapa("Menu/MenuMapas/icon_bosque.png");
        final Button btnDesierto = crearBotonMapa("Menu/MenuMapas/icon_desierto.png");
        final Button btnCueva = crearBotonMapa("Menu/MenuMapas/icon_cueva.png");

        // Lógica de desbloqueo según SaveManager
        if (!SaveManager.isMapUnlocked("desierto")) {
            btnDesierto.setDisabled(true);
            btnDesierto.setColor(0.3f, 0.3f, 0.3f, 1f);
        }
        if (!SaveManager.isMapUnlocked("cueva")) {
            btnCueva.setDisabled(true);
            btnCueva.setColor(0.3f, 0.3f, 0.3f, 1f);
        }

        new ButtonGroup<>(btnBosque, btnDesierto, btnCueva);
        btnBosque.setChecked(true);
        btnBosque.setScale(1.2f);

        labelDesc = new Label("BOSQUE: Peligros y tesoros ocultos.", uiSkin);
        labelDesc.setWrap(true);
        labelDesc.setAlignment(com.badlogic.gdx.utils.Align.center);

        // Añadimos elementos a la columna de mapas (tamaños compactos para ganar espacio)
        tablaMapas.add(new Label("SELECCIONA MAPA", uiSkin)).padBottom(10).center().row();
        tablaMapas.add(btnBosque).size(110, 60).padBottom(5).center().row();
        tablaMapas.add(btnDesierto).size(110, 60).padBottom(5).center().row();
        tablaMapas.add(btnCueva).size(110, 60).padBottom(8).center().row();
        tablaMapas.add(labelDesc).width(200).height(50).padBottom(10).center().row();

        // Inyectamos el Modo Dios debajo de la descripción
        godModeManager.inyectarInterfaz(tablaMapas);

        // --- BOTONES CENTRALES Y DERECHOS ---
        btnJugar = new TextButton("¡EMPEZAR!", uiSkin);
        TextButton btnTienda = new TextButton("TIENDA", uiSkin);
        TextButton btnVolver = new TextButton("Volver", uiSkin);

        // Listeners de Mapas (Mantenemos tu lógica de escalas y fondos)
        btnBosque.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (btnBosque.isChecked()) {
                    actualizarSeleccion(fondoBosque, "BOSQUE: Peligros y tesoros.");
                    GameSession.selectedMapName = "bosque";
                    btnBosque.addAction(Actions.scaleTo(1.2f, 1.2f, 0.15f));
                } else btnBosque.addAction(Actions.scaleTo(1f, 1f, 0.15f));
            }
        });

        btnDesierto.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (btnDesierto.isChecked() && !btnDesierto.isDisabled()) {
                    actualizarSeleccion(fondoDesierto, "DESIERTO: Calor extremo.");
                    GameSession.selectedMapName = "desierto";
                    btnDesierto.addAction(Actions.scaleTo(1.2f, 1.2f, 0.15f));
                } else {
                    if (btnDesierto.isChecked()) labelDesc.setText("DESIERTO: BLOQUEADO.");
                    btnDesierto.addAction(Actions.scaleTo(1f, 1f, 0.15f));
                }
            }
        });

        btnCueva.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (btnCueva.isChecked() && !btnCueva.isDisabled()) {
                    actualizarSeleccion(fondoCueva, "CUEVA: Oscuridad total.");
                    GameSession.selectedMapName = "cueva";
                    btnCueva.addAction(Actions.scaleTo(1.2f, 1.2f, 0.15f));
                } else {
                    if (btnCueva.isChecked()) labelDesc.setText("CUEVA: BLOQUEADO.");
                    btnCueva.addAction(Actions.scaleTo(1f, 1f, 0.15f));
                }
            }
        });

        // Listeners de navegación
        btnJugar.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (!btnJugar.isDisabled()) ejecutarFading(false, () -> game.setScreen(new GameScreen(game)));
            }
        });

        btnVolver.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                ejecutarFading(false, () -> game.setScreen(new MenuScreen(game)));
            }
        });

        // Añadimos a la tabla principal el bloque superior
        mainTable.add(tablaMapas).width(250).padLeft(40).top();
        mainTable.add(btnJugar).size(180, 80).expandX().center();
        mainTable.add(btnTienda).size(90, 90).padRight(40).center();

        stage.addActor(mainTable);

        // --- SECCIÓN INFERIOR: PERSONAJES (Posicionamiento absoluto para que no se corten) ---
        Table charTable = new Table();
        final ButtonGroup<Button> groupChars = new ButtonGroup<>();
        JsonValue characterData = new JsonReader().parse(Gdx.files.internal("data/player_config.json"));
        int charIndex = 1;

        for (JsonValue charEntry : characterData.get("characters")) {
            final String id = charEntry.getString("id");
            final boolean isUnlocked = SaveManager.isCharacterUnlocked(charIndex);
            final Animation<TextureRegion> idleAnim = CharacterFactory.getCharacterIdleAnimation(id);

            final Button btnChar = new Button(uiSkin);
            btnChar.setTransform(true);
            btnChar.setOrigin(25, 25);

            if (!isUnlocked) {
                Image staticImg = new Image(idleAnim.getKeyFrame(0f));
                staticImg.setColor(Color.BLACK);
                btnChar.add(staticImg).size(35, 35);
            } else {
                btnChar.add(new CharacterPreviewActor(idleAnim)).size(35, 35);
            }

            btnChar.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    if (!isUnlocked) return;
                    MenuCharacter modal = new MenuCharacter("", uiSkin, id, idleAnim, () -> {
                        btnChar.setChecked(true);
                        actualizarColoresPersonajes(groupChars);
                        GameSession.selectedCharacterId = id;
                    });
                    modal.setPosition((stage.getWidth()-modal.getWidth())/2, (stage.getHeight()-modal.getHeight())/2);
                    stage.addActor(modal);
                }
            });
            groupChars.add(btnChar);
            charTable.add(btnChar).size(50, 50).pad(5);
            charIndex++;
        }
        actualizarColoresPersonajes(groupChars);

        // --- AJUSTE FINAL DE POSICIONES (A ras de suelo) ---
        float yBase = 30; // Altura para Personajes y Volver

        // Configurar Volver
        btnVolver.setSize(110, 45);
        btnVolver.setPosition(stage.getViewport().getWorldWidth() - 140, yBase);
        stage.addActor(btnVolver);

        // Configurar Fila de Personajes
        charTable.pack(); // Ajusta el tamaño de la tabla a su contenido
        charTable.setPosition(
            (stage.getViewport().getWorldWidth() - charTable.getWidth()) / 2f,
            yBase
        );
        stage.addActor(charTable);
    }

    private void actualizarColoresPersonajes(ButtonGroup<Button> group) {
        int i = 1;
        for (Button b : group.getButtons()) {
            if (!SaveManager.isCharacterUnlocked(i)) b.setColor(Color.BLACK);
            else b.setColor(b.isChecked() ? Color.WHITE : new Color(0.5f, 0.5f, 0.5f, 1f));
            i++;
        }
    }

    private void ejecutarFading(boolean entrar, final Runnable accion) {
        final Image fade = new Image(blackTexture);
        fade.setSize(stage.getViewport().getWorldWidth(), stage.getViewport().getWorldHeight());
        fade.setPosition(0, 0);
        fade.getColor().a = entrar ? 1f : 0f;
        fade.setTouchable(Touchable.disabled);
        stage.addActor(fade);
        fade.toFront();
        fade.addAction(Actions.sequence(Actions.alpha(entrar ? 0f : 1f, 0.4f), Actions.run(accion)));
    }

    private Button crearBotonMapa(String ruta) {
        Texture t = new Texture(Gdx.files.internal(ruta));
        t.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        ImageButton.ImageButtonStyle style = new ImageButton.ImageButtonStyle();
        style.imageUp = new TextureRegionDrawable(new TextureRegion(t));
        ImageButton btn = new ImageButton(style);
        btn.setTransform(true);
        btn.setOrigin(55, 35); // Mitad de 110, 70
        return btn;
    }

    @Override public void resize(int width, int height) { stage.getViewport().update(width, height, true); }

    @Override public void dispose() {
        stage.dispose(); uiSkin.dispose();
        if (fondoBosque != null) fondoBosque.textura.dispose();
        if (fondoDesierto != null) fondoDesierto.textura.dispose();
        if (fondoCueva != null) fondoCueva.textura.dispose();
        if (blackTexture != null) blackTexture.dispose();
    }

    @Override public void hide() {}
    @Override public void pause() {}
    @Override public void resume() {}

    private static class ImagenFondo extends Actor {
        public Texture textura;
        public ImagenFondo(Texture t) { this.textura = t; }
        @Override public void draw(Batch batch, float alpha) {}
    }
}
