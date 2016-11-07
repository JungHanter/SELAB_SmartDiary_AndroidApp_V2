package ssu.sel.smartdiary;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.DialogInterface;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.Iterator;

import ssu.sel.smartdiary.model.UserProfile;
import ssu.sel.smartdiary.network.JsonRestConnector;

public class AnalyticsActivity extends AppCompatActivity {

    private ImageView ivLifeStyleExpand = null;
    private View titleAnalyticsLifeStyle = null;
    private View layoutAnalyticsLifeStyle = null;
    private Spinner spnLifeStyleThings = null;
    private Spinner spnLifeStyleType = null;
    private TextView tvLifeStyleResult = null;
    private View progressLifeStyle = null;

    protected AlertDialog dlgAlert = null;

    private JsonRestConnector analyzeLifeStyleConnector = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayShowTitleEnabled(false);
        View mActionBarView = getLayoutInflater().inflate(R.layout.action_bar_center_with_back_button, null);
        ((TextView)mActionBarView.findViewById(R.id.tvActionBarTitle)).setText("Analytics");
        actionBar.setCustomView(mActionBarView,
                new ActionBar.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT,
                        ActionBar.LayoutParams.MATCH_PARENT, Gravity.CENTER));
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        setContentView(R.layout.activity_analytics);

        ivLifeStyleExpand = (ImageView)findViewById(R.id.ivLifeStyleExpand);
        titleAnalyticsLifeStyle = findViewById(R.id.titleAnalyticsLifeStyle);
        layoutAnalyticsLifeStyle = findViewById(R.id.layoutAnalyticsLifeStyle);
        spnLifeStyleThings = (Spinner)findViewById(R.id.spnLifeStyleThings);
        spnLifeStyleType = (Spinner)findViewById(R.id.spnLifeStyleType);
        tvLifeStyleResult = (TextView)findViewById(R.id.tvLifeStyleResult);
        progressLifeStyle = findViewById(R.id.progressLifeStyle);
        layoutAnalyticsLifeStyle.setVisibility(View.GONE);
        titleAnalyticsLifeStyle.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (layoutAnalyticsLifeStyle.getVisibility() == View.GONE) {
                    layoutAnalyticsLifeStyle.setVisibility(View.VISIBLE);
                    ivLifeStyleExpand.setImageResource(R.drawable.ic_expand_less_black);
//                    layoutAnalyticsLifeStyle.setAlpha(0.0f);
//                    layoutAnalyticsLifeStyle.animate()
//                            .translationY(layoutAnalyticsLifeStyle.getMeasuredHeight())
//                            .alpha(1.0f);
                } else {
                    layoutAnalyticsLifeStyle.setVisibility(View.GONE);
                    ivLifeStyleExpand.setImageResource(R.drawable.ic_expand_more_black);
//                    layoutAnalyticsLifeStyle.animate()
//                            .translationY(0)
//                            .alpha(0.0f)
//                            .setListener(new AnimatorListenerAdapter() {
//                                @Override
//                                public void onAnimationEnd(Animator animation) {
//                                    super.onAnimationEnd(animation);
//                                    layoutAnalyticsLifeStyle.setVisibility(View.GONE);
//                                }
//                            });
                }
            }
        });
        ArrayAdapter<CharSequence> thingAdapter = ArrayAdapter.createFromResource(this,
                R.array.analytics_lifestyle_things, android.R.layout.simple_spinner_item);
        thingAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnLifeStyleThings.setAdapter(thingAdapter);
        ArrayAdapter<CharSequence> typeAdapter = ArrayAdapter.createFromResource(this,
                R.array.analytics_lifestyle_type, android.R.layout.simple_spinner_item);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnLifeStyleType.setAdapter(typeAdapter);


        dlgAlert = new AlertDialog.Builder(this).setMessage("Message")
                .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                }).create();


        analyzeLifeStyleConnector = new JsonRestConnector("analyze/lifestyle", "GET",
                new JsonRestConnector.OnConnectListener() {
                    @Override
                    public void onDone(JSONObject resJson) {
                        if (resJson == null) {
                            Log.d("Main - Json", "No response");
                            openAlertModal("No response.", "Error");
                        } else {
                            try {
                                boolean success = resJson.getBoolean("lifestyle");
                                if (success) {
                                    JSONArray result = resJson.getJSONArray("result");
                                    Log.d("Analytics Activity", result.toString());

                                    String resultString = "Foods Most Liked: \n";
                                    for (int i=0; i<result.length(); i++) {
                                        String thing = result.getString(i);
                                        if (i>0) resultString += ", ";
                                        resultString += thing;
                                    }

                                    tvLifeStyleResult.setText(resultString);
                                    tvLifeStyleResult.setVisibility(View.VISIBLE);
                                } else {
                                    openAlertModal("Analyze Failed");
                                    tvLifeStyleResult.setVisibility(View.GONE);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                                Log.d("Main - Json", "Json parsing error");
                                openAlertModal("Json parsing error.", "Error");
                                tvLifeStyleResult.setVisibility(View.GONE);
                            }
                        }
                        progressLifeStyle.setVisibility(View.GONE);
                    }
                });
    }

    public void onAnalyzeClick(View v) {
        switch (v.getId()) {
            case R.id.btnLifeStyleAnalyze:
                JSONObject reqJson = new JSONObject();
                try {
                    reqJson.put("user_id", UserProfile.getUserProfile().getUserID());
                    reqJson.put("thing_type", "food");
                    reqJson.put("option", "like");
                } catch (Exception e) {}
                progressLifeStyle.setVisibility(View.VISIBLE);
                analyzeLifeStyleConnector.request(reqJson);
                return;
        }
    }

    protected void openAlertModal(CharSequence msg) {
        openAlertModal(msg, "Alert");
    }

    protected void openAlertModal(CharSequence msg, CharSequence title) {
        dlgAlert.setTitle(title);
        dlgAlert.setMessage(msg);
        dlgAlert.show();
    }
}
