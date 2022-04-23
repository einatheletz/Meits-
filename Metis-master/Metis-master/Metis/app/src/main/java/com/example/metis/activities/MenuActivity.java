package com.example.metis.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;

import androidx.appcompat.app.AlertDialog;

import android.widget.Toast;

import static com.example.metis.utils.Consts.*;
import com.example.metis.services.VideoRecordService;
import com.example.metis.utils.EventWriter;
import com.example.metis.data_types.Patient;
import com.example.metis.R;
import com.example.metis.services.SensorsService;
import com.example.metis.utils.ApplicationDataManager;
import com.example.metis.utils.Logger;

/**
 * MenuActivity class is responsible for starting a patient's session:
 * starting sensors and video-recording services in the background,
 * navigating to games and videos,
 * and allowing session ending (closes all the background services).
 */
public class MenuActivity extends AppCompatActivity
        implements View.OnClickListener,
        View.OnTouchListener,
        GestureDetector.OnGestureListener {

    private String TAG = this.getClass().getSimpleName();

    private File touchEventsFile;
    private GestureDetector gestureDetector;

    private Logger logger = Logger.getInstance(APP_FILES_PATH);

    private Button gamesButton;
    private Button videosButton;
    private Button endSessionButton;

    private Intent sensorIntent;
    private Intent videoRecordIntent;
    private final Handler handler = new Handler();
    private Patient patient;

    private AlertDialog endSessionDialog;
    private EditText password;
    AppCompatActivity menuActivityReference = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_menu);
        ApplicationDataManager.fullScreenView(getWindow()); // full screen so the patient won't get back to the starting screen.
        Intent intent = getIntent();
        patient = (Patient) intent.getSerializableExtra(PATIENT_KEYWORD);
        //set screen orientation according to dominant hand
        ApplicationDataManager.setOrientation(patient,this);
        logger.writeLog(LogType.INFO, TAG, "Inside " + TAG);
        DialogInterface.OnClickListener errorListener = new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                //TODO: Call finishProgramFunction
                System.exit(-1);
            }
        };

        gestureDetector = new GestureDetector(this, this);

        // button for game activities.
        gamesButton = (Button) findViewById(R.id.games_button);
        gamesButton.setOnClickListener(this);
        gamesButton.setOnTouchListener(this);

        // button for videos activities.
        videosButton = (Button) findViewById(R.id.videos_button);
        videosButton.setOnClickListener(this);
        videosButton.setOnTouchListener(this);

        // button for ending the session with the current patient.
        endSessionButton = (Button) findViewById(R.id.end_session_button);
        endSessionButton.setOnClickListener(this);
        endSessionButton.setOnTouchListener(this);

        //set alertDialog layout
        final LinearLayout dialogLayout = createDialogLayout();
        //create dialog
        endSessionDialog = createDialog(dialogLayout);
        logger.writeLog(LogType.INFO, TAG, "onCreate. Session started. Patient id: " +
                patient.getPatientId() + ". Patient sensors: " + patient.getSensors().toString());

        // Create the files for sensors, video recordings and touch events
        String currentDateString = DATE_FORMAT.format(Calendar.getInstance().getTime());
        File sensorFile = new File(APP_FILES_PATH + DATA_FOLDER + patient.getPatientId() + "_" + currentDateString + "_sensors_data.csv");
        touchEventsFile = new File(APP_FILES_PATH + DATA_FOLDER + patient.getPatientId() + "_" + currentDateString + "_touch_events.csv");

        // open touch events file.
        if (!touchEventsFile.exists()) {
            try {
                touchEventsFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // create and start the sensors and video recording services in the background.
        sensorIntent = new Intent(this, SensorsService.class);
        sensorIntent.putExtra(PATIENT_KEYWORD, patient);
        sensorIntent.putExtra(SENSORS_FILE_PATH, sensorFile);
        try {
            logger.writeLog(LogType.DEBUG, TAG, "Start sensors service");
            startService(sensorIntent);
        }catch (Exception e) {
            logger.writeLog(LogType.DEBUG, TAG, "Fail to start sensors service");
            ApplicationDataManager.getErrorMessageDialog(this, "Fail to start sensors service",
                    errorListener);

        }
//        bindService(sensorIntent,sensorIntent,0);

        videoRecordIntent = new Intent(this, VideoRecordService.class);
        videoRecordIntent.putExtra(PATIENT_KEYWORD, patient);
        videoRecordIntent.putExtra(CURRENT_DATE_STRING, currentDateString);
        try{
            logger.writeLog(LogType.DEBUG, TAG, "Start video service");
            startService(videoRecordIntent);
        }catch (Exception e){
            logger.writeLog(LogType.DEBUG, TAG, "Fail to start video service");
            ApplicationDataManager.getErrorMessageDialog(this, "Fail to start sensors service",
                    errorListener);
        }
    }

    /**
     * This method is used in order to block back button functionality from the user.
     */
    @Override
    public void onBackPressed() {
        logger.writeLog(LogType.INFO, TAG, "onBackPressed. Back button pressed.");
        Toast.makeText(this,
            LANGUAGE_ARRAY[LANGUAGE_LINE][Messages.This_key_cannot_be_used_at_this_time.ordinal()],
            Toast.LENGTH_SHORT).show();
        ApplicationDataManager.fullScreenView(getWindow());
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

    @Override
    public void onClick(View v) {
        logger.writeLog(LogType.INFO, TAG, "onClick. view: " + v);
        Intent intent;
        switch (v.getId()) {
            case R.id.games_button:
                intent = new Intent(this, GamesActivity.class);
                // pass the touch events file to the games activity.
                intent.putExtra(TOUCH_EVENTS_FILE_PATH, touchEventsFile);
                intent.putExtra(PATIENT_KEYWORD, patient);
                logger.writeLog(LogType.DEBUG, TAG,"Start gameActivity" );
                startActivity(intent);
                break;
            case R.id.videos_button:
                intent = new Intent(this, VideosActivity.class);
                // pass series list and touch events file to the videos activity.
                intent.putStringArrayListExtra(SERIES_KEYWORD,(ArrayList<String>)patient.getSeries());
                intent.putExtra(TOUCH_EVENTS_FILE_PATH, touchEventsFile);
                intent.putExtra(PATIENT_KEYWORD, patient);
                logger.writeLog(LogType.DEBUG, TAG,"Start videoActivity");
                startActivity(intent);
                break;
            case R.id.end_session_button:
                // show end session dialog in order to verify the patient is not ending it.
                logger.writeLog(LogType.DEBUG, TAG,"Start end session");
                showDialog();
                break;
        }
    }

    /**
     * This method is invoked when an unrelated touch occurred
     * @param event motion event of the touch.
     * @return boolean
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        logger.writeLog(LogType.INFO, TAG, "onTouchEvent. event:" + event);
        EventWriter.writeEvent(event, touchEventsFile, TAG, false);
        gestureDetector.onTouchEvent(event); // detect the type of the touch event.
        return true;
    }

    /**
     * This method is invoked when a specific VIEW was touched.
     * @param v The view that had been touched by the user.
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
                case R.id.games_button:
                    EventWriter.writeString("Event,Games button pressed,x:" + x + ",y:" + y, touchEventsFile, TAG);
                    EventWriter.writeEvent(event, touchEventsFile, TAG, true);
                    gestureDetector.onTouchEvent(event);
                    v.performClick();
                    return true;
                case R.id.videos_button:
                    EventWriter.writeString("Event,Videos button pressed,x:" + x + ",y:" + y, touchEventsFile, TAG);
                    EventWriter.writeEvent(event, touchEventsFile, TAG, true);
                    gestureDetector.onTouchEvent(event);
                    v.performClick();
                    return true;
                case R.id.end_session_button:
                    EventWriter.writeString("Event,End Session button pressed,x:" + x + ",y:" + y, touchEventsFile, TAG);
                    EventWriter.writeEvent(event, touchEventsFile, TAG, true);
                    gestureDetector.onTouchEvent(event);
                    v.performClick();
                    return true;
                default:
                    EventWriter.writeString("Unrelated touch", touchEventsFile, TAG);
                    EventWriter.writeEvent(event, touchEventsFile, TAG, true);
                    gestureDetector.onTouchEvent(event);
                    return true;
            }
        } else {
            return false;
        }
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


    /**
     * This method receives a dialog layout and creates an AlertDialog in order to interact
     * with the user and decide if should be allowed to end session.
     * @param dialogLayout layout for the dialog shown to user.
     * @return AlertDialog dialog.
     */
    private AlertDialog createDialog(View dialogLayout) {
        logger.writeLog(LogType.INFO, TAG, "createDialog. dialog created for End Session");
        return new AlertDialog.Builder(this)
                .setTitle("End Session")
                .setIcon(android.R.drawable.ic_lock_power_off)
                .setMessage("Please enter user details to end the session.")
                .setPositiveButton("Done", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        boolean res = ApplicationDataManager.checkEntryDetails(patient.getTherapistName(), password.getText().toString());
                        if (res) {
                            logger.writeLog(LogType.INFO, TAG, "createDialog. Session ended.");
                            // stop both sensors and video recording.
                            logger.writeLog(LogType.DEBUG, TAG, "End sensor service.");
                            stopService(sensorIntent);
                            logger.writeLog(LogType.DEBUG, TAG, "End video service.");
                            stopService(videoRecordIntent);
                            menuActivityReference.finishAffinity();

                            // close logger writer.
                            logger.writeLog(LogType.INFO, TAG, "createDialog. Closing logger writer.");

                            //fix since services take a bit more than 1 second to close.
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    logger.closeWriter();
                                    System.exit(0);
                                }
                            }, 1500);

                        } else {
                            logger.writeLog(LogType.INFO, TAG, "createDialog. wrong username or password for End Session");
                            //message for wrong input
                            Toast.makeText(MenuActivity.this, "Wrong user name or password! Please try again.",
                                    Toast.LENGTH_SHORT).show();
                            //show dialog again for retry.
                            showDialog();
                        }
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        logger.writeLog(LogType.INFO, TAG, "createDialog. Canceled End Session");
                        //message for canceling (session is still active).
                        Toast.makeText(MenuActivity.this, "Session is still active.",
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .setView(dialogLayout)
                .create();
    }

    /**
     * This method creates a linear layout of a dialog.
     * @return LinearLayout dialog layout.
     */
    private LinearLayout createDialogLayout() {
        logger.writeLog(LogType.DEBUG, TAG, "Creates a linear layout of a dialog.");
        LinearLayout dialogLayout = new LinearLayout(this);
        dialogLayout.setOrientation(LinearLayout.VERTICAL);
        //set user name input field
//        userName = new EditText(this);
//        userName.setInputType(InputType.TYPE_CLASS_TEXT);
//        userName.setHint("User Name");
//        dialogLayout.addView(userName);

        //set password input field
        password = new EditText(this);
        password.setInputType(InputType.TYPE_CLASS_NUMBER);
        password.setHint("Password");
        password.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        password.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (TextUtils.isEmpty(s)) {
                    //if( TextUtils.isEmpty(userName.getText()))
                    endSessionDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                } else {
                    endSessionDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                }
            }
        });
        dialogLayout.addView(password);
        logger.writeLog(LogType.DEBUG, TAG, " Finish to creates a linear layout of a dialog.");
        return dialogLayout;
    }

    /**
     * This method is used in order to display an end session dialog.
     * It does not allow to press OK until name and password given.
     */
    private void showDialog() {
        logger.writeLog(LogType.DEBUG, TAG, "Display an end session dialog.");
        if (!isFinishing() && !isDestroyed()) {
            //Here's the magic..
            //Set the dialog to not focusable (makes navigation ignore us adding the window)
            endSessionDialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);

            //Show the dialog!
            endSessionDialog.show();

            //Set the dialog to immersive
            endSessionDialog.getWindow().getDecorView().setSystemUiVisibility(
                    this.getWindow().getDecorView().getSystemUiVisibility());

            //Clear the not focusable flag from the window
            endSessionDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
            endSessionDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
            ApplicationDataManager.fullScreenView(getWindow());
        }
    }
}
