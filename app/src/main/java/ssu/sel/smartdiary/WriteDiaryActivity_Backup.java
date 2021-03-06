//package ssu.sel.smartdiary;
//
//import android.animation.Animator;
//import android.animation.AnimatorListenerAdapter;
//import android.app.Activity;
//import android.app.DatePickerDialog;
//import android.app.TimePickerDialog;
//import android.content.ActivityNotFoundException;
//import android.content.DialogInterface;
//import android.content.Intent;
//import android.graphics.Bitmap;
//import android.graphics.PorterDuff;
//import android.graphics.drawable.Drawable;
//import android.media.AudioManager;
//import android.media.MediaPlayer;
//import android.net.Uri;
//import android.os.Build;
//import android.os.Bundle;
//import android.os.Handler;
//import android.os.Message;
//import android.provider.MediaStore;
//import android.support.v4.content.ContextCompat;
//import android.support.v7.app.ActionBar;
//import android.support.v7.app.AlertDialog;
//import android.support.v7.app.AppCompatActivity;
//import android.text.TextUtils;
//import android.util.Log;
//import android.view.Gravity;
//import android.view.View;
//import android.widget.Button;
//import android.widget.DatePicker;
//import android.widget.EditText;
//import android.widget.ImageView;
//import android.widget.LinearLayout;
//import android.widget.ScrollView;
//import android.widget.SeekBar;
//import android.widget.TextView;
//import android.widget.TimePicker;
//
//import org.json.JSONArray;
//import org.json.JSONObject;
//
//import java.io.BufferedInputStream;
//import java.io.BufferedOutputStream;
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.FileOutputStream;
//import java.io.IOException;
//import java.net.URI;
//import java.util.ArrayList;
//import java.util.Calendar;
//
//import ssu.sel.smartdiary.model.DiaryEnvContext;
//import ssu.sel.smartdiary.model.MediaContext;
//import ssu.sel.smartdiary.model.UserProfile;
//import ssu.sel.smartdiary.network.MultipartRestConnector;
//import ssu.sel.smartdiary.speech.WavRecorder;
//
//public class WriteDiaryActivity_Backup extends AppCompatActivity {
//    protected static final int ACTIVITY_REQ_CODE_PICTURE = 100;
//    protected static final int ACTIVITY_REQ_CODE_CAMERA = 101;
//    protected static final int ACTIVITY_REQ_CODE_VIDEO = 102;
//    protected static final int ACTIVITY_REQ_CODE_MUSIC = 103;
//
//    protected String diaryAcitivityType = null;
//
//    protected EditText edtTitle = null;
//    protected EditText edtContent = null;
//    protected TextView tvDiarySelectDate = null;
//    protected TextView tvDiarySelectTime = null;
//
//    protected EditText edtAnnotation = null;
//    protected EditText edtEnvPlace = null;
//    protected EditText edtEnvWeather = null;
//    protected EditText edtEnvEvents = null;
//
//    protected ScrollView viewWriteDiaryLayout = null;
//    protected View viewProgress = null;
//
//    protected TextView tvDiaryAudioDownloading = null;
//    protected View layoutDiaryAudioPlayer = null;
//    protected Button btnDiaryAudioPlay = null;
//    protected Button btnDiaryAudioPause = null;
//    protected Button btnDiaryAudioForward = null;
//    protected Button btnDiaryAudioBackward = null;
//    protected SeekBar progressDiaryAudio = null;
//    protected TextView tvDiaryAudioNowLength = null;
//    protected TextView tvDiaryAudioMaxLength = null;
//
//    protected LinearLayout layoutAttachmentFiles = null;
//
//    protected AlertDialog dlgAlert = null;
//    protected AlertDialog dlgCancel = null;
//    protected AlertDialog dlgConfirm = null;
//    protected Calendar selectedDate;
//    protected DatePickerDialog dlgDatePicker = null;
//    protected TimePickerDialog dlgTimePicker = null;
//
//    protected File diaryAudioFile = null;
//    protected MediaPlayer mediaPlayer = null;
//
//    protected ArrayList<MediaContext> mediaContextList = null;
//    protected MultipartRestConnector saveDiaryConnector = null;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        initView();
//    }
//
//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        if (mediaPlayer != null) {
//            if (mediaPlayer.isPlaying()) mediaPlayer.stop();
//            mediaPlayer.release();
//            mediaPlayer = null;
//        }
//    }
//
//    protected void initView() {
//        ActionBar actionBar = getSupportActionBar();
//        View mActionBarView = getLayoutInflater().inflate(R.layout.action_bar_write_diary, null);
//        actionBar.setCustomView(mActionBarView,
//                new ActionBar.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT,
//                        ActionBar.LayoutParams.MATCH_PARENT, Gravity.CENTER));
//
//        Intent intent = getIntent();
//        diaryAcitivityType = intent.getStringExtra("WRITE_DIARY_TYPE");
//        if (diaryAcitivityType.equals("NEW_TEXT"))
//            ((TextView) mActionBarView.findViewById(R.id.tvActionBarTitle)).setText("New Text Diary");
//        else if (diaryAcitivityType.equals("NEW_AUDIO")) {
//            ((TextView) mActionBarView.findViewById(R.id.tvActionBarTitle)).setText("New Audio Diary");
//            diaryAudioFile = (File)intent.getSerializableExtra("DIARY_AUDIO");
//        }
//        else
//            throw new NullPointerException("No Intent for WriteDiaryType!");
//        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
//        actionBar.setDisplayShowCustomEnabled(true);
//        actionBar.setDisplayHomeAsUpEnabled(false);
//
//        setContentView(R.layout.activity_write_diary);
//
//        edtTitle = (EditText) findViewById(R.id.edtDiaryTitle);
//        edtContent = (EditText) findViewById(R.id.edtDiaryContent);
//        tvDiarySelectDate = (TextView) findViewById(R.id.tvDiarySelectDate);
//        tvDiarySelectTime = (TextView) findViewById(R.id.tvDiarySelectTime);
//        edtAnnotation = (EditText) findViewById(R.id.edtAnnotation);
//        edtEnvPlace = (EditText) findViewById(R.id.edtEnvPlace);
//        edtEnvWeather = (EditText) findViewById(R.id.edtEnvWeather);
//        edtEnvEvents = (EditText) findViewById(R.id.edtEnvEvents);
//        viewWriteDiaryLayout = (ScrollView) findViewById(R.id.viewWriteDiaryForm);
//        viewProgress = findViewById(R.id.progressLayout);
//        tvDiaryAudioDownloading = (TextView) findViewById(R.id.tvDiaryAudioDownloading);
//        layoutDiaryAudioPlayer = findViewById(R.id.layoutDiaryAudioPlayer);
//        btnDiaryAudioPlay = (Button) findViewById(R.id.btnDiaryAudioPlay);
//        btnDiaryAudioPause = (Button) findViewById(R.id.btnDiaryAudioPause);
//        btnDiaryAudioForward = (Button) findViewById(R.id.btnDiaryAudioForward);
//        btnDiaryAudioBackward = (Button) findViewById(R.id.btnDiaryAudioBackward);
//        progressDiaryAudio = (SeekBar) findViewById(R.id.progressDiaryAudio);
//        tvDiaryAudioNowLength = (TextView) findViewById(R.id.tvDiaryAudioNowLength);
//        tvDiaryAudioMaxLength = (TextView) findViewById(R.id.tvDiaryAudioMaxLength);
//        layoutAttachmentFiles = (LinearLayout) findViewById(R.id.layoutAttachmentFiles);
//
//        Drawable edtTitleBGDrawble = edtTitle.getBackground();
//        edtTitleBGDrawble.mutate().setColorFilter(getResources().getColor(R.color.indigo_500),
//                PorterDuff.Mode.SRC_ATOP);
//        edtTitle.setBackground(edtTitleBGDrawble);
//
//        if (diaryAcitivityType.equals("NEW_AUDIO")) {
////            edtTitle.setText(intent.getStringExtra("DIARY_TITLE"));
//            edtContent.setText(intent.getStringExtra("DIARY_CONTENT"));
////            selectedDate = (Calendar) intent.getSerializableExtra("DIARY_DATE");
//            selectedDate = Calendar.getInstance();
//        } else {
//            selectedDate = Calendar.getInstance();
//        }
//        setModals();
//
//        mediaContextList = new ArrayList<>();
//        setJsonConnectors();
//
//        if (diaryAudioFile != null)
//            setAudioPlayer();
//        else {
//            tvDiaryAudioDownloading.setText("Audio File Load Failed.");
//            tvDiaryAudioDownloading.setVisibility(View.VISIBLE);
//            layoutDiaryAudioPlayer.setVisibility(View.GONE);
//        }
//    }
//
//    protected Thread audioCheckThread = null;
//    protected Handler audioCheckHandler = new Handler() {
//        @Override
//        public void handleMessage(Message msg) {
//            switch (msg.what) {
//                case 0:
//                    if(mediaPlayer != null) {
//                        progressDiaryAudio.setProgress(msg.arg1);
//                        setAudioPlayerNowLengthText(msg.arg1);
//                    }
//            }
//        }
//    };
//    protected void setAudioPlayer() {
//        progressDiaryAudio.getProgressDrawable().setColorFilter(
//            ContextCompat.getColor(this, R.color.pink_A200), PorterDuff.Mode.SRC_IN);
//        progressDiaryAudio.setProgress(0);
//        progressDiaryAudio.setMax(1);
//
//        try {
//            FileInputStream fis = new FileInputStream(diaryAudioFile);
//            mediaPlayer = new MediaPlayer();
//            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
//            mediaPlayer.setDataSource(fis.getFD());
//            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
//                @Override
//                public void onCompletion(MediaPlayer mp) {
//                    mp.seekTo(0);
//                    progressDiaryAudio.setProgress(0);
//                    setAudioPlayerNowLengthText(0);
//                    btnDiaryAudioPause.setVisibility(View.INVISIBLE);
//                    btnDiaryAudioPlay.setVisibility(View.VISIBLE);
//                }
//            });
//            mediaPlayer.prepare();
//
//            int duration = mediaPlayer.getDuration();
//            if (duration < 1000) {
//                tvDiaryAudioMaxLength.setText("00:01");
//            } else {
//                int sec = duration / 1000;
//                int minute = sec / 60;
//                sec = sec % 60;
//                tvDiaryAudioMaxLength.setText(String.format("%02d:%02d", minute, sec));
//            }
//            progressDiaryAudio.setMax(mediaPlayer.getDuration());
//            progressDiaryAudio.setProgress(0);
//            setAudioPlayerNowLengthText(0);
//
//            progressDiaryAudio.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
//                @Override
//                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
//                    if (fromUser && mediaPlayer != null) {
//                        mediaPlayer.seekTo(progress);
//                        setAudioPlayerNowLengthText(progress);
//                    }
//                }
//                public void onStartTrackingTouch(SeekBar seekBar) {}
//                public void onStopTrackingTouch(SeekBar seekBar) {}
//            });
//
//            audioCheckThread = new Thread() {
//                @Override
//                public void run() {
//                    while(true) {
//                        if (mediaPlayer == null) break;
//                        if (mediaPlayer.isPlaying()) {
//                            Message msg = Message.obtain(audioCheckHandler);
//                            msg.what = 0;
//                            msg.arg1 = mediaPlayer.getCurrentPosition();
//                            audioCheckHandler.sendMessage(msg);
//                        }
//                        try {
//                            Thread.sleep(200);
//                        } catch (InterruptedException ie) {}
//                    }
//                }
//            };
//            audioCheckThread.start();
//
//            Log.d("WriteDiaryActivity", "Duration: " + mediaPlayer.getDuration());
//            Log.d("WriteDiaryActivity", "Position: " + mediaPlayer.getCurrentPosition());
//        } catch (Exception e) {
//            e.printStackTrace();
//            openAlertModal("The file colud not play...");
//        }
//
//        btnDiaryAudioPlay.setVisibility(View.VISIBLE);
//        btnDiaryAudioPause.setVisibility(View.INVISIBLE);
//        tvDiaryAudioDownloading.setVisibility(View.GONE);
//        layoutDiaryAudioPlayer.setVisibility(View.VISIBLE);
//    }
//
//    protected void setAudioPlayerSeek(int progress) {
//        if (mediaPlayer != null) {
//            int duration = mediaPlayer.getDuration();
//            if (progress >= duration) progress = duration - 1;
//            else if (progress < 0) progress = 0;
//            mediaPlayer.seekTo(progress);
//            progressDiaryAudio.setProgress(progress);
//            setAudioPlayerNowLengthText(progress);
//        }
//    }
//
//    protected void setAudioPlayerNowLengthText(int progress) {
//        int sec = progress / 1000;
//        int minute = sec / 60;
//        sec = sec % 60;
//        tvDiaryAudioNowLength.setText(String.format("%02d:%02d", minute, sec));
//    }
//
//    protected void setModals() {
//        tvDiarySelectDate.setText(GlobalUtils.DIARY_DATE_FORMAT.format(selectedDate.getTime()));
//        tvDiarySelectTime.setText(GlobalUtils.DIARY_TIME_FORMAT.format(selectedDate.getTime()));
//        dlgDatePicker = new DatePickerDialog(this, datePickerListener,
//                selectedDate.get(Calendar.YEAR), selectedDate.get(Calendar.MONTH),
//                selectedDate.get(Calendar.DAY_OF_MONTH));
//        dlgTimePicker = new TimePickerDialog(this, timePickerListener,
//                selectedDate.get(Calendar.HOUR_OF_DAY), selectedDate.get(Calendar.SECOND), true);
//
//
//        dlgAlert = new AlertDialog.Builder(this).setMessage("Message")
//                .setNeutralButton("OK", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialogInterface, int i) {
//                        dialogInterface.dismiss();
//                    }
//                }).create();
//
//        dlgCancel = new AlertDialog.Builder(this).setMessage("Are you sure to cancel?")
//                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialogInterface, int i) {
//                        if (diaryAcitivityType.equals("NEW_AUDIO")) {
//                            WavRecorder.removeRecordedTempFiles();
//                        }
//
//                        dialogInterface.dismiss();
//                        WriteDiaryActivity_Backup.this.finish();
//                    }
//                }).setNegativeButton("No", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialogInterface, int i) {
//                        dialogInterface.dismiss();
//                    }
//                }).create();
//
//        dlgConfirm = new AlertDialog.Builder(this).setMessage("Are you sure to confirm?")
//                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialogInterface, int i) {
//                        dialogInterface.dismiss();
//                        saveDiary();
//                    }
//                }).setNegativeButton("No", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialogInterface, int i) {
//                        dialogInterface.dismiss();
//                    }
//                }).create();
//    }
//
//    protected void setJsonConnectors() {
//        saveDiaryConnector = new MultipartRestConnector("diary", "POST",
//                new MultipartRestConnector.OnConnectListener() {
//                    @Override
//                    public void onDone(JSONObject resJson) {
//                        if (resJson != null) {
//                            Log.d("WriteDiary - Json", resJson.toString());
//                            try {
//                                boolean success = resJson.getBoolean("create_diary");
//                                int diaryId = resJson.getInt("audio_diary_id");
//                                if (success) {
//                                    if (diaryAcitivityType.equals("NEW_AUDIO")) {
//                                        WavRecorder.removeRecordedTempFiles();
//                                    }
//
//                                    //Copy the temp file to diary audio cache file
//                                    if(diaryAudioFile != null) {
//                                        try {
//                                            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(diaryAudioFile));
//                                            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(
//                                                    GlobalUtils.getAudioDiaryFile(
//                                                            UserProfile.getUserProfile().getUserID(), diaryId)));
//
//                                            byte[] buf = new byte [1024*10];
//                                            int byteLen;
//                                            while ((byteLen = bis.read(buf)) > 0) {
//                                                bos.write(buf, 0 ,byteLen);
//                                            }
//                                            bis.close();
//                                            bos.close();
//
//                                        } catch (Exception e) {
//                                            e.printStackTrace();
//                                            Log.d("WriteDiaryAcitivity", "Audio Copy Failed");
//                                            try {
//                                                GlobalUtils.getAudioDiaryFile(
//                                                        UserProfile.getUserProfile().getUserID(), diaryId)
//                                                                .delete();
//                                            } catch (Exception e2) {}
//                                        }
//                                    }
//
//                                    WriteDiaryActivity_Backup.this.finish();
//                                } else {
//                                    openAlertModal("Diary Create Failed");
//                                }
//
//                            } catch (Exception e) {
//                                e.printStackTrace();
//                                Log.d("Main - Json", "Json parsing error");
//                                openAlertModal("Json parsing error.", "Error");
//                            }
//                        } else {
//                            Log.d("WriteDiary - Json", "No response");
//                            openAlertModal("There is no reponse...");
//                        }
//
//                        showProgress(false);
//                    }
//                });
//    }
//
//    protected void openAlertModal(CharSequence msg) {
//        openAlertModal(msg, "Alert");
//    }
//
//    protected void openAlertModal(CharSequence msg, CharSequence title) {
//        dlgAlert.setTitle(title);
//        dlgAlert.setMessage(msg);
//        dlgAlert.show();
//    }
//
//    protected void saveDiary() {
//        String title = edtTitle.getText().toString();
//        String content = edtContent.getText().toString();
//
//        boolean cancel = false;
//        View focusView = null;
//
//        if (TextUtils.isEmpty(content)) {
//            edtContent.setError("Content is empty");
//            focusView = edtContent;
//            cancel = true;
//        }
//
//        if (TextUtils.isEmpty(title)) {
//            edtTitle.setError("Title is empty");
//            focusView = edtTitle;
//            cancel = true;
//        } else if (title.length() < 4) {
//            edtTitle.setError("Title is too short");
//            focusView = edtTitle;
//            cancel = true;
//        }
//
//        if (cancel) {
//            focusView.requestFocus();
//
//        } else {
//            String annotation = edtAnnotation.getText().toString();
//            String envPlace = edtEnvPlace.getText().toString();
//            String envWeather = edtEnvWeather.getText().toString();
//            String envEvents = edtEnvEvents.getText().toString();
//            long createdTime = selectedDate.getTime().getTime();
//
//            JSONObject json = new JSONObject();
//            try {
//                json.put("user_id", UserProfile.getUserProfile().getUserID());
//                json.put("created_date", createdTime);
//                json.put("title", title);
//                json.put("content", content);
//
//                JSONArray arrContexts = new JSONArray();
//                if (!TextUtils.isEmpty(annotation)) {
//                    JSONObject jsonAnnotation = new JSONObject();
//                    jsonAnnotation.put("type", DiaryEnvContext.CONTEXT_TYPE_ANNOTATION);
//                    jsonAnnotation.put("subtype", "");
//                    jsonAnnotation.put("value", annotation);
//                    jsonAnnotation.put("date_added", createdTime);
//                    arrContexts.put(jsonAnnotation);
//                }
//                if (!TextUtils.isEmpty(envPlace)) {
//                    String[] places = envPlace.split(",");
//                    for (String place : places) {
//                        JSONObject jsonPlace = new JSONObject();
//                        jsonPlace.put("type", DiaryEnvContext.CONTEXT_TYPE_ENVIRONMENT);
//                        jsonPlace.put("subtype", DiaryEnvContext.TYPE_ENV_PLACE);
//                        jsonPlace.put("value", place.trim());
//                        jsonPlace.put("date_added", createdTime);
//                        arrContexts.put(jsonPlace);
//                    }
//                }
//                if (!TextUtils.isEmpty(envWeather)) {
//                    String[] weathers = envWeather.split(",");
//                    for (String weather : weathers) {
//                        JSONObject jsonWeather = new JSONObject();
//                        jsonWeather.put("type", DiaryEnvContext.CONTEXT_TYPE_ENVIRONMENT);
//                        jsonWeather.put("subtype", DiaryEnvContext.TYPE_ENV_WEATHER);
//                        jsonWeather.put("value", weather.trim());
//                        jsonWeather.put("date_added", createdTime);
//                        arrContexts.put(jsonWeather);
//                    }
//                }
//                if (!TextUtils.isEmpty(envEvents)) {
//                    String[] events = envEvents.split(",");
//                    for (String event : events) {
//                        JSONObject jsonEvent = new JSONObject();
//                        jsonEvent.put("type", DiaryEnvContext.CONTEXT_TYPE_ENVIRONMENT);
//                        jsonEvent.put("subtype", DiaryEnvContext.TYPE_ENV_EVENT);
//                        jsonEvent.put("value", event.trim());
//                        jsonEvent.put("date_added", createdTime);
//                        arrContexts.put(jsonEvent);
//                    }
//                }
//                json.put("diary_context", arrContexts);
//
//            } catch (Exception e) {
//                e.printStackTrace();
//                dlgAlert.setMessage("JSON Creation Error");
//                dlgAlert.show();
//                return;
//            }
//
//            showProgress(true);
//            if (diaryAcitivityType.equals("NEW_AUDIO")) {
//                File recordedFile = (File) getIntent().getSerializableExtra("DIARY_AUDIO");
//                saveDiaryConnector.request(recordedFile, json);
//            } else {
////                saveDiaryConnector.request(null, json);
//            }
//        }
//    }
//
//    public void onActionMenuClick(View v) {
//        switch (v.getId()) {
//            case R.id.btnActionBarCancel:
//                dlgCancel.show();
////                ((TextView) v.findViewById(R.id.tvOfBtnActionBarCancel)).setText("Edit");
//                return;
//        }
//    }
//
//    public void onClick(View v) {
//        switch (v.getId()) {
//            case R.id.btnDiaryConfirm:
//                dlgConfirm.show();
//                return;
//            case R.id.tvDiarySelectDate:
//                dlgDatePicker.show();
//                return;
//            case R.id.tvDiarySelectTime:
//                dlgTimePicker.show();
//                return;
//        }
//    }
//
//    public void onAudioControlClick(View v) {
//        switch (v.getId()) {
//            case R.id.btnDiaryAudioPlay:
//                mediaPlayer.start();
//                btnDiaryAudioPlay.setVisibility(View.INVISIBLE);
//                btnDiaryAudioPause.setVisibility(View.VISIBLE);
//                return;
//
//            case R.id.btnDiaryAudioPause:
//                if(mediaPlayer.isPlaying()) {
//                    mediaPlayer.pause();
//                    progressDiaryAudio.setProgress(mediaPlayer.getCurrentPosition());
//                    setAudioPlayerNowLengthText(mediaPlayer.getCurrentPosition());
//                }
//                btnDiaryAudioPause.setVisibility(View.GONE);
//                btnDiaryAudioPlay.setVisibility(View.VISIBLE);
//                return;
//
//            case R.id.btnDiaryAudioForward:
//                if(mediaPlayer != null) {
//                    int duration = mediaPlayer.getDuration();
//                    if (duration < 10000) {
//                        setAudioPlayerSeek(mediaPlayer.getCurrentPosition() + 1000);
//                    } else if (duration < 30000) {
//                        setAudioPlayerSeek(mediaPlayer.getCurrentPosition() + 3000);
//                    } else if (duration < 60000) {
//                        setAudioPlayerSeek(mediaPlayer.getCurrentPosition() + 5000);
//                    } else {
//                        setAudioPlayerSeek(mediaPlayer.getCurrentPosition() + 10000);
//                    }
//                }
//                return;
//
//            case R.id.btnDiaryAudioBackward:
//                if(mediaPlayer != null) {
//                    int duration = mediaPlayer.getDuration();
//                    if (duration < 10000) {
//                        setAudioPlayerSeek(mediaPlayer.getCurrentPosition() - 1000);
//                    } else if (duration < 30000) {
//                        setAudioPlayerSeek(mediaPlayer.getCurrentPosition() - 3000);
//                    } else if (duration < 60000) {
//                        setAudioPlayerSeek(mediaPlayer.getCurrentPosition() - 5000);
//                    } else {
//                        setAudioPlayerSeek(mediaPlayer.getCurrentPosition() - 10000);
//                    }
//                }
//                return;
//        }
//    }
//
//    public void onAttachButtonClick(View v) {
//        switch (v.getId()) {
//            case R.id.btnAttachPicture:
//                Intent pickImage = new Intent(Intent.ACTION_PICK,
//                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//                pickImage.setType("image/*");
//                startActivityForResult(pickImage, ACTIVITY_REQ_CODE_PICTURE);
//                return;
//            case R.id.btnAttachCamera:
//                Intent takeImage = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//                startActivityForResult(takeImage, ACTIVITY_REQ_CODE_CAMERA);
//                return;
//            case R.id.btnAttachVideo:
//                Intent pickVideo = new Intent(Intent.ACTION_PICK,
//                        MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
//                pickVideo.setType("video/*");
//                startActivityForResult(pickVideo, ACTIVITY_REQ_CODE_VIDEO);
//                return;
//            case R.id.btnAttachMusic:
//                Intent pickAudio = new Intent(Intent.ACTION_PICK,
//                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI);
////                Intent pickAudio = new Intent(Intent.ACTION_GET_CONTENT);
////                pickAudio.setType("audio/*");
//                try {
//                    startActivityForResult(pickAudio, ACTIVITY_REQ_CODE_MUSIC);
//                } catch (ActivityNotFoundException anfe) {
//                    anfe.printStackTrace();
//                }
//                return;
//        }
//    }
//
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        if (resultCode == Activity.RESULT_OK) {
//            switch (requestCode) {
//                case ACTIVITY_REQ_CODE_CAMERA:
//                    if ((data != null) && (data.getData() != null)) {
//                        Uri selectedImageUri = data.getData();
//                        String imageFilePath = selectedImageUri.getPath();
//                        Log.d("WriteDiaryActivity", "Camera: " + imageFilePath);
//
//                        MediaContext attachImage = new MediaContext(selectedImageUri,
//                                MediaContext.MEDIA_TYPE_IMAGE);
//                        addMediaContext(attachImage);
//                    }
//                    return;
//                case ACTIVITY_REQ_CODE_PICTURE:
//                    if ((data != null) && (data.getData() != null)) {
//                        Uri selectedImageUri = data.getData();
//                        String imageFilePath = selectedImageUri.getPath();
//                        Log.d("WriteDiaryActivity", "Image: " + imageFilePath);
//
//                        MediaContext attachImage = new MediaContext(selectedImageUri,
//                                MediaContext.MEDIA_TYPE_IMAGE);
//                        addMediaContext(attachImage);
//                    }
//                    return;
//                case ACTIVITY_REQ_CODE_VIDEO:
//                    if ((data != null) && (data.getData() != null)) {
//                        Uri selectedVideoUri = data.getData();
//                        String videoFilePath = selectedVideoUri.getPath();
//                        Log.d("WriteDiaryActivity", "Video: " + videoFilePath);
//                        try {
//                            File file = new File(new URI(videoFilePath));
//                            Log.d("WriteDiaryActivity", "FilePath: " + file.getAbsolutePath());
//                        } catch(Exception e) {}
//                    }
//                    return;
//                case ACTIVITY_REQ_CODE_MUSIC:
//                    if ((data != null) && (data.getData() != null)) {
//                        Uri selectedAudioUri = data.getData();
//                        String audioFilePath = selectedAudioUri.getPath();
//                        Log.d("WriteDiaryActivity", "Music: " + audioFilePath);
//                        try {
//                            File file = new File(new URI(audioFilePath));
//                            Log.d("WriteDiaryActivity", "FilePath: " + file.getAbsolutePath());
//                        } catch(Exception e) {}
//                    }
//                    return;
//            }
//        }
//        super.onActivityResult(requestCode, resultCode, data);
//    }
//
//    protected void addMediaContext(MediaContext mediaContext) {
//        switch(mediaContext.getMediaType()) {
//            case MediaContext.MEDIA_TYPE_IMAGE:
//                ImageView ivMedia = new ImageView(this);
////                ivMedia.setImageURI(mediaContext.getUri());
//                ivMedia.setScaleType(ImageView.ScaleType.FIT_CENTER);
//                ivMedia.setBackgroundResource(R.drawable.background_list_element_no_corner_radius);
//
//                try {
//                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(),
//                            mediaContext.getUri());
//                    ivMedia.setImageBitmap(bitmap);
//
//                    int bitmapWidth = bitmap.getWidth();
//                    int bitmapHeight = bitmap.getHeight();
//                    float bitmapRatio = (float)bitmapHeight / bitmapWidth;
//
//                    int layoutWidth = layoutAttachmentFiles.getMeasuredWidth();
//                    int newIvHeight = (int)(layoutWidth * bitmapRatio);
//
//                    LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
////                            LinearLayout.LayoutParams.MATCH_PARENT,
////                            LinearLayout.LayoutParams.WRAP_CONTENT
//                            layoutWidth, newIvHeight
//                    );
//                    lp.setMargins(0, 0, 0, (int)GlobalUtils.dpToPixel(this, 10));
//                    layoutAttachmentFiles.addView(ivMedia, lp);
//                    viewWriteDiaryLayout.scrollTo(0,viewWriteDiaryLayout.getScrollY()+newIvHeight);
//
//                    mediaContextList.add(mediaContext);
//
//                } catch (IOException ie) {
//                    ie.printStackTrace();
//                    openAlertModal("The image cannot be shown");
//                }
//                return;
//
//            case MediaContext.MEDIA_TYPE_AUDIO:
//                return;
//
//            case MediaContext.MEDIA_TYPE_VIDEO:
//                return;
//        }
//    }
//
//    protected DatePickerDialog.OnDateSetListener datePickerListener =
//            new DatePickerDialog.OnDateSetListener() {
//                @Override
//                public void onDateSet(DatePicker datePicker, int year, int month, int dayOfMonth) {
////                    Calendar cal = Calendar.getInstance();
////                    cal.set(year, month, dayOfMonth, 0, 0);
////                    selectedDate = cal;
//                    selectedDate.set(year, month, dayOfMonth);
//
//                    tvDiarySelectDate.setText(GlobalUtils.DIARY_DATE_FORMAT.format(selectedDate.getTime()));
//                }
//            };
//
//    protected TimePickerDialog.OnTimeSetListener timePickerListener =
//            new TimePickerDialog.OnTimeSetListener() {
//                @Override
//                public void onTimeSet(TimePicker timePicker, int hourOfDay, int minute) {
//                    selectedDate.set(Calendar.HOUR_OF_DAY, hourOfDay);
//                    selectedDate.set(Calendar.MINUTE, minute);
//
//                    tvDiarySelectTime.setText(GlobalUtils.DIARY_TIME_FORMAT.format(selectedDate.getTime()));
//                }
//            };
//
//    protected void showProgress(final boolean show) {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
//            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);
//
//            viewWriteDiaryLayout.setVisibility(show ? View.GONE : View.VISIBLE);
//            viewProgress.animate().setDuration(shortAnimTime).alpha(
//                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
//                @Override
//                public void onAnimationEnd(Animator animation) {
//                    viewWriteDiaryLayout.setVisibility(show ? View.GONE : View.VISIBLE);
//                }
//            });
//
//            viewProgress.setVisibility(show ? View.VISIBLE : View.GONE);
//            viewProgress.animate().setDuration(shortAnimTime).alpha(
//                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
//                @Override
//                public void onAnimationEnd(Animator animation) {
//                    viewProgress.setVisibility(show ? View.VISIBLE : View.GONE);
//                }
//            });
//        } else {
//            viewProgress.setVisibility(show ? View.VISIBLE : View.GONE);
//            viewWriteDiaryLayout.setVisibility(show ? View.GONE : View.VISIBLE);
//        }
//    }
//}
