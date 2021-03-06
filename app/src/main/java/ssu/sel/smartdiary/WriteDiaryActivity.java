package ssu.sel.smartdiary;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Debug;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.VideoView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Calendar;

import ssu.sel.smartdiary.model.DiaryEnvContext;
import ssu.sel.smartdiary.model.MediaContext;
import ssu.sel.smartdiary.model.UserProfile;
import ssu.sel.smartdiary.network.DiaryUploadRestConnector;
import ssu.sel.smartdiary.speech.WavRecorder;
import ssu.sel.smartdiary.view.AudioPlayerView;
import ssu.sel.smartdiary.view.RemovableView;

public class WriteDiaryActivity extends AppCompatActivity {
    private final static int REQUEST_READ_EXTERNAL_STORAGE = 1;
    private final static int REQUEST_CAMERA = 2;

    protected static final int ACTIVITY_REQ_CODE_PICTURE = 100;
    protected static final int ACTIVITY_REQ_CODE_CAMERA = 101;
    protected static final int ACTIVITY_REQ_CODE_VIDEO = 102;
    protected static final int ACTIVITY_REQ_CODE_MUSIC = 103;

    protected Uri lastCapturedImage = null;

    protected String diaryAcitivityType = null;

    protected EditText edtTitle = null;
    protected EditText edtContent = null;
    protected TextView tvDiarySelectDate = null;
    protected TextView tvDiarySelectTime = null;

    protected EditText edtAnnotation = null;
    protected EditText edtEnvPlace = null;
    protected EditText edtEnvWeather = null;
    protected EditText edtEnvHolidays = null;
    protected EditText edtEnvEvents = null;

    protected ScrollView viewWriteDiaryLayout = null;
    protected View viewProgress = null;

    protected File diaryAudioFile = null;
    protected AudioPlayerView diaryRecordAudioPlayer = null;

    protected LinearLayout layoutAttachmentFiles = null;

    protected AlertDialog dlgAlert = null;
    protected AlertDialog dlgCancel = null;
    protected AlertDialog dlgConfirm = null;
    protected Calendar selectedDate;
    protected DatePickerDialog dlgDatePicker = null;
    protected TimePickerDialog dlgTimePicker = null;

//    protected ArrayList<MediaController> mediaControllerList = new ArrayList<>();
    protected ArrayList<RemovableView> removableViewList = new ArrayList<>();
    protected ArrayList<MediaContext> mediaContextList = new ArrayList<>();

    protected DiaryUploadRestConnector saveDiaryConnector = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();

        if (PackageManager.PERMISSION_GRANTED ==
                ActivityCompat.checkSelfPermission(getApplicationContext(),
                        Manifest.permission.READ_EXTERNAL_STORAGE)) {
            //It have permission
        } else {
            requestPermissions(this, REQUEST_READ_EXTERNAL_STORAGE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (diaryRecordAudioPlayer != null) {
            diaryRecordAudioPlayer.remove();
            diaryRecordAudioPlayer = null;
        }

        for (RemovableView removableView : removableViewList) {
            removableView.remove();
        }
    }

    protected void initView() {
        ActionBar actionBar = getSupportActionBar();
        View mActionBarView = getLayoutInflater().inflate(R.layout.action_bar_write_diary, null);
        actionBar.setCustomView(mActionBarView,
                new ActionBar.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT,
                        ActionBar.LayoutParams.MATCH_PARENT, Gravity.CENTER));

        Intent intent = getIntent();
        diaryAcitivityType = intent.getStringExtra("WRITE_DIARY_TYPE");
        if (diaryAcitivityType.equals("NEW_TEXT"))
            ((TextView) mActionBarView.findViewById(R.id.tvActionBarTitle)).setText("New Text Diary");
        else if (diaryAcitivityType.equals("NEW_AUDIO")) {
            ((TextView) mActionBarView.findViewById(R.id.tvActionBarTitle)).setText("New Audio Diary");
            diaryAudioFile = (File)intent.getSerializableExtra("DIARY_AUDIO");
        }
        else
            throw new NullPointerException("No Intent for WriteDiaryType!");
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(false);

        setContentView(R.layout.activity_write_diary);

        edtTitle = (EditText) findViewById(R.id.edtDiaryTitle);
        edtContent = (EditText) findViewById(R.id.edtDiaryContent);
        tvDiarySelectDate = (TextView) findViewById(R.id.tvDiarySelectDate);
        tvDiarySelectTime = (TextView) findViewById(R.id.tvDiarySelectTime);
        edtAnnotation = (EditText) findViewById(R.id.edtAnnotation);
        edtEnvPlace = (EditText) findViewById(R.id.edtEnvPlace);
        edtEnvWeather = (EditText) findViewById(R.id.edtEnvWeather);
        edtEnvHolidays = (EditText) findViewById(R.id.edtHolidays);
        edtEnvEvents = (EditText) findViewById(R.id.edtEnvEvents);
        viewWriteDiaryLayout = (ScrollView) findViewById(R.id.viewWriteDiaryForm);
        viewProgress = findViewById(R.id.progressLayout);
        diaryRecordAudioPlayer = (AudioPlayerView) findViewById(R.id.audioPlayerDiaryRecord);
        layoutAttachmentFiles = (LinearLayout) findViewById(R.id.layoutAttachmentFiles);

        Drawable edtTitleBGDrawble = edtTitle.getBackground();
        edtTitleBGDrawble.mutate().setColorFilter(getResources().getColor(R.color.indigo_500),
                PorterDuff.Mode.SRC_ATOP);
        edtTitle.setBackground(edtTitleBGDrawble);
        edtTitle.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    edtTitle.setError(null);
                    String title = edtTitle.getText().toString();

                    if (TextUtils.isEmpty(title)) {
                        edtTitle.setError(getString(R.string.error_field_required));
                    }
                }
            }
        });
        edtContent.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    edtContent.setError(null);
                    String content = edtContent.getText().toString();

                    if (TextUtils.isEmpty(content)) {
                        edtContent.setError(getString(R.string.error_field_required));
                    }
                }
            }
        });

        if (diaryAcitivityType.equals("NEW_AUDIO")) {
//            edtTitle.setText(intent.getStringExtra("DIARY_TITLE"));
            edtContent.setText(intent.getStringExtra("DIARY_CONTENT"));
//            selectedDate = (Calendar) intent.getSerializableExtra("DIARY_DATE");
            selectedDate = Calendar.getInstance();
        } else {
            selectedDate = Calendar.getInstance();
        }
        setModals();

        setJsonConnectors();

        diaryRecordAudioPlayer.setDiaryAudioName(this);
        boolean diaryAudioSet = diaryRecordAudioPlayer.setAudio(
                "Recorded Diary Audio", diaryAudioFile,
                new AudioPlayerView.OnLoadedListener() {
            @Override
            public void onLoaded() {

            }
        });
        if (!diaryAudioSet) {
            openAlertModal("Diary Record Audio File load Failed.");
        }
    }

    protected void setModals() {
        tvDiarySelectDate.setText(GlobalUtils.DIARY_DATE_FORMAT.format(selectedDate.getTime()));
        tvDiarySelectTime.setText(GlobalUtils.DIARY_TIME_FORMAT.format(selectedDate.getTime()));
        dlgDatePicker = new DatePickerDialog(this, datePickerListener,
                selectedDate.get(Calendar.YEAR), selectedDate.get(Calendar.MONTH),
                selectedDate.get(Calendar.DAY_OF_MONTH));
        dlgTimePicker = new TimePickerDialog(this, timePickerListener,
                selectedDate.get(Calendar.HOUR_OF_DAY), selectedDate.get(Calendar.SECOND), true);


        dlgAlert = new AlertDialog.Builder(this).setMessage("Message")
                .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                }).create();

        dlgCancel = new AlertDialog.Builder(this).setMessage("Are you sure to cancel?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (diaryAcitivityType.equals("NEW_AUDIO")) {
                            WavRecorder.removeRecordedTempFiles();
                        }

                        dialogInterface.dismiss();
                        WriteDiaryActivity.this.finish();
                    }
                }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                }).create();

        dlgConfirm = new AlertDialog.Builder(this).setMessage("Are you sure to confirm?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        saveDiary();
                    }
                }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                }).create();
    }

    protected void setJsonConnectors() {
        saveDiaryConnector = new DiaryUploadRestConnector("diary", "POST",
                new DiaryUploadRestConnector.OnConnectListener() {
                    @Override
                    public void onDone(JSONObject resJson) {
                        if (resJson != null) {
                            Log.d("WriteDiary - Json", resJson.toString());
                            try {
                                boolean success = resJson.getBoolean("create_diary");
                                if (success) {
                                    int diaryId = resJson.getInt("audio_diary_id");
                                    if (diaryAcitivityType.equals("NEW_AUDIO")) {
                                        WavRecorder.removeRecordedTempFiles();
                                    }

                                    //Copy the temp file to diary audio cache file
                                    if(diaryAudioFile != null) {
                                        try {
                                            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(diaryAudioFile));
                                            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(
                                                    GlobalUtils.getDiaryFile(
                                                            UserProfile.getUserProfile().getUserID(), diaryId)));

                                            byte[] buf = new byte [1024*10];
                                            int byteLen;
                                            while ((byteLen = bis.read(buf)) > 0) {
                                                bos.write(buf, 0 ,byteLen);
                                            }
                                            bis.close();
                                            bos.close();

                                        } catch (Exception e) {
                                            e.printStackTrace();
                                            Log.d("WriteDiaryAcitivity", "Audio Copy Failed");
                                            try {
                                                GlobalUtils.getDiaryFile(
                                                        UserProfile.getUserProfile().getUserID(), diaryId)
                                                                .delete();
                                            } catch (Exception e2) {}
                                        }
                                    }

                                    if(mediaContextList.size() > 0) {
                                        for (MediaContext mediaContext : mediaContextList) {
                                            File mediaFile = mediaContext.getFile();
                                            try {
                                                BufferedInputStream bis = new BufferedInputStream(new FileInputStream(mediaFile));
                                                BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(
                                                        GlobalUtils.getDiaryMediaContext(
                                                                UserProfile.getUserProfile().getUserID(), diaryId, mediaFile.getName())));

                                                byte[] buf = new byte [1024*100];
                                                int byteLen;
                                                while ((byteLen = bis.read(buf)) > 0) {
                                                    bos.write(buf, 0 ,byteLen);
                                                }
                                                bis.close();
                                                bos.close();
                                            } catch (Exception e) {
                                                try {
                                                    GlobalUtils.getDiaryMediaContext(
                                                            UserProfile.getUserProfile().getUserID(), diaryId, mediaFile.getName())
                                                            .delete();
                                                } catch (Exception e2) {}
                                            }
                                        }
                                    }

                                    WriteDiaryActivity.this.finish();
                                } else {
                                    openAlertModal("Diary Create Failed.\n Please Retry Again.");
                                }

                            } catch (Exception e) {
                                e.printStackTrace();
                                Log.d("Main - Json", "Json parsing error");
                                openAlertModal("Json parsing error.", "Error");
                            }
                        } else {
                            Log.d("WriteDiary - Json", "No response");
                            openAlertModal("There is no reponse...");
                        }

                        showProgress(false);
                    }
                });
    }

    protected void openAlertModal(CharSequence msg) {
        openAlertModal(msg, "Alert");
    }

    protected void openAlertModal(CharSequence msg, CharSequence title) {
        dlgAlert.setTitle(title);
        dlgAlert.setMessage(msg);
        dlgAlert.show();
    }

    protected void saveDiary() {
        String title = edtTitle.getText().toString();
        String content = edtContent.getText().toString();

        boolean cancel = false;
        View focusView = null;

        if (TextUtils.isEmpty(content)) {
            edtContent.setError(getString(R.string.error_field_required));
            focusView = edtContent;
            cancel = true;
        }

        if (TextUtils.isEmpty(title)) {
            edtTitle.setError(getString(R.string.error_field_required));
            focusView = edtTitle;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();

        } else {
            String annotation = edtAnnotation.getText().toString();
            String envPlace = edtEnvPlace.getText().toString();
            String envWeather = edtEnvWeather.getText().toString();
            String envEvents = edtEnvEvents.getText().toString();
            String envHolidays = edtEnvHolidays.getText().toString();
            long createdTime = selectedDate.getTime().getTime();

            JSONObject json = new JSONObject();
            try {
                json.put("user_id", UserProfile.getUserProfile().getUserID());
                json.put("created_date", createdTime);
                json.put("title", title);
                json.put("content", content);

                JSONArray arrTags = new JSONArray();
                if (!TextUtils.isEmpty(annotation)) {
                    String[] tags = annotation.split(",");
                    for (String tag : tags) {
                        JSONObject jsonTag = new JSONObject();
                        jsonTag.put("value", tag.trim());
                        arrTags.put(jsonTag);
                    }
                }
                json.put("tag", arrTags);

                JSONArray arrEnvContexts = new JSONArray();
                if (!TextUtils.isEmpty(envPlace)) {
                    String[] places = envPlace.split(",");
                    for (String place : places) {
                        JSONObject jsonPlace = new JSONObject();
                        jsonPlace.put("type", DiaryEnvContext.TYPE_ENV_PLACE);
                        jsonPlace.put("value", place.trim());
                        arrEnvContexts.put(jsonPlace);
                    }
                }
                if (!TextUtils.isEmpty(envWeather)) {
                    String[] weathers = envWeather.split(",");
                    for (String weather : weathers) {
                        JSONObject jsonWeather = new JSONObject();
                        jsonWeather.put("type", DiaryEnvContext.TYPE_ENV_WEATHER);
                        jsonWeather.put("value", weather.trim());
                        arrEnvContexts.put(jsonWeather);
                    }
                }
                if (!TextUtils.isEmpty(envEvents)) {
                    String[] events = envEvents.split(",");
                    for (String event : events) {
                        JSONObject jsonEvent = new JSONObject();
                        jsonEvent.put("type", DiaryEnvContext.TYPE_ENV_EVENT);
                        jsonEvent.put("value", event.trim());
                        arrEnvContexts.put(jsonEvent);
                    }
                }
                if (!TextUtils.isEmpty(envHolidays)) {
                    String[] holidays = envHolidays.split(",");
                    for (String holiday : holidays) {
                        JSONObject jsonHoliday = new JSONObject();
                        jsonHoliday.put("type", DiaryEnvContext.TYPE_ENV_HOLIDAY);
                        jsonHoliday.put("value", holiday.trim());
                        arrEnvContexts.put(jsonHoliday);
                    }
                }
                json.put("environmental_context", arrEnvContexts);

            } catch (Exception e) {
                e.printStackTrace();
                dlgAlert.setMessage("JSON Creation Error");
                dlgAlert.show();
                return;
            }

            showProgress(true);
            if (diaryAcitivityType.equals("NEW_AUDIO")) {
                File recordedFile = (File) getIntent().getSerializableExtra("DIARY_AUDIO");

                // URI is not serializable, so mediacontextlist cannot add for extra
//                ArrayList<Integer> mediaTypeList = new ArrayList<>(mediaContextList.size());
//                ArrayList<File> fileList = new ArrayList<>(mediaContextList.size());
//                for (MediaContext mediaContext : mediaContextList) {
//                    mediaTypeList.add(mediaContext.getMediaType());
//                    fileList.add(mediaContext.getFile());
//                }
//
//                Intent i = new Intent(getApplicationContext(), DiaryUploadService.class);
//                i.putExtra(DiaryUploadService.EXTRA_NAME_DIARY_TITLE, title);
//                i.putExtra(DiaryUploadService.EXTRA_NAME_RECORDED_FILE, recordedFile);
//                i.putExtra(DiaryUploadService.EXTRA_NAME_RECORDED_TEMP_FILE, diaryAudioFile);
////                i.putExtra(DiaryUploadService.EXTRA_NAME_MEDIA_CONTEXTS, mediaContextList);
//                i.putExtra(DiaryUploadService.EXTRA_NAME_MEDIA_CONTEXTS_TYPES, mediaTypeList);
//                i.putExtra(DiaryUploadService.EXTRA_NAME_MEDIA_CONTEXTS_FILES, fileList);
//                i.putExtra(DiaryUploadService.EXTRA_NAME_REQUEST_JSON, json.toString());
//                startService(i);

                saveDiaryConnector.request(recordedFile, mediaContextList, json);
            } else {
                saveDiaryConnector.request(null, null, json);
//                openAlertModal("ERROR");
            }
//            WriteDiaryActivity.this.finish();
        }
    }

    public void onActionMenuClick(View v) {
        switch (v.getId()) {
            case R.id.btnActionBarCancel:
                dlgCancel.show();
//                ((TextView) v.findViewById(R.id.tvOfBtnActionBarCancel)).setText("Edit");
                return;
        }
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnDiaryConfirm:
                dlgConfirm.show();
                return;
            case R.id.tvDiarySelectDate:
                dlgDatePicker.show();
                return;
            case R.id.tvDiarySelectTime:
                dlgTimePicker.show();
                return;
        }
    }

    public void onAttachButtonClick(View v) {
        switch (v.getId()) {
            case R.id.btnAttachPicture:
                Intent pickImage = new Intent(Intent.ACTION_PICK,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                pickImage.setType("image/*");
                startActivityForResult(pickImage, ACTIVITY_REQ_CODE_PICTURE);
                return;
            case R.id.btnAttachCamera:
                if (PackageManager.PERMISSION_GRANTED ==
                        ActivityCompat.checkSelfPermission(getApplicationContext(),
                                Manifest.permission.CAMERA)) {
                    //It have permission
                    Log.d("WriteDiaryActivity", "Start Camera Activity with Permissions");
                    ContentValues contentValues = new ContentValues();
                    contentValues.put(MediaStore.Images.Media.TITLE, "Temp Captured");
                    contentValues.put(MediaStore.Images.Media.DESCRIPTION, "Temporary captured image from camera");
                    Intent takeImage = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    lastCapturedImage = null;
//                    Uri tempCapturedUri = GlobalUtils.getTempCapturedImageURI();
                    Uri tempCapturedUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
                    takeImage.putExtra(MediaStore.EXTRA_OUTPUT, tempCapturedUri);
                    lastCapturedImage = tempCapturedUri;
                    startActivityForResult(takeImage, ACTIVITY_REQ_CODE_CAMERA);
                } else {
                    Log.d("WriteDiaryActivity", "Request Camera Permissions");
                    requestPermissions(this, REQUEST_CAMERA);
                }
                return;
            case R.id.btnAttachVideo:
                Intent pickVideo = new Intent(Intent.ACTION_PICK,
                        MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
                pickVideo.setType("video/*");
                startActivityForResult(pickVideo, ACTIVITY_REQ_CODE_VIDEO);
                return;
            case R.id.btnAttachMusic:
                Intent pickAudio = new Intent(Intent.ACTION_PICK,
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI);
//                Intent pickAudio = new Intent(Intent.ACTION_GET_CONTENT);
//                pickAudio.setType("audio/*");
                try {
                    startActivityForResult(pickAudio, ACTIVITY_REQ_CODE_MUSIC);
                } catch (ActivityNotFoundException anfe) {
                    anfe.printStackTrace();
                }
                return;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        String dataString = "null";
        if (data != null) dataString = data.toString();
        Log.d("WriteDiaryActivity", "requestCode: " + requestCode + ", resultCode: " + resultCode +
                ", data: " + dataString);

        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case ACTIVITY_REQ_CODE_CAMERA:
                    if ((data != null) && (data.getData() != null)) {
                        Uri selectedImageUri = data.getData();
                        String imageFilePath = selectedImageUri.getPath();
                        Log.d("WriteDiaryActivity", "Camera: " + imageFilePath);

                        MediaContext attachImage = new MediaContext(this,
                                selectedImageUri, MediaContext.MEDIA_TYPE_IMAGE);
                        addMediaContext(attachImage);
//                    } else if ((data != null) && (data.getExtras().containsKey("data")) && (lastCapturedImage!=null)) {
                    } else if (lastCapturedImage!=null) {
                        Uri selectedImageUri = lastCapturedImage;
                        String imageFilePath = selectedImageUri.getPath();
                        Log.d("WriteDiaryActivity", "Camera (fromBitmap): " + imageFilePath);

                        MediaContext attachImage = new MediaContext(this,
                                selectedImageUri, MediaContext.MEDIA_TYPE_IMAGE);
                        addMediaContext(attachImage);
                    }
                    return;
                case ACTIVITY_REQ_CODE_PICTURE:
                    if ((data != null) && (data.getData() != null)) {
                        Uri selectedImageUri = data.getData();
                        String imageFilePath = selectedImageUri.getPath();
                        Log.d("WriteDiaryActivity", "Image: " + imageFilePath);

                        MediaContext attachImage = new MediaContext(this,
                                selectedImageUri, MediaContext.MEDIA_TYPE_IMAGE);
                        addMediaContext(attachImage);
                    }
                    return;
                case ACTIVITY_REQ_CODE_VIDEO:
                    if ((data != null) && (data.getData() != null)) {
                        Uri selectedVideoUri = data.getData();
                        String videoFilePath = selectedVideoUri.getPath();
                        Log.d("WriteDiaryActivity", "Video: " + videoFilePath);

                        MediaContext attachImage = new MediaContext(this,
                                selectedVideoUri, MediaContext.MEDIA_TYPE_VIDEO);
                        addMediaContext(attachImage);
                    }
                    return;
                case ACTIVITY_REQ_CODE_MUSIC:
                    if ((data != null) && (data.getData() != null)) {
                        Uri selectedAudioUri = data.getData();
                        String audioFilePath = selectedAudioUri.getPath();
                        Log.d("WriteDiaryActivity", "Music: " + audioFilePath);

                        MediaContext attachImage = new MediaContext(this,
                                selectedAudioUri, MediaContext.MEDIA_TYPE_AUDIO);
                        addMediaContext(attachImage);
                    }
                    return;
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    protected void addMediaContext(final MediaContext mediaContext) {
//        Log.d("MediaContext", ""+mediaContext.getFile().exists());
//        Log.d("MediaContext", mediaContext.getFile().toString());
//        Log.d("MediaContext", mediaContext.getUri().toString());

        int height = addMediaContextView(mediaContext, layoutAttachmentFiles);
        viewWriteDiaryLayout.scrollTo(0, viewWriteDiaryLayout.getScrollY() + height);
        mediaContextList.add(mediaContext);
    }

    protected int addMediaContextView(final MediaContext mediaContext, ViewGroup parent) {
        switch(mediaContext.getMediaType()) {
            case MediaContext.MEDIA_TYPE_IMAGE:
                ImageView ivMedia = new ImageView(this);
                ivMedia.setScaleType(ImageView.ScaleType.FIT_CENTER);
                ivMedia.setBackgroundResource(R.drawable.background_list_element_no_corner_radius);

                try {
//                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(),
//                            mediaContext.getUri());
                    Log.d("BitmapFactory", "NativeHeapSize: " + Debug.getNativeHeapSize());
                    Log.d("BitmapFactory", "NativeHeapFreeSize: " + Debug.getNativeHeapFreeSize());
                    Log.d("BitmapFactory", "NativeHeapAllocatedSize: " + Debug.getNativeHeapAllocatedSize());
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inSampleSize=2;
                    options.inDither=true;
                    options.inPurgeable=true;

                    Bitmap bitmap = BitmapFactory.decodeFile(mediaContext.getFile().getAbsolutePath(), options);

                    ivMedia.setImageBitmap(bitmap);

//                    if (bitmap == null) {
//                        bitmap = BitmapFactory.decodeFile(mediaContext.getFile().getAbsolutePath());
//                    }

                    int bitmapWidth = bitmap.getWidth();
                    int bitmapHeight = bitmap.getHeight();
                    float bitmapRatio = (float) bitmapHeight / bitmapWidth;

                    int layoutWidth = layoutAttachmentFiles.getMeasuredWidth();
                    if (layoutWidth == 0) {
                        layoutWidth = getResources().getDisplayMetrics().widthPixels
                                - (int) getResources().getDimension(R.dimen.activity_horizontal_margin);
                    }
                    int newIvHeight = (int) (layoutWidth * bitmapRatio);

                    LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                            layoutWidth, newIvHeight
                    );
                    lp.setMargins(0, 0, 0, (int) GlobalUtils.dpToPixel(this, 10));

                    parent.addView(ivMedia, lp);
                    return newIvHeight;
                } catch (Exception e) {
                    e.printStackTrace();
                    openAlertModal("The image cannot be shown");
                    return 0;
                }

            case MediaContext.MEDIA_TYPE_AUDIO:
                AudioPlayerView audioPlayerView = new AudioPlayerView(this);
                removableViewList.add(audioPlayerView);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
                lp.setMargins(0, 0, 0, (int) GlobalUtils.dpToPixel(this, 10));

                File audioFile = mediaContext.getFile();
                audioPlayerView.setAudio(audioFile.getName(), audioFile);

                parent.addView(audioPlayerView, lp);
                return (int)GlobalUtils.dpToPixel(this, 116+10);

            case MediaContext.MEDIA_TYPE_VIDEO:
                LinearLayout videoLinearLayout = new LinearLayout(this);
                videoLinearLayout.setOrientation(LinearLayout.VERTICAL);
                videoLinearLayout.setBackgroundResource(R.drawable.background_list_element_no_corner_radius);
                LinearLayout.LayoutParams videoLinearLayoutParam =
                        new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                        );
                videoLinearLayoutParam.setMargins(0, 0, 0, (int) GlobalUtils.dpToPixel(this, 10));
                parent.addView(videoLinearLayout, videoLinearLayoutParam);

                //Video View
                final VideoView vvVideo = new VideoView(this);
                vvVideo.setVideoURI(mediaContext.getUri());

                Bitmap thumbnail = ThumbnailUtils.createVideoThumbnail(
                        mediaContext.getFile().getAbsolutePath(),
                        MediaStore.Images.Thumbnails.MINI_KIND);
                BitmapDrawable thumbnailDrawable = new BitmapDrawable(thumbnail);
                videoLinearLayout.setBackgroundDrawable(thumbnailDrawable);

                int bitmapWidth = thumbnail.getWidth();
                int bitmapHeight = thumbnail.getHeight();
                float bitmapRatio = (float) bitmapHeight / bitmapWidth;

                int layoutWidth = layoutAttachmentFiles.getMeasuredWidth();
                if (layoutWidth == 0) {
                    layoutWidth = getResources().getDisplayMetrics().widthPixels
                            - (int) getResources().getDimension(R.dimen.activity_horizontal_margin);
                }
                int newVvHeight = (int) (layoutWidth * bitmapRatio);
                lp = new LinearLayout.LayoutParams(
                        layoutWidth, newVvHeight
                );

                videoLinearLayout.addView(vvVideo, lp);

                //Media Controller
                final MediaController mediaController = new MediaController(this);
                vvVideo.setMediaController(mediaController);

                viewWriteDiaryLayout.getViewTreeObserver().addOnScrollChangedListener(
                        new ViewTreeObserver.OnScrollChangedListener() {
                            @Override
                            public void onScrollChanged() {
                                if (mediaController.isShowing()) {
                                    mediaController.hide();
                                }
                            }
                        }
                );
                vvVideo.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        vvVideo.seekTo(100);
                    }
                }, 100);
                return newVvHeight;
        }
        return 0;
    }

    protected DatePickerDialog.OnDateSetListener datePickerListener =
            new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker datePicker, int year, int month, int dayOfMonth) {
//                    Calendar cal = Calendar.getInstance();
//                    cal.set(year, month, dayOfMonth, 0, 0);
//                    selectedDate = cal;
                    selectedDate.set(year, month, dayOfMonth);

                    tvDiarySelectDate.setText(GlobalUtils.DIARY_DATE_FORMAT.format(selectedDate.getTime()));
                }
            };

    protected TimePickerDialog.OnTimeSetListener timePickerListener =
            new TimePickerDialog.OnTimeSetListener() {
                @Override
                public void onTimeSet(TimePicker timePicker, int hourOfDay, int minute) {
                    selectedDate.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    selectedDate.set(Calendar.MINUTE, minute);

                    tvDiarySelectTime.setText(GlobalUtils.DIARY_TIME_FORMAT.format(selectedDate.getTime()));
                }
            };

    protected void showProgress(final boolean show) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            viewWriteDiaryLayout.setVisibility(show ? View.GONE : View.VISIBLE);
            viewProgress.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    viewWriteDiaryLayout.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            viewProgress.setVisibility(show ? View.VISIBLE : View.GONE);
            viewProgress.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    viewProgress.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            viewProgress.setVisibility(show ? View.VISIBLE : View.GONE);
            viewWriteDiaryLayout.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    private static void requestPermissions(final Activity activity, int requestCode) {
        switch (requestCode) {
            case REQUEST_READ_EXTERNAL_STORAGE:
                if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    new AlertDialog.Builder(activity).setCancelable(false)
                            .setMessage("This application needs 'Read Storage' Permission.\nAre you agree?")
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    ActivityCompat.requestPermissions(activity,
                                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                                            REQUEST_READ_EXTERNAL_STORAGE);
                                }
                            }).show();
                } else {
                    // permission has not been granted yet. Request it directly.
                    ActivityCompat.requestPermissions(activity,
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                            REQUEST_READ_EXTERNAL_STORAGE);
                }
                break;

            case REQUEST_CAMERA:
                if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.CAMERA)) {
                    new AlertDialog.Builder(activity).setCancelable(false)
                            .setMessage("This application needs 'Camera' Permission.\nAre you agree?")
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    ActivityCompat.requestPermissions(activity,
                                            new String[]{Manifest.permission.CAMERA},
                                            REQUEST_CAMERA);
                                }
                            }).show();
                } else {
                    // permission has not been granted yet. Request it directly.
                    ActivityCompat.requestPermissions(activity,
                            new String[]{Manifest.permission.CAMERA},
                            REQUEST_CAMERA);
                }
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_READ_EXTERNAL_STORAGE:
                if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "'Read Storage' Permission is agreed.",
                            Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "'Read Storage' Permission is denied.",
                            Toast.LENGTH_SHORT).show();
                    this.finish();
                }
                return;
            case REQUEST_CAMERA:
                if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "'Camera' Permission is agreed.",
                            Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "'Camera' Permission is denied.",
                            Toast.LENGTH_SHORT).show();
                }
                return;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
}
