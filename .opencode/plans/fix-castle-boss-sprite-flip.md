# Fix Castle Boss sprite visual shift on flip

## Root Cause

Castle boss sprites are 140×93, cropped to 93×93. The character **is not centered** within the cropped frame. The current code uses `spriteOffsetX = 0.25` to shift the bounding box, but since this offset is constant regardless of flip direction, the character's visual center jumps when the sprite mirrors horizontally.

With `ANCHO = 10.5` and `spriteOffsetX = 0.25`:
- Center fraction = `0.5 - spriteOffsetX/ANCHO = 0.5 - 0.025/10.5 = 0.476`
- When flipped, character visual shifts by `abs(0.476 - 0.524) * 10.5 = 0.5` tiles

## Fix

Replace `spriteOffsetX` with `spriteCenterX` (0-1 fraction of where the character's visual center is in the frame). Compute x position so the visual center stays at `getPosition().x` regardless of flip direction.

## Changes

### 1. `ConfigurableEnemy.java` line 54
**Old:** `private float spriteOffsetX = 0;`
**New:** `private float spriteCenterX = 0.5f;`

### 2. `ConfigurableEnemy.java` lines 306-307 (castle boss constructor block)
**Old:**
```java
            spriteOffsetX = config.getFloat("sprite_offset_x", 0);
            float frameDur = 0.083f;
```
**New:**
```java
            float offsetX = config.getFloat("sprite_offset_x", 0);
            spriteCenterX = 0.5f - offsetX / getANCHO();
            float frameDur = 0.083f;
```

### 3. `ConfigurableEnemy.java` line 576 (castle boss draw x calculation)
**Old:** `float x = getPosition().x - getANCHO() / 2 + spriteOffsetX;`
**New:** `float x = getPosition().x - spriteCenterX * getANCHO();`

## Verification

- Character visual center = `x + spriteCenterX * ANCHO` (unflipped) = `(x + ANCHO) + (1 - spriteCenterX) * (-ANCHO)` (flipped) = `getPosition().x`
- Bounding box: from `getPosition().x - spriteCenterX * ANCHO` to `getPosition().x + (1 - spriteCenterX) * ANCHO`
- No visual shift when flipping since the character center is pinned to the entity position
