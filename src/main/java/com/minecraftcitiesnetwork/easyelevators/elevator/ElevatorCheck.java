package com.minecraftcitiesnetwork.easyelevators.elevator;

import com.minecraftcitiesnetwork.easyelevators.Direction;
import java.util.Arrays;

public final class ElevatorCheck {

    public static final int NO_FLOOR = Integer.MIN_VALUE;

    private final ElevatorColumn column;
    private final int[] floors;

    public ElevatorCheck(ElevatorColumn column, int[] floors) {
        this.column = column;
        this.floors = floors;
    }

    public boolean hasTarget(Direction direction) {
        return targetFloorY(direction) != NO_FLOOR;
    }

    public int targetFloorY(Direction direction) {
        int currentY = column.y();
        if (direction == Direction.UP) {
            for (int floorY : floors) {
                if (floorY > currentY) {
                    return floorY;
                }
            }
        } else {
            for (int i = floors.length - 1; i >= 0; i--) {
                int floorY = floors[i];
                if (floorY < currentY) {
                    return floorY;
                }
            }
        }
        return NO_FLOOR;
    }

    public int floorCount() {
        return floors.length;
    }

    public int currentFloorNumber(int feetBlockY) {
        if (floors.length == 0) {
            return 0;
        }
        int index = Arrays.binarySearch(floors, feetBlockY);
        if (index < 0) {
            index = -index - 2;
        }
        return Math.max(1, index + 1);
    }
}
