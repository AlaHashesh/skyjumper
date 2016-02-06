/*
 * MusicService
 *
 * v1.0
 *
 * 2015-08-30
 *
 * Copyright 2015 Ala' Hashesh
 * you may not use this file except in compliance with the author.
 */
package alahashesh.com.skyjumper;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnErrorListener;
import android.os.Binder;
import android.os.IBinder;
import android.widget.Toast;

/**
 * The MusicService is a Service that plays a background music.
 * Currently this service is disabled and intended for future updates.
 *
 * @author  Ala' Hashesh
 * @version 1.0
 * @since   2015-08-30
 */
public class MusicService extends Service  implements MediaPlayer.OnErrorListener {

    private final IBinder mBinder = new ServiceBinder();
    MediaPlayer mPlayer;
    private int length = 0;

    public MusicService() {
    }

    public class ServiceBinder extends Binder {
        MusicService getService() {
            return MusicService.this;
        }
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return mBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        /*open music file here*/
        //mPlayer = MediaPlayer.create(this, R.raw.background_music);
        mPlayer.setOnErrorListener(this);

        if (mPlayer != null) {
            mPlayer.setLooping(true);
            mPlayer.setVolume(100, 100);
        }

        mPlayer.setOnErrorListener(new OnErrorListener() {

            public boolean onError(MediaPlayer mp, int what, int
                    extra) {

                this.onError(mPlayer, what, extra);
                return true;
            }
        });
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mPlayer.start();
        return START_STICKY;
    }

    /**
     * Pauses the music.
     */
    public void pauseMusic() {
        if (mPlayer.isPlaying()) {
            mPlayer.pause();
            length = mPlayer.getCurrentPosition();

        }
    }

    /**
     * Resumes the music.
     */
    public void resumeMusic() {
        if (!mPlayer.isPlaying()) {
            mPlayer.seekTo(length);
            mPlayer.start();
        }
    }

    /**
     * Stops the music.
     */
    public void stopMusic() {
        mPlayer.stop();
        mPlayer.reset();
        mPlayer.release();
        mPlayer = null;
    }

    @Override
    public void onDestroy() {

        /* Before destroying we must stop and release the player*/
        if (mPlayer != null) {
            try {
                mPlayer.stop();
                mPlayer.reset();
                mPlayer.release();
            } finally {
                mPlayer = null;
            }
        }
        super.onDestroy();
    }

    /**
     * Handles any error.
     */
    public boolean onError(MediaPlayer mp, int what, int extra) {

        Toast.makeText(this, "music player failed", Toast.LENGTH_SHORT).show();
        if (mPlayer != null) {
            try {
                mPlayer.stop();
                mPlayer.release();
            } finally {
                mPlayer = null;
            }
        }
        return false;
    }
}