package com.tikisadventure.systems;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.tikisadventure.core.Assets;
import com.badlogic.gdx.math.MathUtils;

public class TrajectorySystem {
    public void render(Batch batch, Vector2 start, Vector2 target) {
        TextureRegion dash = Assets.getRegion("shared", "pp_dash");
        TextureRegion circle = Assets.getRegion("shared", "pp_circle2");
        
        if (dash == null || circle == null) return;
        
        float dist = start.dst(target);
        float angle = MathUtils.atan2(target.y - start.y, target.x - start.x) * MathUtils.radiansToDegrees;
        
        // Draw segments of dash
        // pp_dash is 204x4. Let's make it a reasonable size, say 0.5 units wide.
        float dashWidth = 0.5f;
        float dashHeight = 0.1f;
        
        // We want to fill the distance with dashes.
        float spacing = 0.2f;
        
        for (float d = 0; d < dist; d += (dashWidth + spacing)) {
            float x = start.x + MathUtils.cosDeg(angle) * d;
            float y = start.y + MathUtils.sinDeg(angle) * d;
            
            // Draw dash, centered on (x, y)
            batch.draw(dash, x - dashWidth/2, y - dashHeight/2, dashWidth/2, dashHeight/2, dashWidth, dashHeight, 1, 1, angle);
        }
        
        // Draw circle at target
        float circleSize = 0.4f;
        batch.draw(circle, target.x - circleSize/2, target.y - circleSize/2, circleSize, circleSize);
    }
}
