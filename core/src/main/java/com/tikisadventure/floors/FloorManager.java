package com.tikisadventure.floors;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileSet;
import com.badlogic.gdx.maps.tiled.TiledMapTileSets;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.tikisadventure.core.Assets;
import com.tikisadventure.core.GameSession;
import com.badlogic.gdx.graphics.Color;

import java.util.HashSet;
import java.util.Set;
import java.util.Random;
import com.badlogic.gdx.math.GridPoint2;

public class FloorManager {

    private int currentFloor;
    private int totalFloors;
    private FloorTransition transition;

    private TiledMap currentMap;
    private TiledMapTileLayer collisionLayer;
    private TiledMapTileLayer backgroundLayer;
    private TiledMapTileLayer shadowsLayer;
    private TiledMapTileLayer floorLayer;
    private TiledMapTileLayer borderLayer;
    private java.util.HashSet<Integer> cactusTileIds;
    private TiledMapTileLayer topPathLayer; private TiledMapTileLayer topDoorClosedLayer; private TiledMapTileLayer topDoorOpenLayer;
    private TiledMapTileLayer leftPathLayer; private TiledMapTileLayer leftDoorClosedLayer; private TiledMapTileLayer leftDoorOpenLayer;
    private TiledMapTileLayer rightPathLayer; private TiledMapTileLayer rightDoorClosedLayer; private TiledMapTileLayer rightDoorOpenLayer;
    private TiledMapTileLayer transparentLayer;
    private TiledMapTileLayer miniObjectsLayer;
    private TiledMapTileLayer playerSpawnLayer;
    private TiledMapTileLayer enemiesSpawnLayer;
    private TiledMapTileLayer ground2Layer;
    private boolean doorOpen = false;
    private Random rng;

    // Generación procedural interior
    private Array<ObjectTemplate> treeTemplates;
    private Array<ObjectTemplate> rockTemplates;
    private Array<ObjectTemplate> cactusTemplates;
    private Set<GridPoint2> proceduralCollision;
    private Set<GridPoint2> placedObjectTiles;
    private TiledMapTileLayer proceduralObjectsLayer;
    private TiledMapTileLayer proceduralObjectsLayerBg;
    private TiledMapTileLayer proceduralDecorationsLayer;
    private TiledMapTileLayer proceduralAbovePlayerLayer;
    private int[] decorationTileIds;

    // --- VARIABLES PARA BOSQUE INFINITO ---
    private Array<OuterDecorativeObject> outerObjects;
    private Array<OuterDecoration> outerDecorations;
    private Set<GridPoint2> outerOccupiedTiles;
    private static final int OUTER_AREA_BUFFER = 60;
    private static final int RENDER_PADDING = 4;
    private static final int BORDER_MARGIN = 2;
    // --------------------------------------

    private enum DoorDirection { TOP, LEFT, RIGHT }
    private DoorDirection chosenDoor = DoorDirection.TOP;
    private float roundTimer = 0f;
    private static final float ROUND_DURATION = 20f;
    private SpriteBatch tileBatch;
    private Texture tilesetTexture;
    private int tilesetColumns = 3;
    private int backgroundTileId = 220;
    private int tileWidth = 16;
    private int tileHeight = 16;

    private float tilesPerFloor;
    private float transitionDuration;
    private float doorActivationRadius = 2.0f;

    private JsonValue floorConfig;

    private String currentMapFolder;
    private Set<Integer> usedMapIndices;
    private Array<String> availableMaps;

    private static FloorManager instance;

    public FloorManager(boolean enableParticles) {
        instance = this;
        this.currentFloor = 1;
        this.transition = new FloorTransition(2.0f, enableParticles);
        this.usedMapIndices = new HashSet<>();
        this.availableMaps = new Array<>();
        this.tileBatch = new SpriteBatch();
        this.treeTemplates = new Array<>();
        this.rockTemplates = new Array<>();
        this.cactusTemplates = new Array<>();
        this.proceduralCollision = new HashSet<>();
        this.placedObjectTiles = new HashSet<>();

        // Inicializamos las listas del bosque externo
        this.outerObjects = new Array<>();
        this.outerDecorations = new Array<>();
        this.outerOccupiedTiles = new HashSet<>();

        initTemplates();
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
        String mapName = (GameSession.selectedMapName != null) ? GameSession.selectedMapName : "bosque";
        String mapFile = "maps/" + mapName + "/baseMap.tmx";
        loadMap(mapFile);
        rng = GameSession.getSeededRandomForFloor(currentFloor);
        chosenDoor = DoorDirection.values()[rng.nextInt(3)];
        doorOpen = false;
        roundTimer = 0f;
        proceduralCollision.clear();
        placedObjectTiles.clear();

        if (proceduralObjectsLayer != null) proceduralObjectsLayer = null;
        if (proceduralObjectsLayerBg != null) proceduralObjectsLayerBg = null;
        if (proceduralDecorationsLayer != null) proceduralDecorationsLayer = null;
        if (proceduralAbovePlayerLayer != null) proceduralAbovePlayerLayer = null;

        proceduralObjectsLayer = new TiledMapTileLayer(50, 50, 1, 1);
        proceduralObjectsLayer.setName("Procedural_Objects");
        proceduralObjectsLayerBg = new TiledMapTileLayer(50, 50, 1, 1);
        proceduralObjectsLayerBg.setName("Procedural_Objects_Bg");
        proceduralDecorationsLayer = new TiledMapTileLayer(50, 50, 1, 1);
        proceduralDecorationsLayer.setName("Procedural_Decorations");
        proceduralAbovePlayerLayer = new TiledMapTileLayer(50, 50, 1, 1);
        proceduralAbovePlayerLayer.setName("Procedural_Above_Player");
        if ("bosque".equals(mapName) || "desierto".equals(mapName)) {
            generateProceduralObjects();
            currentMap.getLayers().add(proceduralObjectsLayerBg);
            currentMap.getLayers().add(proceduralObjectsLayer);
            currentMap.getLayers().add(proceduralDecorationsLayer);
            currentMap.getLayers().add(proceduralAbovePlayerLayer);
        }

        if ("desierto".equals(mapName)) {
            cactusTileIds = new java.util.HashSet<>();
            int[] cactusLocalIds = {8, 40, 41};
            for (int id : cactusLocalIds) {
                com.badlogic.gdx.maps.tiled.TiledMapTile tile = resolveTile(id);
                if (tile != null) {
                    cactusTileIds.add(tile.getId());
                    Gdx.app.log("FLOOR", "Cactus tile local " + id + " resolved to GID: " + tile.getId());
                } else {
                    Gdx.app.log("FLOOR", "Cactus tile local " + id + " RESOLVE FAILED!");
                }
            }
        }

        // Generamos el bosque infinito fuera del mapa
        generateOuterInfiniteForest();

        Gdx.app.log("FLOOR", "Generated floor " + currentFloor + " with map: " + mapFile + ", door: " + chosenDoor + ", seed: " + GameSession.currentSeed);
    }

    private void generateOuterInfiniteForest() {
        outerObjects.clear();
        outerDecorations.clear();
        outerOccupiedTiles.clear();

        if (currentMap == null || tilesetTexture == null) return;

        int mapW = floorLayer != null ? floorLayer.getWidth() : 50;
        int mapH = floorLayer != null ? floorLayer.getHeight() : 50;

        int minX = -OUTER_AREA_BUFFER;
        int maxX = mapW + OUTER_AREA_BUFFER;
        int minY = -OUTER_AREA_BUFFER;
        int maxY = mapH + OUTER_AREA_BUFFER;

        // 1. GENERAR OBJETOS GRANDES (Árboles y rocas)
        int totalOuterObjects = 18000;
        for (int i = 0; i < totalOuterObjects; i++) {
            int rx = minX + rng.nextInt(maxX - minX);
            int ry = minY + rng.nextInt(maxY - minY);

            // --- CAMBIO AQUÍ ---
            // 1º Decidimos qué tipo de objeto va a ser antes de comprobar la posición
            boolean isTree = rng.nextFloat() < 0.7f;
            ObjectTemplate template;
            if (isTree) {
                template = treeTemplates.get(rng.nextInt(treeTemplates.size));
            } else {
                template = rockTemplates.get(rng.nextInt(rockTemplates.size));
            }

            // 2º Definimos el margen inferior dinámicamente.
            // Si es árbol usamos 3, si es piedra usamos el BORDER_MARGIN normal (2).
            int bottomMargin = isTree ? 3 : BORDER_MARGIN;

            // 3º Comprobamos la zona de exclusión aplicando el bottomMargin
            // Nota que la parte superior (mapH + BORDER_MARGIN) no cambia.
            if (rx >= -BORDER_MARGIN && rx < mapW + BORDER_MARGIN &&
                ry >= -bottomMargin && ry < mapH + BORDER_MARGIN) {
                continue;
            }
            // -------------------

            if (canPlaceOuterObject(template, rx, ry)) {
                placeOuterObject(template, rx, ry);
            }
        }

        // 2. GENERAR FLORES Y CÉSPED PEQUEÑO
        if (decorationTileIds.length > 0) {
            int totalDecorations = 12000;
            for (int i = 0; i < totalDecorations; i++) {
                int rx = minX + rng.nextInt(maxX - minX);
                int ry = minY + rng.nextInt(maxY - minY);

                // Mantenemos las flores pudiendo nacer hasta el mismo borde (sin BORDER_MARGIN)
                // para que no quede una franja de tierra totalmente vacía.
                if (rx >= 0 && rx < mapW && ry >= 0 && ry < mapH) continue;

                int tileId = decorationTileIds[rng.nextInt(decorationTileIds.length)];

                GridPoint2 key = new GridPoint2(rx, ry);
                if (!outerOccupiedTiles.contains(key)) {
                    outerDecorations.add(new OuterDecoration(rx, ry, tileId));
                    outerOccupiedTiles.add(key);
                }
            }
        }
    }

    private boolean canPlaceOuterObject(ObjectTemplate template, int startX, int startY) {
        // APLICAMOS EL OFFSET ANTES DE COMPROBAR para que coincida con el dibujado real
        int realX = startX - (template.width / 2);

        for (int dy = 0; dy < template.height; dy++) {
            for (int dx = 0; dx < template.width; dx++) {
                int cx = realX + dx, cy = startY + dy;
                GridPoint2 key = new GridPoint2(cx, cy);
                if (outerOccupiedTiles.contains(key)) return false;
            }
        }
        return true;
    }

    private void placeOuterObject(ObjectTemplate template, int startX, int startY) {
        // APLICAMOS EL MISMO OFFSET AQUÍ
        int realX = startX - (template.width / 2);

        for (int dy = 0; dy < template.height; dy++) {
            for (int dx = 0; dx < template.width; dx++) {
                outerOccupiedTiles.add(new GridPoint2(realX + dx, startY + dy));
            }
        }
        // Pasamos la coordenada REAL ya calculada al constructor
        outerObjects.add(new OuterDecorativeObject(realX, startY, template));
    }

    public Vector2 findValidSpawnPosition(int minX, int maxX, int minY, int maxY) {
        for (int attempts = 0; attempts < 200; attempts++) {
            int x = minX + rng.nextInt(maxX - minX + 1);
            int y = minY + rng.nextInt(maxY - minY + 1);

            if (isValidSpawnTile(x, y)) {
                return new Vector2(x, y);
            }
        }
        return new Vector2(10, 10);
    }

    public Vector2 getPlayerSpawnPosition() {
        if (playerSpawnLayer == null) {
            return findValidSpawnPosition(1, 48, 1, 48);
        }

        Array<Vector2> positions = new Array<>();
        for (int y = 0; y < playerSpawnLayer.getHeight(); y++) {
            for (int x = 0; x < playerSpawnLayer.getWidth(); x++) {
                TiledMapTileLayer.Cell cell = playerSpawnLayer.getCell(x, y);
                if (cell != null && cell.getTile() != null) {
                    if (isValidSpawnTile(x, y)) {
                        positions.add(new Vector2(x, y));
                    }
                }
            }
        }

        if (positions.size == 0) return findValidSpawnPosition(1, 48, 1, 48);
        return positions.get(rng.nextInt(positions.size));
    }

    public Array<Vector2> getEnemySpawnPositions() {
        if (enemiesSpawnLayer == null) {
            Array<Vector2> defaultPos = new Array<>();
            defaultPos.add(new Vector2(3, 3));
            defaultPos.add(new Vector2(17, 17));
            return defaultPos;
        }

        Array<Vector2> positions = new Array<>();
        for (int y = 0; y < enemiesSpawnLayer.getHeight(); y++) {
            for (int x = 0; x < enemiesSpawnLayer.getWidth(); x++) {
                TiledMapTileLayer.Cell cell = enemiesSpawnLayer.getCell(x, y);
                if (cell != null && cell.getTile() != null) {
                    positions.add(new Vector2(x, y));
                }
            }
        }

        if (positions.size == 0) {
            Array<Vector2> defaultPos = new Array<>();
            defaultPos.add(new Vector2(3, 3));
            defaultPos.add(new Vector2(17, 17));
            return defaultPos;
        }
        return positions;
    }

    private void loadAvailableMaps() {
        String mapName = (GameSession.selectedMapName != null) ? GameSession.selectedMapName : "bosque";
        currentMapFolder = "maps/" + mapName + "/";

        availableMaps.clear();
        FileHandle mapDir = Gdx.files.internal(currentMapFolder);

        if (!mapDir.isDirectory() || mapDir.list().length == 0) {
            mapDir = Gdx.files.internal("assets/" + currentMapFolder);
            currentMapFolder = "assets/" + currentMapFolder;
        }

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
    }

    private String selectRandomMap() {
        loadAvailableMaps();
        if (availableMaps.size == 0) loadAvailableMaps();
        int randomIndex = rng.nextInt(availableMaps.size);
        return availableMaps.get(randomIndex);
    }

    private void loadMap(String mapFile) {
        if (currentMap != null) currentMap.dispose();

        try {
            currentMap = new TmxMapLoader().load(mapFile);
            TiledMapTileLayer floorLayerTemp = (TiledMapTileLayer) currentMap.getLayers().get("Floor");
            TiledMapTileLayer borderLayerTemp = (TiledMapTileLayer) currentMap.getLayers().get("Border");
            backgroundLayer = (floorLayerTemp != null) ? floorLayerTemp : (TiledMapTileLayer) currentMap.getLayers().get("Ground");
            collisionLayer = (borderLayerTemp != null) ? borderLayerTemp : (TiledMapTileLayer) currentMap.getLayers().get("Objects");

            if (collisionLayer != null) {
                Gdx.app.log("FLOOR", "Collision layer loaded: " + collisionLayer.getName() + ", size: " + collisionLayer.getWidth() + "x" + collisionLayer.getHeight());
            } else {
                Gdx.app.error("FLOOR", "Collision layer is NULL for map: " + mapFile);
            }
            transparentLayer = (TiledMapTileLayer) currentMap.getLayers().get("Transparent");
            topPathLayer = (TiledMapTileLayer) currentMap.getLayers().get("Top_path");
            leftPathLayer = (TiledMapTileLayer) currentMap.getLayers().get("Left_path");
            rightPathLayer = (TiledMapTileLayer) currentMap.getLayers().get("Right_path");
            topDoorClosedLayer = (TiledMapTileLayer) currentMap.getLayers().get("Top_door_closed");
            topDoorOpenLayer = (TiledMapTileLayer) currentMap.getLayers().get("Top_door_open");
            leftDoorClosedLayer = (TiledMapTileLayer) currentMap.getLayers().get("Left_door_closed");
            leftDoorOpenLayer = (TiledMapTileLayer) currentMap.getLayers().get("Left_door_open");
            rightDoorClosedLayer = (TiledMapTileLayer) currentMap.getLayers().get("Right_door_closed");
            rightDoorOpenLayer = (TiledMapTileLayer) currentMap.getLayers().get("Right_door_open");
            miniObjectsLayer = (TiledMapTileLayer) currentMap.getLayers().get("Mini_objects");
            playerSpawnLayer = (TiledMapTileLayer) currentMap.getLayers().get("Player_spawn");
            enemiesSpawnLayer = (TiledMapTileLayer) currentMap.getLayers().get("Enemies_spawn");
            ground2Layer = (TiledMapTileLayer) currentMap.getLayers().get("Ground_2");

            if (backgroundLayer != null) {
                tilesetTexture = loadTilesetTexture(mapFile);
            }

            if (mapFile.contains("desierto")) {
                backgroundTileId = 52;
            } else {
                backgroundTileId = 220;
            }

            // Debug: report loaded door/path layers
            if (topPathLayer != null) Gdx.app.log("FLOOR", "Top_path loaded: " + topPathLayer.getWidth() + "x" + topPathLayer.getHeight());
            else Gdx.app.log("FLOOR", "Top_path not found");
            if (topDoorClosedLayer != null) Gdx.app.log("FLOOR", "Top_door_closed loaded: " + topDoorClosedLayer.getWidth() + "x" + topDoorClosedLayer.getHeight());
            if (topDoorOpenLayer != null) Gdx.app.log("FLOOR", "Top_door_open loaded: " + topDoorOpenLayer.getWidth() + "x" + topDoorOpenLayer.getHeight());
            if (leftPathLayer != null) Gdx.app.log("FLOOR", "Left_path loaded: " + leftPathLayer.getWidth() + "x" + leftPathLayer.getHeight());
            if (leftDoorClosedLayer != null) Gdx.app.log("FLOOR", "Left_door_closed loaded: " + leftDoorClosedLayer.getWidth() + "x" + leftDoorClosedLayer.getHeight());
            if (leftDoorOpenLayer != null) Gdx.app.log("FLOOR", "Left_door_open loaded: " + leftDoorOpenLayer.getWidth() + "x" + leftDoorOpenLayer.getHeight());
            if (rightPathLayer != null) Gdx.app.log("FLOOR", "Right_path loaded: " + rightPathLayer.getWidth() + "x" + rightPathLayer.getHeight());
            if (rightDoorClosedLayer != null) Gdx.app.log("FLOOR", "Right_door_closed loaded: " + rightDoorClosedLayer.getWidth() + "x" + rightDoorClosedLayer.getHeight());
            if (rightDoorOpenLayer != null) Gdx.app.log("FLOOR", "Right_door_open loaded: " + rightDoorOpenLayer.getWidth() + "x" + rightDoorOpenLayer.getHeight());

            if (collisionLayer == null) {
                Gdx.app.error("FLOOR", "Collision layer not found in map: " + mapFile);
            }
            // Puerta seleccionada en generateFloor; no re-seleccionar aquí
        } catch (Exception e) {
            Gdx.app.error("FLOOR", "Error loading map: " + mapFile, e);
            currentMap = null;
            collisionLayer = null;
            backgroundLayer = null;
            transparentLayer = null;
        }
    }

    private Texture loadTilesetTexture(String mapFile) {
        try {
            FileHandle mapFileHandle = Gdx.files.internal(mapFile);
            String mapContent = mapFileHandle.readString();
            String mapDir = mapFile.substring(0, mapFile.lastIndexOf('/') + 1);

            Gdx.app.log("FLOOR", "Loading tileset from map: " + mapFile + ", dir: " + mapDir);

            int tilesetStart = mapContent.indexOf("<tileset");
            if (tilesetStart != -1) {
                int tilesetEnd = mapContent.indexOf(">", tilesetStart);
                String tilesetTag = mapContent.substring(tilesetStart, tilesetEnd);

                int columnsStart = tilesetTag.indexOf("columns=\"");
                if (columnsStart != -1) {
                    columnsStart += 9;
                    int columnsEnd = tilesetTag.indexOf("\"", columnsStart);
                    tilesetColumns = Integer.parseInt(tilesetTag.substring(columnsStart, columnsEnd));
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
                                Gdx.app.log("FLOOR", "Read columns from TSX: " + tilesetColumns);
                                tilesetColumns = Integer.parseInt(tsxContent.substring(tsxColumnsStart, tsxColumnsEnd));
                            }
                        }
                        int imageStart = tsxContent.indexOf("source=\"") + 8;
                        int imageEnd = tsxContent.indexOf("\"", imageStart);
                        String imageName = tsxContent.substring(imageStart, imageEnd);
                        return new Texture(tsxFile.parent().child(imageName));
                    }
                }
            }
        } catch (Exception e) {
            Gdx.app.error("FLOOR", "Could not auto-detect tileset, using default", e);
        }
        tilesetColumns = 30;
        return new Texture(currentMapFolder + "forest_sprites.png");
    }

    private void initTemplates() {
        if ("desierto".equals(GameSession.selectedMapName)) {
            treeTemplates.add(new ObjectTemplate("Obj1", 4, 5,
                new int[][]{
                    {44, 45, 46, 47},
                    {33, 34, 35, 36},
                    {22, 23, 24, 25},
                    {11, 12, 13, 14},
                    {0, 1, 2, 3}
                },
                new boolean[][]{
                    {false, true, true, false},
                    {false, false, false, false},
                    {false, false, false, false},
                    {false, false, false, false},
                    {false, false, false, false}
                },
                0, true));

            rockTemplates.add(new ObjectTemplate("Obj2", 2, 1,
                new int[][]{{51, 52}},
                new boolean[][]{{true, true}}));
            rockTemplates.add(new ObjectTemplate("Obj3", 2, 2,
                new int[][]{{20, 21}, {9, 10}},
                new boolean[][]{{true, true}, {true, true}}));
            rockTemplates.add(new ObjectTemplate("Obj4", 2, 2,
                new int[][]{{53, 54}, {42, 43}},
                new boolean[][]{{false, false}, {false, false}}));
            rockTemplates.add(new ObjectTemplate("Obj5", 2, 1,
                new int[][]{{51, 52}},
                new boolean[][]{{true, true}}));
            rockTemplates.add(new ObjectTemplate("Obj6", 2, 1,
                new int[][]{{29, 30}},
                new boolean[][]{{false, false}}));
            rockTemplates.add(new ObjectTemplate("Obj7", 2, 1,
                new int[][]{{31, 32}},
                new boolean[][]{{false, false}}));

            int[][] simples = {{4}, {5}};
            for (int i = 0; i < simples.length; i++) {
                rockTemplates.add(new ObjectTemplate("Simple" + (i+1), 1, 1,
                    new int[][]{simples[i]}, new boolean[][]{{true}}));
            }

            int[][] foregroundDecos = {{6}, {7}, {15}, {16}, {17}, {18}, {19}};
            for (int i = 0; i < foregroundDecos.length; i++) {
                rockTemplates.add(new ObjectTemplate("FgDeco" + (i+1), 1, 1,
                    new int[][]{foregroundDecos[i]}, new boolean[][]{{false}}));
            }

            cactusTemplates.add(new ObjectTemplate("Cactus1", 1, 1,
                new int[][]{{8}}, new boolean[][]{{false}}));
            cactusTemplates.add(new ObjectTemplate("Cactus2", 1, 1,
                new int[][]{{40}}, new boolean[][]{{false}}));
            cactusTemplates.add(new ObjectTemplate("Cactus3", 1, 1,
                new int[][]{{41}}, new boolean[][]{{false}}));

            decorationTileIds = new int[]{26, 27, 28, 37, 38, 39, 48, 49, 50};
        } else {
            // Árboles
            treeTemplates.add(new ObjectTemplate("Tree1", 3, 4,
                new int[][]{{662, 663, 664}, {632, 633, 634}, {602, 603, 604}, {572, 573, 574}},
                new boolean[][]{{false, true, false}, {false, false, false}, {false, false, false}, {false, false, false}}));
            treeTemplates.add(new ObjectTemplate("Tree2", 3, 4,
                new int[][]{{665, 666, 667}, {635, 636, 637}, {605, 606, 607}, {575, 576, 577}},
                new boolean[][]{{false, true, false}, {false, false, false}, {false, false, false}, {false, false, false}}));
            treeTemplates.add(new ObjectTemplate("Tree3", 3, 5,
                new int[][]{{698, 699, 700}, {668, 669, 670}, {638, 639, 640}, {608, 609, 610}, {578, 579, 580}},
                new boolean[][]{{false, true, false}, {false, false, false}, {false, false, false}, {false, false, false}, {false, false, false}}));

            // Rocas
            rockTemplates.add(new ObjectTemplate("Rock1", 1, 1, new int[][]{{722}}, new boolean[][]{{true}}));
            rockTemplates.add(new ObjectTemplate("Rock2", 2, 1, new int[][]{{723, 724}}, new boolean[][]{{true, true}}));
            rockTemplates.add(new ObjectTemplate("Rock3", 2, 1, new int[][]{{725, 726}}, new boolean[][]{{true, true}}));
            rockTemplates.add(new ObjectTemplate("Rock4", 2, 2, new int[][]{{757, 758}, {727, 728}}, new boolean[][]{{true, true}, {true, true}}));
            rockTemplates.add(new ObjectTemplate("Rock5", 2, 2, new int[][]{{782, 783}, {752, 753}}, new boolean[][]{{true, true}, {true, true}}));
            rockTemplates.add(new ObjectTemplate("Rock6", 2, 2, new int[][]{{784, 785}, {754, 755}}, new boolean[][]{{true, true}, {true, true}}));

            decorationTileIds = new int[]{188, 217, 404, 405, 434, 435};
        }
    }

    private void generateProceduralObjects() {
        if ("desierto".equals(GameSession.selectedMapName)) {
            int numObj1 = rng.nextInt(8) + 5;
            int numObstacles = rng.nextInt(20) + 30;
            int numCactus = rng.nextInt(10) + 10;

            for (int i = 0; i < numObj1; i++) placeRandomObject(treeTemplates);
            for (int i = 0; i < numObstacles; i++) placeRandomObject(rockTemplates);
            for (int i = 0; i < numCactus; i++) placeRandomObject(cactusTemplates);
            generateFloorDecorations();

            Gdx.app.log("FLOOR", "Desert: placed " + numObj1 + " obj1, " + numObstacles + " obstacles, " + numCactus + " cactus (" + proceduralCollision.size() + " collision tiles)");
        } else {
            int numTrees = rng.nextInt(8) + 12;
            int numRocks = rng.nextInt(8) + 10;
            int numDecorations = rng.nextInt(201) + 300;

            for (int i = 0; i < numTrees; i++) placeRandomObject(treeTemplates);
            for (int i = 0; i < numRocks; i++) placeRandomObject(rockTemplates);

            generateFloorDecorations();

            Gdx.app.log("FLOOR", "Placed " + numTrees + " trees, " + numRocks + " rocks, " + numDecorations + " decorations (" + proceduralCollision.size() + " collision tiles)");
        }
    }

    private void placeRandomObject(Array<ObjectTemplate> templates) {
        ObjectTemplate template = templates.get(rng.nextInt(templates.size));
        int mapW = floorLayer != null ? floorLayer.getWidth() : 50;
        int mapH = floorLayer != null ? floorLayer.getHeight() : 50;

        for (int attempt = 0; attempt < 200; attempt++) {
            int x = rng.nextInt(Math.max(1, mapW - template.width));
            int y = rng.nextInt(Math.max(1, mapH - template.height));

            if (canPlaceObject(template, x, y, mapW, mapH)) {
                placeObject(template, x, y);
                return;
            }
        }
    }

    private void generateFloorDecorations() {
        int mapW = floorLayer != null ? floorLayer.getWidth() : 50;
        int mapH = floorLayer != null ? floorLayer.getHeight() : 50;

        Array<Vector2> validTiles = new Array<>();
        for (int y = 0; y < mapH; y++) {
            for (int x = 0; x < mapW; x++) {
                if (canPlaceDecoration(x, y, mapW, mapH)) {
                    validTiles.add(new Vector2(x, y));
                }
            }
        }

        int numToPlace = Math.min(validTiles.size, rng.nextInt(301) + 400);
        for (int i = 0; i < numToPlace && decorationTileIds.length > 0; i++) {
            int idx = rng.nextInt(validTiles.size);
            int tileX = (int)validTiles.get(idx).x;
            int tileY = (int)validTiles.get(idx).y;
            int tileId = decorationTileIds[rng.nextInt(decorationTileIds.length)];
            com.badlogic.gdx.maps.tiled.TiledMapTile tile = resolveTile(tileId);
            if (tile == null) continue;
            TiledMapTileLayer.Cell cell = new TiledMapTileLayer.Cell();
            cell.setTile(tile);
            proceduralDecorationsLayer.setCell(tileX, tileY, cell);
            validTiles.removeIndex(idx);
        }
    }

    private boolean canPlaceDecoration(int x, int y, int mapW, int mapH) {
        if (x < 0 || x >= mapW || y < 0 || y >= mapH) return false;
        if (proceduralDecorationsLayer.getCell(x, y) != null && proceduralDecorationsLayer.getCell(x, y).getTile() != null) return false;
        if (collisionLayer != null && x >= 0 && x < collisionLayer.getWidth() && y >= 0 && y < collisionLayer.getHeight()) {
            TiledMapTileLayer.Cell borderCell = collisionLayer.getCell(x, y);
            if (borderCell != null && borderCell.getTile() != null) return false;
        }

        TiledMapTileLayer doorLayer = getActiveClosedDoorLayer();
        if (doorLayer != null && x >= 0 && x < doorLayer.getWidth() && y >= 0 && y < doorLayer.getHeight()) {
            TiledMapTileLayer.Cell doorCell = doorLayer.getCell(x, y);
            if (doorCell != null && doorCell.getTile() != null) return false;
        }

        TiledMapTileLayer pathLayer = getActivePathLayer();
        if (pathLayer != null && x >= 0 && x < pathLayer.getWidth() && y >= 0 && y < pathLayer.getHeight()) {
            TiledMapTileLayer.Cell pathCell = pathLayer.getCell(x, y);
            if (pathCell != null && pathCell.getTile() != null) return false;
        }

        TiledMapTileLayer doorOpenLayer = null;
        switch (chosenDoor) {
            case TOP: doorOpenLayer = topDoorOpenLayer; break;
            case LEFT: doorOpenLayer = leftDoorOpenLayer; break;
            case RIGHT: doorOpenLayer = rightDoorOpenLayer; break;
        }
        if (doorOpenLayer != null && x >= 0 && x < doorOpenLayer.getWidth() && y >= 0 && y < doorOpenLayer.getHeight()) {
            TiledMapTileLayer.Cell openDoorCell = doorOpenLayer.getCell(x, y);
            if (openDoorCell != null && openDoorCell.getTile() != null) return false;
        }

        if (playerSpawnLayer != null && x >= 0 && x < playerSpawnLayer.getWidth() && y >= 0 && y < playerSpawnLayer.getHeight()) {
            TiledMapTileLayer.Cell spawnCell = playerSpawnLayer.getCell(x, y);
            if (spawnCell != null && spawnCell.getTile() != null) return false;
        }

        if (placedObjectTiles != null && placedObjectTiles.contains(new GridPoint2(x, y))) return false;
        if (proceduralObjectsLayer != null && proceduralObjectsLayer.getCell(x, y) != null && proceduralObjectsLayer.getCell(x, y).getTile() != null) return false;
        if (proceduralObjectsLayerBg != null && proceduralObjectsLayerBg.getCell(x, y) != null && proceduralObjectsLayerBg.getCell(x, y).getTile() != null) return false;
        if (proceduralAbovePlayerLayer != null && proceduralAbovePlayerLayer.getCell(x, y) != null && proceduralAbovePlayerLayer.getCell(x, y).getTile() != null) return false;

        return true;
    }

    private boolean canPlaceObject(ObjectTemplate template, int x, int y, int mapW, int mapH) {
        if (x < 1 || y < 1 || x + template.width > mapW - 1 || y + template.height > mapH - 1) return false;
        for (int dy = 0; dy < template.height; dy++) {
            for (int dx = 0; dx < template.width; dx++) {
                int cx = x + dx, cy = y + dy;
                if (cx < 0 || cx >= mapW || cy < 0 || cy >= mapH) return false;
                if (floorLayer != null) {
                    TiledMapTileLayer.Cell floorCell = floorLayer.getCell(cx, cy);
                    if (floorCell == null || floorCell.getTile() == null) return false;
                }
                if (collisionLayer != null && cx >= 0 && cx < collisionLayer.getWidth() && cy >= 0 && cy < collisionLayer.getHeight()) {
                    TiledMapTileLayer.Cell borderCell = collisionLayer.getCell(cx, cy);
                    if (borderCell != null && borderCell.getTile() != null) return false;
                }
                TiledMapTileLayer doorLayer = getActiveClosedDoorLayer();
                if (doorLayer != null && cx >= 0 && cx < doorLayer.getWidth() && cy >= 0 && cy < doorLayer.getHeight()) {
                    TiledMapTileLayer.Cell doorCell = doorLayer.getCell(cx, cy);
                    if (doorCell != null && doorCell.getTile() != null) return false;
                }
                TiledMapTileLayer pathLayer = getActivePathLayer();
                if (pathLayer != null && cx >= 0 && cx < pathLayer.getWidth() && cy >= 0 && cy < pathLayer.getHeight()) {
                    TiledMapTileLayer.Cell pathCell = pathLayer.getCell(cx, cy);
                    if (pathCell != null && pathCell.getTile() != null) return false;
                }
                if (playerSpawnLayer != null && cx >= 0 && cx < playerSpawnLayer.getWidth() && cy >= 0 && cy < playerSpawnLayer.getHeight()) {
                    TiledMapTileLayer.Cell spawnCell = playerSpawnLayer.getCell(cx, cy);
                    if (spawnCell != null && spawnCell.getTile() != null) return false;
                }
                for (int sdx = -2; sdx <= 2; sdx++) {
                    for (int sdy = -2; sdy <= 2; sdy++) {
                        GridPoint2 key = new GridPoint2(cx + sdx, cy + sdy);
                        if (placedObjectTiles.contains(key)) return false;
                    }
                }
            }
        }
        return true;
    }

    private com.badlogic.gdx.maps.tiled.TiledMapTile resolveTile(int tileId) {
        int numTileSets = 0;
        for (TiledMapTileSet _ts : currentMap.getTileSets()) numTileSets++;
        for (int i = numTileSets - 1; i >= 0; i--) {
            TiledMapTileSet ts = currentMap.getTileSets().getTileSet(i);
            com.badlogic.gdx.maps.tiled.TiledMapTile tile = ts.getTile(tileId);
            if (tile == null) {
                int firstgid = Integer.MAX_VALUE;
                for (com.badlogic.gdx.maps.tiled.TiledMapTile t : ts) {
                    firstgid = Math.min(firstgid, t.getId());
                }
                tile = ts.getTile(firstgid + tileId);
            }
            if (tile != null) return tile;
        }
        return null;
    }

    private void placeObject(ObjectTemplate template, int x, int y) {
        for (int dy = 0; dy < template.height; dy++) {
            TiledMapTileLayer target;
            if (template.abovePlayer) {
                target = (template.ysplit >= 0 && dy < template.ysplit)
                    ? proceduralObjectsLayerBg : proceduralAbovePlayerLayer;
            } else {
                target = (template.ysplit >= 0 && dy < template.ysplit)
                    ? proceduralObjectsLayerBg : proceduralObjectsLayer;
            }
            for (int dx = 0; dx < template.width; dx++) {
                int tileId = template.tileIds[dy][dx];
                com.badlogic.gdx.maps.tiled.TiledMapTile tile = resolveTile(tileId);
                if (tile == null) continue;
                TiledMapTileLayer.Cell cell = new TiledMapTileLayer.Cell();
                cell.setTile(tile);
                target.setCell(x + dx, y + dy, cell);

                if (template.collision[dy][dx]) {
                    proceduralCollision.add(new GridPoint2(x + dx, y + dy));
                }
                placedObjectTiles.add(new GridPoint2(x + dx, y + dy));
            }
        }
    }

    public void update(float delta) {
        transition.update(delta);
    }

    public void renderMap(OrthographicCamera camera) {
        if ((floorLayer == null && backgroundLayer == null) || tilesetTexture == null) return;

        tileBatch.setProjectionMatrix(camera.combined);
        tileBatch.begin();

        // 1. DIBUJAR CÉSPED INFINITO (Color normal, sin filtro)
        tileBatch.setColor(Color.WHITE);
        renderBackgroundTile(camera);

        // 2. DIBUJAR FLORES Y DECORACIONES EXTERNAS (Color normal)
        for (OuterDecoration dec : outerDecorations) {
            if (Math.abs(dec.x - camera.position.x) < OUTER_AREA_BUFFER &&
                Math.abs(dec.y - camera.position.y) < OUTER_AREA_BUFFER) {

                com.badlogic.gdx.maps.tiled.TiledMapTile tile = resolveTile(dec.tileId);
                if (tile != null) {
                    tileBatch.draw(tile.getTextureRegion(), dec.x, dec.y, 1, 1);
                }
            }
        }

        // 3. APLICAR FILTRO GRIS SUAVE SÓLO A ÁRBOLES Y ROCAS EXTERNOS

        tileBatch.setColor(0.7F, 0.7f, 0.7f, 1.0f);

        // 4. DIBUJAR ÁRBOLES Y PIEDRAS EXTERNAS (Con el filtro activo)
        for (OuterDecorativeObject obj : outerObjects) {
            if (Math.abs(obj.x - camera.position.x) < OUTER_AREA_BUFFER &&
                Math.abs(obj.y - camera.position.y) < OUTER_AREA_BUFFER) {

                for (int dy = 0; dy < obj.template.height; dy++) {
                    for (int dx = 0; dx < obj.template.width; dx++) {
                        int tileId = obj.template.tileIds[dy][dx];
                        com.badlogic.gdx.maps.tiled.TiledMapTile tile = resolveTile(tileId);
                        if (tile != null) {
                            tileBatch.draw(tile.getTextureRegion(), obj.x + dx, obj.y + dy, 1, 1);
                        }
                    }
                }
            }
        }

        // 5. RESTAURAR COLOR BLANCO PARA EL MAPA JUGABLE
        tileBatch.setColor(Color.WHITE);

        // 6. RENDERIZAR MAPA INTERNO
        renderLayerInternal(shadowsLayer);
        if (floorLayer != null) renderLayerInternal(floorLayer);
        else if (backgroundLayer != null) renderLayerInternal(backgroundLayer);
        renderLayerInternal(borderLayer != null ? borderLayer : collisionLayer);
        renderLayerInternal(miniObjectsLayer);

        if (chosenDoor == DoorDirection.TOP) {
            if (topPathLayer != null) renderLayerInternal(topPathLayer);
            if (topDoorClosedLayer != null && !doorOpen) renderLayerInternal(topDoorClosedLayer);
            if (topDoorOpenLayer != null && doorOpen) renderLayerInternal(topDoorOpenLayer);
        } else if (chosenDoor == DoorDirection.LEFT) {
            if (leftPathLayer != null) renderLayerInternal(leftPathLayer);
            if (leftDoorClosedLayer != null && !doorOpen) renderLayerInternal(leftDoorClosedLayer);
            if (leftDoorOpenLayer != null && doorOpen) renderLayerInternal(leftDoorOpenLayer);
        } else {
            if (rightPathLayer != null) renderLayerInternal(rightPathLayer);
            if (rightDoorClosedLayer != null && !doorOpen) renderLayerInternal(rightDoorClosedLayer);
            if (rightDoorOpenLayer != null && doorOpen) renderLayerInternal(rightDoorOpenLayer);
        }

        tileBatch.end();
    }

    private void renderBackgroundTile(OrthographicCamera camera) {
        int tileId = backgroundTileId;
        int maxTileId = tilesetColumns * (tilesetTexture.getHeight() / tileHeight);
        if (tileId > maxTileId) tileId = 1;
        int tileIndex = tileId - 1;
        int srcCol = tileIndex % tilesetColumns;
        int srcRow = tileIndex / tilesetColumns;
        int srcX = srcCol * tileWidth;
        int srcY = srcRow * tileHeight;

        TextureRegion region = new TextureRegion(tilesetTexture, srcX, srcY, tileWidth, tileHeight);

        float halfW = camera.viewportWidth / 2f;
        float halfH = camera.viewportHeight / 2f;

        // APLICAMOS EL PADDING DE SEGURIDAD AQUÍ
        // Extendemos el inicio y fin de la cuadrícula de mosaicos
        int startX = (int) Math.floor(camera.position.x - halfW) - RENDER_PADDING;
        int startY = (int) Math.floor(camera.position.y - halfH) - RENDER_PADDING;
        int endX = (int) Math.ceil(camera.position.x + halfW) + RENDER_PADDING;
        int endY = (int) Math.ceil(camera.position.y + halfH) + RENDER_PADDING;

        for (int y = startY; y <= endY; y++) {
                for (int x = startX; x <= endX; x++) {
                    tileBatch.draw(region, x, y, 1, 1);
                }
        }
    }

    private void renderLayerInternal(TiledMapTileLayer layer){
        renderLayerInternal(layer, tileBatch);
    }

    private void renderLayerInternal(TiledMapTileLayer layer, Batch batch){
        if (layer == null) return;
        int height = layer.getHeight();
        int width = layer.getWidth();
        for (int y = 0; y < height; y++){
            for (int x = 0; x < width; x++){
                TiledMapTileLayer.Cell cell = layer.getCell(x, y);
                if (cell != null && cell.getTile() != null){
                    renderTile(cell, x, y, batch);
                }
            }
        }
    }

    private void renderTile(TiledMapTileLayer.Cell cell, int x, int y) {
        renderTile(cell, x, y, tileBatch);
    }

    private void renderTile(TiledMapTileLayer.Cell cell, int x, int y, Batch batch) {
        TextureRegion region = cell.getTile().getTextureRegion();
        batch.draw(region, x, y, 1, 1);
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

    public void renderProceduralDecorations(Batch batch) {
        if (proceduralDecorationsLayer == null || tilesetTexture == null) return;
        renderLayerInternal(proceduralDecorationsLayer, batch);
    }

    public void renderProceduralObjects(Batch batch) {
        if (proceduralObjectsLayer == null || tilesetTexture == null) return;
        renderLayerInternal(proceduralObjectsLayer, batch);
    }

    public void renderProceduralAbovePlayer(Batch batch) {
        if (proceduralAbovePlayerLayer == null || tilesetTexture == null) return;
        renderLayerInternal(proceduralAbovePlayerLayer, batch);
    }

    public void renderProceduralObjectsBg(Batch batch) {
        if (proceduralObjectsLayerBg == null || tilesetTexture == null) return;
        renderLayerInternal(proceduralObjectsLayerBg, batch);
    }

    public void showDoorOpen() { doorOpen = true; }
    public void hideDoorOpen() { doorOpen = false; }

    public void startTransition() {
        float floorHeight = tilesPerFloor;
        float startY = (currentFloor - 1) * floorHeight;
        float endY = currentFloor * floorHeight;
        Vector2 doorPos = findDoorPosition();
        if (doorPos != null) {
            transition.startTransition(doorPos, startY, endY);
        }
    }

    private Vector2 findDoorPosition() {
        TiledMapTileLayer target = null;
        switch (chosenDoor) {
            case TOP: target = (topDoorClosedLayer != null) ? topDoorClosedLayer : (topDoorOpenLayer != null ? topDoorOpenLayer : null); break;
            case LEFT: target = (leftDoorClosedLayer != null) ? leftDoorClosedLayer : (leftDoorOpenLayer != null ? leftDoorOpenLayer : null); break;
            case RIGHT: target = (rightDoorClosedLayer != null) ? rightDoorClosedLayer : (rightDoorOpenLayer != null ? rightDoorOpenLayer : null); break;
        }
        if (target != null) {
            for (int y = 0; y < target.getHeight(); y++) {
                for (int x = 0; x < target.getWidth(); x++) {
                    TiledMapTileLayer.Cell cell = target.getCell(x, y);
                    if (cell != null && cell.getTile() != null) return new Vector2(x, y);
                }
            }
        }
        return null;
    }

    public Vector2 getDoorPosition() { return findDoorPosition(); }
    public boolean isDoorOpen() { return doorOpen; }

    public boolean isPlayerNearDoorOpen(Vector2 playerPos) {
        if (!doorOpen) return false;
        TiledMapTileLayer target = null;
        switch (chosenDoor) {
            case TOP: target = topDoorOpenLayer; break;
            case LEFT: target = leftDoorOpenLayer; break;
            case RIGHT: target = rightDoorOpenLayer; break;
        }
        if (target == null) return false;
        for (int y = 0; y < target.getHeight(); y++) {
            for (int x = 0; x < target.getWidth(); x++) {
                TiledMapTileLayer.Cell cell = target.getCell(x, y);
                if (cell != null && cell.getTile() != null) {
                    if (new Vector2(x, y).dst(playerPos) <= doorActivationRadius) return true;
                }
            }
        }
        return false;
    }

    public boolean isTransitionComplete() { return transition.isComplete(); }
    public boolean isTransitionActive() { return transition.isActive(); }
    public void completeTransition() { currentFloor++; generateFloor(); transition.reset(); }

    public boolean isWall(float worldX, float worldY) {
        if (collisionLayer == null && miniObjectsLayer == null) return false;

        int tileX = (int)Math.floor(worldX);
        int tileY = (int)Math.floor(worldY);

        if (!doorOpen) {
            TiledMapTileLayer targetClosed = null;
            switch (chosenDoor) {
                case TOP: targetClosed = topDoorClosedLayer; break;
                case LEFT: targetClosed = leftDoorClosedLayer; break;
                case RIGHT: targetClosed = rightDoorClosedLayer; break;
            }
            if (targetClosed != null) {
                if (tileX >= 0 && tileX < targetClosed.getWidth() && tileY >= 0 && tileY < targetClosed.getHeight()) {
                    TiledMapTileLayer.Cell doorCell = targetClosed.getCell(tileX, tileY);
                    if (doorCell != null && doorCell.getTile() != null) return true;
                }
            }
        }

        if (collisionLayer != null) {
            if (tileX < 0 || tileX >= collisionLayer.getWidth() || tileY < 0 || tileY >= collisionLayer.getHeight()) return true;
            TiledMapTileLayer.Cell cell = collisionLayer.getCell(tileX, tileY);
            if (cell != null && cell.getTile() != null) {
                if (doorOpen || hasPathTile(tileX, tileY)) return false;
                Gdx.app.log("FLOOR", "Collision at " + tileX + "," + tileY + " in border layer");
                return true;
            }
        }

        if (miniObjectsLayer != null) {
            if (tileX >= 0 && tileX < miniObjectsLayer.getWidth() && tileY >= 0 && tileY < miniObjectsLayer.getHeight()) {
                TiledMapTileLayer.Cell cell = miniObjectsLayer.getCell(tileX, tileY);
                if (cell != null && cell.getTile() != null) return true;
            }
        }

        if (proceduralCollision != null && proceduralCollision.contains(new GridPoint2(tileX, tileY))) return true;

        return false;
    }

    private boolean hasPathTile(int tileX, int tileY) {
        if (tileX < 0 || tileY < 0) return false;
        TiledMapTileLayer path = null;
        if (chosenDoor == DoorDirection.TOP) path = topPathLayer;
        else if (chosenDoor == DoorDirection.LEFT) path = leftPathLayer;
        else path = rightPathLayer;
        if (path != null && tileX < path.getWidth() && tileY < path.getHeight()) {
            TiledMapTileLayer.Cell cell = path.getCell(tileX, tileY);
            if (cell != null && cell.getTile() != null) return true;
        }
        return false;
    }

    private TiledMapTileLayer getActiveClosedDoorLayer() {
        switch (chosenDoor) {
            case TOP: return topDoorClosedLayer;
            case LEFT: return leftDoorClosedLayer;
            case RIGHT: return rightDoorClosedLayer;
        }
        return null;
    }

    private TiledMapTileLayer getActivePathLayer() {
        switch (chosenDoor) {
            case TOP: return topPathLayer;
            case LEFT: return leftPathLayer;
            case RIGHT: return rightPathLayer;
        }
        return null;
    }

    public void resetTransitionOffset() { transition.reset(); }
    public int getCurrentFloor() { return currentFloor; }
    public int getTotalFloors() { return totalFloors; }
    public float getCameraOffset() { return transition.getCurrentOffset(); }
    public static FloorManager getInstance() { return instance; }
    public TiledMapTileLayer getCollisionLayer() { return collisionLayer; }

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

    public boolean isGameComplete() { return currentFloor > totalFloors; }

    public void dispose() {
        if (currentMap != null) currentMap.dispose();
        if (tileBatch != null) tileBatch.dispose();
        transition.dispose();
    }

    public boolean isCactus(float worldX, float worldY) {
        if (!"desierto".equals(GameSession.selectedMapName)) return false;
        if (proceduralObjectsLayer == null || cactusTileIds == null || cactusTileIds.isEmpty()) return false;
        int tileX = (int)Math.floor(worldX);
        int tileY = (int)Math.floor(worldY);
        if (tileX < 0 || tileX >= proceduralObjectsLayer.getWidth() || tileY < 0 || tileY >= proceduralObjectsLayer.getHeight()) return false;
        TiledMapTileLayer.Cell cell = proceduralObjectsLayer.getCell(tileX, tileY);
        if (cell == null || cell.getTile() == null) return false;
        return cactusTileIds.contains(cell.getTile().getId());
    }

    // =======================================================
    // CLASES INTERNAS DE DATOS (Mantenidas al final)
    // =======================================================
    private static class ObjectTemplate {
        String name;
        int width, height;
        int[][] tileIds;
        boolean[][] collision;
        int ysplit = -1;
        boolean abovePlayer;

        ObjectTemplate(String name, int width, int height, int[][] tileIds, boolean[][] collision) {
            this(name, width, height, tileIds, collision, -1, false);
        }

        ObjectTemplate(String name, int width, int height, int[][] tileIds, boolean[][] collision, int ysplit) {
            this(name, width, height, tileIds, collision, ysplit, false);
        }

        ObjectTemplate(String name, int width, int height, int[][] tileIds, boolean[][] collision, int ysplit, boolean abovePlayer) {
            this.name = name;
            this.width = width;
            this.height = height;
            this.tileIds = tileIds;
            this.collision = collision;
            this.ysplit = ysplit;
            this.abovePlayer = abovePlayer;
        }
    }

    private static class OuterDecorativeObject {
        int x, y;
        ObjectTemplate template;

        public OuterDecorativeObject(int x, int y, ObjectTemplate template) {
            this.x = x;
            this.y = y;
            this.template = template;
        }
    }

    private static class OuterDecoration {
        int x, y;
        int tileId;

        public OuterDecoration(int x, int y, int tileId) {
            this.x = x;
            this.y = y;
            this.tileId = tileId;
        }
    }

    // =======================================================
    // MÉTODOS DE APOYO (Validación de Spawn y Capas Activas)
    // =======================================================
    private boolean isValidSpawnTile(int x, int y) {
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                int cx = x + dx, cy = y + dy;

                if (collisionLayer != null && cx >= 0 && cx < collisionLayer.getWidth() && cy >= 0 && cy < collisionLayer.getHeight()) {
                    TiledMapTileLayer.Cell cell = collisionLayer.getCell(cx, cy);
                    if (cell != null && cell.getTile() != null) return false;
                }

                TiledMapTileLayer doorLayer = getActiveClosedDoorLayer();
                if (doorLayer != null && cx >= 0 && cx < doorLayer.getWidth() && cy >= 0 && cy < doorLayer.getHeight()) {
                    TiledMapTileLayer.Cell cell = doorLayer.getCell(cx, cy);
                    if (cell != null && cell.getTile() != null) return false;
                }

                TiledMapTileLayer pathLayer = getActivePathLayer();
                if (pathLayer != null && cx >= 0 && cx < pathLayer.getWidth() && cy >= 0 && cy < pathLayer.getHeight()) {
                    TiledMapTileLayer.Cell cell = pathLayer.getCell(cx, cy);
                    if (cell != null && cell.getTile() != null) return false;
                }
            }
        }
        return true;
    }

}
