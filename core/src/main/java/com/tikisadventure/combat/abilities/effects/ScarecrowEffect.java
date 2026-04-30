package com.tikisadventure.combat.abilities.effects;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.tikisadventure.entities.base.Entity;
import com.tikisadventure.entities.player.Player;
import com.tikisadventure.entities.gadgets.Scarecrow;
import com.tikisadventure.screens.GameScreen;
import com.tikisadventure.floors.FloorManager;

public class ScarecrowEffect implements AbilityEffect {
    private final float duration;

    public ScarecrowEffect(float duration) {
        this.duration = duration;
    }

    @Override
    public boolean execute(Player owner, Array<Entity> enemies, Vector2 targetPosition) {
        Vector2 safePosition = findSafePosition(targetPosition);

        // Eliminamos el anterior si ya existía uno en el mapa
        if (GameScreen.activeScarecrow != null) {
            GameScreen.activeScarecrow.setAlive(false);
        }

        // Instanciamos el nuevo usando el nombre de tu clase
        GameScreen.activeScarecrow = new Scarecrow(safePosition, duration);
        return true;
    }

    private Vector2 findSafePosition(Vector2 startPos) {
        FloorManager fm = FloorManager.getInstance();
        if (fm == null) return startPos;

        float mapWidth = fm.getCollisionLayer() != null ? fm.getCollisionLayer().getWidth() : 100;
        float mapHeight = fm.getCollisionLayer() != null ? fm.getCollisionLayer().getHeight() : 100;
        float margin = 1.2f;

        Vector2 checkPos = new Vector2(startPos);
        checkPos.x = Math.max(margin, Math.min(mapWidth - margin, checkPos.x));
        checkPos.y = Math.max(margin, Math.min(mapHeight - margin, checkPos.y));

        if (!isOverlappingWall(fm, checkPos.x, checkPos.y)) return checkPos;

        float step = 0.5f;
        for (float r = step; r <= 5.0f; r += step) {
            for (float angle = 0; angle < 360; angle += 45) {
                float rad = (float) Math.toRadians(angle);
                float nx = checkPos.x + (float) Math.cos(rad) * r;
                float ny = checkPos.y + (float) Math.sin(rad) * r;
                nx = Math.max(margin, Math.min(mapWidth - margin, nx));
                ny = Math.max(margin, Math.min(mapHeight - margin, ny));

                if (!isOverlappingWall(fm, nx, ny)) return new Vector2(nx, ny);
            }
        }
        return checkPos;
    }

    private boolean isOverlappingWall(FloorManager fm, float cx, float cy) {
        float offset = 0.55f;
        return fm.isWall(cx, cy) || fm.isWall(cx - offset, cy) || fm.isWall(cx + offset, cy) ||
            fm.isWall(cx, cy - offset) || fm.isWall(cx, cy + offset);
    }
}
