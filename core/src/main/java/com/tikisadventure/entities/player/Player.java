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
import com.tikisadventure.floors.FloorManager;
import com.tikisadventure.input.InputHandler;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.tikisadventure.screens.GameScreen;
import com.tikisadventure.systems.powerUps.GlobalStatPowerUp;

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
    private static final float DASH_STEP_MAX = 0.4f;

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
    private boolean autoFireEnabled = true;

    //Robo de vida
    private float lifeLeechPercent = 0.0f; //Porcentaje de curación según daño infligido

    private TextureRegion arrowTexture;
    private TextureRegion doorArrowTexture;
    private float arrowBobTimer = 0f;

    public int totalKills = 0;
    public com.badlogic.gdx.utils.ObjectMap<String, Integer> killDetails = new com.badlogic.gdx.utils.ObjectMap<>();

    private float immunityTimer = 0f;
    private Color outlineColor = new Color(1f, 0.8f, 0f, 1f);

    private static final float QUICKSAND_MAX_TIME = 2.25f;
    private static final float QUICKSAND_SINK_RATE = 0.5f;
    public boolean isInQuicksand = false;
    public float quicksandTimer = 0f;
    public float quicksandSinkAmount = 0f;

    private static final float VOID_DEATH_DURATION = 1.8f;
    public boolean isInVoidTile = false;
    public float voidDeathTimer = 0f;

    public float regenTextAccumulator = 0f;
    public float leechTextAccumulator = 0f;

    public float regenTextTimer = 0f;
    public float leechTextTimer = 0f;

    public static final float MAX_EVASION = 0.75f;
    public static final float MAX_CRIT = 1.0f;
    public static final float MAX_DMG_BONUS = 9.99f;
    public static final float MAX_REGEN = 1.0f;
    public static final float MAX_LEECH = 1.0f;
    public static final float MAX_SPEED_BONUS = 3.0f;
    public static final float MAX_XP_MULTI = 10.99f;
    public static final float MAX_ATTRACTION_RANGE = 21.98f;
    public static final float MAX_LUCK = 1.0f;

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
    public boolean onFatalDamage() {
        if (GameScreen.activeScarecrow != null && GameScreen.activeScarecrow.isAlive() && !GameScreen.scarecrowLocked) {
            System.out.println("¡Muerte evadida! Resucitando en el Espantapájaros...");

            this.healthComponent.currentHealth = this.healthComponent.maxHealth;
            this.getPosition().set(GameScreen.activeScarecrow.getPosition());
            this.grantImmunity(2.0f);

            GameScreen.activeScarecrow.setAlive(false);
            GameScreen.activeScarecrow = null;
            GameScreen.scarecrowLocked = true;

            GameScreen.triggerScarecrowReviveEffects(this);

            return true; // Resucita
        }

        // Si no hay espantapájaros, nos aseguramos de que la vida sea exactamente 0
        if (this.healthComponent != null) {
            this.healthComponent.currentHealth = 0;
        }

        return false; // Muere definitivamente
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

    public float getAbility1CooldownRemaining() {
        return ability1CooldownTimer;
    }

    public float getAbility2CooldownRemaining() {
        return ability2CooldownTimer;
    }

    public void applyDashImpulse(Vector2 impulse, float duration) {
        this.dashVelocity.set(impulse);
        this.dashTimer = duration;
        this.isDashing = true;
        this.velocityComponent.velocidad.set(0, 0);
    }

    private void resolveDashWallCollision() {
        float x = positionComponent.posicion.x;
        float y = positionComponent.posicion.y;
        float halfSize = 0.5f;
        FloorManager fm = FloorManager.getInstance();

        if (fm.isWall(x - halfSize, y)) positionComponent.posicion.x = (float)Math.floor(x - halfSize) + 1 + halfSize;
        if (fm.isWall(x + halfSize, y)) positionComponent.posicion.x = (float)Math.floor(x + halfSize) - halfSize;
        if (fm.isWall(x, y - halfSize)) positionComponent.posicion.y = (float)Math.floor(y - halfSize) + 1 + halfSize;
        if (fm.isWall(x, y + halfSize)) positionComponent.posicion.y = (float)Math.floor(y + halfSize) - halfSize;


    }

    public void update(float delta, Array<Entity> enemies, InputHandler inputHandler) {
        super.update(delta);

        if (!com.tikisadventure.floors.FloorManager.getInstance().isQuicksand(
                positionComponent.posicion.x, positionComponent.posicion.y)) {
            isInQuicksand = false;
            quicksandTimer = 0f;
            quicksandSinkAmount = 0f;
        }

        if (inputHandler.isToggleAutoFireJustPressed) {
            autoFireEnabled = !autoFireEnabled;
        }

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

        if (healthComponent.currentHealth <= 0) {
            isAiming = false;
            return;
        }
        applyKnockback(delta);

        if (dashTimer > 0) {
            float dashSpeed = dashVelocity.len();
            if (dashSpeed > 0.001f) {
                float stepTime = DASH_STEP_MAX / dashSpeed;
                float remaining = delta;
                while (remaining > 0) {
                    float dt = Math.min(remaining, stepTime);
                    positionComponent.posicion.mulAdd(dashVelocity, dt);
                    resolveDashWallCollision();
                    remaining -= dt;
                }
            }
            dashTimer -= delta;
            updateTrail(delta);
            if (dashTimer <= 0) isDashing = false;
            isInQuicksand = false;
            quicksandTimer = 0f;
            quicksandSinkAmount = Math.max(0f, quicksandSinkAmount - delta * 0.5f);
        } else {
            handleInput(inputHandler, delta);
            updateTrailFade(delta);

            if (isInQuicksand) {
                quicksandTimer += delta;
                quicksandSinkAmount = Math.min(1.0f, quicksandTimer / QUICKSAND_MAX_TIME);
                if (quicksandTimer >= QUICKSAND_MAX_TIME) {
                    if (!onFatalDamage()) {
                        healthComponent.currentHealth = 0;
                    }
                }
            } else {
                quicksandSinkAmount = Math.max(0f, quicksandSinkAmount - delta * 0.5f);
            }
        }

        if (voidDeathTimer > 0) {
            voidDeathTimer += delta;
            isInQuicksand = false;
            quicksandSinkAmount = 0;
            inputDirection.setZero();
            estadoActual = Estado.IDLE;
            velocityComponent.velocidad.setZero();
            if (voidDeathTimer >= VOID_DEATH_DURATION) {
                if (!onFatalDamage()) {
                    healthComponent.currentHealth = 0;
                }
                voidDeathTimer = 0;
                isInVoidTile = false;
                this.getTintColor().a = 0;
            }
        }

        actualizarHitboxes();
        weaponManager.update(delta, enemies);
        updateAbilities(delta, enemies, inputHandler);

        if (dashTimer > 0) {
            isInQuicksand = false;
        }
    }

    private void handleInput(InputHandler inputHandler, float delta) {
        if (!inputHandler.moveDirection.isZero()) {
            inputDirection.set(inputHandler.moveDirection).nor();
            lastMoveDirection.set(inputDirection);
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

        if (isInQuicksand) {
            velocityComponent.velocidad.setZero();
            estadoActual = Estado.IDLE;
        }
    }

    private boolean isAiming = false;
    private Vector2 aimingTarget = new Vector2();
    private Vector2 inputDirection = new Vector2();
    private Vector2 lastMoveDirection = new Vector2(-1f, 0f);
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
                Vector2 dir = aimingTarget.cpy().sub(positionComponent.posicion);
                float distance = dir.len();
                if (distance > maxRange) {
                    dir.nor().scl(maxRange);
                    aimingTarget.set(positionComponent.posicion).add(dir);
                }
                profile.specialAbility2.activate(this, enemies, aimingTarget);
                ability2CooldownTimer = profile.specialAbility2.getCooldown();
                canUseAbility2 = false;
                isAiming = false;
                cookingTime = 0;
            }
        }
    }

    public boolean isAiming() { return isAiming; }
    public Vector2 getAimingTarget() { return aimingTarget; }
    public Vector2 getInputDirection() { return inputDirection; }
    public Vector2 getLastMoveDirection() { return lastMoveDirection; }

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
        // Quitamos el "if (health <= 0) return;" para que se pueda dibujar desvaneciéndose

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
            // El rastro también se desvanece con el jugador
            batch.setColor(1, 1, 1, alpha * 0.4f * getTintColor().a);
            Vector2 p = trailPositions.get(i);
            batch.draw(currentFrame, p.x - getANCHO()/2, p.y - getALTO()/2, getANCHO(), getALTO());
        }
        batch.setColor(oldColor);

        batch.setShader(null);
        batch.setColor(Color.WHITE);
        for (Projectile p : activeProjectiles) p.render(batch);
        batch.setColor(Color.WHITE);

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
            batch.setShader(null);
        }

        // Dibujamos al jugador con su Alpha (que irá bajando a 0 en la muerte)
        batch.setColor(getTintColor());

        if (quicksandSinkAmount > 0.01f && currentFrame.getTexture() != null) {
            float visibleHeight = getALTO() * (1.0f - quicksandSinkAmount);
            float drawY = positionComponent.posicion.y + getALTO() / 2f - visibleHeight;
            int texRegionHeight = currentFrame.getRegionHeight();
            int srcY = currentFrame.getRegionY();
            int srcHeight = (int) (texRegionHeight * (1.0f - quicksandSinkAmount));
            if (srcHeight > 0) {
                batch.draw(currentFrame.getTexture(),
                    positionComponent.posicion.x - getANCHO() / 2f,
                    drawY,
                    getANCHO(), visibleHeight,
                    currentFrame.getRegionX(), srcY,
                    currentFrame.getRegionWidth(), srcHeight,
                    currentFrame.isFlipX(), currentFrame.isFlipY());
            }
        } else if (voidDeathTimer > 0.01f) {
            float progress = Math.min(1.0f, voidDeathTimer / VOID_DEATH_DURATION);
            float scale = 1.0f - progress;
            float drawW = getANCHO() * scale;
            float drawH = getALTO() * scale;
            batch.draw(currentFrame,
                positionComponent.posicion.x - drawW / 2f,
                positionComponent.posicion.y,
                drawW, drawH);
        } else {
            batch.draw(currentFrame, positionComponent.posicion.x - getANCHO()/2, positionComponent.posicion.y - getALTO()/2, getANCHO(), getALTO());
        }

        batch.setShader(null);
        batch.setColor(Color.WHITE);
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
    public void setLuck(float suerte) {this.luck = Math.min(MAX_LUCK, suerte);}

    public float getXpMultiplier() { return xpMultiplier; }
    public void setXpMultiplier(float xpMultiplier) { this.xpMultiplier = Math.min(MAX_XP_MULTI, xpMultiplier); }

    public void addSpeedPercent(float percent) {
        if (this.velocityComponent != null && profile != null) {
            float baseSpeed = profile.speed;
            float currentBonusPct = (this.velocityComponent.speed / baseSpeed) - 1.0f;
            float newBonusPct = Math.min(MAX_SPEED_BONUS, currentBonusPct + percent);
            this.velocityComponent.speed = baseSpeed * (1.0f + newBonusPct);
        }
    }

    public float getSpeed() {
        if (this.velocityComponent != null) {
            return this.velocityComponent.speed;
        }
        return 0f;
    }

    public float getKineticDamageBonus() { return kineticDamageBonus; }
    public void addKineticDamageBonus(float amount) { this.kineticDamageBonus = Math.min(MAX_DMG_BONUS, this.kineticDamageBonus + amount); }

    public float getExplosiveDamageBonus() { return explosiveDamageBonus; }
    public void addExplosiveDamageBonus(float amount) { this.explosiveDamageBonus = Math.min(MAX_DMG_BONUS, this.explosiveDamageBonus + amount); }

    public float getFireDamageBonus() { return fireDamageBonus; }
    public void addFireDamageBonus(float amount) { this.fireDamageBonus = Math.min(MAX_DMG_BONUS, this.fireDamageBonus + amount); }

    public float getPoisonDamageBonus() { return poisonDamageBonus; }
    public void addPoisonDamageBonus(float amount) { this.poisonDamageBonus = Math.min(MAX_DMG_BONUS, this.poisonDamageBonus + amount); }

    public float getIceDamageBonus() { return iceDamageBonus; }
    public void addIceDamageBonus(float amount) { this.iceDamageBonus = Math.min(MAX_DMG_BONUS, this.iceDamageBonus + amount); }

    public float getCritChanceBonus() { return critChanceBonus; }
    public void addCritChanceBonus(float amount) { this.critChanceBonus = Math.min(MAX_CRIT, this.critChanceBonus + amount); }

    public float getExtraHealthGained() { return extraHealthGained; }
    public void addExtraHealthGained(float amount) { this.extraHealthGained += amount; }

    // --- NUEVO: Daño de Energía ---
    private float energyDamageBonus = 0f;
    public float getEnergyDamageBonus() { return energyDamageBonus; }
    public void addEnergyDamageBonus(float amount) { this.energyDamageBonus = Math.min(MAX_DMG_BONUS, this.energyDamageBonus + amount); }

    // --- NUEVO: Atajo para subir todos los daños a la vez ---
    public void addAllDamageBonus(float amount) {
        addKineticDamageBonus(amount);
        addExplosiveDamageBonus(amount);
        addFireDamageBonus(amount);
        addPoisonDamageBonus(amount);
        addIceDamageBonus(amount);
        addEnergyDamageBonus(amount);
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
    public void addAttractionRange(float amount) { this.attractionRange = Math.min(MAX_ATTRACTION_RANGE, this.attractionRange + amount); }

    public float getLifeLeechPercent() { return lifeLeechPercent; }
    public void addLifeLeechPercent(float amount) { this.lifeLeechPercent = Math.min(MAX_LEECH, this.lifeLeechPercent + amount); }

    public float getLifeRegenPercent() { return lifeRegenPercent; }
    public void addLifeRegenPercent(float amount) { this.lifeRegenPercent = Math.min(MAX_REGEN, this.lifeRegenPercent + amount); }

    public float getEvasionChance() { return evasionChance; }
    public void addEvasionChance(float amount) { this.evasionChance = Math.min(MAX_EVASION, this.evasionChance + amount); }
    public boolean isAutoFireEnabled() { return autoFireEnabled; }
    public void setAutoFireEnabled(boolean enabled) { this.autoFireEnabled = enabled; }


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

        // 2. Mirar solo el gadget (specialAbility2), NO el Dash (specialAbility1)
        // El Dash (specialAbility1) no inflige daño, así que no debe contar para el filtro de powerUps
        if (profile != null && profile.specialAbility2 != null && profile.specialAbility2.getDamageType() == type) {
            return true;
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

    public boolean isStatCapped(GlobalStatPowerUp.StatType type) {
        switch (type) {
            case EVASION: return evasionChance >= MAX_EVASION;
            case CRIT_CHANCE: return critChanceBonus >= MAX_CRIT;
            case KINETIC_DMG: return kineticDamageBonus >= MAX_DMG_BONUS;
            case EXPLOSIVE_DMG: return explosiveDamageBonus >= MAX_DMG_BONUS;
            case ENERGY_DMG: return energyDamageBonus >= MAX_DMG_BONUS;
            case FIRE_DMG: return fireDamageBonus >= MAX_DMG_BONUS;
            case ICE_DMG: return iceDamageBonus >= MAX_DMG_BONUS;
            case POISON_DMG: return poisonDamageBonus >= MAX_DMG_BONUS;
            case LIFE_REGEN: return lifeRegenPercent >= MAX_REGEN;
            case LIFE_LEECH: return lifeLeechPercent >= MAX_LEECH;
            case SPEED: {
                if (profile == null || velocityComponent == null) return false;
                float bonusPct = (velocityComponent.speed / profile.speed) - 1.0f;
                return bonusPct >= MAX_SPEED_BONUS;
            }
            case XP_GAIN_PERCENT: return xpMultiplier >= MAX_XP_MULTI;
            case ATTRACTION_RANGE: return attractionRange >= MAX_ATTRACTION_RANGE;
            case LUCK: return luck >= MAX_LUCK;
            default: return false;
        }
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
