package com.tikisadventure.projectile.behaviors;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.tikisadventure.combat.Weapon;
import com.tikisadventure.entities.Entity;
import com.tikisadventure.entities.player.Player;
import com.tikisadventure.projectile.Projectile;
import com.tikisadventure.projectile.ProjectileBehavior;

public class ShrapnelBehavior implements ProjectileBehavior {

    private Weapon.ProjectileCreator creator;
    private TextureRegion texture; // Se generará internamente
    private int count;
    private float shrapnelDamage;
    private float size;
    private boolean hasSpawned = false;

    // AHORA RECIBE UN STRING (Ruta del asset)
    public ShrapnelBehavior(Weapon.ProjectileCreator creator, String internalPath, int count, float damage, float size) {
        this.creator = creator;
        this.count = count;
        this.shrapnelDamage = damage;
        this.size = size;

        // CARGA AUTOMÁTICA: El behavior se encarga de buscar el archivo
        try {
            Texture temp = new Texture(internalPath);
            this.texture = new TextureRegion(temp);
        } catch (Exception e) {
            System.err.println("Error cargando textura de metralla: " + internalPath);
        }
    }

    @Override
    public void update(Projectile p, float delta, Array<Entity> enemies) {
        if (!p.isAlive() && !hasSpawned) {
            hasSpawned = true;
            spawnChaos(p);
        }
    }

    private void spawnChaos(Projectile p) {
        if (texture == null) return; // Seguridad por si falló la carga

        for (int i = 0; i < count; i++) {
            float angle = MathUtils.random(0f, 360f);
            Vector2 dir = new Vector2(1, 0).rotateDeg(angle);
            float speed = MathUtils.random(10f, 22f);

            Projectile s = creator.create(
                new Vector2(p.getPosition()),
                dir,
                speed,
                this.shrapnelDamage,
                size * MathUtils.random(0.8f, 1.2f),
                this.texture, // Usa la textura cargada arriba
                null, null, 0
            );

            s.addBehavior(new StandardPhysicsBehavior());
            s.addBehavior(new LifetimeBehavior(0.4f));

            if (p.getOwner() instanceof Player) {
                ((Player) p.getOwner()).addProjectile(s);
            }
        }
    }
}
