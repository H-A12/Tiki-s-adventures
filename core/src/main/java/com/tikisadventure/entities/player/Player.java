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
        // Línea corregida (correcta):
        this.experienceSystem = new com.tikisadventure.systems.ExperienceSystem(this);

        this.healthComponent = new HealthComponent(profile.maxHealth);
        this.positionComponent.posicion.set(0, 0);
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

        if (com.tikisadventure.core.GameSession.godMode && com.tikisadventure.core.GameSession.godModeIsImmortal) {
            // Restauramos la vida al constantemente si se eligió "inmortal" en Modo Dios
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

    private boolean isButtonPressed(int keyCode) {
        if (keyCode == Input.Buttons.LEFT || keyCode == Input.Buttons.RIGHT || keyCode == Input.Buttons.MIDDLE) {
            return Gdx.input.isButtonJustPressed(keyCode);
        }
        return Gdx.input.isKeyJustPressed(keyCode);
    }

    private boolean isAiming = false;
    private Vector2 aimingTarget = new Vector2();
    private Vector2 inputDirection = new Vector2();
    private float cookingTime = 0;

    public Vector2 getInputDirection() { return inputDirection; }

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

        if (inputHandler.useDash && dashTimer <= 0) {
            Vector2 dashDir = inputHandler.moveDirection.cpy();
            if (!dashDir.isZero()) {
                dashDir.nor();
            } else {
                dashDir.set(inputHandler.aimDirection);
                if (dashDir.isZero()) {
                    dashDir.set(1, 0);
                }
            }
            applyDashImpulse(dashDir.scl(20f), 0.15f);
        }

        if (inputHandler.isInteracting) {
            // Interact handled at game level for doors
        }

        // Handle Aiming for Ability 2
        if (profile.specialAbility2 != null) {
            if (inputHandler.isAimingAbility2) {
                isAiming = true;
                cookingTime += delta;

                float maxRange = profile.specialAbility2.getMaxRange();

                if (!inputHandler.aimTargetAbility2.isZero()) {
                    aimingTarget.set(inputHandler.aimTargetAbility2);
                } else {
                    Vector2 dir = inputHandler.aimDirectionAbility2.cpy();
                    if (!dir.isZero()) {
                        dir.scl(maxRange);
                    }
                    aimingTarget.set(positionComponent.posicion).add(dir);
                }

            } else if (isAiming) {
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

    private void handleInput(InputHandler inputHandler, float delta) {
        if (!inputHandler.moveDirection.isZero()) {
            inputDirection.set(inputHandler.moveDirection).nor();
            velocityComponent.velocidad.set(inputDirection).scl(velocityComponent.speed);
            
            // Actualizar estado basado en dirección
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

        if (damageFlashTimer > 0) batch.setShader(null);
        batch.setColor(Color.WHITE);
        for (Projectile p : activeProjectiles) p.render(batch);
        batch.setColor(Color.WHITE);
        if (damageFlashTimer > 0) batch.setShader(Assets.whiteFlashShader);

        batch.draw(currentFrame, positionComponent.posicion.x - getANCHO()/2, positionComponent.posicion.y - getALTO()/2, getANCHO(), getALTO());

        batch.setColor(Color.WHITE);
        if (damageFlashTimer > 0) batch.setShader(null);
        weaponManager.render(batch);

        batch.setColor(1f, 1f, 1f, 1f);
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
            // Calculamos el extra (ej: si speed es 5 y percent es 0.05, el extra es 0.25)
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

    @Override
    public void dispose() {
        super.dispose();
        if (experienceSystem != null) {
            experienceSystem.dispose();
        }
    }
}
