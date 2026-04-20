package com.tikisadventure.floors;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileSet;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.tikisadventure.core.Assets;
import com.tikisadventure.core.GameSession;

import java.util.HashSet;
import java.util.Set;

public class FloorManager {

    private int currentFloor;
    private int totalFloors;
    private FloorTransition transition;

    private TiledMap currentMap;
    private TiledMapTileLayer collisionLayer;
    private TiledMapTileLayer backgroundLayer;
    private TiledMapTileLayer transparentLayer;
    private TiledMapTileLayer doorOpenLayer;
    private TiledMapTileLayer miniObjectsLayer;
    private boolean doorOpen = false;
    private SpriteBatch tileBatch;
    private Texture tilesetTexture;
    private int tilesetColumns = 3;
    private int tileWidth = 16;
    private int tileHeight = 16;

    private float tilesPerFloor;
    private float transitionDuration;
    private float doorActivationRadius = 2.0f;

    private JsonValue floorConfig;

    private String currentMapFolder;
    private Set<Integer> usedMapIndices;
    private Array<String> availableMaps;

    public FloorManager(boolean enableParticles) {
        this.currentFloor = 1;
        this.transition = new FloorTransition(2.0f, enableParticles);
        this.usedMapIndices = new HashSet<>();
        this.availableMaps = new Array<>();
        this.usedMapIndices = new HashSet<>();
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
        doorOpen = false;

        Gdx.app.log("FLOOR", "Generated floor " + currentFloor + " with map: " + mapFile);
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

    private void loadAvailableMaps() {
        String mapName = GameSession.selectedMapName;
        currentMapFolder = "maps/" + mapName + "/";

        availableMaps.clear();

        FileHandle mapDir = Gdx.files.internal(currentMapFolder);
        if (mapDir.isDirectory()) {
            for (FileHandle file : mapDir.list()) {
                if (file.name().endsWith(".tmx")) {
                    availableMaps.add(currentMapFolder + file.name());
                }
            }
        }

        if (availableMaps.size == 0) {
            availableMaps.add("maps/bosque/map_1.tmx");
            currentMapFolder = "maps/bosque/";
            Gdx.app.error("FLOOR", "No maps found for " + mapName + ", using default");
        }

        Gdx.app.log("FLOOR", "Loaded " + availableMaps.size + " maps from " + currentMapFolder);
    }

    private String selectRandomMap() {
        loadAvailableMaps();

        if (availableMaps.size == 0) {
            loadAvailableMaps();
        }

        int randomIndex = (int)(Math.random() * availableMaps.size);
        String selectedMap = availableMaps.get(randomIndex);

        return selectedMap;
    }

    private void loadMap(String mapFile) {
        if (currentMap != null) {
            currentMap.dispose();
        }

        try {
            currentMap = new TmxMapLoader().load(mapFile);
            backgroundLayer = (TiledMapTileLayer) currentMap.getLayers().get("Ground");
            collisionLayer = (TiledMapTileLayer) currentMap.getLayers().get("Objects");
            transparentLayer = (TiledMapTileLayer) currentMap.getLayers().get("Transparent");
            doorOpenLayer = (TiledMapTileLayer) currentMap.getLayers().get("Door_open");
            miniObjectsLayer = (TiledMapTileLayer) currentMap.getLayers().get("Mini_objects");

            if (backgroundLayer != null) {
                tilesetTexture = loadTilesetTexture(mapFile);
            }

            if (collisionLayer == null) {
                Gdx.app.error("FLOOR", "Collision layer not found in map: " + mapFile);
            }
        } catch (Exception e) {
            Gdx.app.error("FLOOR", "Error loading map: " + mapFile, e);
            currentMap = null;
            collisionLayer = null;
            backgroundLayer = null;
            transparentLayer = null;
            doorOpenLayer = null;
        }
    }

    private Texture loadTilesetTexture(String mapFile) {
        try {
            FileHandle mapFileHandle = Gdx.files.internal(mapFile);
            String mapContent = mapFileHandle.readString();

            String mapDir = mapFile.substring(0, mapFile.lastIndexOf('/') + 1);

            int tilesetStart = mapContent.indexOf("<tileset");
            if (tilesetStart != -1) {
                int tilesetEnd = mapContent.indexOf(">", tilesetStart);
                String tilesetTag = mapContent.substring(tilesetStart, tilesetEnd);

                int columnsStart = tilesetTag.indexOf("columns=\"");
                if (columnsStart != -1) {
                    columnsStart += 9;
                    int columnsEnd = tilesetTag.indexOf("\"", columnsStart);
                    String columnsStr = tilesetTag.substring(columnsStart, columnsEnd);
                    tilesetColumns = Integer.parseInt(columnsStr);
                }

                int sourceStart = tilesetTag.indexOf("source=\"");
                String tsxName = null;
                if (sourceStart != -1) {
                    sourceStart += 8;
                    int sourceEnd = tilesetTag.indexOf("\"", sourceStart);
                    tsxName = tilesetTag.substring(sourceStart, sourceEnd);
                }

                if (tsxName != null) {
                    String tsxPath = mapDir + tsxName;

                    FileHandle tsxFile = Gdx.files.internal(tsxPath);
                    if (tsxFile.exists()) {
                        String tsxContent = tsxFile.readString();

                        if (columnsStart == -1) {
                            int tsxColumnsStart = tsxContent.indexOf("columns=\"");
                            if (tsxColumnsStart != -1) {
                                tsxColumnsStart += 9;
                                int tsxColumnsEnd = tsxContent.indexOf("\"", tsxColumnsStart);
                                String columnsStr = tsxContent.substring(tsxColumnsStart, tsxColumnsEnd);
                                tilesetColumns = Integer.parseInt(columnsStr);
                            }
                        }

                        int imageStart = tsxContent.indexOf("source=\"") + 8;
                        int imageEnd = tsxContent.indexOf("\"", imageStart);
                        String imageName = tsxContent.substring(imageStart, imageEnd);

                        FileHandle imageFile = tsxFile.parent().child(imageName);

                        return new Texture(imageFile);
                    }
                }
            }
        } catch (Exception e) {
            Gdx.app.error("FLOOR", "Could not auto-detect tileset, using default", e);
        }

        tilesetColumns = 30;
        return new Texture(currentMapFolder + "forest_sprites.png");
    }

    public void update(float delta) {
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
                    renderTile(cell, x, y);
                }
            }
        }

        if (collisionLayer != null) {
            for (int y = 0; y < collisionLayer.getHeight(); y++) {
                for (int x = 0; x < collisionLayer.getWidth(); x++) {
                    TiledMapTileLayer.Cell cell = collisionLayer.getCell(x, y);
                    if (cell != null && cell.getTile() != null) {
                        renderTile(cell, x, y);
                    }
                }
            }
        }

        if (miniObjectsLayer != null) {
            for (int y = 0; y < miniObjectsLayer.getHeight(); y++) {
                for (int x = 0; x < miniObjectsLayer.getWidth(); x++) {
                    TiledMapTileLayer.Cell cell = miniObjectsLayer.getCell(x, y);
                    if (cell != null && cell.getTile() != null) {
                        renderTile(cell, x, y);
                    }
                }
            }
        }

        if (!doorOpen) {
            TiledMapTileLayer doorClosedLayer = (TiledMapTileLayer) currentMap.getLayers().get("Door_closed");
            if (doorClosedLayer != null) {
                for (int y = 0; y < doorClosedLayer.getHeight(); y++) {
                    for (int x = 0; x < doorClosedLayer.getWidth(); x++) {
                        TiledMapTileLayer.Cell cell = doorClosedLayer.getCell(x, y);
                        if (cell != null && cell.getTile() != null) {
                            renderTile(cell, x, y);
                        }
                    }
                }
            }
        } else if (doorOpenLayer != null) {
            for (int y = 0; y < doorOpenLayer.getHeight(); y++) {
                for (int x = 0; x < doorOpenLayer.getWidth(); x++) {
                    TiledMapTileLayer.Cell cell = doorOpenLayer.getCell(x, y);
                    if (cell != null && cell.getTile() != null) {
                        renderTile(cell, x, y);
                    }
                }
            }
        }

        tileBatch.end();
    }

    private void renderTile(TiledMapTileLayer.Cell cell, int x, int y) {
        int tileId = cell.getTile().getId();
        int tileIndex = tileId - 1;
        int srcCol = tileIndex % tilesetColumns;
        int srcRow = tileIndex / tilesetColumns;
        int srcX = srcCol * tileWidth;
        int srcY = srcRow * tileHeight;

        TextureRegion region = new TextureRegion(
            tilesetTexture, srcX, srcY, tileWidth, tileHeight
        );

        tileBatch.draw(region, x, y, 1, 1);
    }

    public void renderEntities(Batch batch) {
        transition.render(batch);
    }

    public void renderTransparentLayer(OrthographicCamera camera) {
        if (transparentLayer == null || tilesetTexture == null) return;

        tileBatch.setProjectionMatrix(camera.combined);
        tileBatch.begin();

        int mapHeight = transparentLayer.getHeight();

        for (int y = 0; y < mapHeight; y++) {
            for (int x = 0; x < transparentLayer.getWidth(); x++) {
                TiledMapTileLayer.Cell cell = transparentLayer.getCell(x, y);
                if (cell != null && cell.getTile() != null) {
                    renderTile(cell, x, y);
                }
            }
        }

        tileBatch.end();
    }

    public void showDoorOpen() {
        doorOpen = true;
    }

    public void hideDoorOpen() {
        doorOpen = false;
    }

    public void startTransition() {
        float floorHeight = tilesPerFloor;
        float startY = (currentFloor - 1) * floorHeight;
        float endY = currentFloor * floorHeight;

        Vector2 doorPos = findDoorPosition();
        transition.startTransition(doorPos, startY, endY);
    }

    private Vector2 findDoorPosition() {
        if (doorOpenLayer != null) {
            for (int y = 0; y < doorOpenLayer.getHeight(); y++) {
                for (int x = 0; x < doorOpenLayer.getWidth(); x++) {
                    TiledMapTileLayer.Cell cell = doorOpenLayer.getCell(x, y);
                    if (cell != null && cell.getTile() != null) {
                        return new Vector2(x, y);
                    }
                }
            }
        }
        return new Vector2(10, 10);
    }

    public boolean isPlayerNearDoorOpen(Vector2 playerPos) {
        if (!doorOpen || doorOpenLayer == null) return false;

        for (int y = 0; y < doorOpenLayer.getHeight(); y++) {
            for (int x = 0; x < doorOpenLayer.getWidth(); x++) {
                TiledMapTileLayer.Cell cell = doorOpenLayer.getCell(x, y);
                if (cell != null && cell.getTile() != null) {
                    Vector2 doorTilePos = new Vector2(x, y);
                    if (doorTilePos.dst(playerPos) <= doorActivationRadius) {
                        return true;
                    }
                }
            }
        }
        return false;
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

    public boolean isWall(float worldX, float worldY) {
        if (collisionLayer == null) return false;

        int tileX = (int)Math.floor(worldX);
        int tileY = (int)Math.floor(worldY);

        if (tileX < 0 || tileX >= collisionLayer.getWidth() ||
            tileY < 0 || tileY >= collisionLayer.getHeight()) {
            return true;
        }

        TiledMapTileLayer.Cell cell = collisionLayer.getCell(tileX, tileY);
        if (cell != null && cell.getTile() != null) {
            return true;
        }

        if (!doorOpen) {
            TiledMapTileLayer doorClosedLayer = (TiledMapTileLayer) currentMap.getLayers().get("Door_closed");
            if (doorClosedLayer != null) {
                if (tileX >= 0 && tileX < doorClosedLayer.getWidth() &&
                    tileY >= 0 && tileY < doorClosedLayer.getHeight()) {
                    TiledMapTileLayer.Cell doorCell = doorClosedLayer.getCell(tileX, tileY);
                    if (doorCell != null && doorCell.getTile() != null) {
                        return true;
                    }
                }
            }
        }

        return false;
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
        if (tileBatch != null) tileBatch.dispose();
        transition.dispose();
    }
}
