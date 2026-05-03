package com.tikisadventure.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.tikisadventure.core.Assets;
import com.tikisadventure.entities.player.Player;
import com.tikisadventure.systems.powerUps.PowerUp;

public class LevelUpUI extends Window {

    private final Runnable onChoiceMade;
    private final Skin skin;

    public LevelUpUI(Skin skin, Runnable onChoiceMade) {
        super("", skin);
        this.skin = skin;
        this.onChoiceMade = onChoiceMade;

        setModal(true);
        setMovable(false);

        NinePatch ninePatch = new NinePatch(skin.getRegion("select"), 4, 4, 0, 24);
        NinePatchDrawable background = new NinePatchDrawable(ninePatch);
        ninePatch.setColor(new Color(0, 0, 0, 0.85f));
        setBackground(background);
    }

    public void show(float stageWidth, float stageHeight, Array<PowerUp> opciones, Player player) {
        clearChildren();

        Table content = new Table();
        content.pad(30);

        Label title = new Label("¡LEVEL UP!", skin);
        title.setFontScale(2.5f);
        content.add(title).padBottom(40).row();

        Table optionsTable = new Table();

        for (PowerUp opcion : opciones) {
            optionsTable.add(powerUpCardButton(opcion, player)).pad(15).width(240).height(320);
        }

        content.add(optionsTable).padBottom(30).row();
        add(content);
        pack();

        setVisible(true);
        toFront();
        setPosition(
            Math.round((stageWidth - getWidth()) / 2f),
            Math.round((stageHeight - getHeight()) / 2f)
        );
        Gdx.input.setInputProcessor(getStage());
    }

    // Diccionario de iconos relación: nombre --> sprite
    private String getIconPath(String powerUpName) {
        switch (powerUpName) {
            case "Tornillos": return "powerUps_assets/commonScrews";
            case "Pilas Triple A": return "powerUps_assets/commonBatteries";
            case "Petardos": return "powerUps_assets/commonFirecrackers";
            case "Salsa picante": return "powerUps_assets/commonHotSauce";
            case "Huevo podrido": return "powerUps_assets/commonRottenEgg";
            case "Frigopie": return "powerUps_assets/commonFootIceCream";
            case "Golosinas": return "powerUps_assets/commonCandy";
            case "Libro de mates": return "powerUps_assets/commonMathsBook";
            case "Sobre de azúcar": return "powerUps_assets/commonSugarPacket";
            case "Aguja de coser": return "powerUps_assets/commonNeedle";

            case "Llave inglesa": return "powerUps_assets/rareWrench";
            case "Batería": return "powerUps_assets/rareBattery";
            case "Mechero trucado": return "powerUps_assets/rareLighter";

            default: return null;
        }
    }

    private Table powerUpCardButton(final PowerUp powerUpElegido, final Player player) {
        String titulo = powerUpElegido.getName();
        String desc = powerUpElegido.getDescription();
        String rareza = powerUpElegido.getRarity().name();

        String cardPath = "";
        String iconBgPath = "";

        switch (powerUpElegido.getRarity()) {
            case COMUN:
                cardPath = "powerUps_assets/powerUpCommonTemplate";
                iconBgPath = "powerUps_assets/iconCommonTemplate";
                break;
            case RARO:
                cardPath = "powerUps_assets/powerUpRareTemplate";
                iconBgPath = "powerUps_assets/iconRareTemplate";
                break;
            case ESPECIAL:
                cardPath = "powerUps_assets/powerUpEspecialTemplate";
                iconBgPath = "powerUps_assets/iconEspecialTemplate";
                break;
            case EPICO:
                cardPath = "powerUps_assets/PowerUpEpicTemplate";
                iconBgPath = "powerUps_assets/iconEpicTemplate";
                break;
            case LEGENDARIO:
                cardPath = "powerUps_assets/PowerUpLegendaryTemplate";
                iconBgPath = "powerUps_assets/iconLegendaryTemplate";
                break;
        }

        final Table cardGroup = new Table();
        cardGroup.setTouchable(Touchable.enabled);
        cardGroup.setSize(240, 320);
        cardGroup.setTransform(true);
        cardGroup.setOrigin(120, 160);

        Stack layers = new Stack();
        layers.setFillParent(true);

        // --- CAPA 1 (Fondo Absoluto): El recuadro liso del Icono ---
        Table layer1_IconBg = new Table();
        Image bgIcon = new Image(Assets.getRegion("shared", iconBgPath));
        layer1_IconBg.add(bgIcon).width(140).height(140).padBottom(60);

        // --- CAPA 2 (Medio-Fondo): El dibujo del PowerUp en sí ---
        Table layer2_ItemIcon = new Table();
        String itemIconPath = getIconPath(titulo); // Buscamos si tiene icono

        if (itemIconPath != null) {
            TextureRegion itemRegion = Assets.getRegion("shared", itemIconPath);
            if (itemRegion != null) {
                Image itemImg = new Image(itemRegion);
                // Si el fondo mide 140 (18x18), el objeto de (16x16) medirá ~125.
                // Le damos el mismo padBottom(60) para que se dibuje exactamente en el centro del fondo liso.
                layer2_ItemIcon.add(itemImg).width(90).height(90).padBottom(60);
            }
        }

        // --- CAPA 3 (Frente): El marco de la Carta con el agujero ---
        Image layer3_cardFrame = new Image(Assets.getRegion("shared", cardPath));

        // --- CAPA 4 (Primerísimo plano): Los Textos ---
        Table layer4_Texts = new Table();
        layer4_Texts.padLeft(16).padRight(16).padTop(14).padBottom(14);

        Label nameLabel = new Label(titulo, skin);
        nameLabel.setAlignment(Align.center);
        nameLabel.setWrap(true);
        nameLabel.setFontScale(1.1f);

        Label descLabel = new Label(desc, skin);
        descLabel.setAlignment(Align.center);
        descLabel.setWrap(true);
        descLabel.setFontScale(0.85f);

        Label rarityLabel = new Label(rareza, skin);
        rarityLabel.setAlignment(Align.center);
        rarityLabel.setFontScale(0.9f);

        switch (powerUpElegido.getRarity()) {
            case COMUN: rarityLabel.setColor(Color.LIGHT_GRAY); break;
            case RARO: rarityLabel.setColor(Color.GREEN); break;
            case ESPECIAL: rarityLabel.setColor(Color.CYAN); break;
            case EPICO: rarityLabel.setColor(Color.PURPLE); break;
            case LEGENDARIO: rarityLabel.setColor(Color.GOLD); break;
        }

        layer4_Texts.add(nameLabel).top().expandX().fillX().height(55).row();
        layer4_Texts.add().expand().fill().row();
        layer4_Texts.add(descLabel).bottom().expandX().fillX().height(80).padBottom(8).row();
        layer4_Texts.add(rarityLabel).bottom().expandX().fillX().height(35);

        // --- AÑADIMOS LAS CAPAS AL STACK (ESTRICTO ORDEN VISUAL) ---
        layers.add(layer1_IconBg);      // 1. Fondo liso coloreado
        layers.add(layer2_ItemIcon);    // 2. Dibujo del tornillo/golosina/etc (si existe)
        layers.add(layer3_cardFrame);   // 3. Marco de la carta (su agujero tapará lo que sobre)
        layers.add(layer4_Texts);       // 4. Textos en HD

        cardGroup.add(layers).expand().fill();

        // --- EVENTOS DEL RATÓN Y ANIMACIONES ---
        cardGroup.addListener(new com.badlogic.gdx.scenes.scene2d.utils.ClickListener() {
            @Override
            public void enter(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y, int pointer, com.badlogic.gdx.scenes.scene2d.Actor fromActor) {
                super.enter(event, x, y, pointer, fromActor);
                if (pointer == -1) {
                    cardGroup.clearActions();

                    cardGroup.addAction(com.badlogic.gdx.scenes.scene2d.actions.Actions.parallel(
                        com.badlogic.gdx.scenes.scene2d.actions.Actions.scaleTo(1.05f, 1.05f, 0.2f, com.badlogic.gdx.math.Interpolation.fade),
                        com.badlogic.gdx.scenes.scene2d.actions.Actions.forever(
                            com.badlogic.gdx.scenes.scene2d.actions.Actions.sequence(
                                com.badlogic.gdx.scenes.scene2d.actions.Actions.rotateTo(2.0f, 0.8f, com.badlogic.gdx.math.Interpolation.smooth),
                                com.badlogic.gdx.scenes.scene2d.actions.Actions.rotateTo(-2.0f, 0.8f, com.badlogic.gdx.math.Interpolation.smooth)
                            )
                        )
                    ));
                }
            }

            @Override
            public void exit(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y, int pointer, com.badlogic.gdx.scenes.scene2d.Actor toActor) {
                super.exit(event, x, y, pointer, toActor);
                if (pointer == -1) {
                    cardGroup.clearActions();

                    cardGroup.addAction(com.badlogic.gdx.scenes.scene2d.actions.Actions.parallel(
                        com.badlogic.gdx.scenes.scene2d.actions.Actions.scaleTo(1f, 1f, 0.2f, com.badlogic.gdx.math.Interpolation.fade),
                        com.badlogic.gdx.scenes.scene2d.actions.Actions.rotateTo(0f, 0.2f, com.badlogic.gdx.math.Interpolation.fade)
                    ));
                }
            }

            @Override
            public void clicked(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y) {
                powerUpElegido.apply(player);
                if (onChoiceMade != null) {
                    onChoiceMade.run();
                }
            }
        });

        return cardGroup;
    }
}
