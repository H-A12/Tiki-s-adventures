package com.tikisadventure.combat.weapons;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.tikisadventure.entities.Entity;
import com.tikisadventure.entities.player.Tiki;

public class WeaponManager {

    private Tiki player;
    private Array<Weapon> weapons;

    private float radius = 1.2f;

    public WeaponManager(Tiki player){
        this.player = player;
        weapons = new Array<>();
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

        for(int i = 0; i < total; i++){

            Weapon w = weapons.get(i);

            float angle = MathUtils.PI2 / total * i + MathUtils.PI/2;

            float x = player.getPosicion().x + MathUtils.cos(angle) * radius;
            float y = player.getPosicion().y + MathUtils.sin(angle) * radius;

            w.setPosition(x,y);
        }
    }

    public Array<Weapon> getWeapons(){
        return weapons;
    }
}
