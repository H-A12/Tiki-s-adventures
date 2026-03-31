package com.tikisadventure.combat.weapons;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.tikisadventure.entities.base.Entity;
import com.tikisadventure.entities.player.Player;

public class WeaponManager {

    private Player player;
    private Array<Weapon> weapons;
    private float radius = 1.3f;

    public WeaponManager(Player player){
        this.player = player;
        this.weapons = new Array<>();
    }

    public void addWeapon(Weapon weapon){
        weapons.add(weapon);
    }

    public void update(float delta, Array<Entity> enemies){
        updateWeaponPositions();
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
        float centerX = player.getPosicion().x;
        float centerY = player.getPosicion().y;
        float spacing = MathUtils.PI2 / total;

        for(int i = 0; i < total; i++){
            Weapon w = weapons.get(i);
            float angle = (i * spacing) + (MathUtils.PI / 2f) - (spacing / 2f);
            float x = centerX + MathUtils.cos(angle) * radius;
            float y = centerY + MathUtils.sin(angle) * radius;
            w.setPosition(x, y);
        }
    }

    public Array<Weapon> getWeapons(){
        return weapons;
    }

    public void clear() {
        weapons.clear();
    }
}
