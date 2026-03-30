package com.tikisadventure.entities.pickup;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.tikisadventure.assets.Assets; // Asumo que usas tu clase Assets
import com.tikisadventure.entities.base.Entity;
import com.tikisadventure.entities.player.Player;

public class XPOrb extends Pickup {

    private int value;
    private TextureRegion region;

    public XPOrb(Vector2 position, int value) {
        super(position);
        this.value = value;

        // 1. Cargamos la región desde Assets (más seguro que 'new Texture')
        this.region = Assets.getTexture("items/xp_orb");

        // 2. IMPORTANTE: Inicializamos la animación para que Entity/AnimationSystem funcionen
        if (region != null) {
            this.animation = new Animation<>(0.1f, region);
        }

        // Ajustamos tamaño del orbe
        this.ANCHO = 0.4f;
        this.ALTO = 0.4f;

        actualizarHitboxes();
    }

    /**
     * Implementación obligatoria para que compile.
     * El AnimationSystem llamará a esto para saber qué dibujar.
     */
    @Override
    public Animation<TextureRegion> getAnimationForState(Estado estado) {
        return animation;
    }

    @Override
    protected void onPickup(Entity entity) {
        if (entity instanceof Player) {
            Player player = (Player) entity;

            if (player.getExperienceSystem() != null) {
                player.getExperienceSystem().addXP(value);
                // Aquí podrías añadir un sonido: Assets.playSound("xp_up");
            }

            this.alive = false;
        }
    }

    @Override
    public void render(Batch batch, float delta) {
        // Priorizamos 'sprite' (el frame que inyecta AnimationSystem)
        // Si es nulo, usamos la 'region' original como respaldo
        TextureRegion toDraw = (sprite != null) ? sprite : region;

        if (toDraw == null || !alive) return;

        batch.draw(
            toDraw,
            posicion.x - ANCHO / 2,
            posicion.y - ALTO / 2,
            ANCHO,
            ALTO
        );
    }

    public int getValue() {
        return value;
    }
}
