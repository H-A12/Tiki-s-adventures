package com.tikisadventure.entities.player;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.tikisadventure.combat.projectiles.Projectile;
import com.tikisadventure.combat.weapons.WeaponManager;
import com.tikisadventure.components.HealthComponent;
import com.tikisadventure.components.RenderComponent;
import com.tikisadventure.core.Assets;
import com.tikisadventure.entities.base.Entity;
import com.tikisadventure.input.InputHandler;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.tikisadventure.screens.GameScreen;

public class Player extends Entity {

    private CharacterProfile profile;
    private WeaponManager weaponManager;
    private Array<Projectile> activeProjectiles;
    private Array<Entity> allies;
    private com.tikisadventure.systems.ExperienceSystem experienceSystem;

    public enum Estado { IDLE, UP, DOWN, LEFT, RIGHT }
    private Estado estadoActual = Estado.IDLE;

    private Vector2 dashVelocity = new Vector2();
    private float dashTimer = 0;
    private boolean isDashing = false;

    private Array<Vector2> trailPositions = new Array<>();
    private float trailTimer = 0;
    private final float TRAIL_INTERVAL = 0.04f;

    private Vector2 tempMove = new Vector2();

    private float ability1CooldownTimer = 0;
    private boolean canUseAbility1 = true;
    private float ability2CooldownTimer = 0;
    private boolean canUseAbility2 = true;
    private int score = 0;
    private float luck = 0f;
    private float xpMultiplier = 1.0f;

    private float kineticDamageBonus = 0f;
    private float explosiveDamageBonus = 0f;
    private float fireDamageBonus = 0f;
    private float poisonDamageBonus = 0f;
    private float iceDamageBonus = 0f;
    private float critChanceBonus = 0f;
    private float extraHealthGained = 0f;
    private float lifeRegenPercent = 0.0f;
    private float evasionChance = 0.0f;
    private float attractionRange = 2.0f;

    //Robo de vida
    private float lifeLeechPercent = 0.0f; //Porcentaje de curación según daño infligido

    private TextureRegion arrowTexture;
    private TextureRegion doorArrowTexture;
    private float arrowBobTimer = 0f;

    public int totalKills = 0;
    public com.badlogic.gdx.utils.ObjectMap<String, Integer> killDetails = new com.badlogic.gdx.utils.ObjectMap<>();

    private float immunityTimer = 0f;
    private Color outlineColor = new Color(1f, 0.8f, 0f, 1f);

    public float regenTextAccumulator = 0f;
    public float leechTextAccumulator = 0f;

    public float regenTextTimer = 0f;
    public float leechTextTimer = 0f;

    public Player(CharacterProfile profile) {
        super();
        this.profile = profile;
        setSpeed(profile.speed);
        renderComponent = new RenderComponent(profile.idle.getKeyFrame(0), 1.5f, 1.5f);
        setANCHO(1.5f);
        setALTO(1.5f);
        this.weaponManager = new WeaponManager(this);
        this.activeProjectiles = new Array<>();
        this.allies = new Array<>();
        this.experienceSystem = new com.tikisadventure.systems.ExperienceSystem(this);

        this.healthComponent = new HealthComponent(profile.maxHealth);
        this.positionComponent.posicion.set(0, 0);

        this.arrowTexture = Assets.getRegion("shared", "UI_assets/Enemy_arrow");
        this.doorArrowTexture = Assets.getRegion("shared", "UI_assets/Door_arrow");
    }

    @Override
    protected boolean onFatalDamage() {
        if (GameScreen.activeScarecrow != null && GameScreen.activeScarecrow.isAlive() && !GameScreen.scarecrowLocked) {
            System.out.println("¡Muerte evadida! Resucitando en el Espantapájaros...");

            this.healthComponent.currentHealth = this.healthComponent.maxHealth;
            this.getPosition().set(GameScreen.activeScarecrow.getPosition());
            this.grantImmunity(2.0f);

            GameScreen.activeScarecrow.setAlive(false);
            GameScreen.activeScarecrow = null;
            GameScreen.scarecrowLocked = true;

            GameScreen.triggerScarecrowReviveEffects(this);

            return true;
        }
        return false;
    }

    public float getAbility1CooldownPercent() {
        if (profile.specialAbility1 == null) return 1.0f;
        if (ability1CooldownTimer <= 0) return 1.0f;
        return 1.0f - (ability1CooldownTimer / profile.specialAbility1.getCooldown());
    }

    public float getAbility2CooldownPercent() {
        if (profile.specialAbility2 == null) return 1.0f;
        if (ability2CooldownTimer <= 0) return 1.0f;
        return 1.0f - (ability2CooldownTimer / profile.specialAbility2.getCooldown());
    }

    public void applyDashImpulse(Vector2 impulse, float duration) {
        this.dashVelocity.set(impulse);
        this.dashTimer = duration;
        this.isDashing = true;
    }

    public void update(float delta, Array<Entity> enemies, InputHandler inputHandler) {
        super.update(delta);

        if (immunityTimer > 0) {
            immunityTimer -= delta;
            this.getTintColor().a = 1f;
        }

        regenTextTimer += delta;

        if (regenTextTimer >= 2.0f) {

            if (lifeRegenPercent > 0 && isAlive() && healthComponent.currentHealth < healthComponent.maxHealth) {

                // La fórmula pura: 100 HP * 0.01 (1%) = 1 HP cada 2 segundos
                float regenAmount = healthComponent.maxHealth * lifeRegenPercent;

                float vidaAntes = healthComponent.currentHealth;
                heal(regenAmount);
                float vidaRestaurada = healthComponent.currentHealth - vidaAntes;

                regenTextAccumulator += vidaRestaurada;

                if (regenTextAccumulator >= 1.0f) {
                    int healInt = (int) regenTextAccumulator;
                    com.tikisadventure.systems.events.EventBus.publish(
                        new com.tikisadventure.systems.events.HealEvent(this, healInt, com.tikisadventure.systems.events.HealEvent.HealType.REGEN)
                    );
                    regenTextAccumulator -= healInt;
                }
            }
            regenTextTimer = 0f;
        }

        // --- CONTROLADOR DEL TEXTO DE ROBO DE VIDA (1 vez por segundo) ---
        // Fíjate que ahora está completamente fuera del bloque anterior
        leechTextTimer += delta;
        if (leechTextTimer >= 1.0f) {
            if (leechTextAccumulator >= 0.5f) {
                int leechInt = Math.round(leechTextAccumulator);
                if (leechInt >= 1) {
                    com.tikisadventure.systems.events.EventBus.publish(
                        new com.tikisadventure.systems.events.HealEvent(this, leechInt, com.tikisadventure.systems.events.HealEvent.HealType.LEECH)
                    );
                }
                leechTextAccumulator = 0;
            }
            leechTextTimer = 0f;

        }

        if (com.tikisadventure.core.GameSession.godMode && com.tikisadventure.core.GameSession.godModeIsImmortal) {
            this.healthComponent.currentHealth = this.healthComponent.maxHealth;
        }

        if (healthComponent.currentHealth <= 0) return;
        applyKnockback(delta);

        if (dashTimer > 0) {
            positionComponent.posicion.mulAdd(dashVelocity, delta);
            dashTimer -= delta;
            updateTrail(delta);
            if (dashTimer <= 0) isDashing = false;
        } else {
            handleInput(inputHandler, delta);
            updateTrailFade(delta);
        }

        actualizarHitboxes();
        weaponManager.update(delta, enemies);
        updateAbilities(delta, enemies, inputHandler);
    }

    private void handleInput(InputHandler inputHandler, float delta) {
        if (!inputHandler.moveDirection.isZero()) {
            inputDirection.set(inputHandler.moveDirection).nor();
            velocityComponent.velocidad.set(inputDirection).scl(velocityComponent.speed);

            if (Math.abs(inputDirection.y) > Math.abs(inputDirection.x)) {
                estadoActual = (inputDirection.y > 0) ? Estado.UP : Estado.DOWN;
            } else {
                estadoActual = (inputDirection.x > 0) ? Estado.RIGHT : Estado.LEFT;
            }
        } else {
            inputDirection.setZero();
            estadoActual = Estado.IDLE;
            velocityComponent.velocidad.setZero();
        }
    }

    private boolean isAiming = false;
    private Vector2 aimingTarget = new Vector2();
    private Vector2 inputDirection = new Vector2();
    private float cookingTime = 0;

    private void updateAbilities(float delta, Array<Entity> enemies, InputHandler inputHandler) {
        if (ability1CooldownTimer > 0) ability1CooldownTimer -= delta;
        else canUseAbility1 = true;
        if (ability2CooldownTimer > 0) ability2CooldownTimer -= delta;
        else canUseAbility2 = true;

        if (profile.specialAbility1 != null && inputHandler.useAbility1 && canUseAbility1) {
            Vector2 target = positionComponent.posicion.cpy().add(inputHandler.aimDirection.cpy().scl(5));
            boolean success = profile.specialAbility1.activate(this, enemies, target);
            if (success) {
                ability1CooldownTimer = profile.specialAbility1.getCooldown();
                canUseAbility1 = false;
            }
        }

        if (profile.specialAbility2 != null && canUseAbility2) {
            if (inputHandler.isAimingAbility2) {
                isAiming = true;
                cookingTime += delta;

                float maxRange = profile.specialAbility2.getMaxRange();

                if (!inputHandler.aimTargetAbility2.isZero()) {
                    Vector2 dir = inputHandler.aimTargetAbility2.cpy().sub(positionComponent.posicion);
                    float distance = dir.len();
                    if (distance > maxRange) {
                        dir.nor().scl(maxRange);
                        aimingTarget.set(positionComponent.posicion).add(dir);
                    } else {
                        aimingTarget.set(inputHandler.aimTargetAbility2);
                    }
                } else {
                    Vector2 dir = inputHandler.aimDirectionAbility2.cpy();
                    if (!dir.isZero()) {
                        float magnitude = inputHandler.aimMagnitudeAbility2;
                        float distance = magnitude * maxRange;
                        if (distance > maxRange) distance = maxRange;
                        dir.nor().scl(distance);
                    }
                    aimingTarget.set(positionComponent.posicion).add(dir);
                }

            } else if (isAiming && canUseAbility2) {
                float maxRange = profile.specialAbility2.getMaxRange();
                float distance = aimingTarget.dst(positionComponent.posicion);

                if (distance <= maxRange) {
                    profile.specialAbility2.activate(this, enemies, aimingTarget);
                    ability2CooldownTimer = profile.specialAbility2.getCooldown();
                    canUseAbility2 = false;
                }
                isAiming = false;
                cookingTime = 0;
            }
        }
    }

    public boolean isAiming() { return isAiming; }
    public Vector2 getAimingTarget() { return aimingTarget; }
    public Vector2 getInputDirection() { return inputDirection; }

    private void updateTrail(float delta) {
        trailTimer += delta;
        if (trailTimer >= TRAIL_INTERVAL) {
            trailPositions.add(new Vector2(positionComponent.posicion.x, positionComponent.posicion.y));
            trailTimer = 0;
            if (trailPositions.size > 6) trailPositions.removeIndex(0);
        }
    }

    private void updateTrailFade(float delta) {
        if (trailPositions.size > 0) {
            trailTimer += delta;
            if (trailTimer >= TRAIL_INTERVAL) {
                trailPositions.removeIndex(0);
                trailTimer = 0;
            }
        }
    }

    @Override
    public void draw(Batch batch, float delta) {
        if (healthComponent.currentHealth <= 0) return;

        for (Entity a : allies) a.render(batch, delta);

        TextureRegion currentFrame;
        switch (estadoActual) {
            case UP:    currentFrame = profile.up.getKeyFrame(getStateTime(), true); break;
            case DOWN:  currentFrame = profile.down.getKeyFrame(getStateTime(), true); break;
            case LEFT:  currentFrame = profile.left.getKeyFrame(getStateTime(), true); break;
            case RIGHT: currentFrame = profile.right.getKeyFrame(getStateTime(), true); break;
            default:    currentFrame = profile.idle.getKeyFrame(getStateTime(), true); break;
        }

        Color oldColor = batch.getColor();
        for (int i = 0; i < trailPositions.size; i++) {
            float alpha = (float) (i + 1) / (trailPositions.size + 1);
            batch.setColor(1, 1, 1, alpha * 0.4f);
            Vector2 p = trailPositions.get(i);
            batch.draw(currentFrame, p.x - getANCHO()/2, p.y - getALTO()/2, getANCHO(), getALTO());
        }
        batch.setColor(oldColor);

        batch.setShader(null);
        batch.setColor(Color.WHITE);
        for (Projectile p : activeProjectiles) p.render(batch);
        batch.setColor(Color.WHITE);

        // --- SHADER DEFENSIVO: Si algo del shader falla, el juego no crashea ---
        try {
            if (immunityTimer > 0 && Assets.outlineShader != null && Assets.outlineShader.isCompiled()) {
                batch.setShader(Assets.outlineShader);
                Assets.outlineShader.setUniformf("u_textureSize", currentFrame.getTexture().getWidth(), currentFrame.getTexture().getHeight());
                Assets.outlineShader.setUniformf("u_outlineColor", outlineColor);
                Assets.outlineShader.setUniformf("u_outlineSize", 1.0f);
            } else if (damageFlashTimer > 0 && Assets.whiteFlashShader != null && Assets.whiteFlashShader.isCompiled()) {
                batch.setShader(Assets.whiteFlashShader);
            } else {
                batch.setShader(null);
            }
        } catch (Exception e) {
            batch.setShader(null); // Si falla, quitamos el shader para evitar crash
        }

        batch.setColor(getTintColor());
        batch.draw(currentFrame, positionComponent.posicion.x - getANCHO()/2, positionComponent.posicion.y - getALTO()/2, getANCHO(), getALTO());

        batch.setColor(Color.WHITE);
        batch.setShader(null);
        weaponManager.render(batch);

        batch.setColor(1f, 1f, 1f, 1f);
    }

    public void drawEnemyArrow(Batch batch, Array<Entity> enemies) {
        if (enemies == null || enemies.size == 0 || enemies.size > 5) return;

        Entity nearest = null;
        float nearestDist = Float.MAX_VALUE;

        for (Entity e : enemies) {
            if (!e.isAlive()) continue;
            float dist = Vector2.dst(
                positionComponent.posicion.x, positionComponent.posicion.y,
                e.getPosition().x, e.getPosition().y
            );
            if (dist < nearestDist) {
                nearestDist = dist;
                nearest = e;
            }
        }

        if (nearest == null) return;

        float dx = nearest.getPosition().x - positionComponent.posicion.x;
        float dy = nearest.getPosition().y - positionComponent.posicion.y;
        float angle = (float) Math.toDegrees(Math.atan2(dy, dx));

        arrowBobTimer += 0.1f;
        float bobOffset = (float) Math.sin(arrowBobTimer) * 0.1f;

        float arrowX = positionComponent.posicion.x;
        float arrowY = positionComponent.posicion.y + 2.0f + bobOffset;

        batch.draw(arrowTexture, arrowX - 0.5f, arrowY - 0.5f, 0.5f, 0.5f, 1f, 1f, 1f, 1f, angle);
    }

    public void drawDoorArrow(Batch batch, Vector2 doorPos, boolean doorOpen) {
        if (!doorOpen || doorPos == null) return;

        float dx = doorPos.x - positionComponent.posicion.x;
        float dy = doorPos.y - positionComponent.posicion.y;
        float angle = (float) Math.toDegrees(Math.atan2(dy, dx));

        arrowBobTimer += 0.1f;
        float bobOffset = (float) Math.sin(arrowBobTimer) * 0.1f;

        float arrowX = positionComponent.posicion.x;
        float arrowY = positionComponent.posicion.y + 2.0f + bobOffset;

        batch.draw(doorArrowTexture, arrowX - 0.5f, arrowY - 0.5f, 0.5f, 0.5f, 1f, 1f, 1f, 1f, angle);
    }

    public Array<Projectile> getActiveProjectiles() { return activeProjectiles; }
    public void addProjectile(Projectile p) { activeProjectiles.add(p); }
    public CharacterProfile getProfile() { return this.profile; }
    public com.tikisadventure.systems.ExperienceSystem getExperienceSystem() { return this.experienceSystem; }
    public WeaponManager getWeaponFactory() { return weaponManager; }
    public boolean isDashing() { return isDashing; }
    @Override public void update(float delta, Entity target) {}

    public int getScore() { return score; }
    public void addScore(int points) { this.score += points; }
    public void setScore(int score) { this.score = score; }

    public float getLuck() {return luck;}
    public void setLuck(float suerte) {this.luck = suerte;}

    public float getXpMultiplier() { return xpMultiplier; }
    public void setXpMultiplier(float xpMultiplier) { this.xpMultiplier = xpMultiplier; }

    public void addSpeedPercent(float percent) {
        if (this.velocityComponent != null) {
            float bonusSpeed = this.velocityComponent.speed * percent;
            this.velocityComponent.speed += bonusSpeed;
        }
    }

    public float getSpeed() {
        if (this.velocityComponent != null) {
            return this.velocityComponent.speed;
        }
        return 0f;
    }

    public float getKineticDamageBonus() { return kineticDamageBonus; }
    public void addKineticDamageBonus(float amount) { this.kineticDamageBonus += amount; }

    public float getExplosiveDamageBonus() { return explosiveDamageBonus; }
    public void addExplosiveDamageBonus(float amount) { this.explosiveDamageBonus += amount; }

    public float getFireDamageBonus() { return fireDamageBonus; }
    public void addFireDamageBonus(float amount) { this.fireDamageBonus += amount; }

    public float getPoisonDamageBonus() { return poisonDamageBonus; }
    public void addPoisonDamageBonus(float amount) { this.poisonDamageBonus += amount; }

    public float getIceDamageBonus() { return iceDamageBonus; }
    public void addIceDamageBonus(float amount) { this.iceDamageBonus += amount; }

    public float getCritChanceBonus() { return critChanceBonus; }
    public void addCritChanceBonus(float amount) { this.critChanceBonus += amount; }

    public float getExtraHealthGained() { return extraHealthGained; }
    public void addExtraHealthGained(float amount) { this.extraHealthGained += amount; }

    // --- NUEVO: Daño de Energía ---
    private float energyDamageBonus = 0f;
    public float getEnergyDamageBonus() { return energyDamageBonus; }
    public void addEnergyDamageBonus(float amount) { this.energyDamageBonus += amount; }

    // --- NUEVO: Atajo para subir todos los daños a la vez ---
    public void addAllDamageBonus(float amount) {
        this.kineticDamageBonus += amount;
        this.explosiveDamageBonus += amount;
        this.fireDamageBonus += amount;
        this.poisonDamageBonus += amount;
        this.iceDamageBonus += amount;
        this.energyDamageBonus += amount;
    }

    // --- NUEVO: Obtener bonus por DamageType (útil para modificadores) ---
    public float getDamageBonusByType(com.tikisadventure.combat.DamageType type) {
        switch(type) {
            case KINETIC: return kineticDamageBonus;
            case ENERGY: return energyDamageBonus;
            case EXPLOSIVE: return explosiveDamageBonus;
            case FIRE: return fireDamageBonus;
            case POISON: return poisonDamageBonus;
            case ICE: return iceDamageBonus;
            default: return 0f;
        }
    }

    public float getAttractionRange() { return attractionRange; }
    public void addAttractionRange(float amount) { this.attractionRange += amount; }

    public float getLifeLeechPercent() { return lifeLeechPercent; }
    public void addLifeLeechPercent(float amount) { this.lifeLeechPercent += amount; }

    public float getLifeRegenPercent() { return lifeRegenPercent; }
    public void addLifeRegenPercent(float amount) { this.lifeRegenPercent += amount; }

    public float getEvasionChance() { return evasionChance; }
    public void addEvasionChance(float amount) { this.evasionChance += amount; }


    //Robo de vida mecánica
    public void heal(float amount) {
        if (!isAlive() || healthComponent == null) return;

        healthComponent.currentHealth += amount;
        if (healthComponent.currentHealth > healthComponent.maxHealth) {
            healthComponent.currentHealth = healthComponent.maxHealth;
        }
        // Opcional: Podrías lanzar un EventBus.publish(new HealEvent(...)) si quieres mostrar "+1" verde sobre el jugador
    }

    // --- NUEVO: Comprobar si el jugador lleva un DamageType equipado ---
    public boolean hasDamageTypeEquipped(com.tikisadventure.combat.DamageType type) {
        // 1. Mirar las armas
        for (com.tikisadventure.combat.weapons.Weapon w : weaponManager.getWeapons()) {
            if (w.getDamageType() == type) return true;
        }

        // 2. Mirar las habilidades/granadas
        if (profile != null) {
            if (profile.specialAbility1 != null && profile.specialAbility1.getDamageType() == type) return true;
            if (profile.specialAbility2 != null && profile.specialAbility2.getDamageType() == type) return true;
        }

        return false;
    }

    @Override
    public void dispose() {
        super.dispose();
        if (experienceSystem != null) {
            experienceSystem.dispose();
        }
    }

    public void registerKill(String enemyType) {
        totalKills++;
        int current = killDetails.containsKey(enemyType) ? killDetails.get(enemyType) : 0;
        killDetails.put(enemyType, current + 1);
    }

    public void grantImmunity(float duration) {
        this.immunityTimer = duration;
        this.getTintColor().a = 1f;
    }

    public boolean isImmune() {
        return immunityTimer > 0 || (com.tikisadventure.core.GameSession.godMode && com.tikisadventure.core.GameSession.godModeIsImmortal);
    }

    @Override
    public void receiveDamage(float quantity, boolean isCritical, com.tikisadventure.combat.DamageType damageType) {
        // Lógica de Evasión para golpes de físicas o cuerpo a cuerpo
        if (com.badlogic.gdx.math.MathUtils.random() < this.getEvasionChance()) {
            com.tikisadventure.systems.events.EventBus.publish(new com.tikisadventure.systems.events.EvadeEvent(this));
            return; // ¡Esquivado! Cortamos el daño de raíz.
        }

        // Si no lo esquiva, recibe el daño de forma normal llamando a la clase Entity
        super.receiveDamage(quantity, isCritical, damageType);
    }

}
