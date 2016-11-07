package ssu.sel.smartdiary;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Calendar;

import ssu.sel.smartdiary.model.Diary;
import ssu.sel.smartdiary.model.DiaryContext;
import ssu.sel.smartdiary.model.UserProfile;
import ssu.sel.smartdiary.network.MultipartRestConnector;
import ssu.sel.smartdiary.speech.WavRecorder;

public class WriteDiaryActivity extends AppCompatActivity {

    protected String diaryAcitivityType = null;

    protected EditText edtTitle = null;
    protected EditText edtContent = null;
    protected TextView tvDiarySelectDate = null;
    protected TextView tvDiarySelectTime = null;

    protected EditText edtAnnotation = null;
    protected EditText edtEnvPlace = null;
    protected EditText edtEnvWeather = null;
    protected EditText edtEnvEvents = null;

    protected View viewWriteDiaryLayout = null;
    protected View viewProgress = null;

    protected TextView tvDiaryAudioDownloading = null;
    protected Button btnDiaryAudioPlay = null;
    protected Button btnDiaryAudioPause = null;
    protected Button btnDiaryAudioStop = null;

    protected AlertDialog dlgAlert = null;
    protected AlertDialog dlgCancel = null;
    protected AlertDialog dlgConfirm = null;
    protected Calendar selectedDate;
    protected DatePickerDialog dlgDatePicker = null;
    protected TimePickerDialog dlgTimePicker = null;

    protected File audioFile = null;
    protected MediaPlayer mediaPlayer = null;

    protected MultipartRestConnector saveDiaryConnector = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
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
            audioFile = (File)intent.getSerializableExtra("DIARY_AUDIO");
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
        edtEnvEvents = (EditText) findViewById(R.id.edtEnvEvents);
        viewWriteDiaryLayout = findViewById(R.id.viewWriteDiaryForm);
        viewProgress = findViewById(R.id.progressLayout);
        tvDiaryAudioDownloading = (TextView) findViewById(R.id.tvDiaryAudioDownloading);
        btnDiaryAudioPlay = (Button) findViewById(R.id.btnDiaryAudioPlay);
        btnDiaryAudioPause = (Button) findViewById(R.id.btnDiaryAudioPause);
        btnDiaryAudioStop = (Button) findViewById(R.id.btnDiaryAudioStop);

        Drawable edtTitleBGDrawble = edtTitle.getBackground();
        edtTitleBGDrawble.mutate().setColorFilter(getResources().getColor(R.color.indigo_500),
                PorterDuff.Mode.SRC_ATOP);
        edtTitle.setBackground(edtTitleBGDrawble);

        tvDiaryAudioDownloading.setVisibility(View.GONE);
        btnDiaryAudioPause.setVisibility(View.VISIBLE);

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

        if (audioFile != null)
            setAudioPlayer();
    }

    protected void setAudioPlayer() {
        try {
            FileInputStream fis = new FileInputStream(audioFile);
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setDataSource(fis.getFD());
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    mp.seekTo(0);
                    btnDiaryAudioPause.setVisibility(View.GONE);
                    btnDiaryAudioPlay.setVisibility(View.VISIBLE);
                }
            });
            mediaPlayer.prepare();
        } catch (Exception e) {
            e.printStackTrace();
            openAlertModal("The file colud not play...");
        }

        btnDiaryAudioPlay.setVisibility(View.VISIBLE);
        btnDiaryAudioStop.setVisibility(View.VISIBLE);
        btnDiaryAudioPause.setVisibility(View.GONE);
        tvDiaryAudioDownloading.setVisibility(View.GONE);
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
        saveDiaryConnector = new MultipartRestConnector("diary", "POST",
                new MultipartRestConnector.OnConnectListener() {
                    @Override
                    public void onDone(JSONObject resJson) {
                        if (resJson != null) {
                            Log.d("WriteDiary - Json", resJson.toString());
                            try {
                                boolean success = resJson.getBoolean("create_diary");
                                int diaryId = resJson.getInt("audio_diary_id");
                                if (success) {
                                    if (diaryAcitivityType.equals("NEW_AUDIO")) {
                                        WavRecorder.removeRecordedTempFiles();
                                    }

                                    //Copy the temp file to diary audio cache file
                                    if(audioFile != null) {
                                        try {
                                            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(audioFile));
                                            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(
                                                    GlobalUtils.getAudioDiaryFile(
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
                                                GlobalUtils.getAudioDiaryFile(
                                                        UserProfile.getUserProfile().getUserID(), diaryId)
                                                                .delete();
                                            } catch (Exception e2) {}
                                        }
                                    }

                                    WriteDiaryActivity.this.finish();
                                } else {
                                    openAlertModal("Diary Create Failed");
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
            edtContent.setError("Content is empty");
            focusView = edtContent;
            cancel = true;
        }

        if (TextUtils.isEmpty(title)) {
            edtTitle.setError("Title is empty");
            focusView = edtTitle;
            cancel = true;
        } else if (title.length() < 4) {
            edtTitle.setError("Title is too short");
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
            long createdTime = selectedDate.getTime().getTime();

            JSONObject json = new JSONObject();
            try {
                json.put("user_id", UserProfile.getUserProfile().getUserID());
                json.put("created_date", createdTime);
                json.put("title", title);
                json.put("content", content);

                JSONArray arrContexts = new JSONArray();
                if (!TextUtils.isEmpty(annotation)) {
                    JSONObject jsonAnnotation = new JSONObject();
                    jsonAnnotation.put("type", DiaryContext.CONTEXT_TYPE_ANNOTATION);
                    jsonAnnotation.put("subtype", "");
                    jsonAnnotation.put("value", annotation);
                    jsonAnnotation.put("date_added", createdTime);
                    arrContexts.put(jsonAnnotation);
                }
                if (!TextUtils.isEmpty(envPlace)) {
                    String[] places = envPlace.split(",");
                    for (String place : places) {
                        JSONObject jsonPlace = new JSONObject();
                        jsonPlace.put("type", DiaryContext.CONTEXT_TYPE_ENVIRONMENT);
                        jsonPlace.put("subtype", DiaryContext.SUB_TYPE_ENV_PLACE);
                        jsonPlace.put("value", place.trim());
                        jsonPlace.put("date_added", createdTime);
                        arrContexts.put(jsonPlace);
                    }
                }
                if (!TextUtils.isEmpty(envWeather)) {
                    String[] weathers = envWeather.split(",");
                    for (String weather : weathers) {
                        JSONObject jsonWeather = new JSONObject();
                        jsonWeather.put("type", DiaryContext.CONTEXT_TYPE_ENVIRONMENT);
                        jsonWeather.put("subtype", DiaryContext.SUB_TYPE_ENV_WEATHER);
                        jsonWeather.put("value", weather.trim());
                        jsonWeather.put("date_added", createdTime);
                        arrContexts.put(jsonWeather);
                    }
                }
                if (!TextUtils.isEmpty(envEvents)) {
                    String[] events = envEvents.split(",");
                    for (String event : events) {
                        JSONObject jsonEvent = new JSONObject();
                        jsonEvent.put("type", DiaryContext.CONTEXT_TYPE_ENVIRONMENT);
                        jsonEvent.put("subtype", DiaryContext.SUB_TYPE_ENV_EVENT);
                        jsonEvent.put("value", event.trim());
                        jsonEvent.put("date_added", createdTime);
                        arrContexts.put(jsonEvent);
                    }
                }
                json.put("diary_context", arrContexts);

            } catch (Exception e) {
                e.printStackTrace();
                dlgAlert.setMessage("JSON Creation Error");
                dlgAlert.show();
                return;
            }

            showProgress(true);
            if (diaryAcitivityType.equals("NEW_AUDIO")) {
                File recordedFile = (File) getIntent().getSerializableExtra("DIARY_AUDIO");
                saveDiaryConnector.request(recordedFile, json);
            } else {
//                saveDiaryConnector.request(null, json);
            }
        }
    }

    public void onActionMenuClick(View v) {
        switch (v.getId()) {
            case R.id.btnActionBarCancel:
                dlgCancel.show();
                ((TextView) v.findViewById(R.id.tvOfBtnActionBarCancel)).setText("Edit");
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

    public void onAudioControlClick(View v) {
        switch (v.getId()) {
            case R.id.btnDiaryAudioPlay:
                mediaPlayer.start();
                btnDiaryAudioPlay.setVisibility(View.GONE);
                btnDiaryAudioPause.setVisibility(View.VISIBLE);
                return;

            case R.id.btnDiaryAudioPause:
                if(mediaPlayer.isPlaying())
                    mediaPlayer.pause();
                btnDiaryAudioPause.setVisibility(View.GONE);
                btnDiaryAudioPlay.setVisibility(View.VISIBLE);
                return;

            case R.id.btnDiaryAudioStop:
                if (mediaPlayer.isPlaying()) mediaPlayer.pause();
                mediaPlayer.seekTo(0);
                btnDiaryAudioPause.setVisibility(View.GONE);
                btnDiaryAudioPlay.setVisibility(View.VISIBLE);
                return;
        }
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
}
