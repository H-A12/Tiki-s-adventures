package io.github.some_example_name.weapons;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import io.github.some_example_name.personajes.Personaje;

public abstract class Weapon {

    // --- Stats del arma ---
    protected float cd;
    private float lastShootTime;
    protected float damage;
    protected float bulletSpeed;
    protected float bulletSize;
    protected float shootRange;
                                                                                                    //TODA LA CLASE SIN PROBAR
    // --- Target ---
    protected Personaje objetive;

    // --- Posición y rotación ---
    protected Vector2 relativeOffset = new Vector2(); // posición relativa al jugador
    protected Vector2 worldPosition = new Vector2();
    protected float currentAngle;
    protected float visualAngle;

    protected Personaje owner;
    protected TextureRegion sprite;


    public void update(float delta, Array<Personaje> enemies){
        updatePosition();
        searchEnemy(enemies);
        visualAngle += 180f * delta;

    }

    public void updatePosition(){

        worldPosition.set(owner.getPosicion()).add(relativeOffset);

    }

    public void searchEnemy(Array<Personaje> enemies){
        if(objetive == null || objetive.getVida() <= 0) return;

        Personaje moreClose = null;
        float minDistance = Float.MAX_VALUE;

        for(Personaje e : enemies){

            if(e.getVida() <= 0) continue;

            float dx = e.getPosicion().x - worldPosition.x;
            float dy = e.getPosicion().y - worldPosition.y;

            float distance =dx * dx + dy * dy;

            if(distance < minDistance && distance <= shootRange * shootRange){
                minDistance = distance;
                moreClose = e;
            }
        }
        objetive = moreClose;
    }

    public void distributeWeapons(Array<Weapon> weapons, int maxWeapons, float radius) {

        float angleStep = 360f / maxWeapons;

        for (int i = 0; i < weapons.size; i++) {

            float angleDeg = i * angleStep;

            Weapon w = weapons.get(i);

            w.currentAngle = angleDeg; // ángulo visual base si quieres

            w.relativeOffset.set(radius, 0).setAngleDeg(angleDeg);
        }
    }

    private void tryShoot(float delta){
        lastShootTime += delta;
        if(objetive == null || objetive.getVida() <= 0) return;

        if(lastShootTime >= cd) {
            shoot();
            lastShootTime = 0;
        }
    }

    protected abstract void shoot();

    public void render(SpriteBatch batch) {

        float width = sprite.getRegionWidth() / 16f;   // escala del mundo
        float height = sprite.getRegionHeight() / 16f;

        float originX = width / 2f;
        float originY = height / 2f;

        batch.draw(
            sprite,
            worldPosition.x - originX,  // posición centrada
            worldPosition.y - originY,
            originX,                    // punto de rotación X
            originY,                    // punto de rotación Y
            width,
            height,
            1f,                         // escala X
            1f,                         // escala Y
            visualAngle               // rotación
        );
    }
}
