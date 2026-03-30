package com.tikisadventure.components;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.tikisadventure.entities.base.Entity;

public class HomingComponent implements Component {

    private float returnDelay;
    private float rotationSpeed;
    private boolean isReturning = false;
    private Vector2 tempDir = new Vector2();

    public HomingComponent(float returnDelay, float rotationSpeed) {
        this.returnDelay = returnDelay;
        this.rotationSpeed = rotationSpeed;
    }

    @Override
    public void tick(Object owner, float delta, Array<Entity> entities) {
        if (!(owner instanceof Timed) || !(owner instanceof HasDirection) ||
            !(owner instanceof HasOwner) || !(owner instanceof HasPosition) ||
            !(owner instanceof Killable)) {
            return;
        }

        Timed timed = (Timed) owner;
        HasDirection dirInterface = (HasDirection) owner;
        HasOwner ownerInterface = (HasOwner) owner;
        HasPosition posInterface = (HasPosition) owner;
        Killable killable = (Killable) owner;

        if (!killable.isAlive()) return;

        if (timed.getStateTime() >= returnDelay) {
            isReturning = true;
        }

        if (isReturning && ownerInterface.getOwner() != null) {
            Entity actualOwner = (Entity) ownerInterface.getOwner();
            
            tempDir.set(actualOwner.getPosicion())
                .sub(posInterface.getPosition())
                .nor();

            dirInterface.getDirection().lerp(tempDir, rotationSpeed * delta).nor();

            if (posInterface.getPosition().dst2(actualOwner.getPosicion()) < 0.5f) {
                killable.die();
            }
        }
    }

    @Override
    public void onAttach(Object owner) {
        isReturning = false;
    }
}
