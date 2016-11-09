package ssu.sel.smartdiary;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.DialogInterface;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
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

    private View titleAnalyticsLifeActivity = null;
    private ImageView ivLifeActivityExpand = null;
    private View layoutAnalyticsLifeActivity = null;
    private Spinner spnLifeActivityThings = null;
    private Spinner spnLifeActivityPeriod = null;
    private TextView tvLifeActivityResult = null;

    private View titleAnalyticsWellness = null;
    private ImageView ivAnalyticsWellnessExpand = null;
    private View layoutAnalyticsWellness = null;
    private Spinner spnWellnessType = null;
    private Spinner spnWellnessPeriod = null;
    private TextView tvWellnessResult = null;

    private View titleAnalyticsTendency = null;
    private ImageView ivAnalyticsTendencyExpand = null;
    private View layoutAnalyticsTendency = null;
    private Spinner spnLifeTendencyThings = null;
    private Spinner spnTendencyType = null;
    private TextView tvTendencyResult = null;

    private View titleAnalyticsCorrelation = null;
    private ImageView ivAnalyticsCorrelationExpand = null;
    private View layoutAnalyticsCorrelation = null;
    private Spinner spnCorrelationThingX = null;
    private Spinner spnCorrelationThingY = null;
    private TextView tvCorrelationResult = null;


    protected AlertDialog dlgAlert = null;

    private JsonRestConnector analyzeLifeStyleConnector = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayShowTitleEnabled(false);
        View mActionBarView = getLayoutInflater().inflate(R.layout.action_bar_center_with_back_button, null);
        ((TextView)mActionBarView.findViewById(R.id.tvActionBarTitle)).setText("Diary Analytics");
        actionBar.setCustomView(mActionBarView,
                new ActionBar.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT,
                        ActionBar.LayoutParams.MATCH_PARENT, Gravity.CENTER));
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        setContentView(R.layout.activity_analytics);

        titleAnalyticsLifeActivity = findViewById(R.id.titleAnalyticsLifeActivity);
        ivLifeActivityExpand = (ImageView)findViewById(R.id.ivLifeActivityExpand);
        layoutAnalyticsLifeActivity = findViewById(R.id.layoutAnalyticsLifeActivity);
        spnLifeActivityThings = (Spinner)findViewById(R.id.spnLifeActivityThings);
        spnLifeActivityPeriod = (Spinner)findViewById(R.id.spnLifeActivityPeriod);
        tvLifeActivityResult = (TextView)findViewById(R.id.tvLifeActivityResult);
        layoutAnalyticsLifeActivity.setVisibility(View.GONE);
        titleAnalyticsLifeActivity.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (layoutAnalyticsLifeActivity.getVisibility() == View.GONE) {
                    layoutAnalyticsLifeActivity.setVisibility(View.VISIBLE);
                    ivLifeActivityExpand.setImageResource(R.drawable.ic_expand_less_black);
                } else {
                    layoutAnalyticsLifeActivity.setVisibility(View.GONE);
                    ivLifeActivityExpand.setImageResource(R.drawable.ic_expand_more_black);

                }
            }
        });

        titleAnalyticsWellness = findViewById(R.id.titleAnalyticsWellness);
        ivAnalyticsWellnessExpand = (ImageView)findViewById(R.id.ivAnalyticsWellnessExpand);
        layoutAnalyticsWellness = findViewById(R.id.layoutAnalyticsWellness);
        spnWellnessType = (Spinner)findViewById(R.id.spnWellnessType);
        spnWellnessPeriod = (Spinner)findViewById(R.id.spnWellnessPeriod);
        tvWellnessResult = (TextView)findViewById(R.id.tvWellnessResult);
        layoutAnalyticsWellness.setVisibility(View.GONE);
        titleAnalyticsWellness.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (layoutAnalyticsWellness.getVisibility() == View.GONE) {
                    layoutAnalyticsWellness.setVisibility(View.VISIBLE);
                    ivAnalyticsWellnessExpand.setImageResource(R.drawable.ic_expand_less_black);
                } else {
                    layoutAnalyticsWellness.setVisibility(View.GONE);
                    ivAnalyticsWellnessExpand.setImageResource(R.drawable.ic_expand_more_black);

                }
            }
        });

        titleAnalyticsTendency = findViewById(R.id.titleAnalyticsTendency);
        ivAnalyticsTendencyExpand = (ImageView)findViewById(R.id.ivAnalyticsTendencyExpand);
        layoutAnalyticsTendency = findViewById(R.id.layoutAnalyticsTendency);
        spnLifeTendencyThings = (Spinner)findViewById(R.id.spnLifeTendencyThings);
        spnTendencyType = (Spinner)findViewById(R.id.spnTendencyType);
        tvTendencyResult = (TextView)findViewById(R.id.tvTendencyResult);
        layoutAnalyticsTendency.setVisibility(View.GONE);
        titleAnalyticsTendency.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (layoutAnalyticsTendency.getVisibility() == View.GONE) {
                    layoutAnalyticsTendency.setVisibility(View.VISIBLE);
                    ivAnalyticsTendencyExpand.setImageResource(R.drawable.ic_expand_less_black);
                } else {
                    layoutAnalyticsTendency.setVisibility(View.GONE);
                    ivAnalyticsTendencyExpand.setImageResource(R.drawable.ic_expand_more_black);

                }
            }
        });

        titleAnalyticsCorrelation = findViewById(R.id.titleAnalyticsCorrelation);
        ivAnalyticsCorrelationExpand = (ImageView)findViewById(R.id.ivAnalyticsCorrelationExpand);
        layoutAnalyticsCorrelation = findViewById(R.id.layoutAnalyticsCorrelation);
        spnCorrelationThingX = (Spinner)findViewById(R.id.spnCorrelationThingX);
        spnCorrelationThingY = (Spinner)findViewById(R.id.spnCorrelationThingY);
        tvCorrelationResult = (TextView)findViewById(R.id.tvCorrelationResult);
        layoutAnalyticsCorrelation.setVisibility(View.GONE);
        titleAnalyticsCorrelation.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (layoutAnalyticsCorrelation.getVisibility() == View.GONE) {
                    layoutAnalyticsCorrelation.setVisibility(View.VISIBLE);
                    ivAnalyticsCorrelationExpand.setImageResource(R.drawable.ic_expand_less_black);
                } else {
                    layoutAnalyticsCorrelation.setVisibility(View.GONE);
                    ivAnalyticsCorrelationExpand.setImageResource(R.drawable.ic_expand_more_black);

                }
            }
        });

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.analytics_lifestyle_things, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnLifeActivityThings.setAdapter(adapter);

        adapter = ArrayAdapter.createFromResource(this,
                R.array.analytics_period, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnLifeActivityPeriod.setAdapter(adapter);

        adapter = ArrayAdapter.createFromResource(this,
                R.array.analytics_wellness_type, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnWellnessType.setAdapter(adapter);

        adapter = ArrayAdapter.createFromResource(this,
                R.array.analytics_period, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnWellnessPeriod.setAdapter(adapter);

        adapter = ArrayAdapter.createFromResource(this,
                R.array.analytics_things, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnLifeTendencyThings.setAdapter(adapter);

        adapter = ArrayAdapter.createFromResource(this,
                R.array.analytics_tendency_type, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnTendencyType.setAdapter(adapter);

        adapter = ArrayAdapter.createFromResource(this,
                R.array.analytics_things, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnCorrelationThingX.setAdapter(adapter);

        adapter = ArrayAdapter.createFromResource(this,
                R.array.analytics_things, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnCorrelationThingY.setAdapter(adapter);

        tvLifeActivityResult.setText(Html.fromHtml("<font color='#3F51B5'>Activity Pattern in the past year</font><br/>" +
                "<font color='#ff4081'>exercise</font><br/>" +
                "You have exercised every morning in the past year.<br/>" +
                "<font color='#ff4081'>cook</font><br/>" +
                "You have usually cooked for the Saturday dinner."));
        tvWellnessResult.setVisibility(View.GONE);
        tvTendencyResult.setVisibility(View.GONE);
        tvCorrelationResult.setVisibility(View.GONE);


        dlgAlert = new AlertDialog.Builder(this).setMessage("Message")
                .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                }).create();
    }

    public void onAnalyzeClick(View v) {
        switch (v.getId()) {
//            case R.id.btnLifeStyleAnalyze:
//                JSONObject reqJson = new JSONObject();
//                try {
//                    reqJson.put("user_id", UserProfile.getUserProfile().getUserID());
//                    reqJson.put("thing_type", "food");
//                    reqJson.put("option", "like");
//                } catch (Exception e) {}
//                progressLifeStyle.setVisibility(View.VISIBLE);
//                analyzeLifeStyleConnector.request(reqJson);
//                return;
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
