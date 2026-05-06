package com.tikisadventure.combat.weapons;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.tikisadventure.combat.DamageType;
import com.tikisadventure.combat.WeaponCategory;
import com.tikisadventure.combat.projectiles.Projectile;
import com.tikisadventure.effects.EffectManager;
import com.tikisadventure.entities.base.Entity;
import com.tikisadventure.entities.player.Player;
import com.tikisadventure.systems.events.EventBus;
import com.tikisadventure.systems.events.FiredEvent;

public class Weapon {
    // Stats
    protected int price = 0;
    protected int tier = 1;
    protected WeaponCategory category = WeaponCategory.PISTOL;
    protected float critChance = 0.05f;
    protected float critDamageMult = 1.5f;
    protected DamageType damageType = DamageType.KINETIC;

    protected float cd = 1f;
    protected float lastShootTime = 0;
    protected float damage = 10f;
    protected float bulletSpeed = 10f;
    protected float bulletSize = 0.2f;
    protected float shootRange = 10f;
    protected float recoilForce = 0.0f;
    protected float recoilRecovery = 0.0f;
    protected float projectileLifetime = 5.0f;
    protected float spread = 0f;
    protected float imprecision = 0f;
    protected int projectileCount = 1;
    protected int penetration = 0;
    protected float impactKnockback = 0f;
    protected float growthRate = 0f;
    protected float maxRadius = Float.MAX_VALUE;
    protected float rotationSpeed = 0f;
    protected float spreadDelay = 0f;
    protected Array<PendingShot> pendingShots = new Array<>();
    protected static class PendingShot {
        Vector2 position;
        Vector2 direction;
        float delay;

        PendingShot(Vector2 pos, Vector2 dir, float delay) {
            this.position = pos;
            this.direction = dir;
            this.delay = delay;
        }
    }

    protected Vector2 spawnOffset = new Vector2(0, 0);
    protected Vector2 muzzleFlashOffset = new Vector2(0, 0);
    protected TextureRegion projectileTexture;
    protected ProjectileCreator projectileCreator;
    protected String trailType;
    protected float trailInterval = 0f;
    protected String muzzleFlashType;
    protected Array<Emitter> emitters = new Array<>();
    protected Array<ProjectileModifier> modifiers = new Array<>();

    // State
    protected Vector2 recoilOffset = new Vector2(0, 0);
    protected Entity objetive;
    protected Vector2 worldPosition = new Vector2();
    protected Entity owner;
    protected TextureRegion sprite;
    protected EffectManager effectManager;
    protected float visualAngle;
    protected float pivotX = 0.5f;
    protected float pivotY = 0.5f;
    protected Vector2 swingOffset = new Vector2();
    protected float swingRotation = 0f;
    protected float searchTimer = 0;
    protected static final float SEARCH_INTERVAL = 0.1f;
    protected boolean manualAimActive = false;
    protected Vector2 manualTargetPoint = new Vector2();

    protected boolean isSwinging = false;
    protected float swingTimer = 0f;
    protected boolean swingFlip = false;
    protected float swingDuration = 0.15f; //Velocidad del tajo de arma melee
    protected float swingArc = 120f; // Angulo del tajo de arma melee
    protected float returnDelayTimer = 0f;

    public int activeBoomerangs = 0;
    protected boolean fixedSpread = false;

    protected String name = "Arma Base";

    // Añade sus getters y setters
    public int getTier() {
        return tier;
    }

    public String getName() {
        return this.name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public Weapon(Entity owner, ProjectileCreator pc, EffectManager effectManager) {
        this.owner = owner;
        this.projectileCreator = pc;
        this.effectManager = effectManager;
    }

    // Setters
    public void setPrice(int price) { this.price = price; }
    public void setTier(int tier) { this.tier = tier; }
    public void setCategory(WeaponCategory category) { this.category = category; }
    public void setCritChance(float critChance) { this.critChance = critChance; }
    public void setCritDamageMult(float critDamageMult) { this.critDamageMult = critDamageMult; }
    public void setDamageType(DamageType type) { this.damageType = type; }
    public void setDamage(float damage) { this.damage = damage; }
    public void setCooldown(float cd) { this.cd = cd; }
    public void setShootRange(float range) { this.shootRange = range; }
    public void setProjectileTexture(TextureRegion texture) { this.projectileTexture = texture; }
    public void setBulletSpeed(float speed) { this.bulletSpeed = speed; }
    public void setBulletSize(float size) { this.bulletSize = size; }
    public void setRecoil(float force, float recovery) { this.recoilForce = force; this.recoilRecovery = recovery; }
    public void setProjectileCount(int count) { this.projectileCount = count; }
    public void setPenetration(int penetration) { this.penetration = penetration; }
    public void setImpactKnockback(float knockback) { this.impactKnockback = knockback; }
    public void setGrowthRate(float rate) { this.growthRate = rate; }
    public void setMaxRadius(float max) { this.maxRadius = max; }
    public void setRotationSpeed(float speed) { this.rotationSpeed = speed; }
    public void setSpread(float spread) { this.spread = spread; }
    public void setSpreadDelay(float delay) { this.spreadDelay = delay; }
    public void setImprecision(float imprecision) { this.imprecision = imprecision; }
    public void setSpawnOffset(Vector2 offset) { this.spawnOffset.set(offset); }
    public void setMuzzleFlashOffset(Vector2 offset) { this.muzzleFlashOffset.set(offset); }
    public void setMuzzleFlashType(String type) { this.muzzleFlashType = type; }
    public void setTrail(String type, float interval) { this.trailType = type; this.trailInterval = interval; }
    public void setProjectileLifetime(float lifetime) { this.projectileLifetime = lifetime; }
    public void addEmitter(Emitter e) { this.emitters.add(e); }
    public void addModifier(ProjectileModifier m) { this.modifiers.add(m); }
    public void setSprite(TextureRegion sprite) { this.sprite = sprite; }
    public void setManualAim(boolean active, Vector2 targetPoint) {
        this.manualAimActive = active;
        if (active) this.manualTargetPoint.set(targetPoint);
    }
    public void setPivot(float x, float y) {
        this.pivotX = x;
        this.pivotY = y;
    }
    public void setFixedSpread(boolean fixedSpread) { this.fixedSpread = fixedSpread; }

    // Getters
    public float getDamage() { return damage; }
    public DamageType getDamageType() { return damageType; }
    public float getCritChance() { return critChance; }
    public float getCritDamageMult() { return critDamageMult; }
    public Array<ProjectileModifier> getModifiers() { return modifiers; }
    //Getters de la espada
    public WeaponCategory getCategory() { return category; }
    public Float getTargetAngleFromOwner() {
        if (manualAimActive) {
            return new Vector2(manualTargetPoint).sub(owner.getPosition()).angleRad();
        } else if (objetive != null && objetive.isAlive()) {
            return new Vector2(objetive.getPosition()).sub(owner.getPosition()).angleRad();
        }
        return null;
    }
    public boolean isSwinging() { return isSwinging; }
    public float getSwingRotation() { return swingRotation; }
    public float getCooldown() { return cd; }

    public TextureRegion getSprite() {
        return this.sprite;
    }

    // Logic
    public void update(float delta, Array<Entity> enemies) {
        searchEnemy(enemies, delta);
        tryAttack(delta, enemies);
        recoilOffset.lerp(Vector2.Zero, recoilRecovery * delta);

        //Animacion espada
        if (isSwinging) {
            swingTimer += delta;
            float progress = swingTimer / swingDuration;

            if (progress >= 1f) {
                isSwinging = false;
                swingFlip = !swingFlip;
                returnDelayTimer = this.cd * 0.3f;
            } else {
                float startAngle = swingFlip ? (swingArc / 2f) : -(swingArc / 2f);
                float endAngle = swingFlip ? -(swingArc / 2f) : (swingArc / 2f);
                swingRotation = startAngle + (endAngle - startAngle) * progress;
            }
        } else {
            if (returnDelayTimer > 0) {
                returnDelayTimer -= delta;
            } else {
                swingRotation = com.badlogic.gdx.math.MathUtils.lerp(swingRotation, 0f, 15f * delta);
            }
        }
        updateVisual();
        processPendingShots(delta, enemies);
    }

    private void processPendingShots(float delta, Array<Entity> enemies) {
        if (pendingShots.size == 0) return;

        for (int i = pendingShots.size - 1; i >= 0; i--) {
            PendingShot shot = pendingShots.get(i);
            shot.delay -= delta;
            if (shot.delay <= 0) {
                fireSingleProjectile(shot.position, shot.direction);
                pendingShots.removeIndex(i);
            }
        }
    }

    private void searchEnemy(Array<Entity> enemies, float delta) {
        if (manualAimActive) return;
        if (owner instanceof Player && !((Player) owner).isAutoFireEnabled()) {
            objetive = null;
            return;
        }

        searchTimer += delta;
        if (searchTimer < SEARCH_INTERVAL) return;
        searchTimer = 0;

        Entity closest = null;
        float minDistanceSq = Float.MAX_VALUE;
        for (Entity e : enemies) {
            if (!e.isAlive()) continue;
            float distanceSq = worldPosition.dst2(e.getPosition());
            if (distanceSq < minDistanceSq && distanceSq <= shootRange * shootRange) {
                minDistanceSq = distanceSq;
                closest = e;
            }
        }
        objetive = closest;
    }

    private Vector2 getActiveFireDirection() {
        if (manualAimActive) {
            return new Vector2(manualTargetPoint).sub(worldPosition).nor();
        } else if (objetive != null && objetive.isAlive()) {
            return new Vector2(objetive.getPosition()).sub(worldPosition).nor();
        }
        return null;
    }

    private void updateVisual() {
        Vector2 fireDir = getActiveFireDirection();
        if (fireDir != null) {
            visualAngle = fireDir.angleDeg();
        }
    }

    private void tryAttack(float delta, Array<Entity> enemies) {
        lastShootTime += delta;
        Vector2 fireDir = getActiveFireDirection();

        if (fireDir == null) return;

        if (lastShootTime >= cd && activeBoomerangs == 0) {
            fireShot(fireDir, enemies); // <--- AHORA PASAMOS LOS ENEMIGOS
            lastShootTime = 0;
        }
    }

    public void applyRecoil(float customForce, float customRecovery) {
        Vector2 fireDir = getActiveFireDirection();
        if (fireDir == null) return;

        this.recoilRecovery = customRecovery;
        recoilOffset.set(fireDir).scl(-customForce);
    }

    public float getFinalDamage() {
        float baseDamage = this.damage;

        if (owner instanceof Player) {
            Player playerOwner = (Player) owner;
            float bonus = playerOwner.getDamageBonusByType(this.damageType);
            baseDamage *= (1f + bonus);
        }

        return baseDamage;
    }

    private void fireShot(Vector2 baseDir, Array<Entity> enemies) {

        if (this.category == WeaponCategory.MELEE) {
            isSwinging = true;
            swingTimer = 0f;
            swingDuration = Math.min(this.cd * 0.6f, 0.25f);

            float baseAngle = baseDir.angleDeg();
            float force = impactKnockback > 0 ? impactKnockback : 15f; // Empuje por defecto

            //A que enemigos damos con el tajo?
            for (Entity e : enemies) {
                if (!e.isAlive()) continue;

                float dist = worldPosition.dst(e.getPosition());
                // Esta cerca?
                if (dist <= shootRange) {
                    Vector2 toEnemy = new Vector2(e.getPosition()).sub(worldPosition);
                    float enemyAngle = toEnemy.angleDeg();

                    //Esta en el angulo del tajo?
                    float diff = Math.abs(enemyAngle - baseAngle) % 360;
                    if (diff > 180) diff = 360 - diff;

                    if (diff <= swingArc / 2f) {
                        e.receiveDamage(getFinalDamage(), false, damageType);

                        //Knockback
                        if (e instanceof com.tikisadventure.components.traits.Knockbackable) {
                            ((com.tikisadventure.components.traits.Knockbackable) e).getKnockbackVelocity().add(toEnemy.nor().scl(force));
                        }
                    }
                }
            }
            return;
        }

        //Armas a distancia
        if (recoilForce > 0) {
            applyRecoil(recoilForce, recoilRecovery);
        }

        float baseAngle = baseDir.angleDeg();
        float weaponAngle = baseAngle;

        Vector2 muzzleFlashPos = new Vector2(worldPosition);
        if (muzzleFlashType != null) {
            boolean isFlipped = weaponAngle > 90 && weaponAngle < 270;
            float offsetAngle = isFlipped ? weaponAngle - 180 : weaponAngle;

            Vector2 muzzleOffset = new Vector2(muzzleFlashOffset);
            if (isFlipped) {
                muzzleOffset.x = -muzzleOffset.x;
            }
            Vector2 rotatedMuzzleOffset = muzzleOffset.rotateDeg(offsetAngle);
            muzzleFlashPos = new Vector2(worldPosition).add(rotatedMuzzleOffset);
        }

        for (Emitter emitter : emitters) {
            boolean isFlipped = weaponAngle > 90 && weaponAngle < 270;
            float offsetAngle = isFlipped ? weaponAngle - 180 : weaponAngle;
            Vector2 emitterOffset = new Vector2(emitter.offset);
            if (isFlipped) {
                emitterOffset.x = -emitterOffset.x;
            }
            Vector2 rotatedOffset = emitterOffset.rotateDeg(offsetAngle);
            Vector2 emitterPos = new Vector2(worldPosition).add(rotatedOffset);
            Vector2 ejectionDir = new Vector2(1, 0).setAngleDeg(offsetAngle);
            EventBus.publish(new FiredEvent(emitterPos, ejectionDir, emitter.type, null));
        }

        if (muzzleFlashType != null) {
            EventBus.publish(new FiredEvent(muzzleFlashPos, baseDir, null, muzzleFlashType));
        }

        boolean isFlipped = weaponAngle > 90 && weaponAngle < 270;
        float spawnOffsetAngle = isFlipped ? weaponAngle - 180 : weaponAngle;
        Vector2 spawnOffsetVec = new Vector2(spawnOffset);
        if (isFlipped) {
            spawnOffsetVec.x = -spawnOffsetVec.x;
        }
        Vector2 rotatedSpawnOffset = spawnOffsetVec.rotateDeg(spawnOffsetAngle);

        if (spreadDelay > 0 && projectileCount > 1) {
            for (int i = 0; i < projectileCount; i++) {
                float angle = baseAngle;
                angle += MathUtils.random(-spread / 2f, spread / 2f);
                if (imprecision > 0) {
                    angle += MathUtils.random(-imprecision, imprecision);
                }
                Vector2 dir = new Vector2(1, 0).setAngleDeg(angle);
                Vector2 spawnPos = new Vector2(worldPosition).add(rotatedSpawnOffset);
                pendingShots.add(new PendingShot(spawnPos, dir, i * spreadDelay));
            }
        } else {
            for (int i = 0; i < projectileCount; i++) {
                float angle = baseAngle;

                if (projectileCount > 1) {
                    if (this.fixedSpread) {
                        // Cálculo perfecto para abanico:
                        // Divide el ángulo total de spread entre los espacios entre balas
                        float angleStep = spread / (projectileCount - 1);
                        float startAngle = -(spread / 2f);
                        angle += startAngle + (i * angleStep);
                    } else {
                        angle += MathUtils.random(-spread / 2f, spread / 2f);
                    }
                }

                if (imprecision > 0) {
                    angle += MathUtils.random(-imprecision, imprecision);
                }
                Vector2 dir = new Vector2(1, 0).setAngleDeg(angle);

                Projectile p = projectileCreator.create(
                    new Vector2(worldPosition).add(rotatedSpawnOffset),
                    dir, bulletSpeed, getFinalDamage(), bulletSize,
                    projectileTexture, effectManager, trailType, trailInterval,
                    projectileLifetime, critChance, critDamageMult, impactKnockback,
                    this.owner
                );
                p.setDamageType(this.damageType);
                p.setPenetration(this.penetration);
                p.setGrowthRate(this.growthRate);
                p.setMaxRadius(this.maxRadius);
                p.setRotationSpeed(this.rotationSpeed);

                for (ProjectileModifier modifier : modifiers) {
                    modifier.apply(p, effectManager);
                }

                if (owner instanceof Player) {
                    ((Player) owner).addProjectile(p);
                }
            }
        }
    }

    public void render(Batch batch) {

        if (sprite == null) return;
        if (activeBoomerangs > 0) return;

        float width = 1.2f;
        float height = 1.2f;

        if ("Enchufe alcalino".equals(name)) {
            width = 1.5f;
            height = 1.5f;
        }
        if ("Fusil de bolas".equals(name)) {
            width = 1.3f;
            height = 1.3f;
        }

        float originX = pivotX * width;
        float originY = pivotY * height;

        // Logica de rotacion fija
        float finalDrawingAngle;

        // Si es espada y no ataca
        if (this.category == WeaponCategory.MELEE && !isSwinging && Math.abs(swingRotation) < 1f) {
            finalDrawingAngle = -45f;
        } else {
            // Si atacas rota
            float baseRotation = (this.category == WeaponCategory.MELEE) ? -90f : 0f;
            finalDrawingAngle = visualAngle + swingRotation + baseRotation;
        }

        // Flip a armas a distancia solo
        float scaleY = 1f;
        if (this.category != WeaponCategory.MELEE) {
            scaleY = (finalDrawingAngle > 90 && finalDrawingAngle < 270) ? -1f : 1f;
        }

        // --- INICIO LOGICA DE SHADERS (CONTORNO POR TIER) ---
        boolean useOutline = (this.tier > 1); // Solo Tier 2 en adelante tienen contorno

        if (useOutline && com.tikisadventure.core.Assets.outlineShader != null) {
            batch.flush(); // OBLIGATORIO en LibGDX antes de cambiar de shader
            batch.setShader(com.tikisadventure.core.Assets.outlineShader);

            // Tamaño del pixel (usamos el tamaño de la textura completa en memoria)
            float texelWidth = 1f / sprite.getTexture().getWidth();
            float texelHeight = 1f / sprite.getTexture().getHeight();
            com.tikisadventure.core.Assets.outlineShader.setUniformf("u_texelSize", texelWidth, texelHeight);
            com.tikisadventure.core.Assets.outlineShader.setUniformf("u_outlineWidth", 1.0f); // Grosor de 1 pixel

            // Color del contorno dependiendo del Tier
            switch (this.tier) {
                case 2: // Verde (Común)
                    com.tikisadventure.core.Assets.outlineShader.setUniformf("u_outlineColor", 0.0f, 1.0f, 0.0f, 1.0f);
                    break;
                case 3: // Azul (Raro)
                    com.tikisadventure.core.Assets.outlineShader.setUniformf("u_outlineColor", 0.0f, 0.5f, 1.0f, 1.0f);
                    break;
                case 4: // Morado (Épico)
                    com.tikisadventure.core.Assets.outlineShader.setUniformf("u_outlineColor", 0.6f, 0.0f, 0.8f, 1.0f);
                    break;
                case 5: // Dorado (Legendario)
                    com.tikisadventure.core.Assets.outlineShader.setUniformf("u_outlineColor", 1.0f, 0.8f, 0.0f, 1.0f);
                    break;
                default: // Blanco por si acaso
                    com.tikisadventure.core.Assets.outlineShader.setUniformf("u_outlineColor", 1.0f, 1.0f, 1.0f, 1.0f);
                    break;
            }
        }
        // --- FIN LOGICA DE SHADERS ---

        // Dibujamos el sprite normalmente
        batch.draw(sprite,
            (worldPosition.x + recoilOffset.x + swingOffset.x) - originX,
            (worldPosition.y + recoilOffset.y + swingOffset.y) - originY,
            originX, originY, width, height, 1f, scaleY,
            finalDrawingAngle);

        // --- LIMPIEZA DE SHADER ---
        // Tenemos que devolver el batch a la normalidad para que no dibuje el resto del juego con el shader
        if (useOutline && com.tikisadventure.core.Assets.outlineShader != null) {
            batch.flush(); // OBLIGATORIO antes de quitarlo
            batch.setShader(null);
        }
    }

    public void setPosition(float x, float y) { worldPosition.set(x, y); }
    public float getVisualAngle() { return visualAngle; }

    private void fireSingleProjectile(Vector2 spawnPos, Vector2 dir) {
        Projectile p = projectileCreator.create(
            spawnPos, dir, bulletSpeed, getFinalDamage(), bulletSize,
            projectileTexture, effectManager, trailType, trailInterval,
            projectileLifetime, critChance, critDamageMult, impactKnockback,
            this.owner
        );
        p.setDamageType(this.damageType);
        p.setPenetration(this.penetration);
        p.setGrowthRate(this.growthRate);
        p.setMaxRadius(this.maxRadius);
        p.setRotationSpeed(this.rotationSpeed);

        for (ProjectileModifier modifier : modifiers) {
            modifier.apply(p, effectManager);
        }

        if (owner instanceof Player) {
            ((Player) owner).addProjectile(p);
        }
    }

    public com.tikisadventure.entities.base.Entity getOwner() {
        return this.owner;
    }
}
