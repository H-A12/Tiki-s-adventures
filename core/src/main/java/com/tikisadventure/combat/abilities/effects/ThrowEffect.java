package com.tikisadventure.combat.abilities.effects;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.tikisadventure.entities.base.Entity;
import com.tikisadventure.entities.player.Player;
import com.tikisadventure.effects.EffectManager;

//Lanzar un proyectil que ejecuta efectos al impactar
public class ThrowEffect implements AbilityEffect {
    private EffectManager em;
    private String sprite;
    private float speed;
    private float lifetime;
    private String trailType;
    private float trailSpacing;
    private Array<AbilityEffect> onHitEffects;

    public ThrowEffect(EffectManager em, String sprite, float speed, float lifetime, String trailType, float trailSpacing, Array<AbilityEffect> onHitEffects) {
        this.em = em;
        this.sprite = sprite;
        this.speed = speed;
        this.lifetime = lifetime;
        this.trailType = trailType;
        this.trailSpacing = trailSpacing;
        this.onHitEffects = onHitEffects;
    }

    @Override
    public boolean execute(Player owner, Array<Entity> enemies, Vector2 targetPosition) {
        // Obtenemos el vector de dirección real
        Vector2 direction = targetPosition.cpy().sub(owner.getPosition());
        float distance = direction.len();

        // EL SEGURO DEFINITIVO:
        // Si la distancia es menor a medio bloque (jugador quieto, mando en reposo o apuntando a los pies)
        if (distance < 0.5f || direction.isZero()) {
            direction.set(0f, -1f); // Le damos una dirección válida hacia abajo para que Normalize no explote
            distance = 0.1f;        // Forzamos una distancia mínima artificial
        }

        direction.nor();

        // Calculamos el tiempo que tarda en llegar
        float dynamicLifetime = distance / speed;

        // Si el tiempo es ridículamente bajo, lo fijamos en 1 milisegundo (0.001f).
        // Esto garantiza que la granada exista en el motor al menos 1 frame para procesar
        // la creación del Scarecrow INMEDIATAMENTE, antes de que el jugador muera.
        if (dynamicLifetime <= 0.01f) {
            dynamicLifetime = 0.001f;
        }

        GrenadeProjectile grenade = new GrenadeProjectile(owner, enemies, owner.getPosition(), direction, speed, dynamicLifetime, sprite, onHitEffects, em, trailType, trailSpacing);
        owner.addProjectile(grenade);
        return true;
    }
}
