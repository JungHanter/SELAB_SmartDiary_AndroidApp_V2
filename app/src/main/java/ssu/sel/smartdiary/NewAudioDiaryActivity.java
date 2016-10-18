package ssu.sel.smartdiary;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;

import ssu.sel.smartdiary.speech.MSSpeechRecognizer;
import ssu.sel.smartdiary.speech.WavRecorder;

import java.util.ArrayList;
import java.util.Calendar;

public class NewAudioDiaryActivity extends AppCompatActivity {
    private Button btnStartRecord = null;
    private Button btnStopRecord = null;
    private TextView tvRecordStatus = null;
    private EditText edtTitle = null;
    private EditText edtRecord = null;
    private TextView tvDiarySelectDate = null;

    private View viewRecordLayout = null;
    private View viewProgress = null;

    private AlertDialog dlgRecord = null;
    private AlertDialog dlgAlert = null;
    private AlertDialog dlgCancel = null;
    private Calendar selectedDate;
    private DatePickerDialog dlgDatePicker = null;

    public boolean nowRecordingStatus = false;

    private WavRecorder mWavRecorder = null;

    private MSSpeechRecognizer.OnRecognizeDoneListener recognizeDoneListener = null;
    private MSSpeechRecognizer speechRecognizer = null;
    private int recordedPartCount = 0;
    private ArrayList<String> recordedStrings = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActionBar actionBar = getSupportActionBar();
        View mActionBarView = getLayoutInflater().inflate(R.layout.action_bar_new_audio_diary, null);
        actionBar.setCustomView(mActionBarView,
                new ActionBar.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT,
                        ActionBar.LayoutParams.MATCH_PARENT, Gravity.CENTER));
        ((TextView) mActionBarView.findViewById(R.id.tvActionBarTitle)).setText("New Audio Diary");
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(false);
        setContentView(R.layout.activity_new_audio_diary);

        edtTitle = (EditText) findViewById(R.id.edtDiaryTitle);
        edtRecord = (EditText) findViewById(R.id.edtDiaryContent);
        btnStartRecord = (Button) findViewById(R.id.btnStartRecord);
        btnStopRecord = (Button) findViewById(R.id.btnStopRecord);
        tvRecordStatus = (TextView) findViewById(R.id.tvRecordStatus);
        tvDiarySelectDate = (TextView) findViewById(R.id.tvDiarySelectDate);
        setModals();

        viewRecordLayout = findViewById(R.id.layoutBtnRecord);
        viewProgress = findViewById(R.id.progressLayout);

        recognizeDoneListener = new MSSpeechRecognizer.OnRecognizeDoneListener() {
            @Override
            public void onRecognizeDone() {
                Log.d("NewAudioDiaryActivity", "Recognition Done!!!");
                StringBuilder sb = new StringBuilder();
                for (String s : recordedStrings) {
                    sb.append(s).append('\n');
                }
                edtRecord.setText(sb.toString());
                showProgress(false);
            }

            @Override
            public void onPartialRecognizeDone() {
                Log.d("NewAudioDiaryActivity", "Recognition Partially Done!!!");
                StringBuilder sb = new StringBuilder();
                for (String s : recordedStrings) {
                    sb.append(s).append('\n');
                }
                edtRecord.setText(sb.toString());
                recordedPartCount++;
            }

            @Override
            public void onPartialRecognize(String text) {
                if (recordedStrings.size() == recordedPartCount) {
                    recordedStrings.add("");
                }
                recordedStrings.set(recordedPartCount, text);
                StringBuilder sb = new StringBuilder();
                for (String s : recordedStrings) {
                    sb.append(s).append('\n');
                }
                edtRecord.setText(sb.toString());
            }

            @Override
            public void onFail(String message) {
                openAlertModal(message, "Recognition Failed");
                showProgress(false);
            }
        };

        speechRecognizer = new MSSpeechRecognizer(this, recognizeDoneListener);

        GlobalUtils.removeTempFiles();
        Log.d("FileDir", GlobalUtils.RECORDED_TEMP_FILE_DIR.getPath());
    }

    private void setModals() {
        selectedDate = Calendar.getInstance();
        tvDiarySelectDate.setText(GlobalUtils.DIARY_DATE_FORMAT.format(selectedDate.getTime()));
        dlgDatePicker = new DatePickerDialog(this, datePickerListener,
                selectedDate.get(Calendar.YEAR), selectedDate.get(Calendar.MONTH),
                selectedDate.get(Calendar.DAY_OF_MONTH));

        dlgCancel = new AlertDialog.Builder(this).setMessage("Are you sure to cancel?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        GlobalUtils.removeTempFiles();
                        dialogInterface.dismiss();
                        NewAudioDiaryActivity.this.finish();
                    }
                }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                }).create();
        dlgAlert = new AlertDialog.Builder(this).setMessage("Message")
                .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                }).create();
        dlgRecord = new AlertDialog.Builder(this).setMessage("Are you sure to record again? " +
                "It'll remove the previous record!").setTitle("Record Confirm")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        clearText();
                        startRecording();
                    }
                }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                }).create();
    }

    //if recording is started successfully,
    public void startRecording() {
        recordedPartCount = 0;
        recordedStrings.clear();
        clearText();

        mWavRecorder = new WavRecorder();
        mWavRecorder.startRecord();
        setRecordingStatus(true);
    }

    //if recording is stopped successfully,
    public void stopRecording() {
        setRecordingStatus(false);
        mWavRecorder.stopRecord();
        showProgress(true);

        speechRecognizer.startRecognize();
    }

    private void clearText() {
        edtRecord.setText("");
    }

    private void addConvertedText(String text) {
        edtRecord.append(text + '\n');
    }

    private void setRecordingStatus(boolean bRecording) {
        if (bRecording) {
            nowRecordingStatus = true;
            btnStartRecord.setVisibility(View.GONE);
            btnStopRecord.setVisibility(View.VISIBLE);
            tvRecordStatus.setText("Press to stop recording");
        } else {
            nowRecordingStatus = false;
            btnStopRecord.setVisibility(View.GONE);
            btnStartRecord.setVisibility(View.VISIBLE);
            tvRecordStatus.setText("Press to start recording");
        }
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnStartRecord:
                Log.d("TAG", recordedPartCount+"");
                if (recordedPartCount > 0)
                    dlgRecord.show();
                else
                    startRecording();
                return;
            case R.id.btnStopRecord:
                stopRecording();
                return;
            case R.id.tvDiarySelectDate:
                dlgDatePicker.show();
                return;
            case R.id.btnRecordNext:
                Intent intent = new Intent(NewAudioDiaryActivity.this, WriteDiaryActivity.class);
                intent.putExtra("WRITE_DIARY_TYPE", "NEW_AUDIO");

                String content = edtRecord.getText().toString();
                String title = edtTitle.getText().toString();
                intent.putExtra("DIARY_TITLE", title);
                intent.putExtra("DIARY_DATE", selectedDate);
                intent.putExtra("DIARY_CONTENT", content);
                intent.putExtra("DIARY_AUDIO", mWavRecorder.getRecordFile());

                startActivity(intent);
                NewAudioDiaryActivity.this.finish();
                return;
        }
    }

    public void onActionMenuClick(View v) {
        switch (v.getId()) {
            case R.id.btnActionBarCancel:
                dlgCancel.show();
                return;
        }
    }

    private void openAlertModal(CharSequence msg) {
        openAlertModal(msg, "Alert");
    }

    private void openAlertModal(CharSequence msg, CharSequence title) {
        dlgAlert.setTitle(title);
        dlgAlert.setMessage(msg);
        dlgAlert.show();
    }

    private DatePickerDialog.OnDateSetListener datePickerListener =
            new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker datePicker, int year, int month, int dayOfMonth) {
                    Calendar cal = Calendar.getInstance();
                    cal.set(year, month, dayOfMonth, 0, 0);
                    selectedDate = cal;

                    tvDiarySelectDate.setText(year + ". " + (month + 1) + ". " + dayOfMonth + ".");
                }
            };


    private void showProgress(final boolean show) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            viewRecordLayout.setVisibility(show ? View.GONE : View.VISIBLE);
            viewRecordLayout.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    viewRecordLayout.setVisibility(show ? View.GONE : View.VISIBLE);
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
            viewRecordLayout.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }
}