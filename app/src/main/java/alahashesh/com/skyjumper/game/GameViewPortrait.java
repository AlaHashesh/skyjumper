/*
 * GameViewPortrait
 *
 * v1.0
 *
 * 2015-08-30
 *
 * Copyright 2015 Ala' Hashesh
 * you may not use this file except in compliance with the author.
 */
package alahashesh.com.skyjumper.game;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;

import alahashesh.com.skyjumper.MainActivity;
import alahashesh.com.skyjumper.R;

/**
 * The GameViewPortrait is a View that creates the whole game logic.<br>
 *
 * @author Ala' Hashesh
 * @version 1.0
 * @since 2015-08-30
 */
public class GameViewPortrait extends View {

    /* Directions definition*/
    public static final int DOWN = 0;
    public static final int LEFT = 1;
    public static final int RIGHT = 2;

    /* The number of steps that the hero moves with each frame in dp*/
    private int HERO_SPEED = 7;

    /* The number of steps that the walls move with each frame in dp*/
    private int WALLS_SPEED = 5;

    /* The number of steps that the hole move with each frame in dp*/
    private int HOLES_SPEED = 1;

    /* After how much walls you need a moving one?
    * for example if this is set to 1 then all walls will be moving,
    * if it is set to 2 then half the walls will be moving*/
    private int MOVING_HOLES_TIME = 5;     /*After 4 walls a moving one will come*/

    /* Each wall thickness in dp*/
    private int THICKNESS = 10;

    /*
     * For now this variable is used to calculate the width of the hole (i.e. its size)
     * holeSize = (Screen width) / COLUMNS_NUMBER
     */
    private int COLUMNS_NUMBER = 5;

    /* The distance between walls and can be calculated as follows:
     * ((distance - (size of hole)) / HERO_SPEED ) * WALLS_SPEED + max(THICKNESS, hero height)
     * distance here is the width of the screen.
     * This calculation ensures that the hero is capable of passing the wall
     */
    private int WALLS_MARGIN;

    /*
     * This is the maximum Height that the hero is allowed to get down (i.e. the hero y coordinate)
     * For now it's (screen height)/4
     */
    private int MAX_HEIGHT;

    /* Width for now is the hole size divided by 3.5*/
    private int WIDTH;
    private int HEIGHT = 25;                /*in dp*/

    private int unitsToMove;                /*in dp*/
    private int wallsUnitsToMove;           /*in dp*/
    private int holesUnitsToMove;           /*in dp*/

    private long numberOfGeneratedWalls;

    /*
     * Some actions must be executed at Main Activity
     * and this Listener allows this
     */
    GameViewCallBack mListener;

    /*
     * isReady will be set when the View get initialized
     * (i.e. when the width and height of the screen are known)
     */
    private boolean isReady;

    /*
     * isWallPassed will be true if we successfully passed a wall, else
     * it will be false indicating that the hero hit the wall
     */
    private boolean isWallPassed = true;

    /*
     * This flag indicates whether we lost the game or not.
     * Although we can use isWallPassed to do this, adding this flag
     * make the logic mor clear
     */
    private boolean isLost;

    /*
     * The direction in which the hero is going.
     * It can be either left, right or down
     */
    private int direction;

    /* A list where we store all the walls*/
    private ArrayList<Wall> wallsList;

    /* In what index can we add a new wall?
     * This variable answers this question, moreover using this variable
     * and adding a limit for the number of walls currently visible on the screen
     * then it's possible to make the list circular.
     * (i.e. when a wall reaches the top, it's no longer needed) and we can use its place
     * to store a new wall object
     */
    private int indexInWallList;

    /* Limit for the number of walls allowed on the screen.
     * The calculation for this goes as follows:
     *      Height/WALLS_MARGIN
     */
    private int numberOfWalls;

    /* The index of the top wall that the hero will either pass or hit*/
    private int topWallIndex;

    /* Which way is the hero currently looking?
     * The answer can be either left, right, or down.
     * We can use the direction variable for this, but this adds little clarity.
     */
    private int heroFaceDirection;

    /*
     * A list that holds the hero bitmaps.
     * For now their are three bitmaps, one for each direction.
     */
    private ArrayList<Bitmap> heroBitmaps;

    /*
     * A list that holds the wall bitmaps.
     * For now their are two bitmaps, one for each direction.
     */
    private ArrayList<Bitmap> wallBitmaps;

    private int x;                          /* Hero x coordinate*/
    private int y;                          /* Hero y coordinate*/
    private int holeSize;                   /* Hole size*/
    private int score = 0;                  /* current score*/
    CenterMessage scoreMessage;             /* The score message*/

    /*
     * The paint object used to paint and stylish the hero.
     * for now this is just a normal new paint object.
     */
    private Paint heroPaint;

    /*
     * Each wall can have a unique paint object, but for now
     * this object is used for all the walls
     */
    private Paint wallPaint;

    /**
     * Sets the listener.
     *
     * @param callBack A class that implements GameViewCallBack interface
     */
    public void setCallBack(GameViewCallBack callBack) {
        mListener = callBack;
    }

    /**
     * Returns the current state of the game.
     *
     * @return true if the game is finished, false otherwise
     */
    public boolean isFinished() {
        return isLost;
    }

    /**
     * Sets the direction that the hero is going to.
     *
     * @param direction Hero direction
     */
    public void setDirection(int direction) {
        this.direction = direction;
        if (direction == LEFT) {
            heroFaceDirection = LEFT;
        } else if (direction == RIGHT) {
            heroFaceDirection = RIGHT;
        } else if (direction == DOWN) {
            heroFaceDirection = DOWN;
        }
    }

    /**
     * @return Current score
     */
    public int getScore() {
        return score;
    }

    /**
     * Constructs a new game view using your context.<br>
     *
     * @param context Your context
     * @see android.view.View#View(Context)
     */
    public GameViewPortrait(Context context) {
        super(context);
        init();
    }

    /**
     * Constructs a new game view using your context and your attributes.<br>
     *
     * @param context Your context
     * @param attrs   Your attributes
     * @see android.view.View#View(Context, AttributeSet)
     */
    public GameViewPortrait(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    /**
     * Constructs a new game view using your context, your attributes and your style.<br>
     *
     * @param context      Your context
     * @param attrs        Your attributes
     * @param defStyleAttr Your style resource
     * @see android.view.View#View(Context, AttributeSet, int)
     */
    public GameViewPortrait(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    /**
     * Initializes basic variables .
     */
    private void init() {
        heroPaint = new Paint();
        wallPaint = new Paint();
    }

    /**
     * Draws anything related to the game.
     *
     * @param canvas A canvas to do the drawing on
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (!isReady) {
            if (getWidth() > 0 && getHeight() > 0) {

                /* Initialize everything*/
                initializeVariables();

                /*draw hero*/
                canvas.drawBitmap(heroBitmaps.get(heroFaceDirection), x, y, heroPaint);
            }
        } else {
            if (!isLost) {
                updatePosition();

                /*check if we near the top wall*/
                Wall topWall = wallsList.get(topWallIndex);
                if (isWallPassed &&
                        ((topWall.getYCoordinate() < y + HEIGHT) && topWall.getYCoordinate() + THICKNESS > y)) {
                    /*at this stage the hero is colliding with the top wall*/
                    if (isWallHit(topWall.getYCoordinate(), topWall.getHoleCoordinate())) {
                        isLost = true;
                        isWallPassed = false;
                    }

                } else if (topWall.getYCoordinate() + THICKNESS < y) {
                    /*Wall is passed*/
                    if (isWallPassed) {
                        score++;

                        /* change the theme after some score*/
                        /*if (score == 20) {
                            wallPaint.setColor(Color.WHITE);
                            mListener.changeTheme();
                        }*/

                        /* increase game speed after some score*/
                        if (score == 30 || score == 100) {
                            unitsToMove += convertDpToPixel(1, getContext());
                            wallsUnitsToMove += convertDpToPixel(1, getContext());
                            //unitsToMove++;
                            //wallsUnitsToMove++;
                            //Log.d("SPEED", unitsToMove + "");
                        }
                        scoreMessage.setText(score + "");

                        /* Play score increment sound*/
                        MainActivity.playSound(1);
                    }
                    isWallPassed = true;
                    topWallIndex = (topWallIndex + 1) % numberOfWalls;
                }

                if (isLost) {
                    /* We have lost. Hero must touch the top wall*/
                    undoUpdatePosition();
                    fixHeroPosition(topWall.getPreviousHoleXCoordinate(), topWall.getYCoordinate());

                    /* Wall hit sound*/
                    MainActivity.playSound(2);
                }
            }

            /* Draw hero*/
            canvas.drawBitmap(heroBitmaps.get(heroFaceDirection), x, y, heroPaint);

            /* Draw score*/
            canvas.drawText(scoreMessage.getText(), scoreMessage.getX(),
                    scoreMessage.getY(), scoreMessage.getPaint());

            /* Draw Walls*/
            for (int i = 0; i < wallsList.size(); i++) {
                Wall myWall = wallsList.get(i);
                if (myWall != null) {

                    /* Draw from 0 to hole*/
                    //canvas.drawRect(0, myWall.getYCoordinate(), myWall.getHoleCoordinate(isLost),
                    //        myWall.getYCoordinate() + THICKNESS, wallPaint);

                    /* Draw from hole end to end of screen*/
                    //canvas.drawRect(myWall.getHoleCoordinate(isLost) + holeSize + 1, myWall.getYCoordinate(),
                    //        getWidth(), myWall.getYCoordinate() + THICKNESS, wallPaint);
                    /* Draw from 0 to hole*/
                    canvas.drawBitmap(wallBitmaps.get(0), -1 * (getWidth() - myWall.getHoleCoordinate()), myWall.getYCoordinate(), wallPaint);
                    /* Draw from hole end to end of screen*/
                    canvas.drawBitmap(wallBitmaps.get(1), holeSize + myWall.getHoleCoordinate(), myWall.getYCoordinate(), wallPaint);

                }
            }
        }
    }

    /**
     * Initializes everything.
     */
    private void initializeVariables() {

        /* The score message text size*/
        int scoreTextSize = getResources().getDimensionPixelSize(R.dimen.score_text_size);

        /* The amount of pixels that the hero and the each wall moves.
         * When the game thread asks the game to repaint itself then walls and the hero will move
         * by these amounts
         */
        unitsToMove = convertDpToPixel(HERO_SPEED, getContext());
        wallsUnitsToMove = convertDpToPixel(WALLS_SPEED, getContext());
        holesUnitsToMove = convertDpToPixel(HOLES_SPEED, getContext());
        //THICKNESS = convertDpToPixel(THICKNESS, getContext());
        //HEIGHT = convertDpToPixel(HEIGHT, getContext());
        THICKNESS = getHeight() / 25;
        HEIGHT = (int) (THICKNESS * 1.5);
        holeSize = getWidth() / COLUMNS_NUMBER;
        WIDTH = (int) (holeSize / 3.5);
        WALLS_MARGIN = ((getWidth() - (holeSize)) / unitsToMove) * wallsUnitsToMove + Math.max(THICKNESS, HEIGHT);

        /* Hero x and y coordinates*/
        x = getWidth() / 2 - WIDTH / 2;
        MAX_HEIGHT = getHeight() / 4;
        y = MAX_HEIGHT / 2;

        /* The game score message*/
        scoreMessage = new CenterMessage("0", getWidth(), getHeight());
        scoreMessage.getPaint().setTextSize(scoreTextSize);

        numberOfWalls = getHeight() / (WALLS_MARGIN);
        numberOfWalls += 2;

        /* Initialize the list of the walls and add one wall to it*/
        wallsList = new ArrayList<>(numberOfWalls);
        numberOfGeneratedWalls = 1;
        Wall wall = new Wall(getWidth() - holeSize, numberOfGeneratedWalls % MOVING_HOLES_TIME == 0 ? holesUnitsToMove : 0);
        wall.setYCoordinate(getHeight());
        wallsList.add(wall);
        indexInWallList = 1;

        /* There are no other wall so far*/
        for (int i = 1; i < numberOfWalls; i++) {
            wallsList.add(null);
        }

        /* Load hero bitmaps*/
        heroBitmaps = new ArrayList<>(3);

        Rect heroRect = new Rect();
        heroRect.left = 0;
        heroRect.top = 0;
        heroRect.right = heroRect.left + WIDTH;
        heroRect.bottom = heroRect.top + HEIGHT;

        Bitmap temp = Bitmap.createBitmap(WIDTH, HEIGHT, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(temp);
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.hero_down);
        canvas.drawBitmap(bitmap, null, heroRect, heroPaint);
        heroBitmaps.add(temp);

        temp = Bitmap.createBitmap(WIDTH, HEIGHT, Bitmap.Config.ARGB_8888);
        canvas = new Canvas(temp);
        bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.hero_left);
        canvas.drawBitmap(bitmap, null, heroRect, heroPaint);
        heroBitmaps.add(temp);

        temp = Bitmap.createBitmap(WIDTH, HEIGHT, Bitmap.Config.ARGB_8888);
        canvas = new Canvas(temp);
        bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.hero_right);
        canvas.drawBitmap(bitmap, null, heroRect, heroPaint);
        heroBitmaps.add(temp);
        heroFaceDirection = DOWN;

        /* Load wall bitmaps*/
        wallBitmaps = new ArrayList<>(3);

        Rect wallRect = new Rect();
        wallRect.left = 0;
        wallRect.top = 0;
        wallRect.right = heroRect.left + getWidth();
        wallRect.bottom = heroRect.top + THICKNESS;

        temp = Bitmap.createBitmap(getWidth(), THICKNESS, Bitmap.Config.ARGB_8888);
        canvas = new Canvas(temp);
        bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.wall_left);
        canvas.drawBitmap(bitmap, null, wallRect, wallPaint);
        wallBitmaps.add(temp);

        temp = Bitmap.createBitmap(getWidth(), THICKNESS, Bitmap.Config.ARGB_8888);
        canvas = new Canvas(temp);
        bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.wall_right);
        canvas.drawBitmap(bitmap, null, wallRect, wallPaint);
        wallBitmaps.add(temp);

        /* The game is now ready to be painted*/
        isReady = true;
    }

    /**
     * Checks if the wall hits a visible pixel in the hero bitmap.
     *
     * @param wallY Top wall y coordinate
     * @param holeX Hole top left x coordinate
     * @return true if the hero hit the wall, false otherwise
     */
    private boolean isWallHit(int wallY, int holeX) {

        /* We haven't reached the wall yet*/
        if (y + HEIGHT < wallY)
            return false;

        if (holeX < x && (x + WIDTH) < holeX + holeSize)
            return false;

        /* Get the correct bitmap to check against*/
        Bitmap temp = heroBitmaps.get(heroFaceDirection);

        /* We should check every horizontal pixel for a collision but
         * from what y position in the bitmap should we start the check?
         * For now since the height of the hero is bigger than the thickness then there are two possible states
         * for the bitmap y location relative to the hole.
         * 1- Hero's top pixels above the wall
         * 2- Hero's top pixels are passing the wall.
         * 3- Currently there is no option for when the hero is contained entirely within the hole.
         * (i.e. The thickness of the walls is larger than the hero)
         * However The following code should handle even that case.
         * To avoid checking all pixels we should only check the horizontals pixels rows that moved in the last step.
         */
        int start = HEIGHT - ((y + HEIGHT) - wallY);
        int wallI = 0;
        if (wallY < y) {
            wallI = y - wallY;
            start = 0;
        }

        /* To only check the horizontals pixels rows that moved in the last step
         * the loop should end at start + unitsToMove, because the hero bitmap moved unitsToMove pixels
         * in the last step.
         */
        for (int i = start; i < HEIGHT && wallI < THICKNESS; i++, wallI++) {
            for (int j = 0; j < WIDTH; j++) {

                /*if pixel is visible*/
                if (!isPixelTransparent(temp.getPixel(j, i))) {

                    /*we will hit a wall when a visible pixel hit*/
                    if ((j + x) < holeX || (j + x + WIDTH) > (holeX + holeSize)) {
                        if (x < holeX) {
                            if (!isPixelTransparent(wallBitmaps.get(0).getPixel((getWidth() - holeX) + (j + x), wallI))) {
                                return true;
                            }
                        } else if ((x + WIDTH) > (holeX + holeSize) && (j + x) > (holeX + holeSize)) {
                            if (!isPixelTransparent(wallBitmaps.get(1).getPixel((j + x - (holeX + holeSize)), wallI))) {
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    /**
     * Moves the hero according to its direction.
     */
    private void updatePosition() {

        /* Decrement x coordinate to move to left*/
        if (direction == LEFT) {
            x -= unitsToMove;

            /* Handle left screen edge hit*/
            if (x < 0) {
                x = 0;
            }

            /* Increment x coordinate to move to right*/
        } else if (direction == RIGHT) {
            x += unitsToMove;

            /* Handle left screen edge hit*/
            if (x > getWidth() - WIDTH) {
                x = getWidth() - WIDTH;
            }
        }

        /* Get the bottom wall, if this wall passed the WALLS_MARGIN then we should generate a new wall
         * The bottom wall is behind the wall that we just added.
         */
        int bottomWall = (indexInWallList - 1);

        /* This is done to make the list circular*/
        if (bottomWall < 0)
            bottomWall = numberOfWalls - 1;

        /* Decrement every wall y coordinate to move it upside*/
        for (int i = 0; i < wallsList.size(); i++) {
            Wall myWall = wallsList.get(i);
            if (myWall != null) {
                int yCoordinate = myWall.getYCoordinate();

                /* Is this the bottom wall?*/
                if (i == bottomWall) {

                    /* Did it pass WALLS_MARGIN?*/
                    if (yCoordinate <= getHeight() - WALLS_MARGIN) {

                        /* Generate a new wall*/
                        numberOfGeneratedWalls++;
                        Wall newWall = new Wall(getWidth() - holeSize, numberOfGeneratedWalls % MOVING_HOLES_TIME == 0 ? holesUnitsToMove : 0);
                        newWall.setYCoordinate(getHeight());
                        wallsList.set(indexInWallList++, newWall);
                        indexInWallList = indexInWallList % numberOfWalls;
                    }
                }

                yCoordinate -= wallsUnitsToMove;

                /* If a wall reaches the top we should remove it*/
                if (yCoordinate + THICKNESS <= 0) {
                    wallsList.set(i, null);
                } else {
                    myWall.setYCoordinate(yCoordinate);
                    myWall.updateHole();
                }
            }
        }
    }

    /**
     * Undo changes from last step.
     */
    private void undoUpdatePosition() {
        if (direction == LEFT && x != 0) {
            x += unitsToMove;

        } else if (direction == RIGHT && x != getWidth() - WIDTH) {
            x -= unitsToMove;
        }
        for (int i = 0; i < wallsList.size(); i++) {
            if (wallsList.get(i) != null) {
                wallsList.get(i).fixHoleCoordinate();
            }
        }
        y -= wallsUnitsToMove;
    }

    /**
     * Checks if a pixel transparent.
     *
     * @param x Pixel's color
     * @return true if the pixel is transparent, false otherwise
     */
    private boolean isPixelTransparent(int x) {
        return ((x & 0xff000000) >> 24) == 0;
    }


    /**
     * Checks the transparency of the x wall pixel.
     * For more complex pixels then this implementation needs change.
     * This is planned for future updates.
     *
     * @param x     Pixel x coordinate with respective to the wall
     * @param holeX Hole x coordinate
     * @return true if this pixel is transparent, false otherwise
     */
    private boolean getWallPixel(int x, int holeX) {
        // TODO add some logic here
        return !(x < holeX || x > holeX + holeSize);
    }

    /**
     * Fix the hero bitmap so that it doesn't overlap with the wall.<br>
     * The bitmap should only hit the wall.
     *
     * @param topWallHoleXCoordinate Top wall hole x coordinate
     * @param topWallYCoordinate     Top wall y coordinate
     */
    private void fixHeroPosition(int topWallHoleXCoordinate, int topWallYCoordinate) {
        /*hero is not in the hole*/
        if (x > (topWallHoleXCoordinate + holeSize) || x + WIDTH < topWallHoleXCoordinate
                || x + unitsToMove > (topWallHoleXCoordinate + holeSize) ||
                x + WIDTH - unitsToMove < topWallHoleXCoordinate) {
            /*make hero touch wall*/
            if (topWallYCoordinate > y) {
                while (!isWallHit(topWallYCoordinate, topWallHoleXCoordinate)) {
                    y++;
                }
            }
        } else {

            /*hero is partially or fully in the hole attempt to move the hero until it hits*/
            if (direction == DOWN) {
                x += unitsToMove;
                if (isWallHit(topWallYCoordinate, topWallHoleXCoordinate)) {
                    x -= unitsToMove;
                    x -= wallsUnitsToMove;
                    while (!isWallHit(topWallYCoordinate, topWallHoleXCoordinate)) {
                        x++;
                    }
                } else {
                    x -= unitsToMove;
                    x -= unitsToMove;
                    if (isWallHit(topWallYCoordinate, topWallHoleXCoordinate)) {
                        x += unitsToMove;
                        x += wallsUnitsToMove;
                        while (!isWallHit(topWallYCoordinate, topWallHoleXCoordinate)) {
                            x--;
                        }
                    } else {
                        x += unitsToMove;
                        while (!isWallHit(topWallYCoordinate, topWallHoleXCoordinate)) {
                            y++;
                        }
                    }
                }
            } else if (direction == RIGHT) {
                int xMovement = unitsToMove / wallsUnitsToMove;
                while (!isWallHit(topWallYCoordinate, topWallHoleXCoordinate)) {
                    x += xMovement;
                }
            } else if (direction == LEFT) {
                int xMovement = unitsToMove / wallsUnitsToMove;
                while (!isWallHit(topWallYCoordinate, topWallHoleXCoordinate)) {
                    x -= xMovement;
                }
            }
        }
    }

    /**
     * Converts from dp to pixels.<br>
     * Code borrowed from: <br>
     * http://stackoverflow.com/a/9563438<br>
     * Many thanks to both Muhammad Nabeel Arif and Steven Byle
     *
     * @param dp      Units in dp
     * @param context Application context
     * @return Units in pixels
     */
    private int convertDpToPixel(float dp, Context context) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return (int) ((dp * displayMetrics.density) + 0.5);
    }

    /**
     * Restarts the game.
     */
    public void restartGame() {
        isLost = false;
        score = 0;
        scoreMessage.setText("0");
        for (int i = 0; i < wallsList.size(); i++) {
            wallsList.set(i, null);
        }
        numberOfGeneratedWalls = 1;
        Wall wall = new Wall(getWidth() - holeSize, numberOfGeneratedWalls % MOVING_HOLES_TIME == 0 ? holesUnitsToMove : 0);
        wall.setYCoordinate(getHeight());
        wallsList.set(0, wall);
        indexInWallList = 1;
        topWallIndex = 0;
        isWallPassed = true;
        x = getWidth() / 2 - WIDTH / 2;
        y = MAX_HEIGHT / 2;
        direction = DOWN;
        heroFaceDirection = DOWN;
        unitsToMove = convertDpToPixel(HERO_SPEED, getContext());
        wallsUnitsToMove = convertDpToPixel(WALLS_SPEED, getContext());
        holesUnitsToMove = convertDpToPixel(HOLES_SPEED, getContext());
    }

    /**
     * The GameViewCallBack is an Interface that the Main Activity must implements.<br>
     * This interface gives the ability to execute some tasks in the Main Activity.
     *
     * @author Ala' Hashesh
     * @version 1.0
     * @since 2015-08-30
     */
    public interface GameViewCallBack {
        /**
         * Changes the game view current theme. Currently not used.
         */
        void changeTheme();
    }
}
