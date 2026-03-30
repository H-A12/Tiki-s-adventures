package com.tikisadventure.systems;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Array;
import com.tikisadventure.entities.base.Entity;
import com.tikisadventure.effects.EffectManager; // <--- Importado
import java.util.Comparator;

public class RenderSystem {
    private final SpriteBatch batch;
    private final Array<Entity> renderQueue;
    private final Comparator<Entity> yComparator;

    public RenderSystem(SpriteBatch batch) {
        this.batch = batch;
        this.renderQueue = new Array<>();

        // Y-SORTING: Dibujamos de arriba hacia abajo para que los pies tapen lo que hay detrás.
        this.yComparator = (e1, e2) -> Float.compare(e2.getPosicion().y, e1.getPosicion().y);
    }

    /**
     * Procesa y dibuja todas las entidades con orden de profundidad y efectos.
     */
    public void process(Array<Entity> entities, OrthographicCamera camera, float delta, EffectManager effectManager) {
        renderQueue.clear();

        for (Entity entity : entities) {
            if (entity.isAlive()) {
                renderQueue.add(entity);
            }
        }

        // 1. Ordenar por profundidad (Y-Sorting)
        renderQueue.sort(yComparator);

        // 2. Dibujar con feedback de daño
        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        for (Entity entity : renderQueue) {
            // ¿Está la entidad sufriendo daño en este frame?
            boolean isFlashing = effectManager.isEntityFlashing(entity);

            if (isFlashing) {
                // Aplicamos un tinte blanco brillante/rojo para el feedback de daño
                batch.setColor(Color.WHITE); // Reset por seguridad
                batch.setColor(2f, 2f, 2f, 1f); // Sobresaturamos el blanco (Efecto Flash)
            }

            entity.render(batch, delta);

            if (isFlashing) {
                // Restauramos el color normal inmediatamente para la siguiente entidad
                batch.setColor(Color.WHITE);
            }
        }

        batch.end();
    }
}
