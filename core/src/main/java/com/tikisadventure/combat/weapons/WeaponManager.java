package com.tikisadventure.combat.weapons;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.tikisadventure.entities.Entity;
import com.tikisadventure.entities.player.Player; // Cambio: Importamos Player

public class WeaponManager {

    private Player player; // Cambio: De Tiki a Player
    private Array<Weapon> weapons;

    private float radius = 1.2f;

    public WeaponManager(Player player){
        this.player = player;
        this.weapons = new Array<>();
    }

    public void addWeapon(Weapon weapon){
        weapons.add(weapon);
    }

    public void update(float delta, Array<Entity> enemies){
        // Primero posicionamos las armas alrededor del jugador
        updateWeaponPositions();

        // Actualizamos la lógica de cada arma (IA de apuntado y disparo)
        for(Weapon w : weapons){
            w.update(delta, enemies);
        }
    }

    public void render(Batch batch){
        for(Weapon w : weapons){
            w.render(batch);
        }
    }

    private void updateWeaponPositions(){
        int total = weapons.size;
        if (total == 0) return;

        // Calculamos el centro del jugador usando sus dimensiones
        float centerX = player.getPosicion().x;
        float centerY = player.getPosicion().y;

        for(int i = 0; i < total; i++){
            Weapon w = weapons.get(i);

            // Distribuimos las armas equitativamente en un círculo
            // Agregamos una rotación base (PI/2) para que la primera empiece arriba
            float angle = (MathUtils.PI2 / total) * i + MathUtils.PI / 2;

            float x = centerX + MathUtils.cos(angle) * radius;
            float y = centerY + MathUtils.sin(angle) * radius;

            w.setPosition(x, y);

            // Opcional: Hacer que el arma rote visualmente hacia donde apunta
            // w.setRotation(angle * MathUtils.radiansToDegrees);
        }
    }

    public Array<Weapon> getWeapons(){
        return weapons;
    }

    // Método útil si quieres limpiar las armas al morir o cambiar de nivel
    public void clear() {
        weapons.clear();
    }
}
