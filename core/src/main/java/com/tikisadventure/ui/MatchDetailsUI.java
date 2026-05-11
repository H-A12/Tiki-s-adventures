package com.tikisadventure.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.Scaling;
import com.tikisadventure.core.Assets;

public class MatchDetailsUI extends Window {

    private Stage stage;
    private ScrollPane scrollPane;

    public MatchDetailsUI(Skin skin, Stage stage, JsonValue matchData) {
        super("", skin);
        this.stage = stage;

        Image bgImage = new Image(new Texture(Gdx.files.internal("Menu/VentanaDetallesPartida.png")));
        setBackground(bgImage.getDrawable());

        setModal(true);
        setMovable(true);
        setResizable(false);
        pad(45, 40, 30, 40);
        setSize(540, 580);

        Table contentTable = new Table();
        // AÑADIDO: padRight(35) para alejar el texto de la barra de desplazamiento
        contentTable.top().pad(10).padRight(35);

        Pixmap pmScrollBg = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pmScrollBg.setColor(0.75f, 0.75f, 0.75f, 0.5f);
        pmScrollBg.fill();
        TextureRegionDrawable scrollBg = new TextureRegionDrawable(new TextureRegion(new Texture(pmScrollBg)));
        scrollBg.setMinWidth(14);
        pmScrollBg.dispose();

        Pixmap pmScrollKnob = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pmScrollKnob.setColor(0.85f, 0.85f, 0.85f, 1f);
        pmScrollKnob.fill();
        TextureRegionDrawable scrollKnob = new TextureRegionDrawable(new TextureRegion(new Texture(pmScrollKnob)));
        scrollKnob.setMinWidth(14);
        pmScrollKnob.dispose();

        ScrollPane.ScrollPaneStyle scrollStyle = new ScrollPane.ScrollPaneStyle();
        scrollStyle.vScroll = scrollBg;
        scrollStyle.vScrollKnob = scrollKnob;

        scrollPane = new ScrollPane(contentTable, scrollStyle);
        scrollPane.setFadeScrollBars(false);
        scrollPane.setScrollingDisabled(true, false);

        // --- 1. EXTRACCIÓN DE DATOS BÁSICOS ---
        long score = matchData.getLong("score");
        long stageLvl = matchData.getLong("stage");
        long wave = matchData.getLong("wave");
        long kills = matchData.getLong("total_killed");

        String charName = matchData.get("personaje") != null ? matchData.get("personaje").getString("name", "tiki") : "tiki";
        String mapId = matchData.get("mapa") != null ? matchData.get("mapa").getString("string_id", "bosque") : "bosque";
        String gadgetId = matchData.get("gadget") != null ? matchData.get("gadget").getString("string_id", "grenade_kinetic") : matchData.getString("gadget_id", "grenade_kinetic");
        String gadgetName = matchData.get("gadget") != null ? matchData.get("gadget").getString("name", "Desconocido") : matchData.getString("gadget_id", "Sin gadget");

        JsonValue extraData = matchData.get("extra_data");
        if (extraData != null && extraData.isString()) {
            extraData = new JsonReader().parse(extraData.asString());
        }

        // =========================================================
        // SECCIÓN 1: RESUMEN
        // =========================================================
        Label titleGen = new Label("--- RESUMEN ---", skin, "font-14");
        titleGen.setColor(Color.CYAN);
        contentTable.add(titleGen).padBottom(5).row();

        String mapTextureName = "ForestMatchIcon";
        if (mapId.toLowerCase().contains("desierto")) mapTextureName = "DesertMatchIcon";
        if (mapId.toLowerCase().contains("castillo")) mapTextureName = "CastilloMatchIcon";
        addResumenRow(contentTable, skin, "Mapa: " + mapId.toUpperCase(), "UI_assets/" + mapTextureName, false);

        addResumenRow(contentTable, skin, "Personaje: " + charName.toUpperCase(), "player_assets/" + charName.toLowerCase() + "/idle", true);

        addTextRow(contentTable, skin, "Puntuación:", String.valueOf(score));
        addTextRow(contentTable, skin, "Nivel Alcanzado:", stageLvl + "-" + wave);
        addTextRow(contentTable, skin, "Enemigos Totales:", String.valueOf(kills));
        contentTable.add().padBottom(15).row();

        if (extraData != null) {
            // =========================================================
            // SECCIÓN 2: ARSENAL EQUIPADO
            // =========================================================
            JsonValue weapons = extraData.get("weapons_used");
            if (weapons != null && weapons.isArray() && weapons.size > 0) {
                Label titleWeapons = new Label("--- ARSENAL EQUIPADO ---", skin, "font-14");
                titleWeapons.setColor(Color.ORANGE);
                contentTable.add(titleWeapons).padBottom(5).row();

                for (int i = 0; i < weapons.size; i++) {
                    String wName = weapons.getString(i);
                    addEquipmentRow(contentTable, skin, "- " + wName, getWeaponSpritePath(wName), 48f);
                }
                contentTable.add().padBottom(15).row();
            }

            // =========================================================
            // SECCIÓN 3: GADGET EQUIPADO
            // =========================================================
            Label titleGadget = new Label("--- GADGET EQUIPADO ---", skin, "font-14");
            titleGadget.setColor(Color.VIOLET);
            contentTable.add(titleGadget).padBottom(5).row();

            addEquipmentRow(contentTable, skin, gadgetName.toUpperCase(), getGadgetSpritePath(gadgetId), 32f);
            contentTable.add().padBottom(20).row();

            // =========================================================
            // SECCIÓN 4: REGISTRO DE BAJAS
            // =========================================================
            JsonValue killsDetail = extraData.get("kills_detail");
            if (killsDetail != null && killsDetail.size > 0) {
                Label titleKills = new Label("--- REGISTRO DE BAJAS ---", skin, "font-14");
                titleKills.setColor(Color.RED);
                contentTable.add(titleKills).padBottom(5).row();

                for (JsonValue entry = killsDetail.child; entry != null; entry = entry.next) {
                    // CAMBIO AQUÍ: Ahora usa addKillsRow
                    addKillsRow(contentTable, skin, entry.name.toUpperCase() + ":", String.valueOf(entry.asInt()));
                }
                contentTable.add().padBottom(20).row();
            }

            // =========================================================
            // SECCIÓN 5: ESTADÍSTICAS FINALES
            // =========================================================
            JsonValue stats = extraData.get("powerup_stats");
            if (stats != null) {
                Label titleStats = new Label("--- ESTADÍSTICAS FINALES ---", skin, "font-14");
                titleStats.setColor(Color.YELLOW);
                contentTable.add(titleStats).padBottom(5).row();

                addStatRow(contentTable, skin, "Vida Total", "stats_asset/statLife", String.valueOf((int)stats.getFloat("hp", 0)));

                addStatRow(contentTable, skin, "Regen. Vida", "stats_asset/statRegen", (int)(stats.getFloat("reg", 0) * 100) + "%");
                addStatRow(contentTable, skin, "Daño Cinético", "stats_asset/statKineticDamage", (int)(stats.getFloat("kin", 0) * 100) + "%");

                addStatRow(contentTable, skin, "Robo de Vida", "stats_asset/statLifeLeach", (int)(stats.getFloat("rob", 0) * 100) + "%");
                addStatRow(contentTable, skin, "Daño Explosivo", "stats_asset/statExplosionDamage", (int)(stats.getFloat("exp", 0) * 100) + "%");

                int speedPct = (int)(((stats.getFloat("vel", 10f) / 10f) - 1.0f) * 100f);
                addStatRow(contentTable, skin, "Velocidad", "stats_asset/statSpeed", Math.max(0, speedPct) + "%");
                addStatRow(contentTable, skin, "Daño Energía", "stats_asset/statEnergyDamage", (int)(stats.getFloat("ene", 0) * 100) + "%");

                addStatRow(contentTable, skin, "Bonus XP", "stats_asset/statXP", (int)((stats.getFloat("xp", 1) - 1) * 100) + "%");
                addStatRow(contentTable, skin, "Daño Fuego", "stats_asset/statFireDamage", (int)(stats.getFloat("fue", 0) * 100) + "%");

                int attrPct = (int)(((stats.getFloat("atr", 2.0f) / 2.0f) - 1.0f) * 100f);
                addStatRow(contentTable, skin, "Atracción XP", "stats_asset/statAtraction", Math.max(0, attrPct) + "%");
                addStatRow(contentTable, skin, "Daño Hielo", "stats_asset/statIceDamage", (int)(stats.getFloat("hie", 0) * 100) + "%");

                addStatRow(contentTable, skin, "Evasión", "stats_asset/statEvasion", (int)(stats.getFloat("eva", 0) * 100) + "%");
                addStatRow(contentTable, skin, "Daño Veneno", "stats_asset/statPoisonDamage", (int)(stats.getFloat("ven", 0) * 100) + "%");

                addStatRow(contentTable, skin, "Suerte", "stats_asset/statLuck", (int)(stats.getFloat("sue", 0) * 100) + "%");
                addStatRow(contentTable, skin, "Prob. Crítico", "stats_asset/statCrit", (int)(stats.getFloat("crt", 0) * 100) + "%");
            }
        }

        add(scrollPane).expand().fill().pad(10).row();

        TextButton.TextButtonStyle volverStyle = new TextButton.TextButtonStyle();
        volverStyle.up = new TextureRegionDrawable(new TextureRegion(new Texture(Gdx.files.internal("Menu/BotonText.png"))));
        volverStyle.font = skin.get("font-14", Label.LabelStyle.class).font;
        TextButton btnCerrar = new TextButton("Volver", volverStyle);
        btnCerrar.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                getStage().setScrollFocus(null);
                addAction(Actions.sequence(
                    Actions.fadeOut(0.2f),
                    Actions.removeActor()
                ));
            }
        });
        add(btnCerrar).padTop(10).padBottom(10).width(110);
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        Stage s = getStage();
        if (s != null) {
            float w = s.getWidth();
            float h = s.getHeight();
            float esc = Math.min(w / 1280f, h / 720f);
            float targetW = Math.max(400, Math.round(540 * esc));
            float targetH = Math.max(450, Math.round(580 * esc));
            if (getWidth() != targetW || getHeight() != targetH) {
                setSize(targetW, targetH);
                invalidate();
            }
            setPosition(
                Math.round((w - targetW) / 2f),
                Math.round((h - targetH) / 2f)
            );
        }
    }

    public void show() {
        float w = stage.getWidth();
        float h = stage.getHeight();
        float esc = Math.min(w / 1280f, h / 720f);
        float targetW = Math.max(400, Math.round(540 * esc));
        float targetH = Math.max(450, Math.round(580 * esc));
        setSize(targetW, targetH);
        setPosition(Math.round((w - targetW) / 2f), Math.round((h - targetH) / 2f));
        stage.addActor(this);
        setColor(1, 1, 1, 0);
        addAction(Actions.fadeIn(0.2f));
        stage.setScrollFocus(scrollPane);
    }

    // =========================================================
    // MÉTODOS AUXILIARES FINALES (MatchDetailsUI)
    // =========================================================

    private void addTextRow(Table parentTable, Skin skin, String labelText, String valueText) {
        Table row = new Table();
        Label lblL = new Label(labelText, skin, "font-14");
        lblL.setWrap(true);

        Label lblR = new Label(valueText, skin, "font-14");
        lblR.setAlignment(Align.right);
        lblR.setWrap(true);

        // Anchos ajustados para la cabecera (Resumen)
        row.add(lblL).left().width(180);
        row.add().expandX();
        row.add(lblR).right().width(150).padRight(25);

        parentTable.add(row).expandX().fillX().padBottom(4).row();
    }

    // NUEVO MÉTODO EXCLUSIVO PARA EL REGISTRO DE BAJAS (ALINEADO CON STATS)
    private void addKillsRow(Table parentTable, Skin skin, String labelText, String valueText) {
        Table row = new Table();
        Label lblL = new Label(labelText, skin, "font-14");

        Label lblR = new Label(valueText, skin, "font-14");
        lblR.setAlignment(Align.right);

        // 244 es la suma exacta de (texto de stat + margen de icono + ancho de icono)
        row.add(lblL).left().width(244);

        row.add().expandX(); // El "muelle" para separar

        // Exactamente el mismo ancho y margen derecho que usan los números de las Stats
        row.add(lblR).right().width(80).padRight(25);

        // expandX().fillX() es la clave para que la tabla se estire a los bordes igual que la de abajo
        parentTable.add(row).expandX().fillX().padBottom(4).row();
    }

    private void addStatRow(Table parentTable, Skin skin, String nameText, String iconPath, String valueText) {
        Table row = new Table();

        Label lblN = new Label(nameText, skin, "font-14");
        // Aumentado a 200 para empujar el icono más hacia el centro
        row.add(lblN).left().width(200);

        if (iconPath != null && !iconPath.isEmpty()) {
            TextureRegion region = Assets.getRegion("shared", iconPath);
            if (region != null) {
                Image icon = new Image(new TextureRegionDrawable(region));
                icon.setScaling(Scaling.fit);
                // Separamos el icono del texto con padLeft(20)
                row.add(icon).size(24f, 24f).left().padLeft(20);
            } else {
                row.add().size(24f, 24f).left().padLeft(20);
            }
        }

        row.add().expandX();

        Label lblV = new Label(valueText, skin, "font-14");
        lblV.setAlignment(Align.right);
        row.add(lblV).right().width(80).padRight(25);

        parentTable.add(row).expandX().fillX().padBottom(4).row();
    }

    private void addEquipmentRow(Table parentTable, Skin skin, String text, String iconPath, float size) {
        Table row = new Table();
        Label lblT = new Label(text, skin, "font-14");
        lblT.setWrap(true);
        row.add(lblT).left().width(200);

        row.add().expandX();

        if (iconPath != null && !iconPath.isEmpty()) {
            TextureRegion region = Assets.getRegion("shared", iconPath);
            if (region != null) {
                Image icon = new Image(new TextureRegionDrawable(region));
                icon.setScaling(Scaling.fit);
                row.add(icon).size(size, size).right().padRight(25);
            }
        }
        parentTable.add(row).expandX().fillX().padBottom(6).row();
    }

    private void addResumenRow(Table parentTable, Skin skin, String text, String iconPath, boolean isCharacter) {
        Table row = new Table();
        Label lblT = new Label(text, skin, "font-14");
        lblT.setWrap(true);
        row.add(lblT).left().width(200);

        row.add().expandX();

        if (iconPath != null && !iconPath.isEmpty()) {
            TextureRegion region = Assets.getRegion("shared", iconPath);
            if (region != null) {
                if (isCharacter) {
                    region = new TextureRegion(region, 0, 0, 16, 16);
                }
                Image icon = new Image(new TextureRegionDrawable(region));
                icon.setScaling(Scaling.fit);
                row.add(icon).size(42f, 42f).right().padRight(25);
            }
        }
        parentTable.add(row).expandX().fillX().padBottom(6).row();
    }

    private String getWeaponSpritePath(String weaponName) {
        String name = weaponName.toLowerCase();
        if (name.contains("bolas")) return "weapons_assets/BallRifle";
        if (name.contains("clavolleta")) return "weapons_assets/NailGun";
        if (name.contains("palillos")) return "weapons_assets/ToothpickShotgun";
        if (name.contains("pelotas")) return "weapons_assets/TennisLauncher";
        if (name.contains("pirocohete") || name.contains("fuegos")) return "weapons_assets/RocketLauncher";
        if (name.contains("extintor")) return "weapons_assets/Extinguisher";
        if (name.contains("hielo") || name.contains("tritura")) return "weapons_assets/IceGrinder";
        if (name.contains("enchufe")) return "weapons_assets/BatteryPlugger";
        if (name.contains("saxofon") || name.contains("saxofón")) return "weapons_assets/Saxophone";
        if (name.contains("discos") || name.contains("sierras")) return "weapons_assets/DiscLauncher";
        if (name.contains("banana")) return "weapons_assets/Banana";
        if (name.contains("pez") || name.contains("putripez") || name.contains("pudripez")) return "weapons_assets/RottenFish";
        if (name.contains("espada")) return "weapons_assets/Sword";
        return "weapons_assets/Machinegun";
    }

    private String getGadgetSpritePath(String gadgetId) {
        if (gadgetId.contains("explosive")) return "weapons_assets/ShakedCola";
        if (gadgetId.contains("fire")) return "weapons_assets/Jalapeno";
        if (gadgetId.contains("freeze")) return "weapons_assets/IceCandy";
        if (gadgetId.contains("cactus")) return "weapons_assets/Sock";
        if (gadgetId.contains("sewer")) return "weapons_assets/Sewer";
        if (gadgetId.contains("sheel")) return "weapons_assets/MagicSheel";
        if (gadgetId.contains("scarecrow")) return "weapons_assets/Scarecrow";
        if (gadgetId.contains("turret")) return "weapons_assets/Turret";
        if (gadgetId.contains("dash")) return "UI_assets/DashIcon";
        return "weapons_assets/Corn";
    }
}
