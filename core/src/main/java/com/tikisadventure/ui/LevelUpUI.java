package com.tikisadventure.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.Array; // IMPORTANTE: Importamos Array de LibGDX
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
        this.setBackground(skin.newDrawable("white", new Color(0, 0, 0, 0.8f)));

        // Ya NO llamamos a setupLayout() aquí
    }

    // ACTUALIZAMOS el método show para recibir las opciones y el jugador
    public void show(float stageWidth, float stageHeight, Array<PowerUp> opciones, Player player) {
        clearChildren(); // Limpiamos la ventana (por si había cartas del nivel anterior)

        Table content = new Table();
        content.pad(20);

        Label title = new Label("¡LEVEL UP!", skin);
        title.setFontScale(2f);
        content.add(title).padBottom(30).row();

        Table optionsTable = new Table();

        // ¡LA MAGIA AUTOMÁTICA! Creamos una carta por cada opción que nos pase la ruleta
        for (PowerUp opcion : opciones) {
            optionsTable.add(powerUpCardButton(opcion, player)).pad(10).width(200).height(150);
        }

        content.add(optionsTable).padBottom(20).row();
        add(content);
        pack(); // Ajusta la ventana al tamaño de las nuevas cartas

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
        String rareza = powerUpElegido.getRarity().name(); // Extra: Mostramos la rareza

        // Le damos un poco de formato al botón
        TextButton card = new TextButton("[" + rareza + "]\n" + titulo + "\n\n" + desc, skin);

        // Opcional: Hacer que el texto se ajuste (wrap) si es muy largo
        card.getLabel().setWrap(true);

        card.addListener(new com.badlogic.gdx.scenes.scene2d.utils.ClickListener() {
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
