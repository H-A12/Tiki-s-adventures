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

    private Texture texJugar, texJugarP, texTienda, texTiendaP, texVolver, texVolverP;
    private void crearTablaInterfaz() {

        Table mainTable = new Table();
        mainTable.setFillParent(true);
        mainTable.padTop(60);

        // --- CARGA DE ASSETS Y ESTILOS ---
        texJugar = new Texture(Gdx.files.internal("Menu/ButtonPlay.png"));
        texJugarP = new Texture(Gdx.files.internal("Menu/ButtonPlayPressed.png"));
        texTienda = new Texture(Gdx.files.internal("Menu/MenuMapas/buttonTienda.png"));
        texTiendaP = new Texture(Gdx.files.internal("Menu/MenuMapas/buttonTiendaPressed.png"));
        texVolver = new Texture(Gdx.files.internal("Menu/MenuMapas/buttonVolver.png"));
        texVolverP = new Texture(Gdx.files.internal("Menu/MenuMapas/buttonVolverPressed.png"));

        Texture[] todas = {texJugar, texJugarP, texTienda, texTiendaP, texVolver, texVolverP};
        for(Texture t : todas) t.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

        TextButton.TextButtonStyle styleJugar = new TextButton.TextButtonStyle();
        styleJugar.up = new TextureRegionDrawable(new TextureRegion(texJugar));
        styleJugar.down = new TextureRegionDrawable(new TextureRegion(texJugarP));
        styleJugar.font = uiSkin.getFont("default-font");

        TextButton.TextButtonStyle styleTienda = new TextButton.TextButtonStyle();
        styleTienda.up = new TextureRegionDrawable(new TextureRegion(texTienda));
        styleTienda.down = new TextureRegionDrawable(new TextureRegion(texTiendaP));
        styleTienda.font = uiSkin.getFont("default-font");

        TextButton.TextButtonStyle styleVolver = new TextButton.TextButtonStyle();
        styleVolver.up = new TextureRegionDrawable(new TextureRegion(texVolver));
        styleVolver.down = new TextureRegionDrawable(new TextureRegion(texVolverP));
        styleVolver.font = uiSkin.getFont("default-font");

        // --- COLUMNA IZQUIERDA (SOLO MAPAS + MODO DIOS) ---
        Table tablaLateralIzquierda = new Table();
        final Button btnBosque = crearBotonMapa("Menu/MenuMapas/icon_bosque.png");
        final Button btnDesierto = crearBotonMapa("Menu/MenuMapas/icon_desierto.png");
        final Button btnCueva = crearBotonMapa("Menu/MenuMapas/icon_cueva.png");

        if (!SaveManager.isMapUnlocked("desierto")) { btnDesierto.setDisabled(true); btnDesierto.setColor(0.3f, 0.3f, 0.3f, 1f); }
        if (!SaveManager.isMapUnlocked("cueva")) { btnCueva.setDisabled(true); btnCueva.setColor(0.3f, 0.3f, 0.3f, 1f); }

        new ButtonGroup<>(btnBosque, btnDesierto, btnCueva);
        btnBosque.setChecked(true);
        btnBosque.setScale(1.2f);

        labelDesc = new Label("BOSQUE: Peligros y tesoros ocultos.", uiSkin);
        labelDesc.setWrap(true);
        labelDesc.setAlignment(Align.center);

        tablaLateralIzquierda.add(new Label("SELECCIONA MAPA", uiSkin)).padBottom(10).center().row();
        tablaLateralIzquierda.add(btnBosque).size(110, 60).padBottom(5).center().row();
        tablaLateralIzquierda.add(btnDesierto).size(110, 60).padBottom(5).center().row();
        tablaLateralIzquierda.add(btnCueva).size(110, 60).padBottom(8).center().row();
        tablaLateralIzquierda.add(labelDesc).width(200).height(50).padBottom(10).center().row();

        godModeManager.inyectarInterfaz(tablaLateralIzquierda);

        // --- BOTONES PRINCIPALES ---
        btnJugar = new TextButton("", styleJugar);
        btnJugar.setTransform(true);
        TextButton btnTienda = new TextButton("", styleTienda);
        btnTienda.setTransform(true);
        TextButton btnVolver = new TextButton("", styleVolver);
        btnVolver.setTransform(true);

        // --- LISTENERS (USANDO EL HELPER) ---
        configurarListenerEscala(btnJugar, () -> {
            if (!btnJugar.isDisabled()) {
                btnJugar.setTouchable(Touchable.disabled);
                ejecutarFading(false, () -> game.setScreen(new GameScreen(game)));
            }
        });
        configurarListenerEscala(btnTienda, () -> { /* game.setScreen(new TiendaScreen(game)); */ });
        configurarListenerEscala(btnVolver, () -> ejecutarFading(false, () -> game.setScreen(new MenuScreen(game))));

        // Listeners de Mapas (para el cambio de fondo)
        btnBosque.addListener(new ChangeListener() { @Override public void changed(ChangeEvent e, Actor a) { if (btnBosque.isChecked()) { actualizarSeleccion(fondoBosque, "BOSQUE: Peligros."); btnBosque.addAction(Actions.scaleTo(1.2f, 1.2f, 0.15f)); } else btnBosque.addAction(Actions.scaleTo(1f, 1f, 0.15f)); } });
        btnDesierto.addListener(new ChangeListener() { @Override public void changed(ChangeEvent e, Actor a) { if (btnDesierto.isChecked() && !btnDesierto.isDisabled()) { actualizarSeleccion(fondoDesierto, "DESIERTO."); btnDesierto.addAction(Actions.scaleTo(1.2f, 1.2f, 0.15f)); } else { if (btnDesierto.isChecked()) labelDesc.setText("DESIERTO: BLOQUEADO."); btnDesierto.addAction(Actions.scaleTo(1f, 1f, 0.15f)); } } });
        btnCueva.addListener(new ChangeListener() { @Override public void changed(ChangeEvent e, Actor a) { if (btnCueva.isChecked() && !btnCueva.isDisabled()) { actualizarSeleccion(fondoCueva, "CUEVA."); btnCueva.addAction(Actions.scaleTo(1.2f, 1.2f, 0.15f)); } else { if (btnCueva.isChecked()) labelDesc.setText("CUEVA: BLOQUEADO."); btnCueva.addAction(Actions.scaleTo(1f, 1f, 0.15f)); } } });

        // --- ORGANIZACIÓN TABLA PRINCIPAL ---
        mainTable.add(tablaLateralIzquierda).width(250).padLeft(40).top();
        // Añadimos padRight de 250 para compensar el ancho de la columna izquierda y centrar el botón jugar
        mainTable.add(btnJugar).size(200, 110).expandX().center().padRight(250);
        stage.addActor(mainTable);

        // --- SECCIÓN INFERIOR: PERSONAJES ---
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
                Image staticImg = new Image(idleAnim.getKeyFrame(0f)); staticImg.setColor(Color.BLACK); btnChar.add(staticImg).size(35, 35);
            } else {
                btnChar.add(new CharacterPreviewActor(idleAnim)).size(35, 35);
            }
            btnChar.addListener(new ClickListener() {
                @Override public void clicked(InputEvent event, float x, float y) {
                    if (!isUnlocked) return;
                    MenuCharacter modal = new MenuCharacter("", uiSkin, id, idleAnim, () -> {
                        btnChar.setChecked(true); actualizarColoresPersonajes(groupChars); GameSession.selectedCharacterId = id;
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

        // --- POSICIONAMIENTO ABSOLUTO (DERECHA) ---
        float anchoMundo = stage.getViewport().getWorldWidth();
        float altoMundo = stage.getViewport().getWorldHeight();

        // 1. Volver: Arriba a la Derecha
        btnVolver.setSize(50, 50);
        btnVolver.setPosition(anchoMundo - btnVolver.getWidth() - 25, altoMundo - 75);
        stage.addActor(btnVolver);

        // 2. Tienda: Abajo a la Derecha
        btnTienda.setSize(160, 70);
        btnTienda.setPosition(anchoMundo - btnTienda.getWidth() - 30, 30);
        stage.addActor(btnTienda);

        // 3. Personajes: Abajo Centro
        charTable.pack();
        charTable.setPosition((anchoMundo - charTable.getWidth()) / 2f, 30);
        stage.addActor(charTable);
    }

    /**
     * Helper para configurar la animación de escala y el click en un solo paso
     */
    private void configurarListenerEscala(final TextButton btn, final Runnable accion) {
        btn.addListener(new ClickListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                btn.setOrigin(Align.center);
                btn.clearActions();
                btn.addAction(Actions.scaleTo(0.9f, 0.9f, 0.1f));
                return super.touchDown(event, x, y, pointer, button);
            }
            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                btn.clearActions();
                btn.addAction(Actions.scaleTo(1f, 1f, 0.1f));
                super.touchUp(event, x, y, pointer, button);
            }
            @Override
            public void clicked(InputEvent event, float x, float y) {
                accion.run();
            }
        });
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
        stage.dispose();
        uiSkin.dispose();
        if (fondoBosque != null) fondoBosque.textura.dispose();
        if (fondoDesierto != null) fondoDesierto.textura.dispose();
        if (fondoCueva != null) fondoCueva.textura.dispose();
        if (blackTexture != null) blackTexture.dispose();
        Texture[] aLiberar = {texJugar, texJugarP, texTienda, texTiendaP, texVolver, texVolverP, blackTexture};
        for(Texture t : aLiberar) if(t != null) t.dispose();
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
