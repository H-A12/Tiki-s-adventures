package com.tikisadventure.combat.weapons.pistol;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.tikisadventure.combat.weapons.Bullet;
import com.tikisadventure.combat.weapons.Weapon;
import com.tikisadventure.entities.Entity;

public class BasicGun extends Weapon {

    private static Texture texture = new Texture("gun.png");

    private Array<Bullet> bullets = new Array<>();

    public BasicGun(Entity owner) {

        super(owner);

        sprite = new TextureRegion(texture);

        cd = 0.6f;
        damage = 10f;
        bulletSpeed = 8f;
        bulletSize = 0.2f;
        shootRange = 6f;
    }

    @Override
    protected void shoot() {

        if(objetive == null) return;

        Vector2 dir = new Vector2(
            objetive.getPosicion().x - worldPosition.x,
            objetive.getPosicion().y - worldPosition.y
        ).nor();

        Bullet bullet = new Bullet(
            worldPosition,
            dir,
            bulletSpeed,
            damage,
            bulletSize,
            false
        );

        bullets.add(bullet);
    }

    @Override
    public void update(float delta, Array<Entity> enemies) {

        super.update(delta, enemies);

        for(int i = bullets.size - 1; i >= 0; i--){

            Bullet b = bullets.get(i);

            b.update(delta, enemies);

            // eliminar bala si impactó
            if(!b.getPenetration()){
                for(Entity e : enemies){
                    if(e.getVida() <= 0) continue;

                    if(b.getPosition().dst2(e.getPosicion()) <= b.getRadius() * b.getRadius()){
                        bullets.removeIndex(i);
                        break;
                    }
                }
            }
        }

        for(int i = bullets.size - 1; i >= 0; i--){

            Bullet b = bullets.get(i);

            b.update(delta, enemies);

            if(!b.isAlive()){
                bullets.removeIndex(i);
            }
        }

    }

    @Override
    public void render(Batch batch){

        super.render(batch);

        for(Bullet b : bullets){
            batch.draw(
                sprite,
                b.getPosition().x - b.getRadius(),
                b.getPosition().y - b.getRadius(),
                b.getRadius() * 2,
                b.getRadius() * 2
            );
        }
    }

    public void renderBullets(Batch batch){
        for(Bullet b : bullets){
            // aquí dibujarías la bala
        }
    }

}
