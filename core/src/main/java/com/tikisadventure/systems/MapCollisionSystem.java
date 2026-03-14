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

        // Deslizamiento independiente por eje
        float moveX = pos.x - oldPos.x;
        float newX = pos.x;
        if (moveX != 0) {
            boolean collisionX = false;
            if (moveX > 0) { // moviendo a la derecha
                float testX = pos.x + width;
                if (isBlocked(testX, pos.y) || isBlocked(testX, pos.y + height)) {
                    collisionX = true;
                }
            } else { // moviendo a la izquierda
                float testX = pos.x;
                if (isBlocked(testX, pos.y) || isBlocked(testX, pos.y + height)) {
                    collisionX = true;
                }
            }
            if (collisionX) newX = oldPos.x;
        }

        // Deslizamiento vertical independiente
        float moveY = pos.y - oldPos.y;
        float newY = pos.y;
        if (moveY != 0) {
            boolean collisionY = false;
            if (moveY > 0) { // moviendo hacia arriba
                float testY = pos.y + height;
                if (isBlocked(pos.x, testY) || isBlocked(pos.x + width, testY)) {
                    collisionY = true;
                }
            } else { // moviendo hacia abajo
                float testY = pos.y;
                if (isBlocked(pos.x, testY) || isBlocked(pos.x + width, testY)) {
                    collisionY = true;
                }
            }
            if (collisionY) newY = oldPos.y;
        }

        pos.x = newX;
        pos.y = newY;

        // Límites del mapa
        pos.x = MathUtils.clamp(pos.x, 0, collisionLayer.getWidth() - width);
        pos.y = MathUtils.clamp(pos.y, 0, collisionLayer.getHeight() - height);

        e.actualizarHitboxes();
    }
}
