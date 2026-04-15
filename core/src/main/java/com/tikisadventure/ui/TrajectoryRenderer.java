package com.tikisadventure.ui;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Json;
import com.tikisadventure.core.Assets;

public class TrajectoryRenderer {
    private static class TrajectoryConfig {
        public float dotSize;
        public float gapSize;
        public String dotRegionName;
        public String targetRegionName;
        public float targetScale;
    }

    private TrajectoryConfig config;
    private TextureRegion dotRegion;
    private TextureRegion circleRegion;

    private final Vector2 tempDir = new Vector2();
    private final Vector2 tempPos = new Vector2();
    private final Json json = new Json();

    public TrajectoryRenderer() {
        this.config = json.fromJson(TrajectoryConfig.class, Gdx.files.internal("data/trajectory.json"));
        this.dotRegion = Assets.getRegion("shared", config.dotRegionName);
        this.circleRegion = Assets.getRegion("shared", config.targetRegionName);
    }

    public void render(SpriteBatch batch, Vector2 start, Vector2 end) {
        tempDir.set(end).sub(start);
        float length = tempDir.len();
        tempDir.nor();
        float angleDeg = (float) Math.toDegrees(Math.atan2(tempDir.y, tempDir.x));

        batch.setColor(1f, 1f, 1f, 1f);

        float pos = config.dotSize / 2f;
        while (pos < length) {
            tempPos.set(start).add(tempDir.cpy().scl(pos));

            batch.draw(dotRegion,
                tempPos.x - config.dotSize / 2f, tempPos.y - config.dotSize / 2f,
                config.dotSize / 2f, config.dotSize / 2f,
                config.dotSize, config.dotSize,
                1f, 1f, angleDeg);

            pos += config.dotSize + config.gapSize;
        }

        batch.draw(circleRegion, end.x - config.targetScale / 2f, end.y - config.targetScale / 2f, config.targetScale, config.targetScale);
    }

    public void dispose() {
        config = null;
        dotRegion = null;
        circleRegion = null;
    }
}
