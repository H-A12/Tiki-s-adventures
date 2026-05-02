package com.tikisadventure.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
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
        // Aumentamos el padding del contenido principal
        content.pad(30);

        Label title = new Label("¡LEVEL UP!", skin);
        // Título más grande
        title.setFontScale(2.5f);
        content.add(title).padBottom(40).row();

        Table optionsTable = new Table();

        for (PowerUp opcion : opciones) {
            // --- CARTA MÁS GRANDE (Proporción 3:4 mantenida) ---
            // Aumentamos de 180x240 a 240x320
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

    private Button powerUpCardButton(PowerUp powerUpElegido, Player player) {
        String titulo = powerUpElegido.getName();
        String desc = powerUpElegido.getDescription();
        String rareza = powerUpElegido.getRarity().name();

        String texturePath = "";
        switch (powerUpElegido.getRarity()) {
            case COMUN: texturePath = "powerUps_assets/powerUpCommonTemplate"; break;
            case RARO: texturePath = "powerUps_assets/powerUpRareTemplate"; break;
            case ESPECIAL: texturePath = "powerUps_assets/powerUpEspecialTemplate"; break;
            case EPICO: texturePath = "powerUps_assets/PowerUpEpicTemplate"; break;
            case LEGENDARIO: texturePath = "powerUps_assets/PowerUpLegendaryTemplate"; break;
        }

        TextureRegion cardRegion = Assets.getRegion("shared", texturePath);
        TextureRegionDrawable cardDrawable = new TextureRegionDrawable(cardRegion);

        final Button card = new Button(cardDrawable);

        card.padLeft(16).padRight(16).padTop(14).padBottom(14);

        // --- MAGIA VISUAL (Game Feel) ---
        // 1. Permitimos que el botón sea escalable y rotable
        card.setTransform(true);
        // 2. Establecemos el "centro" de la carta para que no escale desde una esquina.
        // Como nuestras cartas son de 240x320, el centro es 120 de ancho y 160 de alto.
        card.setOrigin(120, 160);

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

        card.add(nameLabel).top().expandX().fillX().height(55).row();
        card.add().expand().fill().row();
        card.add(descLabel).bottom().expandX().fillX().height(80).padBottom(8).row();
        card.add(rarityLabel).bottom().expandX().fillX().height(35);

        // --- EVENTOS DEL RATÓN ---
        card.addListener(new com.badlogic.gdx.scenes.scene2d.utils.ClickListener() {

            // Cuando el ratón ENTRA en la carta
            // Cuando el ratón ENTRA en la carta
            @Override
            public void enter(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y, int pointer, com.badlogic.gdx.scenes.scene2d.Actor fromActor) {
                super.enter(event, x, y, pointer, fromActor);
                if (pointer == -1) {
                    card.clearActions();

                    card.addAction(com.badlogic.gdx.scenes.scene2d.actions.Actions.parallel(
                        // Escala más suave
                        com.badlogic.gdx.scenes.scene2d.actions.Actions.scaleTo(1.05f, 1.05f, 0.2f, com.badlogic.gdx.math.Interpolation.fade),

                        // Balanceo infinito, lento y fluido
                        com.badlogic.gdx.scenes.scene2d.actions.Actions.forever(
                            com.badlogic.gdx.scenes.scene2d.actions.Actions.sequence(
                                // Usamos Interpolation.smooth que hace exactamente ese efecto suave
                                com.badlogic.gdx.scenes.scene2d.actions.Actions.rotateTo(2.0f, 0.8f, com.badlogic.gdx.math.Interpolation.smooth),
                                com.badlogic.gdx.scenes.scene2d.actions.Actions.rotateTo(-2.0f, 0.8f, com.badlogic.gdx.math.Interpolation.smooth)
                            )
                        )
                    ));
                }
            }

            // Cuando el ratón SALE de la carta
            @Override
            public void exit(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y, int pointer, com.badlogic.gdx.scenes.scene2d.Actor toActor) {
                super.exit(event, x, y, pointer, toActor);
                if (pointer == -1) {
                    card.clearActions();

                    // Vuelve a su estado original de forma suave y elegante
                    card.addAction(com.badlogic.gdx.scenes.scene2d.actions.Actions.parallel(
                        com.badlogic.gdx.scenes.scene2d.actions.Actions.scaleTo(1f, 1f, 0.2f, com.badlogic.gdx.math.Interpolation.fade),
                        com.badlogic.gdx.scenes.scene2d.actions.Actions.rotateTo(0f, 0.2f, com.badlogic.gdx.math.Interpolation.fade)
                    ));
                }
            }
            // Cuando haces CLIC en la carta
            @Override
            public void clicked(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y) {
                powerUpElegido.apply(player);
                if (onChoiceMade != null) {
                    onChoiceMade.run();
                }
            }
        });

        return card;
    }
}
