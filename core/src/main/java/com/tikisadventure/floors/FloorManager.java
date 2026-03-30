package com.tikisadventure.floors;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;

public class FloorManager {

    private int currentFloor;
    private int totalFloors;
    private FloorGenerator floorGenerator;
    private Door door;
    private FloorTransition transition;
    private int[][] currentLayout;
    private FloorGenerator.RoomShape currentShape;
    
    private Texture floorTexture;
    private Texture wallTexture;
    
    private float tilesPerFloor;
    private float transitionDuration;
    private float doorActivationRadius;
    
    private JsonValue floorConfig;

    public FloorManager(boolean enableParticles) {
        this.currentFloor = 1;
        this.floorGenerator = new FloorGenerator();
        this.transition = new FloorTransition(2.0f, enableParticles);
        
        loadConfig();
        
        try {
            floorTexture = new Texture("empty.png");
            wallTexture = new Texture("collision.png");
        } catch (Exception e) {
            floorTexture = null;
            wallTexture = null;
        }
        
        generateFloor();
    }

    private void loadConfig() {
        JsonReader reader = new JsonReader();
        try {
            JsonValue root = reader.parse(Gdx.files.internal("floor_config.json"));
            totalFloors = root.getInt("total_floors", 5);
            tilesPerFloor = root.getInt("tiles_per_floor", 32);
            transitionDuration = root.getFloat("transition_duration", 2.0f);
            doorActivationRadius = root.getFloat("door_activation_radius", 2.0f);
            floorConfig = root.get("floors");
        } catch (Exception e) {
            Gdx.app.error("FloorManager", "Error loading floor_config.json, using defaults");
            totalFloors = 5;
            tilesPerFloor = 32;
            transitionDuration = 2.0f;
            doorActivationRadius = 2.0f;
        }
    }

    public void generateFloor() {
        currentShape = floorGenerator.getRandomShape();
        currentLayout = floorGenerator.generateLayout(currentShape);
        
        int[] doorPos = floorGenerator.getDoorPosition();
        door = new Door(doorPos[0], doorPos[1]);
        door.hide();
    }

    public void update(float delta) {
        door.update(delta);
        transition.update(delta);
    }

    public void render(Batch batch) {
        if (currentLayout == null) return;
        
        int size = floorGenerator.getFloorSize();
        float offsetY = transition.getCurrentOffset();
        
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                float worldX = x;
                float worldY = size - 1 - y - offsetY;
                
                if (worldY < 0 || worldY >= size) continue;
                
                float drawX = x;
                float drawY = worldY;
                
                Texture tex = (currentLayout[y][x] == 1) ? wallTexture : floorTexture;
                
                if (tex != null) {
                    batch.draw(tex, drawX, drawY, 1, 1);
                }
            }
        }
        
        door.render(batch);
        transition.render(batch);
    }

    public void showDoor() {
        door.show();
    }

    public void hideDoor() {
        door.hide();
    }

    public void startTransition() {
        float floorHeight = tilesPerFloor;
        float startY = (currentFloor - 1) * floorHeight;
        float endY = currentFloor * floorHeight;
        
        door.open();
        transition.startTransition(door.getPosition(), startY, endY);
    }

    public boolean isTransitionComplete() {
        return transition.isComplete();
    }

    public boolean isTransitionActive() {
        return transition.isActive();
    }

    public void completeTransition() {
        currentFloor++;
        transition.reset();
        generateFloor();
    }

    public boolean isPlayerNearDoor(Vector2 playerPos) {
        return door.isPlayerNear(playerPos);
    }

    public boolean canUseDoor() {
        return door.canInteract() && !isTransitionActive();
    }

    public void useDoor() {
        startTransition();
    }

    public boolean isWall(float worldX, float worldY) {
        int tileX = (int)(worldX);
        int tileY = (int)(worldY + transition.getCurrentOffset());
        return floorGenerator.isWallTile(tileX, tileY);
    }

    public int getCurrentFloor() {
        return currentFloor;
    }

    public int getTotalFloors() {
        return totalFloors;
    }

    public float getCameraOffset() {
        return transition.getCurrentOffset();
    }

    public Door getDoor() {
        return door;
    }

    public FloorGenerator getFloorGenerator() {
        return floorGenerator;
    }

    public JsonValue getFloorConfig(int floor) {
        if (floorConfig == null) return null;
        return floorConfig.get(String.valueOf(floor));
    }

    public float getEnemyMultiplier() {
        if (floorConfig == null) return 1.0f;
        JsonValue floorData = floorConfig.get(String.valueOf(currentFloor));
        if (floorData == null) return 1.0f;
        return floorData.getFloat("enemy_multiplier", 1.0f);
    }

    public String[] getAvailableEnemies() {
        if (floorConfig == null) return new String[]{"slime"};
        JsonValue floorData = floorConfig.get(String.valueOf(currentFloor));
        if (floorData == null) return new String[]{"slime"};
        
        JsonValue enemies = floorData.get("available_enemies");
        if (enemies == null) return new String[]{"slime"};
        
        String[] result = new String[enemies.size];
        for (int i = 0; i < enemies.size; i++) {
            result[i] = enemies.getString(i);
        }
        return result;
    }

    public int getBaseEnemyCount() {
        if (floorConfig == null) return 3;
        JsonValue floorData = floorConfig.get(String.valueOf(currentFloor));
        if (floorData == null) return 3;
        return floorData.getInt("base_enemies", 3);
    }

    public boolean isGameComplete() {
        return currentFloor > totalFloors;
    }

    public void dispose() {
        if (floorTexture != null) floorTexture.dispose();
        if (wallTexture != null) wallTexture.dispose();
        if (door != null) door.dispose();
        transition.dispose();
    }
}
