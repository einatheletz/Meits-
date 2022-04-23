package com.example.metis.activities;

import android.content.DialogInterface;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.metis.data_types.Patient;
import com.example.metis.utils.ApplicationDataManager;
import com.example.metis.utils.EventWriter;
import com.example.metis.R;
import com.example.metis.utils.Logger;

import java.io.File;
import java.util.Random;

import static com.example.metis.utils.Consts.*;
import static java.lang.Thread.sleep;

/**
 * SimonActivity class is an activity of the known 'Simon' game.
 * The screen is divided to 4 colors, which lights up in a different
 * pattern each time, and after the pattern is shown to the user, he
 * needs to repeat the same pattern in order to gain more points.
 */
public class SimonActivity extends AppCompatActivity
        implements View.OnClickListener,
        View.OnTouchListener,
        GestureDetector.OnGestureListener {

    private String TAG = this.getClass().getSimpleName();

    private View leftTop;
    private View leftBottom;
    private View rightTop;
    private View rightBottom;
    private TextView textView;
    private ImageButton backButton;
    private boolean isPlaying = false;
    private final int MAX_LENGTH = 1000;
    private int[] array_of_moves = new int[MAX_LENGTH];
    private int numberOfElementsInMovesArray = 0, k = 0, numberOfClicksEachStage = 0, x, sadMusic, highScore = 0, hardness;
    private SoundPool sp = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
    private Random r = new Random();
    private final Handler handler = new Handler();
    private File touchEventsFile;
    private GestureDetector gestureDetector;
    private Logger logger = Logger.getInstance(APP_FILES_PATH);
    private SimonActivity SimonReference;
    private Patient patient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        SimonReference = this;
        setContentView(R.layout.activity_simon);
        patient = (Patient) getIntent().getSerializableExtra(PATIENT_KEYWORD);
        ApplicationDataManager.setOrientation(patient, this);
        ApplicationDataManager.fullScreenView(getWindow());

        sadMusic = sp.load(this, R.raw.sad2, 1);
      
        leftTop = findViewById(R.id.leftTop);
        leftBottom = findViewById(R.id.leftBottom);
        rightTop = findViewById(R.id.rightTop);
        rightBottom = findViewById(R.id.rightBottom);
        textView = findViewById(R.id.textView2);

        touchEventsFile = (File) getIntent().getSerializableExtra(TOUCH_EVENTS_FILE_PATH);

        gestureDetector = new GestureDetector(this, this);

        View.OnTouchListener onTouch = new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    if (!isPlaying) {
                        return true;
                    }

                    String currentPressString = "";

                    switch (v.getId()) {
                        case R.id.leftTop:
                            currentPressString = "On Blue button";
                            x = 1;
                            break;
                        case R.id.rightTop:
                            currentPressString = "On Green button";
                            x = 2;
                            break;
                        case R.id.rightBottom:
                            currentPressString = "On Yellow button";
                            x = 4;
                            break;
                        case R.id.leftBottom:
                            currentPressString = "On Red button";
                            x = 3;
                            break;
                    }
                    if (array_of_moves[numberOfClicksEachStage] != x) { // on wrong click

                        EventWriter.writeString("Event,Wrong Press " + currentPressString, touchEventsFile, TAG);
                        EventWriter.writeEvent(event, touchEventsFile, TAG, true);

                        sp.play(sadMusic, 1, 1, 1, 0, 1f);

                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(SimonActivity.this);

                        alertDialogBuilder.setMessage(LANGUAGE_ARRAY[LANGUAGE_LINE][Messages.Wrong_click.ordinal()] +
                                (numberOfElementsInMovesArray > 0?(numberOfElementsInMovesArray - 1): 0) +
                                LANGUAGE_ARRAY[LANGUAGE_LINE][Messages.Do_you_want_to_keep_playing.ordinal()]);
                        alertDialogBuilder.setPositiveButton(LANGUAGE_ARRAY[LANGUAGE_LINE][Messages.Yes.ordinal()],

                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface arg0, int arg1) {
                                        EventWriter.writeString("Event,Patient wanted to play again", touchEventsFile, TAG);
                                        //Start over
                                        clear();

                                        textView.setText(
                                            LANGUAGE_ARRAY[LANGUAGE_LINE][Messages.Current_score.ordinal()] +
                                            numberOfElementsInMovesArray +
                                            LANGUAGE_ARRAY[LANGUAGE_LINE][Messages.High_score.ordinal()] +  highScore);

                                        AlertDialog.Builder levelsAlert = new AlertDialog.Builder(SimonActivity.this);
                                        levelsAlert.setMessage(LANGUAGE_ARRAY[LANGUAGE_LINE][Messages.Change_the_difficulty_level.ordinal()]);
                                        levelsAlert.setPositiveButton(LANGUAGE_ARRAY[LANGUAGE_LINE][Messages.Yes.ordinal()],
                                                new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface arg0, int arg1) {
                                                        EventWriter.writeString("Event,Patient wanted to change game hardness", touchEventsFile, TAG);
                                                        levelsChoose();
                                                    }
                                                });
                                        levelsAlert.setNegativeButton(LANGUAGE_ARRAY[LANGUAGE_LINE][Messages.No.ordinal()],
                                                new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface arg0, int arg1) {
                                                        EventWriter.writeString("Event,Patient did not want to change game hardness", touchEventsFile, TAG);
                                                        playGame();
                                                    }
                                                });
                                        AlertDialog alertDialog = levelsAlert.create();
                                       showDialog(alertDialog, SimonReference);
                                    }
                                });
                        alertDialogBuilder.setNegativeButton(LANGUAGE_ARRAY[LANGUAGE_LINE][Messages.No.ordinal()], new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                EventWriter.writeString("Event,Patient did not want to play again", touchEventsFile, TAG);
                                finish();
                            }
                        });

                        AlertDialog alertDialog = alertDialogBuilder.create();
                        showDialog(alertDialog, SimonReference);
                        return true;
                    }
                    //on success
                    EventWriter.writeString("Event,Correct Press " + currentPressString, touchEventsFile, TAG);
                    EventWriter.writeEvent(event, touchEventsFile, TAG, true);

                    playSound(v.getId());
                    xorMyColor(v);
                    numberOfClicksEachStage++;
                    if (numberOfElementsInMovesArray == numberOfClicksEachStage) { //if 4 boxes shown, then activate  function
                        //playGame only after 4 clicks have been made by the user

                        EventWriter.writeString("Event,Correct Full Sequence of length " +
                                numberOfElementsInMovesArray, touchEventsFile, TAG);

                        isPlaying = false;
                        numberOfClicksEachStage = 0;
                        if (numberOfElementsInMovesArray > highScore) {
                            highScore = numberOfElementsInMovesArray;
                        }

                        textView.setText(LANGUAGE_ARRAY[LANGUAGE_LINE][Messages.Current_score.ordinal()] +
                                numberOfElementsInMovesArray +
                                LANGUAGE_ARRAY[LANGUAGE_LINE][Messages.High_score.ordinal()] +  highScore);

                        final Runnable r = new Runnable() {
                            public void run() {
                                playGame();
                            }
                        };
                        handler.postDelayed(r, 1000 - 250 * hardness);
                    }
                }
                return true;
            }
        };

        backButton = findViewById(R.id.back_button);
        backButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    float x = event.getX();
                    float y = event.getY();
                    EventWriter.writeString("Event,Back button pressed,x:" + x + ",y:" + y, touchEventsFile, TAG);
                    EventWriter.writeEvent(event, touchEventsFile, TAG, true);
                    gestureDetector.onTouchEvent(event);
                    onBackPressed();
                    return true;
                } else {
                    return false;
                }
            }
        });

        leftTop.setOnTouchListener(onTouch);
        leftBottom.setOnTouchListener(onTouch);
        rightBottom.setOnTouchListener(onTouch);
        rightTop.setOnTouchListener(onTouch);

        LinearLayout simonBackgroundView = (LinearLayout) findViewById(R.id.simon_view);
        AppCompatActivity SimonActivityReference = this;
        simonBackgroundView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                logger.writeLog(LogType.INFO, TAG, "onTouch. Touch event in SimonActivity.");
                EventWriter.writeEvent(event, touchEventsFile, TAG, true);
                SimonActivityReference.onTouchEvent(event);
                return true;
            }
        });
        setContentView(simonBackgroundView);

        levelsChoose();
    }

    public void levelsChoose() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, android.R.style.Theme_Translucent_NoTitleBar_Fullscreen);

        String[] difficultyLevelsOptions = {LANGUAGE_ARRAY[LANGUAGE_LINE][Messages.Easy.ordinal()],
                LANGUAGE_ARRAY[LANGUAGE_LINE][Messages.Medium.ordinal()],
                LANGUAGE_ARRAY[LANGUAGE_LINE][Messages.Hard.ordinal()],
                LANGUAGE_ARRAY[LANGUAGE_LINE][Messages.Very_Hard.ordinal()]};
        builder.setTitle(LANGUAGE_ARRAY[LANGUAGE_LINE][Messages.Select_a_difficulty_level.ordinal()])

                .setItems(difficultyLevelsOptions, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        hardness = which;
                        textView.setText(LANGUAGE_ARRAY[LANGUAGE_LINE][Messages.Current_score.ordinal()] +
                            numberOfElementsInMovesArray +
                            LANGUAGE_ARRAY[LANGUAGE_LINE][Messages.High_score.ordinal()] +  highScore);

                        //on initial start, click the playGame function after delay
                        final Runnable r = new Runnable() {
                            public void run() {
                                playGame();
                            }
                        };
                        handler.postDelayed(r, 3000);
                    }
                });
        AlertDialog myDialog = builder.create();
        showDialog(myDialog, SimonReference);
    }

    private void playSound(int id) {
        //function that play sound according to sound ID
        int audioRes = 0;
        switch (id) {
            case R.id.leftTop:
                audioRes = R.raw.doo;
                break;
            case R.id.rightTop:
                audioRes = R.raw.re;
                break;
            case R.id.rightBottom:
                audioRes = R.raw.mi;
                break;
            case R.id.leftBottom:
                audioRes = R.raw.fa;
                break;

        }
        EventWriter.writeString("Event,sound of keys playing now", touchEventsFile, TAG);
        MediaPlayer p = MediaPlayer.create(this, audioRes);
        p.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mp.release();
            }
        });
        p.start();
    }

    private void xorMyColor(final View v) {
        //function that changes the background color and get it back after 500 milliseconds
        v.getBackground().setAlpha(51);
        final Runnable r = new Runnable() {
            public void run() {
                v.getBackground().setAlpha(255);
            }
        };
        handler.postDelayed(r, 300);
    }

    public void playGame() {
        if (!isFinishing() && !isDestroyed()) {
            EventWriter.writeString("Event,New round started", touchEventsFile, TAG);
            appendValueToArray();
            numberOfElementsInMovesArray++;
            for (k = 0; k < numberOfElementsInMovesArray; k++) {
                click(k);
            }

            final Runnable trueRunnable = new Runnable() {
                @Override
                public void run() {
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(SimonActivity.this);
                    alertDialogBuilder.setMessage(LANGUAGE_ARRAY[LANGUAGE_LINE][Messages.Your_Turn.ordinal()]);
                    EventWriter.writeString("Event,Patient's turn to press", touchEventsFile, TAG);
                    final AlertDialog alertDialog = alertDialogBuilder.create();
                    //check if activity is not destroyed before showing dialog.
                    if (!isFinishing() && !isDestroyed()) {
                        showDialog(alertDialog, SimonReference);
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                alertDialog.cancel();
                                ApplicationDataManager.fullScreenView(getWindow());
                                isPlaying = true;
                            }
                        }, 1000);
                    }
                }
            };
            handler.postDelayed(trueRunnable, (1000 - 250 * hardness) * (numberOfElementsInMovesArray - 1) + 400);
        }
    }

    public void click(final int click_index) {
        //Function that clicks one place randomally on the view
        final Runnable r = new Runnable() {
            public void run() {
                //check if activity is not destroyed before playing sounds.
                if(!isFinishing() && !isDestroyed()) {
                    switch (array_of_moves[click_index]) {
                        case 1:
                            EventWriter.writeString("Event,Blue button shown", touchEventsFile, TAG);
                            playSound(R.id.leftTop);
                            xorMyColor(leftTop);
                            break;
                        case 2:
                            EventWriter.writeString("Event,Green button shown", touchEventsFile, TAG);
                            playSound(R.id.rightTop);
                            xorMyColor(rightTop);
                            break;
                        case 3:
                            EventWriter.writeString("Event,Red button shown", touchEventsFile, TAG);
                            playSound(R.id.leftBottom);
                            xorMyColor(leftBottom);
                            break;
                        case 4:
                            EventWriter.writeString("Event,Yellow button shown", touchEventsFile, TAG);
                            playSound(R.id.rightBottom);
                            xorMyColor(rightBottom);
                            break;
                        default:
                            break;
                    }
                }
            }
        };
        handler.postDelayed(r, (1000 - 250 * hardness) * click_index);
    }


    private int generateRandomNumber() {
        return r.nextInt(4) + 1; // generate random number between 1 and 4
    }

    private void appendValueToArray() {  // add random number to the first free position in the array
        for (int i = 0; i < MAX_LENGTH; i++) {
            if (array_of_moves[i] == 0) {
                array_of_moves[i] = generateRandomNumber();
                break;
            }
        }
    }

    private void clear() {//reset the game to initial state
        for (int i = 0; i < MAX_LENGTH; i++) {
            array_of_moves[i] = 0;
        }
        numberOfClicksEachStage = 0;
        numberOfElementsInMovesArray = 0;
    }

    // Touch listeners:

    @Override
    public void onClick(View v) {

    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return false;
    }

    /**
     * This method is invoked when an unrelated touch occurred
     * @param event motion event of the touch.
     * @return boolean.
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        EventWriter.writeEvent(event, touchEventsFile, TAG, false);
        gestureDetector.onTouchEvent(event);
        return true;
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
    private void showDialog(AlertDialog alertDialog, SimonActivity gameRef){
        //check if activity is not destroyed before showing dialog.
        if(!isFinishing() && !isDestroyed()) {
            //Here's the magic..
            //Set the dialog to not focusable (makes navigation ignore us adding the window)
            alertDialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);

            //Show the dialog!
            alertDialog.show();

            //Set the dialog to immersive
            alertDialog.getWindow().getDecorView().setSystemUiVisibility(
                    gameRef.getWindow().getDecorView().getSystemUiVisibility());

            //Clear the not focusable flag from the window
            alertDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
        }
    }
}

