package com.example.metis.activities;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import com.example.metis.R;
import com.example.metis.utils.ApplicationDataManager;
import com.example.metis.utils.Logger;

import static com.example.metis.utils.Consts.*;

/**
 * MainActivity class is the start activity of the application.
 * It requests for storage, audio and video permissions if not
 * granted already and allows the user to connect using
 * username and password from the 'applicationData.json' file.
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private String TAG = this.getClass().getSimpleName();

    Button loginButton;
    EditText userName;
    EditText password;
    Logger logger;
    AppCompatActivity mainActivityReference = this;
    DialogInterface.OnClickListener errorListener = new DialogInterface.OnClickListener() {

        @Override
        public void onClick(DialogInterface dialog, int which) {
            mainActivityReference.finishAffinity();
            System.exit(0);
        }
    };
    DialogInterface.OnClickListener drawOnTopPermissionListener = new DialogInterface.OnClickListener() {

        @Override
        public void onClick(DialogInterface dialog, int which) {
            Intent myIntent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
            startActivity(myIntent);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);
        try {
            //initialize applicationDataManager map.
            APP_FILES_PATH = getExternalFilesDir(Environment.getDataDirectory().getAbsolutePath());
            ApplicationDataManager.initializeDirs(APP_FILES_PATH);
            logger = Logger.getInstance(APP_FILES_PATH);
            ApplicationDataManager.initializeMap();
        } catch (NullPointerException e) {
            logger.writeLog(LogType.ERROR, TAG, e.toString());

                ApplicationDataManager.getErrorMessageDialog(this, "'applicationData.json' file is missing!", errorListener);

        } catch (Exception e) {
            logger.writeLog(LogType.ERROR, TAG, e.toString());

                ApplicationDataManager.getErrorMessageDialog(this, BASIC_ERROR_MESSAGE, errorListener);

        }
        logger.writeLog(LogType.INFO, TAG, "Inside Main Activity");

        requestAllPermissions();

        loginButton = findViewById(R.id.login_button);
        loginButton.setOnClickListener(this);
        userName = findViewById(R.id.user_name_editText);
        password = findViewById(R.id.password_editText);
    }

    /**
     * This function request permission to camera,audio,storage
     */
    private void requestAllPermissions() {
        if (!checkPermissions()) {
            logger.writeLog(LogType.DEBUG, TAG, "Request permission to " +
                    "camera, audio, storage");
            ActivityCompat.requestPermissions(this, new String[]
                            {Manifest.permission.RECORD_AUDIO,
                                    Manifest.permission.CAMERA,
                                    Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    SUCCESS_PERMISSION_CODE);
            logger.writeLog(LogType.DEBUG, TAG, "Permissions granted successfully" );
        }

    }

    /**
     * This function defines what happens when click on log on button
     * @param v view
     */
    @Override
    public void onClick(View v) {
        if (Settings.canDrawOverlays(this)) {
            logger.writeLog(LogType.DEBUG, TAG, "The app have permission to DrawOverlay");
            if (checkPermissions()) {
                logger.writeLog(LogType.DEBUG, TAG, "The app have permissions to " +
                        "camera, audio, storage");
                if (ApplicationDataManager.checkEntryDetails(userName.getText().toString(), password.getText().toString())) {
                    logger.writeLog(LogType.INFO, TAG,
                            "The user " + userName.getText() + " logged in successfully");
                    Intent intent = new Intent(this, PatientsListActivity.class);
                    intent.putExtra(USER_NAME_KEYWORD, userName.getText().toString());
                    startActivity(intent);
                } else {
                    logger.writeLog(LogType.INFO, TAG,
                            "User name or password are incorrect");
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("User name or password are incorrect. " +
                            "Please try again.");
                    builder.setPositiveButton("OK", null);
                    AlertDialog alertDialog = builder.create();
                    if (!isFinishing() && !isDestroyed()) {
                        alertDialog.show();
                    }
                }
            } else {
                logger.writeLog(LogType.DEBUG, TAG, "The app don't have permissions to " +
                        "camera, audio, storage");
                requestAllPermissions();
            }
        }else{
            logger.writeLog(LogType.DEBUG, TAG, "The app don't have permission to " +
                    "DrawOverlay send the user to permission window");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                String message = "Please allow DrawOnTop permission. In settings window," +
                        " find Metis, and turn on permissions.";
                ApplicationDataManager.getErrorMessageDialog(this, message, drawOnTopPermissionListener);
            }
        }
    }

    /**
     * This function check if the app has permissions to camera, audio, storage.
     * @return true if the app have permissions to camera, audio, storage and false otherwise.
     */
    private boolean checkPermissions() {
        return (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) &&
                (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) &&
                (ContextCompat.checkSelfPermission(MainActivity.this,
                        Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED);
    }
}
