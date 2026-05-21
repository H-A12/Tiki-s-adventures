package com.tikisadventure.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.Scaling;
import com.tikisadventure.core.Assets;
import com.tikisadventure.localization.LanguageManager;
import com.tikisadventure.ui.button.ButtonFactory;

public class MatchDetailsUI extends Window {

    private Stage stage;
    private ScrollPane scrollPane;
    private boolean focusSet = false;
    private final float BASE_WIDTH = 700f;
    private final float BASE_HEIGHT = 740f;

    public MatchDetailsUI(Skin skin, Stage stage, JsonValue matchData) {
        super("", skin);
        this.stage = stage;

        Image bgImage = new Image(new Texture(Gdx.files.internal("Menu/VentanaDetallesPartida.png")));
        setBackground(bgImage.getDrawable());

        setModal(true);
        setMovable(true);
        setResizable(false);
        pad(60, 55, 45, 55);

        // Mantenemos el tamaÃ±o fijo como base para el diseÃ±o
        setSize(BASE_WIDTH, BASE_HEIGHT);
        setOrigin(Align.center);

        Table contentTable = new Table();
        contentTable.top().pad(10).padRight(20);

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

        // --- EXTRACCIÃ“N DE DATOS ---
        long score = matchData.getLong("score");
        long stageLvl = matchData.getLong("stage");
        long wave = matchData.getLong("wave");
        long kills = matchData.getLong("total_killed");

        String charName = matchData.get("personaje") != null ? matchData.get("personaje").getString("name", "tiki") : "tiki";
        String mapId = matchData.get("mapa") != null ? matchData.get("mapa").getString("string_id", "bosque") : "bosque";
        String gadgetId = matchData.get("gadget") != null ? matchData.get("gadget").getString("string_id", "grenade_kinetic") : matchData.getString("gadget_id", "grenade_kinetic");


        JsonValue extraData = matchData.get("extra_data");
        if (extraData != null && extraData.isString()) {
            extraData = new JsonReader().parse(extraData.asString());
        }

        // =========================================================
        // SECCIÃ“N 1: RESUMEN
        // =========================================================
        Label titleGen = new Label(LanguageManager.t("match.summary"), skin, "font-14");
        titleGen.setColor(Color.CYAN);
        titleGen.setAlignment(Align.center);
        contentTable.add(titleGen).expandX().fillX().padBottom(10).row();

        String mapTextureName = "ForestMatchIcon";
        if (mapId.toLowerCase().contains("desierto")) mapTextureName = "DesertMatchIcon";
        if (mapId.toLowerCase().contains("castillo")) mapTextureName = "CastilloMatchIcon";
        String mapName = LanguageManager.t("map." + mapId + ".short");
        if (mapName.equals("map." + mapId + ".short")) mapName = mapId.toUpperCase();
        addResumenRow(contentTable, skin, LanguageManager.t("match.map") + mapName, "UI_assets/" + mapTextureName, false);

        String charKey = "character.name." + charName.toLowerCase();
        String translatedChar = LanguageManager.t(charKey);
        if (translatedChar.equals(charKey)) translatedChar = charName;
        addResumenRow(contentTable, skin, LanguageManager.t("match.character") + translatedChar, "player_assets/" + charName.toLowerCase() + "/idle", true);

        addTextRow(contentTable, skin, LanguageManager.t("match.score"), String.valueOf(score));
        addTextRow(contentTable, skin, LanguageManager.t("match.stagewave"), stageLvl + "-" + wave);
        addTextRow(contentTable, skin, LanguageManager.t("match.kills.label"), String.valueOf(kills));
        contentTable.add().padBottom(15).row();

        if (extraData != null) {
            // =========================================================
            // SECCIÃ“N 2: ARMAS
            // =========================================================
            JsonValue weapons = extraData.get("weapons_used");
            if (weapons != null && weapons.isArray() && weapons.size > 0) {
                Label titleWeapons = new Label(LanguageManager.t("match.weapons"), skin, "font-14");
                titleWeapons.setColor(Color.ORANGE);
                titleWeapons.setAlignment(Align.center);
                contentTable.add(titleWeapons).expandX().fillX().padBottom(10).row();

                for (int i = 0; i < weapons.size; i++) {
                    String wName = weapons.getString(i);
                    String weaponId = getWeaponIdFromName(wName);
                    String translatedWpn = LanguageManager.t("weapon.name." + weaponId);
                    if (translatedWpn.equals("weapon.name." + weaponId)) translatedWpn = wName;
                    addEquipmentRow(contentTable, skin, translatedWpn, getWeaponSpritePath(wName), 48f);
                }
                contentTable.add().padBottom(15).row();
            }

            // =========================================================
            // SECCIÃ“N 3: GADGET
            // =========================================================
            Label titleGadget = new Label(LanguageManager.t("match.gadget"), skin, "font-14");
            titleGadget.setColor(Color.VIOLET);
            titleGadget.setAlignment(Align.center);
            contentTable.add(titleGadget).expandX().fillX().padBottom(10).row();

            String translatedGadget = LanguageManager.t("gadget.name." + gadgetId);
            if (translatedGadget.equals("gadget.name." + gadgetId)) translatedGadget = gadgetId.toUpperCase();
            addEquipmentRow(contentTable, skin, translatedGadget, getGadgetSpritePath(gadgetId), 32f);
            contentTable.add().padBottom(20).row();

            // =========================================================
            // SECCIÃ“N 4: REGISTRO DE BAJAS
            // =========================================================
            JsonValue killsDetail = extraData.get("kills_detail");
            if (killsDetail != null && killsDetail.size > 0) {
                Label titleKills = new Label(LanguageManager.t("match.kills.title"), skin, "font-14");
                titleKills.setColor(Color.RED);
                titleKills.setAlignment(Align.center);
                contentTable.add(titleKills).expandX().fillX().padBottom(10).row();

                for (JsonValue entry = killsDetail.child; entry != null; entry = entry.next) {
                    addKillsRow(contentTable, skin, entry.name.toUpperCase() + ":", String.valueOf(entry.asInt()));
                }
                contentTable.add().padBottom(20).row();
            }

            // =========================================================
            // SECCIÃ“N 5: ESTADÃSTICAS
            // =========================================================
            JsonValue stats = extraData.get("powerup_stats");
            if (stats != null) {
                Label titleStats = new Label(LanguageManager.t("match.stats"), skin, "font-14");
                titleStats.setColor(Color.YELLOW);
                titleStats.setAlignment(Align.center);
                contentTable.add(titleStats).expandX().fillX().padBottom(10).row();

                addStatRow(contentTable, skin, LanguageManager.t("match.stat.hp"), "stats_asset/statLife", String.valueOf((int)stats.getFloat("hp", 0)));
                addStatRow(contentTable, skin, LanguageManager.t("match.stat.regen"), "stats_asset/statRegen", (int)(stats.getFloat("reg", 0) * 100) + "%");
                addStatRow(contentTable, skin, LanguageManager.t("match.stat.kinetic"), "stats_asset/statKineticDamage", (int)(stats.getFloat("kin", 0) * 100) + "%");
                addStatRow(contentTable, skin, LanguageManager.t("match.stat.leech"), "stats_asset/statLifeLeach", (int)(stats.getFloat("rob", 0) * 100) + "%");
                addStatRow(contentTable, skin, LanguageManager.t("match.stat.explosion"), "stats_asset/statExplosionDamage", (int)(stats.getFloat("exp", 0) * 100) + "%");

                int speedPct = (int)(((stats.getFloat("vel", 10f) / 10f) - 1.0f) * 100f);
                addStatRow(contentTable, skin, LanguageManager.t("match.stat.speed"), "stats_asset/statSpeed", Math.max(0, speedPct) + "%");
                addStatRow(contentTable, skin, LanguageManager.t("match.stat.energy"), "stats_asset/statEnergyDamage", (int)(stats.getFloat("ene", 0) * 100) + "%");

                addStatRow(contentTable, skin, LanguageManager.t("match.stat.xp"), "stats_asset/statXP", (int)((stats.getFloat("xp", 1) - 1) * 100) + "%");
                addStatRow(contentTable, skin, LanguageManager.t("match.stat.fire"), "stats_asset/statFireDamage", (int)(stats.getFloat("fue", 0) * 100) + "%");

                int attrPct = (int)(((stats.getFloat("atr", 2.0f) / 2.0f) - 1.0f) * 100f);
                addStatRow(contentTable, skin, LanguageManager.t("match.stat.attraction"), "stats_asset/statAtraction", Math.max(0, attrPct) + "%");
                addStatRow(contentTable, skin, LanguageManager.t("match.stat.ice"), "stats_asset/statIceDamage", (int)(stats.getFloat("hie", 0) * 100) + "%");

                addStatRow(contentTable, skin, LanguageManager.t("match.stat.evasion"), "stats_asset/statEvasion", (int)(stats.getFloat("eva", 0) * 100) + "%");
                addStatRow(contentTable, skin, LanguageManager.t("match.stat.poison"), "stats_asset/statPoison", (int)(stats.getFloat("ven", 0) * 100) + "%");

                addStatRow(contentTable, skin, LanguageManager.t("match.stat.luck"), "stats_asset/statLuck", (int)(stats.getFloat("sue", 0) * 100) + "%");
                addStatRow(contentTable, skin, LanguageManager.t("match.stat.crit"), "stats_asset/statCrit", (int)(stats.getFloat("crt", 0) * 100) + "%");
            }
        }

        add(scrollPane).expand().fill().pad(10).row();

        TextButton btnCerrar = ButtonFactory.createTextButton(LanguageManager.t("ui.close"), () -> {
            if (getStage() != null) getStage().setScrollFocus(null);
            addAction(Actions.sequence(Actions.fadeOut(0.2f), Actions.removeActor()));
        });
        add(btnCerrar).padTop(10).padBottom(10).width(110);
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        Stage s = getStage();
        if (s != null) {
            if (!focusSet) {
                s.setScrollFocus(scrollPane);
                focusSet = true;
            }
            float w = s.getWidth();
            float h = s.getHeight();

            // CAMBIO AQUÃ: Bajamos el multiplicador de 0.9f a 0.65f
            float esc = Math.min(w / 800f, h / 480f) * 0.65f;

            if (getScaleX() != esc) {
                setScale(esc);
            }

            setPosition(
                Math.round((w - getWidth()) / 2f),
                Math.round((h - getHeight()) / 2f)
            );
        }
    }

    public void show() {
        setOrigin(Align.center);

        float w = stage.getWidth();
        float h = stage.getHeight();

        // CAMBIO AQUÃ: Bajamos el multiplicador de 0.9f a 0.65f tambiÃ©n
        float esc = Math.min(w / 800f, h / 480f) * 0.65f;

        setScale(esc);
        setPosition(Math.round((w - getWidth()) / 2f), Math.round((h - getHeight()) / 2f));

        stage.addActor(this);
        setColor(1, 1, 1, 0);
        addAction(Actions.fadeIn(0.2f));
        focusSet = false;
    }

    // =========================================================
    // MÃ‰TODOS AUXILIARES: AHORA ICONOS Y DATOS VAN JUNTOS
    // =========================================================

    private void addTextRow(Table parentTable, Skin skin, String labelText, String valueText) {
        Table row = new Table();
        Label lblL = new Label(labelText, skin, "font-14");
        lblL.setWrap(true);

        Label lblR = new Label(valueText, skin, "font-14");
        lblR.setAlignment(Align.right);

        row.add(lblL).left().width(220);

        row.add().expandX(); // Separador elÃ¡stico central

        row.add(lblR).right().width(120).padRight(15);
        parentTable.add(row).expandX().fillX().padBottom(4).row();
    }

    private void addKillsRow(Table parentTable, Skin skin, String labelText, String valueText) {
        Table row = new Table();
        Label lblL = new Label(labelText, skin, "font-14");

        Label lblR = new Label(valueText, skin, "font-14");
        lblR.setAlignment(Align.right);

        row.add(lblL).left().width(240);

        row.add().expandX(); // Separador elÃ¡stico central

        row.add(lblR).right().width(80).padRight(15);
        parentTable.add(row).expandX().fillX().padBottom(4).row();
    }

    private void addStatRow(Table parentTable, Skin skin, String nameText, String iconPath, String valueText) {
        Table row = new Table();

        // 1. Texto de la izquierda (ej. "Vida Total")
        Label lblN = new Label(nameText, skin, "font-14");
        row.add(lblN).left().width(220);

        // 2. Muelle separador
        row.add().expandX();

        // 3. PRIMERO EL VALOR NUMÃ‰RICO (ej. "20" o "35%")
        Label lblV = new Label(valueText, skin, "font-14");
        lblV.setAlignment(Align.right);
        row.add(lblV).right().width(60).padRight(10); // padRight(10) para separarlo un pelÃ­n del icono

        // 4. DESPUÃ‰S EL ICONO (Pegado a la derecha del todo)
        if (iconPath != null && !iconPath.isEmpty()) {
            TextureRegion region = Assets.getRegion("shared", iconPath);
            if (region != null) {
                Image icon = new Image(new TextureRegionDrawable(region));
                icon.setScaling(Scaling.fit);
                row.add(icon).size(24f, 24f).right().padRight(15);
            } else {
                row.add().size(24f, 24f).right().padRight(15);
            }
        } else {
            row.add().size(24f, 24f).right().padRight(15);
        }

        parentTable.add(row).expandX().fillX().padBottom(4).row();
    }

    private void addEquipmentRow(Table parentTable, Skin skin, String text, String iconPath, float size) {
        Table row = new Table();
        Label lblT = new Label(text, skin, "font-14");
        lblT.setWrap(true);
        row.add(lblT).left().width(220);

        row.add().expandX(); // Separador elÃ¡stico central

        if (iconPath != null && !iconPath.isEmpty()) {
            TextureRegion region = Assets.getRegion("shared", iconPath);
            if (region != null) {
                Image icon = new Image(new TextureRegionDrawable(region));
                icon.setScaling(Scaling.fit);
                row.add(icon).size(size, size).right().padRight(15);
            }
        }
        parentTable.add(row).expandX().fillX().padBottom(6).row();
    }

    private void addResumenRow(Table parentTable, Skin skin, String text, String iconPath, boolean isCharacter) {
        Table row = new Table();
        Label lblT = new Label(text, skin, "font-14");
        lblT.setWrap(true);
        row.add(lblT).left().width(220);

        row.add().expandX(); // Separador elÃ¡stico central

        if (iconPath != null && !iconPath.isEmpty()) {
            TextureRegion region = Assets.getRegion("shared", iconPath);
            if (region != null) {
                if (isCharacter) {
                    region = new TextureRegion(region, 0, 0, 16, 16);
                }
                Image icon = new Image(new TextureRegionDrawable(region));
                icon.setScaling(Scaling.fit);
                row.add(icon).size(42f, 42f).right().padRight(15);
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
        if (name.contains("saxofon") || name.contains("saxofÃ³n")) return "weapons_assets/Saxophone";
        if (name.contains("discos") || name.contains("sierras")) return "weapons_assets/DiscLauncher";
        if (name.contains("banana")) return "weapons_assets/Banana";
        if (name.contains("pez") || name.contains("putripez") || name.contains("putripez")) return "weapons_assets/RottenFish";
        if (name.contains("espada")) return "weapons_assets/Sword";
        return "weapons_assets/Machinegun";
    }

    private String getWeaponIdFromName(String weaponName) {
        String name = weaponName.toLowerCase();
        if (name.contains("bolas")) return "BallRifle";
        if (name.contains("clavolleta")) return "SubmachineGun";
        if (name.contains("palillos")) return "ToothpickShotgun";
        if (name.contains("pelotas")) return "TennisLauncher";
        if (name.contains("pirocohete") || name.contains("fuegos")) return "FireworkLauncher";
        if (name.contains("extintor")) return "Lanzallamas";
        if (name.contains("hielo") || name.contains("tritura")) return "IceGrinder";
        if (name.contains("enchufe")) return "BatteryPlugger";
        if (name.contains("saxofon") || name.contains("saxofón")) return "Saxophone";
        if (name.contains("discos") || name.contains("sierras")) return "LanzaSierras";
        if (name.contains("banana")) return "Boomerang";
        if (name.contains("putripez") || name.contains("putripez") || name.contains("putripez")) return "PezGlobo";
        if (name.contains("espada")) return "EspadaEjemplo";
        return weaponName;
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
