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

        // colisión eje X
        if(isBlocked(pos.x, oldPos.y)){
            pos.x = oldPos.x;
        }

        // colisión eje Y
        if(isBlocked(oldPos.x, pos.y)){
            pos.y = oldPos.y;
        }

        // 🔹 límite duro del mapa (evita que slimes empujen fuera)
        pos.x = MathUtils.clamp(pos.x, 1, collisionLayer.getWidth() - 2);
        pos.y = MathUtils.clamp(pos.y, 1, collisionLayer.getHeight() - 2);

        e.actualizarHitboxes();
    }
}
