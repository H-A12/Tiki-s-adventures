package io.github.some_example_name.weapons;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import io.github.some_example_name.personajes.Personaje;

public class Bullet {

    public Vector2 position;
    public Vector2 direction;
    public float speed;
    public float damage;
    public float radius;
    public boolean penetrate;


    public Bullet(Vector2 startPos, Vector2 dir, float speed, float damage, float radius, boolean penetrate) {
        this.position = new Vector2(startPos);
        this.direction = new Vector2(dir).nor();
        this.speed = speed;
        this.damage = damage;
        this.radius = radius;
        this.penetrate = penetrate;
    }

    public void update(float delta, Array<Personaje> enemies) {
        position.mulAdd(direction, speed*delta);

        // colisión con enemigos
        for(Personaje e : enemies){
            if(e.getVida() <= 0) continue;

            if(position.dst2(e.getPosicion()) <= radius*radius){
                e.setVida(e.getVida()- damage);
                if(!penetrate) {
                    // destruir bala (la eliminarías del array en Main)
                    break;
                }
            }
        }
    }


}
