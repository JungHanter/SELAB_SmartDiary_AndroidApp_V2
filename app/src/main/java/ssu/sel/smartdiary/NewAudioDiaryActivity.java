package ssu.sel.smartdiary;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ScrollView;
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
    private ScrollView scrollNewAudioDiaryForm = null;
    private View viewNewAudioDiaryForm = null;

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
        scrollNewAudioDiaryForm = (ScrollView)findViewById(R.id.viewNewAudioDiaryScroll);
        viewNewAudioDiaryForm = findViewById(R.id.viewNewAudioDiaryForm);

        recognizeDoneListener = new MSSpeechRecognizer.OnRecognizeDoneListener() {
            @Override
            public void onRecognizeDone() {
                Log.d("NewAudioDiaryActivity", "Recognition Done!!!");
                StringBuilder sb = new StringBuilder();
                for (String s : recordedStrings) {
                    sb.append(s).append('\n');
                }
                setDiaryText(sb.toString());
                showProgress(false);
            }

            @Override
            public void onPartialRecognizeDone(String text) {
                Log.d("NewAudioDiaryActivity", "A Part of Recognition Done!!!");
                if (recordedStrings.size() == recordedPartCount) {
                    recordedStrings.add("");
                }
                recordedStrings.set(recordedPartCount, text);
                StringBuilder sb = new StringBuilder();
                for (String s : recordedStrings) {
                    sb.append(s).append('\n');
                }
                setDiaryText(sb.toString());
                recordedPartCount++;
            }

            @Override
            public void onPartialRecognize(String text) {
//                Log.d("NewAudioDiaryActivity", "Partially Recognizing... " + text);
                if (recordedStrings.size() == recordedPartCount) {
                    recordedStrings.add("");
                }
                recordedStrings.set(recordedPartCount, "<font color='#999999'>" + text + "</font>");
                StringBuilder sb = new StringBuilder();
                for (String s : recordedStrings) {
                    sb.append(s).append("<br/>");
                }
                setDiaryText(Html.fromHtml(sb.toString()));
            }

            @Override
            public void onFail(String message) {
                Log.d("NewAudioDiaryActivity", "Recognition Failed");
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
                        mWavRecorder.stopRecord();
                        WavRecorder.removeRecordedTempFiles();
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
                        clearDiaryText();
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
        clearDiaryText();

        mWavRecorder = new WavRecorder();
        mWavRecorder.startRecord();
        setRecordingStatus(true);
    }

    //if recording is stopped successfully,
    public void stopRecording() {
//        showProgress(true);
        showProgressUsingHandler(true);
        setRecordingStatus(false);

        mWavRecorder.stopRecord();
        speechRecognizer.startRecognize();
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


    private void clearDiaryText() {
        edtRecord.setText("");
    }

    private void setDiaryText(String text) {
        edtRecord.setText(text);
        scrollNewAudioDiaryForm.post(new Runnable() {
            @Override
            public void run() {
                scrollNewAudioDiaryForm.smoothScrollTo(0, viewNewAudioDiaryForm.getBottom());
            }
        });
    }

    private void setDiaryText(Spanned spannedText) {
        edtRecord.setText(spannedText);
        scrollNewAudioDiaryForm.post(new Runnable() {
            @Override
            public void run() {
                scrollNewAudioDiaryForm.smoothScrollTo(0, viewNewAudioDiaryForm.getBottom());
            }
        });
    }

    private void showProgress(final boolean show) {
        viewProgress.setVisibility(show ? View.VISIBLE : View.GONE);
        viewRecordLayout.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    private void showProgressUsingHandler(final boolean show) {
        Message msg = Message.obtain(showProgressHandler);
        msg.arg1 = show ? 1 : 0;
        showProgressHandler.sendMessage(msg);
    }

    private Handler showProgressHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if(msg.arg1 == 0) showProgress(false);
            else showProgress(true);
        }
    };
}