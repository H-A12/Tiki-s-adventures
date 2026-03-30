package com.tikisadventure.combat.weapons;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.tikisadventure.entities.base.Entity;

public class WeaponManager {

    private final Entity owner;
    private final Array<Weapon> weapons;

    private float orbitDistance = 1.3f;

    // 180 grados en radianes = Izquierda (Oeste)
    private final float START_ANGLE = MathUtils.PI;

    public WeaponManager(Entity owner) {
        this.owner = owner;
        this.weapons = new Array<>();
    }

    public void addWeapon(Weapon weapon) {
        weapon.setOwner(owner);
        weapons.add(weapon);
        // Recalculamos anclajes inmediatamente al añadir
        updateWeaponAnchors();
    }

    public void update(float delta, Array<Entity> enemies) {
        if (weapons.size == 0) return;

        // Mantiene las armas pegadas al jugador en sus posiciones relativas
        updateWeaponAnchors();

        for (Weapon w : weapons) {
            w.update(delta, enemies);
        }
    }

    /**
     * Distribución estática basada en el número de armas.
     */
    private void updateWeaponAnchors() {
        int total = weapons.size;
        if (total == 0) return;

        float centerX = owner.getPosicion().x;
        float centerY = owner.getPosicion().y;

        // Reparto equitativo del círculo
        float spacing = MathUtils.PI2 / total;

        for (int i = 0; i < total; i++) {
            Weapon w = weapons.get(i);

            // Calculamos el ángulo: empezamos en la izquierda (PI)
            // y sumamos el espacio según el índice.
            float angle = START_ANGLE + (i * spacing);

            float x = centerX + MathUtils.cos(angle) * orbitDistance;
            float y = centerY + MathUtils.sin(angle) * orbitDistance;

            w.setPosition(x, y);
        }
    }

    public void render(Batch batch) {
        for (Weapon w : weapons) {
            w.render(batch);
        }
    }

    public void setOrbitDistance(float distance) { this.orbitDistance = distance; }
    public Array<Weapon> getWeapons() { return weapons; }
}
