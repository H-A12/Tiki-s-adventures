package com.tikisadventure.combat.abilities.effects;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.tikisadventure.effects.EffectManager;
import com.tikisadventure.entities.base.Entity;
import com.tikisadventure.entities.player.Player;
import com.tikisadventure.entities.gadgets.SewerMine;
import com.tikisadventure.floors.FloorManager;

public class SpawnMineEffect implements AbilityEffect {
    private final EffectManager effectManager;
    private final float duration;
    private final float radius;
    private final float damage;
    private final String profile;
    private final Array<SewerMine> globalMinesList;

    public SpawnMineEffect(EffectManager em, Array<SewerMine> minesList, float duration, float radius, float damage, String profile) {
        this.effectManager = em;
        this.globalMinesList = minesList;
        this.duration = duration;
        this.radius = radius;
        this.damage = damage;
        this.profile = profile;
    }

    @Override
    public boolean execute(Player owner, Array<Entity> enemies, Vector2 targetPosition) {
        // 1. Encontrar una posición segura (fuera de muros y dentro del mapa)
        Vector2 safePosition = findSafePosition(targetPosition);

        // 2. Creamos la mina en la posición corregida
        SewerMine mine = new SewerMine(effectManager, safePosition, duration, radius, damage, profile);
        globalMinesList.add(mine);

        return true;
    }

    /**
     * Comprueba si la posición objetivo es un muro o está fuera del mapa.
     * Si es inválida, busca en espiral el hueco libre más cercano.
     */
    private Vector2 findSafePosition(Vector2 startPos) {
        FloorManager fm = FloorManager.getInstance();
        if (fm == null) return startPos;

        float mapWidth = fm.getCollisionLayer() != null ? fm.getCollisionLayer().getWidth() : 100;
        float mapHeight = fm.getCollisionLayer() != null ? fm.getCollisionLayer().getHeight() : 100;

        // Aumentamos el margen a 1.2f (tamaño de la mina) para que no se acerque tanto al borde del mapa
        float margin = 1.2f;

        Vector2 checkPos = new Vector2(startPos);
        checkPos.x = Math.max(margin, Math.min(mapWidth - margin, checkPos.x));
        checkPos.y = Math.max(margin, Math.min(mapHeight - margin, checkPos.y));

        if (!isOverlappingWall(fm, checkPos.x, checkPos.y)) {
            return checkPos;
        }

        // BÚSQUEDA RADIAL: Si hemos caído en un muro/árbol, buscamos alrededor
        float step = 0.5f;
        float maxSearchRadius = 5.0f;

        for (float r = step; r <= maxSearchRadius; r += step) {
            for (float angle = 0; angle < 360; angle += 45) {
                float rad = (float) Math.toRadians(angle);
                float nx = checkPos.x + (float) Math.cos(rad) * r;
                float ny = checkPos.y + (float) Math.sin(rad) * r;

                nx = Math.max(margin, Math.min(mapWidth - margin, nx));
                ny = Math.max(margin, Math.min(mapHeight - margin, ny));

                // Usamos el nuevo método de colisión
                if (!isOverlappingWall(fm, nx, ny)) {
                    return new Vector2(nx, ny);
                }
            }
        }

        return checkPos;
    }

    private boolean isOverlappingWall(FloorManager fm, float cx, float cy) {
        // 0.55f es casi la mitad exacta del sprite de la mina (que mide 1.2f).
        // Al probar las 4 direcciones evitamos que la imagen se solape.
        float offset = 0.55f;

        return fm.isWall(cx, cy) ||           // Centro
            fm.isWall(cx - offset, cy) ||  // Izquierda
            fm.isWall(cx + offset, cy) ||  // Derecha
            fm.isWall(cx, cy - offset) ||  // Abajo
            fm.isWall(cx, cy + offset);    // Arriba
    }
}
