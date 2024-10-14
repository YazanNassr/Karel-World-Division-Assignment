import stanford.karel.SuperKarel;

enum Direction {
    LEFT, RIGHT, BACKWARDS, FORWARDS;
    public Direction opposite() {
        return switch (this) {
            case LEFT -> RIGHT;
            case RIGHT -> LEFT;
            case FORWARDS -> BACKWARDS;
            case BACKWARDS -> FORWARDS;
        };
    }
}

abstract class SuperDuperKarel extends SuperKarel {
    protected int numOfMoves, usedBeepers;

    public SuperDuperKarel()           { setBeepersInBag(1000);             }
    protected void resetStatistics()   { numOfMoves = usedBeepers = 0;      }
    @Override public void move()       { numOfMoves++;  super.move();       }
    @Override public void putBeeper()  { usedBeepers++; super.putBeeper();  }
    @Override public void pickBeeper() { usedBeepers--; super.pickBeeper(); }

    public void putBeeperIfNecessary() {
        if (noBeepersPresent())
            putBeeper();
    }

    public void moveIfYouCan() {
        if (frontIsClear())
            move();
    }

    public int moveTillWall() {
        int numOfMovesBeforeStart = numOfMoves;
        while (frontIsClear()) {
            move();
        }
        return numOfMoves - numOfMovesBeforeStart;
    }

    public void move(int moves, boolean putBeepersOnTheWay) {
        for (int i = 0; i < moves; ++i) {
            if (putBeepersOnTheWay) {
                putBeeperIfNecessary();
            }
            moveIfYouCan();
        }

        if (putBeepersOnTheWay) {
            putBeeperIfNecessary();
        }
    }

    public void turnToDirection(Direction direction) {
        switch (direction) {
            case LEFT -> turnLeft();
            case RIGHT -> turnRight();
            case BACKWARDS -> turnAround();
        }
    }

    public void move(Direction direction, int moves, boolean putBeepersOnTheWay) {
        turnToDirection(direction);
        move(moves, putBeepersOnTheWay);
    }

    public void moveDiagonally(Direction direction) {
        move(direction, 1, false);
        move(direction.opposite(), 1, false);
    }
}

public class Homework extends SuperDuperKarel {
    private final Direction forwards = Direction.FORWARDS;
    private final Direction backwards = Direction.BACKWARDS;
    private Direction left = Direction.LEFT;
    private Direction right = Direction.RIGHT;
    private int streets, avenues;
    private int halfAvenues, halfStreets;
    private boolean oddNumOfStreets, oddNumOfAvenues;

    protected void resetKarel() {
        resetStatistics();
        left = Direction.LEFT;
        right = Direction.RIGHT;
    }

    public void goToStartingPosition() {
        while (notFacingWest()) { turnLeft(); }
        moveTillWall();
        turnLeft();
        moveTillWall();
        turnLeft();
    }

    public void calculateAttributes() {
        oddNumOfAvenues = avenues%2 == 1;
        oddNumOfStreets = streets%2 == 1;
        halfAvenues = avenues/2;
        halfStreets = streets/2;
    }

    private void swap_directions() {
        var tmp = left; left = right; right = tmp;
    }

    private void swap_dimensions() {
        var tmp = streets; streets = avenues; avenues = tmp;
        calculateAttributes();
    }

    public void run() {
        resetKarel();
        findDimensionsOfWorld();
        calculateAttributes();

        if (streets < avenues)
            standardizeProblem();

        if (isSpecialWorld()) {
            handleSpecialWorlds();
        } else if (costOfPerpendicularCuts() <= costOfParallelCuts()) {
            solveUsingPerpendicularCuts();
        } else {
            solveUsingParallelCuts();
        }

        goToStartingPosition();

        System.out.printf("The number of used Beepers is %d\n", usedBeepers);
        System.out.printf("The number of moves made is %d\n", numOfMoves);
    }

    private void standardizeProblem() {
        swap_dimensions();
        turnToDirection(right);
        swap_directions();
    }

    private void solveUsingParallelCuts() {
        int roomSize = (streets*avenues - costOfParallelBeepers()) / 4;
        int taken = (streets*avenues - 4 * roomSize - 3 * Math.min(streets, avenues));

        turnToDirection(left);

        Direction dir = left;

        for (int i = 0, rows = taken/avenues; taken > 0 && i <= rows; ++i, dir = dir.opposite()) {
            if (avenues <= taken) {
                move(avenues - 1, true);
                taken -= avenues;
            } else {
                move(taken - 1, true);
                move(avenues - taken, false);
            }
            move(dir, (beepersPresent() ? 1 : 0), false);
            turnToDirection(dir);
        }

        for (int i = 0; i < 3; ++i) {
            int shifted = (roomSize+taken)%avenues;

            move(dir, (roomSize+taken) / avenues, false);
            move(dir.opposite(), avenues - 1 - (shifted), true);
            moveDiagonally(dir);
            if (shifted > 0) {
                move(forwards, shifted-1, true);
            }
            turnAround();
            taken = shifted;
            dir = dir.opposite();
        }
    }

    private void solveUsingPerpendicularCuts() {
        if (oddNumOfStreets && oddNumOfAvenues) {
            move(left, halfAvenues, false);
            move(left, streets-1, true);
            move(left, halfAvenues, false);
            move(left, halfStreets, false);
            move(left, avenues-1, true);
        } else if (oddNumOfStreets != oddNumOfAvenues) {
            if (oddNumOfAvenues) standardizeProblem();

            move(backwards, halfStreets, false);
            move(right, avenues-1, true);
            move(right, halfStreets, false);
            move(right, halfAvenues-1, false);
            move(right, (halfStreets+1)/2 -1, true);
            move(left, 1, false);
            move(right, 1, halfStreets%2 == 1);
            move(forwards, halfStreets, true);
            move(right, 1, false);
            move(left, 1, halfStreets%2 == 1);
            move(forwards, halfStreets/2-1, true);
        } else {
            int area1 = halfStreets * (halfAvenues - 1);
            int area2 = halfAvenues * (halfStreets - 1);
            int toDel = (area2 - area1) / 2;

            move(backwards, halfStreets-1, false);
            move(right, halfAvenues-1, true);
            moveDiagonally(left);
            move(forwards, halfAvenues-1, true);
            move(right, halfStreets, false);

            if (0 == ((area1&1)^(area2&1)))
                move(0,true);

            move(right, halfAvenues-1, false);
            move(right, halfStreets - 2 - toDel, true);
            moveDiagonally(left);
            move(forwards, toDel, true);

            move();
            if (0 == ((area1&1)^(area2&1)))
                move(0,true);

            if (toDel > 0) {
                moveDiagonally(right);
                move(forwards, toDel - 1, true);
                moveDiagonally(left);
            } else {
                move();
            }
            move(forwards, halfStreets - 2 - toDel, true);
        }
    }

    private int costOfParallelCuts() {
        return 1000*costOfParallelBeepers() + 10 * costOfParallelMoves();
    }

    private int costOfPerpendicularCuts() {
        return 1000*costOfPerpendicularBeepers() + 10*costOfPerpendicularMoves();
    }

    private int costOfParallelMoves() {
        return 2 * (streets-1) + 4 * (avenues-1) + 2 * ((streets*avenues-3*avenues)%4 > 0 ? avenues-1 : 0);
    }

    private int costOfPerpendicularMoves() {
        return 3*(streets+avenues-2) + 1-(streets%2) + 1-avenues%2;
    }

    private int costOfParallelBeepers() {
        if (Math.max(streets, avenues) < 7)
            return streets * avenues; // infinity

        int cuttingBeepers = 3 * Math.min(streets, avenues);
        return cuttingBeepers + (streets*avenues - cuttingBeepers) % 4;
    }

    private int costOfPerpendicularBeepers() {
        if (Math.min(streets, avenues) < 2)
            return streets * avenues; // infinity

        if (oddNumOfStreets && oddNumOfAvenues) {
            return streets + avenues - 1;
        } else if (oddNumOfStreets || oddNumOfAvenues) {
            int theOddOne = (oddNumOfAvenues ? avenues : streets);
            return streets + avenues + -1 + (theOddOne/2 % 2 == 0 ? 0 : 2); // -1 always - from intersection with the first cut, +2 when it's not divisible
        } else {
            return avenues + streets + -2 + (streets/2 * avenues - avenues/2 - streets/2 - 1) % 2 * 2;
        }
    }

    private void findDimensionsOfWorld() {
        avenues = moveTillWall() + 1;
        turnToDirection(left);
        streets = moveTillWall() + 1;
    }

    private boolean isSpecialWorld() {
        return Math.min(streets, avenues) < 3 && Math.max(streets, avenues) < 7;
    }

    private void handleSpecialWorlds() {
        turnToDirection(backwards);
        if (avenues == 1) {
            switch (streets) {
                case 6:
                    putBeeperIfNecessary();
                    move();
                case 5:
                    move();
                case 4:
                    putBeeperIfNecessary();
                    move();
                case 3:
                    move();
                    putBeeperIfNecessary();
            }
        } else if (avenues == 2) {
            int idx = streets;
            Direction dir = right;
            for (; idx >= 5; --idx, dir = dir.opposite()) {
                move(dir, 1, true);
                move(dir.opposite(), 1, true);
            }
            for (; idx > 1; --idx, dir = dir.opposite()) {
                putBeeperIfNecessary();
                moveDiagonally(dir);
            }
            putBeeperIfNecessary();
        }
    }
}