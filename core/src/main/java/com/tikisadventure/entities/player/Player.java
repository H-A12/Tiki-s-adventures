package com.tikisadventure.entities.player;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.tikisadventure.combat.weapons.Bullet;
import com.tikisadventure.combat.weapons.WeaponManager;
import com.tikisadventure.entities.Entity;

public class Player extends Entity {

    private CharacterProfile profile;
    private WeaponManager weaponManager;
    private Array<Bullet> activeBullets;
    private com.tikisadventure.systems.ExperienceSystem experienceSystem;

    // --- ANIMACIÓN Y ESTADO ---
    private float stateTime = 0;
    public enum Estado { IDLE, UP, DOWN, LEFT, RIGHT }
    private Estado estadoActual = Estado.IDLE;

    // --- DASH Y ESTELA ---
    private Vector2 dashVelocity = new Vector2();
    private float dashTimer = 0;
    private boolean isDashing = false;

    private Array<Vector2> trailPositions = new Array<>();
    private float trailTimer = 0;
    private final float TRAIL_INTERVAL = 0.04f;

    private float abilityCooldownTimer = 0;
    private boolean canUseAbility = true;
    private Vector2 tempMove = new Vector2();

    public Player(CharacterProfile profile) {
        super();
        this.profile = profile;

        this.vida = profile.maxHealth;
        this.vida_max = profile.maxHealth;
        this.speed = profile.speed;

        // El sprite inicial será el primer frame de IDLE
        this.sprite = profile.idle.getKeyFrame(0);

        this.ANCHO = 1.5f;
        this.ALTO = 1.5f;

        this.weaponManager = new WeaponManager(this);
        this.activeBullets = new Array<>();
        this.experienceSystem = new com.tikisadventure.systems.ExperienceSystem();

        this.posicion.set(0, 0);
    }

    public void applyDashImpulse(Vector2 impulse, float duration) {
        this.dashVelocity.set(impulse);
        this.dashTimer = duration;
        this.isDashing = true;
    }

    @Override
    public void update(float delta, Entity target) {}

    public void update(float delta, Array<Entity> enemies) {
        if (vida <= 0) return;
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
        updateAbility(delta, enemies);
        updateProjectiles(delta, enemies);
    }

    private void handleInput(float delta) {
        tempMove.set(0, 0);

        if (Gdx.input.isKeyPressed(Input.Keys.W)) { tempMove.y += 1; estadoActual = Estado.UP; }
        else if (Gdx.input.isKeyPressed(Input.Keys.S)) { tempMove.y -= 1; estadoActual = Estado.DOWN; }

        if (Gdx.input.isKeyPressed(Input.Keys.A)) { tempMove.x -= 1; estadoActual = Estado.LEFT; }
        else if (Gdx.input.isKeyPressed(Input.Keys.D)) { tempMove.x += 1; estadoActual = Estado.RIGHT; }

        if (tempMove.isZero()) {
            estadoActual = Estado.IDLE;
        } else {
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
    public void render(Batch batch, float delta) {
        if (vida <= 0) return;

        // 1. Obtener frame de animación según estado
        // Al tener left.png y right.png por separado, NO necesitamos hacer flip manual
        TextureRegion currentFrame;
        switch (estadoActual) {
            case UP:    currentFrame = profile.up.getKeyFrame(stateTime, true); break;
            case DOWN:  currentFrame = profile.down.getKeyFrame(stateTime, true); break;
            case LEFT:  currentFrame = profile.left.getKeyFrame(stateTime, true); break;
            case RIGHT: currentFrame = profile.right.getKeyFrame(stateTime, true); break;
            default:    currentFrame = profile.idle.getKeyFrame(stateTime, true); break;
        }

        // 2. Renderizar Estela (Trail)
        Color oldColor = batch.getColor();
        for (int i = 0; i < trailPositions.size; i++) {
            float alpha = (float) (i + 1) / (trailPositions.size + 1);
            batch.setColor(1, 1, 1, alpha * 0.4f);
            Vector2 p = trailPositions.get(i);
            batch.draw(currentFrame, p.x - ANCHO/2, p.y - ALTO/2, ANCHO, ALTO);
        }
        batch.setColor(oldColor);

        // 3. Renderizar Balas y Personaje (Sin lógica de flip, usamos el asset directo)
        for (Bullet b : activeBullets) b.render(batch);
        batch.draw(currentFrame, posicion.x - ANCHO/2, posicion.y - ALTO/2, ANCHO, ALTO);
        weaponManager.render(batch);
    }

    // --- MÉTODOS DE APOYO ---
    private void updateAbility(float delta, Array<Entity> enemies) {
        if (abilityCooldownTimer > 0) abilityCooldownTimer -= delta;
        else canUseAbility = true;

        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE) && canUseAbility) {
            if (profile.specialAbility != null) {
                profile.specialAbility.activate(this, enemies);
                abilityCooldownTimer = profile.specialAbility.getCooldown();
                canUseAbility = false;
            }
        }
    }

    private void updateProjectiles(float delta, Array<Entity> enemies) {
        for (int i = activeBullets.size - 1; i >= 0; i--) {
            Bullet b = activeBullets.get(i);
            b.update(delta, enemies);
            if (!b.isAlive()) activeBullets.removeIndex(i);
        }
    }

    public void addBullet(Bullet b) { activeBullets.add(b); }
    public CharacterProfile getProfile() { return this.profile; }
    public com.tikisadventure.systems.ExperienceSystem getExperienceSystem() { return this.experienceSystem; }
    public WeaponManager getWeaponManager() { return weaponManager; }
    public boolean isDashing() { return isDashing; }
}
