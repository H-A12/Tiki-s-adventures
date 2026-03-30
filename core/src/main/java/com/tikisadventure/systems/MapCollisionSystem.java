package com.tikisadventure.systems;

import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.tikisadventure.entities.base.Entity;

public class MapCollisionSystem {

    private TiledMapTileLayer collisionLayer;
    // Un vector temporal para no crear objetos nuevos cada frame (ahorro de memoria)
    private final Vector2 oldPos = new Vector2();

    public MapCollisionSystem(TiledMapTileLayer layer){
        this.collisionLayer = layer;
    }

    private boolean isBlocked(float x, float y){
        if (collisionLayer == null) return false;

        int tileX = (int)x;
        int tileY = (int)y;

        if(tileX < 0 || tileY < 0 ||
            tileX >= collisionLayer.getWidth() ||
            tileY >= collisionLayer.getHeight()){
            return true;
        }

        TiledMapTileLayer.Cell cell = collisionLayer.getCell(tileX, tileY);
        return cell != null; // Si hay un tile en la capa de colisión, está bloqueado
    }

    /**
     * CORRECCIÓN: Ahora acepta 'delta' en lugar de 'oldPos'
     */
    public void resolve(Entity e, float delta){
        if (collisionLayer == null) return;

        Vector2 pos = e.getPosicion();
        float width = e.getANCHO();
        float height = e.getALTO();

        // 1. Calculamos la posición anterior restando el movimiento de este frame
        // Esto asume que la entidad ya se movió en su método update()
        oldPos.set(pos.x - (e.getVelocidad().x * delta),
            pos.y - (e.getVelocidad().y * delta));

        float moveX = pos.x - oldPos.x;
        float moveY = pos.y - oldPos.y;

        // 2. Resolver eje X
        if (moveX != 0) {
            boolean collisionX = false;
            float testX = (moveX > 0) ? pos.x + width : pos.x;

            // Verificamos dos puntos en el borde (superior e inferior del sprite)
            if (isBlocked(testX, oldPos.y + 0.1f) ||
                isBlocked(testX, oldPos.y + height - 0.1f)) {
                collisionX = true;
            }

            if (collisionX) {
                pos.x = oldPos.x; // Rebotar a la posición X anterior
            }
        }

        // 3. Resolver eje Y (usando la X ya validada)
        if (moveY != 0) {
            boolean collisionY = false;
            float testY = (moveY > 0) ? pos.y + height : pos.y;

            // Verificamos dos puntos en el borde (izquierdo y derecho del sprite)
            if (isBlocked(pos.x + 0.1f, testY) ||
                isBlocked(pos.x + width - 0.1f, testY)) {
                collisionY = true;
            }

            if (collisionY) {
                pos.y = oldPos.y; // Rebotar a la posición Y anterior
            }
        }

        // 4. Límites del mapa (Clamp)
        pos.x = MathUtils.clamp(pos.x, 0, collisionLayer.getWidth() - width);
        pos.y = MathUtils.clamp(pos.y, 0, collisionLayer.getHeight() - height);

        // Muy importante: sincronizar las hitboxes con la posición final corregida
        e.actualizarHitboxes();
    }

    public void setLayer(TiledMapTileLayer layer) {
        this.collisionLayer = layer;
    }
}
