package ssu.sel.smartdiary.model;

import android.text.TextUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by hanter on 2016. 10. 20..
 */

public class DiaryEnvContext {
    public static final String TYPE_ENV_PLACE = "place";
    public static final String TYPE_ENV_WEATHER = "weather";
    public static final String TYPE_ENV_HOLIDAY = "holiday";
    public static final String TYPE_ENV_EVENT = "event";

    private final long contextId;
    private final String type;
    private String value;

    public DiaryEnvContext(long contextId, String type, String value) {
        this.contextId = contextId;
        this.type = type;
        this.value = value;
    }

    public static DiaryEnvContext fromJSON(JSONObject json) {
        DiaryEnvContext diaryEnvContext = null;
        try {
            long contextId = json.getLong("ec_id");
            String type = json.getString("type");
            String value = json.getString("value");

            if (TYPE_ENV_PLACE.equals(type)) {
                diaryEnvContext = new DiaryEnvPlace(contextId, value);
            } else if (TYPE_ENV_WEATHER.equals(type)) {
                diaryEnvContext = new DiaryEnvWeather(contextId, value);
            } else if (TYPE_ENV_HOLIDAY.equals(type)) {
                diaryEnvContext = new DiaryEnvHoliday(contextId, value);
            } else if (TYPE_ENV_EVENT.equals(type)) {
                diaryEnvContext = new DiaryEnvEvent(contextId, value);
            } else {
                diaryEnvContext = new DiaryEnvContext(contextId, type, value);
            }
        } catch (JSONException je) {
            je.printStackTrace();
        }
        return diaryEnvContext;
    }

    public static String diaryContextsToString(ArrayList<DiaryEnvContext> diaryEnvContexts) {
        String string = "";
        for (DiaryEnvContext diaryEnvContext : diaryEnvContexts) {
            if (TextUtils.isEmpty(string)) {
                string = diaryEnvContext.getValue();
            } else {
                string += ", " + diaryEnvContext.getValue();
            }
        }
        return string;
    }

    public long getContextId() {
        return contextId;
    }

    public String getType() {
        return type;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
