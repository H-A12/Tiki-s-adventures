package com.tikisadventure.combat.weapons.behaviors;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.tikisadventure.combat.projectiles.Projectile;
import com.tikisadventure.combat.weapons.ProjectileCreator;
import com.tikisadventure.combat.weapons.Weapon;
import com.tikisadventure.effects.EffectManager;
import com.tikisadventure.effects.EffectType;
import com.tikisadventure.entities.base.Entity;
import com.tikisadventure.entities.player.Player;
import com.tikisadventure.systems.events.EventBus;
import com.tikisadventure.systems.events.FiredEvent;
import com.tikisadventure.effects.EffectType;
import com.tikisadventure.combat.weapons.ConfigurableWeapon;

import java.util.ArrayList;
import java.util.List;

public class ProjectilePatternBehavior implements AttackBehavior {

    private ProjectileCreator factory;
    private TextureRegion projectileTexture;
    private float speed;
    private float damage;
    private float size;
    private List<ProjectileModifier> modifiers = new ArrayList<>();
    
    private Weapon weapon;
    private float recoilForce = 0.0f;
    private float recoilRecovery = 0.0f;
    
    // Configuración de patrón
    private int count = 1;
    private float spread = 0f;
    private int burstCount = 1;
    private float burstInterval = 0f;
    private List<Emitter> emitters = new ArrayList<>();
    private EffectType trailType = null;
    private float trailInterval = 0f;
    private Vector2 spawnOffset = new Vector2(0, 0);
    private Vector2 muzzleFlashOffset = new Vector2(0, 0);

    // Estado interno
    private int bulletsShotInCurrentBurst = 0;
    private float burstTimer = 0;
    private boolean isBursting = false;
    private Vector2 burstDirection = new Vector2();
    private Entity owner;
    private Entity target;
    private Vector2 worldPosition;
    private EffectManager em;

    public ProjectilePatternBehavior(ProjectileCreator factory, TextureRegion texture, float speed, 
                                     float damage, float size, int count, float spread, 
                                     int burstCount, float burstInterval,
                                     List<Emitter> emitters, EffectType trailType, float trailInterval) {
        this.factory = factory;
        this.projectileTexture = texture;
        this.speed = speed;
        this.damage = damage;
        this.size = size;
        this.count = count;
        this.spread = spread;
        this.burstCount = burstCount;
        this.burstInterval = burstInterval;
        this.emitters = emitters;
        this.trailType = trailType;
        this.trailInterval = trailInterval;
    }

    public void addModifier(ProjectileModifier modifier) {
        modifiers.add(modifier);
    }

    @Override
    public void setWeapon(Weapon weapon) {
        this.weapon = weapon;
    }

    public void setRecoil(float force, float recovery) {
        this.recoilForce = force;
        this.recoilRecovery = recovery;
    }
    
    public void setSpawnOffset(Vector2 spawnOffset) {
        this.spawnOffset.set(spawnOffset);
    }
    
    public void setMuzzleFlashOffset(Vector2 muzzleFlashOffset) {
        this.muzzleFlashOffset.set(muzzleFlashOffset);
    }

    @Override
    public void execute(Entity owner, Entity target, Vector2 worldPosition, EffectManager em) {
        this.owner = owner;
        this.target = target;
        this.worldPosition = worldPosition;
        this.em = em;

        if (burstCount > 1) {
            if (!isBursting && target != null) {
                isBursting = true;
                bulletsShotInCurrentBurst = 0;
                burstTimer = burstInterval;
                burstDirection.set(target.getPosicion()).sub(worldPosition).nor();
            }
        } else {
            fireShot(target != null ? new Vector2(target.getPosicion()).sub(worldPosition).nor() : new Vector2(1,0));
        }
    }

    @Override
    public void update(float delta) {
        if (isBursting) {
            burstTimer += delta;
            if (burstTimer >= burstInterval) {
                fireShot(target != null ? new Vector2(target.getPosicion()).sub(worldPosition).nor() : burstDirection);
                burstTimer = 0;
                bulletsShotInCurrentBurst++;
                if (bulletsShotInCurrentBurst >= burstCount) {
                    isBursting = false;
                }
            }
        }
    }

    private void fireShot(Vector2 baseDir) {
        if (weapon != null && recoilForce > 0) {
            weapon.applyRecoil(recoilForce, recoilRecovery);
        }
        
        float baseAngle = baseDir.angleDeg();
        float weaponAngle = weapon != null ? weapon.getVisualAngle() : baseAngle;
        
        EffectType muzzleFlashType = null;
        Vector2 muzzleFlashPos = new Vector2(worldPosition);
        if (weapon instanceof ConfigurableWeapon) {
            muzzleFlashType = ((ConfigurableWeapon) weapon).getMuzzleFlashType();
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

        for (int i = 0; i < count; i++) {
            float angle = baseAngle;
            if (count > 1) {
                angle += MathUtils.random(-spread / 2f, spread / 2f);
            }
            Vector2 dir = new Vector2(1, 0).setAngleDeg(angle);

            Projectile p = factory.create(
                new Vector2(worldPosition).add(rotatedSpawnOffset),
                dir, speed, damage, size,
                projectileTexture, em, trailType, trailInterval
            );
            
            for (ProjectileModifier modifier : modifiers) {
                modifier.apply(p, em);
            }

            if (owner instanceof Player) {
                ((Player) owner).addProjectile(p);
            }
        }
    }
}
