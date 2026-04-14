package com.tikisadventure.ui;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
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

    public TrajectoryRenderer() {
        Json json = new Json();
        this.config = json.fromJson(TrajectoryConfig.class, Gdx.files.internal("data/trajectory.json"));
        this.dotRegion = Assets.getRegion("shared", config.dotRegionName);
        this.circleRegion = Assets.getRegion("shared", config.targetRegionName);
    }

    public void render(SpriteBatch batch, Vector2 start, Vector2 end) {
        Vector2 dir = end.cpy().sub(start);
        float length = dir.len();
        dir.nor();
        float angleDeg = (float) Math.toDegrees(Math.atan2(dir.y, dir.x));

        batch.setColor(1f, 1f, 1f, 1f);

        float pos = config.dotSize / 2f;
        while (pos < length) {
            Vector2 drawPos = start.cpy().add(dir.cpy().scl(pos));

            batch.draw(dotRegion,
                drawPos.x - config.dotSize / 2f, drawPos.y - config.dotSize / 2f,
                config.dotSize / 2f, config.dotSize / 2f,
                config.dotSize, config.dotSize,
                1f, 1f, angleDeg);

            pos += config.dotSize + config.gapSize;
        }

        // Draw target circle
        batch.draw(circleRegion, end.x - config.targetScale / 2f, end.y - config.targetScale / 2f, config.targetScale, config.targetScale);
    }
}
