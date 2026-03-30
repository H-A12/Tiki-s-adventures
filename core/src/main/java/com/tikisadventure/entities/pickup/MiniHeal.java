package com.tikisadventure.entities.pickup;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.tikisadventure.assets.Assets;
import com.tikisadventure.entities.base.Entity;
import com.tikisadventure.entities.player.Player;

public class MiniHeal extends Pickup {

    private final TextureRegion region;
    private float healAmount = 15f;

    public MiniHeal(Vector2 position) {
        super(position);

        // 1. Cargamos el asset
        this.region = Assets.getTexture("items/miniheal");

        // 2. IMPORTANTE: Inicializamos la animación para cumplir el contrato de Entity
        // Creamos una animación de un solo frame con el sprite del item
        if (region != null) {
            this.animation = new Animation<>(0.1f, region);
        }

        this.ANCHO = 0.6f;
        this.ALTO = 0.6f;

        actualizarHitboxes();
    }

    /**
     * Implementación obligatoria (heredada de Entity -> Pickup).
     * Como ya definimos 'animation' en Pickup, este método lo usa automáticamente.
     * Si quieres un comportamiento específico, puedes sobreescribirlo aquí.
     */
    @Override
    public Animation<TextureRegion> getAnimationForState(Estado estado) {
        return animation;
    }

    @Override
    protected void onPickup(Entity entity) {
        if (entity instanceof Player) {
            Player player = (Player) entity;

            // Usamos los métodos de Player para curar
            float currentHp = player.getVida();
            float maxHp = player.getVida_max();
            player.setStats(Math.min(currentHp + healAmount, maxHp), player.getProfile().speed, 0, 0);
        }
    }

    @Override
    public void render(Batch batch, float delta) {
        // Usamos el 'sprite' inyectado por el AnimationSystem o la región directa
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
}
