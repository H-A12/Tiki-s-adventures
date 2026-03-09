package com.tikisadventure.systems;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;

import com.tikisadventure.entities.Entity;
import com.tikisadventure.entities.enemies.EnemyFactory;

public class EnemySpawner {

    private Array<Entity> enemies;
    private Array<EnemyFactory> enemyTypes = new Array<>();

    private float spawnTimer;

    private final float SPAWN_INTERVAL = 1f;
    private final float SPAWN_RADIUS = 5f;

    public EnemySpawner(Array<Entity> enemies){
        this.enemies = enemies;
    }

    public void addEnemyType(EnemyFactory factory){
        enemyTypes.add(factory);
    }

    public void update(float delta, Entity player){

        spawnTimer += delta;

        if(spawnTimer >= SPAWN_INTERVAL && enemyTypes.size > 0){

            spawnTimer = 0;

            EnemyFactory factory = enemyTypes.random();

            Entity enemy = factory.create();

            float angle = MathUtils.random(0f,360f);

            float x = player.getPosicion().x + MathUtils.cosDeg(angle)*SPAWN_RADIUS;
            float y = player.getPosicion().y + MathUtils.sinDeg(angle)*SPAWN_RADIUS;

            enemy.getPosicion().set(x,y);

            enemies.add(enemy);
        }
    }
}
