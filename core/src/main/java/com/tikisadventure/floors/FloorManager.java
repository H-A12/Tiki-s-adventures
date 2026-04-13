package com.tikisadventure.floors;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.tikisadventure.core.Assets;

import java.util.HashSet;
import java.util.Set;

public class FloorManager {

    private int currentFloor;
    private int totalFloors;
    private Door door;
    private FloorTransition transition;

    private TiledMap currentMap;
    private TiledMapTileLayer collisionLayer;
    private TiledMapTileLayer backgroundLayer;
    private SpriteBatch tileBatch;
    private Texture tilesetTexture;
    private Texture collisionTilesetTexture;
    private int tilesetColumns = 3;
    private int tileWidth = 16;
    private int tileHeight = 16;

    private float tilesPerFloor;
    private float transitionDuration;
    private float doorActivationRadius;

    private JsonValue floorConfig;

    private static final String[] MAP_FILES = {
        "maps/map_casttle1.tmx",
        "maps/map_casttle2.tmx",
        "maps/map_casttle3.tmx",
        "maps/map_casttle4.tmx",
        "maps/map_casttle5.tmx",
        "maps/mapa_100x100.tmx"
    };

    private Set<Integer> usedMapIndices;
    private Array<String> availableMaps;

    public FloorManager(boolean enableParticles) {
        this.currentFloor = 1;
        this.transition = new FloorTransition(2.0f, enableParticles);
        this.usedMapIndices = new HashSet<>();
        this.availableMaps = new Array<>();
        this.tileBatch = new SpriteBatch();

        loadConfig();
        generateFloor();
    }

    private void loadConfig() {
        JsonReader reader = new JsonReader();
        try {
            JsonValue root = reader.parse(Gdx.files.internal("data/floor_config.json"));
            totalFloors = root.getInt("total_floors", 5);
            tilesPerFloor = root.getInt("tiles_per_floor", 32);
            transitionDuration = root.getFloat("transition_duration", 2.0f);
            doorActivationRadius = root.getFloat("door_activation_radius", 2.0f);
            floorConfig = root.get("floors");
        } catch (Exception e) {
            Gdx.app.error("FloorManager", "Error loading data/floor_config.json, using defaults");
            totalFloors = 5;
            tilesPerFloor = 32;
            transitionDuration = 2.0f;
            doorActivationRadius = 2.0f;
        }
    }

    public void generateFloor() {
        String mapFile = selectRandomMap();
        loadMap(mapFile);

        int[] doorPos = findValidSpawnPosition(8, 12, 8, 12);
        door = new Door(doorPos[0], doorPos[1]);
        door.hide();

        Gdx.app.log("FLOOR", "Generated floor " + currentFloor + " with map: " + mapFile + ", door at (" + doorPos[0] + ", " + doorPos[1] + ")");
    }

    public int[] findValidSpawnPosition(int minX, int maxX, int minY, int maxY) {
        int mapHeight = collisionLayer != null ? collisionLayer.getHeight() : 20;

        for (int attempts = 0; attempts < 100; attempts++) {
            int x = minX + (int)(Math.random() * (maxX - minX + 1));
            int y = minY + (int)(Math.random() * (maxY - minY + 1));

            boolean valid = true;
            for (float dx = -1; dx <= 1; dx++) {
                for (float dy = -1; dy <= 1; dy++) {
                    if (isWall(x + dx, y + dy)) {
                        valid = false;
                        break;
                    }
                }
                if (!valid) break;
            }

            if (valid) {
                return new int[]{x, y};
            }
        }

        return new int[]{10, 10};
    }

    private String selectRandomMap() {
        availableMaps.clear();

        for (int i = 0; i < MAP_FILES.length; i++) {
            if (!usedMapIndices.contains(i)) {
                availableMaps.add(MAP_FILES[i]);
            }
        }

        if (availableMaps.size == 0) {
            usedMapIndices.clear();
            for (int i = 0; i < MAP_FILES.length; i++) {
                availableMaps.add(MAP_FILES[i]);
            }
            Gdx.app.log("FLOOR", "All maps used, resetting pool");
        }

        int randomIndex = (int)(Math.random() * availableMaps.size);
        String selectedMap = availableMaps.get(randomIndex);

        for (int i = 0; i < MAP_FILES.length; i++) {
            if (MAP_FILES[i].equals(selectedMap)) {
                usedMapIndices.add(i);
                break;
            }
        }

        return selectedMap;
    }

    private void loadMap(String mapFile) {
        if (currentMap != null) {
            currentMap.dispose();
        }

        try {
            currentMap = new TmxMapLoader().load(mapFile);
            backgroundLayer = (TiledMapTileLayer) currentMap.getLayers().get("Tile Layer 1");
            collisionLayer = (TiledMapTileLayer) currentMap.getLayers().get("collisions");

            if (backgroundLayer != null) {
                tilesetTexture = new Texture("background.png");
            }

            try {
                collisionTilesetTexture = new Texture("empty.png");
            } catch (Exception e) {
                Gdx.app.log("FLOOR", "Could not load empty.png");
                collisionTilesetTexture = tilesetTexture;
            }

            if (collisionLayer == null) {
                Gdx.app.error("FLOOR", "Collision layer not found in map: " + mapFile);
            }
        } catch (Exception e) {
            Gdx.app.error("FLOOR", "Error loading map: " + mapFile, e);
            currentMap = null;
            collisionLayer = null;
            backgroundLayer = null;
        }
    }

    public void update(float delta) {
        door.update(delta);
        transition.update(delta);
    }

    public void renderMap(OrthographicCamera camera) {
        if (backgroundLayer == null || tilesetTexture == null) return;

        tileBatch.setProjectionMatrix(camera.combined);
        tileBatch.begin();

        int mapHeight = backgroundLayer.getHeight();

        for (int y = 0; y < mapHeight; y++) {
            for (int x = 0; x < backgroundLayer.getWidth(); x++) {
                TiledMapTileLayer.Cell cell = backgroundLayer.getCell(x, y);
                if (cell != null && cell.getTile() != null) {
                    int tileId = cell.getTile().getId();
                    int tileIndex = tileId - 1;
                    int srcCol = tileIndex % tilesetColumns;
                    int srcRow = tileIndex / tilesetColumns;
                    int srcX = srcCol * tileWidth;
                    int srcY = srcRow * tileHeight;

                    TextureRegion region = new TextureRegion(
                        tilesetTexture, srcX, srcY, tileWidth, tileHeight
                    );

                    float worldX = x;
                    float worldY = mapHeight - 1 - y;

                    tileBatch.draw(region, worldX, worldY, 1, 1);
                }
            }
        }

        if (collisionLayer != null && collisionTilesetTexture != null) {
            for (int y = 0; y < mapHeight; y++) {
                for (int x = 0; x < collisionLayer.getWidth(); x++) {
                    TiledMapTileLayer.Cell cell = collisionLayer.getCell(x, y);
                    if (cell != null && cell.getTile() != null) {
                        TextureRegion region = new TextureRegion(collisionTilesetTexture);
                        float worldX = x;
                        float worldY = mapHeight - 1 - y;
                        tileBatch.draw(region, worldX, worldY, 1, 1);
                    }
                }
            }
        }

        tileBatch.end();
    }

    public void renderEntities(Batch batch) {
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
        generateFloor();
        transition.reset();
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
        if (collisionLayer == null) return false;

        int tileX = (int)Math.floor(worldX);
        int tileY = collisionLayer.getHeight() - 1 - (int)Math.floor(worldY);

        if (tileX < 0 || tileX >= collisionLayer.getWidth() ||
            tileY < 0 || tileY >= collisionLayer.getHeight()) {
            return true;
        }

        TiledMapTileLayer.Cell cell = collisionLayer.getCell(tileX, tileY);
        return cell != null && cell.getTile() != null;
    }

    public void resetTransitionOffset() {
        transition.reset();
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

    public TiledMapTileLayer getCollisionLayer() {
        return collisionLayer;
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
        if (currentMap != null) currentMap.dispose();
        // Texture disposed by Assets.dispose()
        if (tileBatch != null) tileBatch.dispose();
        if (door != null) door.dispose();
        transition.dispose();
    }
}
