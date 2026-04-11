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
import com.tikisadventure.core.Assets;
import com.tikisadventure.entities.base.Entity;

public class Player extends Entity {

    private CharacterProfile profile;
    private WeaponManager weaponManager;
    private Array<Projectile> activeProjectiles;
    private Array<Entity> allies;
    private com.tikisadventure.systems.ExperienceSystem experienceSystem;

    private float stateTime = 0;
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


    public Player(CharacterProfile profile) {
        super();
        this.profile = profile;
        this.vida = profile.maxHealth;
        this.vida_max = profile.maxHealth;
        this.speed = profile.speed;
        this.sprite = profile.idle.getKeyFrame(0);
        this.ANCHO = 1.5f;
        this.ALTO = 1.5f;
        this.weaponManager = new WeaponManager(this);
        this.activeProjectiles = new Array<>();
        this.allies = new Array<>();
        this.experienceSystem = new com.tikisadventure.systems.ExperienceSystem();
        this.posicion.set(0, 0);
    }

    public void applyDashImpulse(Vector2 impulse, float duration) {
        this.dashVelocity.set(impulse);
        this.dashTimer = duration;
        this.isDashing = true;
    }

    public void update(float delta, Array<Entity> enemies) {
        super.update(delta); // Importante para el timer de daño

        if (vida <= 0) return;
        applyKnockback(delta);
        stateTime += delta;

        if (dashTimer > 0) {
            posicion.mulAdd(dashVelocity, delta);
            dashTimer -= delta;
            updateTrail(delta);
            if (dashTimer <= 0) isDashing = false;
        } else {
            handleInput(delta);
            updateTrailFade(delta);
        }

        actualizarHitboxes();
        weaponManager.update(delta, enemies);
        updateAbilities(delta, enemies);
    }

    private void handleInput(float delta) {
        tempMove.set(0, 0);
        if (Gdx.input.isKeyPressed(Input.Keys.W)) { tempMove.y += 1; estadoActual = Estado.UP; }
        else if (Gdx.input.isKeyPressed(Input.Keys.S)) { tempMove.y -= 1; estadoActual = Estado.DOWN; }
        if (Gdx.input.isKeyPressed(Input.Keys.A)) { tempMove.x -= 1; estadoActual = Estado.LEFT; }
        else if (Gdx.input.isKeyPressed(Input.Keys.D)) { tempMove.x += 1; estadoActual = Estado.RIGHT; }

        if (tempMove.isZero()) estadoActual = Estado.IDLE;
        else {
            tempMove.nor();
            posicion.mulAdd(tempMove, speed * delta);
        }
    }

    private void updateTrail(float delta) {
        trailTimer += delta;
        if (trailTimer >= TRAIL_INTERVAL) {
            trailPositions.add(new Vector2(posicion.x, posicion.y));
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
        if (vida <= 0) return;

        for (Entity a : allies) a.render(batch, delta);

        TextureRegion currentFrame;
        switch (estadoActual) {
            case UP:    currentFrame = profile.up.getKeyFrame(stateTime, true); break;
            case DOWN:  currentFrame = profile.down.getKeyFrame(stateTime, true); break;
            case LEFT:  currentFrame = profile.left.getKeyFrame(stateTime, true); break;
            case RIGHT: currentFrame = profile.right.getKeyFrame(stateTime, true); break;
            default:    currentFrame = profile.idle.getKeyFrame(stateTime, true); break;
        }

        Color oldColor = batch.getColor();
        for (int i = 0; i < trailPositions.size; i++) {
            float alpha = (float) (i + 1) / (trailPositions.size + 1);
            batch.setColor(1, 1, 1, alpha * 0.4f);
            Vector2 p = trailPositions.get(i);
            batch.draw(currentFrame, p.x - ANCHO/2, p.y - ALTO/2, ANCHO, ALTO);
        }
        batch.setColor(oldColor);
        
        if (damageFlashTimer > 0) batch.setShader(null);
        for (Projectile p : activeProjectiles) p.render(batch);
        if (damageFlashTimer > 0) batch.setShader(Assets.whiteFlashShader);
        
        batch.draw(currentFrame, posicion.x - ANCHO/2, posicion.y - ALTO/2, ANCHO, ALTO);
        
        batch.setColor(Color.WHITE); // Restaurar opacidad antes de dibujar armas
        if (damageFlashTimer > 0) batch.setShader(null);
        weaponManager.render(batch);
        
        batch.setColor(1f, 1f, 1f, 1f); // Asegurar reset
    }

    private void updateAbilities(float delta, Array<Entity> enemies) {
        if (ability1CooldownTimer > 0) ability1CooldownTimer -= delta;
        else canUseAbility1 = true;
        if (ability2CooldownTimer > 0) ability2CooldownTimer -= delta;
        else canUseAbility2 = true;

        if (profile.specialAbility1 != null && Gdx.input.isKeyJustPressed(profile.ability1Key) && canUseAbility1) {
            profile.specialAbility1.activate(this, enemies);
            ability1CooldownTimer = profile.specialAbility1.getCooldown();
            canUseAbility1 = false;
        }
        if (profile.specialAbility2 != null && Gdx.input.isKeyJustPressed(profile.ability2Key) && canUseAbility2) {
            profile.specialAbility2.activate(this, enemies);
            ability2CooldownTimer = profile.specialAbility2.getCooldown();
            canUseAbility2 = false;
        }
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
    public void setScore(int score) { this.score = score; } // Muy importante para no perder puntos al cambiar de personaje

}
