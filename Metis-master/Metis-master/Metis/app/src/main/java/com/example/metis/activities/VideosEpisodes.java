package com.example.metis.activities;


import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.core.widget.NestedScrollView;

import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.example.metis.data_types.Margins;
import com.example.metis.R;
import com.example.metis.data_types.Patient;
import com.example.metis.utils.ApplicationDataManager;
import com.example.metis.utils.EventWriter;
import com.example.metis.utils.Logger;

import java.io.File;
import java.util.ArrayList;

import static com.example.metis.utils.Consts.*;

/**
 * VideosEpisodes class is responsible for showing available
 * episodes of the chosen series for the patient, and
 * allowing to choose between them.
 */
public class VideosEpisodes extends AppCompatActivity
        implements
        GestureDetector.OnGestureListener {

    private String TAG = this.getClass().getSimpleName();

    private Logger logger = Logger.getInstance(APP_FILES_PATH);

    private File touchEventsFile;
    private GestureDetector gestureDetector;
    DialogInterface.OnClickListener errorListener;
    private Patient patient;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        ApplicationDataManager.fullScreenView(getWindow());
        errorListener =  new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        };
        touchEventsFile = (File) getIntent().getSerializableExtra(TOUCH_EVENTS_FILE_PATH);
        gestureDetector = new GestureDetector(this, this);
        patient = (Patient) getIntent().getSerializableExtra(PATIENT_KEYWORD);

        LinearLayout scrollAndBackLinearLayout = new LinearLayout(this);
        scrollAndBackLinearLayout.setOrientation(LinearLayout.VERTICAL);
        scrollAndBackLinearLayout.setGravity(Gravity.CENTER);

        //get resources and displayMetrics
        Resources r = getResources();
        DisplayMetrics displayMetrics = r.getDisplayMetrics();
        //init the root view as scrollView and set params
        NestedScrollView rootView = new NestedScrollView(this);
        rootView.setBackground(getDrawable(R.drawable.background_gradient));
        NestedScrollView.LayoutParams lp = new NestedScrollView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        //declare frame dimensions.
        final float DP_VID_IMG_PADDING = r.getDimension(R.dimen.video_image_padding) / displayMetrics.density;
        final float DP_VID_IMG_MRGIN_IN = r.getDimension(R.dimen.video_image_margin_in) / displayMetrics.density;
        final float DP_VID_IMG_MRGIN_OUT = r.getDimension(R.dimen.video_image_margin_out) / displayMetrics.density;

        //get reference of this activity
        AppCompatActivity VideosEpisodesActivityReference = this;

        //get videos/frames/names
        ArrayList<String> vidList = getIntent().getStringArrayListExtra(VIDEOS_KEYWORD);
        ArrayList<String> frameList = getIntent().getStringArrayListExtra(IMAGES_KEYWORD);
        ArrayList<String> vidNames = getIntent().getStringArrayListExtra(NAMES_KEYWORD);


        //if the input arrays are null or empty
        if (vidList == null || vidList.size() <= 0 || frameList == null || frameList.size() <= 0 || vidNames == null || vidNames.size() <= 0) {
            logger.writeLog(LogType.ERROR, TAG, "onCreate. There are no available videos or images to display in the specified folder.");
            //alert that there are no videos
            setContentView(R.layout.activity_videos);
            ApplicationDataManager.setOrientation(patient, this);

                ApplicationDataManager.getErrorMessageDialog(this,
                        "There are no available videos or images to display in the specified folder.", errorListener);

            // input is legitimate, build the activity
        } else {
            logger.writeLog(LogType.INFO, TAG, "onCreate. There are available videos and images for the series.");
            //get the number of videos.
            int numOfVids = vidList.size();
            ArrayList<TableRow> rows = new ArrayList<>();
            //set dimensions in pixels for margins
            int paddingPx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, DP_VID_IMG_PADDING, displayMetrics);
            int mrginInPx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, DP_VID_IMG_MRGIN_IN, displayMetrics);
            int mrginOutPx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, DP_VID_IMG_MRGIN_OUT, displayMetrics);
            //create TableLayout with shrink/strecth all columns properties.
            TableLayout table = new TableLayout(this);
            table.setShrinkAllColumns(true);
            //create new intent that will move to the Video activity.
            Intent intent = new Intent(this, Video.class);
            //for every video in the list.
            for (int vidNum = 0; vidNum < numOfVids; ++vidNum) {
                if (vidNum % FRAMES_PER_ROW == 0) {
                    TableRow row = new TableRow(this);
                    row.setGravity(Gravity.CENTER);
                    rows.add(row);
                }
                LinearLayout linear = new LinearLayout(this);
                linear.setOrientation(LinearLayout.VERTICAL);
                linear.setGravity(Gravity.CENTER);
                //create image button.
                AppCompatImageButton btn = new AppCompatImageButton(this);
                //get the margins.
                Margins margins = new Margins(vidNum, numOfVids, FRAMES_PER_ROW, mrginOutPx, mrginInPx);

                //set layout params and margins.
                TableRow.LayoutParams trlp = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT);
                trlp.setMargins(margins.getLeft(), margins.getTop(), margins.getRight(), margins.getBottom());
                linear.setLayoutParams(trlp);

                //set image resource and scaling.
                Bitmap bitmap = BitmapFactory.decodeFile(frameList.get(vidNum));
                Bitmap resized = Bitmap.createScaledBitmap(bitmap, IMAGE_SIZE, IMAGE_SIZE, true);
                btn.setImageBitmap(resized);
                btn.setScaleType(ImageView.ScaleType.CENTER_CROP);
                btn.setAdjustViewBounds(true);


                //set button border.
                btn.setBackground(getDrawable(R.drawable.border));
                //set button padding.
                btn.setPadding(paddingPx, paddingPx, paddingPx, paddingPx);
                //set button id.
                btn.setId(vidNum);
                //set the button tag (episode name).
                btn.setTag(vidNames.get(vidNum));

                //set button onClick to make it play the video.
                setOnClickAndTouch(btn, intent, vidList.get(vidNum));

                //create video text.
                TextView text = new TextView(this);
                text.setText(vidNames.get(vidNum));
                text.setTextSize(IMAGE_BTN_TEXT_SIZE);
                text.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                text.setBackground(getDrawable(R.drawable.border));

                //add btn to layout.
                linear.addView(btn);
                //add text to layout.
                linear.addView(text);
                //add frame to row.
                rows.get(vidNum / FRAMES_PER_ROW).addView(linear);

            }
            //for every row that was created add it to TableLayout
            for (TableRow row : rows) {
                table.addView(row);
            }
            //add TableLayout to the NestedScrollingView and set the content view.
            rootView.addView(table);
            //set rootView onTouchListener so that it would call onTouchEvent of this activity.
            rootView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    logger.writeLog(LogType.INFO, TAG, "onTouch. Touch event in VideosEpisodes.");
                    rootView.onTouchEvent(event);
                    VideosEpisodesActivityReference.onTouchEvent(event);
                    return true;
                }
            });

            LinearLayout linearLayout = new LinearLayout(this);
            //create image button.
            AppCompatImageButton backButton = new AppCompatImageButton(this);
            //set button image.
            backButton.setBackground(getDrawable(R.drawable.back_button));
            backButton.setMaxHeight(10);
            backButton.setMaxWidth(10);
            backButton.setLayoutParams(new ViewGroup.LayoutParams(150,150));

            View view = new View(this);
            linearLayout.addView(backButton);
            linearLayout.addView(view);

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

            rootView.setLayoutParams(new NestedScrollView.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT));
            scrollAndBackLinearLayout.addView(rootView);

            linearLayout.setLayoutParams(new LinearLayout.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT));
            scrollAndBackLinearLayout.addView(linearLayout);

            scrollAndBackLinearLayout.setBackground(getDrawable(R.drawable.background_gradient));
            setContentView(scrollAndBackLinearLayout);
            ApplicationDataManager.setOrientation(patient, this);
        }
    }

    //function to set OnClick callback with parameters.
    private void setOnClickAndTouch(final AppCompatImageButton btn, final Intent intent, final String vidPath) {
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logger.writeLog(LogType.INFO, TAG, "onClick. Episode chosen: " + (String)v.getTag());
                intent.putExtra(PATH_TO_VIDEO_KEYWORD, vidPath);
                intent.putExtra(TOUCH_EVENTS_FILE_PATH, touchEventsFile);
                intent.putExtra(PATIENT_KEYWORD, patient);
                startActivity(intent);
            }
        });
        btn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    EventWriter.writeString("Event,Episode chosen: " + (String)v.getTag() +
                            ",x:" + event.getX() + ",y:" + event.getY(), touchEventsFile, TAG);
                    EventWriter.writeEvent(event, touchEventsFile, TAG, true);
                    gestureDetector.onTouchEvent(event);
                    return true;
                }
                if(event.getAction() == MotionEvent.ACTION_UP && !(event.getAction() == MotionEvent.ACTION_SCROLL)) {
                    v.performClick();
                    return true;
                }
                return false;
            }
        });
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
}