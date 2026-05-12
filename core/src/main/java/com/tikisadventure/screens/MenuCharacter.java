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
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.DragListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.tikisadventure.core.GameSession;
import com.tikisadventure.core.Assets;

public class MenuCharacter extends Window {

    private static class SpinnableActor extends com.badlogic.gdx.scenes.scene2d.Actor {
        private final Animation<TextureRegion> anim;
        private float stateTime = 0f;
        public float spinVelocity = 0f;

        public SpinnableActor(Animation<TextureRegion> anim) {
            this.anim = anim;
            setSize(200, 200);
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
        setColor(new Color(0.15f, 0.15f, 0.15f, 0.95f));

        Table mainTable = new Table();
        mainTable.pad(30);

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

        TextButton btnSeleccionar = new TextButton("Seleccionar", skin);
        TextButton btnVolver = new TextButton("Volver", skin);

        btnSeleccionar.addListener(new Assets.HoverCursorListener());
        btnSeleccionar.addListener(new ClickListener() {
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

        // --- Leer stats del personaje y extremos (excluyendo TikiBot) ---
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

        float rangeH = maxHealth - minHealth;
        float rangeS = maxSpeed - minSpeed;
        int healthBars = MathUtils.clamp(Math.round(rangeH > 0 ? 2f + 3f * (charHealth - minHealth) / rangeH : 3), 0, 5);
        int speedBars  = MathUtils.clamp(Math.round(rangeS > 0 ? 2f + 3f * (charSpeed  - minSpeed)  / rangeS : 3), 0, 5);

        // --- Construir barras de estadísticas ---
        Stack healthBar = createStatBar(healthBars);
        Stack speedBar  = createStatBar(speedBars);

        TextureRegion extRegion = Assets.getRegion("shared", "UI_assets/characterStatExt");
        float extW = extRegion.getRegionWidth();
        float extH = extRegion.getRegionHeight();

        // --- Layout ---
        Table leftTable = new Table();
        leftTable.add(charActor).size(200, 200).row();
        leftTable.add(nameLabel).padTop(10);

        Label saludLabel = new Label("Salud", skin);
        Label velLabel = new Label("Velocidad", skin);

        Table rightTable = new Table();
        rightTable.add(saludLabel).padRight(8).right();
        rightTable.add(healthBar).size(extW, extH).padBottom(6).row();
        rightTable.add(velLabel).padRight(8).padTop(2).right();
        rightTable.add(speedBar).size(extW, extH).padBottom(10).row();
        rightTable.add().expandY().colspan(2).row();
        rightTable.add(btnSeleccionar).colspan(2).size(140, 45).padBottom(8).row();
        rightTable.add(btnVolver).colspan(2).size(140, 45);

        mainTable.add(leftTable).padRight(40);
        mainTable.add(rightTable);

        add(mainTable);
        pack();
    }

    private Stack createStatBar(int filledBars) {
        TextureRegion extRegion = Assets.getRegion("shared", "UI_assets/characterStatExt");
        TextureRegion intRegion = Assets.getRegion("shared", "UI_assets/characterStatInt");

        float extW = extRegion.getRegionWidth();
        float extH = extRegion.getRegionHeight();
        float barW = Math.max(extW / 8f, 3f);
        float barH = extH - 6f;

        Stack stack = new Stack();

        Table barsTable = new Table();
        for (int i = 0; i < 5; i++) {
            if (i < filledBars) {
                barsTable.add(new Image(intRegion)).size(barW, barH).expandX().uniformX().center();
            } else {
                barsTable.add().expandX().uniformX();
            }
        }

        Image extImage = new Image(extRegion);

        stack.add(barsTable);
        stack.add(extImage);

        return stack;
    }
}
