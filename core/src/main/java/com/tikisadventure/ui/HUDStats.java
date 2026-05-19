package com.tikisadventure.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Scaling;
import com.tikisadventure.combat.DamageType;
import com.tikisadventure.combat.weapons.Weapon;
import com.tikisadventure.entities.player.Player;
import com.badlogic.gdx.Input;
import com.tikisadventure.core.Assets;
import com.tikisadventure.core.SaveManager;
import com.tikisadventure.ui.FontManager;

public class HUDStats {

    private static final float ICON_SIZE = 20f;
    private static final float ROW_PADDING = 10f;
    private static final float TOOLTIP_OFFSET_X = 15f;
    private static final float TOOLTIP_OFFSET_Y = 15f;

    private Table statsPanel;
    private Label toggleStatsButton;
    private Label statsKeyLabel;
    private boolean statsVisible = true;

    private Table tooltipTable;
    private boolean tooltipVisible = false;

    private Label regLabel, leechLabel, speedLabel, xpBonusLabel, attrLabel, evasionLabel, luckLabel;
    private Label kineticLabel, explosiveLabel, energyLabel, fireLabel, iceLabel, poisonLabel, critLabel;

    private Stage stage;
    private Skin skin;
    private Vector2 tempCoords = new Vector2();
    private Player currentPlayer;

    public enum StatCategory {
        REGEN, LEECH, SPEED, XP, ATTRACTION, EVASION, LUCK, CRIT,
        KINETIC, EXPLOSIVE, ENERGY, FIRE, ICE, POISON
    }

    public HUDStats(Skin skin, Stage stage) {
        this.stage = stage;
        this.skin = skin;
        createStatsPanel(skin);
    }

    private Image createStatIcon(String regionName) {
        TextureRegion region = Assets.getRegion("shared", regionName);
        Image icon = new Image(new TextureRegionDrawable(region));
        icon.setSize(ICON_SIZE, ICON_SIZE);
        return icon;
    }

    private Table createStatRowWithTooltip(Image icon, Label valueLabel, String titleText, StatCategory category) {
        Table row = new Table();
        row.add(icon).size(ICON_SIZE).padRight(6);
        row.add(valueLabel).left();

        row.setTouchable(Touchable.enabled);

        row.addListener(new ClickListener() {
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                if (pointer == -1) {
                    rebuildTooltip(category, titleText);
                    tooltipTable.setVisible(true);
                    tooltipVisible = true;
                    tooltipTable.toFront();
                }
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                if (pointer == -1) {
                    tooltipVisible = false;
                    tooltipTable.setVisible(false);
                }
            }
        });

        return row;
    }

    // --- NUEVO: Método para obtener el color brillante según el Tier ---
    private Color getTierColor(int tier) {
        switch (tier) {
            case 2: return new Color(0.3f, 1.0f, 0.3f, 1f); // Verde brillante
            case 3: return new Color(0.3f, 0.7f, 1.0f, 1f); // Azul claro / Celeste (Destaca más en negro)
            case 4: return new Color(0.8f, 0.4f, 1.0f, 1f); // Morado brillante / Violeta
            case 5: return Color.GOLD;                      // Dorado
            case 1:
            default: return Color.WHITE;                    // Blanco (Tier 1 o base)
        }
    }

    private void rebuildTooltip(StatCategory category, String titleText) {
        tooltipTable.clearChildren();

        Label titleLabel = new Label(titleText, skin, "font-18");
        titleLabel.setColor(Color.WHITE);
        tooltipTable.add(titleLabel).center().padBottom(8).row();

        if (currentPlayer == null) {
            tooltipTable.pack();
            return;
        }

        switch (category) {
            case SPEED:
                Label speedDetail = new Label(String.format(java.util.Locale.US, "%.1f", currentPlayer.getSpeed()), skin);
                tooltipTable.add(speedDetail).left().row();
                break;

            case ATTRACTION:
                Label attrDetail = new Label(String.format(java.util.Locale.US, "%.1f", currentPlayer.getAttractionRange()), skin);
                tooltipTable.add(attrDetail).left().row();
                break;

            case REGEN:
                float regenAmount = 0;
                if (currentPlayer.getHealthComponent() != null) {
                    regenAmount = currentPlayer.getHealthComponent().maxHealth * currentPlayer.getLifeRegenPercent();
                }
                Label regenDetail = new Label(String.format(java.util.Locale.US, "%.1f HP / 2s", regenAmount), skin);
                tooltipTable.add(regenDetail).left().row();
                break;

            case KINETIC:
            case EXPLOSIVE:
            case ENERGY:
            case FIRE:
            case ICE:
            case POISON:
                DamageType dt = getDamageTypeFromCategory(category);
                boolean hasWeapons = false;

                // Mostramos el Gadget (Habilidad 2) si tiene el mismo tipo de daño
                if (currentPlayer.getProfile() != null && currentPlayer.getProfile().specialAbility2 != null) {
                    com.tikisadventure.combat.abilities.Ability gadget = currentPlayer.getProfile().specialAbility2;
                    if (gadget.getDamageType() == dt) {
                        hasWeapons = true;
                        Table weaponRow = new Table();

                        if (gadget.getSpritePath() != null) {
                            com.badlogic.gdx.graphics.g2d.TextureRegion gReg = com.tikisadventure.core.Assets.getRegion("shared", gadget.getSpritePath());
                            if (gReg != null) {
                                Image gIcon = new Image(gReg);
                                gIcon.setScaling(com.badlogic.gdx.utils.Scaling.fit);
                                weaponRow.add(gIcon).size(40, 40).padRight(12);
                            }
                        }

                        // Calculamos daño final igual que en las armas
                        float finalDmg = gadget.getBaseDamage() * (1f + currentPlayer.getDamageBonusByType(dt));
                        Label dmgLabel = new Label(String.format(java.util.Locale.US, "%.1f", finalDmg), skin, "font-16");

                        weaponRow.add(dmgLabel).left();
                        tooltipTable.add(weaponRow).left().padBottom(6).row();
                    }
                }

                for (Weapon w : currentPlayer.getWeaponFactory().getWeapons()) {
                    if (w.getDamageType() == dt) {
                        hasWeapons = true;
                        Table weaponRow = new Table();

                        if (w.getSprite() != null) {
                            Image wIcon = new Image(w.getSprite());
                            wIcon.setScaling(Scaling.fit);
                            weaponRow.add(wIcon).size(40, 40).padRight(12);
                        }

                        Label dmgLabel = new Label(String.format(java.util.Locale.US, "%.1f", w.getFinalDamage()), skin, "font-16");

                        // --- AQUÍ APLICAMOS EL COLOR DEL TIER ---
                        dmgLabel.setColor(getTierColor(w.getTier()));

                        weaponRow.add(dmgLabel).left();

                        tooltipTable.add(weaponRow).left().padBottom(6).row();
                    }
                }

                if (!hasWeapons) {
                    Label noWpLabel = new Label("Sin armas", skin);
                    noWpLabel.setColor(Color.GRAY);
                    tooltipTable.add(noWpLabel).left().row();
                }
                break;

            default:
                break;
        }

        tooltipTable.pack();
    }

    private DamageType getDamageTypeFromCategory(StatCategory category) {
        switch (category) {
            case KINETIC: return DamageType.KINETIC;
            case EXPLOSIVE: return DamageType.EXPLOSIVE;
            case ENERGY: return DamageType.ENERGY;
            case FIRE: return DamageType.FIRE;
            case ICE: return DamageType.ICE;
            case POISON: return DamageType.POISON;
            default: return DamageType.KINETIC;
        }
    }

    private void addStatToTable(Image icon, Label label, String title, StatCategory category, boolean isFirstColumn) {
        Cell<Table> cell = statsPanel.add(createStatRowWithTooltip(icon, label, title, category)).left().padBottom(ROW_PADDING);
        if (!isFirstColumn) {
            cell.padLeft(15);
        }
    }

    private void createStatsPanel(Skin skin) {
        statsPanel = new Table();
        statsPanel.setBackground(skin.newDrawable("rect", new Color(0.1f, 0.1f, 0.1f, 0.85f)));
        statsPanel.pad(12);
        statsPanel.setTransform(true);

        regLabel = new Label("0%", skin);
        leechLabel = new Label("0%", skin);
        speedLabel = new Label("0%", skin);
        xpBonusLabel = new Label("0%", skin);
        attrLabel = new Label("+0%", skin);
        evasionLabel = new Label("0%", skin);
        luckLabel = new Label("0%", skin);

        kineticLabel = new Label("0%", skin);
        explosiveLabel = new Label("0%", skin);
        energyLabel = new Label("0%", skin);
        fireLabel = new Label("0%", skin);
        iceLabel = new Label("0%", skin);
        poisonLabel = new Label("0%", skin);
        critLabel = new Label("0%", skin);

        Image regIcon = createStatIcon("stats_asset/statRegen");
        Image leechIcon = createStatIcon("stats_asset/statLifeLeach");
        Image speedIcon = createStatIcon("stats_asset/statSpeed");
        Image xpIcon = createStatIcon("stats_asset/statXP");
        Image attrIcon = createStatIcon("stats_asset/statAtraction");
        Image evasionIcon = createStatIcon("stats_asset/statEvasion");
        Image luckIcon = createStatIcon("stats_asset/statLuck");

        Image kineticIcon = createStatIcon("stats_asset/statKineticDamage");
        Image explosiveIcon = createStatIcon("stats_asset/statExplosionDamage");
        Image energyIcon = createStatIcon("stats_asset/statEnergyDamage");
        Image fireIcon = createStatIcon("stats_asset/statFireDamage");
        Image iceIcon = createStatIcon("stats_asset/statIceDamage");
        Image poisonIcon = createStatIcon("stats_asset/statPoison");
        Image critIcon = createStatIcon("stats_asset/statCrit");

        addStatToTable(regIcon, regLabel, "Regeneración", StatCategory.REGEN, true);
        addStatToTable(kineticIcon, kineticLabel, "Daño Cinético", StatCategory.KINETIC, false);
        statsPanel.row();

        addStatToTable(leechIcon, leechLabel, "Robo de Vida", StatCategory.LEECH, true);
        addStatToTable(explosiveIcon, explosiveLabel, "Daño Explosivo", StatCategory.EXPLOSIVE, false);
        statsPanel.row();

        addStatToTable(speedIcon, speedLabel, "Velocidad", StatCategory.SPEED, true);
        addStatToTable(energyIcon, energyLabel, "Daño de Energía", StatCategory.ENERGY, false);
        statsPanel.row();

        addStatToTable(xpIcon, xpBonusLabel, "Bonus XP", StatCategory.XP, true);
        addStatToTable(fireIcon, fireLabel, "Daño de Fuego", StatCategory.FIRE, false);
        statsPanel.row();

        addStatToTable(attrIcon, attrLabel, "Atracción", StatCategory.ATTRACTION, true);
        addStatToTable(iceIcon, iceLabel, "Daño de Hielo", StatCategory.ICE, false);
        statsPanel.row();

        addStatToTable(evasionIcon, evasionLabel, "Evasión", StatCategory.EVASION, true);
        addStatToTable(poisonIcon, poisonLabel, "Daño Veneno", StatCategory.POISON, false);
        statsPanel.row();

        addStatToTable(luckIcon, luckLabel, "Suerte", StatCategory.LUCK, true);
        addStatToTable(critIcon, critLabel, "Prob. Crític", StatCategory.CRIT, false);
        statsPanel.row();

        statsPanel.pack();
        statsPanel.setPosition(10, 50);

        tooltipTable = new Table();
        tooltipTable.setBackground(skin.newDrawable("rect", new Color(0f, 0f, 0f, 0.90f)));
        tooltipTable.pad(10);
        tooltipTable.setVisible(false);

        toggleStatsButton = new Label("Estadísticas", skin);
        toggleStatsButton.setPosition(10, 5);
        toggleStatsButton.setSize(60, 25);
        toggleStatsButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                toggleStatsPanel();
            }
        });

        stage.addActor(statsPanel);
        stage.addActor(toggleStatsButton);

        Label.LabelStyle statsKeyStyle = new Label.LabelStyle(FontManager.getFont(12), Color.YELLOW);
        int statsKeyCode = SaveManager.getProfileData().inputConfig.keyboardMapping.get("toggleStats");
        String statsKeyName = statsKeyCode >= 0 && statsKeyCode <= 4
            ? getMouseButtonName(statsKeyCode) : Input.Keys.toString(statsKeyCode);
        statsKeyLabel = new Label(statsKeyName, statsKeyStyle);
        statsKeyLabel.setPosition(toggleStatsButton.getX() + toggleStatsButton.getPrefWidth() + 8, toggleStatsButton.getY() + 7);
        stage.addActor(statsKeyLabel);

        stage.addActor(tooltipTable);
    }

    public void toggleStatsPanel() {
        statsVisible = !statsVisible;
        statsPanel.setVisible(statsVisible);
        toggleStatsButton.setColor(statsVisible ? Color.WHITE : Color.GRAY);
    }

    public void updateStats(Player player) {
        if (player != null) {
            this.currentPlayer = player;

            regLabel.setText((int)(player.getLifeRegenPercent() * 100) + "%");
            leechLabel.setText((int)(player.getLifeLeechPercent() * 100) + "%");

            float baseSpeed = player.getProfile().speed;
            int speedBonusPct = (int)(((player.getSpeed() / baseSpeed) - 1.0f) * 100f);
            speedLabel.setText(Math.max(0, speedBonusPct) + "%");

            xpBonusLabel.setText((int)((player.getXpMultiplier() - 1) * 100) + "%");

            int attrBonusPct = (int)(((player.getAttractionRange() / 2.0f) - 1.0f) * 100f);
            attrLabel.setText(Math.max(0, attrBonusPct) + "%");

            evasionLabel.setText((int)(player.getEvasionChance() * 100) + "%");
            luckLabel.setText((int)(player.getLuck() * 100) + "%");

            kineticLabel.setText(String.valueOf((int)(player.getKineticDamageBonus() * 100) + "%"));
            kineticLabel.setColor(player.hasDamageTypeEquipped(DamageType.KINETIC) ? Color.WHITE : Color.GRAY);

            explosiveLabel.setText(String.valueOf((int)(player.getExplosiveDamageBonus() * 100) + "%"));
            explosiveLabel.setColor(player.hasDamageTypeEquipped(DamageType.EXPLOSIVE) ? Color.WHITE : Color.GRAY);

            energyLabel.setText(String.valueOf((int)(player.getEnergyDamageBonus() * 100) + "%"));
            energyLabel.setColor(player.hasDamageTypeEquipped(DamageType.ENERGY) ? Color.WHITE : Color.GRAY);

            fireLabel.setText(String.valueOf((int)(player.getFireDamageBonus() * 100) + "%"));
            fireLabel.setColor(player.hasDamageTypeEquipped(DamageType.FIRE) ? Color.WHITE : Color.GRAY);

            iceLabel.setText(String.valueOf((int)(player.getIceDamageBonus() * 100) + "%"));
            iceLabel.setColor(player.hasDamageTypeEquipped(DamageType.ICE) ? Color.WHITE : Color.GRAY);

            poisonLabel.setText(String.valueOf((int)(player.getPoisonDamageBonus() * 100) + "%"));
            poisonLabel.setColor(player.hasDamageTypeEquipped(DamageType.POISON) ? Color.WHITE : Color.GRAY);

            critLabel.setText((int)(player.getCritChanceBonus() * 100) + "%");

            statsPanel.pack();

            float scale = MathUtils.clamp(stage.getWidth() / 1280f, 0.7f, 2.0f);
            statsPanel.setScale(scale);
            statsPanel.setPosition(10 * scale, 50 * scale);
            toggleStatsButton.setPosition(10 * scale, 5 * scale);
            statsKeyLabel.setPosition(toggleStatsButton.getX() + toggleStatsButton.getPrefWidth() + 8, toggleStatsButton.getY() + 7);
        }
    }

    public void render() {
        if (tooltipVisible && tooltipTable.isVisible()) {
            tempCoords.set(Gdx.input.getX(), Gdx.input.getY());
            stage.screenToStageCoordinates(tempCoords);
            tooltipTable.setPosition(tempCoords.x + TOOLTIP_OFFSET_X, tempCoords.y + TOOLTIP_OFFSET_Y);
        }
    }

    private String getMouseButtonName(int code) {
        switch (code) {
            case Input.Buttons.LEFT: return "Left Click";
            case Input.Buttons.RIGHT: return "Right Click";
            case Input.Buttons.MIDDLE: return "Middle Click";
            case Input.Buttons.BACK: return "Back";
            case Input.Buttons.FORWARD: return "Forward";
            default: return "Button " + code;
        }
    }

    public void bringToFront() {
        if (statsPanel != null) statsPanel.toFront();
        if (toggleStatsButton != null) toggleStatsButton.toFront();
        if (tooltipTable != null) tooltipTable.toFront();
    }
}
