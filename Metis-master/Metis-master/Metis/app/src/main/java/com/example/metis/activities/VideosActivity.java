package com.example.metis.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.core.widget.NestedScrollView;

import android.content.DialogInterface;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.example.metis.utils.Consts.*;

/**
 * VideosActivity class is responsible for showing available
 * video series for the patient, and allowing to choose between them.
 */
public class VideosActivity extends AppCompatActivity
        implements
        GestureDetector.OnGestureListener {

    private String TAG = this.getClass().getSimpleName();

    private File touchEventsFile;
    private GestureDetector gestureDetector;

    private Logger logger = Logger.getInstance(APP_FILES_PATH);

    DialogInterface.OnClickListener errorListener;

    private String DEFAULT_IMAGE_PATH = "default_image/";
    private Patient patient;

    /***
     * in this function there is a list of video series activities,
     * and a list of the paths to each series activity.
     * if you want to add a series to the application:
     *      1. Add an folder 'series' to files.
     *      2. Add a folder 'seriesName' to files->series.
     *      3. Add both 'images' and 'videos' to files->series->seriesName.
     *      4. Add jpg images of the episodes and fitting mp4 with same name under 'images' and 'videos'.
     * @param savedInstanceState saved instance of the intent.
     */
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
        final String filesPath = APP_FILES_PATH + "";

        touchEventsFile = (File) getIntent().getSerializableExtra(TOUCH_EVENTS_FILE_PATH);
        gestureDetector = new GestureDetector(this, this);
        patient = (Patient) getIntent().getSerializableExtra(PATIENT_KEYWORD);

        try {
            //initialize the path to the image of the default empty image.
            DEFAULT_IMAGE_PATH = filesPath + "/" + DEFAULT_IMAGE_PATH;
            DEFAULT_IMAGE_PATH = DEFAULT_IMAGE_PATH + getSeriesEpisodesNames(DEFAULT_IMAGE_PATH, true).get(0);
        } catch (IndexOutOfBoundsException e){
            logger.writeLog(LogType.ERROR, TAG, "onCreate. There is no 'default_image' folder or there are no images in this folder.");

                ApplicationDataManager.getErrorMessageDialog(this,
                        "There is no 'default_image' folder or there are no images in this folder.", errorListener);

        } catch (Exception e){
            logger.writeLog(LogType.ERROR, TAG, "onCreate. An error has occurred with videos images.");

                ApplicationDataManager.getErrorMessageDialog(this,
                        "An error has occurred. Try playing games instead.", errorListener);

        }
        LinearLayout scrollAndBackLinearLayout = new LinearLayout(this);
        scrollAndBackLinearLayout.setOrientation(LinearLayout.VERTICAL);
        scrollAndBackLinearLayout.setGravity(Gravity.CENTER);

        //get resources and displayMetrics
        Resources r = getResources();
        DisplayMetrics displayMetrics = r.getDisplayMetrics();
        //init the root view as scrollView and set params
        NestedScrollView rootView = new NestedScrollView(this);
        rootView.setBackground(getDrawable(R.drawable.background_gradient));
//        NestedScrollView.LayoutParams lp = new NestedScrollView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        //declare frame dimensions.
        final float DP_VID_IMG_PADDING = r.getDimension(R.dimen.video_image_padding) / displayMetrics.density;
        final float DP_VID_IMG_MRGIN_IN = r.getDimension(R.dimen.video_image_margin_in) / displayMetrics.density;
        final float DP_VID_IMG_MRGIN_OUT = r.getDimension(R.dimen.video_image_margin_out) / displayMetrics.density;

        //get reference of this activity
        AppCompatActivity VideosActivityReference = this;

        //get series from intent
        ArrayList<String> vidsDirNames = getIntent().getStringArrayListExtra(SERIES_KEYWORD);

        //handle empty videos folder.
        if (vidsDirNames == null || vidsDirNames.isEmpty()) {

            logger.writeLog(LogType.ERROR, TAG, "onCreate. There are no available videos to display in the specified folder.");

                ApplicationDataManager.getErrorMessageDialog(this,
                        "There are no available videos to display in the specified folder.", errorListener);

        } else {
            //get for each series it's imgs and vids path.
            ArrayList<String> imgsDirsFullPath = new ArrayList<>();
            ArrayList<String> vidsDirsFullPath = new ArrayList<>();
            for (String dir : vidsDirNames) {
                vidsDirsFullPath.add(filesPath + "/" + SERIES_KEYWORD + "/" + dir + "/" + VIDEOS_KEYWORD);
                imgsDirsFullPath.add(filesPath + "/" + SERIES_KEYWORD + "/" + dir + "/" + IMAGES_KEYWORD);
            }
            //folderImgsList initialization
            ArrayList<String> folderImgsList = createFolderImgsList(imgsDirsFullPath);

            ArrayList<TableRow> rows = new ArrayList<>();
            //set dimensions in pixels for margins
            int paddingPx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, DP_VID_IMG_PADDING, displayMetrics);
            int mrginInPx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, DP_VID_IMG_MRGIN_IN, displayMetrics);
            int mrginOutPx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, DP_VID_IMG_MRGIN_OUT, displayMetrics);
            //create TableLayout with shrink/strecth all columns properties.
            TableLayout table = new TableLayout(this);
            table.setShrinkAllColumns(true);
            //create new intent that will move to the VideosEpisodes activity.
            Intent intent = new Intent(this, VideosEpisodes.class);
            intent.putExtra(TOUCH_EVENTS_FILE_PATH, touchEventsFile);
            intent.putExtra(PATIENT_KEYWORD, patient);
            //for every folder in the list.
            for (int foldNum = 0; foldNum < vidsDirNames.size(); ++foldNum) {
                if (foldNum % FRAMES_PER_ROW == 0) {
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
                Margins margins = new Margins(foldNum, vidsDirNames.size(), FRAMES_PER_ROW, mrginOutPx, mrginInPx);

                //set layout params and margins.
                TableRow.LayoutParams trlp = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT);
                trlp.setMargins(margins.getLeft(), margins.getTop(), margins.getRight(), margins.getBottom());
                linear.setLayoutParams(trlp);

                //set image resource and scaling.
                Bitmap bitmap = BitmapFactory.decodeFile(folderImgsList.get(foldNum));
                Bitmap resized = Bitmap.createScaledBitmap(bitmap, IMAGE_SIZE, IMAGE_SIZE, true);
                btn.setImageBitmap(resized);
                btn.setScaleType(ImageView.ScaleType.CENTER_CROP);
                btn.setAdjustViewBounds(true);


                //set button border.
                btn.setBackground(getDrawable(R.drawable.border));
                //set button padding.
                btn.setPadding(paddingPx, paddingPx, paddingPx, paddingPx);
                //set button id.
                btn.setId(foldNum);

                //set the button tag (series name).
                btn.setTag(vidsDirNames.get(foldNum));

                //set button OnClick and OnTouch.
                setOnClickAndTouch(btn, intent, vidsDirsFullPath.get(foldNum), imgsDirsFullPath.get(foldNum));

                //create video text.
                TextView text = new TextView(this);
                text.setText(vidsDirNames.get(foldNum));
                text.setTextSize(IMAGE_BTN_TEXT_SIZE);
                text.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                text.setBackground(getDrawable(R.drawable.border));

                //add btn to layout.
                linear.addView(btn);
                //add text to layout.
                linear.addView(text);
                //add frame to row.
                rows.get(foldNum / FRAMES_PER_ROW).addView(linear);

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
                    logger.writeLog(LogType.INFO, TAG, "onTouch. Touch event in VideosActivity.");
                    rootView.onTouchEvent(event);
                    VideosActivityReference.onTouchEvent(event);
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
            //set screen orientation.
            ApplicationDataManager.setOrientation(patient, this);
        }

    }

    /**
     * This method is used in order to get the images from each series folder.
     * @param imgsDirsFullPath full path for the images directory.
     * @return list of images for the videos.
     */
    private ArrayList<String> createFolderImgsList(ArrayList<String> imgsDirsFullPath) {
        logger.writeLog(LogType.DEBUG, TAG, "Create image list.");
        ArrayList<String> folderImgsList = new ArrayList<>();
        for (String path : imgsDirsFullPath) {
            List<String> names = getSeriesEpisodesNames(path, false);
            folderImgsList.add(names.isEmpty() ? DEFAULT_IMAGE_PATH : names.get(0));
        }
        return folderImgsList;
    }


    /**This method is used in order to get all videos
     * and frames names when series are chosen.
     * @param path
     * @param onlyNames
     * @return
     */
    private List<String> getSeriesEpisodesNames(String path, boolean onlyNames) {
        logger.writeLog(LogType.DEBUG, TAG, "Get all videos and frames names.");
        File[] folder = new File(path).listFiles();
        //if the folder is empty add an ugly fix
        if (folder == null || folder.length <= 0) {
            return new ArrayList<>();
        }
        return Arrays.stream(folder).map(f -> onlyNames ? f.getName() : path + "/" + f.getName()).collect(Collectors.toList());
    }

    /**
     * This method is used in order to set OnClick and OnTouch callbacks with parameters.
     * @param btn The button which the callbacks will be assigned to.
     * @param intent The intent of the video series.
     */
    private void setOnClickAndTouch(final AppCompatImageButton btn, final Intent intent,
                            final String vidsFullPath,
                            final String imgsFullPath
    ) {
        final ArrayList<String> vidsList = (ArrayList<String>) getSeriesEpisodesNames(vidsFullPath, false);
        //sort vidsList to match the output of images.

        Collections.sort(vidsList);
        ArrayList<String> framesList = (ArrayList<String>) getSeriesEpisodesNames(imgsFullPath, false);
        //get names without file extentions.
        final ArrayList<String> namesList = (ArrayList<String>) getSeriesEpisodesNames(vidsFullPath, true)
                .stream().map(name -> name.substring(0,name.indexOf('.'))).collect(Collectors.toList());

        ArrayList<String> imageNames = (ArrayList<String>) getSeriesEpisodesNames(imgsFullPath, true)
                .stream().map(name -> name.substring(0,name.indexOf('.'))).collect(Collectors.toList());

        final ArrayList<String> newImageNames = fillImageNames(framesList, namesList, imageNames);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logger.writeLog(LogType.INFO, TAG, "onClick. Series chosen: " + (String)v.getTag());
                intent.putStringArrayListExtra(VIDEOS_KEYWORD, vidsList);
                intent.putStringArrayListExtra(IMAGES_KEYWORD, newImageNames);
                intent.putStringArrayListExtra(NAMES_KEYWORD, namesList);
                startActivity(intent);
            }
        });

        btn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    EventWriter.writeString("Event,Series chosen: " + (String)v.getTag() +
                            ",x:" + event.getX() + ",y:" + event.getY(), touchEventsFile, TAG);
                    EventWriter.writeEvent(event, touchEventsFile, TAG, true);
                    gestureDetector.onTouchEvent(event);
                    return true;
                } if(event.getAction() == MotionEvent.ACTION_UP && !(event.getAction() == MotionEvent.ACTION_SCROLL)) {
                    v.performClick();
                    return true;
                }
                return false;
            }
        });
    }

    /**
     * This function match images and names, if there is no image enter the name of the
     * default image.
     * @param framesList list of frames.
     * @param namesList list of image names.
     * @param imageNames list of names.
     * @return list of the name of the images.
     */
    private ArrayList<String> fillImageNames(ArrayList<String> framesList,
                                             ArrayList<String> namesList,
                                             ArrayList<String> imageNames) {
        ArrayList<String> newList = new ArrayList<>();
        //sort before comparing lists.
        Collections.sort(namesList);
        Collections.sort(imageNames);
        Collections.sort(framesList);
        for (int i = 0, j = 0; i < namesList.size(); i++) {
            if (j >= imageNames.size()) {
                newList.add(DEFAULT_IMAGE_PATH);
                continue;
            }
            //if not equals add a default image.
            if (!namesList.get(i).equals(imageNames.get(j))) {
                newList.add(DEFAULT_IMAGE_PATH);
            } else {
                newList.add(framesList.get(j));
                j++;
            }
        }
        return newList;
    }

    /**
     * This method is invoked when an unrelated touch occurred
     * @param event motion event of the touch.
     * @return boolean.
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        logger.writeLog(LogType.INFO, TAG, "onTouchEvent. event: " + event);
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
