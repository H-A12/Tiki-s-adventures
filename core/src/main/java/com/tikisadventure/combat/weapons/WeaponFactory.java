package com.tikisadventure.combat.weapons;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.tikisadventure.entities.base.Entity;
import com.tikisadventure.entities.player.Player;


public class WeaponFactory {

    private Player player;
    private Array<Weapon> weapons;

    // Distancia de las armas respecto al centro del jugador
    private float radius = 1.3f;

    public WeaponFactory(Player player){
        this.player = player;
        this.weapons = new Array<>();
    }

    public void addWeapon(Weapon weapon){
        weapons.add(weapon);
    }

    public void update(float delta, Array<Entity> enemies){
        // Reposicionamos las armas en cada frame por si el jugador se mueve
        updateWeaponPositions();

        // Actualizamos la lógica interna de cada arma (CD, apuntado, etc.)
        for(Weapon w : weapons){
            w.update(delta, enemies);
        }
    }

    public void render(Batch batch){
        for(Weapon w : weapons){
            w.render(batch);
        }
    }

    /**
     * Calcula la posición de cada arma en un círculo alrededor del jugador.
     * La lógica incluye un desfase para que con 3 armas, las de arriba queden alineadas.
     */
    private void updateWeaponPositions(){
        int total = weapons.size;
        if (total == 0) return;

        float centerX = player.getPosicion().x;
        float centerY = player.getPosicion().y;

        // Espacio angular entre cada arma
        float spacing = MathUtils.PI2 / total;

        for(int i = 0; i < total; i++){
            Weapon w = weapons.get(i);

            // FÓRMULA DE ALINEACIÓN SIMÉTRICA:
            // 1. MathUtils.PI / 2f nos sitúa en el eje vertical (Arriba).
            // 2. Sumamos (i * spacing) para distribuir el resto.
            // 3. Restamos (spacing / 2f) para que el grupo rote y queden niveladas.
            float angle = (i * spacing) + (MathUtils.PI / 2f) - (spacing / 2f);

            float x = centerX + MathUtils.cos(angle) * radius;
            float y = centerY + MathUtils.sin(angle) * radius;

            w.setPosition(x, y);
        }
    }

    public Array<Weapon> getWeapons(){
        return weapons;
    }

    /**
     * Limpia la lista de armas. Útil al cambiar de personaje o reiniciar nivel.
     */
    public void clear() {
        weapons.clear();
    }
}
