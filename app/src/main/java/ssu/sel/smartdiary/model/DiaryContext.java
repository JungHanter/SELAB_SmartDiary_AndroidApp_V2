package ssu.sel.smartdiary.model;

import android.text.TextUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by hanter on 2016. 10. 20..
 */

public class DiaryContext {
    public static final String CONTEXT_TYPE_ANNOTATION = "annotation";
    public static final String CONTEXT_TYPE_ENVIRONMENT = "environment";
    public static final String SUB_TYPE_ENV_PLACE = "place";
    public static final String SUB_TYPE_ENV_WEATHER = "weather";
    public static final String SUB_TYPE_ENV_EVENT = "event";

    private final long contextId;
    private final String contextType;
    private final String subType;
    private String value;

    public DiaryContext(long contextId, String contextType, String subType, String value) {
        this.contextId = contextId;
        this.contextType = contextType;
        this.subType = subType;
        this.value = value;
    }

    public static DiaryContext fromJSON(JSONObject json) {
        DiaryContext diaryContext = null;
        try {
            long contextId = json.getLong("diary_context_id");
            String contextType = json.getString("type");
            String subType = json.getString("subtype");
            String value = json.getString("value");

            if (CONTEXT_TYPE_ANNOTATION.equals(contextType)) {
                diaryContext = new DiaryAnnotation(contextId, value);
            } else if (CONTEXT_TYPE_ENVIRONMENT.equals(contextType)) {
                if (SUB_TYPE_ENV_PLACE.equals(subType)) {
                    diaryContext = new DiaryPlace(contextId, value);
                } else if (SUB_TYPE_ENV_WEATHER.equals(subType)) {
                    diaryContext = new DiaryWeather(contextId, value);
                } else if (SUB_TYPE_ENV_EVENT.equals(subType)) {
                    diaryContext = new DiaryEvent(contextId, value);
                } else {
                    diaryContext = new DiaryEnvironment(contextId, subType, value);
                }
            } else {
                diaryContext = new DiaryContext(contextId, contextType, subType, value);
            }

        } catch (JSONException je) {
            je.printStackTrace();
        }
        return diaryContext;
    }

    public static String diaryContextsToString(ArrayList<DiaryContext> diaryContexts) {
        String string = "";
        for (DiaryContext diaryContext : diaryContexts) {
            if (TextUtils.isEmpty(string)) {
                string = diaryContext.getValue();
            } else {
                string += ", " + diaryContext.getValue();
            }
        }
        return string;
    }

    public long getContextId() {
        return contextId;
    }

    public String getContextType() {
        return contextType;
    }

    public String getSubType() {
        return subType;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
