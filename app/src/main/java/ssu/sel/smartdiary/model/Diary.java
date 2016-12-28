package ssu.sel.smartdiary.model;

import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;

/**
 * Created by hanter on 16. 10. 7..
 */
public class Diary {
    private int diaryID = -1;
    private String title;
    private Calendar date;
    private String content;
    private ArrayList<String> diaryTags = null;
    private ArrayList<DiaryEnvContext> diaryEnvContexts = null;

    public Diary(int diaryID, String title, Calendar date, String content) {
        this.diaryID = diaryID;
        this.title = title;
        this.date = date;
        this.content = content;
        this.diaryTags = new ArrayList<>();
        this.diaryEnvContexts = new ArrayList<>();
    }

    public Diary(int diaryID, String title, Calendar date, String content,
                 Collection<String> diaryTags,
                 Collection<DiaryEnvContext> diaryEnvContexts) {
        this.diaryID = diaryID;
        this.title = title;
        this.date = date;
        this.content = content;
        this.diaryTags = new ArrayList<>();
        this.diaryTags.addAll(diaryTags);
        this.diaryEnvContexts = new ArrayList<>();
        this.diaryEnvContexts.addAll(diaryEnvContexts);
    }

    public static Diary fromJSON(JSONObject jsonDiary, JSONArray jsonTags, JSONArray jsonEnvContexts) {
        Diary diary = null;
        try {
            long time = jsonDiary.getLong("created_date");
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(time);

            diary = new Diary(jsonDiary.getInt("audio_diary_id"), jsonDiary.getString("title"),
                    calendar, jsonDiary.getString("content"));

            for (int i=0; i<jsonTags.length(); i++) {
                JSONObject jsonTag = jsonTags.getJSONObject(i);
                String tag = jsonTag.getString("value");
                diary.addDiaryTag(tag);
            }

            for (int i = 0; i < jsonEnvContexts.length(); i++) {
                JSONObject jsonContext = jsonEnvContexts.getJSONObject(i);
                DiaryEnvContext diaryEnvContext = DiaryEnvContext.fromJSON(jsonContext);
                if (diaryEnvContext != null) {
                    diary.addDiaryEnvContext(diaryEnvContext);
                }
            }
        } catch (JSONException je) {
            je.printStackTrace();
        }
        return diary;
    }

    public JSONObject toJSON() {
        return null;
    }

    public ArrayList<DiaryEnvContext> getDiaryContexts(String type) {
        ArrayList<DiaryEnvContext> searchContexts = new ArrayList<>();
        for (DiaryEnvContext context : diaryEnvContexts) {
            if(context.getType().equals(type)) {
                searchContexts.add(context);
            }
        }
        return searchContexts;
    }

    public int getDiaryID() {
        return diaryID;
    }

    public void setDiaryID(int diaryID) {
        this.diaryID = diaryID;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Calendar getDate() {
        return date;
    }

    public void setDate(Calendar date) {
        this.date = date;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public ArrayList<DiaryEnvContext> getDiaryEnvContexts() {
        return diaryEnvContexts;
    }

    public DiaryEnvContext getDiaryEnvContext(int index) {
        return diaryEnvContexts.get(index);
    }

    public void addDiaryEnvContext(DiaryEnvContext diaryEnvContext) {
        diaryEnvContexts.add(diaryEnvContext);
    }

    public void addDiaryEnvContexts(Collection<DiaryEnvContext> diaryEnvContexts) {
        this.diaryEnvContexts.addAll(diaryEnvContexts);
    }

    public ArrayList<String> getDiaryTags() {
        return diaryTags;
    }

    public String getDiaryTag(int index) {
        return diaryTags.get(index);
    }

    public String getDiaryTagsString() {
        String tagsString = "";
        for (int i=0; i<diaryTags.size(); i++) {
            if (i>0) {
                tagsString += ", ";
            }
            tagsString += diaryTags.get(i);
        }
        return tagsString;
    }

    public void addDiaryTag(String tag) {
        diaryTags.add(tag);
    }

    public void addDiaryTags(Collection<String> tags) {
        this.diaryTags.addAll(tags);
    }
}
