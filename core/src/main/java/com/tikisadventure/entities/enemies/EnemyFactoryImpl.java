package com.tikisadventure.entities.enemies;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.JsonValue;
import com.tikisadventure.assets.Assets;
import com.tikisadventure.entities.base.Entity;

/**
 * Factoría optimizada: Persistente y capaz de crear cualquier tipo de enemigo
 * escalando sus stats según la oleada actual.
 */
public class EnemyFactoryImpl {

    public EnemyFactoryImpl() {
        // Constructor vacío, la lógica ahora es dinámica por cada llamada a create
    }

    /**
     * Crea un enemigo configurado y escalado.
     * @param enemyType ID del enemigo (ej: "slime", "skeleton")
     * @param currentWave Número de oleada para aplicar multiplicadores
     */
    public Entity create(String enemyType, int currentWave) {
        // 1. Obtener la configuración base (esto debería venir de un JSON central de enemigos)
        // Por ahora, simulamos la carga de stats o la obtenemos de Assets/Config
        JsonValue config = Assets.getEnemyConfig(enemyType);

        if (config == null) {
            // Log de error o fallback a un enemigo básico
            System.err.println("Configuración no encontrada para: " + enemyType);
            return createDefaultSlime(currentWave);
        }

        // 2. Preparar el apartado visual (Animaciones)
        // Extraemos la textura y la dividimos (asumiendo tiles de 16x16 o 32x32)
        TextureRegion fullRegion = Assets.getTexture(config.getString("sprite", "enemies/slime"));
        int tileW = config.getInt("tileWidth", 16);
        int tileH = config.getInt("tileHeight", 16);
        TextureRegion[][] tmp = fullRegion.split(tileW, tileH);

        // Definimos animaciones básicas
        Animation<TextureRegion> idle = new Animation<>(0.15f, tmp[0][0]);
        Animation<TextureRegion> walk = new Animation<>(0.1f, tmp[0]); // Toda la fila 0
        walk.setPlayMode(Animation.PlayMode.LOOP_PINGPONG);

        // 3. Instanciar la entidad (en posición 0,0; el Spawner la moverá después)
        ConfigurableEnemy enemy = new ConfigurableEnemy(0, 0, idle, walk);

        // 4. Escalado de dificultad progresivo
        // Fórmula sugerida: 10% de aumento de stats por cada oleada
        float mult = 1.0f + (currentWave * 0.1f);

        enemy.setStats(
            config.getFloat("hp", 10) * mult,
            config.getFloat("speed", 1.5f), // La velocidad suele ser constante para no romper el balance
            config.getFloat("damage", 2) * mult,
            config.getInt("exp", 5) + (currentWave * 2)
        );

        // 5. Configuración física y de IA
        enemy.ANCHO = config.getFloat("width", 1.0f);
        enemy.ALTO = config.getFloat("height", 1.0f);
        enemy.setAiType(config.getString("ai", "chaser"));

        // 6. Preparar para el mundo
        enemy.actualizarHitboxes();

        return enemy;
    }

    /**
     * Método de respaldo por si falla la carga del JSON
     */
    private Entity createDefaultSlime(int wave) {
        // Lógica simplificada para crear un slime básico rápidamente
        TextureRegion region = Assets.getTexture("enemies/slime_basic");
        Animation<TextureRegion> anim = new Animation<>(0.1f, region.split(16,16)[0]);
        ConfigurableEnemy slime = new ConfigurableEnemy(0, 0, anim, anim);
        slime.setStats(10 + wave, 1.2f, 2, 5);
        return slime;
    }
}
