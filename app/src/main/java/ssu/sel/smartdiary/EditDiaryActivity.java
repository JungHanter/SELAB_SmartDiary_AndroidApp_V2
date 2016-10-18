package ssu.sel.smartdiary;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONObject;

import java.util.Calendar;

import ssu.sel.smartdiary.model.Diary;
import ssu.sel.smartdiary.model.UserProfile;
import ssu.sel.smartdiary.network.JsonRestConnector;
import ssu.sel.smartdiary.network.MultipartRestConnector;

/**
 * Created by hanter on 16. 10. 7..
 */
public class EditDiaryActivity extends WriteDiaryActivity {
    private View mActionBarViewDiary = null;
    private View mActionBarEditDiary = null;

    private Button btnConfirm = null;

    private View viewDiaryAnalytics = null;
    private View viewPositiveAnalytics = null;
    private TextView tvPositiveAnalytics = null;
    private View viewProgressAnalytics = null;
    private TextView tvNoAnalytics = null;

    private int diaryID = -1;
    private Diary nowDiary = null;
    private Diary editedDiary = null;

    private AlertDialog dlgDeleteDone = null;
    private AlertDialog dlgDeleteConfirm = null;

    private JsonRestConnector getDiaryInfoConnector = null;
    private JsonRestConnector deleteDiaryInfoConnector = null;
    private JsonRestConnector analyzeDiaryConnector = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        showProgress(true);
        if (diaryID != -1) {
            JSONObject json = new JSONObject();
            try {
                json.put("user_id", UserProfile.getUserProfile().getUserID());
                json.put("diary_id", diaryID);
            } catch (Exception e) {
                e.printStackTrace();
            }
            getDiaryInfoConnector.request(json);
        }
    }

    @Override
    protected void initView() {
        ActionBar actionBar = getSupportActionBar();
        mActionBarViewDiary = getLayoutInflater().inflate(R.layout.action_bar_view_diary, null);
        mActionBarEditDiary = getLayoutInflater().inflate(R.layout.action_bar_edit_diary, null);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        setContentView(R.layout.activity_write_diary);

        Intent intent = getIntent();
        diaryID = intent.getIntExtra("DIARY_ID", -1);
        if (diaryID == -1) throw new NullPointerException("No Diary ID.");
        Log.d("EditDiaryActivity", "diaryID = " + diaryID);

        edtTitle = (EditText)findViewById(R.id.edtDiaryTitle);
        edtContent = (EditText)findViewById(R.id.edtDiaryContent);
        tvDiarySelectDate = (TextView) findViewById(R.id.tvDiarySelectDate);
        edtAnnotation = (EditText)findViewById(R.id.edtAnnotation);
        edtEnvLocation = (EditText)findViewById(R.id.edtEnvLocation);
        btnConfirm = (Button)findViewById(R.id.btnDiaryConfirm);
        viewWirteDiaryLayout = findViewById(R.id.viewWriteDiaryForm);
        viewProgress = findViewById(R.id.progressLayout);

        viewDiaryAnalytics = findViewById(R.id.viewDiaryAnalytics);
        viewPositiveAnalytics = findViewById(R.id.viewPositiveAnalytics);
        tvPositiveAnalytics = (TextView)findViewById(R.id.tvPositiveAnalytics);
        viewProgressAnalytics = findViewById(R.id.progressAnalyticsLayout);
        tvNoAnalytics = (TextView)findViewById(R.id.tvNoAnalytics);

        diaryAcitivityType = "VIEW";
        setDiaryViews();
        setViewActionBarButtons(false);

        edtTitle.setText("");
        edtContent.setText("");
        selectedDate = Calendar.getInstance();

        setModals();

        setJsonConnectors();
    }

    // update diary
    @Override
    protected void saveDiary() {
        editedDiary = new Diary(diaryID, edtTitle.getText().toString(),
                (Calendar)selectedDate.clone(), edtContent.getText().toString(),
                edtAnnotation.getText().toString(), edtEnvLocation.getText().toString());

        JSONObject json = new JSONObject();
        try {
            json.put("user_id", UserProfile.getUserProfile().getUserID());
            json.put("diary_id", editedDiary.getDiaryID());
            json.put("title", editedDiary.getTitle());
            json.put("timestamp", editedDiary.getDate().getTimeInMillis());
            json.put("text", editedDiary.getContent());
            json.put("annotation", editedDiary.getAnnotation());
            json.put("location", editedDiary.getLocation());
        } catch (Exception e) {
            e.printStackTrace();
            dlgAlert.setMessage("JSON Creation Error");
            dlgAlert.show();
            return;
        }

        Log.d("EditDiary - Json", json.toString());

        showProgress(true);
        setEditActionBarButtons(false);
        saveDiaryConnector.request(null, json);
    }

    @Override
    protected void setJsonConnectors() {
        getDiaryInfoConnector = new JsonRestConnector("diary/detail", "GET",
                new JsonRestConnector.OnConnectListener() {
                    @Override
                    public void onDone(JSONObject resJson) {
                        if (resJson == null) {
                            Log.d("Main - Json", "No response");
                            openAlertModal("No response.", "Error");
                        } else {
                            try {
                                if (resJson.has("result") && resJson.getBoolean("retrieve_diary")) {
                                    JSONObject diary = resJson.getJSONObject("result");

                                    long time = diary.getLong("timestamp");
                                    Calendar calendar = Calendar.getInstance();
                                    calendar.setTimeInMillis(time);

                                    nowDiary = new Diary(diaryID, diary.getString("title"), calendar,
                                            diary.getString("text"), diary.getString("annotation"),
                                            diary.getString("location"));

                                    edtTitle.setText(nowDiary.getTitle());
                                    tvDiarySelectDate.setText(GlobalUtils.DIARY_DATE_FORMAT.format(calendar.getTime()));
                                    edtContent.setText(nowDiary.getContent());
                                    edtAnnotation.setText(nowDiary.getAnnotation());
                                    edtEnvLocation.setText(nowDiary.getLocation());

                                    setViewActionBarButtons(true);

                                    // get analytics
                                    showAnalyticsProgress(true);
                                    JSONObject analyticsJson = new JSONObject();
                                    analyticsJson.put("diary_id", diaryID);
                                    analyzeDiaryConnector.request(analyticsJson);
                                } else {
                                    nowDiary = null;
                                    Log.d("Main - Json", "Retrieve diary failed");
                                }
                            } catch (Exception e) {
                                nowDiary = null;
                                Log.d("Main - Json", "Json parsing error");
                                openAlertModal("Json parsing error.", "Error");
                                e.printStackTrace();
                            }
                        }
                        showProgress(false);
                    }
                });

        saveDiaryConnector = new MultipartRestConnector("diary/update", "POST",
                new MultipartRestConnector.OnConnectListener() {
                    @Override
                    public void onDone(JSONObject resJson) {
                        if (resJson == null) {
                            Log.d("Main - Json", "No response");
                            openAlertModal("No response.", "Error");
                        } else {
                            try {
                                if (resJson.getBoolean("update_diary")) {
                                    nowDiary = editedDiary;
                                    editedDiary = null;

                                    diaryAcitivityType = "VIEW";
                                    setDiaryViews();

                                    // get analytics again
                                    showAnalyticsProgress(true);
                                    JSONObject analyticsJson = new JSONObject();
                                    analyticsJson.put("diary_id", diaryID);
                                    analyzeDiaryConnector.request(analyticsJson);
                                } else {
                                    Log.d("Main - Json", "Update diary failed");
                                    openAlertModal("Update Diary Failed.", "Update Error");
                                    editedDiary = null;

                                }
                            } catch (Exception e) {
                                Log.d("Main - Json", "Json parsing error");
                                openAlertModal("Json parsing error.", "Error");
                                e.printStackTrace();
                            }
                        }
                        showProgress(false);
                        setViewActionBarButtons(true);
                    }
                });

        deleteDiaryInfoConnector = new JsonRestConnector("diary/delete", "POST",
                new JsonRestConnector.OnConnectListener() {
                    @Override
                    public void onDone(JSONObject resJson) {
                        if (resJson == null) {
                            Log.d("Main - Json", "No response");
                            openAlertModal("No response.", "Error");
                        } else {
                            try {
                                if (resJson.getBoolean("delete_diary")) {
                                    dlgDeleteDone.show();
                                } else {
                                    Log.d("Main - Json", "Delete diary failed");
                                    openAlertModal("Delete Diary Failed.", "Delete Error");
                                }
                            } catch (Exception e) {
                                Log.d("Main - Json", "Json parsing error");
                                openAlertModal("Json parsing error.", "Error");
                                e.printStackTrace();
                            }
                        }

                        showProgress(false);
                        setViewActionBarButtons(true);
                    }
                });

        analyzeDiaryConnector = new JsonRestConnector("semantic", "GET",
                new JsonRestConnector.OnConnectListener() {
                    @Override
                    public void onDone(JSONObject resJson) {
                        if (resJson == null) {
                            Log.d("Main - Json", "No response");
                            openAlertModal("No response.", "Error");
                        } else {
                            try {
                                showAnalyticsProgress(false);
                                if (resJson.getBoolean("find_semantic")) {
                                    viewPositiveAnalytics.setVisibility(View.VISIBLE);
                                    tvPositiveAnalytics.setText(resJson.getString("result"));
                                    tvNoAnalytics.setVisibility(View.GONE);
                                } else {
                                    viewPositiveAnalytics.setVisibility(View.GONE);
                                    tvNoAnalytics.setVisibility(View.VISIBLE);
                                }
                            } catch (Exception e) {
                                Log.d("Main - Json", "Json parsing error");
                                openAlertModal("Json parsing error.", "Error");
                                e.printStackTrace();
                                viewPositiveAnalytics.setVisibility(View.GONE);
                                tvNoAnalytics.setVisibility(View.VISIBLE);
                            }
                        }

                        showAnalyticsProgress(false);
                        viewDiaryAnalytics.setVisibility(View.VISIBLE);
                    }
                });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnDiaryConfirm:
                dlgConfirm.show();
                return;
            case R.id.tvDiarySelectDate:
                if (diaryAcitivityType.equals("EDIT"))
                    dlgDatePicker.show();
                return;
        }
    }

    @Override
    public void onActionMenuClick(View v) {
        switch (v.getId()) {
            case R.id.btnActionBarEdit:
                diaryAcitivityType = "EDIT";
                setDiaryViews();
                viewDiaryAnalytics.setVisibility(View.GONE);
                return;
            case R.id.btnActionBarDelete:
                dlgDeleteConfirm.show();
                return;
            case R.id.btnActionBarCancel:
                dlgCancel.show();
                return;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                EditDiaryActivity.this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void setModals() {
        super.setModals();
        dlgCancel = new AlertDialog.Builder(this).setMessage("Are you sure to cancel edit?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        diaryAcitivityType = "VIEW";
                        editedDiary = null;
                        setDiary(nowDiary);
                        setDiaryViews();
                        viewDiaryAnalytics.setVisibility(View.VISIBLE);
                        dialogInterface.dismiss();
                    }
                }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                }).create();

        dlgDeleteConfirm = new AlertDialog.Builder(this).setMessage("Are you sure to delete diary?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        JSONObject json = new JSONObject();
                        try {
                            json.put("user_id", UserProfile.getUserProfile().getUserID());
                            json.put("diary_id", nowDiary.getDiaryID());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        showProgress(true);
                        setEditActionBarButtons(false);
                        deleteDiaryInfoConnector.request(json);
                    }
                }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                }).create();

        dlgDeleteDone = new AlertDialog.Builder(this).setMessage("The diary is deleted.")
                .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        EditDiaryActivity.this.finish();
                    }
                }).setCancelable(false).create();
    }

    private void setDiary(Diary diary) {
        edtTitle.setText(diary.getTitle());
        tvDiarySelectDate.setText(GlobalUtils.DIARY_DATE_FORMAT.format(diary.getDate().getTime()));
        edtContent.setText(diary.getContent());
        edtAnnotation.setText(diary.getAnnotation());
        edtEnvLocation.setText(diary.getLocation());
    }

    private void setDiaryViews() {
        ActionBar actionBar = getSupportActionBar();
        if (diaryAcitivityType.equals("VIEW")) {
            actionBar.setCustomView(mActionBarViewDiary,
                    new ActionBar.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT,
                            ActionBar.LayoutParams.MATCH_PARENT, Gravity.CENTER));
            actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            actionBar.setDisplayHomeAsUpEnabled(true);

            edtTitle.setFocusable(false);
            tvDiarySelectDate.setClickable(false);
            edtContent.setFocusable(false);
            edtAnnotation.setFocusable(false);
            edtEnvLocation.setFocusable(false);
            btnConfirm.setVisibility(View.GONE);

        } else if (diaryAcitivityType.equals("EDIT")) {
            actionBar.setCustomView(mActionBarEditDiary,
                    new ActionBar.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT,
                            ActionBar.LayoutParams.MATCH_PARENT, Gravity.CENTER));
            actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            actionBar.setDisplayHomeAsUpEnabled(false);

//            edtTitle.setClickable(true);
            edtTitle.setFocusable(true);
            edtTitle.setFocusableInTouchMode(true);
            tvDiarySelectDate.setClickable(true);
            edtContent.setFocusable(true);
            edtContent.setFocusableInTouchMode(true);
            edtAnnotation.setFocusable(true);
            edtAnnotation.setFocusableInTouchMode(true);
            edtEnvLocation.setFocusable(true);
            edtEnvLocation.setFocusableInTouchMode(true);
            btnConfirm.setVisibility(View.VISIBLE);
        }
    }

    private void setViewActionBarButtons(boolean bOn) {
        if (bOn) {
            mActionBarViewDiary.findViewById(R.id.btnActionBarEdit).setVisibility(View.VISIBLE);
        } else {
            mActionBarViewDiary.findViewById(R.id.btnActionBarEdit).setVisibility(View.INVISIBLE);
        }
    }

    private void setEditActionBarButtons(boolean bOn) {
        if (bOn) {
            mActionBarEditDiary.findViewById(R.id.btnActionBarCancel).setVisibility(View.VISIBLE);
            mActionBarEditDiary.findViewById(R.id.btnActionBarDelete).setVisibility(View.VISIBLE);
        } else {
            mActionBarEditDiary.findViewById(R.id.btnActionBarCancel).setVisibility(View.INVISIBLE);
            mActionBarEditDiary.findViewById(R.id.btnActionBarDelete).setVisibility(View.INVISIBLE);
        }
    }

    private void showAnalyticsProgress(final boolean show) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            viewProgressAnalytics.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                }
            });

            viewProgressAnalytics.setVisibility(show ? View.VISIBLE : View.GONE);
            viewProgressAnalytics.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    viewProgressAnalytics.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            viewProgressAnalytics.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }
}
