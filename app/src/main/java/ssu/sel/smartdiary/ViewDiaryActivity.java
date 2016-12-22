package ssu.sel.smartdiary;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;

import ssu.sel.smartdiary.model.Diary;
import ssu.sel.smartdiary.model.DiaryContext;
import ssu.sel.smartdiary.model.MediaContext;
import ssu.sel.smartdiary.model.UserProfile;
import ssu.sel.smartdiary.network.DiaryAudioDownloadConnector;
import ssu.sel.smartdiary.network.JsonRestConnector;
import ssu.sel.smartdiary.network.MediaContextDownloadConnector;
import ssu.sel.smartdiary.view.AudioPlayerView;
import ssu.sel.smartdiary.view.MediaContextLoadingView;

/**
 * Created by hanter on 16. 10. 7..
 */
public class ViewDiaryActivity extends WriteDiaryActivity {
    private View viewModeActionbar = null;
    private View editModeActionbar = null;

    private Button btnConfirm = null;

    private View layoutAttachment = null;

    private int diaryID = -1;
    private Diary nowDiary = null;

    private AlertDialog dlgDeleteDone = null;
    private AlertDialog dlgDeleteConfirm = null;

    private JsonRestConnector getDiaryInfoConnector = null;
    private JsonRestConnector deleteDiaryConnector = null;
    private DiaryAudioDownloadConnector diaryAudioDownloadConnector = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        showProgress(true);
        if (diaryID != -1) {
            JSONObject json = new JSONObject();
            try {
                json.put("user_id", UserProfile.getUserProfile().getUserID());
                json.put("audio_diary_id", diaryID);
            } catch (Exception e) {
                e.printStackTrace();
            }
            getDiaryInfoConnector.request(json);
        }
    }

    @Override
    protected void initView() {
        viewModeActionbar = getLayoutInflater().inflate(R.layout.action_bar_view_diary, null);
        editModeActionbar = getLayoutInflater().inflate(R.layout.action_bar_edit_diary, null);

        setContentView(R.layout.activity_write_diary);

        Intent intent = getIntent();
        diaryID = intent.getIntExtra("DIARY_ID", -1);
        if (diaryID == -1) throw new NullPointerException("No Diary ID.");
        Log.d("EditDiaryActivity", "diaryID = " + diaryID);

        edtTitle = (EditText)findViewById(R.id.edtDiaryTitle);
        edtContent = (EditText)findViewById(R.id.edtDiaryContent);
        tvDiarySelectDate = (TextView) findViewById(R.id.tvDiarySelectDate);
        tvDiarySelectTime = (TextView) findViewById(R.id.tvDiarySelectTime);
        edtAnnotation = (EditText)findViewById(R.id.edtAnnotation);
        edtEnvPlace = (EditText)findViewById(R.id.edtEnvPlace);
        edtEnvWeather = (EditText) findViewById(R.id.edtEnvWeather);
        edtEnvHolidays = (EditText) findViewById(R.id.edtHolidays);
        edtEnvEvents = (EditText) findViewById(R.id.edtEnvEvents);
        btnConfirm = (Button)findViewById(R.id.btnDiaryConfirm);
        viewWriteDiaryLayout = (ScrollView) findViewById(R.id.viewWriteDiaryForm);
        viewProgress = findViewById(R.id.progressLayout);
        diaryRecordAudioPlayer = (AudioPlayerView) findViewById(R.id.audioPlayerDiaryRecord);
        layoutAttachment = findViewById(R.id.layoutAttachmentBtns);
        layoutAttachment.setVisibility(View.GONE);
        layoutAttachmentFiles = (LinearLayout) findViewById(R.id.layoutAttachmentFiles);

        edtEnvPlace.setFocusable(false);
        edtEnvWeather.setFocusable(false);
        edtEnvHolidays.setFocusable(false);
        edtEnvEvents.setFocusable(false);

        diaryAcitivityType = "VIEW";

        edtTitle.setText("");
        edtContent.setText("");
        selectedDate = Calendar.getInstance();

        setModals();
        setEditModeViews();
        changeEditMode(false);
        setJsonConnectors();

        diaryRecordAudioPlayer.setDiaryAudioName(this);

        //EXAMPLE
//        findViewById(R.id.exampleAttachment).setVisibility(View.VISIBLE);
    }

    private void setEditModeViews() {
        View menuEdit = viewModeActionbar.findViewById(R.id.btnActionBarEdit);
        View menuEditCancel = editModeActionbar.findViewById(R.id.btnActionBarCancel);
        View menuDelete = editModeActionbar.findViewById(R.id.btnActionBarDelete);

        View.OnClickListener viewModeListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                case R.id.btnActionBarEdit:
                    changeEditMode(true);
                    break;
                }
            }
        };

        View.OnClickListener editModeListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                case R.id.btnActionBarCancel:
                    // roll back to before change
                    setDiary(nowDiary);
                    changeEditMode(false);
                    break;
                case R.id.btnActionBarDelete:
                    dlgDeleteConfirm.show();
                    break;
                }
            }
        };

        menuEdit.setOnClickListener(viewModeListener);
        menuEditCancel.setOnClickListener(editModeListener);
        menuDelete.setOnClickListener(editModeListener);
    }

    public void changeEditMode(boolean editMode) {
        ActionBar actionBar = getSupportActionBar();
        if (editMode) { //viewMode -> editMode
            actionBar.setCustomView(editModeActionbar,
                    new ActionBar.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT,
                            ActionBar.LayoutParams.MATCH_PARENT, Gravity.CENTER));
            actionBar.setDisplayShowCustomEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(false);

            edtTitle.setFocusable(true);
            edtTitle.setFocusableInTouchMode(true);
            tvDiarySelectDate.setFocusable(true);
            tvDiarySelectDate.setClickable(true);
            tvDiarySelectDate.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    dlgDatePicker.show();
                }
            });
            tvDiarySelectTime.setFocusable(true);
            tvDiarySelectTime.setFocusableInTouchMode(true);
            tvDiarySelectTime.setClickable(true);
            tvDiarySelectTime.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    dlgTimePicker.show();
                }
            });
            edtContent.setFocusable(true);
            edtContent.setFocusableInTouchMode(true);
            edtAnnotation.setFocusable(true);
            edtAnnotation.setFocusableInTouchMode(true);
            btnConfirm.setVisibility(View.VISIBLE);

        } else {    //editMode -> viewMode
            actionBar.setCustomView(viewModeActionbar,
                    new ActionBar.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT,
                            ActionBar.LayoutParams.MATCH_PARENT, Gravity.CENTER));
            actionBar.setDisplayShowCustomEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);

            edtTitle.setFocusable(false);
            tvDiarySelectDate.setClickable(false);
            tvDiarySelectDate.setOnClickListener(null);
            tvDiarySelectTime.setClickable(false);
            tvDiarySelectTime.setOnClickListener(null);
            edtContent.setFocusable(false);
            edtAnnotation.setFocusable(false);
            btnConfirm.setVisibility(View.GONE);

            View focusView = this.getCurrentFocus();
            if (focusView != null) {
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(focusView.getWindowToken(), 0);
            }
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
            viewWriteDiaryLayout.scrollTo(0, 0);
        }
    }

    // update diary
    @Override
    protected void saveDiary() {
        return;
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
                            Log.d("ViewDiaryActivity", resJson.toString());
                            try {
                                if (resJson.has("result_detail") && resJson.getBoolean("retrieve_diary")) {
                                    JSONObject diary = resJson.getJSONObject("result_detail");
                                    JSONArray diaryContexts = null;
                                    if (resJson.has("result_context")) {
                                        diaryContexts = resJson.getJSONArray("result_context");
                                    }

                                    nowDiary = Diary.fromJSON(diary, diaryContexts);
                                    setDiary(nowDiary);

                                    boolean bIsDiaryAudioExists = false;
                                    if (!GlobalUtils.getDiaryFile(UserProfile.getUserProfile().getUserID(),
                                            nowDiary.getDiaryID()).exists()) {
                                        //request file download
                                        diaryRecordAudioPlayer.setAudioLoadMessage("Downloading Diary Record Audio...");
                                        diaryAudioDownloadConnector.request(UserProfile.getUserProfile().getUserID(),
                                                nowDiary.getDiaryID());
                                    } else {
                                        bIsDiaryAudioExists = true;
                                        diaryAudioFile = GlobalUtils.getDiaryFile(UserProfile.getUserProfile().getUserID(),
                                                nowDiary.getDiaryID());

                                        boolean diaryAudioSet = diaryRecordAudioPlayer.setAudio(
                                                "Recorded Diary Audio", diaryAudioFile);
                                        if (!diaryAudioSet) {
                                            openAlertModal("Diary Record Audio File load Failed.");
                                            diaryRecordAudioPlayer.setAudioFail("Loading Diary Record Audio Failed");
                                        }
                                    }

                                    //diary media context
                                    if (resJson.has("result_media_context_list")) {
                                        JSONArray mediaContextJsonArray = resJson.getJSONArray("result_media_context_list");
                                        for(int i=0; i<mediaContextJsonArray.length(); i++) {
                                            final JSONObject mediaContextJson = mediaContextJsonArray.getJSONObject(i);
                                            final int mediaContextId = mediaContextJson.getInt("media_context_id");
                                            final String mediaContextName = mediaContextJson.getString("file_name");
                                            final String mediaContextType = mediaContextJson.getString("type");

//                                            addMediaContext(mediaContextId, mediaContextName, mediaContextType);
                                            mediaContextWaitQueue.add(new MediaContextWait(mediaContextId,
                                                    mediaContextName, mediaContextType));
                                        }
                                        setDownloadMediaContext();
                                        if(bIsDiaryAudioExists) {
                                            nextDownloadMediaContext();
                                        }
                                    }

                                    viewWriteDiaryLayout.scrollTo(0, 0);

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

        diaryAudioDownloadConnector = new DiaryAudioDownloadConnector("download", "POST",
                new DiaryAudioDownloadConnector.OnConnectListener() {
                    @Override
                    public void onDone(Boolean success) {
                        if (success) {
                            diaryAudioFile = GlobalUtils.getDiaryFile(UserProfile.getUserProfile().getUserID(),
                                    nowDiary.getDiaryID());

                            boolean diaryAudioSet = diaryRecordAudioPlayer.setAudio(
                                    "Recorded Diary Audio", diaryAudioFile);
                            if (!diaryAudioSet) {
                                openAlertModal("Diary Record Audio File load Failed.");
                                diaryRecordAudioPlayer.setAudioFail("Downloading Diary Record Audio Failed");
                            }
                        } else  {
                            openAlertModal("Downloading audio file is failed.");
                            diaryRecordAudioPlayer.setAudioFail("Downloading Diary Record Audio Failed");
                        }

                        nextDownloadMediaContext();
                    }
                });

        deleteDiaryConnector = new JsonRestConnector("diary/delete", "POST",
                new JsonRestConnector.OnConnectListener() {
                    @Override
                    public void onDone(JSONObject resJson) {
                        if (resJson == null) {
                            Log.d("Main - Json", "No response");
                            openAlertModal("No response.", "Error");
                        } else {
                            try {
                                if (resJson.getBoolean("delete_diary")) {
                                    try {
                                        diaryRecordAudioPlayer.remove();
                                        GlobalUtils.getDiaryFile(UserProfile.getUserProfile().getUserID(),
                                                nowDiary.getDiaryID()).delete();
                                    } catch (Exception e) {}
                                    diaryRecordAudioPlayer = null;
                                    nowDiary = null;

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
                    }
                });

//        saveDiaryConnector = new JsonRestConnector()
    }

    private LinkedList<MediaContextWait> mediaContextWaitQueue = new LinkedList<>();
    private class MediaContextWait {
        int mediaContextId;
        String mediaContextName;
        String mediaContextType;

        ViewGroup layoutContainer = null;
        MediaContextLoadingView loadingView = null;

        public MediaContextWait(int mediaContextId, String mediaContextName, String mediaContextType) {
            this.mediaContextId = mediaContextId;
            this.mediaContextName = mediaContextName;
            this.mediaContextType = mediaContextType;
        }
    }

    private void setDownloadMediaContext() {
        if (mediaContextWaitQueue.size() > 0) {
            for (MediaContextWait mediaContextWait : mediaContextWaitQueue) {
                addMediaContextLoading(mediaContextWait);
            }
        }
    }

    private void nextDownloadMediaContext() {
        if (mediaContextWaitQueue.size() > 0) {
            //start Downloading by queue
            addMediaContextByQueue(mediaContextWaitQueue.pollFirst());
        }
    }

    private void addMediaContextLoading(MediaContextWait mediaContextWait) {
        final LinearLayout layoutContainer = new LinearLayout(this);
        layoutContainer.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams containerParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );

        final MediaContextLoadingView loadingView = new MediaContextLoadingView(this);
        loadingView.setLoadingMessage("Waiting to Download " + mediaContextWait.mediaContextName);
        LinearLayout.LayoutParams loadingViewParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        loadingViewParams.setMargins(0, 0, 0, (int)GlobalUtils.dpToPixel(this, 10));

        layoutAttachmentFiles.addView(layoutContainer, containerParams);
        layoutContainer.addView(loadingView, loadingViewParams);

        mediaContextWait.layoutContainer = layoutContainer;
        mediaContextWait.loadingView = loadingView;
    }

    private void addMediaContextByQueue(final MediaContextWait mediaContextWait) {
        if (mediaContextWait == null) return;

        final File mediaContextFile = GlobalUtils.getDiaryMediaContext(
                UserProfile.getUserProfile().getUserID(),
                nowDiary.getDiaryID(), mediaContextWait.mediaContextName);
        int mediaType = -1;
        switch (mediaContextWait.mediaContextType) {
            case "picture":
                mediaType = MediaContext.MEDIA_TYPE_IMAGE; break;
            case "music":
                mediaType = MediaContext.MEDIA_TYPE_AUDIO; break;
            case "video":
                mediaType = MediaContext.MEDIA_TYPE_VIDEO; break;
        }
        final MediaContext mediaContext = new MediaContext(ViewDiaryActivity.this,
                mediaContextFile, mediaType);

        if (mediaContextFile.exists()) {
            Log.d("ViewDiaryActivity", "File existed: " + mediaContextFile.toString());
            addMediaContextView(mediaContext, mediaContextWait.layoutContainer);
            mediaContextWait.layoutContainer.removeView(mediaContextWait.loadingView);
            mediaContextList.add(mediaContext);

            //download next
            nextDownloadMediaContext();
        } else {    //download
            Log.d("ViewDiaryActivity", "No file! Download: " + mediaContextWait.mediaContextName);
            mediaContextWait.loadingView.setLoadingMessage("Downloading " + mediaContextWait.mediaContextName);

            MediaContextDownloadConnector conn = new MediaContextDownloadConnector("download", "POST",
                    new MediaContextDownloadConnector.OnConnectListener() {
                        @Override
                        public void onDone(Boolean success, String fileName, String type) {
                            if (success) {
                                addMediaContextView(mediaContext, mediaContextWait.layoutContainer);
                                mediaContextWait.layoutContainer.removeView(mediaContextWait.loadingView);
                                mediaContextList.add(mediaContext);
                            } else {
                                Log.d("ViewDiaryActivity", "Download Failed: " + fileName);
                                mediaContextWait.loadingView.setLoadingMessage("Download Failed:" + fileName);
                            }

                            //download next
                            nextDownloadMediaContext();
                        }

                        @Override
                        public void onCancelled() {
                            try {
                                if(mediaContextFile.exists()) {
                                    mediaContextFile.delete();
                                }
                            } catch (Exception e){}
                        }
                    });
            conn.request(UserProfile.getUserProfile().getUserID(), nowDiary.getDiaryID(),
                    mediaContextWait.mediaContextId, mediaContextWait.mediaContextName,
                    mediaContextWait.mediaContextType);
        }
    }

    @Deprecated
    private void addMediaContext(final int mediaContextId, final String mediaContextName,
                                 final String mediaContextType) {
        File mediaContextFile = GlobalUtils.getDiaryMediaContext(
                UserProfile.getUserProfile().getUserID(),
                nowDiary.getDiaryID(), mediaContextName);
        int mediaType = -1;
        switch (mediaContextType) {
            case "picture":
                mediaType = MediaContext.MEDIA_TYPE_IMAGE; break;
            case "music":
                mediaType = MediaContext.MEDIA_TYPE_AUDIO; break;
            case "video":
                mediaType = MediaContext.MEDIA_TYPE_VIDEO; break;
        }
        final MediaContext mediaContext = new MediaContext(ViewDiaryActivity.this,
                mediaContextFile, mediaType);

        if (mediaContextFile.exists()) {
            Log.d("ViewDiaryActivity", "File existed: " + mediaContextFile.toString());
            addMediaContextView(mediaContext, layoutAttachmentFiles);
            mediaContextList.add(mediaContext);
        } else {    //download
            final LinearLayout layoutContainer = new LinearLayout(this);
            layoutContainer.setOrientation(LinearLayout.VERTICAL);
            LinearLayout.LayoutParams containerParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );

            final MediaContextLoadingView loadingView = new MediaContextLoadingView(this);
            loadingView.setLoadingMessage("Downloading " + mediaContextName);
            LinearLayout.LayoutParams loadingViewParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            loadingViewParams.setMargins(0, 0, 0, (int)GlobalUtils.dpToPixel(this, 10));

            layoutAttachmentFiles.addView(layoutContainer, containerParams);
            layoutContainer.addView(loadingView, loadingViewParams);

            //start download
            Log.d("ViewDiaryActivity", "No file! Download: " + mediaContextName);
            MediaContextDownloadConnector conn = new MediaContextDownloadConnector("download", "POST",
                    new MediaContextDownloadConnector.OnConnectListener() {
                        @Override
                        public void onDone(Boolean success, String fileName, String type) {
                            if (success) {
                                addMediaContextView(mediaContext, layoutContainer);
                                layoutContainer.removeView(loadingView);
                            } else {
                                Log.d("ViewDiaryActivity", "Download Failed: " + fileName);
                                loadingView.setLoadingMessage("Download Failed:" + fileName);
                            }
                        }
                    });
            conn.request(UserProfile.getUserProfile().getUserID(), nowDiary.getDiaryID(),
                    mediaContextId, mediaContextName, mediaContextType);
        }
    }

    @Override
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

    @Override
    public void onActionMenuClick(View v) {
        return;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                ViewDiaryActivity.this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void setModals() {
        super.setModals();
        dlgDeleteConfirm = new AlertDialog.Builder(this).setMessage("Are you sure to delete diary?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        JSONObject json = new JSONObject();
                        try {
                            json.put("user_id", UserProfile.getUserProfile().getUserID());
                            json.put("audio_diary_id", nowDiary.getDiaryID());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        // STOP
                        diaryRecordAudioPlayer.pause();

                        showProgress(true);
                        deleteDiaryConnector.request(json);
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
                        ViewDiaryActivity.this.finish();
                    }
                }).setCancelable(false).create();
    }

    private void setDiary(Diary diary) {
        edtTitle.setText(diary.getTitle());
        tvDiarySelectDate.setText(GlobalUtils.DIARY_DATE_FORMAT.format(
                diary.getDate().getTime()));
        edtContent.setText(diary.getContent());

        DiaryContext annotation = diary.getAnnotation();
        if (annotation != null)
            edtAnnotation.setText(annotation.getValue());

        ArrayList<DiaryContext> envPlaces =
                diary.getDiaryContexts(DiaryContext.CONTEXT_TYPE_ENVIRONMENT,
                        DiaryContext.SUB_TYPE_ENV_PLACE);
        ArrayList<DiaryContext> envWeathers =
                diary.getDiaryContexts(DiaryContext.CONTEXT_TYPE_ENVIRONMENT,
                        DiaryContext.SUB_TYPE_ENV_WEATHER);
        ArrayList<DiaryContext> envHolidays =
                diary.getDiaryContexts(DiaryContext.CONTEXT_TYPE_ENVIRONMENT,
                        DiaryContext.SUB_TYPE_ENV_HOLIDAY);
        ArrayList<DiaryContext> envEvents =
                diary.getDiaryContexts(DiaryContext.CONTEXT_TYPE_ENVIRONMENT,
                        DiaryContext.SUB_TYPE_ENV_EVENT);

        edtEnvPlace.setText(DiaryContext.diaryContextsToString(envPlaces));
        edtEnvWeather.setText(DiaryContext.diaryContextsToString(envWeathers));
        edtEnvHolidays.setText(DiaryContext.diaryContextsToString(envHolidays));
        edtEnvEvents.setText(DiaryContext.diaryContextsToString(envEvents));
    }
}
