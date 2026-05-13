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

public class HistoryUI extends Window {

    private Stage stage;
    private Skin skin;
    private String username;

    private Table tabsTable;
    private Table contentTable;
    private Table listTable;
    private ScrollPane scrollPane;

    private Array<JsonValue> allMatches;

    private Button btnRecientes;
    private Button btnMejores;
    private Button btnHazanas;

    private TextureRegionDrawable darkBg;
    private TextureRegionDrawable blackBg;
    private Button.ButtonStyle entryBtnStyle;

    public HistoryUI(Skin skin, Stage stage, String username) {
        super("", skin);
        this.skin = skin;
        this.stage = stage;
        this.username = username;

        Image bgImage = new Image(new Texture(Gdx.files.internal("Menu/VentanaHistorial.png")));
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

        TextureRegionDrawable entryBg = new TextureRegionDrawable(new TextureRegion(new Texture(Gdx.files.internal("Menu/BotonAlargado.png"))));

        entryBtnStyle = new Button.ButtonStyle();
        entryBtnStyle.up = entryBg;
        entryBtnStyle.over = entryBg;

        tabsTable = new Table();
        contentTable = new Table();
        listTable = new Table();
        listTable.top();

        Pixmap pmScrollBg = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pmScrollBg.setColor(0.1f, 0.3f, 0.4f, 0.5f);
        pmScrollBg.fill();
        TextureRegionDrawable scrollBg = new TextureRegionDrawable(new TextureRegion(new Texture(pmScrollBg)));
        pmScrollBg.dispose();

        Pixmap pmScrollKnob = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pmScrollKnob.setColor(0.2f, 0.7f, 0.8f, 1f);
        pmScrollKnob.fill();
        TextureRegionDrawable scrollKnob = new TextureRegionDrawable(new TextureRegion(new Texture(pmScrollKnob)));
        pmScrollKnob.dispose();

        ScrollPane.ScrollPaneStyle scrollStyle = new ScrollPane.ScrollPaneStyle();
        scrollStyle.vScroll = scrollBg;
        scrollStyle.vScrollKnob = scrollKnob;

        scrollPane = new ScrollPane(listTable, scrollStyle);
        scrollPane.setFadeScrollBars(false);
        scrollPane.setScrollingDisabled(true, false);

        add(tabsTable).fillX().padTop(5).padBottom(10).row();
        add(contentTable).fillX().padBottom(5).row();
        add(scrollPane).expand().fill().row();

        TextButton.TextButtonStyle cerrarStyle = new TextButton.TextButtonStyle();
        cerrarStyle.up = new TextureRegionDrawable(new TextureRegion(new Texture(Gdx.files.internal("Menu/BotonText.png"))));
        cerrarStyle.font = skin.get("font-14", Label.LabelStyle.class).font;
        TextButton btnCerrar = new TextButton("Cerrar", cerrarStyle);
        btnCerrar.addListener(new Assets.HoverCursorListener());
        btnCerrar.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                stage.setScrollFocus(null);
                addAction(Actions.sequence(
                    Actions.fadeOut(0.2f),
                    Actions.removeActor()
                ));
            }
        });
        add(btnCerrar).padTop(15).width(110);

        listTable.add(new Label("Cargando base de datos...", skin, "font-14")).center().pad(50);
    }

    public void show() {
        stage.addActor(this);
        setTransform(true);
        setOrigin(com.badlogic.gdx.utils.Align.center);
        setScale(stage.getWidth() / 1333f);
        setPosition(Math.round((stage.getWidth() - getWidth()) / 2f), Math.round((stage.getHeight() - getHeight()) / 2f));
        setColor(1, 1, 1, 0);
        addAction(Actions.fadeIn(0.2f));
        stage.setScrollFocus(scrollPane);
        listTable.clearChildren();
        listTable.add(new Label("Cargando historial...", skin, "font-14")).center().pad(50);
        cargarDatos();
    }

    private void cargarDatos() {
        new ProgressRepository().obtenerHistorial(username, new AuthCallback() {
            @Override
            public void onSuccess(String responseString) {
                JsonReader reader = new JsonReader();
                JsonValue matches = reader.parse(responseString);

                allMatches = new Array<>();
                for (JsonValue match : matches) {
                    allMatches.add(match);
                }

                Gdx.app.postRunnable(new Runnable() {
                    @Override
                    public void run() {
                        construirTabs();
                        mostrarRecientes();
                    }
                });
            }

            @Override
            public void onError(String errorMessage) {
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

    private void construirTabs() {
        tabsTable.clearChildren();

        btnRecientes = new Button(skin);
        btnMejores = new Button(skin);
        btnHazanas = new Button(skin);

        TextureRegion texRecientes = Assets.getRegion("shared", "UI_assets/LastMatches");
        TextureRegion texMejores = Assets.getRegion("shared", "UI_assets/BestMatches");
        TextureRegion texHazanas = Assets.getRegion("shared", "UI_assets/Feats");

        if (texRecientes != null) {
            Image img = new Image(texRecientes);
            img.setScaling(com.badlogic.gdx.utils.Scaling.fit);
            btnRecientes.add(img).expand().fill().pad(2);
        }
        if (texMejores != null) {
            Image img = new Image(texMejores);
            img.setScaling(com.badlogic.gdx.utils.Scaling.fit);
            btnMejores.add(img).expand().fill().pad(2);
        }
        if (texHazanas != null) {
            Image img = new Image(texHazanas);
            img.setScaling(com.badlogic.gdx.utils.Scaling.fit);
            btnHazanas.add(img).expand().fill().pad(2);
        }

        tabsTable.add(btnRecientes).size(180, 52).padRight(14);
        tabsTable.add(btnMejores).size(180, 52).padRight(14);
        tabsTable.add(btnHazanas).size(180, 52);

        btnRecientes.addListener(new Assets.HoverCursorListener());
        btnRecientes.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) { mostrarRecientes(); }
        });
        btnMejores.addListener(new Assets.HoverCursorListener());
        btnMejores.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) { mostrarMejores(); }
        });
        btnHazanas.addListener(new Assets.HoverCursorListener());
        btnHazanas.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) { mostrarHazanas(); }
        });
    }

    private void resaltarPestaña(Button pestañaActiva) {
        Color inactivo = Color.GRAY;
        Color activo = Color.WHITE;

        if (btnRecientes != null) btnRecientes.setColor(inactivo);
        if (btnMejores != null) btnMejores.setColor(inactivo);
        if (btnHazanas != null) btnHazanas.setColor(inactivo);

        if (pestañaActiva != null) {
            pestañaActiva.setColor(activo);
        }
    }

    private void mostrarRecientes() {
        resaltarPestaña(btnRecientes);
        contentTable.clearChildren();
        listTable.clearChildren();

        Label titulo = new Label("ÚLTIMAS PARTIDAS", skin, "font-14");
        titulo.setColor(Color.GREEN);
        Table titleWrap = new Table();
        titleWrap.setBackground(darkBg);
        titleWrap.add(titulo).pad(10);
        contentTable.add(titleWrap).center();

        if (allMatches.size == 0) {
            listTable.add(new Label("No hay partidas.", skin, "font-14")).pad(20);
            return;
        }

        int max = Math.min(10, allMatches.size);
        for (int i = 0; i < max; i++) {
            listTable.add(crearBotonPartida(allMatches.get(i))).fillX().expandX().padBottom(10).row();
        }
    }

    private void mostrarMejores() {
        resaltarPestaña(btnMejores);
        contentTable.clearChildren();
        listTable.clearChildren();

        Label titulo = new Label("MEJORES PARTIDAS", skin, "font-14");
        titulo.setColor(Color.YELLOW);
        Table titleWrap = new Table();
        titleWrap.setBackground(darkBg);
        titleWrap.add(titulo).pad(10);
        contentTable.add(titleWrap).center();

        if (allMatches.size == 0) {
            listTable.add(new Label("No hay partidas.", skin, "font-14")).pad(20);
            return;
        }

        Array<JsonValue> sortedMatches = new Array<>(allMatches);
        sortedMatches.sort(new java.util.Comparator<JsonValue>() {
            @Override
            public int compare(JsonValue a, JsonValue b) {
                return Long.compare(b.getLong("score"), a.getLong("score"));
            }
        });

        int max = Math.min(10, sortedMatches.size);
        for (int i = 0; i < max; i++) {
            listTable.add(crearBotonPartida(sortedMatches.get(i))).fillX().expandX().padBottom(10).row();
        }
    }

    private void mostrarHazanas() {
        resaltarPestaña(btnHazanas);
        contentTable.clearChildren();
        listTable.clearChildren();

        Label titulo = new Label("MÉRITOS", skin, "font-14");
        titulo.setColor(Color.SKY);
        Table titleWrap = new Table();
        titleWrap.setBackground(darkBg);
        titleWrap.add(titulo).pad(10);
        contentTable.add(titleWrap).center();

        if (allMatches.size == 0) {
            listTable.add(new Label("No hay partidas.", skin, "font-14")).pad(20);
            return;
        }

        JsonValue maxScore = allMatches.get(0);
        JsonValue maxKills = allMatches.get(0);
        JsonValue maxWave = allMatches.get(0);

        for (JsonValue m : allMatches) {
            if (m.getLong("score") > maxScore.getLong("score")) maxScore = m;
            if (m.getLong("total_killed") > maxKills.getLong("total_killed")) maxKills = m;

            if (m.getLong("stage") > maxWave.getLong("stage") ||
                (m.getLong("stage") == maxWave.getLong("stage") && m.getLong("wave") > maxWave.getLong("wave"))) {
                maxWave = m;
            }
        }

        listTable.add(new Label("Mayor Puntuación:", skin, "font-14")).left().padBottom(5).row();
        listTable.add(crearBotonPartida(maxScore)).fillX().expandX().padBottom(20).row();

        listTable.add(new Label("Más Enemigos Eliminados:", skin, "font-14")).left().padBottom(5).row();
        listTable.add(crearBotonPartida(maxKills)).fillX().expandX().padBottom(20).row();

        listTable.add(new Label("Más Lejos Llegado (Nivel/Oleada):", skin, "font-14")).left().padBottom(5).row();
        listTable.add(crearBotonPartida(maxWave)).fillX().expandX().padBottom(20).row();
    }

    private Actor crearBotonPartida(final JsonValue matchData) {
        Button.ButtonStyle btnStyle = new Button.ButtonStyle();
        btnStyle.up = entryBtnStyle.up;
        btnStyle.over = entryBtnStyle.over;
        Button btn = new Button(btnStyle);
        btn.padTop(8).padBottom(8).padLeft(5).padRight(10);

        String charName = matchData.get("personaje") != null ? matchData.get("personaje").getString("name", "tiki") : "tiki";
        String mapId = matchData.get("mapa") != null ? matchData.get("mapa").getString("string_id", "bosque") : "bosque";
        String gadgetId = matchData.get("gadget") != null ? matchData.get("gadget").getString("string_id", "grenade_kinetic") : "grenade_kinetic";

        long score = matchData.getLong("score");
        long stage = matchData.getLong("stage");
        long wave = matchData.getLong("wave");
        long kills = matchData.getLong("total_killed");

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
        }
        if (charAnim != null) {
            btn.add(new AnimatedImage(charAnim)).size(45, 45).padLeft(5);
        }

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
            btn.add(new Image(gadgetRegion)).size(35, 35).padLeft(12);
        }

        String mapTextureName = "ForestMatchIcon";
        if (mapId.toLowerCase().contains("desierto")) mapTextureName = "DesertMatchIcon";
        if (mapId.toLowerCase().contains("castillo")) mapTextureName = "CastilloMatchIcon";

        TextureRegion mapRegion = Assets.getRegion("shared", "UI_assets/" + mapTextureName);
        if (mapRegion != null) {
            btn.add(new Image(mapRegion)).size(40, 40).padLeft(12);
        }

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
