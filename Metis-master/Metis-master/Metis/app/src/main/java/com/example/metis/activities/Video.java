package com.example.metis.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.net.Uri;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.example.metis.R;
import com.example.metis.data_types.Patient;
import com.example.metis.utils.ApplicationDataManager;
import com.example.metis.utils.EventWriter;
import com.example.metis.utils.Logger;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import java.io.File;

import static com.example.metis.utils.Consts.*;

/**
 * Video class is responsible for presenting a video chosen
 * by the patient, and allow pausing and scrolling through the episode.
 */
public class Video extends AppCompatActivity
        implements
        View.OnClickListener,
        View.OnTouchListener,
        GestureDetector.OnGestureListener {

    private String TAG = getClass().getSimpleName();

    private Logger logger = Logger.getInstance(APP_FILES_PATH);

    private File touchEventsFile;
    private GestureDetector gestureDetector;

    private SimpleExoPlayer sep;

    private ImageButton playButton;
    private ImageButton pauseButton;
    private ImageButton backButton;
    private Patient patient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        ApplicationDataManager.fullScreenView(getWindow());

        logger.writeLog(LogType.INFO, TAG, "onCreate. Creating video player.");

        touchEventsFile = (File) getIntent().getSerializableExtra(TOUCH_EVENTS_FILE_PATH);
        gestureDetector = new GestureDetector(this, this);
        patient = (Patient) getIntent().getSerializableExtra(PATIENT_KEYWORD);

        // Create and start the chosen video:
        setContentView(R.layout.activity_video);
        ApplicationDataManager.setOrientation(patient, this);
        sep = new SimpleExoPlayer.Builder(this).build();
        PlayerView playerView = findViewById(R.id.video);
        playerView.setPlayer(sep);
        // Produces DataSource instances through which media data is loaded.
        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(this,
                Util.getUserAgent(this, APP_NAME));
        String path = getIntent().getStringExtra(PATH_TO_VIDEO_KEYWORD);
        Uri uri = Uri.parse(path);
        // This is the MediaSource representing the media to be played.
        MediaSource videoSource = new ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(uri);
        // Prepare the player with the source.
        sep.prepare(videoSource);

        playButton = findViewById(R.id.exo_play);
        pauseButton = findViewById(R.id.exo_pause);
        backButton = findViewById(R.id.exo_back);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logger.writeLog(LogType.INFO, TAG, "onClick. Back button pressed.");
                onBackPressed();
            }
        });
        LinearLayout videoPlayerLayout = (LinearLayout) findViewById(R.id.video_player);
        playButton.setOnTouchListener(this);
        pauseButton.setOnTouchListener(this);
        backButton.setOnTouchListener(this);
        videoPlayerLayout.setOnTouchListener(this);
        playerView.setOnTouchListener(this);

        logger.writeLog(LogType.INFO, TAG, "onCreate. Finished creating video player.");
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        sep.release();
    }

    @Override
    public void onClick(View v) {

    }

    /**
     * This method is invoked when an unrelated touch occurred
     * @param event motion event of the touch.
     * @return boolean.
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        logger.writeLog(LogType.INFO, TAG, "onTouchEvent. event:" + event);
        EventWriter.writeEvent(event, touchEventsFile, TAG, false);
        gestureDetector.onTouchEvent(event);
        return true;
    }

    /**
     * This method is invoked when a specific VIEW was touched.
     *
     * @param v     The view that had been touched by the user.
     * @param event motion event of the touch.
     * @return boolean
     */
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        logger.writeLog(LogType.INFO, TAG, "onTouch. view:" + v + ", event:" + event);
        // take action only on press (avoid double touches with UP, SCROLL, etc...)
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            float x = event.getX();
            float y = event.getY();
            switch (v.getId()) {
                case R.id.exo_play:
                    EventWriter.writeString("Event,Play button pressed,x:" + x + ",y:" + y, touchEventsFile, TAG);
                    EventWriter.writeEvent(event, touchEventsFile, TAG, true);
                    gestureDetector.onTouchEvent(event);
                    v.performClick();
                    return true;
                case R.id.exo_pause:
                    EventWriter.writeString("Event,Pause button pressed,x:" + x + ",y:" + y, touchEventsFile, TAG);
                    EventWriter.writeEvent(event, touchEventsFile, TAG, true);
                    gestureDetector.onTouchEvent(event);
                    v.performClick();
                    return true;
                case R.id.exo_back:
                    EventWriter.writeString("Event,Back button pressed,x:" + x + ",y:" + y, touchEventsFile, TAG);
                    EventWriter.writeEvent(event, touchEventsFile, TAG, true);
                    gestureDetector.onTouchEvent(event);
                    v.performClick();
                    return true;
                case R.id.video_player:
                    EventWriter.writeString("Event,Pressed on video view,x:" + x + ",y:" + y, touchEventsFile, TAG);
                    EventWriter.writeEvent(event, touchEventsFile, TAG, false);
                    gestureDetector.onTouchEvent(event);
                    return true;
                case R.id.video:
                    EventWriter.writeString("Event,Pressed on time bar,x:" + x + ",y:" + y, touchEventsFile, TAG);
                    EventWriter.writeEvent(event, touchEventsFile, TAG, false);
                    gestureDetector.onTouchEvent(event);
                    return true;
            }
        }
        return false;
    }

    @Override
    public boolean onDown(MotionEvent e) {
        EventWriter.writeString("Touch Details,Down Press", touchEventsFile, TAG);
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {
        EventWriter.writeString("Touch Details,Show Press", touchEventsFile, TAG);
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        EventWriter.writeString("Touch Details,Single Tap", touchEventsFile, TAG);
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        EventWriter.writeString("Touch Details,Scroll Motion: Distance X: " + distanceX
                + " Distance Y: " + distanceY, touchEventsFile, TAG);
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {
        EventWriter.writeString("Touch Details,Long Press", touchEventsFile, TAG);
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        EventWriter.writeString("Touch Details,Fling Motion: Velocity X: " + velocityX
                + " Velocity Y: " + velocityY, touchEventsFile, TAG);
        return false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        ApplicationDataManager.fullScreenView(getWindow());
    }

    @Override
    protected void onStart() {
        super.onStart();
        ApplicationDataManager.fullScreenView(getWindow());
    }
}