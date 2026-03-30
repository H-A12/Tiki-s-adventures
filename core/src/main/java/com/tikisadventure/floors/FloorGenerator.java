package com.tikisadventure.floors;

import com.badlogic.gdx.math.MathUtils;

public class FloorGenerator {

    public enum RoomShape {
        CROSS,
        SQUARE_WITH_HOLES,
        L_CORNERS
    }

    private static final int SIZE = 32;
    private static final int WALL_TILE = 1;
    private static final int FLOOR_TILE = 0;

    private int[][] currentLayout;

    public FloorGenerator() {}

    public int[][] generateLayout(RoomShape shape) {
        currentLayout = new int[SIZE][SIZE];
        
        switch (shape) {
            case CROSS:
                generateCrossLayout(currentLayout);
                break;
            case SQUARE_WITH_HOLES:
                generateSquareWithHolesLayout(currentLayout);
                break;
            case L_CORNERS:
                generateLCornersLayout(currentLayout);
                break;
        }
        
        return currentLayout;
    }

    public int[][] getCurrentLayout() {
        return currentLayout;
    }

    private void generateCrossLayout(int[][] layout) {
        for (int y = 0; y < SIZE; y++) {
            for (int x = 0; x < SIZE; x++) {
                boolean isWall = false;
                
                int borderWidth = 2;
                if (x < borderWidth || x >= SIZE - borderWidth || 
                    y < borderWidth || y >= SIZE - borderWidth) {
                    isWall = true;
                }
                
                int crossWidth = 8;
                int centerStart = (SIZE - crossWidth) / 2;
                int centerEnd = centerStart + crossWidth;
                if ((x >= centerStart && x < centerEnd) || 
                    (y >= centerStart && y < centerEnd)) {
                    isWall = false;
                }
                
                layout[y][x] = isWall ? WALL_TILE : FLOOR_TILE;
            }
        }
    }

    private void generateSquareWithHolesLayout(int[][] layout) {
        for (int y = 0; y < SIZE; y++) {
            for (int x = 0; x < SIZE; x++) {
                boolean isWall = false;
                
                int borderWidth = 2;
                if (x < borderWidth || x >= SIZE - borderWidth || 
                    y < borderWidth || y >= SIZE - borderWidth) {
                    isWall = true;
                }
                
                int holeSize = 6;
                int holeStart = (SIZE - holeSize) / 2;
                int holeEnd = holeStart + holeSize;
                
                if (x >= 12 && x < 14 && y >= holeStart && y < holeEnd) {
                    isWall = false;
                }
                if (x >= holeStart && x < holeEnd && y >= 12 && y < 14) {
                    isWall = false;
                }
                
                layout[y][x] = isWall ? WALL_TILE : FLOOR_TILE;
            }
        }
    }

    private void generateLCornersLayout(int[][] layout) {
        for (int y = 0; y < SIZE; y++) {
            for (int x = 0; x < SIZE; x++) {
                boolean isWall = false;
                
                int borderWidth = 2;
                if (x < borderWidth || x >= SIZE - borderWidth || 
                    y < borderWidth || y >= SIZE - borderWidth) {
                    isWall = true;
                }
                
                int gapStart = 10;
                int gapEnd = 22;
                
                if (x >= gapStart && x < gapEnd && y >= gapStart && y < gapEnd) {
                    isWall = false;
                }
                
                layout[y][x] = isWall ? WALL_TILE : FLOOR_TILE;
            }
        }
    }

    public RoomShape getRandomShape() {
        RoomShape[] shapes = RoomShape.values();
        return shapes[MathUtils.random(shapes.length - 1)];
    }

    public int[] getDoorPosition() {
        return new int[]{SIZE / 2, 4};
    }

    public int getFloorSize() {
        return SIZE;
    }

    public boolean isWallTile(int tileX, int tileY) {
        if (tileX < 0 || tileX >= SIZE || tileY < 0 || tileY >= SIZE) {
            return true;
        }
        if (currentLayout == null) return false;
        return currentLayout[tileY][tileX] == WALL_TILE;
    }

    public boolean isWallWorld(float worldX, float worldY) {
        int tileX = (int)(worldX);
        int tileY = (int)(worldY);
        return isWallTile(tileX, tileY);
    }
}
