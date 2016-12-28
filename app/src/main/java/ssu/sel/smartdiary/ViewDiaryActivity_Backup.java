//package ssu.sel.smartdiary;
//
//import android.content.DialogInterface;
//import android.content.Intent;
//import android.os.Bundle;
//import android.support.v7.app.ActionBar;
//import android.support.v7.app.AlertDialog;
//import android.util.Log;
//import android.view.Gravity;
//import android.view.MenuItem;
//import android.view.View;
//import android.widget.Button;
//import android.widget.EditText;
//import android.widget.ScrollView;
//import android.widget.TextView;
//
//import org.json.JSONArray;
//import org.json.JSONObject;
//
//import java.util.ArrayList;
//import java.util.Calendar;
//
//import ssu.sel.smartdiary.model.Diary;
//import ssu.sel.smartdiary.model.DiaryEnvContext;
//import ssu.sel.smartdiary.model.UserProfile;
//import ssu.sel.smartdiary.network.AudioDownloadConnector;
//import ssu.sel.smartdiary.network.JsonRestConnector;
//
///**
// * Created by hanter on 16. 10. 7..
// */
//public class ViewDiaryActivity_Backup extends WriteDiaryActivity {
//    private Button btnConfirm = null;
//
//    private int diaryID = -1;
//    private Diary nowDiary = null;
//
//    private AlertDialog dlgDeleteDone = null;
//    private AlertDialog dlgDeleteConfirm = null;
//
//    private JsonRestConnector getDiaryInfoConnector = null;
//    private JsonRestConnector deleteDiaryConnector = null;
//    private AudioDownloadConnector downloadConnector = null;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//
//        showProgress(true);
//        if (diaryID != -1) {
//            JSONObject json = new JSONObject();
//            try {
//                json.put("user_id", UserProfile.getUserProfile().getUserID());
//                json.put("audio_diary_id", diaryID);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//            getDiaryInfoConnector.request(json);
//        }
//    }
//
//    @Override
//    protected void initView() {
//        ActionBar actionBar = getSupportActionBar();
//        View actionbarView = getLayoutInflater().inflate(R.layout.action_bar_view_diary_noedit, null);
//        actionBar.setCustomView(actionbarView,
//                new ActionBar.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT,
//                        ActionBar.LayoutParams.MATCH_PARENT, Gravity.CENTER));
//        actionBar.setDisplayShowCustomEnabled(true);
//        actionBar.setDisplayHomeAsUpEnabled(true);
//
//        setContentView(R.layout.activity_write_diary);
//
//        Intent intent = getIntent();
//        diaryID = intent.getIntExtra("DIARY_ID", -1);
//        if (diaryID == -1) throw new NullPointerException("No Diary ID.");
//        Log.d("EditDiaryActivity", "diaryID = " + diaryID);
//
//        edtTitle = (EditText)findViewById(R.id.edtDiaryTitle);
//        edtContent = (EditText)findViewById(R.id.edtDiaryContent);
//        tvDiarySelectDate = (TextView) findViewById(R.id.tvDiarySelectDate);
//        tvDiarySelectTime = (TextView) findViewById(R.id.tvDiarySelectTime);
//        edtAnnotation = (EditText)findViewById(R.id.edtAnnotation);
//        edtEnvPlace = (EditText)findViewById(R.id.edtEnvPlace);
//        edtEnvWeather = (EditText) findViewById(R.id.edtEnvWeather);
//        edtEnvEvents = (EditText) findViewById(R.id.edtEnvEvents);
//        btnConfirm = (Button)findViewById(R.id.btnDiaryConfirm);
//        viewWriteDiaryLayout = (ScrollView) findViewById(R.id.viewWriteDiaryForm);
//        viewProgress = findViewById(R.id.progressLayout);
//        tvDiaryAudioDownloading = (TextView) findViewById(R.id.tvDiaryAudioDownloading);
//        btnDiaryAudioPlay = (Button) findViewById(R.id.btnDiaryAudioPlay);
//        btnDiaryAudioPause = (Button) findViewById(R.id.btnDiaryAudioPause);
//        btnDiaryAudioForward = (Button) findViewById(R.id.btnDiaryAudioForward);
//        btnDiaryAudioBackward = (Button) findViewById(R.id.btnDiaryAudioBackward);
//
//        diaryAcitivityType = "VIEW";
//        setDiaryViews();
//
//        edtTitle.setText("");
//        edtContent.setText("");
//        selectedDate = Calendar.getInstance();
//
//        tvDiaryAudioDownloading.setVisibility(View.VISIBLE);
//        btnDiaryAudioPlay.setVisibility(View.GONE);
//        btnDiaryAudioPause.setVisibility(View.GONE);
//
//        setModals();
//        setJsonConnectors();
//    }
//
//    // update diary
//    @Override
//    protected void saveDiary() {
//        return;
//    }
//
//    @Override
//    protected void setJsonConnectors() {
//        getDiaryInfoConnector = new JsonRestConnector("diary/detail", "GET",
//                new JsonRestConnector.OnConnectListener() {
//                    @Override
//                    public void onDone(JSONObject resJson) {
//                        if (resJson == null) {
//                            Log.d("Main - Json", "No response");
//                            openAlertModal("No response.", "Error");
//                        } else {
//                            try {
//                                if (resJson.has("result_detail") && resJson.getBoolean("retrieve_diary")) {
//                                    JSONObject diary = resJson.getJSONObject("result_detail");
//                                    JSONArray diaryContexts = null;
//                                    if (resJson.has("result_context")) {
//                                        diaryContexts = resJson.getJSONArray("result_context");
//                                    }
//
//                                    nowDiary = Diary.fromJSON(diary, diaryContexts);
//                                    setDiary(nowDiary);
//
//                                    if (!GlobalUtils.getAudioDiaryFile(UserProfile.getUserProfile().getUserID(),
//                                            nowDiary.getDiaryID()).exists()) {
//                                        //request file download
//                                        downloadConnector.request(UserProfile.getUserProfile().getUserID(),
//                                                nowDiary.getDiaryID());
//                                    } else {
//                                        diaryAudioFile = GlobalUtils.getAudioDiaryFile(UserProfile.getUserProfile().getUserID(),
//                                                nowDiary.getDiaryID());
//                                        setAudioPlayer();
//                                    }
//
//                                    //TODO get analytics
////                                    showAnalyticsProgress(true);
////                                    JSONObject analyticsJson = new JSONObject();
////                                    analyticsJson.put("diary_id", diaryID);
////                                    analyzeDiaryConnector.request(analyticsJson);
//                                } else {
//                                    nowDiary = null;
//                                    Log.d("Main - Json", "Retrieve diary failed");
//                                }
//                            } catch (Exception e) {
//                                nowDiary = null;
//                                Log.d("Main - Json", "Json parsing error");
//                                openAlertModal("Json parsing error.", "Error");
//                                e.printStackTrace();
//                            }
//                        }
//                        showProgress(false);
//                    }
//                });
//
//        downloadConnector = new AudioDownloadConnector("download", "POST",
//                new AudioDownloadConnector.OnConnectListener() {
//                    @Override
//                    public void onDone(Boolean success) {
//                        if (success) {
//                            diaryAudioFile = GlobalUtils.getAudioDiaryFile(UserProfile.getUserProfile().getUserID(),
//                                    nowDiary.getDiaryID());
//                            setAudioPlayer();
//                            Log.d("ViewDiaryActivity", "File downloaded");
//                        } else  {
//                            openAlertModal("Downloading audio file is failed.");
//                            tvDiaryAudioDownloading.setText("Downloading Audio Failed");
//                        }
//                    }
//                });
//
//        deleteDiaryConnector = new JsonRestConnector("diary/delete", "POST",
//                new JsonRestConnector.OnConnectListener() {
//                    @Override
//                    public void onDone(JSONObject resJson) {
//                        if (resJson == null) {
//                            Log.d("Main - Json", "No response");
//                            openAlertModal("No response.", "Error");
//                        } else {
//                            try {
//                                if (resJson.getBoolean("delete_diary")) {
//                                    try {
//                                        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
//                                            mediaPlayer.stop();
//                                            mediaPlayer.release();
//                                        }
//                                        GlobalUtils.getAudioDiaryFile(UserProfile.getUserProfile().getUserID(),
//                                                nowDiary.getDiaryID()).delete();
//                                    } catch (Exception e) {}
//                                    mediaPlayer = null;
//                                    nowDiary = null;
//
//                                    dlgDeleteDone.show();
//                                } else {
//                                    Log.d("Main - Json", "Delete diary failed");
//                                    openAlertModal("Delete Diary Failed.", "Delete Error");
//                                }
//                            } catch (Exception e) {
//                                Log.d("Main - Json", "Json parsing error");
//                                openAlertModal("Json parsing error.", "Error");
//                                e.printStackTrace();
//                            }
//                        }
//
//                        showProgress(false);
//                    }
//                });
//    }
//
//    @Override
//    public void onClick(View v) {
//        switch (v.getId()) {
//            case R.id.btnDiaryConfirm:
//                dlgConfirm.show();
//                return;
//            case R.id.tvDiarySelectDate:
//                if (diaryAcitivityType.equals("EDIT"))
//                    dlgDatePicker.show();
//                return;
//        }
//    }
//
//    @Override
//    public void onActionMenuClick(View v) {
//        switch (v.getId()) {
//            case R.id.btnActionBarDelete:
//                dlgDeleteConfirm.show();
//                return;
//        }
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        switch (item.getItemId()) {
//            case android.R.id.home:
//                ViewDiaryActivity_Backup.this.finish();
//                return true;
//            default:
//                return super.onOptionsItemSelected(item);
//        }
//    }
//
//    @Override
//    protected void setModals() {
//        super.setModals();
//        dlgDeleteConfirm = new AlertDialog.Builder(this).setMessage("Are you sure to delete diary?")
//                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialogInterface, int i) {
//                        JSONObject json = new JSONObject();
//                        try {
//                            json.put("user_id", UserProfile.getUserProfile().getUserID());
//                            json.put("audio_diary_id", nowDiary.getDiaryID());
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                        }
//
//                        // STOP
//                        if (mediaPlayer!=null && mediaPlayer.isPlaying()) mediaPlayer.pause();
//                        btnDiaryAudioPause.setVisibility(View.GONE);
//                        btnDiaryAudioPlay.setVisibility(View.VISIBLE);
//
//                        showProgress(true);
//                        deleteDiaryConnector.request(json);
//                    }
//                }).setNegativeButton("No", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialogInterface, int i) {
//                        dialogInterface.dismiss();
//                    }
//                }).create();
//
//        dlgDeleteDone = new AlertDialog.Builder(this).setMessage("The diary is deleted.")
//                .setNeutralButton("OK", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialogInterface, int i) {
//                        ViewDiaryActivity_Backup.this.finish();
//                    }
//                }).setCancelable(false).create();
//    }
//
//    private void setDiary(Diary diary) {
//        edtTitle.setText(diary.getTitle());
//        tvDiarySelectDate.setText(GlobalUtils.DIARY_DATE_FORMAT.format(
//                diary.getDate().getTime()));
//        edtContent.setText(diary.getContent());
//
//        DiaryEnvContext annotation = diary.getAnnotation();
//        if (annotation != null)
//            edtAnnotation.setText(annotation.getValue());
//
//        ArrayList<DiaryEnvContext> envPlaces =
//                diary.getDiaryEnvContexts(DiaryEnvContext.CONTEXT_TYPE_ENVIRONMENT,
//                        DiaryEnvContext.TYPE_ENV_PLACE);
//        ArrayList<DiaryEnvContext> envWeathers =
//                diary.getDiaryEnvContexts(DiaryEnvContext.CONTEXT_TYPE_ENVIRONMENT,
//                        DiaryEnvContext.TYPE_ENV_WEATHER);
//        ArrayList<DiaryEnvContext> envEvents =
//                diary.getDiaryEnvContexts(DiaryEnvContext.CONTEXT_TYPE_ENVIRONMENT,
//                        DiaryEnvContext.TYPE_ENV_EVENT);
//
//        edtEnvPlace.setText(DiaryEnvContext.diaryContextsToString(envPlaces));
//        edtEnvWeather.setText(DiaryEnvContext.diaryContextsToString(envWeathers));
//        edtEnvEvents.setText(DiaryEnvContext.diaryContextsToString(envEvents));
//    }
//
//    private void setDiaryViews() {
//        edtTitle.setFocusable(false);
//        tvDiarySelectDate.setClickable(false);
//        edtContent.setFocusable(false);
//        edtAnnotation.setFocusable(false);
//        edtEnvPlace.setFocusable(false);
//        edtEnvWeather.setFocusable(false);
//        edtEnvEvents.setFocusable(false);
//        btnConfirm.setVisibility(View.GONE);
//    }
//}
