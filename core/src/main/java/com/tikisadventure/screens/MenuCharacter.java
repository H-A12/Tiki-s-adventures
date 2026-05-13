package com.tikisadventure.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.DragListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.Scaling;
import com.tikisadventure.core.GameSession;
import com.tikisadventure.core.Assets;

public class MenuCharacter extends Window {

    private static class SpinnableActor extends com.badlogic.gdx.scenes.scene2d.Actor {
        private final Animation<TextureRegion> anim;
        private float stateTime = 0f;
        public float spinVelocity = 0f;

        public SpinnableActor(Animation<TextureRegion> anim) {
            this.anim = anim;
            setSize(120, 120);
            setOrigin(Align.center);
        }

        @Override
        public void act(float delta) {
            super.act(delta);
            stateTime += delta;
            setRotation(getRotation() + spinVelocity * delta);
            spinVelocity *= 0.99f;
            if (Math.abs(spinVelocity) < 1f) spinVelocity = 0;
        }

        @Override
        public void draw(Batch batch, float parentAlpha) {
            TextureRegion frame = anim.getKeyFrame(stateTime, true);
            Color color = getColor();
            batch.setColor(color.r, color.g, color.b, color.a * parentAlpha);
            batch.draw(frame, getX(), getY(), getOriginX(), getOriginY(),
                getWidth(), getHeight(), getScaleX(), getScaleY(), getRotation());
            batch.setColor(Color.WHITE);
        }
    }

    public MenuCharacter(String title, Skin skin, final String characterId, Animation<TextureRegion> animacion, final Runnable onSelected) {
        super(title, skin);
        setModal(true);
        setMovable(false);
        // Fondo gris claro
        setColor(new Color(0.85f, 0.85f, 0.85f, 0.95f));

        Table mainTable = new Table();
        mainTable.pad(20);

        final SpinnableActor charActor = new SpinnableActor(animacion);
        charActor.addListener(new DragListener() {
            @Override
            public void drag(InputEvent event, float x, float y, int pointer) {
                charActor.spinVelocity += getDeltaX() * -2f;
            }
        });

        String nombreMostrado = "Tiki";
        String idSeguro = characterId.toLowerCase();
        if (idSeguro.contains("2") || idSeguro.contains("moko")) {
            nombreMostrado = "Moko";
        } else if (idSeguro.contains("3") || idSeguro.contains("zuki") || idSeguro.contains("fuki")) {
            nombreMostrado = "Zuki";
        }

        Label nameLabel = new Label(nombreMostrado, skin, "font-27");
        nameLabel.setAlignment(Align.center);
        // Reducimos la fuente del nombre un 30%
        nameLabel.setFontScale(0.7f);

        // Cambiamos "Seleccionar" por "Elegir"
        TextButton btnElegir = new TextButton("Elegir", skin);
        TextButton btnVolver = new TextButton("Volver", skin);

        btnElegir.addListener(new Assets.HoverCursorListener());
        btnElegir.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                GameSession.selectedCharacterId = characterId;
                if (onSelected != null) onSelected.run();
                remove();
            }
        });

        btnVolver.addListener(new Assets.HoverCursorListener());
        btnVolver.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                remove();
            }
        });

        // --- Leer stats del personaje ---
        float charHealth = 20f, charSpeed = 5.5f;
        float minHealth = Float.MAX_VALUE, maxHealth = 0f;
        float minSpeed  = Float.MAX_VALUE, maxSpeed = 0f;

        try {
            JsonValue config = new JsonReader().parse(Gdx.files.internal("data/player_config.json"));
            JsonValue characters = config.get("characters");

            for (JsonValue c : characters) {
                String id = c.getString("id", "");
                if (id.equalsIgnoreCase("TikiBot")) continue;
                float hp = c.getFloat("maxHealth", 0);
                float sp = c.getFloat("speed", 0);
                if (hp < minHealth) minHealth = hp;
                if (hp > maxHealth) maxHealth = hp;
                if (sp < minSpeed)  minSpeed  = sp;
                if (sp > maxSpeed)  maxSpeed  = sp;
                if (id.equalsIgnoreCase(characterId)) {
                    charHealth = c.getFloat("maxHealth", 20f);
                    charSpeed = c.getFloat("speed", 5.5f);
                }
            }
        } catch (Exception ignored) {}

        // Variables de rangos necesarias
        float rangeH = maxHealth - minHealth;
        float rangeS = maxSpeed - minSpeed;

        int healthLevel = MathUtils.clamp(Math.round(rangeH > 0 ? 1f + 3f * (charHealth - minHealth) / rangeH : 2), 1, 4);
        int speedLevel  = MathUtils.clamp(Math.round(rangeS > 0 ? 1f + 3f * (charSpeed  - minSpeed)  / rangeS : 2), 1, 4);

        TextureRegion healthRegion = Assets.getRegion("shared", "UI_assets/statCharacterBar" + healthLevel);
        TextureRegion speedRegion  = Assets.getRegion("shared", "UI_assets/statCharacterBar" + speedLevel);

        Image healthBarImg = new Image(healthRegion);
        healthBarImg.setScaling(Scaling.fit);
        healthBarImg.setColor(Color.RED);

        Image speedBarImg = new Image(speedRegion);
        speedBarImg.setScaling(Scaling.fit);
        speedBarImg.setColor(Color.YELLOW);

        float UI_SCALE = 3.5f;
        float finalBarW = healthRegion.getRegionWidth() * UI_SCALE;
        float finalBarH = healthRegion.getRegionHeight() * UI_SCALE;

        // --- Layout Principal ---
        Table contentTable = new Table();

        Table leftTable = new Table();
        leftTable.add(charActor).size(120, 120).row();
        // Aumentamos el margen superior para despegar el texto del personaje
        leftTable.add(nameLabel).padTop(12);

        Label saludLabel = new Label("Salud:", skin);
        saludLabel.setFontScale(0.8f);
        Label velLabel = new Label("Velocidad:", skin);
        velLabel.setFontScale(0.8f);

        Table rightTable = new Table();

        // Alineamos los labels a la izquierda y aplicamos la sangría (padLeft)
        rightTable.add(saludLabel).align(Align.left).padLeft(5).padBottom(0).row();
        rightTable.add(healthBarImg).size(finalBarW, finalBarH).padBottom(15).row();

        rightTable.add(velLabel).align(Align.left).padLeft(5).padBottom(0).row();
        rightTable.add(speedBarImg).size(finalBarW, finalBarH).row();

        contentTable.add(leftTable).padRight(35);
        contentTable.add(rightTable);

        Table buttonTable = new Table();
        // Botón Elegir primero a la izquierda
        buttonTable.add(btnElegir).size(110, 35).padRight(10);
        buttonTable.add(btnVolver).size(110, 35);

        mainTable.add(contentTable).expand().center().row();
        mainTable.add(buttonTable).expandX().center().padTop(25);

        add(mainTable).pad(10);
        pack();
    }
}
