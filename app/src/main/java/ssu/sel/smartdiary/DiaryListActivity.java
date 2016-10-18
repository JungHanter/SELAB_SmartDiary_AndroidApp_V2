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

public class DiaryListActivity extends AppCompatActivity {
    private JsonRestConnector getDiaryConnector = null;
    private JsonRestConnector searchTimeConnector = null;
    private JsonRestConnector searchTextConnector = null;

    private View viewProgress = null;
    private ListView listDiary = null;
    private DiaryListViewAdapter adapter = null;

    private DiarySearchToolbar searchToolbar = null;
    private DiaryListViewAdapter searchAdapter = null;

    private boolean bSearchMode = false;

    private AlertDialog dlgAlert = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayShowTitleEnabled(false);
        View mActionBarView = getLayoutInflater().inflate(R.layout.action_bar_center_with_back_and_search, null);
        ((TextView)mActionBarView.findViewById(R.id.tvActionBarTitle)).setText("Diaries");
        actionBar.setCustomView(mActionBarView,
                new ActionBar.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT,
                        ActionBar.LayoutParams.MATCH_PARENT, Gravity.CENTER));
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        setContentView(R.layout.activity_diary_list);

        viewProgress = findViewById(R.id.progressLayout);
        listDiary = (ListView)findViewById(R.id.listDiary);
        searchToolbar = (DiarySearchToolbar)findViewById(R.id.searchToolbar);
        adapter = new DiaryListViewAdapter();
        searchAdapter = new DiaryListViewAdapter();
        listDiary.setAdapter(adapter);

        dlgAlert = new AlertDialog.Builder(this).setMessage("Message")
                .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                }).create();

        DiaryListViewAdapter.OnDiaryViewItemClickListener diaryViewItemClickListener =
                new DiaryListViewAdapter.OnDiaryViewItemClickListener() {
            @Override
            public void onClick(DiaryListViewItem diary) {
                Intent intent = new Intent(DiaryListActivity.this, EditDiaryActivity.class);
                intent.putExtra("DIARY_ID", diary.getDiaryID());
                startActivity(intent);
            }
        };
        adapter.setOnDiaryViewItemClickListener(diaryViewItemClickListener);
        searchAdapter.setOnDiaryViewItemClickListener(diaryViewItemClickListener);

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

        JSONObject json = new JSONObject();
        try {
            json.put("user_id", UserProfile.getUserProfile().getUserID());
        } catch (Exception e) {
            e.printStackTrace();
        }
        getDiaryConnector.request(json);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.diary_list_menu, menu);
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
                    listDiary.setAdapter(adapter);
                    adapter.notifyDataSetChanged();
                } else {
                    bSearchMode = true;
                    item.setIcon(R.drawable.iconmonstr_close_padding2);
                    searchToolbar.setVisibility(View.VISIBLE);
                    listDiary.setAdapter(searchAdapter);
                    searchAdapter.notifyDataSetChanged();
                }
                return true;
            case android.R.id.home:
                DiaryListActivity.this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
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
        getDiaryConnector = new JsonRestConnector("diary", "GET",
                new JsonRestConnector.OnConnectListener() {
                    @Override
                    public void onDone(JSONObject resJson) {
                        if (resJson == null) {
                            Log.d("Main - Json", "No response");
                        } else {
                            try {
                                if (resJson.has("result") && resJson.getBoolean("retrieve_diary")) {
                                    JSONArray diaries = resJson.getJSONArray("result");
                                    for (int i=0; i<diaries.length(); i++) {
                                        JSONObject diary = diaries.getJSONObject(i);
                                        long time = diary.getLong("timestamp");
                                        Calendar calendar = Calendar.getInstance();
                                        calendar.setTimeInMillis(time);

                                        adapter.addItem(new DiaryListViewItem(
                                                diary.getInt("diary_id"),
                                                diary.getString("title"),
                                                GlobalUtils.DIARY_DATE_FORMAT.format(calendar.getTime()),
                                                diary.getString("text")
                                        ));
                                    }
                                    adapter.notifyDataSetChanged();
                                } else {
                                    adapter.clear();
                                    adapter.notifyDataSetChanged();
                                    Log.d("Main - Json", "Retrieve diary failed");
                                    openAlertModal("No result.");
                                }
                            } catch (Exception e) {
                                Log.d("Main - Json", "Json parsing error");
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            listDiary.setVisibility(show ? View.GONE : View.VISIBLE);
            viewProgress.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    listDiary.setVisibility(show ? View.GONE : View.VISIBLE);
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
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            viewProgress.setVisibility(show ? View.VISIBLE : View.GONE);
            listDiary.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }
}
