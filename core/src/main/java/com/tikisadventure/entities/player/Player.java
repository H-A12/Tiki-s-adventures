package com.tikisadventure.entities.player;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.tikisadventure.combat.weapons.Bullet;
import com.tikisadventure.combat.weapons.WeaponManager;
import com.tikisadventure.entities.Entity;
import com.tikisadventure.systems.ExperienceSystem;

public class Player extends Entity {

    private CharacterProfile profile;
    private WeaponManager weaponManager;
    private Array<Bullet> activeBullets;
    private ExperienceSystem experienceSystem;

    private float abilityCooldownTimer = 0;
    private boolean canUseAbility = true;
    private Vector2 tempMove = new Vector2();

    public Player(CharacterProfile profile) {
        super();
        this.profile = profile;

        this.vida = profile.maxHealth;
        this.vida_max = profile.maxHealth;
        this.speed = profile.speed;
        this.sprite = profile.sprite;

        this.ANCHO = 1f;
        this.ALTO = 1f;

        this.weaponManager = new WeaponManager(this);
        this.activeBullets = new Array<>();
        this.experienceSystem = new ExperienceSystem();

        this.posicion.set(0, 0);
    }

    // --- MÉTODOS DE ACCESO (GETTERS) ---

    /**
     * Devuelve el perfil del personaje.
     * Esto soluciona el error 'cannot find symbol: method getProfile()'
     */
    public CharacterProfile getProfile() {
        return this.profile;
    }

    public ExperienceSystem getExperienceSystem() {
        return this.experienceSystem;
    }

    public WeaponManager getWeaponManager() {
        return weaponManager;
    }

    // --- LÓGICA DE JUEGO ---

    @Override
    public void update(float delta, Entity target) {
        // Implementación requerida por la clase abstracta Entity
    }

    public void update(float delta, Array<Entity> enemies) {
        if (vida <= 0) return;

        handleInput(delta);
        actualizarHitboxes();
        weaponManager.update(delta, enemies);
        updateAbility(delta, enemies);
        updateProjectiles(delta, enemies);
    }

    private void handleInput(float delta) {
        tempMove.set(0, 0);

        if (Gdx.input.isKeyPressed(Input.Keys.W)) tempMove.y += 1;
        if (Gdx.input.isKeyPressed(Input.Keys.S)) tempMove.y -= 1;
        if (Gdx.input.isKeyPressed(Input.Keys.A)) tempMove.x -= 1;
        if (Gdx.input.isKeyPressed(Input.Keys.D)) tempMove.x += 1;

        if (!tempMove.isZero()) {
            tempMove.nor();
            posicion.mulAdd(tempMove, speed * delta);
        }
    }

    private void updateAbility(float delta, Array<Entity> enemies) {
        if (abilityCooldownTimer > 0) {
            abilityCooldownTimer -= delta;
        } else {
            canUseAbility = true;
        }

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
            if (!b.isAlive()) {
                activeBullets.removeIndex(i);
            }
        }
    }

    @Override
    public void render(Batch batch, float delta) {
        if (vida <= 0 || sprite == null) return;

        for (Bullet b : activeBullets) {
            b.render(batch);
        }

        batch.draw(sprite, posicion.x - ANCHO/2, posicion.y - ALTO/2, ANCHO, ALTO);
        weaponManager.render(batch);
    }

    public void addBullet(Bullet b) {
        activeBullets.add(b);
    }

    public float getAbilityCooldownPercent() {
        if (profile.specialAbility == null) return 0;
        return Math.max(0, abilityCooldownTimer / profile.specialAbility.getCooldown());
    }

    // Compatibilidad con Entity
    public float getANCHO() { return ANCHO; }
    public float getALTO() { return ALTO; }
}
