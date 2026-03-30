package com.tikisadventure.combat.projectiles;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.utils.Array;

public class ProjectileManager {
    private final Array<Projectile> projectiles = new Array<>();

    public void add(Projectile p) {
        projectiles.add(p);
    }

    public void update(float delta) {
        // Recorremos hacia atrás para poder eliminar proyectiles muertos de forma segura
        for (int i = projectiles.size - 1; i >= 0; i--) {
            Projectile p = projectiles.get(i);
            p.update(delta);
            if (!p.isAlive()) {
                projectiles.removeIndex(i);
            }
        }
    }

    /**
     * Sincronizado con la firma de Projectile.render(Batch, float)
     */
    public void render(Batch batch, float delta) {
        for (Projectile p : projectiles) {
            p.render(batch, delta);
        }
    }

    public Array<Projectile> getProjectiles() {
        return projectiles;
    }
}
