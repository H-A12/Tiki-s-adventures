package com.tikisadventure.entities.pickup;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.tikisadventure.core.Assets;
import com.tikisadventure.entities.base.Entity;
import com.tikisadventure.entities.player.Player;
import com.tikisadventure.systems.events.EventBus;
import com.tikisadventure.systems.events.OrbCollectedEvent;

public class XPOrb extends Pickup {
    private static TextureRegion texture;
    private int value;

    // --- NUEVO: Variables para la atracción ---
    private boolean isBeingAttracted = false;
    private float attractSpeed = 5.0f; // Velocidad a la que vuela hacia el jugador

    public XPOrb() {
        super();
        if (texture == null) texture = Assets.getRegion("shared", "pickup_assets/orbXP");
    }

    public void init(Vector2 position, int value) {
        super.init(position);
        this.value = value;
        this.isBeingAttracted = false; // Resetear al hacer init desde el pool
    }

    // --- Lógica de actualización de movimiento CORREGIDA ---
    @Override
    public void update(float delta, Entity target) {
        if (!isAlive()) return;

        // 1. PRIMERO CALCULAMOS EL MOVIMIENTO (ATRACCIÓN)
        if (target instanceof Player) {
            Player player = (Player) target;

            // Calculamos la distancia al jugador
            float distanceToPlayer = positionComponent.posicion.dst(player.getPosition());

            // Si está dentro del rango de atracción del jugador, empezamos a atraerlo
            if (distanceToPlayer <= player.getAttractionRange()) {
                isBeingAttracted = true;
            }

            // Si está siendo atraído, se mueve hacia el jugador
            if (isBeingAttracted) {
                Vector2 direction = new Vector2(player.getPosition()).sub(positionComponent.posicion).nor();
                positionComponent.posicion.mulAdd(direction, attractSpeed * delta);

                // La velocidad aumenta progresivamente para dar un efecto "imán" chulo
                attractSpeed += 15.0f * delta;
            }
        }

        // 2. DESPUÉS LLAMAMOS AL SUPER PARA COMPROBAR COLISIONES
        // Ahora que ya se ha movido, comprobamos si en esta nueva posición ha tocado al jugador
        super.update(delta, target);
    }

    @Override
    protected void onPickup(Entity entity) {
        if (entity instanceof Player) {
            Player player = (Player) entity;
            int xpFinal = Math.round(this.value * player.getXpMultiplier());

            EventBus.publish(new OrbCollectedEvent(xpFinal));
        }
    }

    @Override
    public void draw(Batch batch, float delta) {
        if (texture == null || !isAlive()) return;
        // Calculamos el tamaño visual con un aumento del 50% (factor 1.5)
        float drawW = getANCHO() * 1.3f;
        float drawH = getALTO() * 1.3f;

        batch.draw(texture,
            positionComponent.posicion.x - drawW / 2,
            positionComponent.posicion.y - drawH / 2,
            drawW,
            drawH
        );
    }

    @Override
    public void reset() {
        super.reset();
        this.value = 0;
        this.isBeingAttracted = false;
        this.attractSpeed = 5.0f;
    }
}
