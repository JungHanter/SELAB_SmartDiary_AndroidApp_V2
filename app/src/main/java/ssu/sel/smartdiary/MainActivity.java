package ssu.sel.smartdiary;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Calendar;

import ssu.sel.smartdiary.model.UserProfile;
import ssu.sel.smartdiary.network.JsonRestConnector;
import ssu.sel.smartdiary.view.DiaryListViewAdapter;
import ssu.sel.smartdiary.view.DiaryListViewItem;
import ssu.sel.smartdiary.view.DiarySearchToolbar;

public class MainActivity extends AppCompatActivity {
    public static MainActivity rootMainActivity = null;

    private JsonRestConnector getRecentDiaryConnector = null;
    private JsonRestConnector searchTimeConnector = null;
    private JsonRestConnector searchTextConnector = null;

    private View viewMainLayout = null;
    private View viewProgress = null;

    private int[] recentDiaryIDs = new int[0];

    private DiarySearchToolbar searchToolbar = null;
    private ListView listSearchDiary = null;
    private DiaryListViewAdapter searchAdapter = null;

    private boolean bSearchMode = false;

    private AlertDialog dlgAlert = null;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        rootMainActivity = null;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        rootMainActivity = this;

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayShowTitleEnabled(false);
        View mActionBarView = getLayoutInflater().inflate(R.layout.action_bar_main, null);
        actionBar.setCustomView(mActionBarView,
                new ActionBar.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT,
                ActionBar.LayoutParams.MATCH_PARENT, Gravity.CENTER));
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        actionBar.setDisplayShowCustomEnabled(true);

        setContentView(R.layout.activity_main);

        viewMainLayout = findViewById(R.id.mainContentLayout);
        viewProgress = findViewById(R.id.progressLayout);
        searchToolbar = (DiarySearchToolbar)findViewById(R.id.searchToolbar);
        listSearchDiary = (ListView)findViewById(R.id.listSearchDiary);
        searchAdapter = new DiaryListViewAdapter();
        searchAdapter.setOnDiaryViewItemClickListener(
                new DiaryListViewAdapter.OnDiaryViewItemClickListener() {
            @Override
            public void onClick(DiaryListViewItem diary) {
                Intent intent = new Intent(MainActivity.this, ViewDiaryActivity.class);
                intent.putExtra("DIARY_ID", diary.getDiaryID());
                startActivity(intent);
            }
        });
        listSearchDiary.setAdapter(searchAdapter);

        //button sizes
        View layoutBtnDiaries = findViewById(R.id.layoutBtnDiaries);
        layoutBtnDiaries.measure(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        Log.d("MainAcitivty", "LayoutDiaryBtns: " + layoutBtnDiaries.getMeasuredWidth() +
                ", " + layoutBtnDiaries.getMeasuredHeight());
        int layoutWidth = layoutBtnDiaries.getMeasuredWidth();
        int btnWidth = layoutWidth / 6;
        int btnLayoutWidth = btnWidth * 2;
//        findViewById(R.id.layoutShowAllDiaries).setLayoutParams(
//                new LinearLayout.LayoutParams(btnLayoutWidth, LinearLayout.LayoutParams.WRAP_CONTENT));
//        findViewById(R.id.layoutNewAudioDiary).setLayoutParams(
//                new LinearLayout.LayoutParams(btnLayoutWidth, LinearLayout.LayoutParams.WRAP_CONTENT));
//        findViewById(R.id.tvShowAllDiaries).getLayoutParams().width = btnLayoutWidth;
//        findViewById(R.id.tvNewAudioDiary).getLayoutParams().width = btnLayoutWidth;
//        findViewById(R.id.btnShowAllDiaries).setLayoutParams(
//                new LinearLayout.LayoutParams(btnWidth, btnWidth));
//        findViewById(R.id.btnNewAudioDiary).setLayoutParams(
//                new LinearLayout.LayoutParams(btnWidth, btnWidth));

        findViewById(R.id.layoutNewAudioDiary).setLayoutParams(
                new LinearLayout.LayoutParams(btnLayoutWidth, btnLayoutWidth));
        findViewById(R.id.layoutShowAllDiaries).setLayoutParams(
                new LinearLayout.LayoutParams(btnLayoutWidth, btnLayoutWidth));
        findViewById(R.id.layoutAnalytics).setLayoutParams(
                new LinearLayout.LayoutParams(btnLayoutWidth, btnLayoutWidth));

        dlgAlert = new AlertDialog.Builder(this).setMessage("Message")
                .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                }).create();

        setJsonConnectors();

        searchToolbar.setOnSearchListener(new DiarySearchToolbar.OnSearchListener() {
            @Override
            public void onSearchTime(Calendar startDate, Calendar endDate) {
                Log.d("Search - Time", startDate.toString() + " ~ " + endDate.toString());
                showProgress(true);
                searchAdapter.clear();

                JSONObject json = new JSONObject();
                try {
                    json.put("user_id", UserProfile.getUserProfile().getUserID());
                    json.put("timestamp_from", startDate.getTimeInMillis());
                    json.put("timestamp_to", endDate.getTimeInMillis());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                searchTimeConnector.request(json);
            }

            @Override
            public void onSearchText(String text) {
                Log.d("Search - Text", text);
                showProgress(true);
                searchAdapter.clear();

                JSONObject json = new JSONObject();
                try {
                    json.put("user_id", UserProfile.getUserProfile().getUserID());
                    json.put("keyword", text);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                searchTextConnector.request(json);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        //recent 5 diaries
        JSONObject json = new JSONObject();
        try {
             json.put("user_id", UserProfile.getUserProfile().getUserID());
            json.put("limit", 5);
        } catch (Exception e) {
            e.printStackTrace();
        }

        showProgress(true);
        getRecentDiaryConnector.request(json);
    }

    // https://developer.android.com/training/appbar/action-views.html
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_search:
                if (viewProgress.isShown()) return true;

                if (bSearchMode) {
                    bSearchMode = false;
                    item.setIcon(R.drawable.iconmonstr_search_padding);
                    searchToolbar.setVisibility(View.GONE);
                    listSearchDiary.setVisibility(View.GONE);
                    viewMainLayout.setVisibility(View.VISIBLE);
                } else {
                    bSearchMode = true;
                    item.setIcon(R.drawable.iconmonstr_close_padding2);
                    viewMainLayout.setVisibility(View.GONE);
                    searchToolbar.setVisibility(View.VISIBLE);
                    listSearchDiary.setVisibility(View.VISIBLE);
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void onActionMenuClick(View v) {
        switch (v.getId()) {
            case R.id.btnActionBarProfile:
                Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                startActivity(intent);
                return;
            case R.id.btnActionBarSettings:
                intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent);
                return;
        }
    }

    public void onClick(View v) {
        Intent intent;
        switch (v.getId()) {
            case R.id.btnNewAudioDiary:
                intent = new Intent(MainActivity.this, NewAudioDiaryActivity.class);
                startActivity(intent);
                return;
            case R.id.btnShowAllDiaries:
                intent = new Intent(MainActivity.this, DiaryListActivity.class);
                startActivity(intent);
                return;
            case R.id.btnAnalytics:
                intent = new Intent(MainActivity.this, AnalyticsActivity.class);
                startActivity(intent);
                return;
            case R.id.tvDiaryListElem1:
            case R.id.tvDiaryListElem2:
            case R.id.tvDiaryListElem3:
            case R.id.tvDiaryListElem4:
            case R.id.tvDiaryListElem5:
                int selectedDiary = (v.getId() - R.id.tvDiaryListElem1) / 4;
                intent = new Intent(MainActivity.this, ViewDiaryActivity.class);
                intent.putExtra("DIARY_ID", recentDiaryIDs[selectedDiary]);
                startActivity(intent);
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

    private void setJsonConnectors() {
        getRecentDiaryConnector = new JsonRestConnector("diary", "GET",
                new JsonRestConnector.OnConnectListener() {
                    @Override
                    public void onDone(JSONObject resJson) {
                        if (resJson == null) {
                            Log.d("Main - Json", "No response");
                            openAlertModal("No response.", "Error");
                        } else {
                            try {
                                if (resJson.has("result") && resJson.getBoolean("retrieve_diary")) {
                                    JSONArray diaries = resJson.getJSONArray("result");
                                    recentDiaryIDs = new int[diaries.length()];
                                    for (int i=0; i<diaries.length(); i++) {
                                        JSONObject diary = diaries.getJSONObject(i);
//                                        Log.d("Diary - Json", diary.toString());

                                        View diaryLayout = findViewById(R.id.tvDiaryListElem1 + 4*i);
                                        TextView tvTitle = (TextView)findViewById(R.id.tvDiaryListElemTitle1 + 4*i);
                                        TextView tvDate = (TextView)findViewById(R.id.tvDiaryListElemDate1 + 4*i);
                                        TextView tvContent = (TextView)findViewById(R.id.tvDiaryListElemContent1 + 4*i);

                                        tvTitle.setText(diary.getString("title"));
                                        long time = diary.getLong("created_date");
                                        Calendar calendar = Calendar.getInstance();
                                        calendar.setTimeInMillis(time);
                                        tvDate.setText(GlobalUtils.DIARY_DATE_FORMAT.format(calendar.getTime()));
                                        tvContent.setText(diary.getString("content"));

                                        recentDiaryIDs[i] = diary.getInt("audio_diary_id");
//                                        Log.d("Diary Text - JsonString", diary.getString("text"));
                                        diaryLayout.setVisibility(View.VISIBLE);
                                    }
                                    for (int i=diaries.length(); i<5; i++) {
                                        View diaryLayout = findViewById(R.id.tvDiaryListElem1 + 4*i);
                                        diaryLayout.setVisibility(View.GONE);
                                    }
                                } else {
                                    recentDiaryIDs = new int[0];
                                    Log.d("Main - Json", "Retrieve diary failed");
                                }
                            } catch (Exception e) {
                                recentDiaryIDs = new int[0];
                                Log.d("Main - Json", "Json parsing error");
                                openAlertModal("Json parsing error.", "Error");
                                e.printStackTrace();
                            }
                        }

                        showProgress(false);
                    }

                    @Override
                    public void onCancelled() {
                        showProgress(false);
                    }
                });

        searchTimeConnector = new JsonRestConnector("diary", "GET",
                new JsonRestConnector.OnConnectListener() {
                    @Override
                    public void onDone(JSONObject resJson) {
                        if (resJson == null) {
                            Log.d("Search - Json", "No response");
                            openAlertModal("No response.", "Error");
                        } else {
                            try {
                                if (resJson.has("result") && resJson.getBoolean("retrieve_diary")) {
                                    JSONArray diaries = resJson.getJSONArray("result");
                                    for (int i=0; i<diaries.length(); i++) {
                                        JSONObject diary = diaries.getJSONObject(i);
                                        long time = diary.getLong("created_date");
                                        Calendar calendar = Calendar.getInstance();
                                        calendar.setTimeInMillis(time);

                                        searchAdapter.addItem(new DiaryListViewItem(
                                                diary.getInt("audio_diary_id"),
                                                diary.getString("title"),
                                                GlobalUtils.DIARY_DATE_FORMAT.format(calendar.getTime()),
                                                diary.getString("content")
                                        ));
                                    }
                                    searchAdapter.notifyDataSetChanged();
                                } else {
                                    Log.d("Main - Json", "Retrieve diary failed");
                                    openAlertModal("No result.");
                                }
                            } catch (Exception e) {
                                searchAdapter.clear();
                                searchAdapter.notifyDataSetChanged();
                                Log.d("Main - Json", "Json parsing error");
                                openAlertModal("Json parsing error.", "Error");
                                e.printStackTrace();
                            }
                        }
                        showProgress(false);
                    }
                });

        searchTextConnector = new JsonRestConnector("diary/match", "GET",
                new JsonRestConnector.OnConnectListener() {
                    @Override
                    public void onDone(JSONObject resJson) {
                        if (resJson == null) {
                            Log.d("Search - Json", "No response");
                            openAlertModal("No response.", "Error");
                        } else {
                            try {
                                if (resJson.has("result") && resJson.getBoolean("retrieve_diary")) {
                                    JSONArray diaries = resJson.getJSONArray("result");
                                    for (int i=0; i<diaries.length(); i++) {
                                        JSONObject diary = diaries.getJSONObject(i);
                                        long time = diary.getLong("timestamp");
                                        Calendar calendar = Calendar.getInstance();
                                        calendar.setTimeInMillis(time);

                                        searchAdapter.addItem(new DiaryListViewItem(
                                                diary.getInt("diary_id"),
                                                diary.getString("title"),
                                                GlobalUtils.DIARY_DATE_FORMAT.format(calendar.getTime()),
                                                diary.getString("text")
                                        ));
                                    }
                                    searchAdapter.notifyDataSetChanged();
                                } else {
                                    Log.d("Main - Json", "Retrieve diary failed");
                                    openAlertModal("No result.");
                                }
                            } catch (Exception e) {
                                searchAdapter.clear();
                                searchAdapter.notifyDataSetChanged();
                                Log.d("Main - Json", "Json parsing error");
                                openAlertModal("Json parsing error.", "Error");
                                e.printStackTrace();
                            }
                        }
                        showProgress(false);
                    }
                });
    }

    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            if (bSearchMode) {
                searchToolbar.setVisibility(show ? View.GONE : View.VISIBLE);
                listSearchDiary.setVisibility(show ? View.GONE : View.VISIBLE);
                viewProgress.animate().setDuration(shortAnimTime).alpha(
                        show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        searchToolbar.setVisibility(show ? View.GONE : View.VISIBLE);
                        listSearchDiary.setVisibility(show ? View.GONE : View.VISIBLE);
                    }
                });
            } else {
                viewMainLayout.setVisibility(show ? View.GONE : View.VISIBLE);
                viewProgress.animate().setDuration(shortAnimTime).alpha(
                        show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        viewMainLayout.setVisibility(show ? View.GONE : View.VISIBLE);
                    }
                });
            }

            viewProgress.setVisibility(show ? View.VISIBLE : View.GONE);
            viewProgress.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    viewProgress.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            viewProgress.setVisibility(show ? View.VISIBLE : View.GONE);
            if (bSearchMode) {
                searchToolbar.setVisibility(show ? View.GONE : View.VISIBLE);
                listSearchDiary.setVisibility(show ? View.GONE : View.VISIBLE);
            } else
                viewMainLayout.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }
}
