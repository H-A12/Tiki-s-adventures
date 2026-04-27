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
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.tikisadventure.entities.player.CharacterFactory;
import com.tikisadventure.core.GameSession;
import com.tikisadventure.core.SaveManager;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.tikisadventure.core.Assets;

public class MenuMapScreen implements Screen {
    private final Game game;
    private Stage stage;
    private Skin uiSkin;
    private SpriteBatch batch;

    private Group grupoFondos;
    private ImagenFondo fondoBosque, fondoDesierto, fondoCueva;
    private int mapaActualIndex = 0;
    private final String[] nombresMapas = {"BOSQUE MUCOSO", "DESIERTO SECAROCAS", "MUSGOCUEVA"};
    private final String[] descripcionesMapas = {
        "BOSQUE MUCOSO: El amanecer de la aventura de Tiki.",
        "DESIERTO SECAROCAS: Recuerda mantenerte hidratado.",
        "MUSGOCUEVA: Todo es muy negro y húmedo aquí dentro..."
    };

    private Label labelTituloMapa, labelDesc;
    private TextButton btnJugar;
    private Image btnFlechaAbajo;
    private Texture texJugar, texTienda, texVolver, texFlecha;

    private Texture blackScreen;
    private boolean iniciandoPantalla = true;

    private MenuGodMode godModeManager;
    private float resetTimer = 0f;

    // --- VARIABLES PARA EL GADGET VISUAL ---
    private Button btnEquippedGadget;
    private Image equippedGadgetImage;

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
        stage = new Stage(new StretchViewport(800, 480));
        Gdx.input.setInputProcessor(stage);
        uiSkin = new Skin(Gdx.files.internal("uiskin.json"));

        if (batch == null) batch = new SpriteBatch();

        godModeManager = new MenuGodMode(stage, uiSkin);

        grupoFondos = new Group();
        fondoBosque = new ImagenFondo(new Texture(Gdx.files.internal("Menu/MenuMapas/fondo_bosque.png")));
        fondoDesierto = new ImagenFondo(new Texture(Gdx.files.internal("Menu/MenuMapas/fondo_desierto.png")));
        fondoCueva = new ImagenFondo(new Texture(Gdx.files.internal("Menu/MenuMapas/fondo_cueva.png")));

        fondoBosque.setPosition(0, 0);
        fondoDesierto.setPosition(0, -480);
        fondoCueva.setPosition(0, -960);

        grupoFondos.addActor(fondoBosque);
        grupoFondos.addActor(fondoDesierto);
        grupoFondos.addActor(fondoCueva);
        stage.addActor(grupoFondos);

        texJugar = new Texture(Gdx.files.internal("Menu/ButtonPlay.png"));
        texTienda = new Texture(Gdx.files.internal("Menu/MenuMapas/buttonTienda.png"));
        texVolver = new Texture(Gdx.files.internal("Menu/MenuMapas/buttonVolver.png"));
        texFlecha = new Texture(Gdx.files.internal("Menu/MenuMapas/flecha_down.png"));

        if (blackScreen == null) {
            Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
            pixmap.setColor(Color.BLACK);
            pixmap.fill();
            blackScreen = new Texture(pixmap);
            pixmap.dispose();
        }

        crearInterfaz();

        iniciandoPantalla = true;
        final Image telonInmediato = new Image(blackScreen);
        telonInmediato.setSize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        telonInmediato.setTouchable(Touchable.disabled);
        telonInmediato.getColor().a = 1f;
        stage.addActor(telonInmediato);

        telonInmediato.addAction(Actions.sequence(
            Actions.delay(0.1f),
            Actions.run(new Runnable() {
                @Override
                public void run() {
                    iniciandoPantalla = false;
                }
            }),
            Actions.fadeOut(0.5f),
            Actions.removeActor()
        ));

        ejecutarFading(true, null);
    }

    private void crearInterfaz() {
        TextButton.TextButtonStyle styleJugar = new TextButton.TextButtonStyle(null, null, null, uiSkin.getFont("default-font"));
        styleJugar.up = new TextureRegionDrawable(new TextureRegion(texJugar));

        TextButton.TextButtonStyle styleTienda = new TextButton.TextButtonStyle(null, null, null, uiSkin.getFont("default-font"));
        styleTienda.up = new TextureRegionDrawable(new TextureRegion(texTienda));

        TextButton.TextButtonStyle styleVolver = new TextButton.TextButtonStyle(null, null, null, uiSkin.getFont("default-font"));
        styleVolver.up = new TextureRegionDrawable(new TextureRegion(texVolver));

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
        btnVolver.setSize(45, 45);
        btnVolver.setPosition(800 - 60, 420);
        btnVolver.setTransform(true);
        btnVolver.setOrigin(Align.center);
        stage.addActor(btnVolver);


        // --- MODO DIOS ---
        Table tableGod = new Table();
        tableGod.setPosition(100, 80);
        godModeManager.inyectarInterfaz(tableGod);
        stage.addActor(tableGod);

        // --- PERSONAJES ---
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
        charTable.setPosition(40, 200);
        stage.addActor(charTable);
        actualizarColoresPersonajes(group);

        // --- BOTÓN VISUAL DE GADGET ---
        // Se coloca en Y=135, exactamente debajo de los personajes (Y=200) y encima de Modo Dios (Y=100)
        btnEquippedGadget = new Button(uiSkin);
        equippedGadgetImage = new Image();

        // NUEVO: Aseguramos que la imagen se adapte sin deformarse
        equippedGadgetImage.setScaling(com.badlogic.gdx.utils.Scaling.fit);

        // NUEVO: Le damos un tamaño interior más pequeño (35x35) para que respire dentro de los bordes
        btnEquippedGadget.add(equippedGadgetImage).size(30, 30).center();

        // Mismo tamaño interior que personajes
        btnEquippedGadget.setSize(40, 40); // Mismo tamaño exterior que personajes (55x55)
        btnEquippedGadget.setPosition(45, 150);

        updateEquippedGadgetIcon();

        btnEquippedGadget.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                mostrarSelectorGadgets();
            }
        });

        stage.addActor(btnEquippedGadget);

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

        final Runnable actualizarTiendaCallback = new Runnable() {
            @Override
            public void run() {
                updateEquippedGadgetIcon();
            }
        };

        configurarListenerBoton(btnTienda, () -> {
            ShopScreen shop = new ShopScreen(uiSkin, actualizarTiendaCallback);
            shop.setPosition((800-shop.getWidth())/2, (480-shop.getHeight())/2);
            stage.addActor(shop);
        });

        actualizarInterfazMapa(0);
    }

    private TextureRegion getGadgetIcon(String gadgetId) {
        try {
            JsonValue abilitiesData = new JsonReader().parse(Gdx.files.internal("data/abilities_config.json"));
            JsonValue def = abilitiesData.get(gadgetId);
            if (def != null && def.has("effects")) {
                JsonValue effects = def.get("effects");
                for (int i = 0; i < effects.size; i++) {
                    JsonValue eff = effects.get(i);
                    if ("THROW".equals(eff.getString("type"))) {
                        String spriteName = eff.get("params").getString("sprite");
                        TextureRegion region = Assets.getRegion("shared", spriteName);
                        if (region != null) return region;
                        if (spriteName.startsWith("weapons_assets/")) {
                            return Assets.getRegion("shared", spriteName.replace("weapons_assets/", ""));
                        }
                    }
                }
            }
        } catch (Exception e) {
            Gdx.app.error("MenuMapScreen", "No se pudo leer el icono del gadget " + gadgetId, e);
        }
        return Assets.getRegion("shared", "UI_assets/UI_Crosshair");
    }

    private void updateEquippedGadgetIcon() {
        String currentId = SaveManager.getEquippedGadget();
        if (currentId == null || currentId.isEmpty()) currentId = "grenade_kinetic";

        TextureRegion icon = getGadgetIcon(currentId);
        if (icon != null) {
            equippedGadgetImage.setDrawable(new TextureRegionDrawable(icon));
        }
    }

    private void mostrarSelectorGadgets() {
        final Window modal = new Window("Seleccionar Gadget", uiSkin);
        modal.setModal(true);
        modal.setMovable(false);

        Table grid = new Table();
        Array<String> availableGadgets = new Array<>();

        availableGadgets.add("grenade_kinetic");
        if (SaveManager.isCharacterUnlocked(2)) availableGadgets.add("grenade_explosive");
        if (SaveManager.isCharacterUnlocked(3)) availableGadgets.add("grenade_fire");
        if (SaveManager.isGadgetOwned("grenade_freeze")) availableGadgets.add("grenade_freeze");

        int col = 0;
        String equipped = SaveManager.getEquippedGadget();
        if (equipped == null || equipped.isEmpty()) equipped = "grenade_kinetic";

        for (final String id : availableGadgets) {
            Button btn = new Button(uiSkin);
            Image img = new Image(getGadgetIcon(id));
            btn.add(img).size(40, 40);

            if (id.equals(equipped)) {
                btn.setChecked(true);
            }

            btn.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    SaveManager.setEquippedGadget(id);
                    updateEquippedGadgetIcon();
                    modal.remove();
                }
            });

            grid.add(btn).size(55, 55).pad(10);
            col++;
            if (col >= 3) {
                grid.row();
                col = 0;
            }
        }

        TextButton btnCerrar = new TextButton("Cerrar", uiSkin);
        btnCerrar.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                modal.remove();
            }
        });

        modal.add(grid).pad(15).row();
        modal.add(btnCerrar).padTop(10).padBottom(10).width(120);
        modal.pack();

        modal.setPosition(Math.round((stage.getWidth() - modal.getWidth()) / 2f),
            Math.round((stage.getHeight() - modal.getHeight()) / 2f));
        stage.addActor(modal);
    }

    private void ejecutarFading(boolean entrar, final Runnable accionAlTerminar) {
        final Image fadeOverlay = new Image(blackScreen);
        fadeOverlay.setSize(stage.getWidth(), stage.getHeight());
        fadeOverlay.setPosition(0, 0);

        if (entrar) {
            fadeOverlay.setTouchable(Touchable.disabled);
            fadeOverlay.getColor().a = 1f;
        } else {
            fadeOverlay.setTouchable(Touchable.enabled);
            fadeOverlay.getColor().a = 0f;
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

        stage.addActor(fadeOverlay);
    }

    private void actualizarInterfazMapa(int index) {
        String clave = (index == 1) ? "desierto" : (index == 2) ? "cueva" : "bosque";
        boolean isUnlocked = SaveManager.isMapUnlocked(clave);
        labelDesc.setText(isUnlocked ? descripcionesMapas[index] : "???");
        btnJugar.setDisabled(!isUnlocked);
        GameSession.selectedMapName = clave;
    }

    private boolean animando = false;
    private void cambiarSiguienteMapa() {

        grupoFondos.clearActions();
        getFondo(mapaActualIndex).clearActions();
        getFondo((mapaActualIndex + 1) % 3).clearActions();
        getFondo((mapaActualIndex + 2) % 3).clearActions();

        int anteriorIndex = mapaActualIndex;
        mapaActualIndex = (mapaActualIndex + 1) % 3;
        float duracion = animando ? 0.2f : 0.6f; // Duración más corta si ya está animando
        animando = true;

        getFondo(anteriorIndex).addAction(Actions.alpha(0, duracion));

        if (anteriorIndex == 2 && mapaActualIndex == 0) {
            fondoBosque.setPosition(0, -1440);
            grupoFondos.addAction(Actions.sequence(
                Actions.moveTo(0, 1440, duracion, Interpolation.exp5Out),
                Actions.run(() -> {
                    grupoFondos.setPosition(0, 0);
                    fondoBosque.setPosition(0, 0);
                    animando = false; // Fin de la animación
                })
            ));
        } else {
            grupoFondos.addAction(Actions.sequence(
                Actions.moveTo(0, mapaActualIndex * 480, duracion, Interpolation.exp5Out),
                Actions.run(() -> animando = false) // Fin de la animación
            ));
        }

        getFondo(mapaActualIndex).addAction(Actions.alpha(1, duracion));

        labelTituloMapa.setText(nombresMapas[mapaActualIndex]);
        actualizarInterfazMapa(mapaActualIndex);

        btnFlechaAbajo.addAction(Actions.sequence(Actions.scaleTo(1.2f, 0.8f, 0.1f), Actions.scaleTo(1f, 1f, 0.1f)));
    }


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
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                if (!btn.isPressed()) {
                    btn.setColor(Color.LIGHT_GRAY); // Oscurece ligeramente para hover
                    btn.addAction(Actions.scaleTo(1.05f, 1.05f, 0.1f));
                }
            }
            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                if (!btn.isPressed()) {
                    btn.setColor(Color.WHITE); // Restaura al normal
                    btn.addAction(Actions.scaleTo(1f, 1f, 0.1f));
                }
            }
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                btn.setColor(Color.GRAY); // Menos oscuro que DARK_GRAY
                btn.addAction(Actions.scaleTo(0.92f, 0.92f, 0.1f));
                return super.touchDown(event, x, y, pointer, button);
            }
            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                btn.setColor(Color.WHITE); // Restaura
                btn.addAction(Actions.scaleTo(1f, 1f, 0.1f));

            }
        });
    }


    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        stage.act(delta);
        stage.draw();

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
        Texture[] texs = {texJugar, texTienda, texVolver, texFlecha};
        for(Texture t : texs) if(t != null) t.dispose();
    }
}
