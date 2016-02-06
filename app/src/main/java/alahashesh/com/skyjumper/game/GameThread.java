/*
 * GameThread
 *
 * v1.0
 *
 * 2015-08-30
 *
 * Copyright 2015 Ala' Hashesh
 * you may not use this file except in compliance with the author.
 */
package alahashesh.com.skyjumper.game;

import android.util.Log;

/**
 * The GameThread is a Thread that makes the game view repaint itself.<br>
 * Right now the repainting is done every 17 milliseconds, which should run
 * the game at 60 FPS.
 *
 * @author Ala' Hashesh
 * @version 1.0
 * @since 2015-08-30
 */
public class GameThread extends Thread {

    /* Some calls need to be send back to the main Activity*/
    GameThreadCallBack mCallbacks;

    /* A flag that must be true for the thread to run*/
    private boolean isRunning = true;

    /**
     * Stops the thread.
     */
    public void stopGameThread() {
        isRunning = false;
    }

    public GameThread(GameThreadCallBack mCallbacks) {
        this.mCallbacks = mCallbacks;
    }

    @Override
    public void run() {
        while (isRunning) {
            try {
                sleep(17);
            } catch (InterruptedException e) {
                Log.d("GAMETHREAD", "Error while sleeping");
            }
            if (!mCallbacks.update()) {
                break;
            }
        }
    }

    /**
     * The GameThreadCallBack is an Interface that the Main Activity must implements.<br>
     * This interface gives the ability to execute some tasks in the Main Activity.
     *
     * @author Ala' Hashesh
     * @version 1.0
     * @since 2015-08-30
     */
    public interface GameThreadCallBack {

        /**
         * Make the game updates it self.
         * @return
         *          true if the player hasn't lost yet, false otherwise.
         */
        boolean update();
    }
}
