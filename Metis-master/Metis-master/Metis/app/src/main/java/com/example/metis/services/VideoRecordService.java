package com.example.metis.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;

import com.example.metis.data_types.Patient;
import com.example.metis.R;
import com.example.metis.utils.Logger;

import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.util.Date;

import static com.example.metis.utils.Consts.*;

/**
 * VideoRecordService class is responsible for video recording with
 * the front camera. It stores the video in mp4 file.
 */
public class VideoRecordService extends Service implements SurfaceHolder.Callback {

    private String TAG = VideoRecordService.class.getSimpleName();

    Logger logger = Logger.getInstance(APP_FILES_PATH);

    private WindowManager windowManager;
    private SurfaceView surfaceView;
    private Camera camera = null;
    private MediaRecorder mediaRecorder = null;

    private static String patientId = "";
    private String dateString;

    private Patient patient;
    private int counter;

    /**
     * This function defines what happens when the service is started.
     * @return int START_STICKY.
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        counter = 1;

        dateString = (String) intent.getSerializableExtra(CURRENT_DATE_STRING);


        patient = (Patient) intent.getSerializableExtra(PATIENT_KEYWORD);
        if (patient != null) {
            patientId = patient.getPatientId();
        } else {
            logger.writeLog(LogType.ERROR, TAG, "onStartCommand. Current patient is null");
        }

        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel channel;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            channel = new NotificationChannel("YOUR_CHANNEL_ID",
                    "YOUR_CHANNEL_NAME",
                    NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("YOUR_NOTIFICATION_CHANNEL_DISCRIPTION");

            if (mNotificationManager != null) {
                mNotificationManager.createNotificationChannel(channel);
                // Start foreground service to avoid an unexpected kill

                Notification notification = new Notification.Builder(this)
                        .setContentTitle("Background Video Recorder")
                        .setContentText("")
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setChannelId(channel.getId())
                        .build();
                startForeground(1234, notification);
            }

            // Create new SurfaceView, set its size to 1x1, move it to the top left corner and set this service as a callback
            windowManager = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
            surfaceView = new SurfaceView(this);
            WindowManager.LayoutParams params = new WindowManager.LayoutParams(1, 1,
                    Build.VERSION.SDK_INT < Build.VERSION_CODES.O ?
                            WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY :
                            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT);
            try {
                windowManager.addView(surfaceView, params);
            } catch (Exception e) {
                logger.writeLog(LogType.INFO, TAG,
                        "Failed to add a view to windowManger" + e.getLocalizedMessage());
            }
            surfaceView.getHolder().addCallback(this);
        }
        return START_STICKY;
    }

    /**
     * This method is called when surface for showing the video
     * is created. It initializes and starts MediaRecorder.
     * @param surfaceHolder surface for showing the video.
     */
    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder)  {
        // open the front-facing camera of the phone.
        if(camera == null) {
            Camera cam = null;
            Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
            int cameraCount = Camera.getNumberOfCameras();
            for (int camIdx = 0; camIdx < cameraCount; camIdx++) {
                Camera.getCameraInfo(camIdx, cameraInfo);
                if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                    try {
                        logger.writeLog(LogType.INFO, TAG, "Camera successfully opened");
                        cam = Camera.open(camIdx);
                        break;
                    } catch (RuntimeException e) {
                        logger.writeLog(LogType.ERROR, TAG, "surfaceCreated. Camera failed to open: " + e.getLocalizedMessage());
                        Log.e(TAG, "Camera failed to open: " + e.getLocalizedMessage());
                    }
                }
            }
            camera = cam;
        }

        /**
         * Initialize MediaRecorder and start recording:
         */
        mediaRecorder = new MediaRecorder();
        camera.unlock();

        // Set surface, camera, and audio source for the recording.
        mediaRecorder.setPreviewDisplay(surfaceHolder.getSurface());
        mediaRecorder.setCamera(camera);
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

        // Set the quality for the video.
        mediaRecorder.setProfile(CamcorderProfile.get(CAMERA_RESOLUTION_MAP.get(patient.getCameraResolution())));

        mediaRecorder.setPreviewDisplay(surfaceHolder.getSurface());
        // Set the output file.
        logger.writeLog(LogType.INFO, TAG, "surfaceCreated. Record video path: "
                + getExternalFilesDir(Environment.getDataDirectory().getAbsolutePath()).getAbsolutePath()
                + DATA_FOLDER + patientId + "_" + dateString + ".mp4");
        mediaRecorder.setOutputFile(
                getExternalFilesDir(Environment.getDataDirectory().getAbsolutePath()).getAbsolutePath()
                        +DATA_FOLDER + patientId + "_" + dateString + ".mp4");
        mediaRecorder.setOnInfoListener(new MediaRecorder.OnInfoListener() {
            @Override
            public void onInfo(MediaRecorder mediaRecorder, int what, int extra)    {
                logger.writeLog(LogType.INFO, TAG,"MediaRecorder onInfo:" + what);
                if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_FILESIZE_APPROACHING) {
                    File mediaStorageDir = new File( getExternalFilesDir(Environment.getDataDirectory().getAbsolutePath()).getAbsolutePath()
                            , "data");
                    if (!mediaStorageDir.exists()) {
                        mediaStorageDir.mkdirs();
                    }
                    String mediaFilePath = mediaStorageDir.getPath() + File.separator +
                            patientId + "_" + dateString +"_" + counter + ".mp4";
                    counter++;
                    File mediaFile = new File(mediaFilePath);

                    Log.e("RecordActivity", mediaFilePath);
                    try {
                        RandomAccessFile f = new RandomAccessFile(mediaFile, "rw");
                        try {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                mediaRecorder.setNextOutputFile(f.getFD());
                            }

                        } finally {
                            f.close();
                        }
                    } catch (Exception ex) {
                        logger.writeLog(LogType.ERROR, TAG,"RecordActivity :" + ex);
                    }
                }
            }

        });
        try {

            //set orientaion angle to display the recorded video.
            // 270 - for portrait, 0 - for landscape.

            //if not left handed - normal landscape orientation.
            if(!LEFT_HAND_KEYWORD.equalsIgnoreCase(patient.getDominantHand())) {
                mediaRecorder.setOrientationHint(LANDSCAPE_CAMERA_ORIENTATION);
            }
            else{
                mediaRecorder.setOrientationHint(REVERSE_LANDSCAPE_CAMERA_ORIENTATION);
            }
            //prepare mediaRecorder.
            mediaRecorder.prepare();
        } catch (Exception e) {
            logger.writeLog(LogType.ERROR, TAG, "surfaceCreated. Camera failed to open: " + e.getLocalizedMessage());
        }
        // Start recording in background.
        mediaRecorder.start();
        logger.writeLog(LogType.INFO, TAG, "surfaceCreated. MediaRecorder started recording.");
    }

    /**
     * This method is used when stopService is called upon this service.
     * It Stops recording, releases mediaRecorder, releases the
     * use of the camera and removes the SurfaceView.
     */
    @Override
    public void onDestroy() {
        mediaRecorder.stop();
        mediaRecorder.reset();
        mediaRecorder.release();

        camera.lock();
        camera.release();
        logger.writeLog(LogType.INFO, TAG,
                "onDestroy. MediaRecorder stopped recording. Camera released.");
        windowManager.removeView(surfaceView);
        logger.writeLog(LogType.INFO, TAG,
                "onDestroy. WindowManager removed view.");
        super.onDestroy();
    }
    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int format, int width, int height) {}

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {}

    @Override
    public IBinder onBind(Intent intent) { return null; }

}