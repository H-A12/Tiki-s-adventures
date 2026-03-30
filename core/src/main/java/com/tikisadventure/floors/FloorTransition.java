package com.tikisadventure.floors;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

public class FloorTransition {

    public enum TransitionState {
        IDLE,
        PLAYER_ENTERING,
        CAMERA_MOVING,
        COMPLETE
    }

    private TransitionState state;
    private float duration;
    private float elapsed;
    private float startY;
    private float endY;
    private float currentOffset;
    
    private Vector2 doorPosition;
    
    private Array<TransitionParticle> particles;
    private boolean particlesEnabled;
    private Texture particleTexture;
    
    private static final float PARTICLE_SPAWN_INTERVAL = 0.05f;
    private float particleTimer = 0;

    public FloorTransition(float duration, boolean enableParticles) {
        this.state = TransitionState.IDLE;
        this.duration = duration;
        this.elapsed = 0;
        this.currentOffset = 0;
        this.particlesEnabled = enableParticles;
        this.particles = new Array<>();
        
        if (particlesEnabled) {
            try {
                particleTexture = new Texture("particle.png");
            } catch (Exception e) {
                particlesEnabled = false;
            }
        }
    }

    public void startTransition(Vector2 doorPos, float startY, float endY) {
        this.doorPosition = doorPos;
        this.startY = startY;
        this.endY = endY;
        this.elapsed = 0;
        this.currentOffset = 0;
        this.state = TransitionState.PLAYER_ENTERING;
        this.particles.clear();
        this.particleTimer = 0;
    }

    public void update(float delta) {
        if (state == TransitionState.IDLE || state == TransitionState.COMPLETE) {
            return;
        }

        elapsed += delta;

        if (state == TransitionState.PLAYER_ENTERING) {
            if (elapsed >= 0.3f) {
                state = TransitionState.CAMERA_MOVING;
                elapsed = 0;
            }
        }
        
        if (state == TransitionState.CAMERA_MOVING) {
            float progress = Math.min(elapsed / duration, 1f);
            currentOffset = progress * (endY - startY);
            
            if (particlesEnabled) {
                updateParticles(delta);
            }
            
            if (progress >= 1f) {
                state = TransitionState.COMPLETE;
                currentOffset = endY - startY;
            }
        }
    }

    private void updateParticles(float delta) {
        particleTimer += delta;
        
        if (particleTimer >= PARTICLE_SPAWN_INTERVAL) {
            particleTimer = 0;
            spawnParticles();
        }
        
        for (int i = particles.size - 1; i >= 0; i--) {
            TransitionParticle p = particles.get(i);
            p.update(delta);
            if (!p.isAlive()) {
                particles.removeIndex(i);
            }
        }
    }

    private void spawnParticles() {
        float x = doorPosition.x + (float)(Math.random() - 0.5) * 2;
        float y = doorPosition.y - currentOffset + (float)(Math.random() - 0.5) * 1;
        
        Vector2 vel = new Vector2(
            (float)(Math.random() - 0.5) * 3,
            -2 - (float)(Math.random() * 2)
        );
        
        float lifetime = 0.5f + (float)(Math.random() * 0.5f);
        float size = 0.1f + (float)(Math.random() * 0.15f);
        
        particles.add(new TransitionParticle(x, y, vel, lifetime, size));
    }

    public void render(Batch batch) {
        if (particlesEnabled) {
            for (TransitionParticle p : particles) {
                p.render(batch, particleTexture);
            }
        }
    }

    public boolean isComplete() {
        return state == TransitionState.COMPLETE;
    }

    public boolean isActive() {
        return state != TransitionState.IDLE && state != TransitionState.COMPLETE;
    }

    public float getCurrentOffset() {
        return currentOffset;
    }

    public TransitionState getState() {
        return state;
    }

    public void reset() {
        state = TransitionState.IDLE;
        elapsed = 0;
        currentOffset = 0;
        particles.clear();
    }

    public void dispose() {
        if (particleTexture != null) {
            particleTexture.dispose();
        }
        particles.clear();
    }

    private static class TransitionParticle {
        Vector2 position;
        Vector2 velocity;
        float lifetime;
        float maxLifetime;
        float size;
        float rotation;
        float rotationSpeed;

        public TransitionParticle(float x, float y, Vector2 vel, float lifetime, float size) {
            this.position = new Vector2(x, y);
            this.velocity = vel;
            this.lifetime = lifetime;
            this.maxLifetime = lifetime;
            this.size = size;
            this.rotation = (float)(Math.random() * 360);
            this.rotationSpeed = (float)(Math.random() - 0.5) * 360;
        }

        public void update(float delta) {
            position.mulAdd(velocity, delta);
            velocity.scl(0.95f);
            lifetime -= delta;
            rotation += rotationSpeed * delta;
        }

        public boolean isAlive() {
            return lifetime > 0;
        }

        public void render(Batch batch, Texture texture) {
            if (texture == null || !isAlive()) return;
            
            float alpha = lifetime / maxLifetime;
            batch.setColor(1, 1, 0.5f, alpha);
            batch.draw(texture, position.x - size/2, position.y - size/2, 
                       size, size);
            batch.setColor(1, 1, 1, 1);
        }
    }
}
