package ssu.sel.smartdiary;

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
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.Iterator;

import ssu.sel.smartdiary.model.UserProfile;
import ssu.sel.smartdiary.network.JsonRestConnector;

public class AnalyticsActivity extends AppCompatActivity {

    private View titleAnalyticsLifeActivity = null;
    private ImageView ivLifeActivityExpand = null;
    private View layoutAnalyticsLifeActivity = null;
    private Spinner spnLifeActivityPeriod = null;
    private TextView tvLifeActivityResult = null;
    private ProgressBar progressLifeActivity = null;

    private View titleAnalyticsTendency = null;
    private ImageView ivAnalyticsTendencyExpand = null;
    private View layoutAnalyticsTendency = null;
    private Spinner spnTendencyPeriod = null;
    private TextView tvTendencyResult = null;
    private ProgressBar progressTendency = null;

    private View titleAnalyticsWellness = null;
    private ImageView ivAnalyticsWellnessExpand = null;
    private View layoutAnalyticsWellness = null;
    private Spinner spnWellnessType = null;
    private Spinner spnWellnessPeriod = null;
    private TextView tvWellnessResult = null;

    private View titleAnalyticsCorrelation = null;
    private ImageView ivAnalyticsCorrelationExpand = null;
    private View layoutAnalyticsCorrelation = null;
    private Spinner spnCorrelationThingX = null;
    private Spinner spnCorrelationThingY = null;
    private TextView tvCorrelationResult = null;


    protected AlertDialog dlgAlert = null;

    private JsonRestConnector analyzeTendencyConnector = null;
    private JsonRestConnector analyzeLifeActivityConnector = null;


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
        progressLifeActivity = (ProgressBar)findViewById(R.id.progressLifeActivity);

        titleAnalyticsTendency = findViewById(R.id.titleAnalyticsTendency);
        ivAnalyticsTendencyExpand = (ImageView)findViewById(R.id.ivAnalyticsTendencyExpand);
        layoutAnalyticsTendency = findViewById(R.id.layoutAnalyticsTendency);
        spnTendencyPeriod = (Spinner)findViewById(R.id.spnTendencyPeriod);
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
        progressTendency = (ProgressBar)findViewById(R.id.progressTendency);

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
                R.array.analytics_period, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnTendencyPeriod.setAdapter(adapter);

        adapter = ArrayAdapter.createFromResource(this,
                R.array.analytics_correlation_aspects, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnCorrelationThingX.setAdapter(adapter);

        adapter = ArrayAdapter.createFromResource(this,
                R.array.analytics_correlation_aspects, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnCorrelationThingY.setAdapter(adapter);

//        tvLifeActivityResult.setText(Html.fromHtml("<font color='#3F51B5'>Activity Pattern in the past year</font><br/>" +
//                "<font color='#ff4081'>exercise</font><br/>" +
//                "You have exercised every morning in the past year.<br/>" +
//                "<font color='#ff4081'>cook</font><br/>" +
//                "You have usually cooked for the Saturday dinner."));
        tvLifeActivityResult.setVisibility(View.GONE);
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


        analyzeTendencyConnector = new JsonRestConnector("analyze/tendency", "GET",
                new JsonRestConnector.OnConnectListener() {
                    @Override
                    public void onDone(JSONObject resJson) {
                        if (resJson == null) {
                            Log.d("Main - Json", "No response");
                            openAlertModal("No response.", "Error");
                        } else {
                            try {
                                boolean success = resJson.getBoolean("analyzed");
                                if (success) {
                                    JSONObject result = resJson.getJSONObject("result");
                                    Log.d("Analytics Activity", result.toString());

                                    StringBuilder resultString = new StringBuilder();
                                    JSONObject posResult = result.getJSONObject("pos");
                                    Iterator<String> posType = posResult.keys();
                                    while (posType.hasNext()) {
                                        String type = posType.next();

                                        JSONArray likeArray = posResult.getJSONArray(type);
                                        if (likeArray.length() > 0) {
                                            resultString.append("Most liked " + type).append(":\n");
                                            for (int i=0; i<likeArray.length(); i++) {
                                                JSONArray ta = likeArray.getJSONArray(i);
                                                String name = ta.getString(0);
                                                double value = ta.getDouble(1);
                                                resultString.append(name).append(" (");
                                                String valueString = "Slightly Like";
                                                if (value == 1) {
                                                    valueString = "Absolutely Like";
                                                } else if (value >= 0.8) {
                                                    valueString = "Very Like";
                                                } else if (value >= 0.6) {
                                                    valueString = "More Like";
                                                } else if (value >= 0.4) {
                                                    valueString = "Like";
                                                }
                                                value = ((int)(value*1000))/1000.0;
                                                resultString.append(valueString).append("; ")
                                                        .append(value).append(")\n");
                                            }
                                            resultString.append('\n');
                                        }
                                    }
                                    JSONObject negResult = result.getJSONObject("neg");
                                    Iterator<String> negType = negResult.keys();
                                    while (negType.hasNext()) {
                                        String type = negType.next();

                                        JSONArray dislikeArray = negResult.getJSONArray(type);
                                        if (dislikeArray.length() > 0) {
                                            resultString.append("Most disliked " + type).append(":\n");
                                            for (int i=0; i<dislikeArray.length(); i++) {
                                                JSONArray ta = dislikeArray.getJSONArray(i);
                                                String name = ta.getString(0);
                                                double value = ta.getDouble(1);
                                                resultString.append(name).append(" (");
                                                String valueString = "Slightly Dislike";
                                                if (value == -1) {
                                                    valueString = "Absolutely Dislike";
                                                } else if (value <= -0.8) {
                                                    valueString = "Very Dislike";
                                                } else if (value <= -0.6) {
                                                    valueString = "More Dislike";
                                                } else if (value <= -0.4) {
                                                    valueString = "Dislike";
                                                }
                                                value = ((int)(value*1000))/1000.0;
                                                resultString.append(valueString).append("; ")
                                                        .append(value).append(")\n");
                                            }
                                            resultString.append('\n');
                                        }
                                    }

                                    tvTendencyResult.setText(resultString.toString());
                                    tvTendencyResult.setVisibility(View.VISIBLE);
                                } else {
                                    openAlertModal("Analyze Failed");
                                    tvTendencyResult.setVisibility(View.GONE);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                                Log.d("Main - Json", "Json parsing error");
                                openAlertModal("Json parsing error.", "Error");
                                tvTendencyResult.setVisibility(View.GONE);
                            }
                        }
                        progressTendency.setVisibility(View.GONE);
                    }
                });
        analyzeTendencyConnector.setReadTimeOut(180000); // 3 min

        analyzeLifeActivityConnector = new JsonRestConnector("analyze/activity_pattern", "GET",
                new JsonRestConnector.OnConnectListener() {
                    @Override
                    public void onDone(JSONObject resJson) {
                        if (resJson == null) {
                            Log.d("Main - Json", "No response");
                            openAlertModal("No response.", "Error");
                        } else {
                            try {
                                boolean success = resJson.getBoolean("analyzed");
                                if (success) {
                                    JSONObject result = resJson.getJSONObject("result");
                                    Log.d("Analytics Activity", result.toString());

                                    StringBuilder resultString = new StringBuilder();
                                    JSONArray recurrentList = result.getJSONArray("recurrent");
                                    if (recurrentList.length() > 0) {
                                        resultString.append("Recurrent Analysis:\n");
                                        for (int i=0; i<recurrentList.length(); i++) {
                                            JSONArray recItem = recurrentList.getJSONArray(i);
                                            String name = recItem.getString(0);
                                            double valuePercent = ((int)(recItem.getDouble(1)*1000))/10.0;
                                            resultString.append(name).append(": ").
                                                    append(valuePercent).append("%\n");
                                        }
                                        resultString.append('\n');
                                    }
                                    JSONArray frequencyList = result.getJSONArray("frequency");
                                    if (frequencyList.length() > 0) {
                                        resultString.append("Frequency Analysis:\n");
                                        for (int i=0; i<frequencyList.length(); i++) {
                                            JSONArray freqItem = frequencyList.getJSONArray(i);
                                            String name = freqItem.getString(0);
                                            JSONArray values = freqItem.getJSONArray(1);
                                            resultString.append(name).append(": ");
                                            for (int k=0; k<values.length(); k++) {
                                                int day = (int)(values.getDouble(k));
                                                if (values.length()>=2 && k==values.length()-1) {
                                                    resultString.append(" and ");
                                                } else if (k>0) {
                                                    resultString.append(", ");
                                                }
                                                resultString.append(day);
                                            }
                                            resultString.append(" days\n");
                                        }
                                        resultString.append('\n');
                                    }
                                    JSONArray regularityList = result.getJSONArray("regularity");
                                    if (regularityList.length() > 0) {
                                        resultString.append("Regularity Analysis:\n");
                                        for (int i=0; i<regularityList.length(); i++) {
                                            JSONArray regItem = regularityList.getJSONArray(i);
                                            String name = regItem.getString(0);
                                            JSONArray values = regItem.getJSONArray(1);
                                            resultString.append(name).append(": ");
                                            for (int k=0; k<values.length(); k++) {
                                                int day = (int)(values.getDouble(k));
                                                if (values.length()>=2 && k==values.length()-1) {
                                                    resultString.append(" and ");
                                                } else if (k>0) {
                                                    resultString.append(", ");
                                                }
                                                resultString.append(day);
                                            }
                                            resultString.append(" days\n");
                                        }
                                    }

                                    tvLifeActivityResult.setText(resultString.toString());
                                    tvLifeActivityResult.setVisibility(View.VISIBLE);
                                } else {
                                    openAlertModal("Analyze Failed");
                                    tvLifeActivityResult.setVisibility(View.GONE);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                                Log.d("Main - Json", "Json parsing error");
                                openAlertModal("Json parsing error.", "Error");
                                tvLifeActivityResult.setVisibility(View.GONE);
                            }
                        }
                        progressLifeActivity.setVisibility(View.GONE);
                    }
                });
        analyzeLifeActivityConnector.setReadTimeOut(180000);
    }

    public void onAnalyzeClick(View v) {
        JSONObject reqJson = new JSONObject();
        long endTime = Calendar.getInstance().getTimeInMillis();
        long startTime = 0;

        int periodPos = spnTendencyPeriod.getSelectedItemPosition();
        switch (v.getId()) {
        case R.id.btnTendencyAnalyze:
            periodPos = spnTendencyPeriod.getSelectedItemPosition();
            break;
        case R.id.btnLifeActivityAnalyze:
            periodPos = spnLifeActivityPeriod.getSelectedItemPosition();
            break;
        }
        switch (periodPos) {
        case 1:
            startTime = endTime - (1000l * 60l * 60l * 24l * 365l * 10l);
            break;
        case 2:
            startTime = endTime - (1000l * 60l * 60l * 24l * 365l * 5l);
            break;
        case 3:
            startTime = endTime - (1000l * 60l * 60l * 24l * 365l);
            break;
        case 4:
            startTime = endTime - (1000l * 60l * 60l * 24l * 30l * 6);
            break;
        case 5:
            startTime = endTime - (1000l * 60l * 60l * 24l * 30l * 3);
            break;
        case 6:
            startTime = endTime - (1000l * 60l * 60l * 24l * 30l);
            break;
        }

        try {
            reqJson.put("user_id", UserProfile.getUserProfile().getUserID());
            reqJson.put("timestamp_from", startTime);
//            reqJson.put("timestamp_to", endTime);
        } catch (Exception e) {}

        switch (v.getId()) {
            case R.id.btnTendencyAnalyze:
                progressTendency.setVisibility(View.VISIBLE);
                analyzeTendencyConnector.request(reqJson);
                return;

            case R.id.btnLifeActivityAnalyze:
                int daysInterval = 3;

                switch (periodPos) {
                    case 0: daysInterval = 0; break;
                    case 1: daysInterval = 30; break;
                    case 2: daysInterval = 30; break;
                    case 3: daysInterval = 14; break;
                    case 4: daysInterval = 7; break;
                    case 5: daysInterval = 7; break;
                    case 6: daysInterval = 3; break;
                }
                Log.d("LifeActivityAnalytics", "Days Interval " + daysInterval);
                try {
                    reqJson.put("user_id", UserProfile.getUserProfile().getUserID());
                    reqJson.put("timestamp_from", startTime);
                    reqJson.put("interval", daysInterval);
                } catch (Exception e) {}

                progressLifeActivity.setVisibility(View.VISIBLE);
                analyzeLifeActivityConnector.request(reqJson);
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
