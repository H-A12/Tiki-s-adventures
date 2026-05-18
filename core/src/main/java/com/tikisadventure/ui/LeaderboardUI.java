package com.tikisadventure.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.tikisadventure.core.Assets;
import com.tikisadventure.database.core.AuthCallback;
import com.tikisadventure.database.progress.ProgressRepository;
import com.tikisadventure.ui.button.ButtonFactory;

public class LeaderboardUI extends Window {

    private Stage stage;
    private Skin skin;

    private Table tabsTable;
    private Table contentTable;
    private Table listTable;

    private Button btnBosque;
    private Button btnDesierto;
    private Button btnCastillo;

    private TextureRegionDrawable darkBg;
    private TextureRegionDrawable blackBg;
    private Button.ButtonStyle yellowBtnStyle;
    private ScrollPane scrollPane;
    private boolean focusSet = false;

    public LeaderboardUI(Skin skin, Stage stage) {
        super("", skin);
        this.skin = skin;
        this.stage = stage;

        Image bgImage = new Image(new Texture(Gdx.files.internal("Menu/VentanaTikyranking.png")));
        setBackground(bgImage.getDrawable());

        setModal(true);
        setMovable(false);
        setResizable(false);
        pad(55, 50, 40, 50);

        setSize(700, 720);
        setPosition(Math.round((stage.getWidth() - getWidth()) / 2f), Math.round((stage.getHeight() - getHeight()) / 2f));

        Pixmap pmDark = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pmDark.setColor(0.15f, 0.15f, 0.15f, 0.85f);
        pmDark.fill();
        darkBg = new TextureRegionDrawable(new TextureRegion(new Texture(pmDark)));
        pmDark.dispose();

        Pixmap pmBlack = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pmBlack.setColor(Color.BLACK);
        pmBlack.fill();
        blackBg = new TextureRegionDrawable(new TextureRegion(new Texture(pmBlack)));
        pmBlack.dispose();

        TextureRegionDrawable entryBg = new TextureRegionDrawable(new TextureRegion(ButtonFactory.getBotonAlargadoTex()));

        yellowBtnStyle = new Button.ButtonStyle();
        yellowBtnStyle.up = entryBg;
        yellowBtnStyle.over = entryBg;

        tabsTable = new Table();
        contentTable = new Table();
        listTable = new Table();
        listTable.top();

        Pixmap pmScrollBg = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pmScrollBg.setColor(0.3f, 0.3f, 0.1f, 0.5f);
        pmScrollBg.fill();
        TextureRegionDrawable scrollBg = new TextureRegionDrawable(new TextureRegion(new Texture(pmScrollBg)));
        pmScrollBg.dispose();

        Pixmap pmScrollKnob = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pmScrollKnob.setColor(1f, 0.9f, 0.1f, 1f);
        pmScrollKnob.fill();
        TextureRegionDrawable scrollKnob = new TextureRegionDrawable(new TextureRegion(new Texture(pmScrollKnob)));
        pmScrollKnob.dispose();

        scrollBg.setMinWidth(12);
        scrollKnob.setMinWidth(12);
        ScrollPane.ScrollPaneStyle scrollStyle = new ScrollPane.ScrollPaneStyle();
        scrollStyle.vScroll = scrollBg;
        scrollStyle.vScrollKnob = scrollKnob;

        scrollPane = new ScrollPane(listTable, scrollStyle);
        scrollPane.setFadeScrollBars(false);
        scrollPane.setScrollingDisabled(true, false);

        add(tabsTable).fillX().padTop(20).padBottom(10).row();
        add(contentTable).fillX().padBottom(5).row();
        add(scrollPane).expand().fill().padBottom(25).row();

        TextButton btnCerrar = ButtonFactory.createTextButton("Cerrar", () -> {
            stage.setScrollFocus(null);
            addAction(Actions.sequence(
                Actions.fadeOut(0.2f),
                Actions.removeActor()
            ));
        });
        add(btnCerrar).padTop(15).padBottom(15).width(110);

        construirTabs();
    }

    public void show() {
        focusSet = false;
        stage.addActor(this);
        setTransform(true);
        setOrigin(com.badlogic.gdx.utils.Align.center);
        setScale(stage.getWidth() / 1333f);
        setPosition(Math.round((stage.getWidth() - getWidth()) / 2f), Math.round((stage.getHeight() - getHeight()) / 2f));
        setColor(1, 1, 1, 0);
        addAction(Actions.fadeIn(0.2f));
        cargarDatos("bosque", btnBosque, "TOP 50: BOSQUE", Color.GREEN);
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        if (!focusSet && getStage() != null) {
            getStage().setScrollFocus(scrollPane);
            focusSet = true;
        }
    }

    private void construirTabs() {
        tabsTable.clearChildren();

        Button.ButtonStyle tabBtnStyle = new Button.ButtonStyle();
        TextureRegionDrawable tabBg = new TextureRegionDrawable(new TextureRegion(ButtonFactory.getBotonAlargadoTex()));
        tabBtnStyle.up = tabBg;
        tabBtnStyle.down = tabBg;
        tabBtnStyle.over = tabBg;
        tabBtnStyle.checked = tabBg;

        btnBosque = new Button(tabBtnStyle);
        btnDesierto = new Button(tabBtnStyle);
        btnCastillo = new Button(tabBtnStyle);

        TextureRegion texBosque = Assets.getRegion("shared", "UI_assets/ForestMatchIcon");
        TextureRegion texDesierto = Assets.getRegion("shared", "UI_assets/DesertMatchIcon");
        TextureRegion texCastillo = Assets.getRegion("shared", "UI_assets/CastilloMatchIcon");

        if (texBosque != null) {
            Image img = new Image(texBosque);
            img.setScaling(com.badlogic.gdx.utils.Scaling.fit);
            btnBosque.add(img).expand().fill().pad(2);
        }
        if (texDesierto != null) {
            Image img = new Image(texDesierto);
            img.setScaling(com.badlogic.gdx.utils.Scaling.fit);
            btnDesierto.add(img).expand().fill().pad(2);
        }
        if (texCastillo != null) {
            Image img = new Image(texCastillo);
            img.setScaling(com.badlogic.gdx.utils.Scaling.fit);
            btnCastillo.add(img).expand().fill().pad(2);
        }

        tabsTable.add(btnBosque).size(180, 52).padRight(14);
        tabsTable.add(btnDesierto).size(180, 52).padRight(14);
        tabsTable.add(btnCastillo).size(180, 52);

        ButtonFactory.configure(btnBosque, () -> cargarDatos("bosque", btnBosque, "TOP 50: BOSQUE", Color.GREEN));
        ButtonFactory.configure(btnDesierto, () -> cargarDatos("desierto", btnDesierto, "TOP 50: DESIERTO", Color.YELLOW));
        ButtonFactory.configure(btnCastillo, () -> cargarDatos("castillo", btnCastillo, "TOP 50: CASTILLO", Color.PURPLE));
    }

    private void cargarDatos(final String mapId, Button botonActivo, final String mapName, final Color colorName) {
        resaltarPestaña(botonActivo);
        contentTable.clearChildren();
        listTable.clearChildren();

        Label titulo = new Label(mapName, skin, "font-14");
        titulo.setColor(colorName);
        Table titleWrap = new Table();
        titleWrap.setBackground(darkBg);
        titleWrap.add(titulo).pad(10);
        contentTable.add(titleWrap).center();

        listTable.add(new Label("Cargando base de datos...", skin, "font-14")).center().pad(50);

        new ProgressRepository().obtenerLeaderboard(mapId, new AuthCallback() {
            @Override
            public void onSuccess(String responseString) {
                JsonReader reader = new JsonReader();
                final JsonValue matches = reader.parse(responseString);

                Gdx.app.postRunnable(new Runnable() {
                    @Override
                    public void run() {
                        listTable.clearChildren();
                        if (matches.size == 0) {
                            listTable.add(new Label("Aún no hay partidas en este mapa.", skin, "font-14")).pad(20);
                            return;
                        }

                        int rank = 1;
                        for (JsonValue match : matches) {
                            listTable.add(crearBotonPartida(match, rank)).fillX().expandX().padBottom(10).row();
                            rank++;
                        }
                    }
                });
            }

            @Override
            public void onError(final String errorMessage) {
                Gdx.app.postRunnable(new Runnable() {
                    @Override
                    public void run() {
                        listTable.clearChildren();
                        listTable.add(new Label("Error de red: " + errorMessage, skin, "font-13")).center();
                    }
                });
            }
        });
    }

    private void resaltarPestaña(Button pestañaActiva) {
        Color inactivo = Color.GRAY;
        Color activo = Color.WHITE;

        if (btnBosque != null) btnBosque.setColor(inactivo);
        if (btnDesierto != null) btnDesierto.setColor(inactivo);
        if (btnCastillo != null) btnCastillo.setColor(inactivo);

        if (pestañaActiva != null) {
            pestañaActiva.setColor(activo);
        }
    }

    private Actor crearBotonPartida(final JsonValue matchData, int rank) {
        Button.ButtonStyle btnStyle = new Button.ButtonStyle();
        btnStyle.up = yellowBtnStyle.up;
        btnStyle.over = yellowBtnStyle.over;
        Button btn = new Button(btnStyle);
        btn.padTop(10).padBottom(10).padLeft(5).padRight(10);

        // --- EXTRACCIÓN DE DATOS ---
        String playerName = matchData.get("jugador") != null ? matchData.get("jugador").getString("name", "Desconocido") : "Desconocido";
        String charName = matchData.get("personaje") != null ? matchData.get("personaje").getString("name", "tiki") : "tiki";
        String gadgetId = matchData.get("gadget") != null ? matchData.get("gadget").getString("string_id", "grenade_kinetic") : "grenade_kinetic";

        long score = matchData.getLong("score");
        long stage = matchData.getLong("stage");
        long wave = matchData.getLong("wave");
        long kills = matchData.has("total_killed") ? matchData.getLong("total_killed") : 0;

        // --- ESTRUCTURA DEL BOTÓN ---
        // 1. Número de ranking
        Label rankLabel = new Label("#" + rank, skin, "font-14");
        if(rank == 1) rankLabel.setColor(Color.GOLD);
        else if(rank == 2) rankLabel.setColor(Color.LIGHT_GRAY);
        else if(rank == 3) rankLabel.setColor(Color.CORAL);
        btn.add(rankLabel).padRight(10).width(40);

        // 2. Columna Izquierda: Nombre del jugador y Sprites
        Table leftCol = new Table();
        Label nameLabel = new Label(playerName.toUpperCase(), skin, "font-13");
        nameLabel.setWrap(true);
        nameLabel.setColor(Color.CYAN);
        leftCol.add(nameLabel).left().padBottom(4).width(110).row();

        Table spriteRow = new Table();
        // 2a. Sprite Animado (Personaje)
        String rutaSprite = "player_assets/" + charName.toLowerCase() + "/idle";
        TextureRegion atlasRegion = Assets.getRegion("shared", rutaSprite);
        Animation<TextureRegion> charAnim = null;
        if (atlasRegion != null) {
            int frameSize = 16;
            int frameCount = atlasRegion.getRegionWidth() / frameSize;
            TextureRegion[] frames = new TextureRegion[frameCount];
            for (int i = 0; i < frameCount; i++) {
                frames[i] = new TextureRegion(atlasRegion, i * frameSize, 0, frameSize, frameSize);
            }
            charAnim = new Animation<>(0.15f, frames);
            charAnim.setPlayMode(Animation.PlayMode.LOOP);
            spriteRow.add(new AnimatedImage(charAnim)).size(35, 35).padRight(10);
        }

        // 2b. Icono del Gadget
        String gadgetTexturePath = "weapons_assets/Corn";
        if (gadgetId.contains("explosive")) gadgetTexturePath = "weapons_assets/ShakedCola";
        else if (gadgetId.contains("fire")) gadgetTexturePath = "weapons_assets/Jalapeno";
        else if (gadgetId.contains("freeze")) gadgetTexturePath = "weapons_assets/IceCandy";
        else if (gadgetId.contains("cactus")) gadgetTexturePath = "weapons_assets/Sock";
        else if (gadgetId.contains("sewer")) gadgetTexturePath = "weapons_assets/Sewer";
        else if (gadgetId.contains("sheel")) gadgetTexturePath = "weapons_assets/MagicSheel";
        else if (gadgetId.contains("scarecrow")) gadgetTexturePath = "weapons_assets/Scarecrow";
        else if (gadgetId.contains("turret")) gadgetTexturePath = "weapons_assets/Turret";
        else if (gadgetId.contains("dash")) gadgetTexturePath = "UI_assets/DashIcon";

        TextureRegion gadgetRegion = Assets.getRegion("shared", gadgetTexturePath);
        if (gadgetRegion != null) {
            spriteRow.add(new Image(gadgetRegion)).size(25, 25);
        }

        leftCol.add(spriteRow).left();
        btn.add(leftCol).expandX().left().padRight(10);

        // --- NUEVA TABLA HORIZONTAL PARA ESTADÍSTICAS ---
        Table statsTable = new Table();
        statsTable.defaults().align(Align.center); // Alinea todo verticalmente al centro por defecto

        // 1. Puntos
        Label scoreLabel = new Label(score + " pts.", skin, "font-13");
        scoreLabel.setColor(Color.YELLOW);
        statsTable.add(scoreLabel).padRight(16);

        // 2. Kills + Calavera
        Label killsLabel = new Label(String.valueOf(kills), skin, "font-13");
        statsTable.add(killsLabel).padRight(6);
        TextureRegion skullReg = Assets.getRegion("shared", "UI_assets/skull");
        if (skullReg != null) {
            // Tamaño subido a 24x24
            statsTable.add(new Image(skullReg)).size(24, 24).padRight(16);
        }

        // 3. Stage-Wave + Espada
        Label stageLabel = new Label(stage + "-" + wave, skin, "font-13");
        statsTable.add(stageLabel).padRight(6);
        TextureRegion swordReg = Assets.getRegion("shared", "UI_assets/sword");
        if (swordReg != null) {
            // Tamaño subido a 24x24
            statsTable.add(new Image(swordReg)).size(24, 24);
        }

        // padRight(40) empuja todas las stats hacia la izquierda alejándolas del borde del botón
        btn.add(statsTable).expandX().right().padRight(20);

        // --- LISTENER ---
        btn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                new MatchDetailsUI(skin, getStage(), matchData).show();
            }
        });

        btn.addListener(new Assets.HoverCursorListener());
        btn.addListener(new ClickListener() {
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                btn.setColor(0.65f, 0.65f, 0.65f, 1f);
            }
            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                btn.setColor(Color.WHITE);
            }
        });

        return btn;
    }
}
