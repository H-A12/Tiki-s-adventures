package com.tikisadventure.entities.enemies;

import com.tikisadventure.entities.base.Entity;
import com.tikisadventure.systems.WaveSystem;

//Implementación de EnemyFactory que crea ConfigurableEnemy con tipo y WaveSystem
public class EnemyFactoryImpl implements EnemyFactory {

    private String enemyType;
    private WaveSystem waveSystem;

    public EnemyFactoryImpl(String enemyType, WaveSystem waveSystem) {
        this.enemyType = enemyType;
        this.waveSystem = waveSystem;
    }

    @Override
    public Entity create() {
        return new ConfigurableEnemy(enemyType, waveSystem);
    }
}
