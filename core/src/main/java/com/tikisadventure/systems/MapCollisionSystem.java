package com.tikisadventure.systems;

import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.tikisadventure.entities.Entity;

public class MapCollisionSystem {

    private TiledMapTileLayer collisionLayer;

    public MapCollisionSystem(TiledMapTileLayer layer){
        this.collisionLayer = layer;
    }

    private boolean isBlocked(float x, float y){

        int tileX = (int)x;
        int tileY = (int)y;

        if(tileX < 0 || tileY < 0 ||
            tileX >= collisionLayer.getWidth() ||
            tileY >= collisionLayer.getHeight()){
            return true;
        }

        TiledMapTileLayer.Cell cell = collisionLayer.getCell(tileX, tileY);

        return cell != null;
    }

    public void resolve(Entity e, Vector2 oldPos){

        Vector2 pos = e.getPosicion();
        
        float width = e.getANCHO();
        float height = e.getALTO();

        float moveX = pos.x - oldPos.x;
        float moveY = pos.y - oldPos.y;

        // Resolver X primero
        float newX = pos.x;
        if (moveX != 0) {
            boolean collisionX = false;
            
            if (moveX > 0) {
                // Moviendo derecha - verificar borde derecho
                float testX = pos.x + width;
                float testY1 = pos.y + 0.1f;
                float testY2 = pos.y + height - 0.1f;
                if (isBlocked(testX, testY1) || isBlocked(testX, testY2)) {
                    collisionX = true;
                }
            } else {
                // Moviendo izquierda - verificar borde izquierdo
                float testX = pos.x;
                float testY1 = pos.y + 0.1f;
                float testY2 = pos.y + height - 0.1f;
                if (isBlocked(testX, testY1) || isBlocked(testX, testY2)) {
                    collisionX = true;
                }
            }
            
            if (collisionX) {
                newX = oldPos.x;
            }
        }

        // Resolver Y usando newX (la posición X ya resuelta)
        float newY = pos.y;
        if (moveY != 0) {
            boolean collisionY = false;
            
            if (moveY > 0) {
                // Moviendo arriba - verificar borde superior
                float testX1 = newX + 0.1f;
                float testX2 = newX + width - 0.1f;
                float testY = pos.y + height;
                if (isBlocked(testX1, testY) || isBlocked(testX2, testY)) {
                    collisionY = true;
                }
            } else {
                // Moviendo abajo - verificar borde inferior
                float testX1 = newX + 0.1f;
                float testX2 = newX + width - 0.1f;
                float testY = pos.y;
                if (isBlocked(testX1, testY) || isBlocked(testX2, testY)) {
                    collisionY = true;
                }
            }
            
            if (collisionY) {
                newY = oldPos.y;
            }
        }

        // Aplicar nuevas posiciones
        pos.x = newX;
        pos.y = newY;

        // Límites del mapa
        pos.x = MathUtils.clamp(pos.x, 0, collisionLayer.getWidth() - width);
        pos.y = MathUtils.clamp(pos.y, 0, collisionLayer.getHeight() - height);

        e.actualizarHitboxes();
    }
}
