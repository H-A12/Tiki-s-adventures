package com.tikisadventure.screens;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.DragListener;
import com.badlogic.gdx.utils.Align;
import com.tikisadventure.core.GameSession;
import com.tikisadventure.core.Assets;

public class MenuCharacter extends Window {

    // --- EL ACTOR RULETA ---
    private static class SpinnableActor extends com.badlogic.gdx.scenes.scene2d.Actor {
        private final Animation<TextureRegion> anim;
        private float stateTime = 0f;
        public float spinVelocity = 0f; // Velocidad de giro

        public SpinnableActor(Animation<TextureRegion> anim) {
            this.anim = anim;
            setSize(200, 200); // Tamaño grande para el modal
            setOrigin(Align.center); // Gira desde el centro
        }

        @Override
        public void act(float delta) {
            super.act(delta);
            stateTime += delta;

            // Aplicamos rotación
            setRotation(getRotation() + spinVelocity * delta);

            // LA MAGIA DE LA RULETA: 0.99f significa que pierde solo un 1% de velocidad por frame.
            // Esto hace que tarde muchísimo en detenerse, como una ruleta de casino.
            spinVelocity *= 0.99f;

            // Si la velocidad es ínfima, lo paramos por completo
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

    // Constructor del Modal
    public MenuCharacter(String title, Skin skin, final String characterId, Animation<TextureRegion> animacion, final Runnable onSelected) {
        super(title, skin);
        setModal(true); // Bloquea los clics fuera de la ventana
        setMovable(false); // Evita que el usuario la arrastre por el título

        // Le damos un fondo semi-transparente oscuro al estilo
        setColor(new Color(0.15f, 0.15f, 0.15f, 0.95f));

        Table mainTable = new Table();
        mainTable.pad(30);

        // --- 1. PERSONAJE (LA RULETA) ---
        final SpinnableActor charActor = new SpinnableActor(animacion);
        charActor.addListener(new DragListener() {
            @Override
            public void drag(InputEvent event, float x, float y, int pointer) {
                // Al arrastrar, le damos impulso. (Usamos -2f para calibrar la fuerza)
                charActor.spinVelocity += getDeltaX() * -2f;
            }
        });

        // Nombre dinámico
        String nombreMostrado = "Tiki"; // Por defecto

        // Pasamos el ID a minúsculas para que no importe si en el JSON pusiste "Moko", "MOKO" o "moko"
        String idSeguro = characterId.toLowerCase();

        if (idSeguro.contains("2") || idSeguro.contains("moko")) {
            nombreMostrado = "Moko";
        } else if (idSeguro.contains("3") || idSeguro.contains("zuki") || idSeguro.contains("fuki")) {
            nombreMostrado = "Zuki";
        }

        Label nameLabel = new Label(nombreMostrado, skin, "font-27");
        nameLabel.setAlignment(Align.center);

        // --- 2. BOTONES ---
        TextButton btnSeleccionar = new TextButton("Seleccionar", skin);
        TextButton btnVolver = new TextButton("Volver", skin);

        btnSeleccionar.addListener(new Assets.HoverCursorListener());
        btnSeleccionar.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                GameSession.selectedCharacterId = characterId;
                // Ejecutamos el código para actualizar el color en el menú de atrás
                if (onSelected != null) onSelected.run();
                remove(); // Destruye el modal
            }
        });

        btnVolver.addListener(new Assets.HoverCursorListener());
        btnVolver.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                remove(); // Destruye el modal sin hacer nada más
            }
        });

        // --- 3. MAQUETACIÓN FINAL ---
        Table leftTable = new Table();
        leftTable.add(charActor).size(200, 200).row();
        leftTable.add(nameLabel).padTop(10);

        Table rightTable = new Table();
        rightTable.add(btnSeleccionar).size(180, 60).padBottom(20).row();
        rightTable.add(btnVolver).size(180, 60);

        mainTable.add(leftTable).padRight(40);
        mainTable.add(rightTable);

        add(mainTable);
        pack(); // Ajusta la ventana al tamaño de su contenido
    }
}
