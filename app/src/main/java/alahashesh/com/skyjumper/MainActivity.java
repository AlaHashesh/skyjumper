/*
 * MainActivity
 *
 * v1.0
 *
 * 2015-08-30
 *
 * Copyright 2015 Ala' Hashesh
 * you may not use this file except in compliance with the author.
 */
package alahashesh.com.skyjumper;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.v4.content.ContextCompat;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.util.HashMap;

import alahashesh.com.skyjumper.game.GameThread;
import alahashesh.com.skyjumper.game.GameViewPortrait;

/**
 * The MainActivity is an activity that is responsible for driving the whole application.
 *
 * @author Ala' Hashesh
 * @version 1.0
 * @since 2015-08-30
 */
public class MainActivity extends Activity implements GameThread.GameThreadCallBack,
        GameViewPortrait.GameViewCallBack, ServiceConnection {

    /* indicates whether the game is mute or not.*/
    public static boolean isMute;

    /* indicates whether the activity is linked to service player.*/
    private boolean mIsBound = false;

    /* Saves the service instance.*/
    private MusicService mServ;

    /* Sound pool used to play short sounds when increasing the score and when dying*/
    private static SoundPool soundPool;
    private static HashMap<Integer, Integer> soundPoolMap;

    /* The game background image*/
    static ImageView mBackgroundImageView;

    /* The start screen, which is displayed when the game starts*/
    RelativeLayout startScreen;
    Button mStartButton;

    /* The pause screen, which is displayed when the game pauses*/
    RelativeLayout pauseScreen;

    /* The game view. In a word this is the game*/
    GameViewPortrait gameViewPortrait;

    /* The thread that runs the game*/
    GameThread thread;

    /* The finish screen, which is displayed when the player loses*/
    LinearLayout finishScreen;
    AdView mAdView;                 /* Google ads*/
    TextView mScoreTextView;        /* Score*/
    TextView mMaxScoreTextView;     /* Max score*/
    TextView muteIcon;              /* Mute Icon (i.e. button)*/
    Button mRestartButton;          /* Restarts the game*/

    /* A flag that indicates that the game is running*/
    public static boolean isRunning = false;

    /* A flag that indicates that the game is started*/
    private boolean isStarted = false;

    /* Request the Scoreboard Activity to sync the score*/
    public static final int SYNC_REQUEST = 100;

    /* Is the game visible to the user now?*/
    private boolean isCreated = false;

    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        /* Initializing mute icon*/
        muteIcon = (TextView)findViewById(R.id.mute_icon);
        SharedPreferences sharedPreferences =
                getSharedPreferences("GAME_PREF", Context.MODE_PRIVATE);
        isMute = sharedPreferences.getBoolean("MUTE", false);
        if(isMute){
            if(android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                muteIcon.setBackgroundDrawable(ContextCompat.getDrawable(MainActivity.this, R.drawable.mute));
            } else {
                muteIcon.setBackground(ContextCompat.getDrawable(MainActivity.this, R.drawable.mute));
            }
        }

        muteIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isMute = !isMute;
                if(isMute){
                    if(android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                        muteIcon.setBackgroundDrawable(ContextCompat.getDrawable(MainActivity.this, R.drawable.mute));
                    } else {
                        muteIcon.setBackground(ContextCompat.getDrawable(MainActivity.this, R.drawable.mute));
                    }
                } else {
                    if(android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                        muteIcon.setBackgroundDrawable(ContextCompat.getDrawable(MainActivity.this, R.drawable.sound));
                    } else {
                        muteIcon.setBackground(ContextCompat.getDrawable(MainActivity.this, R.drawable.sound));
                    }
                }
            }
        });

        initSounds(getApplicationContext());

        /* Starting the service of the player, if not already started.
         * For now the music is intended for future updates
         */
        //Intent music = new Intent(this, MusicService.class);
        //startService(music);
        //doBindService();

        mBackgroundImageView = (ImageView) findViewById(R.id.iv_background_image);

        /* The start screen*/
        startScreen = (RelativeLayout) findViewById(R.id.start_dialog_container);
        mStartButton = (Button) findViewById(R.id.btn_play);

        /* After clicking play the game should start*/
        mStartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                /* Hide the start screen*/
                startScreen.setVisibility(View.INVISIBLE);

                /* Stop any running instances of the game*/
                if (thread != null) {
                    thread.stopGameThread();
                    thread = null;
                }

                /* Run the game*/
                thread = new GameThread(MainActivity.this);
                thread.start();

                /* The game is started now*/
                isStarted = true;

                /* The game is running now*/
                isRunning = true;


            }
        });

        /* The pause screen*/
        pauseScreen = (RelativeLayout) findViewById(R.id.pause_dialog_container);

        /* The pause screen should hide when the user touch it*/
        pauseScreen.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {

                    /* Hide pause screen*/
                    pauseScreen.setVisibility(View.INVISIBLE);

                    /* Stop any running instances of the game*/
                    if (thread != null) {
                        thread.stopGameThread();
                        thread = null;
                    }

                    thread = new GameThread(MainActivity.this);
                    thread.start();

                    /* The game is now running*/
                    isRunning = true;

                }
                if (event.getAction() == MotionEvent.ACTION_DOWN ||
                        event.getAction() == MotionEvent.ACTION_UP) {

                    /* now we must send the touch event to the main screen
                     * Code borrowed from
                     * http://stackoverflow.com/a/7001356
                     * Many thanks to azdev
                     */

                    /* Obtain MotionEvent object*/
                    long downTime = SystemClock.uptimeMillis();
                    long eventTime = SystemClock.uptimeMillis() + 100;
                    float x = event.getX();
                    float y = event.getY();
                    /* List of meta states found here:
                     * developer.android.com/reference/android/view/KeyEvent.html#getMetaState()
                     */

                    int metaState = 0;
                    MotionEvent motionEvent = MotionEvent.obtain(
                            downTime,
                            eventTime,
                            event.getAction(),
                            x,
                            y,
                            metaState
                    );

                    /* Dispatch touch event to the game view*/
                    gameViewPortrait.dispatchTouchEvent(motionEvent);
                }
                return true;
            }
        });

        /* The finish screen*/
        finishScreen = (LinearLayout) findViewById(R.id.finish_dialog_container);
        mAdView = (AdView) findViewById(R.id.adView);
        mRestartButton = (Button) findViewById(R.id.btn_restart);

        /* Restart the game when this button is clicked*/
        mRestartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                restartGame();
            }
        });

        mScoreTextView = (TextView) findViewById(R.id.tv_finish_score);
        mMaxScoreTextView = (TextView) findViewById(R.id.tv_finish_max_score);

        gameViewPortrait = (GameViewPortrait) findViewById(R.id.game_view);

        /* Some calls need to be executed in the Main Activity, therefore this callback is set*/
        gameViewPortrait.setCallBack(this);

        /* Send commands to the game according to the touch position*/
        gameViewPortrait.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                /* Only the horizontal touch point is required*/
                float x = event.getX();

                /* Sends commands to the game if and only if it's running and not finished*/
                boolean validFlag = isRunning && !gameViewPortrait.isFinished();

                /* If x is horizontally greater that half of screen then we need to move the hero to
                 * the right, else we must move it to the left.
                 */
                switch (event.getAction()) {

                    /* Finger touched the screen*/
                    case MotionEvent.ACTION_DOWN:
                        if (validFlag) {
                            if (x > v.getWidth() / 2) {
                                gameViewPortrait.setDirection(GameViewPortrait.RIGHT);
                            } else {
                                gameViewPortrait.setDirection(GameViewPortrait.LEFT);
                            }
                        }
                        break;

                    /* Finger left the screen*/
                    case MotionEvent.ACTION_UP:
                        if (validFlag) {
                            gameViewPortrait.setDirection(GameViewPortrait.DOWN);
                        }
                        break;

                    /* Finger is moving on the screen*/
                    case MotionEvent.ACTION_MOVE:
                        if (validFlag) {
                            if (x > v.getWidth() / 2) {
                                gameViewPortrait.setDirection(GameViewPortrait.RIGHT);
                            } else {
                                gameViewPortrait.setDirection(GameViewPortrait.LEFT);
                            }
                        }
                        break;
                }
                return true;
            }
        });
    }

    /* Detect first run*/
    @Override
    protected void onStart() {
        super.onStart();
        if(!isCreated){
            isCreated = true;
            /* Reload Google ads*/
            AdRequest adRequest = new AdRequest.Builder().build();
            mAdView.loadAd(adRequest);
        }
    }

    /* Avoid destroying the game when pressing the back button*/
    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        moveTaskToBack(true);
    }

    @Override
    protected void onPause() {
        super.onPause();

        /* When pausing we must stop the, stop the music and display the pause screen*/
        isRunning = false;
        if (mServ != null) {
            mServ.pauseMusic();
        }
        if (thread != null) {
            thread.stopGameThread();
        }

        /* Display the pause screen if and only if the game is started and not finished*/
        if (isStarted && !gameViewPortrait.isFinished()) {
            pauseScreen.setVisibility(View.VISIBLE);
        }
        //pauseTime = System.currentTimeMillis();
    }

    @Override
    protected void onResume() {
        super.onResume();

        /* If a system call causes the game to pause then restart it*/
        /*if(System.currentTimeMillis() - pauseTime < 300){
            if(thread != null){
                thread.stopGameThread();
            }
            pauseScreen.setVisibility(View.INVISIBLE);
            *//* Run the game*//*
            thread = new GameThread(MainActivity.this);
            thread.start();
            isRunning = true;
        }*/

        /* When Resuming we must restart the music.
         * Restarting the game is handled by the pause screen
         */
        if (mServ != null) {
            mServ.resumeMusic();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        SharedPreferences sharedPreferences =
                getSharedPreferences("GAME_PREF", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("MUTE", isMute);
        editor.apply();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        /* When the game finishes we must stop the music service*/
        if (mServ != null) {
            mServ.stopMusic();
            mServ.stopSelf();
        }
        doUnbindService();
    }

    @Override
    public boolean update() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                gameViewPortrait.invalidate();
            }
        });
        if (!gameViewPortrait.isFinished()) {
            return true;
        } else {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    finishGame();
                }
            });
            return false;
        }
    }

    /**
     * Finishes the game.
     */
    public void finishGame() {

        /* Show finish screen*/
        finishScreen.setVisibility(View.VISIBLE);

        /* Get score and update the max score if necessary*/
        int score = gameViewPortrait.getScore();
        SharedPreferences sharedPreferences =
                getSharedPreferences("GAME_PREF", Context.MODE_PRIVATE);
        int maxScore = sharedPreferences.getInt("MAX_SCORE", 0);
        if (score > maxScore) {
            maxScore = score;
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt("MAX_SCORE", maxScore);
            editor.apply();
        }

        /* Update the UI*/
        mScoreTextView.setText(getString(R.string.integer_place_holder, score));
        mMaxScoreTextView.setText(getString(R.string.integer_place_holder, maxScore));

        /* Reload Google ads*/
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
    }

    /**
     * Restarts the game.
     */
    public void restartGame() {

        /* Restart the game*/
        gameViewPortrait.restartGame();

        /* Restart the thread*/
        if (thread != null) {
            thread.stopGameThread();
            thread = null;
        }
        thread = new GameThread(this);
        thread.start();

        /*Pause Google ads*/
        mAdView.pause();

        /* Hide finish screen*/
        finishScreen.setVisibility(View.INVISIBLE);

        /* The game is now running*/
        isRunning = true;
    }

    /**
     * Initializes SoundPool and any used short sound tracks.<br>
     * Code borrowed from:<br>
     * https://dzoneq.com/articles/playing-sounds-android<br>
     * http://stackoverflow.com/a/27552576<br>
     * Many thanks to Tony Siciliani and user3833732
     *
     * @param context Game context
     */
    @SuppressWarnings("deprecation")
    private static void initSounds(Context context) {
        /* Construct a SoundPool with a maximum number of simultaneous streams of 2,
         * and of type STREAM_MUSIC, which is what game applications would use.
         * The last argument to the constructor is supposed to be for the sample-rate converter quality,
         * but seems to have no noticeable effect.
         */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            AudioAttributes attributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build();
            soundPool = new SoundPool.Builder()
                    .setAudioAttributes(attributes)
                    .build();
        } else {
            soundPool = new SoundPool(2, AudioManager.STREAM_MUSIC, 0);
        }
        soundPoolMap = new HashMap<>(3);
        soundPoolMap.put(1, soundPool.load(context, R.raw.score_increase, 1));
        soundPoolMap.put(2, soundPool.load(context, R.raw.hit_sound, 1));

    }

    /**
     * Plays a sound.
     * Code borrowed from:<br>
     * https://dzoneq.com/articles/playing-sounds-android<br>
     * Many thanks to Tony Siciliani
     *
     * @param soundID The sound to play
     */
    public static void playSound(int soundID) {

        /* play sounds if the game isn't mute*/
        if(!isMute) {
            /* whatever in the range = 0.0 to 1.0*/
            float volume = 0.9f;

            /*play sound with same right and left volume, with a priority of 1,
             *zero repeats (i.e play once), and a playback rate of 1f.
             */
            soundPool.play(soundPoolMap.get(soundID), volume, volume, 1, 0, 1f);
        }

    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder binder) {
        mServ = ((MusicService.ServiceBinder) binder).getService();
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        mServ = null;
    }

    /**
     * Binds the music service with the activity.
     */
    @SuppressWarnings("unused")
    public void doBindService() {
        Intent intent = new Intent(this, MusicService.class);
        getApplicationContext().bindService(intent, this, Context.BIND_AUTO_CREATE);
        mIsBound = true;
    }

    /**
     * Unbinds the music service from the activity.
     */
    public void doUnbindService() {
        if (mIsBound) {
            getApplicationContext().unbindService(this);
            mIsBound = false;
        }
    }

    /**
     * Changes current game view theme by changing the view background.
     */
    @Override
    public void changeTheme() {
        if (thread != null) {
            thread.stopGameThread();
            thread = null;
        }

        /* Load fade out animation*/
        Animation fadeOut = AnimationUtils.loadAnimation(MainActivity.this, R.anim.fade_out);
        fadeOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                Animation fadeIn = AnimationUtils.loadAnimation(MainActivity.this, R.anim.fade_in);
                fadeIn.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                        /*set new theme here before fading in*/
                        //mBackgroundImageView.setImageURI(Uri.parse("android.resource://alaha" +
                        //        "shesh.com.skyjumper/drawable/" + R.drawable.sky2));
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {

                        /*start the game when the animation finishes*/
                        if (thread == null) {
                            thread.stopGameThread();
                            thread = null;
                        }

                        thread = new GameThread(MainActivity.this);
                        thread.start();
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });

                /* Start the fade in animation*/
                mBackgroundImageView.startAnimation(fadeIn);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });

        /* Start the fade out animation*/
        mBackgroundImageView.startAnimation(fadeOut);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        /* Update the finish screen with Facebook score if necessary (i.e. when it's greater)*/
        if (requestCode == SYNC_REQUEST && resultCode == RESULT_OK) {
            int tempScore = 0;
            if (data.getExtras() != null) {
                tempScore = data.getExtras().getInt("MAX_SCORE");
            }
            mMaxScoreTextView.setText(getString(R.string.integer_place_holder, tempScore));
        }
    }
}
