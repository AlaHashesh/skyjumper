/*
 * Wall
 *
 * v1.0
 *
 * 2015-08-30
 *
 * Copyright 2015 Ala' Hashesh
 * you may not use this file except in compliance with the author.
 */
package alahashesh.com.skyjumper.game;

import java.util.Random;

/**
 * The Wall is a Class that creates a wall with a hole in it.<br>
 * The hole position is random.
 *
 * @author Ala' Hashesh
 * @version 1.0
 * @since 2015-08-30
 */
public class Wall {

    private int holeXCoordinate;            /* Hole x coordinate*/
    private int previousHoleXCoordinate;    /* Previous hole x coordinate*/
    private int yCoordinate;                /* Hole y coordinate*/
    private int maxX;                       /* Maximum possible x coordinate*/
    private boolean isMoving;               /* Is this a moving hole?*/
    private int unitsToMove;                /* The number of pixels that we can move the hole by*/
    private int direction;                  /* The direction that we can move the hole to*/

    private static final int LEFT = 0;
    private static final int RIGHT = 1;

    //TODO wallSize should be in this class
    //static int wallSize;

    /**
     * @return
     *          Hole x coordinate after moving the hole
     */
    public int getHoleCoordinate() {
        return holeXCoordinate;
    }

    /**
     * Update hole position if it's moving.
     */
    public void updateHole(){
        previousHoleXCoordinate = holeXCoordinate;
        if(isMoving)
            move();
    }

    /**
     * Undo the last hole move.
     */
    public void fixHoleCoordinate() {
        if(isMoving)
            holeXCoordinate = previousHoleXCoordinate;
    }

    /**
     * @return
     *          Hole x coordinate without moving the hole
     */
    public int getPreviousHoleXCoordinate() {
        return previousHoleXCoordinate;
    }

    /**
     * @return
     *          Hole y coordinate
     */
    public int getYCoordinate() {
        return yCoordinate;
    }

    /**
     * Readjusts the vertical position of the wall.<br>
     *
     * @param yCoordinate
     *                      the new y coordinate
     */
    public void setYCoordinate(int yCoordinate) {
        this.yCoordinate = yCoordinate;
    }

    /**
     * Constructs a new wall object.
     *
     * @param width
     *              Width of the screen minus the hole size
     */
    public Wall(int width, int unitsToMove) {
        Random random = new Random();
        holeXCoordinate = random.nextInt(width);
        maxX = width;
        if(unitsToMove > 0) {
            isMoving = true;
            this.unitsToMove = unitsToMove;
            /* Left or right*/
            direction = random.nextInt(2);
        }
    }

    /**
     * @return
     *          The type of the wall (i.e. moving wall or not)
     */
    public boolean isMovingWall(){
        return isMoving;
    }

    /**
     * Move the hole
     */
    private void move(){
        if(direction == RIGHT){
            holeXCoordinate += unitsToMove;
            if(holeXCoordinate > maxX){
                direction = LEFT;
                holeXCoordinate -= unitsToMove;
            }
        } else if (direction == LEFT){
            holeXCoordinate -= unitsToMove;
            if(holeXCoordinate < 0){
                direction = RIGHT;
                holeXCoordinate += unitsToMove;
            }
        }
    }

}
