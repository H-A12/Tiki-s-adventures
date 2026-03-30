package com.tikisadventure.systems;

import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.utils.Array;
import com.tikisadventure.entities.base.Entity;
import com.tikisadventure.entities.player.Player;
import com.tikisadventure.effects.EffectManager; // <--- Importado

/**
 * Orquestador de lógica. Controla el orden en que los sistemas procesan los datos.
 */
public class SystemManager {

    private final AISystem aiSystem;
    private final CollisionSystem collisionSystem;
    private final AnimationSystem animationSystem;
    private final MapCollisionSystem mapCollision;
    private final EnemySpawner spawner;
    private final WaveSystem waveSystem;
    private final EffectManager effectManager; // <--- Referencia central

    public SystemManager(Array<Entity> entities, TiledMapTileLayer collisionLayer, EffectManager effectManager) {
        this.effectManager = effectManager;

        // Inicialización con inyección de dependencias
        this.waveSystem = new WaveSystem("default");
        this.spawner = new EnemySpawner(entities, collisionLayer, waveSystem);
        this.aiSystem = new AISystem();

        // Ahora pasamos el effectManager al sistema de colisiones
        this.collisionSystem = new CollisionSystem(effectManager);

        this.animationSystem = new AnimationSystem();
        this.mapCollision = new MapCollisionSystem(collisionLayer);
    }

    public void update(float delta, Array<Entity> entities, Player player) {
        // 1. Fase de Nacimiento y Estrategia
        spawner.update(delta, player);
        aiSystem.update(entities, player, delta);

        // 2. Fase de Lógica Interna y Limpieza
        for (int i = entities.size - 1; i >= 0; i--) {
            Entity e = entities.get(i);
            if (e.isAlive()) {
                e.update(delta);
            } else if (e != player) {
                entities.removeIndex(i);
            }
        }

        // 3. Fase Visual y Efectos
        animationSystem.update(entities, delta);
        effectManager.update(delta); // <--- Actualizamos el timer de las partículas y flashes

        // 4. Fase de Física y Combate (Aquí se disparan los efectos)
        collisionSystem.update(player, entities, delta);

        // 5. Resolución de Mapa (Colisiones con muros)
        mapCollision.resolve(player, delta);
        for (Entity e : entities) {
            if (e != player && e.isAlive()) {
                mapCollision.resolve(e, delta);
            }
        }
    }

    public WaveSystem getWaveSystem() { return waveSystem; }
}
