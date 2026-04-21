package com.tikisadventure.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;

public class LevelUpUI extends Window {

    private final Runnable onChoiceMade;
    private final Skin skin;

    public LevelUpUI(Skin skin, Runnable onChoiceMade) {
        super("", skin);
        this.skin = skin;
        this.onChoiceMade = onChoiceMade;

        setModal(true);
        setMovable(false);

        // Fondo algo oscuro para resaltar ventana levelUp
        this.setBackground(skin.newDrawable("white", new Color(0, 0, 0, 0.5f)));

        setupLayout();
    }

    private void setupLayout() {
        clearChildren();

        Table content = new Table();
        content.pad(20);

        Label title = new Label("¡LEVEL UP!", skin);
        title.setFontScale(2f);
        content.add(title).padBottom(30).row();

        // Tabla para contenido
        Table optionsTable = new Table();

        // Tablas ejemplo (de momento solo texto)
        optionsTable.add(powerUpCardButton("Más Daño", "Aumenta el daño un 10%")).pad(10);
        optionsTable.add(powerUpCardButton("Más Vida", "Cura 20 HP y sube el máximo")).pad(10);
        optionsTable.add(powerUpCardButton("Velocidad", "Moverse un 5% más rápido")).pad(10);

        content.add(optionsTable).padBottom(20).row();

        add(content);
        pack();
    }

    private Button powerUpCardButton(String name, String desc) {
        TextButton card = new TextButton(name + "\n\n" + desc, skin);

        card.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                // Aplicar eleccion jugador
                Gdx.app.log("LEVEL_UP", "Elegido: " + name);

                // Eleccion del jugador terminada
                if (onChoiceMade != null) onChoiceMade.run();
            }
        });

        return card;
    }

    public void show(float stageWidth, float stageHeight) {
        setVisible(true);
        toFront();
        setPosition(
            (stageWidth - getWidth()) / 2f,
            (stageHeight - getHeight()) / 2f
        );
        Gdx.input.setInputProcessor(getStage());
    }
}
