package com.tikisadventure.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.tikisadventure.ui.CharacterPreviewActor;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.tikisadventure.entities.player.CharacterFactory;
import com.tikisadventure.core.GameSession;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;

public class MenuMapScreen implements Screen {

    private final Game game;
    private Stage stage;
    private Skin uiSkin;

    // Gestion de fondos y actores
    private Group grupoFondos;
    private ImagenFondo fondoBosque;
    private ImagenFondo fondoDesierto;
    private ImagenFondo fondoCueva;
    private ImagenFondo fondoMostradoActualmente;

    //Elementos actualizados dinámicamente
    private Label labelDesc;
    private TextButton btnJugar;

    //Gestor del menu ModoDios
    private MenuGodMode godModeManager;

    public MenuMapScreen(Game game) {
        this.game = game;
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
        stage = new Stage(new FitViewport(800, 480));
        Gdx.input.setInputProcessor(stage);

        uiSkin = new Skin(Gdx.files.internal("uiskin.json"));

        // Inicializamos nuestro gestor del Modo Dios pasándole los recursos
        godModeManager = new MenuGodMode(stage, uiSkin);

        // Inicializar fondos
        grupoFondos = new Group();
        fondoBosque = new ImagenFondo(new Texture(Gdx.files.internal("Menu/fondo_bosque.png")));
        fondoDesierto = new ImagenFondo(new Texture(Gdx.files.internal("Menu/fondo_desierto.png")));
        fondoCueva = new ImagenFondo(new Texture(Gdx.files.internal("Menu/fondo_cueva.png")));

        grupoFondos.addActor(fondoBosque);
        grupoFondos.addActor(fondoDesierto);
        grupoFondos.addActor(fondoCueva);

        fondoDesierto.getColor().a = 0;
        fondoCueva.getColor().a = 0;
        fondoMostradoActualmente = fondoBosque;

        stage.addActor(grupoFondos);

        // Configurar UI
        SelectBox.SelectBoxStyle estiloFijo = new SelectBox.SelectBoxStyle(uiSkin.get(SelectBox.SelectBoxStyle.class));
        estiloFijo.backgroundOpen = estiloFijo.background;
        estiloFijo.backgroundOver = estiloFijo.background;
        if(estiloFijo.overFontColor != null) estiloFijo.overFontColor = estiloFijo.fontColor;

        Table mainTable = new Table();
        mainTable.setFillParent(true);
        mainTable.pad(20);

        Table columnaIzquierda = new Table();

        final SelectBox<String> selectorMapas = new SelectBox<>(estiloFijo);
        selectorMapas.setItems("Bosque Mucoso", "Desierto Secarocas (X)", "Musgocueva (X)");

        labelDesc = new Label("BOSQUE MUCOSO: El amanecer de la aventura de Tiki.", uiSkin);
        labelDesc.setWrap(true);
        labelDesc.setAlignment(com.badlogic.gdx.utils.Align.topLeft);

        columnaIzquierda.add(new Label("MAPAS", uiSkin)).padBottom(10).row();
        columnaIzquierda.add(selectorMapas).width(220).height(40).padBottom(20).row();
        columnaIzquierda.add(labelDesc).width(220).height(150).top();

        // Llamamos al gestor del MenuGodMode.java para que inyecte su UI en la columna izquierda
        godModeManager.inyectarInterfaz(columnaIzquierda);

        btnJugar = new TextButton("¡EMPEZAR!", uiSkin);
        TextButton btnTienda = new TextButton("TIENDA", uiSkin);

        // Seleccion de personaje
        Table charTable = new Table();
        final ButtonGroup<Button> group = new ButtonGroup<>();

        JsonValue characterData = new JsonReader().parse(Gdx.files.internal("data/player_config.json"));
        for (JsonValue charEntry : characterData.get("characters")) {
            final String id = charEntry.getString("id");

            Animation<TextureRegion> idleAnim = CharacterFactory.getCharacterIdleAnimation(id);
            Button btn = new Button(uiSkin);
            btn.add(new CharacterPreviewActor(idleAnim)).size(48, 48);

            btn.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    if (btn.isChecked()) {
                        GameSession.selectedCharacterId = id;
                        for (Button b : group.getButtons()) {
                            b.setColor(b.isChecked() ? Color.WHITE : new Color(0.5f, 0.5f, 0.5f, 1f));
                        }
                    }
                }
            });
            group.add(btn);
            charTable.add(btn).size(64, 64).pad(10);
        }

        group.getButtons().first().setChecked(true);
        for (Button b : group.getButtons()) {
            b.setColor(b.isChecked() ? Color.WHITE : new Color(0.5f, 0.5f, 0.5f, 1f));
        }

        mainTable.row();
        mainTable.add(charTable).colspan(3).padTop(20);
        mainTable.row();

        mainTable.add(columnaIzquierda).expand().left();
        mainTable.add(btnJugar).size(180, 80).expand().center();
        mainTable.add(btnTienda).size(120, 120).expand().right();
        mainTable.row();

        TextButton btnVolver = new TextButton("Volver", uiSkin);
        mainTable.add(btnVolver).colspan(3).right().padTop(20);

        // Cambio de fondo difuminado
        selectorMapas.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                String seleccionado = selectorMapas.getSelected();
                ImagenFondo fondoSiguiente;

                if (seleccionado.contains("Bosque")) {
                    labelDesc.setText("BOSQUE MUCOSO: El amanecer de la aventura de Tiki.");
                    btnJugar.setDisabled(false);
                    btnJugar.setColor(Color.WHITE);
                    fondoSiguiente = fondoBosque;
                } else if (seleccionado.contains("Desierto")) {
                    labelDesc.setText("??? (Desierto)");
                    btnJugar.setDisabled(true);
                    btnJugar.setColor(0.5f, 0.5f, 0.5f, 0.5f);
                    fondoSiguiente = fondoDesierto;
                } else {
                    labelDesc.setText("??? (Cueva)");
                    btnJugar.setDisabled(true);
                    btnJugar.setColor(0.5f, 0.5f, 0.5f, 0.5f);
                    fondoSiguiente = fondoCueva;
                }

                if (fondoSiguiente != fondoMostradoActualmente) {
                    float duracion = 0.4f;
                    fondoSiguiente.getColor().a = 0;
                    fondoSiguiente.toBack();
                    fondoSiguiente.addAction(Actions.fadeIn(duracion));
                    fondoMostradoActualmente.addAction(Actions.fadeOut(duracion));
                    fondoMostradoActualmente = fondoSiguiente;
                }
            }
        });

        btnJugar.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (!btnJugar.isDisabled()) {
                    game.setScreen(new GameScreen(game));
                }
            }
        });

        btnVolver.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new MenuScreen(game));
            }
        });

        stage.addActor(mainTable);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.act(delta);
        stage.draw();
    }

    @Override public void resize(int width, int height) { stage.getViewport().update(width, height, true); }
    @Override public void hide() { dispose(); }
    @Override public void pause() {}
    @Override public void resume() {}

    @Override
    public void dispose() {
        if (stage != null) stage.dispose();
        if (uiSkin != null) uiSkin.dispose();
        if (fondoBosque != null) fondoBosque.textura.dispose();
        if (fondoDesierto != null) fondoDesierto.textura.dispose();
        if (fondoCueva != null) fondoCueva.textura.dispose();
    }
}
