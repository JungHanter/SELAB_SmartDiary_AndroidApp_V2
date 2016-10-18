package ssu.sel.smartdiary;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import ssu.sel.smartdiary.model.UserProfile;
import ssu.sel.smartdiary.network.MultipartRestConnector;

public class WriteDiaryActivity extends AppCompatActivity {

    protected String diaryAcitivityType = null;

    protected EditText edtTitle = null;
    protected EditText edtContent = null;
    protected TextView tvDiarySelectDate = null;

    protected EditText edtAnnotation = null;
    protected EditText edtEnvLocation = null;

    protected View viewWirteDiaryLayout = null;
    protected View viewProgress = null;

    protected AlertDialog dlgAlert = null;
    protected AlertDialog dlgCancel = null;
    protected AlertDialog dlgConfirm = null;
    protected Calendar selectedDate;
    protected DatePickerDialog dlgDatePicker = null;
    protected DateFormat diaryDateFormat = new SimpleDateFormat("yyyy. M. dd.", Locale.getDefault());

    protected MultipartRestConnector saveDiaryConnector = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
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
            ((TextView)mActionBarView.findViewById(R.id.tvActionBarTitle)).setText("New Text Diary");
        else if (diaryAcitivityType.equals("NEW_AUDIO"))
            ((TextView)mActionBarView.findViewById(R.id.tvActionBarTitle)).setText("New Audio Diary");
        else
            throw new NullPointerException("No Intent for WriteDiaryType!");
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(false);

        setContentView(R.layout.activity_write_diary);

        edtTitle = (EditText)findViewById(R.id.edtDiaryTitle);
        edtContent = (EditText)findViewById(R.id.edtDiaryContent);
        tvDiarySelectDate = (TextView) findViewById(R.id.tvDiarySelectDate);
        edtAnnotation = (EditText)findViewById(R.id.edtAnnotation);
        edtEnvLocation = (EditText)findViewById(R.id.edtEnvLocation);
        viewWirteDiaryLayout = findViewById(R.id.viewWriteDiaryForm);
        viewProgress = findViewById(R.id.progressLayout);

        if (diaryAcitivityType.equals("NEW_AUDIO")) {
            edtTitle.setText(intent.getStringExtra("DIARY_TITLE"));
            edtContent.setText(intent.getStringExtra("DIARY_CONTENT"));
            selectedDate = (Calendar) intent.getSerializableExtra("DIARY_DATE");
        } else {
            selectedDate = Calendar.getInstance();
        }
        setModals();

        setJsonConnectors();
    }

    protected void setModals() {
        tvDiarySelectDate.setText(diaryDateFormat.format(selectedDate.getTime()));
        dlgDatePicker = new DatePickerDialog(this, datePickerListener,
                selectedDate.get(Calendar.YEAR), selectedDate.get(Calendar.MONTH),
                selectedDate.get(Calendar.DAY_OF_MONTH));

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
                            GlobalUtils.removeTempFiles();
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
                                Boolean success = resJson.getBoolean("create_diary");
                                if (success) {
                                    if (diaryAcitivityType.equals("NEW_AUDIO")) {
                                        GlobalUtils.removeTempFiles();
                                    }

                                    WriteDiaryActivity.this.finish();
                                } else {
                                    dlgAlert.setMessage("Diary Create Failed");
                                    dlgAlert.show();
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
        JSONObject json = new JSONObject();

        try {
            json.put("user_id", UserProfile.getUserProfile().getUserID());
            json.put("timestamp", selectedDate.getTime().getTime());
            json.put("title", edtTitle.getText().toString());
            json.put("text", edtContent.getText().toString());
            json.put("annotation", edtAnnotation.getText().toString());
            json.put("location", edtEnvLocation.getText().toString());
        } catch (Exception e) {
            e.printStackTrace();
            dlgAlert.setMessage("JSON Creation Error");
            dlgAlert.show();
            return;
        }

        showProgress(true);
        if (diaryAcitivityType.equals("NEW_AUDIO")) {
            saveDiaryConnector.request(GlobalUtils.RECORDED_TEMP_FILE_DIR, json);
        } else {
            saveDiaryConnector.request(null, json);
        }
    }

    public void onActionMenuClick(View v) {
        switch (v.getId()) {
            case R.id.btnActionBarCancel:
                dlgCancel.show();
                ((TextView)v.findViewById(R.id.tvOfBtnActionBarCancel)).setText("Edit");
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
        }
    }

    protected DatePickerDialog.OnDateSetListener datePickerListener =
            new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker datePicker, int year, int month, int dayOfMonth) {
                    Calendar cal = Calendar.getInstance();
                    cal.set(year, month, dayOfMonth, 0, 0);
                    selectedDate = cal;

                    tvDiarySelectDate.setText(GlobalUtils.DIARY_DATE_FORMAT.format(selectedDate.getTime()));
                }
            };

    protected void showProgress(final boolean show) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            viewWirteDiaryLayout.setVisibility(show ? View.GONE : View.VISIBLE);
            viewProgress.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    viewWirteDiaryLayout.setVisibility(show ? View.GONE : View.VISIBLE);
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
            viewWirteDiaryLayout.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }
}
